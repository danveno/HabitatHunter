package edu.arizona.biosemantics.habitat.eval;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.bb.BBEntity;
import edu.arizona.biosemantics.bb.BBEntityLink;
import edu.arizona.biosemantics.habitat.io.A2FormatFileUtil;

public class EntityEvaluation {
	
	private A2FormatFileUtil a2FileUtil = new A2FormatFileUtil();
	
	private int match; //TruePositive
	private int partialMatchNum;
	private double partialMatch;//Substitution, Partial Match
	private int deletion;//False Negative
	private int insertion;//False Positive
	
	private double precision;
	private double recall;
	
	private int linkMatch;
	private int linkDeletion;
	private int linkInsertion;
	
	private String[] labels;
	private Map<String, Integer> labelIndex;
	private double[] correct;
	private double[] gsd;
	private double[] predict;
	
	
	public EntityEvaluation(String[] labels){
		this.labels = labels;
		correct = new double[labels.length];
		gsd = new double[labels.length];
		predict = new double[labels.length];
		
		labelIndex = new HashMap();
		for(int i=0;i<labels.length;i++){
			labelIndex.put(labels[i], i);
		}
	}
	
	/**
	 * evaluate the whole dataset
	 * @param refFolder
	 * @param predictFolder
	 */
	public void evalDataset(String refFolder, String predictFolder){
		File refFiles = new File(refFolder);
		File[] files = refFiles.listFiles();
		for(File refFile : files){
			if(!refFile.getName().endsWith(".a2")) continue;
			String preFile = predictFolder+"/"+refFile.getName();
			System.out.println(preFile);
			//if(preFile.indexOf("10496597")>-1)
			this.evalFile(refFile.getPath(), preFile);
		}
	}

	/**
	 * evaluate for one file
	 * Note that the offset information must be kept.
	 *  reference: Structured and Extended Named Entity Evaluation in Automatic Speech Transcriptions
	 * @param referenceFile
	 * @param predictFile
	 */
	public void evalFile(String referenceFile, String predictFile){
		List<BBEntity> refEntities = a2FileUtil.parseEntity(referenceFile);
		for(BBEntity entity:refEntities){
			if(entity.getType().equals("Title")||entity.getType().equals("Paragraph")) continue;
			gsd[labelIndex.get(entity.getType())]++;
		}
		List<BBEntity> preEntities = a2FileUtil.parseEntity(predictFile);
		for(BBEntity entity:preEntities){
			if(entity.getType().equals("Title")||entity.getType().equals("Paragraph")) continue;
			predict[labelIndex.get(entity.getType())]++;
		}
		//evaluateNER(refEntities, preEntities);
		
		List<BBEntityLink> refEntityLinks = a2FileUtil.parseA2File(referenceFile);
		Map<BBEntity, String> refEntityOntoTerms = parseEntityClass(refEntities, refEntityLinks);
		List<BBEntityLink> preEntityLinks = a2FileUtil.parseA2File(predictFile);
		Map<BBEntity, String> preEntityOntoTerms = parseEntityClass(preEntities, preEntityLinks);
		
		evaluateNERAndLink(refEntities,preEntities,refEntityOntoTerms,preEntityOntoTerms);
	}
	
	/**
	 * find the ontology class of the entity
	 * @param entities
	 * @param entityLinks
	 * @return
	 */
	public Map<BBEntity, String> parseEntityClass(List<BBEntity> entities,
			List<BBEntityLink> entityLinks) {
		Map<BBEntity, String> eneityOntoTerms = new HashMap();
		for(BBEntity entity:entities){
			for(BBEntityLink entityLink:entityLinks){
				if(entity.geteID().equals(entityLink.geteID())){
					eneityOntoTerms.put(entity, entityLink.getOntologyClass());
				}
			}
		}
		return eneityOntoTerms;
	}

	/**
	 * evaluate NER entities
	 * @param refEntities
	 * @param preEntities
	 */
	public void evaluateNER(List<BBEntity> refEntities, List<BBEntity> preEntities){
		//paring
		Set<BBEntity> refMatched = new HashSet();
		Set<BBEntity> preMatched = new HashSet();
		
		//System.out.println("gold standard entities="+refEntities.size());
		//System.out.println("test entities="+preEntities.size());
		for(BBEntity refEntity:refEntities){
			if(refEntity.getType().equals("Title")||refEntity.getType().equals("Paragraph")) continue;
			boolean found = false;
			for(BBEntity preEntity:preEntities){
				if(preEntity.getType().equals("Title")||preEntity.getType().equals("Paragraph")) continue;
				double sp = scoreOfPair(refEntity,preEntity);//boundary function
				if(sp==0){
					continue;
				}else if(sp==1){
					match++;
					refMatched.add(refEntity);
					preMatched.add(preEntity);
					System.out.println("perfect match:"+refEntity.getType()+":"+refEntity.getName()+":"+preEntity.getName());
				}else if(sp>0){
					partialMatchNum++;
					partialMatch+=sp;
					
					refMatched.add(refEntity);
					preMatched.add(preEntity);
					
					System.out.println("partitial match:"+refEntity.getType()+":"+refEntity.getName()+":"+preEntity.getName());
				}
				if(!found) found = true;
				//double cscore = normalizationScore(refEntity,preEntity);
			}
			
			if(!found) System.out.println("missing:"+refEntity.getType()+":"+refEntity.getName()+":");
		}
		
		//get the number of deletion and insertion
		deletion +=  refEntities.size() - refMatched.size();
		insertion += preEntities.size() - preMatched.size();
	}
	
	
	/**
	 * evaluate NER entities
	 * @param refEntities
	 * @param preEntities
	 */
	public void evaluateNERAndLink(List<BBEntity> refEntities, List<BBEntity> preEntities,
			Map<BBEntity, String> refEntityOntoTerms,Map<BBEntity, String> preEntityOntoTerms){
		//paring
		Set<BBEntity> refMatched = new HashSet();
		Set<BBEntity> preMatched = new HashSet();
		
		//System.out.println("gold standard entities="+refEntities.size());
		//System.out.println("test entities="+preEntities.size());
		for(BBEntity refEntity:refEntities){
			if(refEntity.getType().equals("Title")||refEntity.getType().equals("Paragraph")) continue;
			String entityType = refEntity.getType();
			boolean found = false;
//			System.out.println("current entity:"+refEntity.getName());
			//find exact match
			for(BBEntity preEntity:preEntities){
				if(preEntity.getType().equals("Title")||preEntity.getType().equals("Paragraph")) continue;
				double sp = scoreOfPair(refEntity,preEntity);//boundary function
				String refClass = refEntityOntoTerms.get(refEntity);
				String preClass = preEntityOntoTerms.get(preEntity);
				boolean sameClass = refClass.equals(preClass);
				//System.out.println("OntoClass:"+preEntity.getName()+":"+sp);
				if(sp==0){
					continue;
				}else if(sp==1){//exact match
					match++;
					if(sameClass){
						linkMatch++;
						correct[labelIndex.get(entityType)]++;
					}
					refMatched.add(refEntity);
					preMatched.add(preEntity);
					System.out.print("perfect match:"+refEntity.getType()+":"+refEntity.getName()+":"+preEntity.getName());
					System.out.println("\tOntoClass:"+sameClass+":"+refClass+":"+preClass);
					found = true;
				}
				//double cscore = normalizationScore(refEntity,preEntity);
			}
			
			
			//find partial match
			if(!found){
				for(BBEntity preEntity:preEntities){
					if(preEntity.getType().equals("Title")||preEntity.getType().equals("Paragraph")) continue;
					double sp = scoreOfPair(refEntity,preEntity);//boundary function
					String refClass = refEntityOntoTerms.get(refEntity);
					String preClass = preEntityOntoTerms.get(preEntity);
					boolean sameClass = refClass.equals(preClass);
					//System.out.println("OntoClass:"+sameClass+":"+refClass+":"+preClass);
					if(sp==0){
						continue;
					}else if(sp>0&&sp<1){//partial match
						partialMatchNum++;
						partialMatch+=sp;
						
						if(sameClass) linkMatch++;
						
						refMatched.add(refEntity);
						preMatched.add(preEntity);
						
						System.out.print("partitial match:"+refEntity.getType()+":"+refEntity.getName()+":"+preEntity.getName());
						System.out.println("\tOntoClass:"+sameClass+":"+refClass+":"+preClass);
						found = true;
					}
					//double cscore = normalizationScore(refEntity,preEntity);
				}
			}
			
			if(!found) System.out.println("missing:"+refEntity.getType()+":"+refEntity.getName()+":");
		}
		
		//get the number of deletion and insertion
		deletion +=  refEntities.size() - refMatched.size();
		insertion += preEntities.size() - preMatched.size();
		
		for(int i=0;i<labels.length;i++){
			System.out.println(labels[i]+"correct:"+correct[labelIndex.get(labels[i])]+"/"+predict[labelIndex.get(labels[i])]+"/"+gsd[labelIndex.get(labels[i])]+" P:"+correct[labelIndex.get(labels[i])]/predict[labelIndex.get(labels[i])]+" R:"+correct[labelIndex.get(labels[i])]/gsd[labelIndex.get(labels[i])]);
		}
		
		System.out.println("Overall P:"+linkMatch/sum(predict)+" R:"+linkMatch/sum(gsd));
	}
	
	
	public double sum(double[] values){
		double sum=0;
		for(int i=0;i<labels.length;i++){
			sum+=values[i];
		}
		return sum;
	}
	
	/**
	 * the scores of a pair, The boundary function
	 * the Jaccard of the names of the pair
	 * the boundary information of the pair 
	 * 
	 * @param refEntity
	 * @param preEntity
	 * @return
	 */
	public double scoreOfPair(BBEntity refEntity, BBEntity preEntity){
		
		//1, jaccard should not be 0, 
		double jaccard = jaccardByLCSCharacter(refEntity.getName(), preEntity.getName());
		
		//2, the boundary should have overlap
		boolean boundaryOverLapped = boundaryOverLapped(refEntity, preEntity);
		
		if(boundaryOverLapped){
			return jaccard;
		}else{
			return 0;
		}
	}
	
	
	public boolean boundaryOverLapped(BBEntity refEntity, BBEntity preEntity) {
		int refStart =  refEntity.getStart();
		int refEnd = refEntity.getEnd();
		
		int refStart2 =  refEntity.getStart2();
		int refEnd2 = refEntity.getEnd2();
		
		int preStart =  preEntity.getStart();
		int preEnd = preEntity.getEnd();
		
		if(refStart2==-1){//only one substring
			if(preStart>refEnd||preEnd<refStart) return false;
		}else{//two substrings
			if(preStart>refEnd2||preEnd<refStart||preStart>refEnd&&preEnd<=refStart2) return false; 
		}
		
		return true;
	}


	/**
	 * jaccard index by computing LCS
	 * @param str1
	 * @param str2
	 * @return
	 */
	public double jaccardByLCSToken(String str1, String str2){
		String[] str1Fields = str1.split("[\\s]+");
		String[] str2Fields = str2.split("[\\s]+");
		
		String LCS = LongestCommonSubstring.longestSubstring(str1, str2);
		int common = "".equals(LCS)?0:LCS.split("[\\s]+").length;
		
		return common/(str1Fields.length+str2Fields.length-common+0.0);
	}
	
	/**
	 * jaccard index by computing LCS
	 * @param str1
	 * @param str2
	 * @return
	 */
	public double jaccardByLCSCharacter(String str1, String str2){
		
		String LCS = LongestCommonSubstring.longestSubstring(str1, str2);
		int common = "".equals(LCS)?0:LCS.length();
		
		return common/(str1.length()+str2.length()-common+0.0);
	}
	
	
	/**
	 * For Bacteria: If the predicted taxon identifier is the same as the reference C = 1, otherwise C = 0.
	 * For Habitat: C = Wang(0,65).
	 * @return
	 */
	public double normalizationScore(BBEntityLink refLink, BBEntityLink preLink){
		String ontology = refLink.getOntology();
		double norScore = 0;
		
		String refontoClass = refLink==null?null:refLink.getOntologyClass();
		String preOntoClass = preLink==null?null:preLink.getOntologyClass();
		if("NCBI_Taxonomy".equals(ontology)){//bacteria
			norScore = refontoClass.equals(preOntoClass)?1:0;
		}else{//Habitat, apply Wang similarity
			//TODO:
		}
		
		return norScore;
	}
	
	
	public static void main(String[] args){
		String str1 = "or we can start a longer substring. See the java code (mainly from wikipedia) for yourself:";
		String str2 = "we can";
		
		EntityEvaluation eval = new EntityEvaluation(new String[]{"Bacteria","Habitat"});
		//System.out.println(eval.jaccardByLCSCharacter(str1, str2));
		
		
		String clfRsFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\clsresults\\BB-cat+ner-47483.a2";
		
		String gsdA1File ="F:\\Habitat\\BacteriaBiotope\\2016\\BioNLP-ST-2016_BB-cat+ner_dev\\BB-cat+ner-47483.a1";
		
		//eval.evalFile(gsdA1File, clfRsFile);
		
		//String clfRsFolder = "F:\\Habitat\\BacteriaBiotope\\experiments\\crfresults";
		String clfRsFolder = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\submission";
		String gsdA1Folder ="F:\\Habitat\\BacteriaBiotope\\2016eval\\BioNLP-ST-2016_BB-cat+ner_dev";
		eval.evalDataset(gsdA1Folder, clfRsFolder);
		System.out.println("match="+eval.match+"\npartialMatchNum="+eval.partialMatchNum+"\ndeletion="+eval.deletion+"\ninsertion="+eval.insertion);
	}
}

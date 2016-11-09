package edu.arizona.biosemantics.bb;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.habitat.io.A1FormatFileUtil;
import edu.arizona.biosemantics.habitat.io.A2FormatFileUtil;
import edu.arizona.biosemantics.habitat.ontology.OntologyManager;
import edu.arizona.biosemantics.habitat.ontology.OntologyTerm;
import edu.arizona.biosemantics.habitat.ontology.ncbi.NamesParser;
import edu.arizona.biosemantics.habitat.ontomap.OntologyMapping;
import edu.arizona.biosemantics.habitat.term.AcronymDetector;
import edu.arizona.biosemantics.habitat.term.AcronymGenerator;
import edu.arizona.biosemantics.habitat.term.PartialCoreferenceFinder;
import edu.arizona.biosemantics.habitat.term.TaxonNameTool;
import edu.arizona.biosemantics.util.EditDistance;


/**
 * 
 * @author maojin
 *
 */
public class BacteriaLinkToOntology {
	
	private AcronymDetector acroDetector = new AcronymDetector();
	private AcronymGenerator acroGenerator = new AcronymGenerator();
	
	private OntologyManager targetOntology; //the target ontology used to map
	
	private PartialCoreferenceFinder partCorfFinder = new PartialCoreferenceFinder();
	private OntologyMapping ontoMapping;
	
	
	public BacteriaLinkToOntology(OntologyManager targetOntology){
		this.targetOntology = targetOntology;
		this.ontoMapping = new OntologyMapping(this.targetOntology);
	}
	
	public BacteriaLinkToOntology(){
		//read NCBI Taxonomy ontology
		NamesParser ncbiNameParser = new NamesParser();
		ncbiNameParser.parse(Config.ncbiNamesDmp);
		this.targetOntology = ncbiNameParser.getOntology();
		this.ontoMapping = new OntologyMapping(this.targetOntology);
	}
	
	
	
	public Map<BBEntity, OntologyTerm> normalizeEntities(List<BBEntity> bbentities) {
		List<BBEntity> entities = new ArrayList();
		for(BBEntity entity:bbentities){
			if(entity.getType().equals("Bacteria")) entities.add(entity);
		}
		System.out.println("In total, there are "+entities.size()+" bacteria entities");
		
		//detect acronyms
		List<BBEntity> acronyms = new ArrayList();
		for(BBEntity entity:entities){
			if(acroDetector.isAcronym(entity.getName())){
				if(entity.getName().split(" ").length>1) entity.setName(TaxonNameTool.getGenusAndSpeciesName(entity.getName()));
				acronyms.add(entity);
			}
		}
		
		//Find the original term for acronyms
		//TODO: improve
		Map<BBEntity, String> acronymOrigins = new HashMap();
		for(BBEntity acron:acronyms){
			String acronName = acron.getName();
			//map global Abbrieviations
			String originalTerm = acroDetector.findGlobalAcronym(acron.getName());
			
			if(originalTerm!=null){
				//System.out.println(acron.getName()+"===>"+originalTerm);
				acronymOrigins.put(acron, originalTerm);
			}else{
				 originalTerm = originalTermInDoc(acron, entities,"Bacteria");
				if(originalTerm!=null){
					//System.out.println(acron.getName()+" GLOBAL ===>"+acroDetector.findGlobalAcronym(acron.getName()));
					acronymOrigins.put(acron, originalTerm); 
				}
			}
			
			//L. lactis subsp. cremoris===> Lactococcus 
			//System.out.println(acronName);
			if(originalTerm==null&&(acronName.charAt(1)=='.'||acronName.charAt(1)==' ')){
				String firstTerm = freqestFirstLetterInDoc(acron, entities);
//						System.out.println(acron.getName()+" First ===>"+firstTerm);
				if(firstTerm!=null){
					String[] words = acronName.split("[\\s]+");
					originalTerm = firstTerm;
					for(int i=1;i<words.length;i++){
						originalTerm+=" "+words[i];
					}
					acronymOrigins.put(acron, originalTerm); 
				}
				//System.out.println(acron.getName()+" acronym is ===>"+originalTerm);
			}
			
			//System.out.println(acronName+"  ===>"+originalTerm);
		}
		
		//System.out.println("Find acronyms  ===>"+acronymOrigins.size());
		
		//match
		Map<BBEntity, OntologyTerm> entityMatchedMap = this.mapBBEntityToNCBI(entities,acronymOrigins);
		//System.out.println(entityMatchedMap.size()+" entities are found from "+entities.size());
		
		System.out.println(entityMatchedMap.size()+" entities are found from "+entities.size());
		for(BBEntity entity:entities){
			OntologyTerm ontoTerm = entityMatchedMap.get(entity);
			if(ontoTerm!=null){
				ontoTerm.setOntology("NCBI_Taxonomy");
				System.out.println(entity.getName()+" "+ontoTerm.getClassId());
			}else{
				System.out.println(entity.getName()+" ");
			}
		}
		
		return entityMatchedMap;
	}

	
	
	/**
	 * using exact matching method to classify bacteria entities
	public void matchAllFiles(String bbentitiesFolder, String outputFolder){
		
		
		
		//read all entities
		List<BBEntity> entities = entityReader.readFromFolder(bbentitiesFolder);
		
		//organize entities by doc
		Map<String, List> docAllEntities = entityReader.getDocEntities(entities);
				
				
		for(int i=0;i<entities.size();){
			if(!entities.get(i).getType().equals("Bacteria")){
				entities.remove(i);
			}else{
				//preprocessing
				//remove mutat
				String noMutName = this.removeMutant(entities.get(i).getName());
				if(!"".equals(noMutName)){
					entities.get(i).setName(noMutName);
				}
				
				//System.out.println(entities.get(i).getDocID()+"\t"+entities.get(i).geteID()+"\t"+entities.get(i).getName());
				i++;
			}
		}
		
		//organize entities by doc
		Map<String, List> docEntityLists = entityReader.getDocEntities(entities);
		
		
		//detect acronyms
		List<BBEntity> acronyms = new ArrayList();
		for(BBEntity entity:entities){
			//System.out.println(entity.getDocID()+"\t"+entity.geteID()+"\t"+entity.getName());
			if(acroDetector.isAcronym(entity.getName())){
				if(entity.getName().split(" ").length>1) entity.setName(TaxonNameTool.getGenusAndSpeciesName(entity.getName()));
				acronyms.add(entity);
			}
		}
		
		//Find the original term for acronyms
		//TODO: improve
		Map<BBEntity, String> acronymOrigins = new HashMap();
		for(BBEntity acron:acronyms){
			String docId = acron.getDocID();
			String acronName = acron.getName();
			List docEntityList = docEntityLists.get(docId);
			String originalTerm = originalTermInDoc(acron, docEntityList);
			if(originalTerm!=null){
				//System.out.println(acron.getName()+"===>"+originalTerm);
				acronymOrigins.put(acron, originalTerm);
			}else{
				//map global Abbrieviations
				originalTerm = acroDetector.findGlobalAcronym(acron.getName());
				if(originalTerm!=null){
					//System.out.println(acron.getName()+" GLOBAL ===>"+acroDetector.findGlobalAcronym(acron.getName()));
					acronymOrigins.put(acron, originalTerm); 
				}
			}
			
			//L. lactis subsp. cremoris===> Lactococcus 
			//System.out.println(acronName);
			if(originalTerm==null&&(acronName.charAt(1)=='.'||acronName.charAt(1)==' ')){
				String firstTerm = freqestFirstLetterInDoc(acron, docEntityList);
//				System.out.println(acron.getName()+" First ===>"+firstTerm);
				if(firstTerm!=null){
					String[] words = acronName.split("[\\s]+");
					originalTerm = firstTerm;
					for(int i=1;i<words.length;i++){
						originalTerm+=" "+words[i];
					}
					acronymOrigins.put(acron, originalTerm); 
				}
			}
			
			//System.out.println(acronName+"  ===>"+originalTerm);
		}
		
		
		
		//match
		Map<BBEntity, OntologyTerm> entityMatchedMap = this.mapBBEntityToNCBI(entities,acronymOrigins,docEntityLists);
		//System.out.println(entityMatchedMap.size()+" entities are found from "+entities.size());
		
		System.out.println(entityMatchedMap.size()+" entities are found from "+entities.size());
		for(BBEntity entity:entities){
			OntologyTerm ontoTerm = entityMatchedMap.get(entity);
			if(ontoTerm!=null){
				ontoTerm.setOntology("NCBI_Taxonomy");
				System.out.println(entity.getName()+" "+ontoTerm.getClassId());
			}else{
				System.out.println(entity.getName()+" ");
			}
		}
		
		a2Util.generateSubmission(docAllEntities, entityMatchedMap, outputFolder, true);
	}*/
	
	
	/*
	 * find the original term for an acronym
	 */
	public String originalTermInDoc(BBEntity acronym, List<BBEntity> docEntityList, String entityType) {
		String acronymName = acronym.getName();
		for(BBEntity docEntity : docEntityList){
			//!acroDetector.isAcronym(docEntity.getName())&&
			if(!docEntity.geteID().equals(acronym.geteID())&&docEntity.getType().equals(entityType)
					&&!acronym.getName().equals(docEntity.getName())&&acronym.getName().length()<docEntity.getName().length()){
//				acroGenerator.setOrgTerm(docEntity.getName());
//				acroGenerator.generateAll();
//				Set<String> termAcronyms = acroGenerator.getAcronyms();
//				//System.out.println(docEntity.getName()+" "+termAcronyms);
//				if(termAcronyms.contains(acronymName)){
//					return docEntity.getName();
//				}
				
				String acro = acroDetector.findBestLongForm(acronym.getName(), docEntity.getName());
				if(acro!=null) return docEntity.getName();
				//B.thuringiensis ===> Bacillus thuringiensis serovar sotto
				//for(String termAc:termAcronyms){
				//	if(termAc.startsWith(acronymName)||termAc.endsWith(acronymName)||termAc.equalsIgnoreCase(acronymName)){
						/*
						int length = acronymName.split(" ").length;
						String[] fields = docEntity.getName().split(" ");
						String newStr = "";
						for(int i=0;i<length;i++){
							newStr+=fields[i];
							newStr+=" ";
						}
						return newStr.trim();
						*/
					//	return docEntity.getName();
					//}
				//}
			}
		}
		return null;
	}
	
	/*
	 * find the original term for an acronym
	 */
	public String freqestFirstLetterInDoc(BBEntity acronym, List<BBEntity> docEntityList) {
		String acronymName = acronym.getName();
		char firstChar = acronymName.charAt(0);
		Hashtable<String, Integer> candFirstTerm = new Hashtable();
		for(BBEntity docEntity : docEntityList){
			String docEntityName = docEntity.getName();
			String[] words = docEntityName.split("[\\s&-]+");
			if(!docEntity.geteID().equals(acronym.geteID())&&!acroDetector.isAcronym(docEntityName)&&docEntity.getType().equals("Bacteria")
					&&(words[0].toUpperCase().charAt(0)==firstChar)){
				Integer freq = candFirstTerm.get(docEntityName);
				int score = words[0].charAt(0)==firstChar?10:1;
				if(freq==null) candFirstTerm.put(words[0], score);
					else  candFirstTerm.put(words[0], freq+score);
			}
		}
		
		int highestFreq = 0;
		String highestWord = null;
		Enumeration enu = candFirstTerm.keys();
		while(enu.hasMoreElements()) {
			String word = (String) enu.nextElement();
			int freq = candFirstTerm.get(word);
			if(freq>highestFreq){
				highestFreq = freq;
				highestWord = word;
			}
		}  
		
		return highestWord;
	}

	/**
	 * map BB Entities to NCBITaxonomy
	 * 
	 * @param ncbiNamesDmp
	 * 
	 */
	public Map mapBBEntityToNCBI(List<BBEntity> docEntities, Map<BBEntity, String> acronymOrigins){
		
		//System.out.println(docEntities.size()+" to be normalized");
		
		//read bb entities
		Map<BBEntity, OntologyTerm> entityMatchedMap = new HashMap(); 
		
		for(BBEntity entity : docEntities){
			String entityName = entity.getName();
			if(acroDetector.isAcronym(entityName)){
				String orgname =  acronymOrigins.get(entity);
				if(orgname!=null){
					entityName = orgname;
					System.out.println(entityName+" orgname: "+entity.getName());
				}
//				else if(entityName.charAt(1)=='.'){
//					System.out.println(entityName+" is the new one of "+entity.getName());
//					entityName = bestInitalEntity(entityName, docEntities);
//					System.out.println(entityName+" is the new one of "+entity.getName());
//				}
			}
			
			
			//get genus and species name
			//1, exact match
			entityName = TaxonNameTool.replaceNonmolSuffixes(entityName);
			OntologyTerm ontoTerm = ontoMapping.exactMatch(entityName);
			//System.out.print("using exact match for ["+entityName+"] find:");
			
			//2, use scientific name
			if(ontoTerm==null){
				ontoTerm = ontoMapping.exactMatch(getScientificName(entityName));
			}
			
			if(ontoTerm==null){
				String name = getScientificName(entityName);
				//remove mutant
				name = removeMutant(name);
				if(name.length()>0&&name.charAt(0)>='A'&&name.charAt(0)<='Z') ontoTerm = ontoMapping.exactMatch(name);
			}
			
			//3, find by Genus and species
			if(ontoTerm==null&&entityName.split("[\\s\\-]+").length>1){//use genus and species name
				entityName = TaxonNameTool.getGenusAndSpeciesName(entityName);
				System.out.println(entityName);
				ontoTerm = ontoMapping.exactMatch(entityName);
				if(ontoTerm==null&&entity.getName().endsWith("acteria")){
					ontoTerm = ontoMapping.exactMatch(getScientificName(entityName));
				}
			}else if(ontoTerm!=null){
				entityMatchedMap.put(entity, ontoTerm);
				continue;
			}
			
			
			if(ontoTerm!=null) {
				entityMatchedMap.put(entity, ontoTerm);
			}

			if(ontoTerm==null&&acroDetector.isAcronym(entity.getName())){
				//String orgName = acronymOrigins.get(entity);
				//OntologyTerm ontoTermForAcro = ontoMapping.exactMap(orgName);
				
				OntologyTerm ontoTermForAcro = ontoMapping.uniqAbbMatch(entityName);
				
				if(ontoTermForAcro!=null) {
					entityMatchedMap.put(entity, ontoTermForAcro);
				}
			}
			
		}
		
		
		//partial coreference
		//Map<BBEntity, BBEntity> entityCoref = new HashMap();
		for(BBEntity entity:docEntities){
			if(!entityMatchedMap.keySet().contains(entity)){
				String docID = entity.getDocID();
				BBEntity reffedEntity = partCorfFinder.referredEntity(entity, docEntities);
				
				if(reffedEntity!=null) {
					OntologyTerm ontoTerm =  entityMatchedMap.get(reffedEntity);
					System.out.println(entity.getName()+" refers to "+reffedEntity.getName());
					if(ontoTerm!=null){
						entityMatchedMap.put(entity, ontoTerm);
						//System.out.println(" ["+ontoTerm.getClassId()+"]");
						//System.out.println("size="+entityMatchedMap.size());
					}
				}
			}
		}
		
		
		//check typo, 
		for(BBEntity entity:docEntities){
			OntologyTerm ontoTerm = entityMatchedMap.get(entity);
			
			// check typo in the document
			if(ontoTerm==null&&entity.getName().length()>5){
				for(BBEntity docEntity : docEntities){
					int distance = EditDistance.computeLevenshteinDistance(docEntity.getName().toLowerCase(), entity.getName().toLowerCase());
					if(docEntity!=entity&&distance>0&&distance<=2){//docEntity is the correct one
						ontoTerm = entityMatchedMap.get(docEntity);
						entityMatchedMap.put(entity, ontoTerm);
					}
					
					if(ontoTerm==null){//Lactococcus lactis   lactococci
						String[] words = docEntity.getName().split("[\\s]+");
						distance = EditDistance.computeLevenshteinDistance(words[0].toLowerCase(), entity.getName().toLowerCase());
						if(docEntity!=entity&&distance>0&&distance<=2){//docEntity is the correct one
							ontoTerm = entityMatchedMap.get(docEntity);
							if(ontoTerm!=null){
								entityMatchedMap.put(entity, ontoTerm);
								System.out.print("\n"+entity.getName()+"\t correct typo:"+ontoTerm.getClassId());
							}
						}
					}
				}
			}
			
			//special processing, BifidobacteriaBifidobacterium
			//if(ontoTerm==null&&entity.getName().endsWith("acteria")){
			if(ontoTerm==null&&entity.getName().endsWith("acteria")){
				ontoTerm = ontoMapping.exactMatch(getScientificName(entity.getName()));
				if(ontoTerm!=null){
					System.out.println(entity.getDocID()+"_"+entity.geteID()+"_"+entity.getName()+"===>"+ontoTerm.getClassId());
					entityMatchedMap.put(entity, ontoTerm);
				}
			}
			
			//Remove one world, Rickettsial==>Rickettsia
			if(ontoTerm==null&&entity.getName().split("[\\s]+").length==1&&!acroDetector.isAcronym(entity.getName())){
				ontoTerm = ontoMapping.exactMatch(entity.getName().substring(0, entity.getName().length()-1));
				
				if(ontoTerm!=null){
					System.out.println(entity.getDocID()+"_"+entity.geteID()+"_"+entity.getName()+"===>"+ontoTerm.getClassId());
					entityMatchedMap.put(entity, ontoTerm);
				}
			}
			

			System.out.print("\n"+entity.getName()+"\t final:");
			if(ontoTerm!=null){
				System.out.println(ontoTerm.getClassId());
			}else{
				System.out.println();
			}
		}
		
		return entityMatchedMap;
	}
	
	
	public String bestInitalEntity(String entityName, List<BBEntity> docEntities) {
		for(BBEntity entity:docEntities){
			if(entity.getName().substring(0, 1).equalsIgnoreCase(entityName.substring(0, 1))){
				String[] words = entity.getName().split("[\\s]+");
				return words[0]+entityName.substring(entityName.indexOf(".")+1);
			}
		}
		return null;
	}

	public String getScientificName(String entityName){
		return entityName.replace("acteria", "acterium");
	}

	/**
	 * map BB Entities to NCBITaxonomy
	 * 
	 * @param ncbiNamesDmp
	 * 
	
	public Map mapBBEntityToNCBI(List<BBEntity> entities, Map<BBEntity, String> acronymOrigins,Map<String, List> docEntityLists){
		
		//System.out.print(this.targetOntology.termMap.size());
		
		//read bb entities
		Map<BBEntity, OntologyTerm> entityMatchedMap = new HashMap(); 
		
		for(BBEntity entity : entities){
			if(entity.getType().equals("Bacteria")){
				String entityName = entity.getName();
				
				
				if(acroDetector.isAcronym(entityName)){
					String orgname =  acronymOrigins.get(entity);
					if(orgname!=null) entityName = orgname;
				}
				
				//get genus and species name
				entityName = TaxonNameTool.replaceNonmolSuffixes(entityName);
				OntologyTerm ontoTerm = ontoMapping.exactMap(entityName);
				
				if(ontoTerm==null&&entity.getName().endsWith("acteria")){
					ontoTerm = ontoMapping.exactMap(entityName.replace("acteria", "acterium"));
				}
				
				if(ontoTerm==null&&entityName.split(" ").length>1){//use genus and species name
					entityName = TaxonNameTool.getGenusAndSpeciesName(entityName);
					ontoTerm = ontoMapping.exactMap(entityName);
					if(ontoTerm==null&&entity.getName().endsWith("acteria")){
						ontoTerm = ontoMapping.exactMap(entityName.replace("acteria", "acterium"));
					}
				}else if(ontoTerm!=null){
					entityMatchedMap.put(entity, ontoTerm);
					continue;
				}
				
				//System.out.print("\n"+entityName+"\t");
				
				if(ontoTerm!=null) {
					entityMatchedMap.put(entity, ontoTerm);
				}

				if(ontoTerm==null&&acroDetector.isAcronym(entity.getName())){
					//String orgName = acronymOrigins.get(entity);
					//OntologyTerm ontoTermForAcro = ontoMapping.exactMap(orgName);
					
					OntologyTerm ontoTermForAcro = ontoMapping.uniqAbbMatch(entityName);
					
					if(ontoTermForAcro!=null) {
						entityMatchedMap.put(entity, ontoTermForAcro);
					}
				}
			}
		}
		
		
		//partialcoreference
		//Map<BBEntity, BBEntity> entityCoref = new HashMap();
		for(BBEntity entity:entities){
			if(!entityMatchedMap.keySet().contains(entity)){
				String docID = entity.getDocID();
				List docEntityList = docEntityLists.get(docID);
				BBEntity reffedEntity = partCorfFinder.referredEntity(entity, docEntityList);
				
				if(reffedEntity!=null) {
					OntologyTerm ontoTerm =  entityMatchedMap.get(reffedEntity);
					//System.out.println(entity.getName()+" refers to "+reffedEntity.getName());
					if(ontoTerm!=null){
						entityMatchedMap.put(entity, ontoTerm);
						//System.out.println(" ["+ontoTerm.getClassId()+"]");
						//System.out.println("size="+entityMatchedMap.size());
					}
				}
			}
		}
		System.out.println(entityMatchedMap.size()+" entities are found from "+entities.size());
		
		
		//check typo, 
		for(BBEntity entity:entities){
			OntologyTerm ontoTerm = entityMatchedMap.get(entity);
			
			// check typo in the document
			if(ontoTerm==null&&entity.getName().length()>5){
				String docID = entity.getDocID();
				List<BBEntity> docEntityList = docEntityLists.get(docID);
				for(BBEntity docEntity : docEntityList){
					int distance = EditDistance.computeLevenshteinDistance(docEntity.getName().toLowerCase(), entity.getName().toLowerCase());
					if(docEntity!=entity&&distance>0&&distance<=2){//docEntity is the correct one
						ontoTerm = entityMatchedMap.get(docEntity);
						entityMatchedMap.put(entity, ontoTerm);
					}
					
					if(ontoTerm==null){//Lactococcus lactis   lactococci
						String[] words = docEntity.getName().split("[\\s]+");
						distance = EditDistance.computeLevenshteinDistance(words[0].toLowerCase(), entity.getName().toLowerCase());
						if(docEntity!=entity&&distance>0&&distance<=2){//docEntity is the correct one
							ontoTerm = entityMatchedMap.get(docEntity);
							entityMatchedMap.put(entity, ontoTerm);
						}
					}
				}
			}
			
			//special processing, BifidobacteriaBifidobacterium
			//if(ontoTerm==null&&entity.getName().endsWith("acteria")){
			if(ontoTerm==null&&entity.getName().endsWith("acteria")){
				ontoTerm = ontoMapping.exactMap(entity.getName().replace("acteria", "acterium"));
				if(ontoTerm!=null){
					System.out.println(entity.getDocID()+"_"+entity.geteID()+"_"+entity.getName()+"===>"+ontoTerm.getClassId());
					entityMatchedMap.put(entity, ontoTerm);
				}
			}
			
			//Remove one world, Rickettsial==>Rickettsia
			if(ontoTerm==null&&entity.getName().split("[\\s]+").length==1&&!acroDetector.isAcronym(entity.getName())){
				ontoTerm = ontoMapping.exactMap(entity.getName().substring(0, entity.getName().length()-1));
				
				if(ontoTerm!=null){
					System.out.println(entity.getDocID()+"_"+entity.geteID()+"_"+entity.getName()+"===>"+ontoTerm.getClassId());
					entityMatchedMap.put(entity, ontoTerm);
				}
			}
		}
				
				
		return entityMatchedMap;
	}
	 */
	
	
	/**
	 * remove one pattern of mutation
	 * non-O1 V. cholerae
	 * @param entityName
	 * @return
	 */
	public String removeMutant(String entityName){
		String[] words = entityName.split("[\\s]+");
		String newTerm = "";
		if(words.length>2){
			if(words[1].charAt(0)==words[1].toUpperCase().charAt(0)&&words[2].charAt(0)==words[2].toLowerCase().charAt(0)){
				for(int i=1;i<words.length;i++){
					newTerm+=words[i]+" ";
				}
			}
		}
		return newTerm.trim();
	}
	
	
	/**
	 * generate A2 file
	 * @param docEntityList
	 * @param entityAnnMaps
	 * @param outputFolder
	
	public void generateSubmission(Map<String, List> docEntityList, Map<BBEntity, OntologyTerm> entityAnnMaps, String outputFolder){
		Set docSet = docEntityList.keySet();
		Iterator docIter = docSet.iterator();
		while(docIter.hasNext()){
			String doc = (String) docIter.next();
			File file = new File(outputFolder+"/"+doc.replace(".a1", ".a2"));
			
			List<BBEntity> docEntities = docEntityList.get(doc);
			
			FileWriter fw;
			try {
				fw = new FileWriter(file);
				int num = 1;
				
				for(BBEntity anEntity : docEntities){
					if(anEntity.getName()!=null){
						fw.write(anEntity.geteID()+"\t"+anEntity.getType()+" "+anEntity.getStart()+" "+anEntity.getEnd()+"\t"+anEntity.getName()+"\n");
					}
				}
				
				for(BBEntity entity : docEntities){
					OntologyTerm assignedClass = entityAnnMaps.get(entity);
					if(assignedClass!=null){
						fw.write("N"+num+++"\tNCBI_Taxonomy Annotation:"+entity.geteID()+" Referent:"+assignedClass.getClassId()+"\n");
					}
				}
				fw.flush();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	 */
	
	
	public static void main(String[] args){
		String ncbiNamesDmp = "F:\\Habitat\\BacteriaBiotope\\resources\\NCBI Taxonomy\\names.dmp";
		String bbentitiesFolder = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\entity";//dev;train
		//String bbentitiesFolder = "F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\results";//dev;train
		
		//read NCBI Taxonomy ontology
		NamesParser ncbiNameParser = new NamesParser();
		ncbiNameParser.parse(ncbiNamesDmp);
		
		BacteriaLinkToOntology bc = new BacteriaLinkToOntology(ncbiNameParser.getOntology());//Bacteria, 2 ncbiNameParser.prune("2")
		//"F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\entity"
		//"F:\\Habitat\\BacteriaBiotope\\experiments\\crfresults"
		//bc.matchAllFiles(bbentitiesFolder,"F:\\Habitat\\Bacteria Biotope\\bionlp 2016\\UA_SI_dev");
		String outputFolder ="F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\submission";
		//bc.matchAllFiles(bbentitiesFolder,outputFolder);
		
//		AcronymGenerator acroGenerator = new AcronymGenerator();
//		acroGenerator.setOrgTerm("Escherichia coli");
//		Set termAcronyms = acroGenerator.getAcronyms();
//		System.out.println(" "+termAcronyms);
//		if(termAcronyms.contains("E. coli")){
//			System.out.println("orgininal="+"Escherichia coli");
//		}
		//System.out.println(TaxonNameTool.getGenusAndSpeciesName("R. conorii reference strain no. 7"));
	}

	
	
}

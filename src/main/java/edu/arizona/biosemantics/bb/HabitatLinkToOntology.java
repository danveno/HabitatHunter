package edu.arizona.biosemantics.bb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.habitat.feature.UMBCSim;
import edu.arizona.biosemantics.habitat.feature.WordSenseRender;
import edu.arizona.biosemantics.habitat.ontology.OntologyManager;
import edu.arizona.biosemantics.habitat.ontology.OntologyOBOParser;
import edu.arizona.biosemantics.habitat.ontology.OntologyTerm;
import edu.arizona.biosemantics.habitat.ontomap.OntologyMapping;
import edu.arizona.biosemantics.habitat.term.AcronymDetector;
import edu.arizona.biosemantics.habitat.term.TaxonNameTool;
import edu.arizona.biosemantics.nlp.PhraseHeader;
import edu.mit.jwi.item.POS;



/**
 * normalize habitat entity with OntoBiotope ontology
 * 
 * @author maojin
 *
 */
public class HabitatLinkToOntology {
	private OntologyMapping biotopeOntoMapping;
	private WordSenseRender wordSenseRender = new WordSenseRender();
	private AcronymDetector acroDetector = new AcronymDetector();
	private PhraseHeader phraseHeader = new PhraseHeader();
	
	public HabitatLinkToOntology(){
		initOntoBiotope();
	}
	
	public void initOntoBiotope(){
		//read OntoBiotope
		OntologyOBOParser biotopeParser = new OntologyOBOParser();
		biotopeParser.parse(Config.ontoBiotopePath);
		biotopeParser.formHierachy();
		OntologyManager biotopeOntoMgr = biotopeParser.getOntology();
		biotopeOntoMapping = new OntologyMapping(biotopeOntoMgr);
		
		try {
			wordSenseRender.loadDictionary(Config.wordNetDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	
	/*
	 * find the original term for an acronym
	 */
	public String originalTermInDoc(BBEntity acronym, List<BBEntity> docEntityList, String entityType) {
		String acronymName = acronym.getName();
		for(BBEntity docEntity : docEntityList){
			if(!docEntity.geteID().equals(acronym.geteID())&&docEntity.getType().equals(entityType)
					&&!acronymName.equals(docEntity.getName())&&acronymName.length()<docEntity.getName().length()){
				String acro = acroDetector.findBestLongForm(acronymName, docEntity.getName());
				if(acro!=null) return docEntity.getName();
			}
		}
		return null;
	}
	
	/**
	 * get the acronym & long form pairs
	 * @param acronyms
	 * @return
	 */
	public Map<BBEntity, String> obtainAcronymMap(List<BBEntity> acronyms,  List<BBEntity> entities){
		//Find the original term for acronyms
		Map<BBEntity, String> acronymOrigins = new HashMap();
		for(BBEntity acron:acronyms){
			String acronName = acron.getName();
			//map global Abbrieviations
			String originalTerm = acroDetector.findGlobalAcronym(acron.getName());
			
			if(originalTerm!=null){
				//System.out.println(acron.getName()+"===>"+originalTerm);
				acronymOrigins.put(acron, originalTerm);
			}else{
				 originalTerm = originalTermInDoc(acron, entities,"Habitat");
				if(originalTerm!=null){
					acronymOrigins.put(acron, originalTerm); 
				}
			}
		}
		return acronymOrigins;
	}
	
	/**
	 * normalize by simple substring match
	 * @param habitatEntity
	 * @return
	 */
	public Set<OntologyTerm> normalizeBySubstring(BBEntity habitatEntity){
		Set<OntologyTerm> mappedOntoTerms = biotopeOntoMapping.findCoverredTerm(habitatEntity.getName());
		return mappedOntoTerms;
	}
	
	
	/**
	 * normalize by exact match
	 * @param habitatEntity
	 * @return
	 */
	public OntologyTerm normalizeByExactMatch(String entityName){
		//Set<OntologyTerm> mappedOntoTerms = new HashSet();
		
		OntologyTerm mappedTerm = biotopeOntoMapping.exactMatch(entityName);
		if(mappedTerm!=null){
			//mappedOntoTerms.add(mappedTerm);
			System.out.println(entityName+"--->exact match habitat is:"+mappedTerm.getClassId());
		}
		if(entityName.split("[\\s]+").length==1&&mappedTerm==null){
			String orgName = wordSenseRender.stemWord(entityName, POS.NOUN);
			System.out.println("exact match for "+orgName);
			if(orgName!=null&&!orgName.equals(entityName)){
				mappedTerm = biotopeOntoMapping.exactMatch(orgName);
			}
			if(mappedTerm!=null){
				System.out.println("exact matched: "+mappedTerm.getClassId());
				//mappedOntoTerms.add(mappedTerm);
			}
		} 
		
		/*else{
			if(name.split(" ").length==1) name = wordSenseRender.getSense(name, POS.NOUN);
			mappedTerm = biotopeOntoMapping.exactMap(name);
			if(mappedTerm!=null){
				mappedOntoTerms.add(mappedTerm);
				System.out.println(name+"--->Habitat--->is:"+mappedTerm.getClassId());
			}
		}*/
		return mappedTerm;
	}

	/**
	 * normalize by exact match
	 * @param habitatEntity
	 * @return
	 */
	public OntologyTerm normalizeByEditDistanceMatch(String entityName){
		OntologyTerm mappedOntoTerm = biotopeOntoMapping.editDistanceMatch(entityName);
		/*
		OntologyTerm mappedTerm = biotopeOntoMapping.editDistanceMap(name);
		if(mappedTerm!=null){
			mappedOntoTerms.add(mappedTerm);
			System.out.println(habitatEntity.getName()+"--->Habitat--->is:"+mappedTerm.getClassId());
		}
		else{
			if(name.split(" ").length==1) name = wordSenseRender.getSense(name, POS.NOUN);
			mappedTerm = biotopeOntoMapping.exactMap(name);
			if(mappedTerm!=null){
				mappedOntoTerms.add(mappedTerm);
				System.out.println(name+"--->Habitat--->is:"+mappedTerm.getClassId());
			}
		}*/
		return mappedOntoTerm;
	}
	
	/**
	 * normalize all the entities
	 * 
	 * @param bbentities
	 * @return
	 */
	public Map<BBEntity, OntologyTerm> normalizeEntities(List<BBEntity> bbentities) {
		//detect acronyms
		List<BBEntity> acronyms = new ArrayList();
		for(BBEntity entity:bbentities){
			if(entity.getType().equals("Habitat")&&acroDetector.isAcronym(entity.getName())){
				if(entity.getName().split(" ").length>1) entity.setName(TaxonNameTool.getGenusAndSpeciesName(entity.getName()));
				acronyms.add(entity);
			}
		}
		
		Map<BBEntity, String> acronyLongForms = this.obtainAcronymMap(acronyms, bbentities);
		
		Map<BBEntity, OntologyTerm> habitatTerms = new HashMap<BBEntity, OntologyTerm>();
		for(BBEntity entity:bbentities){
			if(entity.getType().equals("Habitat")){
				//find substring
				//Set<OntologyTerm> mappedOntoTerms = normalizeBySubstring(entity);
				
				String entityName = entity.getName();
				
				OntologyTerm  mappedOntoTerm = null;
				/*
				if(acroDetector.isAcronym(entityName)&&acroDetector.has2Uppercase(entityName)){
					String longForm = acronyLongForms.get(entityName);
					if(longForm==null){
						System.out.println(entityName+" has no long form.");
						if(entityName.split("[\\s\\-]+").length==1) continue;
					}else{ 
						entityName = longForm;
					}
				}
				*/
				//exact match
				mappedOntoTerm = normalizeByExactMatch(entityName);
				
				//use the lemma
				//this method harms the performance
				/**/
				if(mappedOntoTerm==null){
					String[] entityWords = entityName.split("[\\s]+");
					//find the header, and then use the header
					if(entityWords.length>1){
						String header = phraseHeader.simpleExtract(entityName);
						System.out.println(header+" is a header of ["+entityName+"]");
						if(header!=null&&!header.equals(entityName)){
							mappedOntoTerm = normalizeByExactMatch(header);
							System.out.println(header+" is a header of ["+entityName+"]");
						}
						
						//use the last term as the upper class
						if(mappedOntoTerm==null){
							header = entityWords[entityWords.length-1];
							System.out.println(header+" is a header of ["+entityName+"]");
							if(header!=null&&!header.equals(entityName)){
								mappedOntoTerm = normalizeByExactMatch(header);
								System.out.println(header+" is a header of ["+entityName+"]");
							}
						}
						
						//use the first term as the upper class
						if(mappedOntoTerm==null){
							header = entityWords[0];
							System.out.println(header+" is a header of ["+entityName+"]");
							if(header!=null&&!header.equals(entityName)){
								mappedOntoTerm = normalizeByExactMatch(header);
								System.out.println(header+" is a header of ["+entityName+"]");
							}
						}
						
						if(mappedOntoTerm!=null){
							mappedOntoTerm =  biotopeOntoMapping.selectFromSubClass(mappedOntoTerm,entityName);
						}
						
					}
					
					//variant match
					if(mappedOntoTerm!=null){
//						OntologyTerm mappedTerm  = mappedOntoTerms.iterator().next();
						System.out.println(entityName+" is matched to "+mappedOntoTerm.getClassId()+"-"+mappedOntoTerm.getPreferredName());
					}
				}
				
				//variant match
				if(mappedOntoTerm==null){
					//if(entityName.length()>5) 
					mappedOntoTerm = normalizeByEditDistanceMatch(entityName);
				}
				
				//highest similarity match
				if(mappedOntoTerm==null){
					mappedOntoTerm  = biotopeOntoMapping.jaccardMatch(entityName);
					if(mappedOntoTerm!=null){
						System.out.println(entity.getName()+" jaccard similarity is "+mappedOntoTerm.getPreferredName());
					}
				}
				
				/*
				if(mappedOntoTerm==null){
					mappedOntoTerm  = biotopeOntoMapping.globalHighestUMBCSim(entityName);
					if(mappedOntoTerm!=null){
						System.out.println(entity.getName()+" jaccard similarity is "+mappedOntoTerm.getPreferredName());
					}
				}
				*/
				
				if(mappedOntoTerm!=null){
					habitatTerms.put(entity, mappedOntoTerm);
				}else{
					System.out.println(entity.getName()+"--->Habitat--->not found!");
				}
			}
		}
		return habitatTerms;
	}

}

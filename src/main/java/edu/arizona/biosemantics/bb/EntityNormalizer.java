package edu.arizona.biosemantics.bb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import java.util.Set;

import edu.arizona.biosemantics.habitat.io.A1FormatFileUtil;
import edu.arizona.biosemantics.habitat.io.A2FormatFileUtil;
import edu.arizona.biosemantics.habitat.io.FileUtil;
import edu.arizona.biosemantics.habitat.ontology.OntologyManager;
import edu.arizona.biosemantics.habitat.ontology.OntologyOBOParser;
import edu.arizona.biosemantics.habitat.ontology.OntologyTerm;
import edu.arizona.biosemantics.habitat.ontomap.OntologyMapping;


/**
 * normalize the recognized entities with ontology classes
 * @author maojin
 *
 */
public class EntityNormalizer {
	private BacteriaLinkToOntology bactLinker = new BacteriaLinkToOntology();
	private HabitatLinkToOntology habitatLinker = new HabitatLinkToOntology();
	
	private A2FormatFileUtil a2Util = new A2FormatFileUtil();
	
	private A1FormatFileUtil entityReader = new A1FormatFileUtil();

	
	
	
	/**
	 * using exact matching method to classify bacteria entities
	 */
	public void matchAllFiles(String bbentitiesFolder, String outputFolder){
		//organize entities by doc
		//Map<String, List> docAllEntities = new HashMap();
		
		//read all entities
		File folderFile = new File(bbentitiesFolder);
		
		File[] files = folderFile.listFiles();
		for(File aFile : files ){
			String docName = aFile.getName();
			if(docName.endsWith(".a1")){
				String fileName = FileUtil.getFileName(docName);
				System.out.println("Normalzing:"+fileName);
				List bbentities = entityReader.readFromFile(aFile.getPath());
				//docAllEntities.put(fileName, bbentities);
				//if(!fileName.startsWith("BB-cat+ner-10496597")) continue;
				
				//normalize bacteria entities
				Map<BBEntity, OntologyTerm> bacteriaEntityOntoTerms = bactLinker.normalizeEntities(bbentities);
				
				Map<BBEntity, OntologyTerm> habitatEntityOntoTerms = habitatLinker.normalizeEntities(bbentities);
				
				a2Util.generateSubmission(docName, bbentities, bacteriaEntityOntoTerms,habitatEntityOntoTerms, outputFolder, true);
			}
			//break;
		}
	}
	
	
	public static void main(String[] args){
		String bbentitiesFolder = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\entity_gsd";//entity2  entity_gsd
		String outputFolder ="F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\submission";
		
		EntityNormalizer entityNormalizer = new EntityNormalizer();
		entityNormalizer.matchAllFiles(bbentitiesFolder, outputFolder);
		
		/*
		NamesParser ncbiNameParser = new NamesParser();
		ncbiNameParser.parse(Config.ncbiNamesDmp);
		OntologyManager targetOntology = ncbiNameParser.getOntology();
		OntologyMapping ontoMapping = new OntologyMapping(targetOntology);
		System.out.println(ontoMapping.exactMap("Yersinia"));
		*/
		
		//read OntoBiotope
		/*
				OntologyOBOParser biotopeParser = new OntologyOBOParser();
				biotopeParser.parse(Config.ontoBiotopePath);
				biotopeParser.formHierachy();
				OntologyManager biotopeOntoMgr = biotopeParser.getOntology();
				OntologyTerm term = new OntologyTerm();
				term.setClassId("002133");
				System.out.println("002133="+biotopeOntoMgr.getOffsprings(term).size());
				*/
	}
}

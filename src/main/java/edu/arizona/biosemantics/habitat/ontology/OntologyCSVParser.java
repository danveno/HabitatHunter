package edu.arizona.biosemantics.habitat.ontology;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;

/**
 * read the ontology
 * @author maojin
 */
public class OntologyCSVParser extends OntologyParser{
	
	
	/**
	 * parse enviroment ontology
	 */
	public void parseENVO(String ontoPath){
		CSVReader reader;
		try {
			reader = new CSVReader(new BufferedReader(new InputStreamReader(new FileInputStream(ontoPath), "UTF8")));
			List<String[]> lines = reader.readAll();
			
		    //0Class ID	 1Preferred Label	2Synonyms	3Definitions	4Obsolete	5CUI	6Semantic Types	
			//7Parents	8consider	database_cross_reference
			termMap = new HashMap();
			parChildMap = new HashMap();
			
			for(int index=1; index<lines.size();index++){
				String[] aClass = lines.get(index);
				String classId = aClass[0].trim();
				String preferredName = aClass[1].trim();
				String synonyms = aClass[2].trim();//sheet iron roof|sheet-iron roof
				String parentId = aClass[7].trim();//http://purl.obolibrary.org/obo/ENVO_0010170 | http://purl.obolibrary.org/obo/ENVO_00003891
				
				OntologyTerm ontoTerm = new OntologyTerm();
				ontoTerm.setClassId(classId);
				ontoTerm.setPreferredName(preferredName);
				if(!"".equals(synonyms)){
					ontoTerm.setSynonyms(synonyms.split("\\|"));
				}
				if(!"".equals(parentId)){
					String[] parents = parentId.split("\\|");
					ontoTerm.setParentId(parents);
					
					putIntoHierarchy(parents, ontoTerm);
				}
				termMap.put(classId, ontoTerm);
				
			}
			//System.out.println("term size:"+termMap.size());
			//System.out.println("parent size:"+parChildMap.size());
			reader.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * parse OntoBiotope ontology
	 */
	public void parseOntoBiotope(String ontoPath){
		CSVReader reader;
		try {
			reader = new CSVReader(new BufferedReader(new InputStreamReader(new FileInputStream(ontoPath), "UTF8")));
			List<String[]> lines = reader.readAll();
			
			///0Class ID	1Preferred Label	2Synonyms	3Definitions	4Obsolete	5CUI	6Semantic Types
			//7Parents	8database_cross_reference	9has_exact_synonym	has_obo_format_version	has_related_synonym

			termMap = new HashMap();
			parChildMap = new HashMap();
			
			for(int index=1; index<lines.size();index++){
				String[] aClass = lines.get(index);
				String classId = aClass[0].trim();
				String preferredName = aClass[1].trim();
				String synonyms = aClass[2].trim();//sheet iron roof|sheet-iron roof
				String parentId = aClass[7].trim();//http://purl.obolibrary.org/obo/ENVO_0010170 | http://purl.obolibrary.org/obo/ENVO_00003891
				
				OntologyTerm ontoTerm = new OntologyTerm();
				ontoTerm.setClassId(classId);
				ontoTerm.setPreferredName(preferredName);
				if(!"".equals(synonyms)){
					ontoTerm.setSynonyms(synonyms.split("\\|"));
				}
				if(!"".equals(parentId)&&!"http://www.w3.org/2002/07/owl#Thing".equals(parentId)&&!"http://purl.obolibrary.org/obo/MBTO_00000872".equals(parentId)){
					String[] parents = parentId.split("\\|");
					ontoTerm.setParentId(parents);
					
					putIntoHierarchy(parents, ontoTerm);
				}
				termMap.put(classId, ontoTerm);
				
			}
			//System.out.println("term size:"+termMap.size());
			//System.out.println("parent size:"+parChildMap.size());
			reader.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * parse habitat ontology developed in our group
	 */
	public void parseHabitOnto(String ontoPath){
		CSVReader reader;
		try {
			reader = new CSVReader(new BufferedReader(new InputStreamReader(new FileInputStream(ontoPath), "UTF8")));
			List<String[]> lines = reader.readAll();
			
			//0candidate term	1ocurrences in FNA	2selected term	3synonyms	4narrower terms	
			//5broader terms	6decisons made by	7InEnvO/ExtBioPortalDef/InmParentClass


			termMap = new HashMap();
			parChildMap = new HashMap();
			
			for(int index=1; index<lines.size();index++){
				String[] aClass = lines.get(index);
				String classId = aClass[2].trim();
				String preferredName = aClass[2].trim();
				preferredName = preferredName.replace("?", "").trim();
				if("".equals(preferredName)||preferredName.length()<2) continue;
				String synonyms = aClass[3].trim();//hillside, bank, bend
				synonyms = synonyms.replace("?", "").trim();
				String parentId = aClass[5].trim();//forests, woodlands
				parentId = parentId.replace("?", "").trim();
				
				OntologyTerm ontoTerm = new OntologyTerm();
				ontoTerm.setClassId(classId);
				ontoTerm.setPreferredName(preferredName);
				if(!"".equals(synonyms)){
					ontoTerm.setSynonyms(synonyms.split(","));
				}
				if(!"".equals(parentId)){
					String[] parents = parentId.split(",");
					ontoTerm.setParentId(parents);
					
					putIntoHierarchy(parents, ontoTerm);
				}
				termMap.put(classId, ontoTerm);
				
			}
			//System.out.println("term size:"+termMap.size());
			//System.out.println("parent size:"+parChildMap.size());
			reader.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	
	
	
	
	
	public static void main(String[] args){
		OntologyCSVParser op = new OntologyCSVParser();
		
		//parse envo ontology
		String envoPath = "F:\\Habitat\\ontology\\ENVO\\ENVO.csv";
		op.parseENVO(envoPath);
		
		//parse OntoBiotope
		//String OntoBiotopePath = "F:\\Habitat\\ontology\\OntoBiotope\\ONTOBIOTOPE.csv";
		//op.parseOntoBiotope(OntoBiotopePath);
		
		//parse Habitat Ontology
		//String habitOntologyPath = "F:\\Habitat\\ontology\\Habitat Ontology\\Habitat Ontology Candidate Terms.csv";
		//op.parseHabitOnto(habitOntologyPath);
		
		System.out.println("total classes="+op.termMap.size());
		System.out.println("first level terms="+op.getFirstLevelTerms().size());
		System.out.println("leaf terms="+op.getLeafTerms().size());
		
		
		op.output(op.getFirstLevelTerms(), "F:\\Habitat\\ontology\\ENVO\\ENVO_1levelterm.txt");
		op.output(op.getLeafTerms(), "F:\\Habitat\\ontology\\ENVO\\ENVO_leafterm.txt");
		op.outputAllTerm("F:\\Habitat\\ontology\\ENVO\\ENVO_all.txt");
		op.outputNoFirstLevelTerm("F:\\Habitat\\ontology\\ENVO\\ENVO_no1levelterm.txt");
		
		
		/*
		op.output(op.getLeafTerms(), "F:\\Habitat\\ontology\\OntoBiotope\\OntoBiotope13_leafterm.txt");
		op.output(op.getFirstLevelTerms(), "F:\\Habitat\\ontology\\OntoBiotope\\OntoBiotope13_1levelterm.txt");
		op.outputAllTerm("F:\\Habitat\\ontology\\OntoBiotope\\OntoBiotope13_all.txt");
		op.outputNoFirstLevelTerm("F:\\Habitat\\ontology\\OntoBiotope\\OntoBiotope13_no1levelterm.txt");
		*/
		
		/*
		op.output(op.getFirstLevelTerms(), "F:\\Habitat\\ontology\\Habitat Ontology\\HabitOnto_1levelterm.txt");
		op.output(op.getLeafTerms(), "F:\\Habitat\\ontology\\Habitat Ontology\\HabitOnto_leafterm.txt");
		op.outputAllTerm("F:\\Habitat\\ontology\\Habitat Ontology\\HabitOnto_all.txt");
		op.outputNoFirstLevelTerm("F:\\Habitat\\ontology\\Habitat Ontology\\HabitOnto_no1levelterm.txt");
		*/
	}
}

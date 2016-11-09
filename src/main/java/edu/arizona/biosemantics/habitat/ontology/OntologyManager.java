package edu.arizona.biosemantics.habitat.ontology;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.arizona.biosemantics.habitat.io.FileUtil;



/**
 * Manage Ontology and Support basic visit
 * 
 * @author maojin
 *
 */
public class OntologyManager {
	/**
	 * classId, OntologyTerm
	 * each class in the ontology is a term
	 */
	public Map<String, OntologyTerm> termMap;
	
	/**
	 * classId, all its children
	 */
	public Map<String, List<OntologyTerm>> parChildMap;
	
	
	public Set<String> tokenSet;


	
	/**
	 * according to termMap, generate tokenset
	 */
	public void initTokenSet(String tokenFile) {
		tokenSet = new HashSet();
		List<String> tokens = FileUtil.readLineFromFile(tokenFile);
		for(String token:tokens){
			tokenSet.add(token);
		}
	}
	
	
	/**
	 * according to termMap, generate tokenset
	 */
	public void initTokenSet() {
		tokenSet = new HashSet();
		Set<Entry<String, OntologyTerm>> valueSet = termMap.entrySet();
		Iterator<Entry<String, OntologyTerm>> valueIter = valueSet.iterator();
		while(valueIter.hasNext()){
			OntologyTerm ot = valueIter.next().getValue();
			String term = ot.getPreferredName();
			String[] termFields = term.split("[\\s]+");
			for(String item : termFields){
				tokenSet.add(item.toLowerCase());
			}
			
			String[] synomyms = ot.getSynonyms();
			if(synomyms!=null){
				for(String syn : synomyms){
					termFields = syn.split("[\\s]+");
					for(String item : termFields){
						tokenSet.add(item.toLowerCase());
					}
				}
			}
		}
		
		/*
		FileWriter ncbiTokensWriter;
		try {
			ncbiTokensWriter = new FileWriter(new File("F:\\Habitat\\BacteriaBiotope\\experiments\\ontology term list\\bacteria_all_tokens.txt"));
			for(String token:tokenSet){
				if(token.endsWith(".")||token.endsWith(",")||token.endsWith("'")) token = token.substring(0, token.length()-1);
				if(token.length()>1) ncbiTokensWriter.write(token+"\n");
			}
			ncbiTokensWriter.flush();
			ncbiTokensWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
		
	}


	public List<OntologyTerm> getOffsprings(OntologyTerm term) {
		List offSprings = new ArrayList();
		addAllChildren(term,offSprings);
		return offSprings;
	}


	public void addAllChildren(OntologyTerm term, List offSprings) {
		List<OntologyTerm> chilren = this.parChildMap.get(term.getClassId());
		if(chilren!=null&&chilren.size()>0){
			offSprings.addAll(chilren);
			for(OntologyTerm child:chilren){
				addAllChildren(child,offSprings);
			}
		}
	}
	
	
}

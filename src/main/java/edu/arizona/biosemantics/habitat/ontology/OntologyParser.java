package edu.arizona.biosemantics.habitat.ontology;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


/**
 * ontology parser
 * 
 * @author maojin
 *
 */
public class OntologyParser {
	/**
	 * this has been changed from <classId, OntologyTerm> to <OntologyTerm, classId>
	 * each class in the ontology is a term
	 */
	public Map<String, OntologyTerm> termMap;
	
	/**
	 * classId, all its children
	 */
	public Map<String, List<OntologyTerm>> parChildMap;
	
	/**
	 * first level terms
	 */
	public List<OntologyTerm> firstLevelTerms;
	
	/**
	 * Get all the first level terms which do not have a parent classid
	 * 
	 * @return
	 */
	public List<OntologyTerm> getFirstLevelTerms(){
		if(termMap!=null){
			firstLevelTerms = new ArrayList();
			for(Entry<String, OntologyTerm> termEnt:termMap.entrySet()){
				if(termEnt.getValue().getParentId()==null){
					firstLevelTerms.add(termEnt.getValue());
				}
			}
		}else{
			System.out.println("The ontology has not been parsed!");
		}
		return firstLevelTerms;
	}
	
	/**
	 * Leaf Terms are those terms that donot have an entry to parChildMap 
	 * @return
	 */
	public List<OntologyTerm> getLeafTerms(){
		List leafTerms = null;
		if(termMap!=null){
			leafTerms = new ArrayList();
			Set parSet = parChildMap.keySet();
			for(Entry<String, OntologyTerm> termEnt:termMap.entrySet()){
				if(!parSet.contains(termEnt.getKey())){
					leafTerms.add(termEnt.getValue());
				}
			}
		}else{
			System.out.println("The ontology has not been parsed!");
		}
		return leafTerms;
	}
	
	
	/**
	 * put into hierarchy
	 * @param parents
	 * @param ontoTerm
	 */
	public void putIntoHierarchy(String[] parents, OntologyTerm ontoTerm) {
		for(String parentId : parents){
			List<OntologyTerm> children = parChildMap.get(parentId);
			if(children ==null ){
				children = new ArrayList();
			}
			
			children.add(ontoTerm);
			parChildMap.put(parentId, children);
		}
	}
	
	
	/**
	 * store the termlist into the file
	 * @param termList
	 * @param file
	 */
	public void output(List<OntologyTerm> termList, String file){
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			//split each paragraph
			for(OntologyTerm onterm : termList){
				fw.write(onterm.getPreferredName());
				fw.write("\n");
				if(onterm.getSynonyms()!=null){
					for(String syn : onterm.getSynonyms()){
						fw.write(syn.trim());
						fw.write("\n");
					}
				}
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * output all the terms
	 * @param file
	 */
	public void outputAllTerm(String file){
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			//split each paragraph
			for(Entry<String, OntologyTerm> ontermEntry : this.termMap.entrySet()){
				OntologyTerm onterm = ontermEntry.getValue();
				fw.write(onterm.getPreferredName());
				fw.write("\n");
				if(onterm.getSynonyms()!=null){
					for(String syn : onterm.getSynonyms()){
						fw.write(syn.trim());
						fw.write("\n");
					}
				}
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * output all the terms
	 * @param file
	 */
	public void outputNoFirstLevelTerm(String file){
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			Set firstLevelSet = new HashSet();
			for(OntologyTerm ot:firstLevelTerms){
				firstLevelSet.add(ot.getClassId());
			}
			
			//split each paragraph
			for(Entry<String, OntologyTerm> ontermEntry : this.termMap.entrySet()){
				OntologyTerm onterm = ontermEntry.getValue();
				if(firstLevelSet.contains(onterm.getClassId())) continue;
				fw.write(onterm.getPreferredName());
				fw.write("\n");
				if(onterm.getSynonyms()!=null){
					for(String syn : onterm.getSynonyms()){
						fw.write(syn.trim());
						fw.write("\n");
					}
				}
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * get the ontology
	 * @return
	 */
	public OntologyManager getOntology(){
		OntologyManager ontoMgr = new OntologyManager();
		ontoMgr.termMap = this.termMap;
		ontoMgr.parChildMap = this.parChildMap;
		
		return ontoMgr;
	}
	
	
	
	/**
	 * get the ontology
	 * @return
	 */
	public OntologyManager prune(String classId){
		OntologyManager ontoMgr = new OntologyManager();
		
		//classId, OntologyTerm
		Map newTermMap = new HashMap();
		findChildren(classId,newTermMap);
		
		ontoMgr.termMap = newTermMap;
		ontoMgr.parChildMap = this.parChildMap;
		
		return ontoMgr;
	}

	
	private void findChildren(String pclassId, Map newTermMap) {
		//List Map<String, List<OntologyTerm>>
		List<OntologyTerm> childList = parChildMap.get(pclassId);
		if(childList!=null){
			for(OntologyTerm term : childList){
				newTermMap.put(term.getClassId(), term);
				findChildren(term.getClassId(),newTermMap);
			}
		}
	}
	
}

package edu.arizona.biosemantics.habitat.ontology;

import java.util.Set;


/**
 * Ontology term
 * @author maojin
 */
public class OntologyTerm {
	
	private String classId;//key
	private String preferredName;
	private String[] synonyms;
	private String[] parentId;
	private String ontology;
	private Integer level;
	private boolean isLeaf; //whether this node is a leaf
	private boolean isObsolete;
	
	public String getClassId() {
		return classId;
	}
	public void setClassId(String classId) {
		this.classId = classId;
	}
	public String getPreferredName() {
		return preferredName;
	}
	public void setPreferredName(String preferredName) {
		this.preferredName = preferredName;
	}
	public String[] getSynonyms() {
		return synonyms;
	}
	public void setSynonyms(String[] synonyms) {
		this.synonyms = synonyms;
	}
	public String[] getParentId() {
		return parentId;
	}
	public void setParentId(String[] parentId) {
		this.parentId = parentId;
	}
	public Integer getLevel() {
		return level;
	}
	public void setLevel(Integer level) {
		this.level = level;
	}
	public boolean isLeaf() {
		return isLeaf;
	}
	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}
	public boolean isObsolete() {
		return isObsolete;
	}
	public void setObsolete(boolean isObsolete) {
		this.isObsolete = isObsolete;
	}
	public String getOntology() {
		return ontology;
	}
	public void setOntology(String ontology) {
		this.ontology = ontology;
	}
}

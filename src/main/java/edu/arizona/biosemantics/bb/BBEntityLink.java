package edu.arizona.biosemantics.bb;


/**
 * the mapping from an entity to a class
 * 
 * e.g., N1	NCBI_Taxonomy Annotation:T3 Referent:727
 * 
 * @author maojin
 *
 */
public class BBEntityLink {
	private String linkID;
	private String ontology;
	private String eID;
	private String docID;
	private String ontologyClass;
	
	public BBEntityLink(){
		
	}
	
	public BBEntityLink(String fileName, String linkID, String ontology,
			String eID, String ontologyClass) {
		this.linkID = linkID;
		this.ontology = ontology;
		this.eID = eID;
		this.ontologyClass = ontologyClass;
		this.docID = docID;
	}
	
	
	public String getLinkID() {
		return linkID;
	}
	public void setLinkID(String linkID) {
		this.linkID = linkID;
	}
	public String getOntology() {
		return ontology;
	}
	public void setOntology(String ontology) {
		this.ontology = ontology;
	}
	public String geteID() {
		return eID;
	}
	public void seteID(String eID) {
		this.eID = eID;
	}
	public String getDocID() {
		return docID;
	}
	public void setDocID(String docID) {
		this.docID = docID;
	}
	public String getOntologyClass() {
		return ontologyClass;
	}
	public void setOntologyClass(String ontologyClass) {
		this.ontologyClass = ontologyClass;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer()
			.append(this.linkID).append("\t").append(this.ontology).append(" ")
				.append("Annotation:").append(this.eID).append(" ")
				.append("Referent:");
		if("NCBI_Taxonomy".equals(this.ontology)){
			sb.append(this.ontologyClass);
		}else{
			sb.append("OBT:").append(this.ontologyClass);
		}
		return sb.toString();
	}
	
}

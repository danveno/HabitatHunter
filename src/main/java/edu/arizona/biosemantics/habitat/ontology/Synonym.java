package edu.arizona.biosemantics.habitat.ontology;

/**
 * Synonyms for terms
 * 
 * @author maojin
 * 
 */
public class Synonym {
	private Long sid;
	private String tid;// term id
	private String synonym;
	private String type;
	private String typeCont;

	public Long getSid() {
		return sid;
	}

	public void setSid(Long sid) {
		this.sid = sid;
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public String getSynonym() {
		return synonym;
	}

	public void setSynonym(String synonym) {
		this.synonym = synonym;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTypeCont() {
		return typeCont;
	}

	public void setTypeCont(String typeCont) {
		this.typeCont = typeCont;
	}
}

package edu.arizona.biosemantics.habitat.ontology;

/**
 * relations between terms
 * 
 * @author maojin
 * 
 */
public class Relation {
	private Long rid;
	private String sourceId;// source term id
	private String targetId;// target term id
	private Integer type;
	private int subtype;

	public Long getRid() {
		return rid;
	}

	public void setRid(Long rid) {
		this.rid = rid;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public int getSubtype() {
		return subtype;
	}

	public void setSubtype(Integer subtype) {
		this.subtype = subtype;
	}
}

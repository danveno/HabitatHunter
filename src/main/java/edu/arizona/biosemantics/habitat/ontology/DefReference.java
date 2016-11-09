package edu.arizona.biosemantics.habitat.ontology;

/**
 * the references of term definitions
 * 
 * @author maojin
 * 
 */
public class DefReference {
	private Long did;
	private String tid;
	private String source;// the source of the reference
	private String item;// the detailed item

	public Long getDid() {
		return did;
	}

	public void setDid(Long did) {
		this.did = did;
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}
}

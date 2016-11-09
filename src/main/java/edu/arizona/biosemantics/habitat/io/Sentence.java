package edu.arizona.biosemantics.habitat.io;

public class Sentence {
	private int docid;
	private int sentid;
	private String text;
	
	
	public Sentence(){
		
	}
	
	public Sentence(String text){
		this.text = text;
	}
	
	public Sentence(Integer docid,  Integer sentid, String text) {
		this.docid = docid;
		this.sentid = sentid;
		this.text = text;
	}
	
	public int getDocid() {
		return docid;
	}
	public void setDocid(int docid) {
		this.docid = docid;
	}
	public int getSentid() {
		return sentid;
	}
	public void setSentid(int sentid) {
		this.sentid = sentid;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}
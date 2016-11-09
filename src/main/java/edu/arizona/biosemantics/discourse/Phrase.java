package edu.arizona.biosemantics.discourse;

import java.util.ArrayList;
import java.util.List;

import edu.arizona.biosemantics.bb.BBEntity;



/**
 * phrase structure
 * @author maojin
 *
 */
public class Phrase implements Cloneable{
	private List<Token> phTokens = new ArrayList();
	
	private String text;
	private int offset = -1;
	private int offend = -1;
	private String label;
	
	//parse basic information from tokens
	public void parse(){
		StringBuffer sb = new StringBuffer();
		if(phTokens.size()>0){
			
			Token preToken = phTokens.get(0);
			sb.append(preToken.getText());
			offset = preToken.getOffset();
			offend = preToken.getOffend();
			
			for(int i=1; i<phTokens.size();i++){
				Token curToken = phTokens.get(i);
				if(preToken.getOffend()==curToken.getOffset()){
					sb.append(curToken.getText());
				}else if(preToken.getOffend()<curToken.getOffset()){
					sb.append(" ");
					sb.append(curToken.getText());
				}
				preToken = curToken;
				
				offend = curToken.getOffend();
			}
		}
		text = sb.toString();
	}
	
	
	/**
	 * get labels according to token
	 */
	public void label(){
		
		for(int i=0; i<phTokens.size();i++){
			Token curToken = phTokens.get(i);
			String ner = (String) curToken.getAttribute(TokenAttribute.NER);
			if(ner!=null){
				label = ner;
				if(!label.equals("O")) break;
			}
		}
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getOffend() {
		return offend;
	}

	public void setOffend(int offend) {
		this.offend = offend;
	}

	public List getTokens(){
		return phTokens;
	}
	
	public void addToken(Token token){
		phTokens.add(token);
	}
	
	public Token token(int index){
		return phTokens.get(index);
	}
	
	public void removeToken(int i) {
		phTokens.remove(i);		
	}

	public String toString(){
		if(text==null){
//			StringBuffer sb = new StringBuffer();
//			if(phTokens.size()>0){
//				Token preToken = phTokens.get(0);
//				sb.append(preToken.getText());
//				
//				for(int i=1; i<phTokens.size();i++){
//					Token curToken = phTokens.get(i);
//					if(preToken.getOffend()==curToken.getOffset()){
//						sb.append(curToken.getText());
//					}else if(preToken.getOffend()<curToken.getOffset()){
//						sb.append(" ");
//						sb.append(curToken.getText());
//					}
//					preToken = curToken;
//				}
//			}
			parse();
		}
		return text;
	}

	public Phrase clone(){
		try {
			return (Phrase) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}


	public String getLabel() {
		return label;
	}
	
	public void applyLabel(String label){
		this.label = label;
		for(int i=0; i<phTokens.size();i++){
			Token curToken = phTokens.get(i);
			curToken.setAttribute(TokenAttribute.NER, label);
		}
	}


	public BBEntity toEntity(String docID) {
		BBEntity newEntity = new BBEntity();
		newEntity.setName(this.text);
		newEntity.setStart(this.offset);
		newEntity.setEnd(this.offend);
		newEntity.setDocID(docID);
		newEntity.setTokens(this.phTokens);
		newEntity.setType(this.label);
		return newEntity;
	}
}

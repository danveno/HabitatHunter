package edu.arizona.biosemantics.nlp;

/**
 * items in dependency tree
 * 
 * @author maojin
 *
 */
public class DepTreeItem {
	private int sentId;
	private int itemId;
	private String text;

	public DepTreeItem(int sentId, int itemId, String text) {
		this.sentId = sentId;
		this.itemId = itemId;
		this.text = text;
	}

	public int getSentId() {
		return sentId;
	}

	public void setSentId(int sentId) {
		this.sentId = sentId;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	@Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 17 + sentId;
        hash = hash * 31 + itemId;
        hash = hash * 59 + text.hashCode();
        return hash;
    }
	
	@Override
	public boolean equals(Object dti) {
	   if(this==null&&dti==null) return true;
       if(this==dti) return true;
       if(this.hashCode()==dti.hashCode()) return true;
       return false;
    }

	
	public String toString(){
		return this.sentId+" "+this.itemId+" "+text;
	}
}

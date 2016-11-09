package edu.arizona.biosemantics.nlp;

import java.util.List;

import edu.arizona.biosemantics.discourse.Token;


/**
 * identify phrase header
 * 
 * @author maojin
 *
 */
public class PhraseHeader {
	
	/**
	 * apply a simple method to extract the head of the phrase
	 * @param phrase
	 * @return
	 */
	public String simpleExtract(String phrase){
		String[] words = phrase.split("[\\s+]");
		
		StringBuffer head = new StringBuffer();
		int frontIndex = -1;
		for(String word:words){
			frontIndex++;
			if(word.equals("with")||word.equals("in")||word.equals("of")) break;
		}
		if(frontIndex!=words.length-1){
			for(int i=0;i<frontIndex;i++){
				head.append(words[i]).append(" ");
			}
			return head.toString().trim();
		}
		//System.out.println(frontIndex);
		/*
		int rearIndex = 0;
		for(String word:words){
			rearIndex++;
			if(word.equals("of")) break;
		}
		if(rearIndex!=words.length){
			for(int i=rearIndex;i<words.length;i++){
				head.append(words[i]).append(" ");
			}
			return head.toString().trim();
		}
		*/
		
		return phrase;
	}
	
	/**
	 * extract head string from token list
	 * 
	 * @param tokenList
	 * @return
	 */
	public String extract(List<Token> tokenList){
		return null;
	}
	
	
	public static void main(String[] args){
		//String str="patient with a big humor";
		//String str="a big cup of huge coke";
		//String str="a big eagle in the sky";
		String str="a plane";
		PhraseHeader ph = new PhraseHeader();
		System.out.println(str+" --> "+ph.simpleExtract(str));
	}
}

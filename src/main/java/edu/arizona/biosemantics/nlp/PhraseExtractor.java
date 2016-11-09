package edu.arizona.biosemantics.nlp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.discourse.Phrase;
import edu.arizona.biosemantics.discourse.Token;
import edu.arizona.biosemantics.discourse.TokenAttribute;


/**
 * pattern based phrase extraction for
 * 1) Genia results
 * 2) Stanford Parser
 * 
 * @author maojin
 *
 */
public class PhraseExtractor {
	private Set<String> stopHeadWords = new HashSet();
	
	{
		stopHeadWords.add("other");
		stopHeadWords.add("most");
		stopHeadWords.add("other");
		stopHeadWords.add("and");
		stopHeadWords.add("or");
		stopHeadWords.add("which");
		stopHeadWords.add("it");
		stopHeadWords.add("there");
		stopHeadWords.add("he");
		stopHeadWords.add("this");
		stopHeadWords.add("that");
		stopHeadWords.add("she");
		stopHeadWords.add("we");
	}
	
	private Set<String> stopEndWords = new HashSet();
	
	{
		stopEndWords.add("cell");
		stopEndWords.add("which");
	}
	
	
	
	/**
	 * Genia noun phrases
	 * 1, read the phrase structure: B-NP, I-NP
	 * 2, if the POS tag of the first B-NP is DT, CD, remove this token
	 * 3, if the NP is connected by and/or with tag CC.
	 * 
	 * Reference : Detection and categorization of bacteria habitats using shallow linguistic analysis
	 * 
	 * @param tokenList
	 * @return
	 */
	public List<Phrase> getFromGenia(List<Token> tokenList){
		List<Phrase> phrases = new ArrayList();
		Phrase newPhrase = null;
		boolean isPhrase = false;
		for(Token t:tokenList){
			String phraseTag = (String) t.getAttribute(TokenAttribute.PhraseBIO);
			if(!isPhrase&&"B-NP".equals(phraseTag)){
				isPhrase = true;
				newPhrase = new Phrase();
				newPhrase.addToken(t);
				phrases.add(newPhrase);
			}else if(isPhrase&&phraseTag!=null&&phraseTag.endsWith("NP")){
				newPhrase.addToken(t);
			}else{
				isPhrase = false;
			}
		}
		
		refinePhrase(phrases);
		return phrases;
	}

	/**
	 * 2, remove non-informative terms-----if the POS tag of the first B-NP is DT, CD, remove this token
	 * 3, remove JJ ADV
	 * 3, if the NP is connected by and/or with tag CC.
	 * @param phrases
	 */
	public void refinePhrase(List<Phrase> phrases) {
		//2, if the POS tag of the first B-NP is DT, CD, remove this token
		for(Phrase phrase:phrases){
			Token firstToken = phrase.token(0);
			String pos = (String) firstToken.getAttribute(TokenAttribute.POS);
			
			if("DT".equals(pos)||"CD".equals(pos)||"PRP$".equals(pos)||"PRP".equals(pos)||"PRP$".equals(pos)||stopHeadWords.contains(firstToken.getText().toLowerCase())||firstToken.getText().length()==1){
				phrase.removeToken(0);
			}
			
			if(phrase.getTokens().size()>1){
				Token lastToken = phrase.token(phrase.getTokens().size()-1);
				String lastText = lastToken.getText();
				if(stopEndWords.contains(lastText)){
					//System.out.println(phrase.toString());
					phrase.removeToken(phrase.getTokens().size()-1);
				}
			}
		}
		int size = phrases.size();
		for(int p=0;p<size;p++){
			Phrase phrase = phrases.get(p).clone();
			for(int i=0;i<phrase.getTokens().size();i++){
				Token firstToken = phrase.token(i);
				String pos = (String) firstToken.getAttribute(TokenAttribute.POS);
				
				if("JJ".equals(pos)||"ADV".equals(pos)||"RB".equals(pos)){
					phrase.removeToken(i);
				}else{
					break;
				}
			}
			phrases.add(phrase);
		}
		
		//TODO: Discontinuous entity handling
		
	}
}

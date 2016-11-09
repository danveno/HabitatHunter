package edu.arizona.biosemantics.habitat.feature; 

import java.util.List;

import edu.arizona.biosemantics.discourse.Token;
import edu.arizona.biosemantics.habitat.io.FileUtil;
import edu.arizona.biosemantics.util.StringUtil;


/**
 * the tokens from stanford lemma files are not the same as the tokens from conllx files
 * they are to be aligned
 * @author maojin
 *
 */
public class LemmaAlignment {

	public void align(List<Token> tokenSeqList, String lemmaFileName){
		List<String> lemmaPairLines = FileUtil.readLineFromFile(lemmaFileName);
		int tokenIndex = 0;
		int lemmaIndex = 0;
		
		for(;tokenIndex<tokenSeqList.size()&&lemmaIndex<lemmaPairLines.size();){
			Token token = tokenSeqList.get(tokenIndex);
			String lemmaPair = lemmaPairLines.get(lemmaIndex);
			if(lemmaPair.equals("")){
				lemmaIndex++;
				continue;
			}
			if(token.getText().length()==1){
				tokenIndex++;
				continue;
			}
			
			String[] pair = lemmaPair.split("\t");
			String word = pair[0];
			word = StringUtil.replaceStanfordWilds(word);
			String lemma = pair[1];
			
			if(word.equals(token.getText())){
				token.setLemma(lemma);
				System.out.println(token.getText()+" lemma: "+token.getLemma());
				lemmaIndex++;
				tokenIndex++;
			}else if(word.startsWith(token.getText())){
				System.out.println(token.getText()+" is part1 of  "+word);
				tokenIndex++;
			}else if(word.endsWith(token.getText())){
				System.out.println(token.getText()+" is part2 of  "+word);
				tokenIndex++;
			}else{
				lemmaIndex++;
				System.out.println(token.getText()+" middle  "+word);
			}
			
		}
		
	}
}

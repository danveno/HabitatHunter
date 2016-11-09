package edu.arizona.biosemantics.habitat.sentclf;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.arizona.biosemantics.discourse.Sentence;
import edu.arizona.biosemantics.habitat.extract.RegExpGenerator;


/**
 * judge whether the sentence is a taget sentence which can match the terms
 * Use Exactly Regular Expression Matching
 * 
 * @author maojin
 *
 */
public class SentenceFilterByTerms implements ISentBiClassifier{
	private List<String> regExpList;
	private List<String> terms;
	private RegExpGenerator regExpGenerator;
	
	public SentenceFilterByTerms(List terms){
		this.terms = terms;
		this.regExpGenerator = new RegExpGenerator(terms);
		this.regExpList = regExpGenerator.generate();
	}

	
	public SentenceFilterByTerms(String termFile){
		this.terms = terms;
		this.regExpGenerator = new RegExpGenerator(terms);
		this.regExpList = regExpGenerator.generate();
	}
	
	
	
	public List<String> getRegExpList() {
		return regExpList;
	}

	public List<String> getTerms() {
		return terms;
	}

	public RegExpGenerator getRegExpGenerator() {
		return regExpGenerator;
	}


	/**
	 * use all the regular expressions to test whether the sentence is a
	 * candidate or not
	 * 
	 * @param sentText
	 * @return
	 */
	public boolean isPositive(Sentence sent) {
		for(String regExp : regExpList){
			Matcher matcher = Pattern.compile(regExp).matcher(sent.getText());
			if(matcher.find()) {
				//System.out.println(regExp+" "+sent.getText());
				return true;
			}
		}
		
		return false;
	}

}

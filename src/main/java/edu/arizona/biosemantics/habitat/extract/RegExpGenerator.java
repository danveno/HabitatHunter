package edu.arizona.biosemantics.habitat.extract;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.arizona.biosemantics.habitat.io.TermReader;


/**
 * 
 * "[Ii]nteract(s|ed|ing)?"
 * 
 * Reference:
 * Textpresso: An Ontology-Based Information Retrieval and Extraction System for Biological Literature
 * 
 * @author maojin
 *
 */
public class RegExpGenerator {
	private List<String> terms;
	private List<String> regExpList;
	
	public List<String> getTerms() {
		return terms;
	}

	public List<String> getRegExpList() {
		return regExpList;
	}

	
	public RegExpGenerator(){
		
	}
	
	public RegExpGenerator(String termListFile){
		TermReader termReader = new TermReader();
		terms = termReader.readTermsFromOneColumnFile(termListFile);
	}
	
	public RegExpGenerator(List<String> terms){
		this.terms = terms;
	}
	
	
	/**
	 * generate a list of regular expressions
	 * 
	 * @param termList
	 * @return
	 */
	public List<String> generate(){
		return generate(terms);
	}
	
	/**
	 * generate a list of regular expressions
	 * 
	 * @param termList
	 * @return
	 */
	public List<String> generate(List<String> terms){
		regExpList = new ArrayList();
		for(String term : terms){
			regExpList.add(regExpForTerm(term));
		}
		
		return regExpList;
	}
	

	
	/**
	 * 1: do not use complex expressions
	 * @param line
	 * @return
	 */
	public String regExpForTerm(String term) {
		StringBuffer regExpSB = new StringBuffer();
		System.out.println(term);
		term = replaceWildCard(term);
		System.out.println(term);
		//   "\\s"+term+"[\\,\\.\\s\\?\\:]|\\s"+term+"\\s|^"+term+"\\s|\\s"+term+"$";
		regExpSB.append("[\\,\\.\\s\\?\\:]").append(term).append("[\\,\\.\\s\\?\\:]|^")
			.append(term).append("[\\,\\.\\s\\?\\:]|[\\,\\.\\s\\?\\:]")
			.append(term).append("$");
		//System.out.println(term);
		return regExpSB.toString();
	}
	
	/**
	 * 
	 * @param term
	 * @return
	 */
	public String replaceWildCard(String term){
		return term//.replaceAll("\\%","\\%")
				//.replaceAll("\\_","\\_")
				.replaceAll("\\[","\\\\[")
				.replaceAll("\\]","\\\\]")
				.replaceAll("\\^","\\\\^")
				.replaceAll("\\*","\\\\*")
				.replaceAll("\\?","\\\\?")
				.replaceAll("\\-","\\\\-")
				.replaceAll("\\–","\\\\–")
				.replaceAll("\\(","\\\\(")
				.replaceAll("\\)","\\\\)")
				.replaceAll("\\{","\\\\{")
				.replaceAll("\\}","\\\\}")
				;
	}
	
	
	
	public static void main(String[] args){
		String arg = "Mud and rocks, spruce mire along a creek, surrounded by managed forest m.";
		
		String term = "spruce mire along a creek, surrounded by managed forest";
		
		RegExpGenerator regGenerator = new RegExpGenerator();
		
		String regExp = regGenerator.regExpForTerm(term);
		System.out.println(regExp);
		Matcher matcher = Pattern.compile(regExp).matcher(arg);			
		if(matcher.find()) {
			System.out.println("true");
		}
	}

}

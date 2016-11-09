package edu.arizona.biosemantics.habitat.term;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generator 
 * @author maojin
 *
 */
public class AcronymGenerator {
	private String orgTerm;
	private Set acronyms;
	
	public AcronymGenerator(){
		
	}
	
	public AcronymGenerator(String orgTerm){
		this.orgTerm = orgTerm;
	}
	
	public void setOrgTerm(String orgTerm){
		this.orgTerm = orgTerm;
	}
	
	public Set getAcronyms(){
		if(acronyms==null) this.generateAll();
		return acronyms;
	}
	
	/**
	 * generate andidate 
	 */
	public void generateAll(){
		acronyms = new HashSet();
		if(orgTerm!=null&&orgTerm.trim().length()>0){
			String[] words = orgTerm.split("[\\s&-]+");
			
			acronyms.add(firstLetterAcronym(words));//H influenzae
			acronyms.add(firstLetterDotAcronym(words));//H. influenzae
			acronyms.add(allFirstLetterAcronym(words));//HI
			acronyms.add(allFirstLetterDotAcronym(words));//H.I.
			acronyms.add(allFirstLetterNoStrainAcronym(words));//
		}
	}

	
	public String allFirstLetterNoStrainAcronym(String[] words) {
		StringBuffer sb = new StringBuffer();
		for(String word : words){
			if(!word.equals("strain")) sb.append(Character.toUpperCase(word.charAt(0)));
		}
		return sb.toString();
	}
	
	public String allFirstLetterDotAcronym(String[] words) {
		StringBuffer sb = new StringBuffer();
		for(String word : words){
			sb.append(Character.toUpperCase(word.charAt(0))).append(".");
			
		}
		return sb.toString();
	}

	public String allFirstLetterAcronym(String[] words) {
		StringBuffer sb = new StringBuffer();
		for(String word : words){
			sb.append(Character.toUpperCase(word.charAt(0)));
		}
		return sb.toString();
	}

	public String firstLetterAcronym(String[] words) {
		StringBuffer sb = new StringBuffer();
		sb.append(Character.toUpperCase(words[0].charAt(0)));
		for(int i=1;i<words.length;i++){
			sb.append(" ").append(words[i]);
		}
		return sb.toString();
	}
	
	public String firstLetterDotAcronym(String[] words) {
		StringBuffer sb = new StringBuffer();
		sb.append(Character.toUpperCase(words[0].charAt(0))).append(".");
		for(int i=1;i<words.length;i++){
			sb.append(" ").append(words[i]);
		}
		return sb.toString();
	}
	
	
	public static void main(String[] args){
		AcronymGenerator agen = new AcronymGenerator("Haemophilus influenzae");
		Set<String> acronyms = agen.getAcronyms();
		for(String ac : acronyms){
			System.out.println(ac);
		}
	}
	
}

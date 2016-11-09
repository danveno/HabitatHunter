package edu.arizona.biosemantics.habitat.term;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.bb.BBEntity;


/**
 * Identify acronyms according to rules
 * 
 * @author maojin
 *
 */
public class AcronymDetector {
	
	public Map<String, String> globalAcronyms = new HashMap();
	{
		globalAcronyms.put("MRSA","Staphylococcus aureus");//methicillin-resistant Staphylococcus aureus
		globalAcronyms.put("EPEC","Escherichia coli");//enteropathogenic 
		globalAcronyms.put("NTHi","Haemophilus influenzae");//nontypeable Haemophilus influenzae
		globalAcronyms.put("MDRTB","Multi-drug-resistant tuberculosis");//tuberculosis
		globalAcronyms.put("VRE","Staphylococcus aureus");//Vancomycin-Resistant Enterococci
		globalAcronyms.put("MDRP","Pseudomonas aeruginosa");//multidrug resistant Pseudomonas aeruginosa
		globalAcronyms.put("LAB","Lactobacillales");//multidrug resistant Pseudomonas aeruginosa
	}
	
	
	public String findGlobalAcronym(String acronym){
		return globalAcronyms.get(acronym);
	}
	
	/**
	 * detect whether a term is an acronym based on some simple rules
	 * 
	 * @param term
	 * @return
	 */
	public boolean isAcronym(String term){
		boolean isacy = false;
		if(!isacy){
			isacy = isAllUppercase(term);
		}
		if(!isacy){
			isacy = isFirstTermUpperCase(term);
		}
		if(isacy){
			isacy = term.length()>1;
		}
		//System.out.println(term+" "+isacy);
		return isacy;
	}
	
	/**
	 * detect whether a term is an acronym based on some simple rules
	 * 
	 * @param term
	 * @return
	 */
	public boolean has2Uppercase(String term){
		boolean isacy = false;
		int leng=0;
		for(int i=0;i<term.length();i++){
			if(Character.isUpperCase(term.charAt(i))) leng++;
		}
		//System.out.println(term+" "+isacy);
		return leng>=2;
	}
	
	
	public boolean isAllUppercase(String term){
		return term.equals(term.toUpperCase());
	}
	
	
	public boolean isFirstTermUpperCase(String term){
		String[] words = term.split("[\\s&]+");
		if(words.length>1){
			if(words[0].length()<=2){
				return words[0].equals(words[0].toUpperCase());
			}else{
				return false;
			}
		}else if(words.length==1&&words[0].length()<=5){
			return words[0].charAt(0)==words[0].toUpperCase().charAt(0);
		}else{
			return false;
		}
	}
	
	
	/**
	 * reference: 
	 * Hearst, M. S. (2003). A simple algorithm for identifying abbreviation definitions in biomedical text.
	 * 
	 * 
	 * Method findBestLongForm takes as input a short-form and a long- form
	 * candidate (a list of words) and returns the best long-form that matches
	 * the short-form, or null if no match is found.
	 **/
	public String findBestLongForm(String shortForm, String longForm) {
		int sIndex; // The index on the short form
		int lIndex; // The index on the long form
		char currChar; // The current character to match
		sIndex = shortForm.length() - 1; // Set sIndex at the end of the
		// short form
		lIndex = longForm.length() - 1; // Set lIndex at the end of the
		// long form
		for (; sIndex >= 0; sIndex--) { // Scan the short form starting
			// from end to start
			// Store the next character to match. Ignore case
			currChar = Character.toLowerCase(shortForm.charAt(sIndex));
			// ignore non alphanumeric characters
			if (!Character.isLetterOrDigit(currChar))
				continue;
			// Decrease lIndex while current character in the long form
			// does not match the current character in the short form.
			// If the current character is the first character in the
			// short form, decrement lIndex until a matching character
			// is found at the beginning of a word in the long form.
			while (((lIndex >= 0) && (Character.toLowerCase(longForm
					.charAt(lIndex)) != currChar))
					|| ((sIndex == 0) && (lIndex > 0) && (Character
							.isLetterOrDigit(longForm.charAt(lIndex - 1)))))
				lIndex--;
			// If no match was found in the long form for the current
			// character, return null (no match).
			if (lIndex < 0)
				return null;
			// A match was found for the current character. Move to the
			// next character in the long form.
			lIndex--;
		}
		// Find the beginning of the first word (in case the first
		// character matches the beginning of a hyphenated word).
		lIndex = longForm.lastIndexOf(" ", lIndex) + 1;
		// Return the best long form, the substring of the original
		// long form, starting from lIndex up to the end of the original
		// long form.
		return longForm.substring(lIndex);
	}
	
	
	/*
	 * find the original term for an acronym
	 */
	public String acronymLongForm(BBEntity acronymEntity, List<BBEntity> candEntityList) {
		String acronymName = acronymEntity.getName();
		for(BBEntity docEntity : candEntityList){
			String longTerm = findBestLongForm(acronymName, docEntity.getName());
			if(longTerm!=null) return docEntity.getName();
		}
		return null;
	}
	
	/*
	 * treat the acronym as an anaphor
	 * find its antecedent entity
	 * be sure that the candEntityList is ordered by the occurrence.
	 */
	public BBEntity acronymAntecedentEntity(BBEntity acronymEntity, List<BBEntity> candEntityList) {
		String acronymName = acronymEntity.getName();
		for(BBEntity docEntity : candEntityList){
			String longTerm = findBestLongForm(acronymName, docEntity.getName());
			if(longTerm!=null) return docEntity;
		}
		return null;
	}
	
	
	/**
	 * Escherichia coli	HIT
		E. coli	
		Eco
	 * @param args
	 */
	public static void main(String[] args){
		AcronymDetector acrDetector = new AcronymDetector();
		/*
		acrDetector.isAcronym("H. influenzae");
		acrDetector.isAcronym("HI");
		acrDetector.isAcronym("H influenzae");
		acrDetector.isAcronym("H.I.");
		acrDetector.isAcronym("Haemophilus influenzae type B");
		*/
		
		System.out.println(acrDetector.findBestLongForm("LPSs","lipopolysaccharides"));
		System.out.println(acrDetector.findBestLongForm("R. conorii","two R. conorii reference strains"));
	}

}

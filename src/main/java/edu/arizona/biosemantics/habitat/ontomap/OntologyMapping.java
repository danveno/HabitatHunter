package edu.arizona.biosemantics.habitat.ontomap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import edu.arizona.biosemantics.bb.BBEntity;
import edu.arizona.biosemantics.bb.Config;
import edu.arizona.biosemantics.habitat.feature.UMBCSim;
import edu.arizona.biosemantics.habitat.io.A1FormatFileUtil;
import edu.arizona.biosemantics.habitat.io.FileUtil;
import edu.arizona.biosemantics.habitat.ontology.OntologyManager;
import edu.arizona.biosemantics.habitat.ontology.OntologyOBOParser;
import edu.arizona.biosemantics.habitat.ontology.OntologyTerm;
import edu.arizona.biosemantics.habitat.ontology.ncbi.NamesParser;
import edu.arizona.biosemantics.habitat.term.AcronymGenerator;
import edu.arizona.biosemantics.util.EditDistance;
import edu.arizona.biosemantics.util.SimilarityUtil;
import edu.arizona.biosemantics.util.StringUtil;



/**
 * find term class in ontology classes
 * @author maojin
 *
 */
public class OntologyMapping implements IOntologyMapping {
	
	private AcronymGenerator acronymGenerator = new AcronymGenerator();
	private UMBCSim umbcSim = new UMBCSim();
	
	private OntologyManager ontoMgr;
	
	private Map<String, OntologyTerm> uniqAbbTermMap;//String, OntologyTerm
	
	private Set<String> stopSet;
	
	private String tokenList = null;
	
	
	public OntologyMapping(OntologyManager ontoMgr){
		this.ontoMgr = ontoMgr;
	}
	
	public OntologyMapping(String tokenList){
		ontoMgr = new OntologyManager();
		this.tokenList = tokenList;
	}
	
	public void setOntologyManager(OntologyManager ontoMgr){
		this.ontoMgr = ontoMgr;
	}
	
	public OntologyManager getOntologyManager(){
		return ontoMgr;
	}
	
	public void setStopSet(Set stopSet){
		this.stopSet = stopSet;
	}
	
	/**
	 * generate unique abbreviations
	 */
	public Map generateUniqAbbsMap(){
		
		uniqAbbTermMap = new HashMap();//String, OntologyTerm
		Map<String, Integer> termAbbFreq = new HashMap();
		
		Map<String, OntologyTerm> terms = ontoMgr.termMap;
		Set<String> classSet = terms.keySet();
		for(String classId : classSet){
			OntologyTerm term = terms.get(classId);
			
			String name = term.getPreferredName();
			String[] words = name.split("[\\s&-]+");
			String firstCapAbb = acronymGenerator.firstLetterAcronym(words);
			String firstCapDotAbb = acronymGenerator.firstLetterDotAcronym(words);
			
			uniqAbbTermMap.put(firstCapAbb, term);
			uniqAbbTermMap.put(firstCapDotAbb, term);
			
			Integer freq = termAbbFreq.get(firstCapDotAbb);
			if(freq==null){
				termAbbFreq.put(firstCapAbb, 1);
				termAbbFreq.put(firstCapDotAbb, 1);
			}else{
				termAbbFreq.put(firstCapAbb, freq+1);
				termAbbFreq.put(firstCapDotAbb, freq+1);
			}
		}
		
		//remove abbreviation frequency bigger than one
		Set capAbbSet = termAbbFreq.keySet();
		Iterator capAbbIter = capAbbSet.iterator();
		while(capAbbIter.hasNext()){
			String abb = (String) capAbbIter.next();
			if(termAbbFreq.get(abb)>1){
				uniqAbbTermMap.remove(abb);
			}
		}
		
		
		return uniqAbbTermMap;
	}
	
	
	/**
	 * use a unique abbreviation term list to match the termTxt
	 * @param termTxt
	 * @return
	 */
	public OntologyTerm uniqAbbMatch(String termTxt) {
		if(uniqAbbTermMap==null) this.generateUniqAbbsMap();
		String[] words = termTxt.split("[\\s&-]+");
		String firstCapAbb = acronymGenerator.firstLetterAcronym(words);
		OntologyTerm term = uniqAbbTermMap.get(firstCapAbb);
		if(term!=null) return term;
		String firstCapDotAbb = acronymGenerator.firstLetterDotAcronym(words);
		term = uniqAbbTermMap.get(firstCapDotAbb);
		if(term!=null) return term;
		return null;
	}
	
	
	
	/**
	 * extract the term from the ontology
	 */
	public OntologyTerm exactMatch(String termTxt) {
		Map<String, OntologyTerm> terms = ontoMgr.termMap;
		//Set<OntologyTerm> termSet = terms.values();
		Set<String> classSet = terms.keySet();
		for(String classId : classSet){
			OntologyTerm term = terms.get(classId);
			if(term.getPreferredName().equalsIgnoreCase(termTxt)){
				return term;
			}else{
				String[] synonyms = term.getSynonyms();
				if(synonyms!=null){
					for(String syn : synonyms){
						if(syn.equalsIgnoreCase(termTxt)){
							return term;
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * extract the term from the ontology
	 */
	public OntologyTerm editDistanceMatch(String termTxt) {
		Map<String, OntologyTerm> terms = ontoMgr.termMap;
		//Set<OntologyTerm> termSet = terms.values();
		Set<String> classSet = terms.keySet();
		for(String classId : classSet){
			OntologyTerm term = terms.get(classId);
			
			int distance = EditDistance.computeLevenshteinDistance(term.getPreferredName().toLowerCase(), termTxt.toLowerCase());
			
			if(distance<=2){
				System.out.println(termTxt+" within limited distance is: "+term.getPreferredName().toLowerCase());
				return term;
			}else{
				String[] synonyms = term.getSynonyms();
				if(synonyms!=null){
					for(String syn : synonyms){
						distance = EditDistance.computeLevenshteinDistance(syn.toLowerCase(), termTxt.toLowerCase());
						if(distance<=2){
							System.out.println(termTxt+" within limited distance is: "+syn.toLowerCase());
							return term;
						}
					}
				}
			}
		}
		return null;
	}
	
	
	/**
	 * extract the term from the ontology
	 * 
	 * get the  term with highest Jaccard similarity
	 * 
	 */
	public OntologyTerm jaccardMatch(String termTxt) {
		Map<String, OntologyTerm> terms = ontoMgr.termMap;
		//Set<OntologyTerm> termSet = terms.values();
		Set<String> classSet = terms.keySet();
		OntologyTerm mappedOntoTerms = null;
		double maxJaccard = 0;
		for(String classId : classSet){
			OntologyTerm term = terms.get(classId);
			//not directly jaccard but jaccard with an edit distance
			double jaccard = SimilarityUtil.jaccardLemma(term.getPreferredName().toLowerCase(), termTxt.toLowerCase());
			if(maxJaccard<jaccard){
				mappedOntoTerms = term;
				maxJaccard = jaccard;
				System.out.println(term.getPreferredName().toLowerCase() +"|"+termTxt.toLowerCase()+"|"+jaccard);
			}
			String[] synonyms = term.getSynonyms();
			if(synonyms!=null){
				for(String syn : synonyms){
					jaccard =  SimilarityUtil.jaccardLemma(syn.toLowerCase(), termTxt.toLowerCase());
					if(maxJaccard<jaccard){
						mappedOntoTerms = term;
						maxJaccard = jaccard;
						System.out.println(term.getPreferredName().toLowerCase() +"|"+termTxt.toLowerCase()+"|"+jaccard);
					}
				}
			}
		}
		return mappedOntoTerms;
	}
	
	
	
	
	/**
	 * extract the term from the ontology
	 */
	public Set<String> coverMap(String termTxt) {
		Map<String, OntologyTerm> terms = ontoMgr.termMap;
		Set<String> matchedTerms = new HashSet();
		Set<String> classSet = terms.keySet();
		for(String classId : classSet){
			OntologyTerm term = terms.get(classId);
			
			String termStr = term.getPreferredName();
			String patternString = "\\s"+termStr+"\\s|^"+termStr+"\\s|\\s"+termStr+"$|^"+termStr+"$"; // regular expression pattern
			Pattern pattern = Pattern.compile(patternString);
			Matcher matcher = pattern.matcher(termTxt);			
			if (matcher.find()) {
				matchedTerms.add(termStr);
			}else{
				String[] synonyms = term.getSynonyms();
				if(synonyms!=null){
					for(String syn : synonyms){
						patternString = "\\s"+syn+"\\s|^"+syn+"\\s|\\s"+syn+"$|^"+syn+"$"; // regular expression pattern
						pattern = Pattern.compile(patternString);
						matcher = pattern.matcher(termTxt);			
						if (matcher.find()) {
							matchedTerms.add(syn);
						}
					}
				}
			}
		}
		return matchedTerms;
	}
	
	/**
	 * extract the term from the ontology
	 */
	public Set<OntologyTerm> findCoverredTerm(String termTxt) {
		Map<String, OntologyTerm> terms = ontoMgr.termMap;
		Set<OntologyTerm> matchedTerms = new HashSet();
		Set<String> classSet = terms.keySet();
		for(String classId : classSet){
			OntologyTerm term = terms.get(classId);
			
			String termStr = term.getPreferredName();
			String patternString = "\\s"+termStr+"\\s|^"+termStr+"\\s|\\s"+termStr+"$|^"+termStr+"$"; // regular expression pattern
			Pattern pattern = Pattern.compile(patternString);
			Matcher matcher = pattern.matcher(termTxt);			
			if (matcher.find()) {
				matchedTerms.add(term);
			}else{
				String[] synonyms = term.getSynonyms();
				if(synonyms!=null){
					for(String syn : synonyms){
						patternString = "\\s"+syn+"\\s|^"+syn+"\\s|\\s"+syn+"$|^"+syn+"$"; // regular expression pattern
						pattern = Pattern.compile(patternString);
						matcher = pattern.matcher(termTxt);			
						if (matcher.find()) {
							matchedTerms.add(term);
						}
					}
				}
			}
		}
		return matchedTerms;
	}

	
	/**
	 * public find whether the token exists or not
	 * all terms are converted into lowercases
	 * @param token
	 * @return
	 */
	public boolean testTokenExists(String token){
		token = token.toLowerCase();
		if(token.length()<=2) return false;
		if(stopSet!=null&&stopSet.contains(token)) return false;
		if(ontoMgr.tokenSet==null) ontoMgr.initTokenSet();
		if(ontoMgr.tokenSet.contains(token)) return true;
		if(token.endsWith("s")) token = token.substring(0,token.length()-1);
			else if(token.endsWith("es")) token = token.substring(0,token.length()-2);
		if(ontoMgr.tokenSet.contains(token)) return true;
		return false;
 	}
	
	/**
	 * find the lemma and test whether it's in the terms of ontology
	 * 
	 * @param token
	 * @return
	 */
	public boolean testSimpleToken(String token){
		if(token==null||"".equals(token)) return false;
		token = token.toLowerCase();
		if(token.length()<=2) return false;
		if(stopSet!=null&&stopSet.contains(token)) return false;
		if(ontoMgr.tokenSet==null) ontoMgr.initTokenSet(this.tokenList);
		if(ontoMgr.tokenSet.contains(token)) return true;
		if(token.endsWith("s")) token = token.substring(0,token.length()-1);
			else if(token.endsWith("es")) token = token.substring(0,token.length()-2);
		if(ontoMgr.tokenSet.contains(token)) return true;
		return false;
	}
	
	
	public OntologyTerm selectFromSubClass(OntologyTerm term,
			String entityName) {
		List<OntologyTerm> offspringTerms = ontoMgr.getOffsprings(term);
		System.out.println("offspirngterms:"+offspringTerms.size());
		OntologyTerm highestTerm = null;
		if(offspringTerms.size()>0){
			//highest jaccard value
			//highestTerm = highestJaccard(offspringTerms, term, entityName);
			highestTerm = highestUMBCSim(offspringTerms, term, entityName);
			
			if(highestTerm!=null)  System.out.println("choose the highest jaccard term:"+highestTerm.getPreferredName().toLowerCase() +"|"+entityName.toLowerCase());
		}
		
		if(highestTerm==null) highestTerm = term;
		return highestTerm;
	}
	
	
	public OntologyTerm highestJaccard(List<OntologyTerm> termList, OntologyTerm superTerm, String targetTxt){
		OntologyTerm mappedOntoTerms = null;
		double maxJaccard = SimilarityUtil.jaccardLemma(superTerm.getPreferredName().toLowerCase(), targetTxt.toLowerCase());;
		for(OntologyTerm term : termList){
			//OntologyTerm term = terms.get(classId);
			//not directly jaccard but jaccard with an edit distance
			double jaccard = SimilarityUtil.jaccardLemma(term.getPreferredName().toLowerCase(), targetTxt.toLowerCase());
			if(maxJaccard<jaccard){
				mappedOntoTerms = term;
				maxJaccard = jaccard;
				System.out.println(term.getPreferredName().toLowerCase() +"|"+targetTxt.toLowerCase()+"|"+jaccard);
			}
			String[] synonyms = term.getSynonyms();
			if(synonyms!=null){
				for(String syn : synonyms){
					jaccard =  SimilarityUtil.jaccardLemma(syn.toLowerCase(), targetTxt.toLowerCase());
					if(maxJaccard<jaccard){
						mappedOntoTerms = term;
						maxJaccard = jaccard;
						System.out.println(term.getPreferredName().toLowerCase() +"|"+targetTxt.toLowerCase()+"|"+jaccard);
					}
				}
			}
		}
		return mappedOntoTerms;
	}
	
	
	/**
	 * highest with UMBC Similarity
	 * 
	 * @param termList
	 * @param superTerm
	 * @param targetTxt
	 * @return
	 */
	public OntologyTerm highestUMBCSim(List<OntologyTerm> termList, OntologyTerm superTerm, String targetTxt){
		OntologyTerm mappedOntoTerms = null;
		double maxJaccard = umbcSim.callWebAPI(superTerm.getPreferredName().toLowerCase(), targetTxt.toLowerCase());;
		for(OntologyTerm term : termList){
			//OntologyTerm term = terms.get(classId);
			//not directly jaccard but jaccard with an edit distance
			double jaccard = umbcSim.callWebAPI(term.getPreferredName().toLowerCase(), targetTxt.toLowerCase());
			if(maxJaccard<jaccard){
				mappedOntoTerms = term;
				maxJaccard = jaccard;
				System.out.println(term.getPreferredName().toLowerCase() +"|"+targetTxt.toLowerCase()+"|"+jaccard);
			}
			String[] synonyms = term.getSynonyms();
			if(synonyms!=null){
				for(String syn : synonyms){
					jaccard =  umbcSim.callWebAPI(syn.toLowerCase(), targetTxt.toLowerCase());
					if(maxJaccard<jaccard){
						mappedOntoTerms = term;
						maxJaccard = jaccard;
						System.out.println(term.getPreferredName().toLowerCase() +"|"+targetTxt.toLowerCase()+"|"+jaccard);
					}
				}
			}
		}
		return mappedOntoTerms;
	}
	
	
	/**
	 * highest with UMBC Similarity
	 * 
	 * @param termList
	 * @param superTerm
	 * @param targetTxt
	 * @return
	 */
	public OntologyTerm globalHighestUMBCSim(String targetTxt){
		OntologyTerm mappedOntoTerms = null;
		double maxJaccard = 0;
		ontoMgr.termMap.keySet();
		for(String classId: ontoMgr.termMap.keySet()){
			OntologyTerm term = ontoMgr.termMap.get(classId);
			//not directly jaccard but jaccard with an edit distance
			double jaccard = umbcSim.callWebAPI(term.getPreferredName().toLowerCase(), targetTxt.toLowerCase());
			if(maxJaccard<jaccard){
				mappedOntoTerms = term;
				maxJaccard = jaccard;
				System.out.println(term.getPreferredName().toLowerCase() +"|"+targetTxt.toLowerCase()+"|"+jaccard);
			}
			String[] synonyms = term.getSynonyms();
			if(synonyms!=null){
				for(String syn : synonyms){
					jaccard =  umbcSim.callWebAPI(syn.toLowerCase(), targetTxt.toLowerCase());
					if(maxJaccard<jaccard){
						mappedOntoTerms = term;
						maxJaccard = jaccard;
						System.out.println(term.getPreferredName().toLowerCase() +"|"+targetTxt.toLowerCase()+"|"+jaccard);
					}
				}
			}
		}
		return mappedOntoTerms;
	}
	
	
	
	public static void main(String[] args){
		//Salmonellae
		//egg
		//foods
		//Ozone
		//active
		/*
		String ncbiNamesDmp = "F:\\Habitat\\BacteriaBiotope\\resources\\NCBI Taxonomy\\names.dmp";
		String nodesDmp = "F:\\Habitat\\BacteriaBiotope\\resources\\NCBI Taxonomy\\nodes.dmp";
		NamesParser ncbiNameParser = new NamesParser();
		ncbiNameParser.parse(ncbiNamesDmp);
		Set nodeIds = ncbiNameParser.obtainBacteriaIds(nodesDmp);
		ncbiNameParser.filterBacteria(nodeIds);
		OntologyManager ncbiOntoMgr = ncbiNameParser.getOntology();
		
		//read OntoBiotope
		//OntologyOBOParser biotopeParser = new OntologyOBOParser();
		//biotopeParser.parse(Config.ontoBiotopePath);
		//OntologyManager biotopeOntoMgr = biotopeParser.getOntology();
		//read OntoBiotope
				
		OntologyMapping ncbiOntoMapping = new OntologyMapping(ncbiOntoMgr);
		//OntologyMapping ncbiOntoMapping = new OntologyMapping(Config.ontoBiotopeCleanedTokens);
		Set stopwordSet = FileUtil.readTermSet(Config.stopwordFile);
		ncbiOntoMapping.setStopSet(stopwordSet);
		
		System.out.println(ncbiOntoMapping.testTokenExists("active"));
		System.out.println(ncbiOntoMapping.testTokenExists("Salmonellae"));
		System.out.println(ncbiOntoMapping.testTokenExists("salmonella"));
		System.out.println(ncbiOntoMapping.testTokenExists("egg"));
		System.out.println(ncbiOntoMapping.testTokenExists("foods"));
		System.out.println(ncbiOntoMapping.testTokenExists("food"));
		
		*/
		List<String> lines = FileUtil.readLineFromFile(new File("F:\\Habitat\\BacteriaBiotope\\experiments\\ontology term list\\bacteria_cleaned_tokens.txt"));
		FileWriter ncbiTokensWriter;
		try {
			ncbiTokensWriter = new FileWriter(new File("F:\\Habitat\\BacteriaBiotope\\experiments\\ontology term list\\bacteria_finer_tokens.txt"));
			Set<String> tokenSet = new HashSet();
			for(String line:lines){
				List<String> tokens  = StringUtil.ruleTokenize(line.trim());
				for(String token:tokens) tokenSet.add(token);
			}
			
			for(String token:tokenSet){
				if(token.length()>1) ncbiTokensWriter.write(token+"\n");
			}
			ncbiTokensWriter.flush();
			ncbiTokensWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
}

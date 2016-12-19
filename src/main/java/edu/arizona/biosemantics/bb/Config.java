package edu.arizona.biosemantics.bb;

import java.util.ArrayList;
import java.util.List;

public class Config {
	
	public static String dataset = "test";
	public static String txtFolder ="F:\\Habitat\\BacteriaBiotope\\2013";
	public static String a1AnnFolder ="F:\\Habitat\\BacteriaBiotope\\2013";
	public static String stanfordFolder = "F:\\Habitat\\BacteriaBiotope\\resources\\BB3\\stanford-parser\\"+dataset;
	public static String speciesFolder ="F:\\Habitat\\BacteriaBiotope\\resources\\BB3\\species-dictionary\\"+dataset;
	public static String linnaeusFolder ="F:\\Habitat\\BacteriaBiotope\\resources\\BB3\\linnaeus\\"+dataset;
	public static String cocoaFolder = "F:\\Habitat\\BacteriaBiotope\\resources\\BB3\\cocoa";
	public static String geniatagFolder  ="F:\\Habitat\\BacteriaBiotope\\resources\\BB3\\genia-tagger\\"+dataset;
	public static String nounPhraseFolder = "F:\\Habitat\\BacteriaBiotope\\resources\\BB3\\nounphrase\\"; 
	
	public static String brownClusterFile = "F:\\Habitat\\BacteriaBiotope\\resources\\browncluster\\13trdete_150.txt";
	//public static String brownClusterFile = "F:\\Habitat\\BacteriaBiotope\\resources\\browncluster\\16trdetepl_200.txt";
	
	public static String ncbiNamesDmp = "F:\\Habitat\\BacteriaBiotope\\resources\\NCBI Taxonomy\\names.dmp";
	public static String ncbiCleanedTokens = "F:\\Habitat\\BacteriaBiotope\\resources\\ontology term list\\bacteria_finer_tokens.txt";
	//public static String ncbiCleanedTokens = "F:\\Habitat\\BacteriaBiotope\\experiments\\ontology term list\\ncbi_cleaned_tokens2.txt";
	public static String ncbiFineTokens = "F:\\Habitat\\BacteriaBiotope\\resources\\ontology term list\\bacteria_finer_tokens.txt";
	public static String ncbiNonBacTokens = "F:\\Habitat\\BacteriaBiotope\\resources\\ontology term list\\nonbacteria_clean_tokens.txt";
	//public static String ncbiFineTokens = "F:\\Habitat\\BacteriaBiotope\\experiments\\ontology term list\\ncbi_fine_tokens.txt";
	
	public static String ontoBiotopePath = "F:\\Habitat\\BacteriaBiotope\\resources\\OntoBiotope_BioNLP-ST-2016.obo";
	public static String ontoBiotopeCleanedTokens = "F:\\Habitat\\BacteriaBiotope\\resources\\ontology term list\\ontobio_cleaned_tokens.txt";
	
	public static String stopwordFile ="F:/Habitat/BacteriaBiotope/resources/ontology term list/stopwords.txt";
	
	public static String wordNetDir = "D:\\Program Files\\WordNet\\dict";
	
	public static String nGramTokenizerOptions = "-delimiters ' ' -max 1 -min 1";
	//public static String stringToWordVectorOptions = "-R 1,3,8,9,10,12,13,14,15,16,17,20,21,22 -W " + Integer.MAX_VALUE + " -T -L -M 1 -tokenizer weka.core.tokenizers.WordTokenizer "+nGramTokenizerOptions;
	public static String stringToWordVectorOptions = "-R 1,2,7,8,9,11,12,13,14,15,16,19,20,21 -W " + Integer.MAX_VALUE + " -T -L -M 1 -tokenizer weka.core.tokenizers.WordTokenizer "+nGramTokenizerOptions;
	public static String multiFilterOptions = "-D -F weka.filters.unsupervised.attribute.StringToWordVector " + stringToWordVectorOptions + "";
	public static String libSVMOptions = "-h 0 -S 0 -D 3 -K 2 -G 0 -R 0 -N 0.5 -M 100 -C 2048 -P 1e-3";
	
	public static String wordEmbeddingClusterFile = "F:/Habitat/BacteriaBiotope/resources/embeddingcluster/bb2013_WEC_240.txt";
	//public static String wordEmbeddingClusterFile = "F:/Habitat/BacteriaBiotope/resources/embeddingcluster/bb2016_WEC_150.txt";
	
	public static List<String> strainSpecs = new ArrayList();
	static{
		strainSpecs.add("strain");
		strainSpecs.add("strains");
		strainSpecs.add("serovar");
		strainSpecs.add("serovars");
		strainSpecs.add("serotype");
		strainSpecs.add("serotypes");
		strainSpecs.add("mutants");
		strainSpecs.add("mutant");
		strainSpecs.add("isolate");
		strainSpecs.add("isolates");
		strainSpecs.add("subsp");
		
		strainSpecs.add("biotypes");
		
		//not included at the end
		strainSpecs.add("gen. nov.");
		strainSpecs.add("gen. nov");
		strainSpecs.add("sp.nov.");
		strainSpecs.add("sp.nov");
	}
	
	
	public static List<String> headStrainSpecs = new ArrayList();
	static{
		headStrainSpecs.add("strain");
		headStrainSpecs.add("serovar");
		headStrainSpecs.add("serotype");
		headStrainSpecs.add("mutant");
		strainSpecs.add("isolate");
		strainSpecs.add("subsp");
	}
	
	public static List<String> nomenclaturals = new ArrayList();
	static{
		nomenclaturals.add("spp.");
		nomenclaturals.add("spp");
		nomenclaturals.add("sp.");
		nomenclaturals.add("sp");
	}
	
	public static List<String> mutants = new ArrayList();
	static{
		mutants.add("Ara+");
		mutants.add("Ara-");
	}
	
	
	public static List<String> genericHabitats = new ArrayList();
	
	static{
		genericHabitats.add("antibiotic");
		genericHabitats.add("antimicrobial");
		genericHabitats.add("biopsy specimens");
		genericHabitats.add("biotope");
		genericHabitats.add("carrier");
		genericHabitats.add("cohort");
		genericHabitats.add("culture");
		genericHabitats.add("drug");
		genericHabitats.add("ecosystem");
		genericHabitats.add("environment");
		genericHabitats.add("extract");
		genericHabitats.add("extracellular");
		genericHabitats.add("field");
		genericHabitats.add("growth medium");
		genericHabitats.add("host");
		genericHabitats.add("in vitro");
		genericHabitats.add("in vivo");
		genericHabitats.add("media");
		genericHabitats.add("medium");
		genericHabitats.add("microbe");
		genericHabitats.add("microbial");
		genericHabitats.add("microorganism");
		genericHabitats.add("nature");
		genericHabitats.add("niche");
		genericHabitats.add("population");
		genericHabitats.add("product");
		genericHabitats.add("site");
		genericHabitats.add("solution");
		genericHabitats.add("subject");
		genericHabitats.add("substrate");
		genericHabitats.add("substrat");
		genericHabitats.add("suspension");
		genericHabitats.add("underdevelopped countries");
		genericHabitats.add("vector");
		genericHabitats.add("world");
	}
	
}

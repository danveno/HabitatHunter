package edu.arizona.biosemantics.nlp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.arizona.biosemantics.habitat.io.FileUtil;
import edu.arizona.biosemantics.util.StringUtil;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel.OutputFormat;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.EnglishGrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.UniversalSemanticHeadFinder;
import edu.stanford.nlp.util.CoreMap;


/**
 * Stanford parser
 * @author maojin
 *
 */
public class ParserWrapper {
	private StanfordCoreNLP sfCoreNLP;
	private LexicalizedParser lexParser;
	
	private TreebankLanguagePack treebankPack = new PennTreebankLanguagePack();
	private GrammaticalStructureFactory grammerStruFactory = treebankPack.grammaticalStructureFactory();
	
	public ParserWrapper(StanfordCoreNLP sfCoreNLP,LexicalizedParser lexParser ){
		this.lexParser = lexParser;
		this.sfCoreNLP = sfCoreNLP;
	}

	/**
	 * print conll format file
	 * @param sourceFile
	 * @param outputFile
	 * @throws IOException 
	 */
	public void printParsing(String sourceFile, String outputFile) throws IOException {
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();

		// new StringReader(filename)
		FileWriter fw = new FileWriter(outputFile);
		for (List<HasWord> sentence : new DocumentPreprocessor(sourceFile)) {
			Tree parse = lexParser.apply(sentence);

			GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
			//GrammaticalStructure.printDependencies(gs, gs.typedDependencies(),
			//		parse, true, false);
			fw.write(
					GrammaticalStructure.dependenciesToString(gs, gs.typedDependencies(),
					parse, true, false));
			fw.write("\n");
			fw.flush();
		}
		fw.close();
	}
	
	/**
	 * 
	 * @param file CoNLL_X file
	 * http://ilk.uvt.nl/conll/#dataformat
	 */
	public List<GrammaticalStructure> readConllXFile(String fileName){
		//grammerStruFactory.newGrammaticalStructure(t)
		try {
			List<GrammaticalStructure> gramStruList = EnglishGrammaticalStructure.readCoNLLXGrammaticalStructureCollection(fileName);
			return gramStruList;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * basic universal dependency tree
	 * @param fileName
	 */
	public void parseTypedDependency(String fileName){
		//lexParser.setOptionFlags("-extradependencies","MAXIMAL");//depparse.
		for (List<HasWord> sentence : new DocumentPreprocessor(fileName)) {
			Tree parse = lexParser.parse(sentence);
			GrammaticalStructure gs = grammerStruFactory.newGrammaticalStructure(parse);
			//List<TypedDependency> dtree = (List<TypedDependency>) gs.typedDependenciesCCprocessed(GrammaticalStructure.Extras.MAXIMAL);
			List<TypedDependency> dtree = (List<TypedDependency>) gs.typedDependenciesCCprocessed(GrammaticalStructure.Extras.MAXIMAL);
			for(TypedDependency dep:dtree) System.out.println(dep.toString());
			System.out.println();
			
			//UniversalEnglishGrammaticalStructure uegs = new UniversalEnglishGrammaticalStructure(parse);
		}
	}
	
	/**
	 * shallow parse
	 * @param text
	 * @param outputFile
	 * @throws IOException
	 */
	public void shallowParse(String text, String outputFile) throws IOException{
		Annotation annotation = new Annotation(text);
		this.sfCoreNLP.annotate(annotation);
		//CoreAnnotations.SentencesAnnotation.class
		//Tree tree = annotation.get(TreeAnnotation.class);
		//SemanticGraph dependencies = annotation.get(CollapsedCCProcessedDependenciesAnnotation.class);
		//System.out.println(dependencies);
		FileWriter lemmaFw = new FileWriter(new File(outputFile+".shp"));
		
      	List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
      	for(CoreMap sent: sentences) {
      		 for (CoreLabel token: sent.get(TokensAnnotation.class)) {
               String word = token.get(TextAnnotation.class);
               String lemma = token.get(LemmaAnnotation.class);
               lemmaFw.write(token.word()+"\t"+token.tag()+"\t"+token.beginPosition()+"\t"+token.endPosition()+"\t"+token.lemma());
               lemmaFw.write("\n");
               //lemmaFw.write(word+"\t"+lemma+"\n"+"\t"+token.beginPosition());
      		 }
      		lemmaFw.write("\n");
      	}
      	lemmaFw.flush();
      	lemmaFw.close();
	}
	
	/**
	 * shallow parse
	 * @param text
	 * @param outputFile
	 * @throws IOException
	 */
	public void shallowParse(String text, String outputFile,Annotation annotation) throws IOException{
		FileWriter lemmaFw = new FileWriter(new File(outputFile+".shp"));
		
      	List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
      	for(CoreMap sent: sentences) {
      		 for (CoreLabel token: sent.get(TokensAnnotation.class)) {
               String word = token.get(TextAnnotation.class);
               String lemma = token.get(LemmaAnnotation.class);
               lemmaFw.write(token.word()+"\t"+token.tag()+"\t"+token.beginPosition()+"\t"+token.endPosition()+"\t"+token.lemma());
               lemmaFw.write("\n");
               //lemmaFw.write(word+"\t"+lemma+"\n"+"\t"+token.beginPosition());
      		 }
      		lemmaFw.write("\n");
      	}
      	lemmaFw.flush();
      	lemmaFw.close();
	}
	
	
	public void parseEnhancedTypedDependency(String text, String outputFile) throws IOException{
		Annotation annotation = new Annotation(text);
		this.sfCoreNLP.annotate(annotation);
		//CoreAnnotations.SentencesAnnotation.class
		//Tree tree = annotation.get(TreeAnnotation.class);
		//SemanticGraph dependencies = annotation.get(CollapsedCCProcessedDependenciesAnnotation.class);
		//System.out.println(dependencies);
		FileWriter treeFw = new FileWriter(new File(outputFile+".stp"));
		FileWriter depFw = new FileWriter(new File(outputFile+".dep"));
		FileWriter depccFw = new FileWriter(new File(outputFile+".depcc"));
		
      	List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
      	for(CoreMap sent: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
//            for (CoreLabel token: sent.get(TokensAnnotation.class)) {
//              // this is the text of the token
//              String word = token.get(TextAnnotation.class);
//              // this is the POS tag of the token
//              String pos = token.get(PartOfSpeechAnnotation.class);
//              // this is the NER label of the token
//              String ne = token.get(NamedEntityTagAnnotation.class);       
//            }
      		
            // this is the parse tree of the current sentence
            Tree  tree =  sent.get(TreeAnnotation.class);
            treeFw.write(tree.toString());
            treeFw.write("\n");
           
            
            GrammaticalStructure gs = grammerStruFactory.newGrammaticalStructure(tree);
			//List<TypedDependency> dtree = (List<TypedDependency>) gs.typedDependenciesCCprocessed(GrammaticalStructure.Extras.MAXIMAL);
            
            List<TypedDependency> dtree = (List<TypedDependency>) gs.typedDependencies();
			for(TypedDependency dep:dtree) {
				depFw.write(dep.toString());
				depFw.write("\n");
			}
			depFw.write("\n");
            
           dtree = (List<TypedDependency>) gs.typedDependenciesCCprocessed(GrammaticalStructure.Extras.MAXIMAL);
			for(TypedDependency dep:dtree){
				depccFw.write(dep.toString());
				depccFw.write("\n");
			}
			depccFw.write("\n");
          }
      	 treeFw.flush();
         treeFw.close();
         depFw.flush();
         depFw.close();
         depccFw.flush();
         depccFw.close();
	}
	
	
	public void parseEnhancedTypedDependency(String text, String outputFile, Annotation annotation) throws IOException{
		FileWriter treeFw = new FileWriter(new File(outputFile+".stp"));
		FileWriter depFw = new FileWriter(new File(outputFile+".dep"));
		FileWriter depccFw = new FileWriter(new File(outputFile+".depcc"));
		
      	List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
      	for(CoreMap sent: sentences) {
            // this is the parse tree of the current sentence
            Tree  tree =  sent.get(TreeAnnotation.class);
            treeFw.write(tree.toString());
            treeFw.write("\n");
           
            
            GrammaticalStructure gs = grammerStruFactory.newGrammaticalStructure(tree);
			//List<TypedDependency> dtree = (List<TypedDependency>) gs.typedDependenciesCCprocessed(GrammaticalStructure.Extras.MAXIMAL);
            
            List<TypedDependency> dtree = (List<TypedDependency>) gs.typedDependencies();
			for(TypedDependency dep:dtree) {
				depFw.write(dep.toString());
				depFw.write("\n");
			}
			depFw.write("\n");
            
           dtree = (List<TypedDependency>) gs.typedDependenciesCCprocessed(GrammaticalStructure.Extras.MAXIMAL);
			for(TypedDependency dep:dtree){
				depccFw.write(dep.toString());
				depccFw.write("\n");
			}
			depccFw.write("\n");
          }
      	 treeFw.flush();
         treeFw.close();
         depFw.flush();
         depFw.close();
         depccFw.flush();
         depccFw.close();
	}
	
	
	/**
	 * 
	 * @param text
	 * @param outputFile
	 * @throws IOException
	 */
	public void getPhraseHead(String text, String outputFile) throws IOException{
		Annotation annotation = new Annotation(text);
		this.sfCoreNLP.annotate(annotation);
		//CoreAnnotations.SentencesAnnotation.class
		//Tree tree = annotation.get(TreeAnnotation.class);
		//SemanticGraph dependencies = annotation.get(CollapsedCCProcessedDependenciesAnnotation.class);
		//System.out.println(dependencies);
		FileWriter headFw = new FileWriter(new File(outputFile+".head"));
		UniversalSemanticHeadFinder univHF = new UniversalSemanticHeadFinder();
      	List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
      	for(CoreMap sent: sentences) {
      		 Tree  tree =  sent.get(TreeAnnotation.class);
      		 List<Tree> children = tree.getChildrenAsList();
      		 
      		 for(Tree child: children){
      			findHead(child, tree, univHF,headFw);
      		 }
        }
      	headFw.flush();
      	headFw.close();
	}
	
	/**
	 * 
	 * @param text
	 * @param outputFile
	 * @throws IOException
	 */
	public void getPhraseHead(String text, String outputFile, Annotation annotation) throws IOException{
		FileWriter headFw = new FileWriter(new File(outputFile+".head"));
		UniversalSemanticHeadFinder univHF = new UniversalSemanticHeadFinder();
      	List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
      	for(CoreMap sent: sentences) {
      		 Tree  tree =  sent.get(TreeAnnotation.class);
      		 List<Tree> children = tree.getChildrenAsList();
      		 
      		 for(Tree child: children){
      			findHead(child, tree, univHF,headFw);
      		 }
        }
      	headFw.flush();
      	headFw.close();
	}
	
	
	/**
	 * 
	 * @param text
	 * @param outputFile
	 * @throws IOException
	 */
	public Annotation annotate(String text, String outputFile) throws IOException{
		Annotation annotation = new Annotation(text);
		this.sfCoreNLP.annotate(annotation);
		return annotation;
	}
	
	/**
	 * 
	 * @param node
	 * @param parent
	 * @param headFinder
	 * @param headFw
	 */
	public void findHead(Tree node, Tree parent, HeadFinder headFinder, FileWriter headFw) {
	      if (node == null || node.isLeaf()) {
	         return;
	      }
	      //if node is a NP - Get the terminal nodes to get the words in the NP      
	      if(node.value().equals("NP") ) {

//	         System.out.print(" Noun Phrase is ");
//	         List<Tree> leaves = node.getLeaves();
//
//	         for(Tree leaf : leaves) {
//	            System.out.print(leaf.toString()+" ");
//
//	         }

	         Tree head = node.headTerminal(headFinder, parent);
	         
	         try {
				headFw.write(head+"\t"+((CoreLabel)head.label()).beginPosition()+"\t"+((CoreLabel)head.label()).endPosition()+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}

	    }

	    for(Tree child : node.children()) {
	    	findHead(child, node, headFinder,headFw);
	    }

	 }
	
	
	
	public List tokenize(String file){
		// option #1: By sentence.
	      DocumentPreprocessor dp = new DocumentPreprocessor(file);
	      List<String[]> sentenceList = new ArrayList();
	      for (List<HasWord> sentence : dp) {
	        //System.out.println(sentence);
	    	  String[] sentArr = new String[sentence.size()];
	    	  int i=0;
	    	  for(HasWord word:sentence) sentArr[i++] = word.word();
	    	  sentenceList.add(sentArr);
	      }
	      return sentenceList;
	      /*
	      // option #2: By token
	      PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(new FileReader(arg),
	              new CoreLabelTokenFactory(), "");
	      while (ptbt.hasNext()) {
	        CoreLabel label = ptbt.next();
	        System.out.println(label);
	      }
	      */
	}
	
	public static void main(String[] args){
		Properties stanfordCoreProperties = new Properties();
		stanfordCoreProperties.put("annotators", "tokenize, ssplit, pos, lemma, parse");//, parse
		//stanfordCoreProperties.put("depparse.extradependencies", "MAXIMAL");
		StanfordCoreNLP sfCoreNLP = new StanfordCoreNLP(stanfordCoreProperties);
		
		LexicalizedParser lexParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		
		ParserWrapper parser = new ParserWrapper(sfCoreNLP, lexParser);
		String sourceFile = "F:\\Habitat\\BacteriaBiotope\\2016\\BioNLP-ST-2016_BB-cat_test\\BB-cat-21557059.txt";
		//parser.tokenize(sourceFile);
		
		
		
		
		//String sourceFolder = "F:\\Habitat\\BacteriaBiotope\\resources\\microbiadataset\\combined";
		//String destFolder = "F:\\Habitat\\BacteriaBiotope\\resources\\microbiadataset\\combined_parsed";
		
		String sourceFolder = "F:\\Habitat\\BacteriaBiotope\\2013\\BioNLP-ST-2013_Bacteria_Biotopes_test";
		String destFolder = "F:\\Habitat\\BacteriaBiotope\\resources\\BB3\\stanford-parser\\test\\BioNLP-ST-2013_Bacteria_Biotopes_test";
		File[] files = new File(sourceFolder).listFiles();
		
		
		try {
		//	FileWriter fw = new FileWriter("F:\\Habitat\\BacteriaBiotope\\resources\\microbiadataset\\all_tokens.txt");
		
		for(int i=0; i<files.length;i++){
			File file = files[i];
			String fileName = file.getName();
			if(!fileName.endsWith(".txt")) continue;//only process TEXT file
				String text = FileUtil.readContent(file);
				parser.printParsing(file.getPath(), destFolder+"/"+fileName+".conllx");
				parser.parseEnhancedTypedDependency(text, destFolder+"/"+fileName);
				parser.shallowParse(text, destFolder+"/"+fileName);
				parser.getPhraseHead(text, destFolder+"/"+fileName);
				
//				List<String[]> sents = parser.tokenize(file.getAbsolutePath());
//				for(String[] sent:sents){
//					for(String word:sent){
//						if(!StringUtil.isPunctuation(word)) fw.write(word+" ");
//					}
//					fw.write("\n");
//				}
//				fw.flush();
		}
		
//		fw.close();
		
		
		} catch (IOException e) {
			e.printStackTrace();
		}
//		String file = "F:\\Habitat\\BacteriaBiotope\\resources\\microbiadataset\\combined\\2006151.txt";
//		//parser.parseTypedDependency(file);
//		parser.parseEnhancedTypedDependency("Comparison of constitutive and inducible transcriptional enhancement mediated by kappa B-related sequences: modulation of activity in B cells by human T-cell leukemia virus type I tax gene. ");
	}
}

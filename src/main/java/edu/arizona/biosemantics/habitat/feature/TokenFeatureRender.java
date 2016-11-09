package edu.arizona.biosemantics.habitat.feature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import spark.utils.StringUtils;
import edu.arizona.biosemantics.bb.BBEntity;
import edu.arizona.biosemantics.bb.Config;
import edu.arizona.biosemantics.discourse.Phrase;
import edu.arizona.biosemantics.discourse.Token;
import edu.arizona.biosemantics.discourse.TokenAttribute;
import edu.arizona.biosemantics.habitat.io.FileUtil;
import edu.arizona.biosemantics.habitat.io.PlainTextReader;
import edu.arizona.biosemantics.habitat.ontology.OntologyManager;
import edu.arizona.biosemantics.habitat.ontology.OntologyOBOParser;
import edu.arizona.biosemantics.habitat.ontology.ncbi.NamesParser;
import edu.arizona.biosemantics.habitat.ontomap.OntologyMapping;
import edu.arizona.biosemantics.nlp.DepTreeItem;
import edu.arizona.biosemantics.nlp.PhraseExtractor;
import edu.arizona.biosemantics.util.StringUtil;


/**
 * 
 * @author maojin
 *
 */
public class TokenFeatureRender {
	
	private Map<String, String> brownClusterMap; 
	
	private ResourceReader resourceReader;
	private PlainTextReader plainTextReader;
	private OntologyMapping ncbiOntoMapping;
	private OntologyMapping ncbiNonBacOntoMapping;
	private OntologyMapping biotopeOntoMapping;
	
	private LemmaAlignment lemmaAlignment;
	private WordSenseRender wordSenseRender;
	private PhraseExtractor phraseExtractor;
	
	private int fileNum;
	private int sentenceNum;
	private int tokenNum;
	
	public TokenFeatureRender(){
		resourceReader = new ResourceReader();
		plainTextReader = new PlainTextReader();
		lemmaAlignment = new LemmaAlignment();
		wordSenseRender = new WordSenseRender();
		phraseExtractor = new PhraseExtractor();
		initOntologyMappingTool();
	}
	
	public void initOntologyMappingTool(){
		//read NCBI Taxonomy Bacteria ontology
		Set stopwordSet = FileUtil.readTermSet(Config.stopwordFile);
		ncbiOntoMapping = new OntologyMapping(Config.ncbiCleanedTokens);
		ncbiOntoMapping.setStopSet(stopwordSet);
		
		
		//read NCBI Taxonomy Bacteria ontology
		ncbiNonBacOntoMapping = new OntologyMapping(Config.ncbiNonBacTokens);
		ncbiNonBacOntoMapping.setStopSet(stopwordSet);
		
		//read OntoBiotope
		OntologyManager ontoMgr = new OntologyManager();
		ontoMgr.initTokenSet(Config.ontoBiotopeCleanedTokens);
		biotopeOntoMapping = new OntologyMapping(ontoMgr);
		biotopeOntoMapping.setStopSet(stopwordSet);
		
		brownClusterMap = resourceReader.readBrownCluster(Config.brownClusterFile);
	}
	
	
	/**
	 * render the features for CRF Inputs
	 * The tokens are based on Stanford conllx results.
	 * @param inputFolder
	 * @param datasetName
	 */
	public List<Token> render(String datasetName) {
		// read collx
		String collxDatasetFolder = Config.stanfordFolder + "/" + datasetName;
		System.out.println(collxDatasetFolder);
		// hold all the tokens in the dataset
		List datasetTokens = new ArrayList();

		File stanfordFolderFile = new File(collxDatasetFolder);
		File[] collxFiles = stanfordFolderFile.listFiles();
		String currentFileName = null;
		int lineNum = 1;
		int maxSentId = 0;
		for (File collxFile : collxFiles) {
			if (collxFile.getName().indexOf(".conllx") == -1) {
				continue;
			}

			fileNum++;
			System.out.println("current fileName:" + collxFile.getName());
			datasetTokens.addAll(this.renderForFile(datasetName, collxFile.getName()));
			//maxSentId = tokenList.get(tokenList.size() - 1).getSentenceId();
			//lineNum += tokenList.size() + maxSentId;

//			if (currentFileName != null && !currentFileName.equals(fileName)) {
//				// outputCRFFileBlank(crfFile);
//				lineNum += 1;
//			}
//			currentFileName = fileName;

			// output to a file
			
		}// /one document end
		return datasetTokens;
	}
	
	
	/**
	 * 
	 * @param tokenList
	 * @param pheadList
	 */
	public void renderHeadFeature(List<Token> tokenList, List<Token> pheadList) {
		for(Token token:tokenList){
			for(Token headToken:pheadList){
				if(headToken.getText().equals(token.getText())&&token.getOffend()==headToken.getOffend()&&token.getOffset()==headToken.getOffset()){
					pheadList.remove(headToken);
					token.setAttribute(TokenAttribute.isHead, true);
					break;
				}
			}
		}
	}

	/**
	 * merge the genia token list to Stanford token list, render Phrase Structure and GENIA label
	 * @param sfTokenList Stanford token list
	 * @param gtTokenList GENIA token list
	 * @return
	 */
	public List<Token> mergeStanfordGeniaTokens(List<Token> sfTokenList,
			List<Token> gtTokenList) {
		//System.out.println(sfTokenList.size()+" "+gtTokenList.size());
		int gtIndex = 0;
		boolean gtLonger = false;
		for(Token sfToken : sfTokenList){
			String sfText = sfToken.getText();
			sfText = sfText.replace("`", "'");
			
			Token gtToken = gtTokenList.get(gtIndex);
			String gtText = gtToken.getText();
			
			//System.out.println(sfText+"   ?????     "+gtIndex+" "+gtText);
			if(sfText.equals(gtText)){//the same
				sfToken.setAttribute(TokenAttribute.GeniaLabel, gtToken.getAttribute(TokenAttribute.GeniaLabel));
				sfToken.setAttribute(TokenAttribute.PhraseBIO, gtToken.getAttribute(TokenAttribute.PhraseBIO));
				gtIndex++;//gtToken moves to next
				gtLonger = false;
				//System.out.println(sfText+" equal ===>"+gtText);
			}else if(sfText.startsWith(gtText)){//sfText is longer than gtText..... sfText covers gtText
				sfToken.setAttribute(TokenAttribute.GeniaLabel, gtToken.getAttribute(TokenAttribute.GeniaLabel));
				sfToken.setAttribute(TokenAttribute.PhraseBIO, gtToken.getAttribute(TokenAttribute.PhraseBIO));
				//gtToken moves to next iteratively
				//System.out.println(sfText+" starts ===>"+gtText);
				sfText = sfText.substring(gtText.length(),sfText.length());
				gtIndex++;
				
				String gtText2 = gtTokenList.get(gtIndex).getText();
//				if(!sfText.equals(gtText2)&&gtText2.startsWith(sfText)){
//					System.out.println(sfText+" partof  ===>"+gtText2);
//					gtLonger = true;
//					continue;//next sfToken
//				}else{
					while(!sfText.equals(gtText2)){
						//System.out.println(sfText+" continues  ===>"+gtText2);
						sfText = sfText.substring(gtText2.length(),sfText.length());
						
						gtIndex++;
						gtText2 = gtTokenList.get(gtIndex).getText();
//						if(!sfText.equals(gtText2)&&gtText2.startsWith(sfText)){
//							System.out.println(sfText+" partof  ===>"+gtText2);
//							gtLonger = true;
//							gtIndex--;
//							break;
//						}
					}
//				}
				
				gtIndex++;
			}
//			}else if(!gtLonger&&gtText.startsWith(sfText)){
//				sfToken.setAttribute(TokenAttribute.GeniaLabel, gtToken.getAttribute(TokenAttribute.GeniaLabel));
//				sfToken.setAttribute(TokenAttribute.PhraseBIO, gtToken.getAttribute(TokenAttribute.PhraseBIO));
//				gtLonger = true;
//				System.out.println(sfText+" part of  ===>"+gtText);
//				//gtToken doesnot moves
//			}else if(gtLonger){
//				sfToken.setAttribute(TokenAttribute.GeniaLabel, gtToken.getAttribute(TokenAttribute.GeniaLabel));
//				sfToken.setAttribute(TokenAttribute.PhraseBIO, gtToken.getAttribute(TokenAttribute.PhraseBIO));
//				System.out.println(sfText+" continue part of  ===>"+gtText);
//				if(gtText.endsWith(sfText)){
//					gtLonger = false;
//					gtIndex++;//gtToken moves to next
//				}
//			}
			
			
		}
		return sfTokenList;
	}

	/**
	 * 
	 * @param tokenList
	 * @param deptTreeLines
	 */
	public void extractDependencyFeature(List<Token> tokenList, List<String> deptTreeLines) {
		int startId =0;
		int sentId=0;
		Map<DepTreeItem, Token> tokenmap = new HashMap();
		
		//read all the tokens
		for(String line : deptTreeLines){
			if(line.isEmpty()){
				sentId++;
				continue;
			}
			//System.out.println(line);
			DepTreeItem[] relation = transformRelation(line,sentId);
			DepTreeItem source = relation[1];//by order
			int sourceTokenId = getToken(tokenList, startId, source.getText());
			startId = sourceTokenId;
			Token sourceToken = tokenList.get(sourceTokenId);
			tokenmap.put(source, sourceToken);
		}
		
		//renumber sentence
		sentId=0;
		for(String line : deptTreeLines){
			if(line.isEmpty()){
				sentId++;
				continue;
			}
			
			if(line.startsWith("nsubjpass")||line.startsWith("nsubj")||line.startsWith("dobj")||line.startsWith("iobj")
					||line.startsWith("amod")||line.startsWith("nn")||line.startsWith("nmod")||line.startsWith("case")){
				DepTreeItem[] relation = transformRelation(line,sentId);
				DepTreeItem target = relation[0];
				DepTreeItem source = relation[1];//by order
				
//				int sourceTokenId = getToken(tokenList, startId, source.getText());
//				startId = sourceTokenId;
//				Token sourceToken = tokenList.get(sourceTokenId);
//				tokenmap.put(source, sourceToken);
				
				Token sourceToken = tokenmap.get(source);
				Token targetToken = tokenmap.get(target);
//				if(targetToken==null){
//					int targetTokenId = getToken(tokenList, startId, target.getText());
//					targetToken = tokenList.get(targetTokenId);
//					tokenmap.put(target, targetToken);
//				}
				
				//System.out.println(line);
				//System.out.println("source="+source+" "+sourceToken.getLemma());
				//System.out.println("target="+target+" "+targetToken.getLemma());
				
				//nsubjpass(evaluated-42, immunoreactivities-28)
				//nsubj(suggest-5, lines-2)
				if(line.startsWith("nsubjpass")||line.startsWith("nsubj")){//the lemmas of verbs for which the token acts as a subject
					if(targetToken.getAttribute(TokenAttribute.POS).toString().startsWith("V")) 
						sourceToken.setAttribute(TokenAttribute.subVerb, targetToken.getLemma());
				}else if(line.startsWith("dobj")||line.startsWith("iobj")){//the lemmas of verbs for which the token acts as an object
					
					sourceToken.setAttribute(TokenAttribute.objVerb, targetToken.getLemma());
				}else if(line.startsWith("amod")||line.startsWith("nn")||line.startsWith("nmod")){//Modifier structure for nouns
					//nmod(therapy-6, treatment-9)
					//amod(side-effects-5, major-4)
					//nmod(collected-3, dogs-5)   --- flees were collected from dogs, cats, and opossums.
					sourceToken.setAttribute(TokenAttribute.modNoun, targetToken.getLemma());
					//TODO:multiple values
					//https://www.quora.com/How-do-I-use-machine-learning-with-classification-data-that-has-multiple-values-per-variable
					//http://stackoverflow.com/questions/16657663/how-can-i-deal-with-multiple-values-in-some-attribute
					targetToken.setAttribute(TokenAttribute.modifier, sourceToken.getLemma());
				}else if(line.startsWith("case")){//case structure
					//case(phosphatase-7, of-4)
					sourceToken.setAttribute(TokenAttribute.prepNoun, targetToken.getLemma());
					//TODO:multiple values
					//https://www.quora.com/How-do-I-use-machine-learning-with-classification-data-that-has-multiple-values-per-variable
					//http://stackoverflow.com/questions/16657663/how-can-i-deal-with-multiple-values-in-some-attribute
					targetToken.setAttribute(TokenAttribute.preposition, sourceToken.getLemma());
				}
			
			
			}
		}
		
	}
	
	
	/**
	 * obtain the source and target items in the depedency relation
	 * 
	 * nmod(lines-2, evidence-4)
	 * amod(electrophoresis-18, sulfate-polyacrylamide-16)
	 * 
	 * @param relationStr
	 * @return
	 */
	public DepTreeItem[] transformRelation(String relationStr, int sentId) {
		String twoTermStr = relationStr.substring(relationStr.indexOf("(")+1, relationStr.indexOf(")"));
		String[] terms = twoTermStr.split(",");
		terms = recoverRelation(terms);
		String targetStr = terms[0].substring(0,terms[0].lastIndexOf("-"));
		String targetId = terms[0].substring(terms[0].lastIndexOf("-")+1, terms[0].length());
		DepTreeItem targetItem = new DepTreeItem(sentId, Integer.parseInt(targetId),targetStr.trim());
		
		String sourceStr = terms[1].substring(0,terms[1].lastIndexOf("-"));
		String sourceId = terms[1].substring(terms[1].lastIndexOf("-")+1, terms[1].length());
		DepTreeItem sourceItem = new DepTreeItem(sentId, Integer.parseInt(sourceId),sourceStr.trim());
		//System.out.println(sourceItem);
		//System.out.println(targetItem);
		return new DepTreeItem[]{targetItem, sourceItem};
	}

	/**
	 * every term must have a hypen
	 * @param terms
	 */
	public String[] recoverRelation(String[] terms) {
		List<String> termArr = new ArrayList();
		for(int i=0;i<terms.length;i++){
			if(terms[i].indexOf("-")==-1){
				terms[i+1]=terms[i]+","+terms[i+1];
			}else{
				termArr.add(terms[i]);
			}
		}
		String[] fterms = new String[termArr.size()];
		return termArr.toArray(fterms);
	}

	/**
	 * return the token index in the tokenList
	 * 
	 * @param tokenList
	 * @param sentId
	 * @param startId
	 * @return
	 */
	public int getToken(List<Token> tokenList, int startId, String tokenStr){
		//System.out.println(startId+" "+tokenStr+" "+tokenList.size());
		for(;startId<tokenList.size();startId++){
			Token token = tokenList.get(startId);
			if(token.getText().equals(tokenStr)){
				return startId;
			}
		}
		return -1;
	}

	
	/**
	 * render the features for CRF Inputs
	 * The tokens are based on Stanford conllx results.
	 * @param inputFolder
	 * @param datasetName
	 * @deprecated
	 */
	public List renderCRFInputFeatures(String datasetName) {
		// read collx
		String collxDatasetFolder = Config.stanfordFolder + "/" + datasetName;
		System.out.println(collxDatasetFolder);
		// hold all the tokens in the dataset
		List datasetTokens = new ArrayList();

		File stanfordFolderFile = new File(collxDatasetFolder);
		File[] collxFiles = stanfordFolderFile.listFiles();
		String currentFileName = null;
		System.out.println("collxFiles = " + collxFiles.length);
		int lineNum = 1;
		int maxSentId = 0;
		for (File collxFile : collxFiles) {
			if (collxFile.getName().indexOf(".conllx") == -1) {
				continue;
			}

			String fileName = FileUtil.getFileName(collxFile.getName());
			fileNum++;
			System.out.println("current fileName:" + fileName);

			// Token List in this file
			List<Token> tokenList = resourceReader.readTokenFromConllx(collxFile);

			// obtain offset information
			String txtFile = Config.txtFolder + "/" + datasetName + "/" + fileName + ".txt";
			tokenList = plainTextReader.matchOffset(txtFile, tokenList);
			
			//Lemma Alignement
			String lemmaFileName = Config.stanfordFolder + "/" + datasetName+"/"+fileName+".txt.lemma";
			lemmaAlignment.align(tokenList, lemmaFileName);
			
			if(true) break;
			// Species List
			String speciesRsFile = Config.speciesFolder + "/" + datasetName
					+ "/" + fileName + ".spec";
			List speciesList = resourceReader.readSPECIESResults(speciesRsFile);

			String linnaeusFile = Config.linnaeusFolder + "/" + datasetName
					+ "/" + fileName + ".tags";
			List linRsList = resourceReader.readLinnaeusResults(linnaeusFile);

			// cocoa annotation
			//String cocoaFile = Config.cocoaFolder + "/" + datasetName + "/"
			//		+ fileName + ".cocoa";
			//List cocoaList = resourceReader.readCocoa(cocoaFile);
			//Map cocoaTokenType = getTokenEntityType(cocoaList);

			// the habitat and bacteria annotation
			// Make sure they were in the same place
			String a1File = Config.a1AnnFolder + "/" + datasetName + "/"
					+ fileName + ".a1";
			List<BBEntity> annEntityList = resourceReader
					.readAnnEntities(a1File);
			// List<BBEntity> annCorruptEntities = corruptEntity(annEntityList);

			for (Token token : tokenList) {
				tokenNum++;
				// presence in the results of SPECIES
				boolean isAppearInSpecies = isAppear(token, speciesList);
				if (isAppearInSpecies) {
					token.setAttribute(TokenAttribute.ISInSpecies, 1);
				} else {
					token.setAttribute(TokenAttribute.ISInSpecies, 0);
				}

				// presence in the results of Linnaerus
				boolean isAppearInLinnaerus = isAppear(token, linRsList);
				if (isAppearInLinnaerus) {
					token.setAttribute(TokenAttribute.ISInLinnaerus, 1);
				} else {
					token.setAttribute(TokenAttribute.ISInLinnaerus, 0);
				}

				// presence in OntoBiotope
				boolean inOntoBiotope = biotopeOntoMapping.testSimpleToken(token.getText());
				if (inOntoBiotope) {
					token.setAttribute(TokenAttribute.InOntoBiotope, 1);
				} else {
					token.setAttribute(TokenAttribute.InOntoBiotope, 0);
				}

				// presence in NCBITaxonomy
				boolean inNCBITaxonomy = ncbiOntoMapping.testSimpleToken(token.getText());
				if (inNCBITaxonomy) {
					token.setAttribute(TokenAttribute.InNCBITaxonomy, 1);
				} else {
					token.setAttribute(TokenAttribute.InNCBITaxonomy, 0);
				}

//				String tokenType = (String) cocoaTokenType.get(token.getText());
//				if (tokenType == null)
//					tokenType = "O";
//				token.setAttribute(TokenAttribute.CocoaType, tokenType);

				String bclusterID = brownClusterMap.get(token.getText());
				bclusterID = bclusterID == null ? "O" : bclusterID;
				token.setAttribute(TokenAttribute.BrownCluster, bclusterID);
				/*
				 * for(BBEntity currentEntity : annEntityList){
				 * if(currentEntity.
				 * getStart()<=token.getOffset()&&currentEntity.
				 * getEnd()>=token.getOffend
				 * ()&&currentEntity.getName().indexOf(token.getText())>-1){
				 * System
				 * .out.println(token.getText()+" "+currentEntity.getType()); }
				 * }
				 */
				String nerType = detectNERType(annEntityList, token);
				token.setAttribute(TokenAttribute.NER, nerType);
				// System.out.println(token.getText()+" "+token.getAttribute(TokenAttribute.NER));
				datasetTokens.add(token);
			}
			maxSentId = tokenList.get(tokenList.size() - 1).getSentenceId();
			lineNum += tokenList.size() + maxSentId;

			if (currentFileName != null && !currentFileName.equals(fileName)) {
				// outputCRFFileBlank(crfFile);
				lineNum += 1;
			}
			currentFileName = fileName;

			// output to a file
		}// /one document end
		System.out.println("FileNum=" + fileNum + " SentenceNum=" + sentenceNum
				+ " TokenNum=" + tokenNum);
		return datasetTokens;

	}
	
	
	/**
	 * for a file, get the token list and render the features 
	 * @param datasetName
	 * @param collxFile
	 * @return
	 */
	public List<Token> renderForFile(String datasetName,String collxFileName){
		
		File collxFile = new File(collxFileName);
		List fileTokenList = new ArrayList();
		
		String fileName = FileUtil.getFileName(collxFile.getName());
		System.out.println("current fileName:" + fileName);

		// read token list from Stanford shallow parse results
		String shpFile = Config.stanfordFolder + "/" + datasetName+"/"+fileName+".txt.shp";
		List<Token> tokenList = resourceReader.readTokenFromShallowParse(shpFile);

		// obtain offset information
		String txtFile = Config.txtFolder + "/" + datasetName + "/" + fileName + ".txt";
		//tokenList = plainTextReader.matchOffset(txtFile, tokenList);
		
		// read token list from GENIA tagger results
		String gtFile = Config.geniatagFolder+ "/" + datasetName+"/"+fileName+".gt";
		List gtTokenList = resourceReader.readTokenFromGeniaTagger(gtFile);
		
		gtTokenList = resourceReader.finerTokenize(gtTokenList);
		//TODO: combine them into a final token list
		tokenList = mergeStanfordGeniaTokens(tokenList, gtTokenList);
		
		// read Phrase head
		String pheadFile = Config.stanfordFolder + "/" + datasetName+"/"+fileName+".txt.head";
		List pheadList = resourceReader.readPhraseHead(pheadFile);
		renderHeadFeature(tokenList,pheadList);
		
		//read dependency tree
		String depFile = Config.stanfordFolder + "/" + datasetName+"/"+fileName+".txt.dep";
		//System.out.println(depFile);
		List<String> deptTreeLines = FileUtil.readLineFromFile(depFile);
		extractDependencyFeature(tokenList, deptTreeLines);
		
		// Species List
		//String speciesRsFile = Config.speciesFolder + "/" + datasetName + "/" + fileName + ".spec";
		//List speciesList = resourceReader.readSPECIESResults(speciesRsFile);

		//String linnaeusFile = Config.linnaeusFolder + "/" + datasetName + "/" + fileName + ".tags";
		//List linRsList = resourceReader.readLinnaeusResults(linnaeusFile);
		// cocoa annotation
		/*
		String cocoaFile = Config.cocoaFolder + "/" + datasetName + "/" + fileName + ".cocoa";
		List cocoaList;
		Map cocoaTokenType = null;
		try {
			cocoaList = resourceReader.readCocoa(cocoaFile);
			cocoaTokenType = getTokenEntityType(cocoaList);
		} catch (Exception e) {
			cocoaTokenType = new HashMap();
			e.printStackTrace();
		}
		*/

		// the habitat and bacteria annotation
		// Make sure they were in the same place
		String a1File = Config.a1AnnFolder + "/" + datasetName + "/" + fileName + ".a1";
		List<BBEntity> annEntityList = resourceReader.readAnnEntities(a1File);
		// List<BBEntity> annCorruptEntities = corruptEntity(annEntityList);

		int tokenId = 1;
		for (Token token : tokenList) {
			tokenNum++;
			//re-order
			token.setTokenId(tokenId++);
			
			//features from shallow parse results
			token.setAttribute(TokenAttribute.Length, token.getText().length());
			token.setAttribute(TokenAttribute.Typographic, StringUtil.getTypographic(token.getText()));//4 typographic types
			//token.setAttribute(TokenAttribute.Typographic, getSimpleTypographic(token.getText()));//4 typographic types
			token.setAttribute(TokenAttribute.pPunct, StringUtil.hasPunctuation(token.getText()));//5 isPunctuation  Punc_
			token.setAttribute(TokenAttribute.pDigit, StringUtil.hasDigit(token.getText()));//6 presentsDigit   Digit_
			token.setAttribute(TokenAttribute.pCap, StringUtil.hasCapital(token.getText()));//6 presentsDigit   Digit_
			
			
			/*
			// presence in the results of SPECIES
			boolean isAppearInSpecies = isAppear(token, speciesList);
			if (isAppearInSpecies) {
				token.setAttribute(TokenAttribute.ISInSpecies, 1);
			} else {
				token.setAttribute(TokenAttribute.ISInSpecies, 0);
			}

			// presence in the results of Linnaerus
			boolean isAppearInLinnaerus = isAppear(token, linRsList);
			if (isAppearInLinnaerus) {
				token.setAttribute(TokenAttribute.ISInLinnaerus, 1);
			} else {
				token.setAttribute(TokenAttribute.ISInLinnaerus, 0);
			}
			*/
			// presence in OntoBiotope
			boolean inOntoBiotope = biotopeOntoMapping.testSimpleToken(token.getLemma());
			//if (inOntoBiotope) {
				token.setAttribute(TokenAttribute.InOntoBiotope, inOntoBiotope);
			//} else {
			//	token.setAttribute(TokenAttribute.InOntoBiotope, 0);
			//}

			// presence in NCBITaxonomy
			boolean inNCBITaxonomy = ncbiOntoMapping.testSimpleToken(token.getText());
			//if (inNCBITaxonomy) {
				token.setAttribute(TokenAttribute.InNCBITaxonomy, inNCBITaxonomy);
			//} else {
			//	token.setAttribute(TokenAttribute.InNCBITaxonomy, 0);
			//}

			// presence in NCBITaxonomy
			boolean inNonBacteria = ncbiNonBacOntoMapping.testSimpleToken(token.getText());
			token.setAttribute(TokenAttribute.InNonBacteria, inNonBacteria);
			
			/*
			String tokenType = (String) cocoaTokenType.get(token.getText());
			if (tokenType == null)
				tokenType = "O";
			token.setAttribute(TokenAttribute.CocoaType, tokenType);
			 */
			
			String bclusterID = brownClusterMap.get(token.getText());
			bclusterID = bclusterID == null ? "O" : bclusterID;
			token.setAttribute(TokenAttribute.BrownCluster, bclusterID);
			
			//get word sense
			String posTag = (String)token.getAttribute(TokenAttribute.POS);
			token.setAttribute(TokenAttribute.wordSense, wordSenseRender.getExtendedSense(token.getLemma(), posTag));
			
			/*
			 * for(BBEntity currentEntity : annEntityList){
			 * if(currentEntity.
			 * getStart()<=token.getOffset()&&currentEntity.
			 * getEnd()>=token.getOffend
			 * ()&&currentEntity.getName().indexOf(token.getText())>-1){
			 * System
			 * .out.println(token.getText()+" "+currentEntity.getType()); }
			 * }
			 */
			String nerType = detectNERType(annEntityList, token);
			token.setAttribute(TokenAttribute.NER, nerType);
			if(nerType.equals("Geographical")) System.out.println(token.getAttribute(TokenAttribute.NER));
			fileTokenList.add(token);
		}
		return fileTokenList;
	}
	
	/**
	 * for a file, get the token list and render the features 
	 * @param datasetName
	 * @param collxFile
	 * @return
	 */
	public List<Token> readTokenForFile(String datasetName,String fileName){
		
		System.out.println("current fileName:" + fileName);

		// read token list from Stanford shallow parse results
		String shpFile = Config.stanfordFolder + "/" + datasetName+"/"+fileName+".txt.shp";
		List<Token> tokenList = resourceReader.readTokenFromShallowParse(shpFile);

		// obtain offset information
		String txtFile = Config.txtFolder + "/" + datasetName + "/" + fileName + ".txt";
		//tokenList = plainTextReader.matchOffset(txtFile, tokenList);
		
		// read token list from GENIA tagger results
		String gtFile = Config.geniatagFolder+ "/" + datasetName+"/"+fileName+".gt";
		List gtTokenList = resourceReader.readTokenFromGeniaTagger(gtFile);
		
		gtTokenList = resourceReader.finerTokenize(gtTokenList);
		//TODO: combine them into a final token list
		tokenList = mergeStanfordGeniaTokens(tokenList, gtTokenList);
		
		// read Phrase head
		String pheadFile = Config.stanfordFolder + "/" + datasetName+"/"+fileName+".txt.head";
		List pheadList = resourceReader.readPhraseHead(pheadFile);
		renderHeadFeature(tokenList,pheadList);
		
		//read dependency tree
		String depFile = Config.stanfordFolder + "/" + datasetName+"/"+fileName+".txt.dep";
		List<String> deptTreeLines = FileUtil.readLineFromFile(depFile);
		extractDependencyFeature(tokenList, deptTreeLines);

		return tokenList;
	}
	
	
	
	
	/**
	 * render the features for CRF Inputs
	 * The tokens are based on Stanford conllx results.
	 * @param inputFolder
	 * @param datasetName
	 */
	public void extractPhrase(String datasetName) {
		// read collx
		String collxDatasetFolder = Config.stanfordFolder + "/" + datasetName;
		String npFolder = Config.nounPhraseFolder + "/" + datasetName;
		System.out.println(collxDatasetFolder);
		// hold all the tokens in the dataset
		List datasetTokens = new ArrayList();

		File stanfordFolderFile = new File(collxDatasetFolder);
		File[] collxFiles = stanfordFolderFile.listFiles();
		String currentFileName = null;
		int lineNum = 1;
		int maxSentId = 0;
		for (File collxFile : collxFiles) {
			if (collxFile.getName().indexOf(".conllx") == -1) {
				continue;
			}

			fileNum++;
			System.out.println("current fileName:" + collxFile.getName());
			String collxFileName = collxFile.getName();
			
			List<Phrase> phList = this.extractPhraseForFile(datasetName, collxFileName); 

			String npFile = npFolder+"/"+StringUtil.getFileName(collxFileName)+".np";
			// output to a file
			try {
				FileWriter fw = new FileWriter(npFile);
				for(Phrase phrase:phList){
					List<Token> tokens = phrase.getTokens();
					if(tokens.size()>0&&phrase.toString().length()>1)
						fw.write(phrase.toString()+"\t"+tokens.get(0).getOffset()+"\t"+tokens.get(tokens.size()-1).getOffend()+"\t"+tokens.get(0).getTokenId()+"\t"+tokens.get(tokens.size()-1).getTokenId()+"\n");
				}
				fw.flush();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		}
	}
	
	
	/**
	 * for a file, get the phrases according to Genia Tagger
	 * @param datasetName
	 * @param collxFile
	 * @return
	 */
	public List<Phrase> extractPhraseForFile(String datasetName,String collxFileName){
		
		File collxFile = new File(collxFileName);
		
		String fileName = FileUtil.getFileName(collxFile.getName());
		System.out.println("current fileName:" + fileName);

		// read token list from Stanford shallow parse results
		String shpFile = Config.stanfordFolder + "/" + datasetName+"/"+fileName+".txt.shp";
		List<Token> tokenList = resourceReader.readTokenFromShallowParse(shpFile);

		// obtain offset information
		String txtFile = Config.txtFolder + "/" + datasetName + "/" + fileName + ".txt";
		//tokenList = plainTextReader.matchOffset(txtFile, tokenList);
		
		// read token list from GENIA tagger results
		String gtFile = Config.geniatagFolder+ "/" + datasetName+"/"+fileName+".gt";
		List gtTokenList = resourceReader.readTokenFromGeniaTagger(gtFile);
		
		gtTokenList = resourceReader.finerTokenize(gtTokenList);
		//TODO: combine them into a final token list
		tokenList = mergeStanfordGeniaTokens(tokenList, gtTokenList);
		
		//extract Phrases from Genia results
		List<Phrase> phraseList = phraseExtractor.getFromGenia(tokenList);
		System.out.println("phraselist="+phraseList.size());
		return phraseList;
	}
	
	
	/**
	 *  not used
	 *  
	 * @param datasetName
	 * @return
	 */
	public List<Token> readDatasetTokenSequence(String datasetName){
		//read collx
		String collxDatasetFolder = Config.stanfordFolder+"/"+datasetName;
		
		List<Token> allTokenList = new ArrayList();
		
		//output file
		File stanfordFolderFile = new File(collxDatasetFolder);
		File[] collxFiles = stanfordFolderFile.listFiles();
		System.out.println("isMatch="+collxDatasetFolder);
		for(File collxFile : collxFiles){
			if(collxFile.getName().indexOf(".conllx")==-1){
				continue;
			}
			
			String fileName = FileUtil.getFileName(collxFile.getName());
			fileNum++;
			
			System.out.println("current fileName:"+fileName);
			//Token List in this file with blank lines
			List<Token> tokenList = this.readTokenForFile(datasetName, fileName);
			
			String txtFile = Config.txtFolder+"/"+datasetName+"/"+fileName+".txt";
			plainTextReader.matchOffset(txtFile, tokenList);
			allTokenList.addAll(tokenList);
			//allTokenList.add(null);
		}
		return allTokenList;
	}
	
	
	/**
	 *  not used
	 *  
	 * @param datasetName
	 * @return
	 */
	public List<Token> readTokenSequences(String datasetName){
		//read collx
		String collxDatasetFolder = Config.stanfordFolder+"/"+datasetName;
		
		List<Token> allTokenList = new ArrayList();
		
		//output file
		File stanfordFolderFile = new File(collxDatasetFolder);
		File[] collxFiles = stanfordFolderFile.listFiles();
		
		for(File collxFile : collxFiles){
			if(collxFile.getName().indexOf(".conllx")==-1){
				continue;
			}
			
			String fileName = FileUtil.getFileName(collxFile.getName());
			fileNum++;
			
			System.out.println("current fileName:"+fileName);
			//Token List in this file with blank lines
			List<Token> tokenList = resourceReader.readConllxWithEmptyLine(collxFile);
			
			String txtFile = Config.txtFolder+"/"+datasetName+"/"+fileName+".txt";
			plainTextReader.matchOffset(txtFile, tokenList);
			allTokenList.addAll(tokenList);
			//allTokenList.add(null);
		}
		return allTokenList;
	}
	

	/**
	 * find the type of each token in the terms
	 * @param cocoaList
	 * @return
	 */
	public Map getTokenEntityType(List<BBEntity> cocoaList) {
		Map tokenType = new HashMap();
		for(BBEntity entity : cocoaList){
			String term = entity.getName();
			String type = entity.getType();
			String[] termTokens = term.split(" ");
			for(String token : termTokens){
				tokenType.put(token, type);
			}
		}
		return tokenType;
	}

	/**
	 * 
	 * @param annEntityList
	 * @return
	 */
	public List<BBEntity> corruptEntity(List<BBEntity> annEntityList) {
		List<BBEntity> bbList = new ArrayList<BBEntity>();
		for(BBEntity orgEntity:annEntityList){
			int start = orgEntity.getStart();
			int end = orgEntity.getEnd();
			String entityTxt = orgEntity.getName();
			String[] entityFields = entityTxt.split(" ");
			for(String entityItem:entityFields){
				//String eID, String docID, String name, String type
				bbList.add(new BBEntity(orgEntity.geteID(), orgEntity.getDocID(), entityItem, orgEntity.getType(), start, start+entityItem.length()));
				start=start+entityItem.length()+1;
			}
		}
		return bbList;
	}
	
	/**
	 * 
	 * @param annEntityList
	 * @return
	 */
	public String detectNERType(List<BBEntity> annEntityList, Token token) {
		//List<BBEntity> bbList = new ArrayList<BBEntity>();
		for(BBEntity orgEntity:annEntityList){
			int start = orgEntity.getStart();
			int end = orgEntity.getEnd();
			int start2 = orgEntity.getStart2();
			int end2 = orgEntity.getEnd2();
			String entityTxt = orgEntity.getName();
			String nerType = orgEntity.getType();
			String tokenStr = token.getText();
//			if("-LRB-".equals(tokenStr)) tokenStr="(";
//			if("-RRB-".equals(tokenStr)) tokenStr=")";
			if(((token.getOffset()>=start&&token.getOffend()<=end)||
					(token.getOffset()>=start2&&token.getOffend()<=end2)
					)&&entityTxt.indexOf(tokenStr)>-1){
				return nerType;
			}
		}
		return "O";
	}
	
	
//	/**
//	 * 
//	 * @param tokenList
//	 * @param crfFormatFile
//	 */
//	public void outputCRFFileBlank(String crfFormatFile){
//		try {
//			FileWriter fw = new FileWriter(new File(crfFormatFile),true);
//			fw.write("\n");
//			fw.flush();
//			fw.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	/**
//	 * 
//	 * @param tokenList
//	 * @param crfFormatFile
//	 */
//	public void outputCRFFile(String fileName, List<Token> tokenList, String crfFormatFile, boolean isTrain){
//		try {
//			FileWriter fw = new FileWriter(new File(crfFormatFile),true);
//			//fw.write(fileName+"\n");
//			//Format: 
//			/***
//			 * Column 1: character offset; 2: length in characters;
//				3: token; 4: typographic features; 5: presence of punctuation; 6: presence of digit; 7: length in characters
//				(with a generic ’7’ category for length higher than seven characters); 8: presence of the token in the
//				OntoBiotope ontology; 9: presence of the token in the NCBI taxonomy; 10: category of the token from
//				the Cocoa annotations; 11: cluster identifier; 12: expected answer
//			 */
//			int currentSent = -1;
//			sentenceNum++;
//			String lastLabel = "";
//			for(Token token : tokenList){
//				int sentId = token.getSentenceId();
//				if(currentSent!=-1&&sentId!=currentSent){
//					fw.write("\n");
//					sentenceNum++;
//				}
//				currentSent = sentId;
//				fw.write(token.getText()+" ");//token
//				fw.write(""+token.getOffset()+" ");//1: character offset   OFFSET_ 
//				fw.write(""+token.getText().length()+" ");//2: length in characters;  LEN_
//				fw.write(token.getAttribute(TokenAttribute.POS)+" ");//3  POS;
//				fw.write(getSimpleTypographic(token.getText())+" ");//4 typographic types
//				fw.write(""+isPunctuation(token.getText())+" ");//5 isPunctuation  Punc_
//				fw.write(""+presentsDigit(token.getText())+" ");//6 presentsDigit   Digit_
//				//presence of dot, hyphen, comma
//				fw.write(""+(token.getText().length()>7?7:token.getText().length())+" ");//7: length in characters (with a generic ’7’ category for length higher than seven characters)
//				fw.write(""+token.getAttribute(TokenAttribute.InOntoBiotope)+" ");//presence of the token in the OntoBiotope ontology   OB_
//				fw.write(""+token.getAttribute(TokenAttribute.InNCBITaxonomy)+" ");//presence of the token in the NCBITaxonomy ontology
//				fw.write(""+token.getAttribute(TokenAttribute.CocoaType)+" ");//cocoa type
//				fw.write(""+token.getAttribute(TokenAttribute.BrownCluster)+" ");//BrownCluster
//				//fw.write(""+token.getAttribute(TokenAttribute.ISInSpecies)+" ");//IN SPECIES RESULTS
//				//fw.write(""+token.getAttribute(TokenAttribute.ISInLinnaerus)+" ");//ISInLinnaerus RESULTS
//				//the last one?
//				
//				if(isTrain){
//					String currentLabel = token.getAttribute(TokenAttribute.NER).toString();
//					if(currentLabel.equals("Habitat")&&"Habitat".equals(lastLabel)){
//						fw.write(" I-Habitat");//I-Habitat
//					}else if(currentLabel.equals("Habitat")&&!"Habitat".equals(lastLabel)){
//						fw.write(" B-Habitat");//currentLabel
//					}if(currentLabel.equals("Bacteria")&&"Bacteria".equals(lastLabel)){
//						fw.write(" I-Bacteria");//I-Bacteria
//					}else if(currentLabel.equals("Bacteria")&&!"Bacteria".equals(lastLabel)){
//						fw.write(" B-Bacteria");//B-Bacteria
//					}else if(currentLabel.equals("O")){
//						fw.write(" O");//"O"
//					}
//					lastLabel = currentLabel;
//				}
//				
//				fw.write("\n");
//			}
//			fw.flush();
//			fw.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	
	
	
	
	/**
	 * Different typographic types
	 * AA --- all capitalized
	 * Aa --- First capitalized
	 * aa --- all lowercases
	 * 
	 * 
	 * digits?
	 * O --- not characters
	 * @return
	 */
	public String getSimpleTypographic(String str){
		boolean containsLetter = false;
		//for(int i = 0;i < str.length();i++){
			if(!Character.isLetter(str.charAt(0))){
				containsLetter = true;
				//break;
			}
		//}//
		if(containsLetter) return "O";//others, not characters
		
		if(str.toUpperCase().equals(str)) return "AA";
		if(str.toLowerCase().equals(str)) return "aa";
		return "Aa";
	}
	
	
	
	
	
	/**
	 * 
	 * @param token
	 * @param speciesList
	 * @return
	 */
	public boolean isAppear(Token token, List<Token> matchList) {
		String tokenStr = token.getText();
		for(Token term :matchList ){
			String termStr = term.getText();
			String[] termItems = termStr.split("[\\s]+");
			for(String termItem: termItems){
				if(termItem.equals(tokenStr)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	public static void main(String[] args){
		String datasetName = "BioNLP-ST-2016_BB-cat+ner_dev";
		TokenFeatureRender tokenFeatureRender = new TokenFeatureRender();
		List tokenList = tokenFeatureRender.render(datasetName);
		//tokenFeatureRender.extractPhrase(datasetName);
		//List datasetTokens = tokenFeatureRender.renderCRFInputFeatures(datasetName);
		//tokenFeatureRender.transformRelation("amod(lymphoma-8, mucosa-associated-7)",1);
	}
}

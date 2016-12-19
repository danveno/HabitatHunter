package edu.arizona.biosemantics.habitat.feature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import edu.arizona.biosemantics.bb.Config;
import edu.arizona.biosemantics.discourse.Token;
import edu.arizona.biosemantics.discourse.TokenAttribute;
import edu.arizona.biosemantics.habitat.io.FileUtil;


/**
 * Deal with generate input files for multiple tools
 * 
 * @author maojin
 *
 */
public class FeatureFileGenerator {
	private List<Token> datasetTokenList;//sequenced tokens with features renderred
	
	public FeatureFileGenerator(List datasetTokenList){
		this.datasetTokenList = datasetTokenList;
	}
	
	public FeatureFileGenerator(){
		
	}
	
	public void setDatasetTokenList(List<Token> datasetTokenList){
		this.datasetTokenList = datasetTokenList;
	}
	
	/**
	 * generate the input file for Mallet sequence labelling, CRF/HMM
	 * The features are from TokenFeatureRender
	 * 
	 * @param tokenList
	 * @param crfFormatFile
	 * @param isTrain
	 */
	public void outputCRFFile(String crfFormatFile, boolean isTrain){
		try {
			FileWriter fw = new FileWriter(new File(crfFormatFile),false);
			//fw.write(fileName+"\n");
			//Format: 
			/***
			 * 
			 * The features are:
			 *  0 the text of the token
			 *  1 character offset
				2 typographic type
				3 presence of capitalized characters
				4 presence of punctuation 
				5 presence of digit
				6 length in characters
				
				7 the lemma of the token
				8 POS tag
				9 Phrase tag
				10 is the head of the noun phrase that the token belongs
				
				11 the lemma of the verb for which the token acts as a subject
				12 the lemma of the verb for which the token acts as an object
				13 the lemma of the noun for which the token acts as modifiers
				14 the lemma of the modifier of that token which is a noun.
				15  the lemma of the noun for which the token acts as a preposition
				16  the preposition of the token
				
				17 presence of the token in the OntoBiotope ontology
				18 presence of the token in the NCBI taxonomy of Bacteria
				19 presence of the token in the NCBI taxonomy but bacteria
				//20 the category of the token from the Cocoa annotation
				21 the category of the GENIA Tagger annotation
				22 Cluster identifier according to the Brown cluster
				23 Word sense
				24 label
			 */
			int currentSent = -1;
			String lastLabel = "";
			for(Token token : this.datasetTokenList){
				int sentId = token.getSentenceId();
				if(currentSent!=-1&&sentId!=currentSent){
					fw.write("\n");
				}
				currentSent = sentId;
				fw.write(token.getText()+" ");//0 token
				//fw.write(""+token.getOffset()+" ");//1: character offset
				fw.write(token.getAttribute(TokenAttribute.Typographic)+" ");//2 typographic types
				fw.write((boolean)token.getAttribute(TokenAttribute.pCap)?"pCap":"O");//3 presence of capitalized characters
				fw.write(" ");
				fw.write((boolean)token.getAttribute(TokenAttribute.pPunct)?"pPunct":"O");//4 presence of punctuation 
				fw.write(" ");
				fw.write((boolean)token.getAttribute(TokenAttribute.pDigit)?"pDigit":"O");//5 presentsDigit   Digit_
				fw.write(" ");
				fw.write(token.getText().length()+" ");//6  length in characters;  LEN_
				fw.write(token.getLemma()+" ");//7 the lemma of the token

				fw.write(token.getAttribute(TokenAttribute.POS)+" ");//8   POS;
				
				String phraseTag = (String) token.getAttribute(TokenAttribute.PhraseBIO);
				phraseTag = phraseTag == null ? "O " : phraseTag+" ";
				fw.write(phraseTag);//9  Phrase tag
				
				fw.write(token.getAttribute(TokenAttribute.isHead)!=null?"isHead":"O");//10  head tag
				fw.write(" ");
				
				/*	*/
				String subVerb = (String)token.getAttribute(TokenAttribute.subVerb);
				subVerb=subVerb==null?"O":"sv_"+subVerb;
				fw.write(subVerb+" ");//11 the lemma of the verb for which the token acts as a subject
				String objVerb = (String)token.getAttribute(TokenAttribute.objVerb);
				objVerb=objVerb==null?"O":"ov_"+objVerb;
				fw.write(objVerb+" ");//12 the lemma of the verb for which the token acts as an object
				String modNoun = (String)token.getAttribute(TokenAttribute.modNoun);
				modNoun=modNoun==null?"O":"mn_"+modNoun;
				fw.write(modNoun+" ");//13 the lemma of the noun for which the token acts as modifiers
				String modifier = (String)token.getAttribute(TokenAttribute.modifier);
				modifier=modifier==null?"O":"m_"+modifier;
				fw.write(modifier+" ");//14 the lemma of the modifier of that token which is a noun.
				String preposition = (String)token.getAttribute(TokenAttribute.preposition);
				preposition=preposition==null?"O":"pr_"+preposition;
				fw.write(preposition+" ");//15 the lemma of the modifier of that token which is a noun.
				String prepNoun = (String)token.getAttribute(TokenAttribute.prepNoun);
				prepNoun=prepNoun==null?"O":"pn_"+prepNoun;
				fw.write(prepNoun+" ");//16 the lemma of the modifier of that token which is a noun.
			
				
				//fw.write((token.getText().length()>7?7:token.getText().length())+" ");//7: length in characters (with a generic ’7’ category for length higher than seven characters)
				fw.write((boolean)token.getAttribute(TokenAttribute.InOntoBiotope)?"InOntoB":"O");//17 presence of the token in the OntoBiotope ontology   OB_
				fw.write(" ");
				fw.write((boolean)token.getAttribute(TokenAttribute.InNCBITaxonomy)?"InNCBIT":"O");//18 presence of the token in the NCBITaxonomy ontology of bacteria
				fw.write(" ");
				fw.write((boolean)token.getAttribute(TokenAttribute.InNonBacteria)?"InNonNCBIT":"O");//19 presence of the token in the NCBITaxonomy ontology but bacteria
				fw.write(" ");
				String cocoa = (String)token.getAttribute(TokenAttribute.CocoaType);
				cocoa = cocoa == null ? "O " : cocoa+" ";
				fw.write(cocoa+" ");//cocoa type
				String geniaTagger = (String) token.getAttribute(TokenAttribute.GeniaLabel);
				geniaTagger = geniaTagger == null ? "O " : geniaTagger+" ";
				fw.write(geniaTagger);//19
				//16 GENIA Tagger
				fw.write(token.getAttribute(TokenAttribute.BrownCluster)+" ");//20 BrownCluster
				
				String wordSense = (String) token.getAttribute(TokenAttribute.wordSense);
				fw.write(wordSense==null?"O":"ws_"+wordSense);//21 word sense
				fw.write(" ");
				//fw.write(""+token.getAttribute(TokenAttribute.ISInSpecies)+" ");//IN SPECIES RESULTS
				//fw.write(""+token.getAttribute(TokenAttribute.ISInLinnaerus)+" ");//ISInLinnaerus RESULTS
				//the last one?
				
				if(isTrain){//22
					String currentLabel = token.getAttribute(TokenAttribute.NER).toString();
					if(currentLabel.equals("Habitat")&&"Habitat".equals(lastLabel)){
						fw.write("I-Habitat");//I-Habitat
					}else if(currentLabel.equals("Habitat")&&!"Habitat".equals(lastLabel)){
						fw.write("B-Habitat");//currentLabel
					}else if(currentLabel.equals("Bacteria")&&"Bacteria".equals(lastLabel)){
						fw.write("I-Bacteria");//I-Bacteria
					}else if(currentLabel.equals("Bacteria")&&!"Bacteria".equals(lastLabel)){
						fw.write("B-Bacteria");//B-Bacteria
					}else if(currentLabel.equals("Geographical")&&"Geographical".equals(lastLabel)){
						fw.write("I-Geographical");//I-Geographical
					}else if(currentLabel.equals("Geographical")&&!"Geographical".equals(lastLabel)){
						fw.write("B-Geographical");//B-Geographical
					}else{
						fw.write("O");//"O"
					}
					lastLabel = currentLabel;
				}
				
				fw.write("\n");
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Generate Wapiti input file
	 * Each lines are made of tokens separated either by spaces or by tabulations.
	 * The features are from TokenFeatureRender
	 * The observations must be prefixed by either 'u', 'b' or '*' in order to specify if it is unigram, bigram or both.
	 * @param tokenList
	 * @param crfFormatFile
	 * @param isTrain
	 */
	public void outputWapitiFile(String crfFormatFile, boolean isTrain){
		try {
			FileWriter fw = new FileWriter(new File(crfFormatFile),false);
			//fw.write(fileName+"\n");
			//Format: 
			int currentSent = -1;
			String lastLabel = "";
			for(Token token : this.datasetTokenList){
				int sentId = token.getSentenceId();
				if(currentSent!=-1&&sentId!=currentSent){
					fw.write("\n");
				}
				currentSent = sentId;
				//fw.write("u");
				fw.write(token.getText()+"\t");//0 token
				////fw.write("u");
				//fw.write(""+token.getOffset()+"\t");//1: character offset
				//fw.write("u");
				fw.write(token.getAttribute(TokenAttribute.Typographic)+"\t");//2 typographic types
				//fw.write("u");
				fw.write((boolean)token.getAttribute(TokenAttribute.pCap)?"pCap":"O");//3 presence of capitalized characters
				fw.write("\t");
				//fw.write("u");
				fw.write((boolean)token.getAttribute(TokenAttribute.pPunct)?"pPunct":"O");//4 presence of punctuation 
				fw.write("\t");
				//fw.write("u");
				fw.write((boolean)token.getAttribute(TokenAttribute.pDigit)?"pDigit":"O");//5 presentsDigit   Digit_
				fw.write("\t");
				//fw.write("u");
				fw.write(token.getText().length()+"\t");//6  length in characters;  LEN_
				//fw.write("u");
				fw.write(token.getLemma()+"\t");//7 the lemma of the token
				//fw.write("u");
				fw.write(token.getAttribute(TokenAttribute.POS)+"\t");//8   POS;
				
				String phraseTag = (String) token.getAttribute(TokenAttribute.PhraseBIO);
				phraseTag = phraseTag == null ? "O " : phraseTag+"\t";
				//fw.write("u");
				fw.write(phraseTag);//9  Phrase tag
				//fw.write("u");
				fw.write(token.getAttribute(TokenAttribute.isHead)!=null?"isHead":"O");//10  head tag
				fw.write("\t");
				
				/*	*/
				String subVerb = (String)token.getAttribute(TokenAttribute.subVerb);
				subVerb=subVerb==null?"O":"sv_"+subVerb;
				//fw.write("u");
				fw.write(subVerb+"\t");//11 the lemma of the verb for which the token acts as a subject
				String objVerb = (String)token.getAttribute(TokenAttribute.objVerb);
				objVerb=objVerb==null?"O":"ov_"+objVerb;
				//fw.write("u");
				fw.write(objVerb+"\t");//12 the lemma of the verb for which the token acts as an object
				String modNoun = (String)token.getAttribute(TokenAttribute.modNoun);
				modNoun=modNoun==null?"O":"mn_"+modNoun;
				//fw.write("u");
				fw.write(modNoun+"\t");//13 the lemma of the noun for which the token acts as modifiers
				String modifier = (String)token.getAttribute(TokenAttribute.modifier);
				//fw.write("u");
				modifier=modifier==null?"O":"m_"+modifier;
				//fw.write("u");
				fw.write(modifier+"\t");//14 the lemma of the modifier of that token which is a noun.
				
				String preposition = (String)token.getAttribute(TokenAttribute.preposition);
				preposition=preposition==null?"O":"pr_"+preposition;
				//fw.write("u");
				fw.write(preposition+"\t");//15 the lemma of the modifier of that token which is a noun.
				String prepNoun = (String)token.getAttribute(TokenAttribute.prepNoun);
				prepNoun=prepNoun==null?"O":"pn_"+prepNoun;
				//fw.write("u");
				fw.write(prepNoun+"\t");//16 the lemma of the modifier of that token which is a noun.
			
				
				//fw.write((token.getText().length()>7?7:token.getText().length())+"\t");//7: length in characters (with a generic ’7’ category for length higher than seven characters)
				//fw.write("u");
				fw.write((boolean)token.getAttribute(TokenAttribute.InOntoBiotope)?"InOntoB":"O");//17 presence of the token in the OntoBiotope ontology   OB_
				fw.write("\t");
				//fw.write("u");
				fw.write((boolean)token.getAttribute(TokenAttribute.InNCBITaxonomy)?"InNCBIT":"O");//18 presence of the token in the NCBITaxonomy ontology
				fw.write("\t");
				fw.write((boolean)token.getAttribute(TokenAttribute.InNonBacteria)?"InNonNCBIT":"O");//19 presence of the token in the NCBITaxonomy ontology but bacteria
				fw.write("\t");
				/*
				String cocoa = (String)token.getAttribute(TokenAttribute.CocoaType);
				cocoa = cocoa == null ? "O" : cocoa;
				fw.write(cocoa+"\t");//cocoa type
				*/
				String geniaTagger = (String) token.getAttribute(TokenAttribute.GeniaLabel);
				geniaTagger = geniaTagger == null ? "O\t" : geniaTagger+"\t";
				//fw.write("u");
				fw.write(geniaTagger);//19
				//16 GENIA Tagger
				//fw.write("u");
				fw.write(token.getAttribute(TokenAttribute.BrownCluster)+"\t");//20 BrownCluster
				
				String wordSense = (String) token.getAttribute(TokenAttribute.wordSense);
				//fw.write("u");
				fw.write(wordSense==null?"O":"ws_"+wordSense);//21 word sense
				fw.write("\t");
				
				String wordembCluster = (String) token.getAttribute(TokenAttribute.wordEmbedCluster);
				//fw.write("u");
				fw.write(wordembCluster==null?"O":wordembCluster);//21 word sense
				fw.write("\t");
				
				////fw.write(""+token.getAttribute(TokenAttribute.ISInSpecies)+"\t");//IN SPECIES RESULTS
				//fw.write(""+token.getAttribute(TokenAttribute.ISInLinnaerus)+"\t");//ISInLinnaerus RESULTS
				//the last one?
				
				if(isTrain){//22
					String currentLabel = token.getAttribute(TokenAttribute.NER).toString();
					if(currentLabel.equals("Habitat")&&"Habitat".equals(lastLabel)){
						fw.write("I-Habitat");//I-Habitat
					}else if(currentLabel.equals("Habitat")&&!"Habitat".equals(lastLabel)){
						fw.write("B-Habitat");//currentLabel
					}if(currentLabel.equals("Bacteria")&&"Bacteria".equals(lastLabel)){
						fw.write("I-Bacteria");//I-Bacteria
					}else if(currentLabel.equals("Bacteria")&&!"Bacteria".equals(lastLabel)){
						fw.write("B-Bacteria");//B-Bacteria
					}else if(currentLabel.equals("Geographical")&&"Geographical".equals(lastLabel)){
						fw.write("I-Geographical");//I-Geographical
					}else if(currentLabel.equals("Geographical")&&!"Geographical".equals(lastLabel)){
						fw.write("B-Geographical");//B-Geographical
					}else if(currentLabel.equals("O")){
						fw.write("O");//"O"
					}
					lastLabel = currentLabel;
				}
				
				fw.write("\n");
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * 
	 * @param subsetIndex
	 * @param allFeatureFile
	 * @param excerptFile
	 * @param containLabel ---- the last one is the label
	 */
	public void excerptFeatures(int[] subsetIndex, String allFeatureFile, String excerptFile){
		List<String> features = FileUtil.readLineFromFile(allFeatureFile);
		
		try {
			FileWriter fw = new FileWriter(excerptFile);
			for(String line : features){
				line = line.trim();
				if(line.equals("")){
					fw.write("\n");
					continue;
				}
				String[] fields = line.split("[\\s]+");
				StringBuffer sb = new StringBuffer();
				for(int index : subsetIndex){
					sb.append(fields[index]).append(" ");					
				}
				fw.write(sb.toString().trim());
				fw.write("\n");
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	public void truncate(String sourceFile, String truncatedFile){
		List<String> lines = FileUtil.readLineFromFile(sourceFile);
		int[] distance = new int[lines.size()];
		int[] reverseDis = new int[lines.size()];
		
		int alway = 0;
		int index =0;
		for(String line : lines){
			if(line.endsWith("Habitat")||line.endsWith("Bacteria")||line.equals("")) alway = 0;
			else alway++;
			distance[index++]=alway;
		}
		
		alway=0;
		for(index--;index>=0;index--){
			String line = lines.get(index);
			if(line.endsWith("Habitat")||line.endsWith("Bacteria")||line.equals("")) alway = 0;
				else alway++;
			reverseDis[index]=alway;
		}
		
		
		FileWriter fw;
		try {
			fw = new FileWriter(truncatedFile);
			for(index++;index<lines.size();index++){
				String line = lines.get(index);
				if(distance[index]>5&&reverseDis[index]>5&&!"".equals(line)){
					continue;
				}
				fw.write(line);
				fw.write("\n");
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	public static void main(String[] args){
		/*
		 * BioNLP-ST-2016_BB-cat+ner_plus
		 * BioNLP-ST-2013_Bacteria_Biotopes_train
		 * BioNLP-ST-2016_BB-event_test
		 * 
		 */
		//String datasetName = "BioNLP-ST-2016_BB-event_test";
		//String datasetName = "BioNLP-ST-2013_Bacteria_Biotopes_train";//BioNLP-ST-2016_BB-event_test,BioNLP-ST-2013_Bacteria_Biotopes_test
		
		
		/*
		boolean isTrain = true;
		//String crfFormatFile = "F:/Habitat/BacteriaBiotope/experiments/seqlab/2016/wordembed/"+datasetName+"_wec_"+clusterNum+".txt";
		TokenFeatureRender tokenFeatureRender = new TokenFeatureRender();
		List tokenList = tokenFeatureRender.render(datasetName);
		
		FeatureFileGenerator genFeatuerFile = new FeatureFileGenerator(tokenList);
		//genFeatuerFile.outputCRFFile(crfFormatFile, isTrain);
		String wptFormatFile = "F:/Habitat/BacteriaBiotope/experiments/seqlab/2013/"+datasetName+".txt";
		//String wptFormatFile = "F:/Habitat/BacteriaBiotope/experiments/seqlab/2016/"+datasetName+".txt";
		genFeatuerFile.outputWapitiFile(wptFormatFile, isTrain);
		*/
		/*
		for(int clusterNum=110; clusterNum<=300; clusterNum=clusterNum+10){
			//Config.brownClusterFile = "F:\\Habitat\\BacteriaBiotope\\resources\\browncluster\\13trdete_"+clusterNum+".txt";
			Config.wordEmbeddingClusterFile ="F:/Habitat/BacteriaBiotope/resources/embeddingcluster/bb2013_WEC_"+clusterNum+".txt";
			boolean isTrain = true;
			//String crfFormatFile = "F:/Habitat/BacteriaBiotope/experiments/seqlab/2013/wordembed/"+datasetName+"_wec_"+clusterNum+".txt";
			TokenFeatureRender tokenFeatureRender = new TokenFeatureRender();
			List tokenList = tokenFeatureRender.render(datasetName);
			
			FeatureFileGenerator genFeatuerFile = new FeatureFileGenerator(tokenList);
			//genFeatuerFile.outputCRFFile(crfFormatFile, isTrain);
			String wptFormatFile = "F:/Habitat/BacteriaBiotope/experiments/seqlab/2013/wordembed/"+datasetName+"_wc_"+clusterNum+".txt";
			//String wptFormatFile = "F:/Habitat/BacteriaBiotope/experiments/seqlab/2016/"+datasetName+".txt";
			genFeatuerFile.outputWapitiFile(wptFormatFile, isTrain);
			boolean isTrain = true;
			TokenFeatureRender tokenFeatureRender = new TokenFeatureRender();
			List tokenList = tokenFeatureRender.render(datasetName);
			
			FeatureFileGenerator genFeatuerFile = new FeatureFileGenerator(tokenList);
			String wptFormatFile = "F:/Habitat/BacteriaBiotope/experiments/seqlab/2013/wordembed/"+datasetName+"_wec_"+clusterNum+".gsd";
			genFeatuerFile.outputWapitiFile(wptFormatFile, isTrain);
		}
	*/
		//FeatureFileGenerator genFeatuerFile = new FeatureFileGenerator();
		//genFeatuerFile.truncate("F:/Habitat/BacteriaBiotope/experiments/CRFinputs/all/"+datasetName+".txt", "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/all/"+datasetName+"_tr.txt");

		/**/
		
		String datasetName = "BioNLP-ST-2013_Bacteria_Biotopes_test";//BioNLP-ST-2016_BB-event_test,BioNLP-ST-2013_Bacteria_Biotopes_test
		boolean isTrain = true;
		//String crfFormatFile = "F:/Habitat/BacteriaBiotope/experiments/seqlab/2013/"+datasetName+".gsd";
		TokenFeatureRender tokenFeatureRender = new TokenFeatureRender();
		List tokenList = tokenFeatureRender.render(datasetName);
		
		FeatureFileGenerator genFeatuerFile = new FeatureFileGenerator(tokenList);
		String wptFormatFile = "F:/Habitat/BacteriaBiotope/experiments/seqlab/2013/"+datasetName+".gsd";
		genFeatuerFile.outputWapitiFile(wptFormatFile, isTrain);
		//genFeatuerFile.outputCRFFile(crfFormatFile, isTrain);
		
		
		datasetName = "BioNLP-ST-2013_Bacteria_Biotopes_test";
		isTrain = false;
		tokenFeatureRender = new TokenFeatureRender();
		tokenList = tokenFeatureRender.render(datasetName);
		
		genFeatuerFile = new FeatureFileGenerator(tokenList);
		//genFeatuerFile.outputCRFFile(crfFormatFile, isTrain);
		wptFormatFile = "F:/Habitat/BacteriaBiotope/experiments/seqlab/2013/"+datasetName+".txt";
		genFeatuerFile.outputWapitiFile(wptFormatFile, isTrain);
		
			
		/***
		 * 
		 * The features are:
		 *  0 the text of the token
		 * # 1 character offset
			1 typographic type
			2 presence of capitalized characters
			3 presence of punctuation 
			4 presence of digit
			5 length in characters
			
			6 the lemma of the token
			7 POS tag
			8 Phrase tag
			9 is the head of the noun phrase that the token belongs
			
			10 the lemma of the verb for which the token acts as a subject
			11 the lemma of the verb for which the token acts as an object
			12 the lemma of the noun for which the token acts as modifiers
			13 the lemma of the modifier of that token which is a noun.
			14  the lemma of the noun for which the token acts as a preposition
			15  the preposition of the token
			
			16 presence of the token in the OntoBiotope ontology
			17 presence of the token in the NCBI taxonomy
			18 presence of the token in the NCBI taxonomy but bacteria
			#19 the category of the token from the Cocoa annotation
			#19 the category of the GENIA Tagger annotation
			20 Cluster identifier according to the Brown cluster
			21 Word sense
			22 word embedding
			23 label
		
		
		 
		FeatureFileGenerator genFeatuerFile = new FeatureFileGenerator();
		int[] orthIndex = new int[]{0,1,2,5,7,16,17,19};
		int[] orthGsdIndex = new int[]{0,1,2,5,7,16,17,19,21};
		//train
		String allFeatureFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/all5/BioNLP-ST-2016_BB-event+ner_train_all.wpt";
		String excerptFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/wapiti/BioNLP-ST-2016_BB-event+ner_train.wpt";
		genFeatuerFile.excerptFeatures(orthGsdIndex, allFeatureFile, excerptFile);
		
		
		//dev gold
		allFeatureFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/all5/BioNLP-ST-2016_BB-event+ner_dev_all.wptgsd";
		excerptFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/wapiti/BioNLP-ST-2016_BB-event+ner_dev.gsd";
		genFeatuerFile.excerptFeatures(orthGsdIndex, allFeatureFile, excerptFile);
		
		//dev test
		allFeatureFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/all5/BioNLP-ST-2016_BB-event+ner_dev_all.wpt";
		excerptFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/wapiti/BioNLP-ST-2016_BB-event+ner_dev.wpt";
		genFeatuerFile.excerptFeatures(orthIndex, allFeatureFile, excerptFile);
				 */
		
		/*	
		int[] orthMorphIndex = new int[]{0,1,2,3,4,5,6,7,8,9,10};
		int[] orthMorphGsdIndex = new int[]{0,1,2,3,4,5,6,7,8,9,10, 22};
		
		//train
		allFeatureFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/all/BioNLP-ST-2016_BB-cat+ner_train.txt";
		excerptFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/orth_morph/BioNLP-ST-2016_BB-cat+ner_train.txt";
		genFeatuerFile.excerptFeatures(orthMorphGsdIndex, allFeatureFile, excerptFile);
		
		//dev gold
		allFeatureFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/all/BioNLP-ST-2016_BB-cat+ner_dev.gsd";
		excerptFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/orth_morph/BioNLP-ST-2016_BB-cat+ner_dev.gsd";
		genFeatuerFile.excerptFeatures(orthMorphGsdIndex, allFeatureFile, excerptFile);
		
		//dev test
		allFeatureFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/all/BioNLP-ST-2016_BB-cat+ner_dev.txt";
		excerptFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/orth_morph/BioNLP-ST-2016_BB-cat+ner_dev.txt";
		genFeatuerFile.excerptFeatures(orthMorphIndex, allFeatureFile, excerptFile);
		
		int[] orthSynIndex = new int[]{0,1,2,3,4,5,6,11,12,13,14,15,16};
		int[] orthSynGsdIndex = new int[]{0,1,2,3,4,5,6,11,12,13,14,15,16,22};
		
		//train
		allFeatureFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/all/BioNLP-ST-2016_BB-cat+ner_train.txt";
		excerptFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/orth_syn/BioNLP-ST-2016_BB-cat+ner_train.txt";
		genFeatuerFile.excerptFeatures(orthSynGsdIndex, allFeatureFile, excerptFile);
		
		//dev gold
		allFeatureFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/all/BioNLP-ST-2016_BB-cat+ner_dev.gsd";
		excerptFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/orth_syn/BioNLP-ST-2016_BB-cat+ner_dev.gsd";
		genFeatuerFile.excerptFeatures(orthSynGsdIndex, allFeatureFile, excerptFile);
		
		//dev test
		allFeatureFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/all/BioNLP-ST-2016_BB-cat+ner_dev.txt";
		excerptFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/orth_syn/BioNLP-ST-2016_BB-cat+ner_dev.txt";
		genFeatuerFile.excerptFeatures(orthSynIndex, allFeatureFile, excerptFile);
		
		int[] orthknowIndex = new int[]{0,1,2,3,4,5,6,17,18,19,20,21};
		int[] orthknowGsdIndex = new int[]{0,1,2,3,4,5,6,17,18,19,20,21,22};
		
		//train
		allFeatureFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/all/BioNLP-ST-2016_BB-cat+ner_train.txt";
		excerptFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/orth_know/BioNLP-ST-2016_BB-cat+ner_train.txt";
		genFeatuerFile.excerptFeatures(orthknowGsdIndex, allFeatureFile, excerptFile);
		
		//dev gold
		allFeatureFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/all/BioNLP-ST-2016_BB-cat+ner_dev.gsd";
		excerptFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/orth_know/BioNLP-ST-2016_BB-cat+ner_dev.gsd";
		genFeatuerFile.excerptFeatures(orthknowGsdIndex, allFeatureFile, excerptFile);
		
		//dev test
		allFeatureFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/all/BioNLP-ST-2016_BB-cat+ner_dev.txt";
		excerptFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/orth_know/BioNLP-ST-2016_BB-cat+ner_dev.txt";
		genFeatuerFile.excerptFeatures(orthknowIndex, allFeatureFile, excerptFile);
		
				
		int[] orthMorphSynIndex = new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
		int[] orthMorphSynGsdIndex = new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,22};
		//train
		allFeatureFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/all/BioNLP-ST-2016_BB-cat+ner_train.txt";
		excerptFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/orth_morph_syn/BioNLP-ST-2016_BB-cat+ner_train.txt";
		genFeatuerFile.excerptFeatures(orthMorphSynGsdIndex, allFeatureFile, excerptFile);
		
		//dev gold
		allFeatureFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/all/BioNLP-ST-2016_BB-cat+ner_dev.gsd";
		excerptFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/orth_morph_syn/BioNLP-ST-2016_BB-cat+ner_dev.gsd";
		genFeatuerFile.excerptFeatures(orthMorphSynGsdIndex, allFeatureFile, excerptFile);
		
		//dev test
		allFeatureFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/all/BioNLP-ST-2016_BB-cat+ner_dev.txt";
		excerptFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/orth_morph_syn/BioNLP-ST-2016_BB-cat+ner_dev.txt";
		genFeatuerFile.excerptFeatures(orthMorphSynIndex, allFeatureFile, excerptFile);
		
		
		int[] groinIndex = {0,1,2,3,4,5,6,7,8,17,18,20};
		int[] groinGSDIndex = new int[]{0,1,2,3,4,5,6,7,8,17,18,20,22};
		//train
		allFeatureFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/all/BioNLP-ST-2016_BB-cat+ner_train.txt";
		excerptFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/groin/BioNLP-ST-2016_BB-cat+ner_train.txt";
		genFeatuerFile.excerptFeatures(groinGSDIndex, allFeatureFile, excerptFile);
		
		//dev gold
		allFeatureFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/all/BioNLP-ST-2016_BB-cat+ner_dev.gsd";
		excerptFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/groin/BioNLP-ST-2016_BB-cat+ner_dev.gsd";
		genFeatuerFile.excerptFeatures(groinGSDIndex, allFeatureFile, excerptFile);
		
		//dev test
		allFeatureFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/all/BioNLP-ST-2016_BB-cat+ner_dev.txt";
		excerptFile = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/groin/BioNLP-ST-2016_BB-cat+ner_dev.txt";
		genFeatuerFile.excerptFeatures(groinIndex, allFeatureFile, excerptFile);
		*/
	}
}

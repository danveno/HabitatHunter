package edu.arizona.biosemantics.habitat.ner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.bb.BBEntity;
import edu.arizona.biosemantics.discourse.Token;
import edu.arizona.biosemantics.discourse.TokenAttribute;
import edu.arizona.biosemantics.habitat.feature.ResourceReader;
import edu.arizona.biosemantics.habitat.io.FileUtil;
import edu.arizona.biosemantics.habitat.io.PlainTextReader;
import edu.arizona.biosemantics.habitat.ontology.OntologyManager;
import edu.arizona.biosemantics.habitat.ontology.OntologyOBOParser;
import edu.arizona.biosemantics.habitat.ontology.ncbi.NamesParser;
import edu.arizona.biosemantics.habitat.ontomap.OntologyMapping;


/**
 * Using CRF method for Bacteria NER
 * 
 * @author maojin
 *
 */
public class MalletInputGenerator {
	private String dataset = "dev";
	private String txtFolder ="F:\\Habitat\\BacteriaBiotope\\2016";
	private String a1AnnFolder ="F:\\Habitat\\BacteriaBiotope\\2016";
	private String collxFolder = "F:\\Habitat\\BacteriaBiotope\\resources\\BB3\\stanford-parser\\"+dataset;
	private String speciesFolder ="F:\\Habitat\\BacteriaBiotope\\resources\\BB3\\species-dictionary\\"+dataset;
	private String linnaeusFolder ="F:\\Habitat\\BacteriaBiotope\\resources\\BB3\\linnaeus\\"+dataset;
	private String cocoaFolder ="F:\\Habitat\\BacteriaBiotope\\resources\\BB3\\cocoa";
	private String brownClusterFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\brown_train_dev_output.txt";
	private Map<String, String> brownClusterMap; 
	
	private String ncbiNamesDmp = "F:\\Habitat\\BacteriaBiotope\\resources\\NCBI Taxonomy\\names.dmp";
	private String ontoBiotopePath = "F:\\Habitat\\BacteriaBiotope\\resources\\OntoBiotope_BioNLP-ST-2016.obo";
	private ResourceReader resourceReader;
	private PlainTextReader plainTextReader;
	private OntologyMapping ncbiOntoMapping;
	private OntologyMapping biotopeOntoMapping;
	
	private int fileNum;
	private int sentenceNum;
	private int tokenNum;
	
	public MalletInputGenerator(){
		resourceReader = new ResourceReader();
		plainTextReader = new PlainTextReader();
	}
	
	public void initOntologyMappingTool(String stopWordFile){
		//read NCBI Taxonomy ontology
		NamesParser ncbiNameParser = new NamesParser();
		ncbiNameParser.parse(ncbiNamesDmp);
		
		Set stopwordSet = FileUtil.readTermSet(stopWordFile);
		OntologyManager ncbiOntoMgr = ncbiNameParser.getOntology();
		ncbiOntoMapping = new OntologyMapping(ncbiOntoMgr);
		ncbiOntoMapping.setStopSet(stopwordSet);
		
		//read OntoBiotope
		OntologyOBOParser biotopeParser = new OntologyOBOParser();
		biotopeParser.parse(ontoBiotopePath);
		OntologyManager biotopeOntoMgr = biotopeParser.getOntology();
		biotopeOntoMapping = new OntologyMapping(biotopeOntoMgr);
		biotopeOntoMapping.setStopSet(stopwordSet);
		
		brownClusterMap = resourceReader.readBrownCluster(this.brownClusterFile);
	}
	
	
	/**
	 * 
	 * @param inputFolder
	 * @param datasetName
	 */
	public void generateCRFInput(String inputFolder, String datasetName, boolean isTrain, String suffix){
		//read collx
		String collxDatasetFolder = this.collxFolder+"/"+datasetName;
		
		//output file
		String crfFile = inputFolder+"/"+datasetName+"_"+suffix+".txt";
		
		//output file line index file
		String lineFile = inputFolder+"/"+datasetName+"_line.txt";
		
		try{
			FileWriter lineFw = new FileWriter(new File(lineFile));
			
			File collxFolderFile = new File(collxDatasetFolder);
			File[] collxFiles = collxFolderFile.listFiles();
			String currentFileName = null;
			
			int lineNum=1;
			int maxSentId = 0;
			for(File collxFile : collxFiles){
				if(collxFile.getName().indexOf(".conllx")==-1){
					continue;
				}
				
				String fileName = FileUtil.getFileName(collxFile.getName());
				fileNum++;
				
				System.out.println("current fileName:"+fileName);
				lineFw.write(fileName);
				lineFw.write(" ");
				lineFw.write(lineNum+"\n");
				
				//Token List in this file
				List<Token> tokenList = resourceReader.readTokenFromConllx(collxFile);
				
				String txtFile = txtFolder+"/"+datasetName+"/"+fileName+".txt";
				plainTextReader.matchOffset(txtFile, tokenList);
				/**/
				//Species List
				String speciesRsFile = this.speciesFolder+"/"+datasetName+"/"+fileName+".spec";
				List speciesList = resourceReader.readSPECIESResults(speciesRsFile);
				
				String linnaeusFile = this.linnaeusFolder+"/"+datasetName+"/"+fileName+".tags";
				List linRsList = resourceReader.readLinnaeusResults(linnaeusFile);
				
				//cocoa annotation
				String cocoaFile = this.cocoaFolder+"/"+datasetName+"/"+fileName+".cocoa";
				List cocoaList = resourceReader.readCocoa(cocoaFile);
				Map cocoaTokenType = getTokenEntityType(cocoaList);
				
				//the habitat and bacteria annotation
				//Make sure they were in the same place
				String a1File = this.a1AnnFolder+"/"+datasetName+"/"+fileName+".a1";
				List<BBEntity> annEntityList = resourceReader.readAnnEntities(a1File);
				//List<BBEntity> annCorruptEntities = corruptEntity(annEntityList);
				
				for(Token token : tokenList){
					tokenNum++;
					//presence in the results of SPECIES
					boolean isAppearInSpecies = isAppear(token, speciesList);
					if(isAppearInSpecies){
						token.setAttribute(TokenAttribute.ISInSpecies, "SPECIES");
					}else{
						token.setAttribute(TokenAttribute.ISInSpecies, "O");
					}
					
					//presence in the results of Linnaerus
					boolean isAppearInLinnaerus = isAppear(token, linRsList);
					if(isAppearInLinnaerus){
						token.setAttribute(TokenAttribute.ISInLinnaerus, "Linnaerus");
					}else{
						token.setAttribute(TokenAttribute.ISInLinnaerus, "O");
					}
					
					//presence in OntoBiotope
					boolean inOntoBiotope = biotopeOntoMapping.testTokenExists(token.getText());
					if(inOntoBiotope){
						token.setAttribute(TokenAttribute.InOntoBiotope, "MBTO");
					}else{
						token.setAttribute(TokenAttribute.InOntoBiotope, "O");
					}
					
					//presence in NCBITaxonomy
					boolean inNCBITaxonomy = ncbiOntoMapping.testTokenExists(token.getText());
					if(inNCBITaxonomy){
						token.setAttribute(TokenAttribute.InNCBITaxonomy, "NCBI");
					}else{
						token.setAttribute(TokenAttribute.InNCBITaxonomy, "O");
					}
					
					String tokenType = (String) cocoaTokenType.get(token.getText());
					if(tokenType==null) tokenType = "O";
					token.setAttribute(TokenAttribute.CocoaType, tokenType);
					
					String bclusterID = brownClusterMap.get(token.getText());
					bclusterID = bclusterID==null?"O":bclusterID;
					token.setAttribute(TokenAttribute.BrownCluster, bclusterID);
					/*
					for(BBEntity currentEntity : annEntityList){
						if(currentEntity.getStart()<=token.getOffset()&&currentEntity.getEnd()>=token.getOffend()&&currentEntity.getName().indexOf(token.getText())>-1){
							System.out.println(token.getText()+" "+currentEntity.getType());
						}
					}
					*/
					if(isTrain){
						String nerType = detectNERType(annEntityList,token);
						token.setAttribute(TokenAttribute.NER, nerType);
					}
					//System.out.println(token.getText()+" "+token.getAttribute(TokenAttribute.NER));
				}
				maxSentId = tokenList.get(tokenList.size()-1).getSentenceId();
				lineNum += tokenList.size()+maxSentId; 
				
				if(currentFileName!=null&&!currentFileName.equals(fileName)){
					outputCRFFileBlank(crfFile);
					lineNum += 1; 
				}
				currentFileName =  fileName;
				
				//output to a file
				outputCRFFile(fileName,tokenList,crfFile,isTrain);
			}///one document end
		
			lineFw.flush();
			lineFw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("FileNum="+fileNum+" SentenceNum="+sentenceNum+" TokenNum="+tokenNum);
	}
	
	
	public List<Token> readTokenSequences(String datasetName){
		//read collx
		String collxDatasetFolder = this.collxFolder+"/"+datasetName;
		
		List<Token> allTokenList = new ArrayList();
		
		//output file
		File collxFolderFile = new File(collxDatasetFolder);
		File[] collxFiles = collxFolderFile.listFiles();
		
		for(File collxFile : collxFiles){
			if(collxFile.getName().indexOf(".conllx")==-1){
				continue;
			}
			
			String fileName = FileUtil.getFileName(collxFile.getName());
			fileNum++;
			
			System.out.println("current fileName:"+fileName);
			//Token List in this file with blank lines
			List<Token> tokenList = resourceReader.readConllxWithEmptyLine(collxFile);
			
			String txtFile = txtFolder+"/"+datasetName+"/"+fileName+".txt";
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
		List<BBEntity> bbList = new ArrayList<BBEntity>();
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
	
	
	/**
	 * 
	 * @param tokenList
	 * @param crfFormatFile
	 */
	public void outputCRFFileBlank(String crfFormatFile){
		try {
			FileWriter fw = new FileWriter(new File(crfFormatFile),true);
			fw.write("\n");
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param tokenList
	 * @param crfFormatFile
	 */
	public void outputCRFFile(String fileName, List<Token> tokenList, String crfFormatFile, boolean isTrain){
		try {
			FileWriter fw = new FileWriter(new File(crfFormatFile),true);
			//fw.write(fileName+"\n");
			//Format: 
			/***
			 * Column 1: character offset; 2: length in characters;
				3: token; 4: typographic features; 5: presence of punctuation; 6: presence of digit; 7: length in characters
				(with a generic ’7’ category for length higher than seven characters); 8: presence of the token in the
				OntoBiotope ontology; 9: presence of the token in the NCBI taxonomy; 10: category of the token from
				the Cocoa annotations; 11: cluster identifier; 12: expected answer
			 */
			int currentSent = -1;
			sentenceNum++;
			String lastLabel = "";
			for(Token token : tokenList){
				int sentId = token.getSentenceId();
				if(currentSent!=-1&&sentId!=currentSent){
					fw.write("\n");
					sentenceNum++;
				}
				currentSent = sentId;
				fw.write(token.getText()+" ");//token
				fw.write(""+token.getOffset()+" ");//1: character offset   OFFSET_ 
				fw.write(""+token.getText().length()+" ");//2: length in characters;  LEN_
				fw.write(token.getAttribute(TokenAttribute.POS)+" ");//3  POS;
				fw.write(getSimpleTypographic(token.getText())+" ");//4 typographic types
				fw.write(""+isPunctuation(token.getText())+" ");//5 isPunctuation  Punc_
				fw.write(""+presentsDigit(token.getText())+" ");//6 presentsDigit   Digit_
				//presence of dot, hyphen, comma
				fw.write(""+(token.getText().length()>7?7:token.getText().length())+" ");//7: length in characters (with a generic ’7’ category for length higher than seven characters)
				fw.write(""+token.getAttribute(TokenAttribute.InOntoBiotope)+" ");//presence of the token in the OntoBiotope ontology   OB_
				fw.write(""+token.getAttribute(TokenAttribute.InNCBITaxonomy)+" ");//presence of the token in the NCBITaxonomy ontology
				fw.write(""+token.getAttribute(TokenAttribute.CocoaType)+" ");//cocoa type
				fw.write(""+token.getAttribute(TokenAttribute.BrownCluster)+" ");//BrownCluster
				//fw.write(""+token.getAttribute(TokenAttribute.ISInSpecies)+" ");//IN SPECIES RESULTS
				//fw.write(""+token.getAttribute(TokenAttribute.ISInLinnaerus)+" ");//ISInLinnaerus RESULTS
				//the last one?
				
				if(isTrain){
					String currentLabel = token.getAttribute(TokenAttribute.NER).toString();
					if(currentLabel.equals("Habitat")&&"Habitat".equals(lastLabel)){
						fw.write(" I-Habitat");//I-Habitat
					}else if(currentLabel.equals("Habitat")&&!"Habitat".equals(lastLabel)){
						fw.write(" B-Habitat");//currentLabel
					}if(currentLabel.equals("Bacteria")&&"Bacteria".equals(lastLabel)){
						fw.write(" I-Bacteria");//I-Bacteria
					}else if(currentLabel.equals("Bacteria")&&!"Bacteria".equals(lastLabel)){
						fw.write(" B-Bacteria");//B-Bacteria
					}else if(currentLabel.equals("O")){
						fw.write(" O");//"O"
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
	 * presents any digit
	 * @param str
	 * @return
	 */
	public String presentsDigit(String str){
		for(int i = 0;i < str.length();i++){
			if(Character.isDigit(str.charAt(i))){
				return "Digit";
			}
		}//
		return "O";
	}
	
	
	/**
	 * check whether its a punctuation
	 * @param str
	 * @return
	 */
	public String isPunctuation(String str){
		char c = str.charAt(0);
        if(str.length()==1&&(c == ','
            || c == '.'
            || c == '!'
            || c == '?'
            || c == ':'
            || c == ';'))	return "Punc";
        return "O";
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
		MalletInputGenerator bacCRFNer = new MalletInputGenerator();
		String stopwordFile ="F:/Habitat/BacteriaBiotope/experiments/stopwords.txt";
		bacCRFNer.initOntologyMappingTool(stopwordFile);
		String inputFolder = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs";
		String datasetName = "BioNLP-ST-2016_BB-cat_dev";//dev
		boolean isTrain = true;
		String suffix = "12f_gsd";
		bacCRFNer.generateCRFInput(inputFolder, datasetName,isTrain,suffix);
	}

}
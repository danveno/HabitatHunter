package edu.arizona.biosemantics.habitat.ner;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.nicta.csp.brateval.CompareEntities;
import edu.arizona.biosemantics.bb.BBEntity;
import edu.arizona.biosemantics.bb.Config;
import edu.arizona.biosemantics.bb.EntitySorter;
import edu.arizona.biosemantics.discourse.Phrase;
import edu.arizona.biosemantics.discourse.Token;
import edu.arizona.biosemantics.discourse.TokenAttribute;
import edu.arizona.biosemantics.habitat.feature.TokenFeatureRender;
import edu.arizona.biosemantics.habitat.io.A1FormatFileUtil;
import edu.arizona.biosemantics.habitat.io.FileUtil;
import edu.arizona.biosemantics.habitat.ontology.OntologyManager;
import edu.arizona.biosemantics.habitat.ontology.OntologyOBOParser;
import edu.arizona.biosemantics.habitat.ontology.OntologyTerm;
import edu.arizona.biosemantics.habitat.ontomap.OntologyMapping;
import edu.arizona.biosemantics.nlp.PhraseExtractor;
import edu.arizona.biosemantics.nlp.PhraseRelationExtractor;
import edu.arizona.biosemantics.util.StringUtil;
import edu.stanford.nlp.util.EditDistance;



/**
 * after CRF sequence labeling, 
 * @author maojin
 *
 */
public class PostProcessor {
	private TokenFeatureRender tokenFeatureRender = new TokenFeatureRender();
	private PhraseExtractor phraseExtractor = new PhraseExtractor();
	private PhraseRelationExtractor phraseRelationExtractor = new PhraseRelationExtractor();
	private EditDistance editDistance = new EditDistance();
	private A1FormatFileUtil a1Util = new A1FormatFileUtil();
	private OntologyMapping biotopeOntoMapping;
	
	private String[] orderredFiles;
	
	private Map<String, List<Token>> fileTokenMap;
	
	private Map<String, List<Phrase>> filePhraseMap;
	
	private List<Token> datasetTokens;
	
	private Map<String, List<BBEntity>> fileEntityMap;
	
	public void initOntoBiotope(){
		//read OntoBiotope
		OntologyOBOParser biotopeParser = new OntologyOBOParser();
		biotopeParser.parse(Config.ontoBiotopePath);
		OntologyManager biotopeOntoMgr = biotopeParser.getOntology();
		biotopeOntoMapping = new OntologyMapping(biotopeOntoMgr);
    }
	
	
	/**
	 * 1, result file---the first field is the label of the token
	 * 2, read the token and offset
	 * 
	 */
	public void recover(String datasetName, String predictionFile,String outputFolder){
		datasetTokens = tokenFeatureRender.readDatasetTokenSequence(datasetName);
		
		List<String> predictLabels = FileUtil.readNonEmptyLineFromFile(predictionFile);
		
		boolean isMatch = checkOrderMatch(datasetTokens, predictLabels);
		System.out.println("predLines.size()="+predictLabels.size());
		System.out.println("allTokenSeqs="+datasetTokens.size());
		System.out.println("isMatch="+isMatch);
		
		//render the predicted labels to tokens
		renderPredictedLabel(datasetTokens, predictLabels);
		
		this.fileTokenMap = segTokenListByFile(datasetTokens);
		
		this.orderredFiles = new String[fileTokenMap.keySet().size()];
		fileTokenMap.keySet().toArray(this.orderredFiles);
		
		extractPhrases(fileTokenMap);
		
		//postprocessing();
		//datasetTokens = allTokenSeqs;
	}




	public void postprocessing() {
		//post processing each file
		for(String fileName:orderredFiles){
			System.out.println("\n\n"+fileName);
			List<BBEntity> entities = fileEntityMap.get(fileName);
			for(BBEntity entity:entities) System.out.println(entity.getName()+" -> "+entity.getType());
			List<Phrase> phraseList = filePhraseMap.get(fileName);
			for(Phrase phrase:phraseList){
				if(phrase.getLabel()==null||"O".equals(phrase.getLabel())){
					System.out.println(phrase.getText());
				}
			}
			/*Map<String, Phrase> anaphorMap =  phraseRelationExtractor.findSubstringAnaphorPhrase(phraseList);
			
			//unlabel phrases
			for(Phrase phrase:phraseList){
				if((phrase.getLabel()==null||"O".equals(phrase.getLabel()))&&anaphorMap.containsKey(phrase.getText())){
					Phrase pairedPhrase = anaphorMap.get(phrase.getText());
					System.out.println(phrase.toString()+" refers to :[ "+pairedPhrase.getText()+"] "+pairedPhrase.getLabel());
					if("Bacteria".equals(pairedPhrase.getLabel())) phrase.applyLabel(pairedPhrase.getLabel());
				}
			}
			
			 
			Map<String, Phrase> acronymMap = phraseRelationExtractor.identifyAcronymPhrasePair(phraseList);
			
			
			//unlabel phrases
			for(Phrase phrase:phraseList){
				if((phrase.getLabel()==null||"O".equals(phrase.getLabel()))&&acronymMap.containsKey(phrase.getText())){
					Phrase pairedPhrase = acronymMap.get(phrase.getText());
					if("Bacteria".equals(pairedPhrase.getLabel())){
						phrase.applyLabel(pairedPhrase.getLabel());
						System.out.println(phrase.toString()+" long form:[ "+pairedPhrase.getText()+"]"+pairedPhrase.getLabel());
						}
				}
			}*/
		}
	}

	/**
	 * if the phrase matches exactly with an entity, label the phrase with the label of the entity
	 * 
	 */
	public void exactEntityResolve(){
		for(String fileName:orderredFiles){
			System.out.println("\n\n"+fileName);
			List<BBEntity> entities = fileEntityMap.get(fileName);
			List<Phrase> phraseList = filePhraseMap.get(fileName);
			for(Phrase phrase:phraseList){
				if(phrase.getLabel()==null||"O".equals(phrase.getLabel())){
					for(BBEntity entity:entities){
						if(entity.getName().trim().split(" ").length==phrase.getText().trim().split(" ").length&&(
								entity.getName().toLowerCase().equals(phrase.getText().toLowerCase())
								||(entity.getName().length()>5&&editDistance.score(entity.getName().toLowerCase(), phrase.getText().toLowerCase())<=2))){
							phrase.applyLabel(entity.getType());
							BBEntity pentity = phrase.toEntity(fileName);
							System.out.println(phrase.getText()+" "+entities.contains(pentity));
							if(!entities.contains(pentity))  entities.add(pentity);
							System.out.println(phrase.getText()+"-->["+entity.getName()+"]new label-->"+phrase.getLabel()+entity.getName().trim().split(" ").length+" "+phrase.getText().trim().split(" ").length);
							break;
						}
						
					}
				}
			}
		}
	}
	
	
	/**
	 * 1, remove the generic habitat
	 */
	public void lengthFilter() {
		for(String fileName:orderredFiles){
			System.out.println("\n\n"+fileName);
			List<BBEntity> entities = fileEntityMap.get(fileName);
			for(int i=0;i<entities.size();i++){
				BBEntity entity = entities.get(i);
				if(entity.getType().equals("Habitat")){
					if(entity.getName().length()<3){
						entities.remove(i);
						i--;
					}
				}else if(entity.getType().equals("Bacteria")){
					if(entity.getName().length()<2){
						entities.remove(i);
						i--;
					}
				}
			}
		}
	}
	
	
	/**
	 * 1, remove the generic habitat
	 */
	public void handleHabitat() {
		for(String fileName:orderredFiles){
			System.out.println("\n\n"+fileName);
			List<BBEntity> entities = fileEntityMap.get(fileName);
			List<Token> fileTokens = fileTokenMap.get(fileName);
			for(int i=0;i<entities.size();i++){
				BBEntity entity = entities.get(i);
				if(entity.getType().equals("Habitat")){
					//remove generic habitat
					if(Config.genericHabitats.contains(entity.getName())){
						entities.remove(i);
						i--;
						continue;
					}
					
					/*
					List<Token> entityTokens = entity.getTokens();
					//add adjectives
					Token previousToken = null;
					for(int t=0;t<fileTokens.size();t++){
						if(fileTokens.get(t).equals(entityTokens.get(0))&&t>0){
							previousToken = fileTokens.get(t-1);
							break;
						}
					}
					
					if(previousToken!=null){
						String pos = (String) previousToken.getAttribute(TokenAttribute.POS);
						if(pos.startsWith("JJ")){
							entity.setName(previousToken.getText()+" "+entity.getName());
							entity.setStart(previousToken.getOffset());
							System.out.println(entity.getName()+"   previous adj token = "+previousToken.getText());
						}
					}
					*/
					
					//System.out.println(entity.getName()+" = habitat");
					//overlapping entities
					/*
					Set<String> mappedOntoTerms = biotopeOntoMapping.coverMap(entity.getName());
					for(String ontoTermStr:mappedOntoTerms){
						if(!ontoTermStr.equals(entity.getName())){
							BBEntity newHabitat = createInnerEntity(entity, ontoTermStr);
							if(!entities.contains(newHabitat))  entities.add(newHabitat);
							System.out.println(entity.getName()+" inner entity = "+ontoTermStr);
						}
					}*/
				}
			}
		}
	}


	public BBEntity createInnerEntity(BBEntity entity, String ontoTermStr) {
		BBEntity newEntity = new BBEntity();
		newEntity.setName(ontoTermStr);
		newEntity.setType(entity.getType());
		newEntity.setDocID(entity.getDocID());
		int index = entity.getName().indexOf(ontoTermStr);
		newEntity.setStart(entity.getStart()+index);
		newEntity.setEnd(newEntity.getStart()+ontoTermStr.length());
		return newEntity;
	}

	/**
	 * deal with the overlap problem
	 * find whether a bacteria entity stays in a habitat entity
	 */
	public void findBacteriaFromHabitats() {
		for(String fileName:orderredFiles){
			System.out.println("\n\n"+fileName);
			List<BBEntity> entities = fileEntityMap.get(fileName);
			for(int i=0;i<entities.size();i++){
				BBEntity entity = entities.get(i);
				if(entity.getType().equals("Bacteria")){
					for(int j=i+1;j<entities.size();j++){
						BBEntity habitatEntity = entities.get(j);
						if(habitatEntity.getType().equals("Habitat")
								&&null!=StringUtil.matchShortTerm(habitatEntity.getName(), entity.getName())){
							//create the bacteria entity
							BBEntity newBacteria = createInnerEntity(habitatEntity, entity.getName());
							newBacteria.setType("Bacteria");
							if(!entities.contains(newBacteria))  entities.add(newBacteria);
							System.out.println(habitatEntity.toString()+" inner bacteria entity = "+newBacteria.toString());
						}
				}
			}
			}//end of a file
		}
	}
	
	

	/**
	 * if the last token of the entity is a specification remove it
	 * 
	 */
	public void handleSpecificationSuffix(){
		//post processing each file
		for(String fileName:orderredFiles){
			System.out.println("\n\n"+fileName);
			List<BBEntity> entities = fileEntityMap.get(fileName);
			
			List<Token> fileTokens = fileTokenMap.get(fileName);
			for(BBEntity entity:entities){
				List<Token> entityTokens = entity.getTokens();
				/*
				//remove the last character dot
				if(entity.getName().endsWith(")")&&entity.getName().indexOf("(")==-1){
					Token lastToken = entityTokens.get(entityTokens.size()-1);
					String text = lastToken.getText();
					lastToken.setText(text.substring(0,text.length()-1));
					entity.setName(entity.getName().substring(0,entity.getName().length()-1));
					//System.out.println(entity.getName()+" -> remove end parenthese");
				}
				*/
				
				if(!entity.getType().equals("Bacteria")) continue;
				
				System.out.println(entity.getName()+" -> "+entity.getType());
				
				//remove strain specifications
				if(endsWith(entity.getName(),Config.strainSpecs)){
					entity.removeLastToken();
					System.out.println(entity.getName()+" -> remove specifications ");
				}
				
				//TODO: this will damage the token structure.
				//remove the last character dot
				if(entity.getName().endsWith(".")&&!endsWith(entity.getName(),Config.nomenclaturals)){
					//entity.setName(entity.getName().substring(0,entity.getName().length()-1));
					
					if(entityTokens.get(entityTokens.size()-1).getText().equals(".")){
						entity.removeLastToken();
					}else if(entity.getName().length()>2){
						Token lastToken = entityTokens.get(entityTokens.size()-1);
						String text = lastToken.getText();
						lastToken.setText(text.substring(0,text.length()-1));
						entity.setName(entity.getName().substring(0,entity.getName().length()-1));
						entity.setEnd(entity.getEnd()-1);
					}
					System.out.println(entity.getName()+" -> remove end dot");
				}
				
				/**/
				
				//complement the nomenclaturals
				String followingFourTokenStr = followFourTokenStr(entityTokens.get(entityTokens.size()-1),fileTokens);
				
				String matchNomenclatural = startsWithStr(followingFourTokenStr, Config.nomenclaturals);
				System.out.println("followingFourTokenStr="+followingFourTokenStr);
				if(matchNomenclatural!=null){//BB-cat+ner-19552770
					entity.setName(entity.getName()+" "+matchNomenclatural);
					entity.setEnd(entity.getEnd()+matchNomenclatural.length()+1);
					System.out.println("followingFourTokenStr="+followingFourTokenStr+"   nomenc = "+matchNomenclatural);
				}
				
				//last token
				Token previousToken = null;
				for(int i=0;i<fileTokens.size();i++){
					if(fileTokens.get(i).equals(entityTokens.get(0))&&i>0){
						previousToken = fileTokens.get(i-1);
						break;
					}
				}
				
				if(previousToken!=null&&(Config.headStrainSpecs.contains(previousToken.getText())||Config.mutants.contains(previousToken.getText()))){
					entity.setName(previousToken.getText()+" "+entity.getName());
					entity.setStart(previousToken.getOffset());
					System.out.println(entity.getName()+"   previous token = "+previousToken.getText());
				}
			}
			//break;
		}
	}
	
	
	
	/**
	 * combine adjacent entities
	 */
	public void combineAdjecent() {
		//post processing each file
		for(String fileName:orderredFiles){
			System.out.println("\n\n"+fileName);
			List<BBEntity> entities = fileEntityMap.get(fileName);
			
			//sort the entities
			Collections.sort(entities, new EntitySorter());
			
			
			if(entities.size()==0) continue;
			BBEntity curEntity = null;
			BBEntity lastEntity = entities.get(0);
			for(int i=1;i<entities.size();){
				curEntity =entities.get(i);
				int dif = curEntity.getStart()-lastEntity.getEnd();
				System.out.println(curEntity.getStart()+" "+dif);
				if((dif==0||dif==1)&&curEntity.getType().equals(lastEntity.getType())){
					if(dif==0) lastEntity.setName(lastEntity.getName()+curEntity.getName());
					else lastEntity.setName(lastEntity.getName()+" "+curEntity.getName());
					entities.remove(i);
					System.out.println("combined="+lastEntity.getName());
				}else{
					i++;
					lastEntity=curEntity;
				}
				
			}
		}
	}
	
	public String followFourTokenStr(Token token, List<Token> fileTokens) {
		StringBuffer sb = new StringBuffer();
		boolean start = false;
		int index = 4;
		int lastEnd = -1;
		for(int i=0;i<fileTokens.size();i++){
			Token curToken = fileTokens.get(i);
			
			
			if(start&&index>0){
				if(lastEnd==curToken.getOffset()||index == 4)sb.append(curToken.getText());
					else sb.append(" ").append(curToken.getText());
				index--;
			}
			if(index==0){
				break;
			}
			lastEnd = curToken.getOffend();
			
			if(curToken.equals(token)) start = true;
		}
		return sb.toString();
	}




	public boolean endsWith(String name, List<String> nomenclaturals) {
		for(String ncstr:nomenclaturals){
			if(name.endsWith(ncstr)) return true;
		}
		return false;
	}

	public boolean startsWith(String name, List<String> nomenclaturals) {
		for(String ncstr:nomenclaturals){
			if(name.startsWith(ncstr)) return true;
		}
		return false;
	}

	public String startsWithStr(String name, List<String> nomenclaturals) {
		for(String ncstr:nomenclaturals){
			if(name.startsWith(ncstr)) return ncstr;
		}
		return null;
	}


	/**
	 * 
	 */
	public void resolveSubstringAnaphor(){
		//post processing each file
		for(String fileName:orderredFiles){
			System.out.println("\n\n"+fileName);
			List<BBEntity> entities = fileEntityMap.get(fileName);
			//for(BBEntity entity:entities) System.out.println(entity.getName()+" -> "+entity.getType());
			List<Phrase> phraseList = filePhraseMap.get(fileName);
			for(Phrase phrase:phraseList){
				if(phrase.getLabel()==null||"O".equals(phrase.getLabel())){//unannotated
					BBEntity anaphorEntity = phraseRelationExtractor.findSubstringAnaphorEntity(phrase,entities);
					if(anaphorEntity!=null&&anaphorEntity.getType().equals("Bacteria")){
						phrase.applyLabel(anaphorEntity.getType());
						BBEntity pentity = phrase.toEntity(fileName);
						if(!entities.contains(pentity)) entities.add(pentity);
						System.out.println(phrase.getText()+"'s anaphor is ["+anaphorEntity.getName()+"] "+anaphorEntity.getType());
					}
				}
			}
		}
	}
	
	
	/**
	 * extract phrases for files using the Genia structure
	 * 
	 * @param fileTokenMap
	 */
	public void extractPhrases(Map<String, List<Token>> fileTokenMap) {
		filePhraseMap = new LinkedHashMap();
		for(String file : fileTokenMap.keySet()){
			List<Token> tokenList = fileTokenMap.get(file);
			List<Phrase> phraseList = phraseExtractor.getFromGenia(tokenList);
			//label phrases
			for(int i=0;i<phraseList.size();){
				Phrase phrase  = phraseList.get(i);
				phrase.parse();
				phrase.label();
				if(!phrase.toString().trim().equals("")) i++;
				else phraseList.remove(i);
				//System.out.println(phrase.getText());
			}
			filePhraseMap.put(file, phraseList);
		}
	}


	/**
	 * segment tokens
	 * @param allTokenSeqs
	 * @return
	 */
	public Map<String, List<Token>> segTokenListByFile(List<Token> allTokenSeqs) {
		Map<String, List<Token>> fileTokenMap = new LinkedHashMap();
		for(Token token : allTokenSeqs){
			String fileName = token.getFileName();
			List tokenList = fileTokenMap.get(fileName);
			if(tokenList==null){
				tokenList = new ArrayList();
				fileTokenMap.put(fileName, tokenList);
			}
			tokenList.add(token);
		}
		return fileTokenMap;
	}


	/**
	 * 
	 * @param allTokenSeqs
	 * @param predictLabels
	 */
	public void renderPredictedLabel(List<Token> allTokenSeqs, List<String> predictLabels) {
		for(int index =0 ;index < allTokenSeqs.size() ;index++){
			Token token = allTokenSeqs.get(index);
			String label =  predictLabels.get(index);
			if(label.indexOf("-")>-1) label = label.substring(label.indexOf("-")+1);
			//System.out.println(label);
			token.setAttribute(TokenAttribute.NER, label.trim());
		}
	}

	public boolean checkOrderMatch(List<Token> allTokenSeqs,
			List<String> predLines) {
		return predLines.size()==allTokenSeqs.size();
	}
	
	/**
	 * extract entities in each file
	 * @return
	 */
	public Map<String, List<BBEntity>> extractFileEntity(){
		fileEntityMap = new LinkedHashMap();
		for(String file : fileTokenMap.keySet()){
			List<Token> tokenList = fileTokenMap.get(file);
			
			List<BBEntity> entities = new ArrayList();
			
			BBEntity entity = new BBEntity();
			//entities.add(entity);
			for(Token token:tokenList){
				String label = 	token.getLabel();
				if(label.indexOf("Habitat")>-1){//habitat
					if(entity.getName()==null){//a new one
						entity.setName(token.getText());
						entity.setStart(token.getOffset());
						entity.setEnd(token.getOffend());
						entity.setType("Habitat");
						entities.add(entity);
					}else if(entity.getName()!=null&&entity.getType().equals("Habitat")){
						if(token.getOffset()-entity.getEnd()==0){
							entity.setName(entity.getName()+""+token.getText());
						}else{
							entity.setName(entity.getName()+" "+token.getText());
						}
						entity.setEnd(token.getOffend());
					}else{//the former is bacteria, add to the list and create a new one
						entity = new BBEntity();
						entities.add(entity);
						entity.setName(token.getText());
						entity.setStart(token.getOffset());
						entity.setEnd(token.getOffend());
						entity.setType("Habitat");
					}
				}else if(label.indexOf("Bacteria")>-1){//Bacteria
					if(entity.getName()==null){//a new one
						entity.setName(token.getText());
						entity.setStart(token.getOffset());
						entity.setEnd(token.getOffend());
						entity.setType("Bacteria");
						entities.add(entity);
					}else if(entity.getName()!=null&&entity.getType().equals("Bacteria")){
						if(token.getOffset()-entity.getEnd()==0){
							entity.setName(entity.getName()+""+token.getText());
						}else{
							entity.setName(entity.getName()+" "+token.getText());
						}
						entity.setEnd(token.getOffend());
					}else{//the former is bacteria, add to the list and create a new one
						entity = new BBEntity();
						entities.add(entity);
						entity.setName(token.getText());
						entity.setStart(token.getOffset());
						entity.setEnd(token.getOffend());
						entity.setType("Bacteria");
					}
				}else if(label.trim().equals("O")||label.trim().equals("")){
					entity = new BBEntity();
				}
			}//
			fileEntityMap.put(file, entities);
		}
		return fileEntityMap;
	}
	
	/**
	 * extract entities in each file
	 * @return
	 */
	public Map<String, List<BBEntity>> extractFileEntity2(){
		fileEntityMap = new LinkedHashMap();
		for(String file : fileTokenMap.keySet()){
			List<Token> tokenList = fileTokenMap.get(file);
			
			List<BBEntity> entities = new ArrayList();
			
			BBEntity entity = new BBEntity();
			//entities.add(entity);
			for(Token token:tokenList){
				String label = 	token.getLabel();
				if(label.indexOf("Habitat")>-1){//habitat
					if(entity.getName()==null){//a new one
						entity.addToken(token);
						entity.setType("Habitat");
						entities.add(entity);
					}else if(entity.getName()!=null&&entity.getType().equals("Habitat")){
						entity.addToken(token);
					}else{//the former is bacteria, add to the list and create a new one
						entity = new BBEntity();
						entities.add(entity);
						entity.addToken(token);
						entity.setType("Habitat");
					}
				}else if(label.indexOf("Bacteria")>-1){//Bacteria
					if(entity.getName()==null){//a new one
						entity.addToken(token);
						entity.setType("Bacteria");
						entities.add(entity);
					}else if(entity.getName()!=null&&entity.getType().equals("Bacteria")){
						entity.addToken(token);
						entity.setEnd(token.getOffend());
					}else{//the former is bacteria, add to the list and create a new one
						entity = new BBEntity();
						entities.add(entity);
						entity.addToken(token);
						entity.setType("Bacteria");
					}
				}else if(label.trim().equals("O")||label.trim().equals("")){
					entity = new BBEntity();
				}
			}//
			fileEntityMap.put(file, entities);
		}
		return fileEntityMap;
	}
	
	
	
	public void updateBBEntity() {
		extractFileEntity();
	}
	
	public void outputFinalSequence(String finalSeqFile) {
		FileWriter fw;
		try {
			fw = new FileWriter(finalSeqFile);
			String lastLabel = null;
			//System.out.println("the final output length is "+datasetTokens.size());
			for(String fileName:orderredFiles){
				List<Token> tokenList = fileTokenMap.get(fileName);
				//unlabel phrases
				for(Token token:tokenList){
					String currentLabel = (String) token.getAttribute(TokenAttribute.NER);
					String outputlabel = null;
					if(currentLabel.equals("Habitat")&&"Habitat".equals(lastLabel)){
						outputlabel = "I-Habitat";//I-Habitat
					}else if(currentLabel.equals("Habitat")&&!"Habitat".equals(lastLabel)){
						outputlabel = "B-Habitat";//currentLabel
					}if(currentLabel.equals("Bacteria")&&"Bacteria".equals(lastLabel)){
						outputlabel = "I-Bacteria";//I-Bacteria
					}else if(currentLabel.equals("Bacteria")&&!"Bacteria".equals(lastLabel)){
						outputlabel = "B-Bacteria";//B-Bacteria
					}else if(currentLabel.equals("O")){
						outputlabel = "O";//"O"
					}
					lastLabel = currentLabel;
					fw.write(outputlabel+"\n");
				}
			}
				fw.flush();
				fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * output entities into a given folder
	 * @param folder
	 */
	public void outputEntities(String folder){
		for(String fileName:orderredFiles){
			List<BBEntity> entities = fileEntityMap.get(fileName);
			a1Util.write(folder, fileName, entities);
		}
	}
	
	/**
	 * output phrases into a given folder
	 * @param folder
	 */
	public void outputPhrase(String folder){
		for(String fileName:orderredFiles){
			List<Phrase> phrases = filePhraseMap.get(fileName);
			try {
				FileWriter fw = new FileWriter(folder+"/"+fileName+".ph");
				for(Phrase p:phrases){
					fw.write(p.getText()+"\t"+p.getLabel()+"\n");
				}
				fw.flush();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args){
		PostProcessor postProcessing = new PostProcessor();
		postProcessing.initOntoBiotope();
		
		
		String predictionFile ="F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\all5\\BioNLP-ST-2016_BB-cat+ner_dev.rs";
		String datasetName = "BioNLP-ST-2016_BB-cat+ner_dev";
		postProcessing.recover(datasetName, predictionFile, "");
		postProcessing.extractFileEntity2();
		postProcessing.lengthFilter();
		postProcessing.handleHabitat();
		postProcessing.exactEntityResolve();
//		postProcessing.extractFileEntity2();
		
		postProcessing.handleSpecificationSuffix();
		postProcessing.combineAdjecent();
//		//postProcessing.postprocessing();
		
		postProcessing.resolveSubstringAnaphor();
		postProcessing.findBacteriaFromHabitats();
		//postProcessing.updateBBEntity();
		//postProcessing.outputPhrase("F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\phrase");
		postProcessing.outputEntities("F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\entity5");
		try {
			CompareEntities.main(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String rsAnaphorRs ="F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\all2\\BioNLP-ST-2016_BB-cat+ner_dev_anaphor.rs";
		//postProcessing.outputFinalSequence(rsAnaphorRs);
		
		String gsdFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\all2\\BioNLP-ST-2016_BB-cat+ner_dev.gsd";
		
		String conllEvalFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\all2\\BioNLP-ST-2016_BB-cat+ner_dev_anaphor.conll";
		
		MalletResultProcessor cefGen = new MalletResultProcessor();
		//cefGen.getCollEvalFile(gsdFile,rsAnaphorRs, conllEvalFile);
	}

}

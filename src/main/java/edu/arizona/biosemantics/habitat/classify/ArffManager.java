package edu.arizona.biosemantics.habitat.classify;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import edu.arizona.biosemantics.bb.Config;
import edu.arizona.biosemantics.discourse.Token;
import edu.arizona.biosemantics.discourse.TokenAttribute;
import edu.arizona.biosemantics.habitat.feature.TokenFeatureRender;
import edu.arizona.biosemantics.habitat.io.FileUtil;


/**
 * 
 * @author maojin
 * % 1. Title: Iris Plants Database
   % 
   % 2. Sources:
   %      (a) Creator: R.A. Fisher
   %      (b) Donor: Michael Marshall (MARSHALL%PLU@io.arc.nasa.gov)
   %      (c) Date: July, 1988
   % 
   @RELATION iris
 
   @ATTRIBUTE sepallength  NUMERIC
   @ATTRIBUTE sepalwidth   NUMERIC
   @ATTRIBUTE petallength  NUMERIC
   @ATTRIBUTE petalwidth   NUMERIC
   @ATTRIBUTE class        {Iris-setosa,Iris-versicolor,Iris-virginica}
   
   @DATA
   5.1,3.5,1.4,0.2,Iris-setosa
   4.9,3.0,1.4,0.2,Iris-setosa
   
   can use string attribute to handle vectors.
   String attributes allow us to create attributes containing arbitrary textual values. This is very useful in text-mining applications, as we can create datasets with string attributes, then write Weka Filters to manipulate strings (like StringToWordVectorFilter).
 */
public class ArffManager {
	
	private List<Token> datasetTokens;
	
	private static ArrayList<Attribute> attributes;
	
	private static Attribute text = new Attribute("text",(FastVector) null,0); //new Attribute("text");
	private static Attribute offset = new Attribute("offset",1);
	private static Attribute typographic = new Attribute("typographic",(FastVector) null,2);
	private static Attribute pCap = new Attribute("pCap",3);
	private static Attribute isPunctuation = new Attribute("isPunctuation",4);
	private static Attribute presentsDigit = new Attribute("presentsDigit",5);
	private static Attribute length = new Attribute("_length_",6);
	
	private static Attribute lemma = new Attribute("lemma",(FastVector) null,7);
	private static Attribute pos = new Attribute("pos",(FastVector) null,8);
	private static Attribute phraseBIO = new Attribute("phraseBIO",(FastVector) null,9);
	private static Attribute phraseHead = new Attribute("phraseHead",10);
	
	private static Attribute subVerb = new Attribute("subVerb",(FastVector) null,11);
	private static Attribute objVerb = new Attribute("objVerb",(FastVector) null,12);
	private static Attribute modNoun = new Attribute("modNoun",(FastVector) null,13);
	private static Attribute modifier = new Attribute("modifier",(FastVector) null,14);
	private static Attribute prepNoun = new Attribute("prepNoun",(FastVector) null,15);
	private static Attribute preposition = new Attribute("preposition",(FastVector) null,16);
	
	private static Attribute InOntoBiotope = new Attribute("InOntoBiotope",17);
	private static Attribute InNCBITaxonomy = new Attribute("InNCBITaxonomy",18);
	//private static Attribute CocoaType = new Attribute("CocoaType",(FastVector) null,10);
	private static Attribute GeniaLabel = new Attribute("GeniaLabel",(FastVector) null,19);
	private static Attribute BrownCluster = new Attribute("BrownCluster",(FastVector) null,20);
	//private static Attribute ISInSpecies = new Attribute("ISInSpecies",11);
	//private static Attribute ISInLinnaerus = new Attribute("ISInLinnaerus",12);
	private static Attribute wordSense = new Attribute("wordSense",(FastVector) null,21);
	
	//{"Habitat","Bacteria","O"}
	private static ArrayList<String> classLabels = new ArrayList();
	static {
		classLabels.add("Habitat");
		classLabels.add("Bacteria");
		classLabels.add("O");
	}
	private static Attribute label = new Attribute("csflabel",classLabels,22);
	//private static Attribute label = new Attribute("label",22);
	
	static {
		/*
		    0 the text of the token
		    1 character offset
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
			18 presence of the token in the NCBI taxonomy
			#the category of the token from the Cocoa annotation
			19 the category of the GENIA Tagger annotation
			20 Cluster identifier according to the Brown cluster
			21 Word sense
			22 label
		 */
		attributes = new ArrayList();
		
		attributes.add(text);
		//attributes.add(offset);
		attributes.add(typographic);
		attributes.add(pCap);
		attributes.add(isPunctuation);
		attributes.add(presentsDigit);
		attributes.add(length);
		
		attributes.add(lemma);
		attributes.add(pos);
		attributes.add(phraseBIO);
		attributes.add(phraseHead);
		
		attributes.add(subVerb);// the lemma of the verb for which the token acts as a subject
		attributes.add(objVerb); //the lemmas of verbs for which the token acts as an object
		attributes.add(modNoun);//the lemmas of nouns for which the token acts as modifiers
		attributes.add(modifier);//the lemmas of modifiers of that token.
		attributes.add(prepNoun); //the lemma of the noun for which the token acts as a preposition
		attributes.add(preposition); //the preposition of the token
		
		
		//attributes.add(length7);
		attributes.add(InOntoBiotope);
		attributes.add(InNCBITaxonomy);
		//attributes.add(CocoaType);
		attributes.add(GeniaLabel); //Genia Tagger Label
		attributes.add(BrownCluster);
		attributes.add(wordSense); //extended word sense
		//attributes.add(ISInSpecies);
		//attributes.add(ISInLinnaerus);
		
		attributes.add(label);
	}
	
	public ArffManager(List tokenList ){
		this.datasetTokens = tokenList;
	}
	
	
	/**
	 * generate arff file
	 * @param arffFilePath
	 */
	public void generateArffFile(String arffFilePath){
		try {
			FileWriter fw = new FileWriter(arffFilePath);
			
			fw.write("@RELATION BioNLP_BB\n\n");
			
			//attributes
			fw.write("@ATTRIBUTE text STRING\n");
			fw.write("@ATTRIBUTE offset NUMERIC\n");
			fw.write("@ATTRIBUTE typographic STRING\n");
			fw.write("@ATTRIBUTE pCap NUMERIC\n");
			fw.write("@ATTRIBUTE isPunctuation NUMERIC\n");
			fw.write("@ATTRIBUTE presentsDigit NUMERIC\n");
			fw.write("@ATTRIBUTE length NUMERIC\n");
			
			fw.write("@ATTRIBUTE lemma STRING\n");
			fw.write("@ATTRIBUTE pos STRING\n");
			fw.write("@ATTRIBUTE phraseBIO STRING\n");
			fw.write("@ATTRIBUTE phraseHead NUMERIC\n");
			
			fw.write("@ATTRIBUTE subVerb STRING\n");
			fw.write("@ATTRIBUTE objVerb STRING\n");
			fw.write("@ATTRIBUTE modNoun STRING\n");
			fw.write("@ATTRIBUTE modifier STRING\n");
			fw.write("@ATTRIBUTE prepNoun STRING\n");
			fw.write("@ATTRIBUTE preposition STRING\n");
			
			
			//fw.write("@ATTRIBUTE length7\");
			fw.write("@ATTRIBUTE InOntoBiotope NUMERIC\n");
			fw.write("@ATTRIBUTE InNCBITaxonomy NUMERIC\n");
			//fw.write("@ATTRIBUTE CocoaType\");
			fw.write("@ATTRIBUTE GeniaLabel STRING\n"); //Genia Tagger Label
			fw.write("@ATTRIBUTE BrownCluster STRING\n");
			fw.write("@ATTRIBUTE wordSense STRING\n"); //extended word sense
			
			fw.write("\n@DATA\n");
			
			for(Token token : datasetTokens){
				if("\"".equals(token.getText()))
					fw.write("\"QUOTE\" ");//1: character offset   OFFSET_ 
				else{
					fw.write("\""+token.getText().replace(",", "_")+"\" ");//1: character offset   OFFSET_ 
				}

				fw.write(token.getOffset()+" ");//1: character offset
				fw.write(token.getAttribute(TokenAttribute.Typographic)+" ");//2 typographic types
				fw.write((Boolean)token.getAttribute(TokenAttribute.pCap)?1:0);//3 presence of capitalized characters
				fw.write(" ");
				fw.write((Boolean)token.getAttribute(TokenAttribute.pPunct)?1:0);//4 presence of punctuation 
				fw.write(" ");
				fw.write((Boolean)token.getAttribute(TokenAttribute.pDigit)?1:0);//5 presentsDigit   Digit_
				fw.write(" ");
				fw.write(token.getText().length()+" ");//6  length in characters;  LEN_
				if("\"".equals(token.getLemma()))
					fw.write("\"QUOTE\" ");//1: character offset   OFFSET_ 
				else{
					fw.write("\""+token.getLemma().replace(",", "_")+"\" ");//1: character offset   OFFSET_ 
				}

				if("\"".equals(token.getAttribute(TokenAttribute.POS)))
					fw.write("\"QUOTE\" ");//1: character offset   OFFSET_ 
				else{
					fw.write("\""+token.getAttribute(TokenAttribute.POS)+"\" ");//1: character offset   OFFSET_ 
				}
				
				String phraseTag = (String) token.getAttribute(TokenAttribute.PhraseBIO);
				phraseTag = phraseTag == null ? "O" : phraseTag+" ";
				fw.write(phraseTag);//9  Phrase tag
				
				fw.write(token.getAttribute(TokenAttribute.isHead)!=null?1:0);//9  Phrase tag
				fw.write(" ");
				
				/*	*/
				String subVerb = (String)token.getAttribute(TokenAttribute.subVerb);
				subVerb=subVerb==null?"O":subVerb;
				fw.write(subVerb.replace(",", "_")+" ");//10 the lemma of the verb for which the token acts as a subject
				String objVerb = (String)token.getAttribute(TokenAttribute.objVerb);
				objVerb=objVerb==null?"O":objVerb;
				fw.write(objVerb.replace(",", "_")+" ");//11 the lemma of the verb for which the token acts as an object
				String modNoun = (String)token.getAttribute(TokenAttribute.modNoun);
				modNoun=modNoun==null?"O":modNoun;
				fw.write(modNoun.replace(",", "_")+" ");//12 the lemma of the noun for which the token acts as modifiers
				String modifier = (String)token.getAttribute(TokenAttribute.modifier);
				modifier=modifier==null?"O":modifier;
				fw.write(modifier.replace(",", "_")+" ");//13 the lemma of the modifier of that token which is a noun.
				String preposition = (String)token.getAttribute(TokenAttribute.preposition);
				preposition=preposition==null?"O":preposition;
				fw.write(preposition.replace(",", "_")+" ");//13 the lemma of the modifier of that token which is a noun.
				String prepNoun = (String)token.getAttribute(TokenAttribute.prepNoun);
				prepNoun=prepNoun==null?"O":prepNoun;
				fw.write(prepNoun.replace(",", "_")+" ");//13 the lemma of the modifier of that token which is a noun.
			
				
				//fw.write((token.getText().length()>7?7:token.getText().length())+" ");//7: length in characters (with a generic ’7’ category for length higher than seven characters)
				fw.write((Boolean)token.getAttribute(TokenAttribute.InOntoBiotope)?1:0);//14 presence of the token in the OntoBiotope ontology   OB_
				fw.write(" ");
				fw.write((Boolean)token.getAttribute(TokenAttribute.InNCBITaxonomy)?1:0);//15 presence of the token in the NCBITaxonomy ontology
				fw.write(" ");
				//fw.write(token.getAttribute(TokenAttribute.CocoaType)+" ");//cocoa type
				String geniaTagger = (String) token.getAttribute(TokenAttribute.GeniaLabel);
				geniaTagger = geniaTagger == null ? "O" : geniaTagger+" ";
				fw.write(geniaTagger);
				//16 GENIA Tagger
				fw.write(token.getAttribute(TokenAttribute.BrownCluster)+" ");//17 BrownCluster
				
				String wordSense = (String) token.getAttribute(TokenAttribute.wordSense);
				fw.write(wordSense==null?"O":wordSense);//18 word sense
				fw.write(" ");
				
				String currentLabel = token.getAttribute(TokenAttribute.NER).toString();
				fw.write(currentLabel);//"O"
				fw.write("\n");//"O"
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * generate arff file
	 * @param arffFilePath
	 */
	public void transformArffFile(String arffFilePath){
		try {
			Instances dataset = this.initDataset("trainset");
			for(Token token : datasetTokens){
				//dataset.add(this.buildFromToken(token,dataset));
				
				dataset.add(this.buildFromTokenWithPrefix(token,dataset));
			}
			 ArffSaver saver = new ArffSaver();
			 saver.setInstances(dataset);
			 saver.setFile(new File(arffFilePath));
			 saver.writeBatch();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * build an instance from a token
	 * @param token
	 * @return
	 */
	public Instance buildFromToken(Token token, Instances dataset){
		//System.out.println("attributes.size="+attributes.size());
		Instance instance = new DenseInstance(attributes.size());
		instance.setDataset(dataset);
		
		instance.setValue(text,token.getText());
		//instance.setValue(offset,token.getOffset());
		instance.setValue(typographic,(String)token.getAttribute(TokenAttribute.Typographic));
		instance.setValue(pCap,(Boolean)token.getAttribute(TokenAttribute.pCap)?1:0);
		instance.setValue(isPunctuation,(Boolean)token.getAttribute(TokenAttribute.pPunct)?1:0);
		instance.setValue(presentsDigit,(Boolean)token.getAttribute(TokenAttribute.pDigit)?1:0);
		instance.setValue(length,token.getText().length());
		
		instance.setValue(lemma,token.getLemma());
		String phraseTag = (String) token.getAttribute(TokenAttribute.PhraseBIO);
		phraseTag = phraseTag == null ? "O" : phraseTag;
		instance.setValue(pos,(String)token.getAttribute(TokenAttribute.POS));
		instance.setValue(phraseBIO,phraseTag );
		instance.setValue(phraseHead,token.getAttribute(TokenAttribute.isHead)!=null?1:0);
		
		
		String subVerbVale = (String)token.getAttribute(TokenAttribute.subVerb);
		subVerbVale=subVerbVale==null?"O":subVerbVale;
		String objVerbVale = (String)token.getAttribute(TokenAttribute.objVerb);
		objVerbVale=objVerbVale==null?"O":objVerbVale;
		String modNounVale = (String)token.getAttribute(TokenAttribute.modNoun);
		modNounVale=modNounVale==null?"O":modNounVale;
		String modifierVale = (String)token.getAttribute(TokenAttribute.modifier);
		modifierVale=modifierVale==null?"O":modifierVale;
		String prepositionVale = (String)token.getAttribute(TokenAttribute.preposition);
		prepositionVale=prepositionVale==null?"O":prepositionVale;
		String prepNounVale = (String)token.getAttribute(TokenAttribute.prepNoun);
		prepNounVale=prepNounVale==null?"O":prepNounVale;
		
		instance.setValue(subVerb,subVerbVale);
		instance.setValue(objVerb,objVerbVale);
		instance.setValue(modNoun,modNounVale);
		instance.setValue(modifier,modifierVale);
		instance.setValue(prepNoun,prepNounVale);
		instance.setValue(preposition,prepositionVale);
		
		
		//instance.setValue(length7\");
		instance.setValue(InOntoBiotope,(Boolean)token.getAttribute(TokenAttribute.InOntoBiotope)?1:0);
		instance.setValue(InNCBITaxonomy,(Boolean)token.getAttribute(TokenAttribute.InNCBITaxonomy)?1:0);
		//instance.setValue(CocoaType\");
		String geniaTagger = (String) token.getAttribute(TokenAttribute.GeniaLabel);
		geniaTagger = geniaTagger == null ? "O" : geniaTagger+" ";
		instance.setValue(GeniaLabel,geniaTagger); //Genia Tagger Label
		instance.setValue(BrownCluster,(String)token.getAttribute(TokenAttribute.BrownCluster));
		String wordSenseValue = (String)token.getAttribute(TokenAttribute.wordSense);
		wordSenseValue=wordSenseValue==null?"O":wordSenseValue;
		instance.setValue(wordSense,wordSenseValue); //extended word sense
		
		String currentLabel = token.getAttribute(TokenAttribute.NER).toString();
		instance.setClassValue(currentLabel);
		return instance;
	}
	
	/**
	 * build an instance from a token
	 * class index is set to integer
	 * 
	 * @param token
	 * @return
	 */
	public Instance buildFromTokenWithPrefix(Token token, Instances dataset){
		//System.out.println("attributes.size="+attributes.size());
		Instance instance = new DenseInstance(attributes.size());
		instance.setDataset(dataset);
		
		instance.setValue(text,token.getText());
		//instance.setValue(offset,token.getOffset());
		instance.setValue(typographic,"Ty_"+(String)token.getAttribute(TokenAttribute.Typographic));
		instance.setValue(pCap,(Boolean)token.getAttribute(TokenAttribute.pCap)?1:0);
		instance.setValue(isPunctuation,(Boolean)token.getAttribute(TokenAttribute.pPunct)?1:0);
		instance.setValue(presentsDigit,(Boolean)token.getAttribute(TokenAttribute.pDigit)?1:0);
		instance.setValue(length,token.getText().length());
		
		instance.setValue(lemma,token.getLemma());
		String phraseTag = (String) token.getAttribute(TokenAttribute.PhraseBIO);
		phraseTag = phraseTag == null ? "O" : phraseTag;
		instance.setValue(pos,"POS_"+(String)token.getAttribute(TokenAttribute.POS));
		instance.setValue(phraseBIO,"PT_"+phraseTag );
		instance.setValue(phraseHead,token.getAttribute(TokenAttribute.isHead)!=null?1:0);
		
		
		String subVerbVale = (String)token.getAttribute(TokenAttribute.subVerb);
		subVerbVale=subVerbVale==null?"O":subVerbVale;
		String objVerbVale = (String)token.getAttribute(TokenAttribute.objVerb);
		objVerbVale=objVerbVale==null?"O":objVerbVale;
		String modNounVale = (String)token.getAttribute(TokenAttribute.modNoun);
		modNounVale=modNounVale==null?"O":modNounVale;
		String modifierVale = (String)token.getAttribute(TokenAttribute.modifier);
		modifierVale=modifierVale==null?"O":modifierVale;
		String prepositionVale = (String)token.getAttribute(TokenAttribute.preposition);
		prepositionVale=prepositionVale==null?"O":prepositionVale;
		String prepNounVale = (String)token.getAttribute(TokenAttribute.prepNoun);
		prepNounVale=prepNounVale==null?"O":prepNounVale;
		
		instance.setValue(subVerb,"sv_"+subVerbVale);
		instance.setValue(objVerb,"ov_"+objVerbVale);
		instance.setValue(modNoun, "mn_"+modNounVale);
		instance.setValue(modifier,"mf_"+modifierVale);
		instance.setValue(prepNoun,"pn_"+prepNounVale);
		instance.setValue(preposition,"pp_"+prepositionVale);
		
		
		//instance.setValue(length7\");
		instance.setValue(InOntoBiotope,(Boolean)token.getAttribute(TokenAttribute.InOntoBiotope)?1:0);
		instance.setValue(InNCBITaxonomy,(Boolean)token.getAttribute(TokenAttribute.InNCBITaxonomy)?1:0);
		//instance.setValue(CocoaType\");
		String geniaTagger = (String) token.getAttribute(TokenAttribute.GeniaLabel);
		geniaTagger = geniaTagger == null ? "O" : geniaTagger+" ";
		instance.setValue(GeniaLabel,"GT_"+geniaTagger); //Genia Tagger Label
		instance.setValue(BrownCluster,(String)token.getAttribute(TokenAttribute.BrownCluster));
		String wordSenseValue = (String)token.getAttribute(TokenAttribute.wordSense);
		wordSenseValue=wordSenseValue==null?"O":wordSenseValue;
		instance.setValue(wordSense,"ws_"+wordSenseValue); //extended word sense
		
		String currentLabel = token.getAttribute(TokenAttribute.NER).toString();
		instance.setClassValue(classLabels.indexOf(currentLabel));
		return instance;
	}
	
	/**
	 * @param fileTokens
	 * @return
	 */
	public List<Instance> buildForFile(Instances testSet, List<Token> fileTokens) {
		List fileInstances = new ArrayList();
		for(Token token : fileTokens){
			Instance tokenInst = buildFromToken(token,testSet);
			testSet.add(tokenInst);
			fileInstances.add(tokenInst);
		}
		return fileInstances;
	}
	
	public Instances initDataset(String datasetName) {
		Instances testDataset = new Instances(datasetName, attributes, 0);
		testDataset.setClassIndex(testDataset.numAttributes() - 1);
		return testDataset;
	}
	
	/**
	 * 1, the attribute index should be the filteredArff index plus one.
	 * 2, the target value should be the index in classLabels plus one.
	 * 3, if the classValue is missing, the class should be 0
	 * 
	 * 
	 * @param filteredArff
	 * @param ssFormatFile
	 * @param classLabels
	 * @param classIndex
	 */
	public void transformToSVMStructFormat(String filteredArff, String ssFormatFile, List<String> classLabels, int classIndex){
		List<String> lines = FileUtil.readLineFromFile(filteredArff);
		try {
			FileWriter fw = new FileWriter(ssFormatFile);
			//System.out.println(lines.size());
			for(int i=1;i<lines.size();i++){//skip the first line
				if(lines.get(i).equals("")||lines.get(i).startsWith("@")) continue;
				String line = lines.get(i).replace("{", "").replace("}", "");
				//System.out.println(line);
				String[] attrs = line.split(",");
				int classValue = 0;
				StringBuffer attrStrBuf = new StringBuffer();
				for(String attr:attrs){
					String[] attrField = attr.split(" ");
					int attIndex = Integer.parseInt(attrField[0]);
					if(attIndex==classIndex){//index
						classValue = classLabels.indexOf(attrField[1]);
					}else{
						attrStrBuf.append((attIndex+1)+":"+attrField[1]+" ");
					}
				}
				
				fw.write((classValue+1)+" "+attrStrBuf+"\n");
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void transformTestset(String labeledFormatFile, String  testFormatFile){
		List<String> lines = FileUtil.readLineFromFile(labeledFormatFile);
		try {
			FileWriter fw = new FileWriter(testFormatFile);
			//System.out.println(lines.size());
			for(int i=1;i<lines.size();i++){//skip the first line
				if(lines.get(i).equals("")||lines.get(i).startsWith("@")) continue;
				String line = lines.get(i);
				line = line.substring(line.indexOf(" ")+1);
				fw.write(line+"\n");
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args){
		
		String datasetName = "BioNLP-ST-2016_BB-cat+ner";
		/*String datasetName = "BioNLP-ST-2016_BB-cat+ner_dev";
		TokenFeatureRender tokenFeatureRender = new TokenFeatureRender();
		List tokenList = tokenFeatureRender.render(datasetName);
		
		ArffManager arffManager = new ArffManager(tokenList);
		
		String arffFilePath = "F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\arff\\"+datasetName+"_pfx.arff";
		arffManager.transformArffFile(arffFilePath);
		
		*/
		ArffManager arffManager = new ArffManager(null);
		String fillterArffFilePath = "F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\arff\\BioNLP-ST-2016_BB-cat+ner_combine_pfx_fs.arff";
		String ssFormatFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\arff\\"+datasetName+"_fs.svmst";
		String testFormatFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\arff\\"+datasetName+"_fs_test.svmst";
		arffManager.transformToSVMStructFormat(fillterArffFilePath,ssFormatFile, arffManager.classLabels, 7);
		arffManager.transformTestset(ssFormatFile, testFormatFile);
		
	}

	
	
}

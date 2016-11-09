package edu.arizona.biosemantics.habitat.classify;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.SpreadSubsample;
import edu.arizona.biosemantics.bb.BBEntity;
import edu.arizona.biosemantics.bb.Config;
import edu.arizona.biosemantics.discourse.Token;
import edu.arizona.biosemantics.habitat.feature.TokenFeatureRender;
import edu.arizona.biosemantics.habitat.io.A1FormatFileUtil;
import edu.arizona.biosemantics.habitat.io.FileUtil;
import edu.arizona.biosemantics.habitat.ner.MalletResultProcessor;


/**
 * run the NER classification task for one dataset 
 * @author maojin
 *
 */
public class EntityClassify {
	
	private TokenFeatureRender tokenFeatureRender;
	
	//private ClassifierWrapper classifierWrapper;
	private Predictor predictor;
	public A1FormatFileUtil a1Util = new A1FormatFileUtil();
	
	
	public EntityClassify(){
		tokenFeatureRender = new TokenFeatureRender();
		//classifierWrapper = new ClassifierWrapper();
	}
	
	public void setPredictor(Predictor predictor){
		this.predictor = predictor;
	}
	
	/*
	public void loadClassifier(String[] labels, String trainedClassifierModel){
		classifierWrapper.setClassLabels(labels);
		classifierWrapper.setClassifierPath(trainedClassifierModel);
		FilteredClassifier classifier = classifierWrapper.loadClassifier();
	}
	*/
	
	/**
	 * classify the dataset
	 * @param datasetName
	 * @param outputFolder
	 * @param sequenceOutputFile
	 * @throws Exception
	 */
	public void classifyForDataset(String datasetName, String outputFolder, String sequenceOutputFile) throws Exception{
		//1, read all the files in the dataset
		// read collx
		String collxDatasetFolder = Config.stanfordFolder + "/" + datasetName;
		System.out.println(collxDatasetFolder);
		// hold all the tokens in the dataset
		File collxFolderFile = new File(collxDatasetFolder);
		File[] collxFiles = collxFolderFile.listFiles();
		
		FileUtil.createFolder(outputFolder);
		FileUtil.create(sequenceOutputFile);
		FileWriter fw = new FileWriter(new File(sequenceOutputFile));
		
		for (File collxFile : collxFiles) {
			String fileName = collxFile.getName();
			if(!fileName.endsWith("conllx")) continue;
			fileName = fileName.substring(0,fileName.indexOf("."));
			
			List<BBEntity> entities =  new ArrayList();
			//read title and paragaraph
			entities.addAll(FileUtil.readTitleAndParasFromFile(Config.txtFolder+"/"+datasetName+"/"+fileName+".txt"));
			
			//2, generate all instances for one file
			List<Token> fileTokens = tokenFeatureRender.renderForFile(datasetName, collxFile.getAbsolutePath());
			ArffManager arffManager = new ArffManager(fileTokens);
			Instances testSet = arffManager.initDataset("test");
			List<Instance> fileInstances = arffManager.buildForFile(testSet, fileTokens);
		    
			//be sure that the test set contains many class labels
//			System.out.println("testSet.numclasses="+testSet.numClasses());
//			System.out.println("testSet.numclasses="+testSet.classAttribute().name());
//			System.out.println("testSet.numclasses="+testSet.classAttribute().numValues());
			//3, classify all the tokens for the file
			Instances labeledResults = predictor.classify(testSet);
			
			entities.addAll(extractEntities(fileTokens, labeledResults));
			
			a1Util.write(outputFolder, fileName, entities);
			
			//
			String lastLabel = null;
			Iterator<Instance> labeledResultsIter = labeledResults.iterator();
			while(labeledResultsIter.hasNext()){
				Instance inst = labeledResultsIter.next();
				String currentLabel = inst.stringValue(inst.classIndex());
				//FileUtil.writeStr(fileName, label+"\n", true);
				
				if(currentLabel.equals("Habitat")&&"Habitat".equals(lastLabel)){
					fw.write("I-Habitat");//I-Habitat
				}else if(currentLabel.equals("Habitat")&&!"Habitat".equals(lastLabel)){
					fw.write("B-Habitat");//currentLabel
				}if(currentLabel.equals("Bacteria")&&"Bacteria".equals(lastLabel)){
					fw.write("I-Bacteria");//I-Bacteria
				}else if(currentLabel.equals("Bacteria")&&!"Bacteria".equals(lastLabel)){
					fw.write("B-Bacteria");//B-Bacteria
				}else if(currentLabel.equals("O")){
					fw.write("O");//"O"
				}
				lastLabel = currentLabel;
				
				fw.write("\n");
			}
			fw.flush();
		}
		fw.close();
	}


	/**
	 * extract entities from labeled tokens
	 * @param fileTokens
	 * @param fileInstances
	 * @return
	 */
	public List<BBEntity> extractEntities(List<Token> fileTokens,
			Instances dataset) {
		List<Instance> fileInstances = dataset.subList(0, dataset.numInstances());
		return extractEntities(fileTokens,fileInstances);
	}
	
	
	/**
	 * extract entities from labeled tokens
	 * @param fileTokens
	 * @param fileInstances
	 * @return
	 */
	public List<BBEntity> extractEntities(List<Token> fileTokens,
			List<Instance> fileInstances) {
		
		List<BBEntity> entities = new ArrayList();
		BBEntity entity = new BBEntity();
		entities.add(entity);
		
		for(int t = 0; t<fileTokens.size(); t++){
			Token token = fileTokens.get(t);
			Instance inst = fileInstances.get(t);
			String label = inst.stringValue(inst.classIndex());
			//System.out.println(token.getText()+" "+label);
			if(label.indexOf("Habitat")>-1){//habitat
				if(entity.getName()==null){//a new one
					entity.setName(token.getText());
					entity.setStart(token.getOffset());
					entity.setEnd(token.getOffend());
					entity.setType("Habitat");
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
			}else if(label.trim().equals("O")||label.equals("")){
				entity = new BBEntity();
				entities.add(entity);
			}
		}
		return entities;
	}
	
	public static void main(String[] args){
		EntityClassify habitatClfer = new EntityClassify();
		String trainedClassifierModel ="F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\model\\BioNLP-ST-2016_BB-cat+ner_j48_fs.model";
		String[] labels = {"Habitat","Bacteria","O"};
		ClassifierWrapper clfWrapper = new ClassifierWrapper(labels, trainedClassifierModel);
		habitatClfer.setPredictor(clfWrapper);
		
		String dataset = "BioNLP-ST-2016_BB-cat+ner_dev";
		String outputFolder = "F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\results\\"+dataset;
		String sqLabelFolder = "F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\sqresults\\"+dataset+"_j48_fs.rs";
		try{
			habitatClfer.classifyForDataset(dataset, outputFolder,sqLabelFolder);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		String gsdFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\all3\\BioNLP-ST-2016_BB-cat+ner_dev.gsd";
		String conllEvalFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\eval\\BioNLP-ST-2016_BB-cat+ner_dev.j48conll";
		
//		String rsFile = "F:\\dataset\\conll_ner\\formatted\\eng_testa.rs";
//		String gsdFile = "F:\\dataset\\conll_ner\\formatted\\eng.testa";
//		String conllEvalFile ="F:\\dataset\\conll_ner\\formatted\\eng_testa.conll";
		MalletResultProcessor cefGen = new MalletResultProcessor();
		cefGen.getCollEvalFile(gsdFile,sqLabelFolder, conllEvalFile);
		
		
		//Perl conlleval.pl < BioNLP-ST-2016_BB-cat+ner_dev.nbconll
	}
}

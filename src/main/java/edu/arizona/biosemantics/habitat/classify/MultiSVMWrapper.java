package edu.arizona.biosemantics.habitat.classify;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import edu.arizona.biosemantics.bb.Config;
import edu.arizona.biosemantics.habitat.ner.MalletResultProcessor;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.attribute.StringToNominal;
import weka.filters.unsupervised.attribute.StringToWordVector;


/**
 * One vs all SVM classifiers
 * 
 * @author maojin
 *
 */
public class MultiSVMWrapper implements Predictor{
	
	public FilteredClassifier[] svmClassifiers;
	public ClassifierWrapper[] svmWrappers;
	public String[] classLabels;
	
	public MultiSVMWrapper(){
		
	}
	
	public MultiSVMWrapper(String[] labels){
		this.classLabels = labels;
	}
	
	public MultiSVMWrapper(String[] labels, String modelFolder, String corpusName){
		this.classLabels = labels;
		loadSVMs(corpusName, labels,  modelFolder);
	}
	
	public ClassifierWrapper setupMutltiClassifier(){
		ClassifierWrapper wekaClfWrapper = new ClassifierWrapper();
		
		LibSVM svm = new LibSVM();
		try {
			svm.setOptions(weka.core.Utils.splitOptions(Config.libSVMOptions));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		wekaClfWrapper.setupClassifier(svm);
		return wekaClfWrapper;
	}
	
//	
//	public void setupClassifier(){
//		this.classifier = new FilteredClassifier();
//		 StringToWordVector STWfilter = new StringToWordVector(); 
//		 try {
//			STWfilter.setOptions(weka.core.Utils.splitOptions(Config.stringToWordVectorOptions));
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}
//		    STWfilter.setIDFTransform(true);//-I
//		    STWfilter.setTFTransform(true);//T
////		 String[] options = new String[2];
////	      options[0] = "-R";                                    // "range"
////	      options[1] = (attIndex+1)+""; 
//	      
//			//System.out.println(attIndex+" this field is a string field");
//	      StringToNominal	stringToNominal = new StringToNominal();
////			stringToNominal.setOptions(options);
//			
//		 //Filter unbalanced instances
////	    SMOTE smoteFilter = new  SMOTE();
////	     smoteFilter.setNearestNeighbors(5);
////	     smoteFilter.setPercentage(200);//1000%
//	     SpreadSubsample subsampleFilter = new SpreadSubsample();
//	     subsampleFilter.setDistributionSpread(2.0);
//	    
//		//Filter Attribute selection
//		int n = 20; //100， number of features to select 
//	    AttributeSelection attributeSelection = new  AttributeSelection(); 
//	    Ranker ranker = new Ranker(); 
//	    ranker.setNumToSelect(n);
//	    ranker.setThreshold(0.0);
//	    
//	   InfoGainAttributeEval infoGainAttributeEval = new InfoGainAttributeEval(); 
//	   //ChiSquaredAttributeEval chiSquAttributeEval = new ChiSquaredAttributeEval();
//	   // attributeSelection.setEvaluator(chiSquAttributeEval);
//	    attributeSelection.setEvaluator(infoGainAttributeEval); 
//	    attributeSelection.setSearch(ranker); 
//	    
//	    
//	    MultiFilter filter = new MultiFilter();
//	    filter.setFilters(new Filter[]{STWfilter, stringToNominal, attributeSelection});//
//
//
//	    LibSVM svm = new LibSVM();
//		try {
//			svm.setOptions(weka.core.Utils.splitOptions(Config.libSVMOptions));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		classifier.setClassifier(svm);
//	    classifier.setFilter(filter);
//	}
	
	
	/**
	 * split the datasetInto MultipleDataset
	 * @param dataset
	 * @param targetLabels
	 */
	public Instances[] splitDataset(Instances dataset, String[] targetLabels){
		Instances[] svmOneVsAllDatasets = new Instances[targetLabels.length];
		
		ArrayList attributes = new ArrayList();
		Enumeration attrEnum = dataset.enumerateAttributes();
		while(attrEnum.hasMoreElements()){
			Attribute attr = (Attribute) attrEnum.nextElement();
			attributes.add(attr);
		}
		attributes.add(dataset.attribute(dataset.classIndex()));
		System.out.println(attributes.size());
		int i=0;
		for(String targetLabel:targetLabels){
			Instances labelDataset = new Instances(targetLabel, attributes , 0);
			labelDataset.setClassIndex(labelDataset.numAttributes() - 1); 
			ArrayList<String> classLabels = new ArrayList();
			classLabels.add(targetLabel);
			classLabels.add("O");
			Attribute label = new Attribute("label",classLabels,labelDataset.numAttributes()-1);
			labelDataset.setClass(label);
			svmOneVsAllDatasets[i++]=labelDataset;
			Enumeration instEnum = dataset.enumerateInstances();
			while(instEnum.hasMoreElements()){
				Instance inst = (Instance) instEnum.nextElement();
				Instance ninst = new DenseInstance(inst);
				
				ninst.setDataset(labelDataset);
				if(ninst.stringValue(dataset.classIndex()).equals(targetLabel)){
					labelDataset.add(ninst);
				}else{
					ninst.setClassValue("O");
					labelDataset.add(ninst);
				}
			}
		}
		
		return svmOneVsAllDatasets;
	}
	
	
	/**
	 * split the arff into multiple arffs
	 * and then train multiple classifiers
	 * 
	 * @param arffFileName
	 * @param arffFolder
	 * @param targetLabels
	 * @param modelFolder
	 */
	public void trainSVMs(String arffFileName, String corpusName, String arffFolder, String[] targetLabels, String modelFolder){
		
		ClassifierWrapper wekaClfWrapper = this.setupMutltiClassifier();
		
		Instances orgDataset = wekaClfWrapper.loadInstances(arffFileName);
		//Instances[] splitDatasets = splitDataset(orgDataset, targetLabels);
		for(int i=0;i<targetLabels.length;i++){
			//wekaClfWrapper.saveArff(splitDatasets[i],arffFolder+"/"+corpusName+"_"+targetLabels[i]+".arff");
			Instances dataset = wekaClfWrapper.loadInstances(arffFolder+"/"+corpusName+"_"+targetLabels[i]+".arff");
			wekaClfWrapper.setDataset(dataset);
			wekaClfWrapper.setClassifierPath(modelFolder+"/"+corpusName+"_"+targetLabels[i]+".model");
			//wekaClfWrapper.setupClassifier(new LibSVM());
			wekaClfWrapper.train();//train and serialize the model
		}
	}
	
	
	/**
	 * load multiple classifiers
	 * @param corpusName
	 * @param targetLabels
	 * @param modelFolder
	 */
	public void loadSVMs(String corpusName, String[] targetLabels, String modelFolder){
		this.svmClassifiers = new FilteredClassifier[targetLabels.length];
		svmWrappers = new ClassifierWrapper[targetLabels.length];
		for(int i=0;i<targetLabels.length;i++){
			svmWrappers[i] = new ClassifierWrapper();
			String modelFilePath = modelFolder+"/"+corpusName+"_"+targetLabels[i]+".model";
			//To-Do: classifierWrapper.setClassLabels(labels);
			String[] labels = {targetLabels[i], "O"};
			svmWrappers[i].setClassLabels(labels);
			svmWrappers[i].setClassifierPath(modelFilePath);
			svmWrappers[i].loadClassifier();
		}
		
	}
	
	/**
	 * find the 
	 * @param instance
	 * @return
	 * @throws Exception
	 */
	public String classifyInstance(Instance instance) throws Exception{
		String label = null;
		double finalMaxProb = 0.0;
		
		for(ClassifierWrapper clfWrapper : svmWrappers){
			double[] resultDistribution = clfWrapper.getClassifier().distributionForInstance(instance);
			//find the most probably one
			int maxPropabilityIndex = 0;
			double maxPropability = 0.0;
			for(int i=0; i<resultDistribution.length; i++) {
				if(resultDistribution[i] > maxPropability) {
					maxPropability = resultDistribution[i];
					maxPropabilityIndex = i;
				}
			}
			String[] labels = clfWrapper.getLabels();
			String candLabel = labels[maxPropabilityIndex];
			
			for(String cl: classLabels){
				if(cl.equals(candLabel)&&maxPropability>finalMaxProb){
					label = candLabel;
					finalMaxProb = maxPropability;
					System.out.println("new one:"+label);
				}
			}
		}
		
		if(label==null) label ="O";
		return label;
	}
	
	
	/**
	 * predict the label by one classifier
	 * @param instance
	 * @param filteredClassifier
	 * @param labels
	 * @return
	 * @throws Exception 
	 */
	public String getClassification(Instance instance, ClassifierWrapper clfWrapper) throws Exception{
		double[] resultDistribution = clfWrapper.getClassifier().distributionForInstance(instance);
		//find the most probably one
		int maxPropabilityIndex = 0;
		double maxPropability = 0.0;
		for(int i=0; i<resultDistribution.length; i++) {
			if(resultDistribution[i] > maxPropability) {
				maxPropability = resultDistribution[i];
				maxPropabilityIndex = i;
			}
		}
		String[] labels = clfWrapper.getLabels();
		return labels[maxPropabilityIndex];
	}
	
	
	@Override
	public Instances classify(Instances unlabeled) {
		// create copy
		Instances labeled = new Instances(unlabeled);
		try {
			// label instances
			for (int i = 0; i < unlabeled.numInstances(); i++) {
				labeled.instance(i).setClassValue(classifyInstance(unlabeled.instance(i)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return labeled;
	}
	
	
	public static void main(String[] args){
		
		/* train 
		String corpusName = "BioNLP-ST-2016_BB-cat+ner";
		String arffFileName = "F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\arff\\BioNLP-ST-2016_BB-cat+ner_train_pfx.arff";
		String arffFolder = "F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\arff";
		String[] targetLabels = {"Habitat", "Bacteria"};
		String modelFolder = "F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\model";
		MultiSVMWrapper msClfer = new MultiSVMWrapper();
		msClfer.trainSVMs(arffFileName, corpusName, arffFolder, targetLabels, modelFolder);
		 */
		
		/*  */
		String clfMethod = "SVM_OVA";
		EntityClassify habitatClfer = new EntityClassify();
		String modelFolder ="F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\model";
		String trainDataset = "BioNLP-ST-2016_BB-cat+ner";
		String[] labels = {"Bacteria", "Habitat"};
		MultiSVMWrapper clfWrapper = new MultiSVMWrapper(labels, modelFolder, trainDataset);
		habitatClfer.setPredictor(clfWrapper);
		
		String testDataset = "BioNLP-ST-2016_BB-cat+ner_dev";
		String outputFolder = "F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\results\\"+testDataset+"_"+clfMethod;
		String sqLabelFolder = "F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\sqresults\\"+testDataset+"_"+clfMethod+".rs";
		try{
			habitatClfer.classifyForDataset(testDataset, outputFolder,sqLabelFolder);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		String gsdFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\all\\BioNLP-ST-2016_BB-cat+ner_dev.gsd";
		String conllEvalFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\eval\\BioNLP-ST-2016_BB-cat+ner_dev"+"_"+clfMethod+".conll";
		
//			String rsFile = "F:\\dataset\\conll_ner\\formatted\\eng_testa.rs";
//			String gsdFile = "F:\\dataset\\conll_ner\\formatted\\eng.testa";
//			String conllEvalFile ="F:\\dataset\\conll_ner\\formatted\\eng_testa.conll";
		MalletResultProcessor cefGen = new MalletResultProcessor();
		cefGen.getCollEvalFile(gsdFile,sqLabelFolder, conllEvalFile);
		
	}

	
}
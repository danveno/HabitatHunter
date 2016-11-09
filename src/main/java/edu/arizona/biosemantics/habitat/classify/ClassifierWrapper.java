package edu.arizona.biosemantics.habitat.classify;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Random;

import edu.arizona.biosemantics.bb.Config;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.StringToNominal;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class ClassifierWrapper implements Predictor{
	private String arffSource;
	private String classifierPath;
	private FilteredClassifier classifier;
	private Instances dataset;
	private String[] classLabels;

	
	public ClassifierWrapper(){
		
	}
	
	/**
	 * Initialize from serialized classifier
	 * @param classLabels
	 * @param classifierPath
	 */
	public ClassifierWrapper(String[] classLabels, String classifierPath){
		this.classLabels = classLabels;
		this.classifierPath = classifierPath;
		this.loadClassifier();
	}
	
	
	public void setArffSource(String arffSource) {
		this.arffSource = arffSource;
		Instances dataset = loadInstances(arffSource);
		setDataset(dataset);
	}
	
	public void setDataset(Instances dataset){
		this.dataset = dataset;
	}
	
	public void setClassifierPath(String classifierPath){
		this.classifierPath = classifierPath;
	}
	
	public void setClassLabels(String[] classLabels){
		this.classLabels = classLabels;
	}

	public FilteredClassifier getClassifier() {
		return this.classifier;
	}
	
	public String[] getLabels() {
		return this.classLabels;
	}
	
	public Instances loadInstances(String arffFile){
		DataSource source;
		Instances dataset=null;
		try {
			source = new DataSource(arffFile);
			dataset = source.getDataSet();
			if (dataset.classIndex() == -1)
				dataset.setClassIndex(dataset.numAttributes() - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataset;
	}
	
	
	public void setupClassifier(Classifier model){
		this.classifier = new FilteredClassifier();
		 StringToWordVector STWfilter = new StringToWordVector(); 
		 try {
			STWfilter.setOptions(weka.core.Utils.splitOptions(Config.stringToWordVectorOptions));
			//STWfilter.setIDFTransform(false);//-I
			//STWfilter.setTFTransform(false);//T
		} catch (Exception e) {
			e.printStackTrace();
		}
		   
//		 String[] options = new String[2];
//	      options[0] = "-R";                                    // "range"
//	      options[1] = (attIndex+1)+""; 
	      
			//System.out.println(attIndex+" this field is a string field");
	      StringToNominal stringToNominal = new StringToNominal();
          //stringToNominal.setOptions(C);
		
	      
	      
	      Normalize normalizeAttrs = new Normalize();
		 //Filter unbalanced instances
//	    SMOTE smoteFilter = new  SMOTE();
//	     smoteFilter.setNearestNeighbors(5);
//	     smoteFilter.setPercentage(200);//1000%
	     SpreadSubsample subsampleFilter = new SpreadSubsample();
	     subsampleFilter.setDistributionSpread(3.0);
	    
		//Filter Attribute selection
		int n = 10; //100， number of features to select 
	    AttributeSelection attributeSelection = new  AttributeSelection(); 
	    Ranker ranker = new Ranker(); 
	    ranker.setNumToSelect(n);
	    ranker.setThreshold(0.0);
	    
	   InfoGainAttributeEval infoGainAttributeEval = new InfoGainAttributeEval(); 
	   //ChiSquaredAttributeEval chiSquAttributeEval = new ChiSquaredAttributeEval();
	   // attributeSelection.setEvaluator(chiSquAttributeEval);
	    attributeSelection.setEvaluator(infoGainAttributeEval); 
	    attributeSelection.setSearch(ranker); 
	    
	    
	    MultiFilter filter = new MultiFilter();
	    //STWfilter,normalizeAttrs, attributeSelection
	    //J48: stringToNominal, attributeSelection
	    // SVM:, attributeSelection ,attributeSelection
	    //NB 1: STWfilter
	    //J48: ,subsampleFilter
	    //SVM: STWfilter
	    filter.setFilters(new Filter[]{STWfilter,subsampleFilter});// , attributeSelection,, subsampleFilter ,attributeSelection
	    classifier.setClassifier(model);
	    // classifier.setClassifier(new NaiveBayes());
	    classifier.setFilter(filter);
	}
	
	/**
	 * save the filtered instances into an arff file
	 * @param arffFilteredFilePath
	 */
	public void saveFilteredInstances(String arffFilteredFilePath){
		try {
			classifier.getFilter().setInputFormat(dataset);
			dataset = Filter.useFilter(dataset, classifier.getFilter());
			 ArffSaver saver = new ArffSaver();
			 saver.setInstances(dataset);
			 //String datasetName = "BioNLP-ST-2016_BB-cat+ner_dev";
			 //String arffFilePath = "F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\arff\\"+datasetName+"_filtered.arff";
			 saver.setFile(new File(arffFilteredFilePath));
			 saver.writeBatch();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void train() {
		try {
			// load the dataset
			if(dataset==null){
				dataset = loadInstances(arffSource);
			}
			
			//System.out.println("Instance Num: "+dataset.size());
			
			//dataset = filterBySTN(dataset);
			
			//NaiveBayes classifier = new NaiveBayes();
			
			//new J48();
			//SMO classifier = new SMO();
//			classifier.getFilter().setInputFormat(dataset);
//			
//			dataset = Filter.useFilter(dataset, classifier.getFilter());
//			 ArffSaver saver = new ArffSaver();
//			 saver.setInstances(dataset);
//				String datasetName = "BioNLP-ST-2016_BB-cat+ner_dev";
//			 String arffFilePath = "F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\arff\\"+datasetName+"_filtered.arff";
//			 saver.setFile(new File(arffFilePath));
//			 saver.writeBatch();
			 
			classifier.buildClassifier(dataset);

			Evaluation eval = new Evaluation(dataset);
			eval.crossValidateModel(classifier, dataset, 3, new Random(1));
			System.out.println("error rate:"+eval.errorRate());
			System.out.println("precision of class 0:"+eval.precision(0));
			System.out.println("precision of class 1:"+eval.precision(1));
			
			serilize(classifierPath, classifier);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * filter dataset by StringToNorminal
	 * @param dataset
	 * @return
	 */
	public Instances filterBySTN(Instances dataset){
		StringToNominal stringToNominal;
		try {
			
			for (int attIndex = 0; attIndex <= dataset.numAttributes() - 1; attIndex++) {
				 String[] options = new String[2];
			      options[0] = "-R";                                    // "range"
			      options[1] = (attIndex+1)+""; 
			      
				if (dataset.attribute(attIndex).isString()) {//||attIndex==dataset.numAttributes() - 1
					//System.out.println(attIndex+" this field is a string field");
					stringToNominal = new StringToNominal();
					stringToNominal.setOptions(options);
					stringToNominal.setInputFormat(dataset);
					dataset = Filter.useFilter(
							dataset, stringToNominal);
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException(
					"String to nominal conversion failed", ex);
		}
		return dataset;
	}
	
	/**
	 * serialize object
	 * 
	 * @param objPath
	 * @param obj
	 */
	public void serilize(String objPath,Object obj){
		try {
			weka.core.SerializationHelper.write(objPath, obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * read a classifier from the saved model file
	 */
	public Object readModel(String modelPath) {
		Object cls = null;
		try {
			cls = (Object) weka.core.SerializationHelper.read(modelPath);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return cls;
	}

	public FilteredClassifier loadClassifier() {
		Object cls = readModel(classifierPath);
		this.classifier =  (FilteredClassifier) cls;
		return this.classifier;
	}

	
	/**
	 * classify the instances
	 * @param unlabeled
	 * @return
	 */
	public Instances classify(Instances unlabeled) {
		// create copy
		Instances labeled = new Instances(unlabeled);
		try {
			// label instances
			for (int i = 0; i < unlabeled.numInstances(); i++) {
				double[] labelProbs = classifier
						.distributionForInstance(unlabeled.instance(i));
				//System.out.println(". predicted value: " + unlabeled.get(i).classAttribute().value((int) classifier.classifyInstance(unlabeled.get(i))));
				//System.out.print("classify results: "+" "+determineLabel(labelProbs)+"\n");
				labeled.instance(i).setClassValue(determineLabel(labelProbs));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return labeled;
	}
	
	/**
	 * classify the instances
	 * @param unlabeled
	 * @return
	 */
	public void classify(List<Instance> unlabeledList) {
		try {
			// label instances
			for (int i = 0; i < unlabeledList.size(); i++) {
				double[] labelProbs = classifier
						.distributionForInstance(unlabeledList.get(i));
				System.out.print("classify results: "+unlabeledList.get(i).toString()+" "+determineLabel(labelProbs)+"\n");
				// .classifyInstance(unlabeled.instance(i));
				unlabeledList.get(i).setClassValue(determineLabel(labelProbs));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * classify the instances
	 * 
	 * @param unlabeled
	 * @return
	 */
	public Instance classify(Instance unlabeled) {
		// create copy
		Instance labeled = (Instance) unlabeled.copy();
		try {
			// label instances
			double[] labelProbs = classifier.distributionForInstance(unlabeled);
			// .classifyInstance(unlabeled.instance(i));
			//System.out.print("classify results: "+labeled.toString()+" "+determineLabel(labelProbs)+"\n");
			labeled.setClassValue(determineLabel(labelProbs));
			System.out.print("classify results: "+labeled.toString()+"\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return labeled;
	}
	
	
	/**
	 * determine the labels
	 * @param labelProbs
	 * @return
	 */
	public String determineLabel(double[] labelProbs){
		int highIndex = -1;
		double highProb = 0;
		for(int i=0;i<labelProbs.length;i++){
			double prob = labelProbs[i];
			if(prob>highProb){
				highIndex = i;
				highProb = prob;
			}
		}
		return classLabels[highIndex];
	}
	
	
	/**
	 * save arff file
	 * @param dataset
	 * @param arffFilePath
	 */
	public void saveArff(Instances dataset, String arffFilePath){
		 ArffSaver saver = new ArffSaver();
		 saver.setInstances(dataset);
		 try {
			saver.setFile(new File(arffFilePath));
			saver.writeBatch();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		ClassifierWrapper classifier = new ClassifierWrapper();
		String arffSource = "F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\arff\\BioNLP-ST-2016_BB-cat+ner_train_pfx.arff";
		String trainedClassifierModel ="F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\model\\BioNLP-ST-2016_BB-cat+ner_j48_fs.model";
		String[] labels = {"Habitat","Bacteria","O"};
		
		classifier.setArffSource(arffSource);
		classifier.setClassifierPath(trainedClassifierModel);
		//classifier.setupClassifier(new NaiveBayes());
		classifier.setupClassifier(new J48());
		classifier.train();
		
		//String filteredArffFilePath = "F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\arff\\BioNLP-ST-2016_BB-cat+ner_combine_pfx_fs.arff";
		//classifier.saveFilteredInstances(filteredArffFilePath);
		
		/*
		classifier = new ClassifierWrapper();
		classifier.setClassifierPath(trainedClassifierModel);
		classifier.loadClassifier();
		
		classifier.setArffSource(arffSource);
		classifier.setClassLabels(labels);
		
		Instances samples = classifier.loadInstances(arffSource);
		samples = classifier.filterBySTN(samples);
		
		List<Instance> subsamples = samples.subList(1, 100);
		for(Instance inst:subsamples){
			classifier.classify(inst);
		}
		*/
	}//;;

}

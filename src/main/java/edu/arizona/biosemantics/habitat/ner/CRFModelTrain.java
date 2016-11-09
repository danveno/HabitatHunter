package edu.arizona.biosemantics.habitat.ner;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFOptimizableByLabelLikelihood;
import cc.mallet.fst.CRFTrainerByValueGradients;
import cc.mallet.fst.CRFWriter;
import cc.mallet.fst.MultiSegmentationEvaluator;
import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.optimize.Optimizable;
import cc.mallet.types.InstanceList;


/**
 * call the trainer
 * @author maojin
 *
 */
public class CRFModelTrain {
	/**
	 * "ner_crf.model"
	 * @param trainingData
	 * @param testingData
	 * @param modelFile
	 */
	public void run(InstanceList trainingData, InstanceList testingData, String modelFile) {
		// setup:
		// CRF (model) and the state machine
		// CRFOptimizableBy* objects (terms in the objective function)
		// CRF trainer
		// evaluator and writer

		// model, a subclass of Transducer
		CRF crf = new CRF(trainingData.getDataAlphabet(), trainingData.getTargetAlphabet());
		// construct the finite state machine
		//First order: one weight for every pair of labels and observations.
		//crf.addFullyConnectedStates();
		//crf.addFullyConnectedStatesForLabels();
		crf.addStatesForLabelsConnectedAsIn(trainingData);
		
		//“three-quarter” order: one weight for every pair of labels and observations
		//crf.addStatesForThreeQuarterLabelsConnectedAsIn(trainingData);
		//HMM-style
		crf.addStatesForThreeQuarterLabelsConnectedAsIn(trainingData);
		
		//Second order: one weight for every triplet of labels and observations.
		crf.addStatesForBiLabelsConnectedAsIn(trainingData);
		
		//“Half” order: equivalent to independent classifiers, except some transitions may be illegal.
		//crf.addStatesForHalfLabelsConnectedAsIn(trainingData);
		
		// initialize model's weights
		crf.setWeightsDimensionAsIn(trainingData, false);

		// CRFOptimizableBy* objects (terms in the objective function)
		// objective 1: label likelihood objective
		CRFOptimizableByLabelLikelihood optLabel = new CRFOptimizableByLabelLikelihood(
				crf, trainingData);

		// CRF trainer
		Optimizable.ByGradientValue[] opts = new Optimizable.ByGradientValue[] { optLabel };
		// by default, use L-BFGS as the optimizer
		CRFTrainerByValueGradients crfTrainer = new CRFTrainerByValueGradients(
				crf, opts);

		/**/
		// *Note*: labels can also be obtained from the target alphabet
		String[] labels = new String[] { "B-Bacteria", "I-Bacteri", "B-Habitat", "I-Habitat" };
		TransducerEvaluator evaluator = new MultiSegmentationEvaluator(
				new InstanceList[] { trainingData, testingData }, new String[] {
						"train", "test" }, labels, labels) {
			@Override
			public boolean precondition(TransducerTrainer tt) {
				// evaluate model every 5 training iterations
				return tt.getIteration() % 5 == 0;
			}
		};
		crfTrainer.addEvaluator(evaluator);

		CRFWriter crfWriter = new CRFWriter(modelFile) {
			@Override
			public boolean precondition(TransducerTrainer tt) {
				// save the trained model after training finishes
				return tt.getIteration() % Integer.MAX_VALUE == 0;
			}
		};
		crfTrainer.addEvaluator(crfWriter);
		
		
		
		
		// all setup done, train until convergence
		crfTrainer.setMaxResets(0);
		crfTrainer.train(trainingData, Integer.MAX_VALUE);
		
		// evaluate
		evaluator.evaluate(crfTrainer);
		// save the trained model (if CRFWriter is not used)
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(modelFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(crf);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		String trainingFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\all\\BioNLP-ST-2016_BB-cat+ner_train.txt";
		String trainedModelFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\all\\BioNLP-ST-2016_BB-cat+ner_train_oneclick.model";
		//String testingFilename="F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\all\\BioNLP-ST-2016_BB-cat+ner_dev.gsd";
		String testingFilename="F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\all\\BioNLP-ST-2016_BB-cat+ner_dev.txt";
		
//		String trainingFile = "F:\\dataset\\conll_ner\\formatted\\eng.train";
//		String trainedModelFile = "F:\\dataset\\conll_ner\\formatted\\eng_mallet.model";
//		String testingFilename="F:\\dataset\\conll_ner\\formatted\\eng.testa";
		ImportFormattedFile importer = new ImportFormattedFile();
		InstanceList trainingData = importer.readInstanceList(trainingFile);
		InstanceList testingData = importer.readInstanceList(testingFilename);
		CRFModelTrain bacNer = new CRFModelTrain();
		bacNer.run(trainingData, testingData, trainedModelFile);
	}
}

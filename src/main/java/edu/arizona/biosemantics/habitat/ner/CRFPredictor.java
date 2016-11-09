package edu.arizona.biosemantics.habitat.ner;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;

import cc.mallet.fst.CRF;
import cc.mallet.fst.MaxLatticeDefault;
import cc.mallet.fst.NoopTransducerTrainer;
import cc.mallet.fst.TokenAccuracyEvaluator;
import cc.mallet.fst.Transducer;
import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.FeatureVectorSequence;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Labeling;
import cc.mallet.types.Sequence;

/**
 * --model-file nouncrf  stest
 * @author maojin
 *
 */
public class CRFPredictor {
	
	private int nBest = 1;
	private boolean includeInput = true;
	private static int cacheSize = 100000;
	
	
	/**
	 * the last column is the annotation result
	 * @param modelPath
	 * @param testData
	 */
	public void predict(String modelPath, InstanceList testData, String testResult){
		ObjectInputStream s;
		try {
			s = new ObjectInputStream(
					new FileInputStream(modelPath));
			CRF crf = (CRF) s.readObject();
			s.close();
			
			//crf.transduce(testData);
			TransducerEvaluator eval = new TokenAccuracyEvaluator(new InstanceList[] {
					testData }, new String[] { "Testing" });
			test(crf, eval, testData,testResult);
			
			//outputTestResults(testData,testResult);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Test a transducer on the given test data, evaluating accuracy with the
	 * given evaluator
	 *
	 * @param model
	 *            a <code>Transducer</code>
	 * @param eval
	 *            accuracy evaluator
	 * @param testing
	 *            test data
	 */
	public void test(CRF crf, TransducerEvaluator eval,
			InstanceList testData, String testResultFile) {
		//eval.evaluateInstanceList(tt, testing, "Testing");
		//false 1 100000
		//includeInputOption.value()+" "+nBestOption.value+" "+cacheSizeOption.value()
		try {
			FileWriter fw = new FileWriter(testResultFile);
			for (int i = 0; i < testData.size(); i++) {
				Sequence input = (Sequence) testData.get(i).getData();
				//System.out.println("input size ="+input.size());
				Sequence[] outputs = apply(crf, input, nBest);
				//System.out.println(input.size()+" "+outputs.length+" "+outputs[outputs.length-1]);
				int k = outputs.length;
				boolean error = false;
				for (int a = 0; a < k; a++) {
					if (outputs[a].size() != input.size()) {
						error = true;
					}
				}
				if (!error) {
					for (int j = 0; j < input.size(); j++) {
						StringBuffer buf = new StringBuffer();
						for (int a = 0; a < k; a++)
							buf.append(outputs[a].get(j).toString())
									.append(" ");
						if (includeInput) {
							FeatureVector fv = (FeatureVector) input.get(j);
							buf.append(fv.toString(true));
						}
						//System.out.println(buf.toString());
						fw.write(buf.toString()+"\n");
					}
					//System.out.println();
					fw.write("\n");
				}
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * Apply a transducer to an input sequence to produce the k highest-scoring
	 * output sequences.
	 *
	 * @param model
	 *            the <code>Transducer</code>
	 * @param input
	 *            the input sequence
	 * @param k
	 *            the number of answers to return
	 * @return array of the k highest-scoring output sequences
	 */
	public static Sequence[] apply(Transducer model, Sequence input, int k) {
		Sequence[] answers;
		if (k == 1) {
			answers = new Sequence[1];
			answers[0] = model.transduce(input);
		} else {
			MaxLatticeDefault lattice = new MaxLatticeDefault(model, input,
					null, cacheSize);//cacheSizeOption.value()

			answers = lattice.bestOutputSequences(k).toArray(new Sequence[0]);
		}
		return answers;
	}
	
	
	/**
	 * 
	 * @param testData
	 * @param resultsFile
	 */
	public void outputTestResults(InstanceList testData, String resultsFile){
		try {
			FileWriter fw = new FileWriter(resultsFile);
			Iterator testIter = testData.iterator();
			while(testIter.hasNext()){
				Instance testInst = (Instance) testIter.next();
				Object testLabel = testInst.getTarget();
				FeatureVectorSequence fv = (FeatureVectorSequence) testInst.getData();
				System.out.println(testLabel.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args){
		String nerModelPath = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\all\\BioNLP-ST-2016_BB-cat+ner_train_oneclick.model";
		String testFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\all\\BioNLP-ST-2016_BB-cat+ner_dev.gsd";
		String testResult = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\all\\BioNLP-ST-2016_BB-cat+ner_dev_oneclick.rs";
		
//		String nerModelPath =  "F:\\dataset\\conll_ner\\formatted\\eng_mallet.model";
//		String testFile = "F:\\dataset\\conll_ner\\formatted\\eng_testa.raw.txt";
//		String testResult = "F:\\dataset\\conll_ner\\formatted\\eng_testa.rs";
		
		
		ImportFormattedFile importer = new ImportFormattedFile();
		InstanceList testData = importer.readInstanceList(testFile);
		//FeatureVector fv =  ((FeatureVectorSequence) testData.get(0).getData()).get(8);
		
		//System.out.println("feature size="+fv.toString(true)+" "+fv.numLocations());
//		 int indicesLength = fv.numLocations();
//		    for (int i = 0; i <fv.getIndices().length; i++) {
//			//System.out.println ("FeatureVector toString i="+i);
//			    //System.out.println ("FeatureVector toString: i="+i+" index="+indices[i]);
//		    	System.out.println (fv.getIndices()[i]);
//			    //sb.append ("("+indices[i]+")");
//		    }
		CRFPredictor crfPredictor = new CRFPredictor();
		crfPredictor.predict(nerModelPath, testData, testResult);
	}
}

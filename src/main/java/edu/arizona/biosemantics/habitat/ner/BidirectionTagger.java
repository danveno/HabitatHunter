package edu.arizona.biosemantics.habitat.ner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import cc.mallet.fst.CRF;
import cc.mallet.fst.MultiSegmentationEvaluator;
import cc.mallet.fst.NoopTransducerTrainer;
import cc.mallet.fst.TokenAccuracyEvaluator;
import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.types.Alphabet;
import cc.mallet.types.ArraySequence;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;



/**
 * combine the bidirection tagger
 * 
 * @author maojin
 * 
 */
public class BidirectionTagger extends SimpleTagger{
	/**
	 * each item in the List is a sequence. 
	 * one item contains multiple sequence label results. 
	 */
	public List<Sequence[]> forwardSeqs = new ArrayList();
	public List<Double[]> forwardSeqWeights = new ArrayList();
	
	public List<Sequence[]> backwardSeqs = new ArrayList();
	public List<Double[]> backwardSeqWeights = new ArrayList();
	
	
	public List<Sequence[]> fmergedSeqs = new ArrayList();
	public List<Double[]> fmergedWeights = new ArrayList();
	
	/**
	 * tag the results of sequences
	 * 
	 * @param modelFilePath
	 * @param testFilePath
	 * @param sequences
	 * @param sequenceWeights
	 * @param nBest
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void tagDataset(String modelFilePath, String testFilePath, List<Sequence[]> sequences, List<Double[]> sequenceWeights, int nBest) 
			throws IOException, ClassNotFoundException{
		Reader testFile = null;
		InstanceList testData = null;
		testFile = new FileReader(new File(testFilePath));

		//read crf model
		Pipe p = null;
		CRF crf = null;
		ObjectInputStream s = new ObjectInputStream(new FileInputStream(modelFilePath));
		crf = (CRF) s.readObject();
		s.close();
		p = crf.getInputPipe();

		//if (testOption.value != null) {
			p.setTargetProcessing(true);
			testData = new InstanceList(p);
			testData.addThruPipe(new LineGroupIterator(testFile, Pattern
					.compile("^\\s*$"), true));
//		} else {
//			p.setTargetProcessing(false);
//			testData = new InstanceList(p);
//			testData.addThruPipe(new LineGroupIterator(testFile, Pattern
//					.compile("^\\s*$"), true));
//		}
		
		//sequences = new ArrayList();
		//sequenceWeights = new ArrayList();
		
		for (int i = 0; i < testData.size(); i++) {
			Sequence input = (Sequence) testData.get(i).getData();
			Object[] multiRs = applyWithWeights(crf, input, nBest);
			Sequence[] outputs = (Sequence[]) multiRs[0];
			sequences.add(outputs);
			Double[] weights = (Double[]) multiRs[1];
			sequenceWeights.add(weights);
		}
		
		System.out.println(sequences.size());
	}
	
	/**
	 * merge the sequences of forward and backward
	 * 
	 */
	public void mergeSequence(){
		//reverseBackwardOrder();
		int seqNum = forwardSeqs.size();
		for(int seqIndex = 0; seqIndex<seqNum;seqIndex++){
			Sequence[] forwardSeq = forwardSeqs.get(seqIndex);
			Double[] forwardWeight = forwardSeqWeights.get(seqIndex);
			//the same input sequence
			Sequence[] backwardSeq = backwardSeqs.get(seqNum-1-seqIndex);
			Double[] backwardWeight = backwardSeqWeights.get(seqNum-1-seqIndex);
			
			List<Sequence> mergedSeqs = new ArrayList();
			List<Double> mergedSeqWeights = new ArrayList();
			
			Set matchSet = new HashSet();
			for(int k=0;k<forwardSeq.length;k++){
				Sequence kfSeq = forwardSeq[k];
				double kfWeight = forwardWeight[k];
				
				boolean isMatch = false;
				for(int b=0; b<forwardSeq.length;b++){
					Sequence backSeq = backwardSeq[b];
					backSeq = reverseBackwardOrder(backSeq);
					double backWeight = backwardWeight[b];
					if(matchSeq(kfSeq, backSeq)){//match backword and forward
						isMatch = true;
						mergedSeqs.add(kfSeq);
						mergedSeqWeights.add(kfWeight+backWeight);
						matchSet.add(b);
					}
				}
				//if this forward seq is not match with any backward seq, add it to the mergedSeq directly
				/**/
				if(!isMatch){
					mergedSeqs.add(kfSeq);
					mergedSeqWeights.add(kfWeight);
				}
				
			}
			
			//add all the backward sequences that do not match with forward seq
			for(int b=0; b<forwardSeq.length;b++){
				Sequence backSeq = backwardSeq[b];
				double backWeight = backwardWeight[b];
				if(!matchSet.contains(b)){
					mergedSeqs.add(backSeq);
					mergedSeqWeights.add(backWeight);
				}
			}
			
			
			//rank the merged sequences according to weights
			rankSequence(mergedSeqs, mergedSeqWeights);
			//System.out.println(mergedSeqs.size()+" "+mergedSeqWeights.size()+" "+mergedSeqWeights.get(0));
			fmergedSeqs.add(mergedSeqs.toArray(new Sequence[0]));
			fmergedWeights.add(mergedSeqWeights.toArray(new Double[0]));
		}
	}

	
	public void rankSequence(List<Sequence> mergedSeqs,
			List<Double> mergedSeqWeights) {
		for(int i=0;i<mergedSeqWeights.size();i++){
			double max = mergedSeqWeights.get(i);
			Sequence maxSeq = mergedSeqs.get(i);
			for(int j=i+1;j<mergedSeqWeights.size();j++){
				double weight = mergedSeqWeights.get(j);
				Sequence seq = mergedSeqs.get(j);
				if(weight>max){//swap
					mergedSeqWeights.set(i, weight);
					mergedSeqWeights.set(j, max);
					max = weight;
					
					mergedSeqs.set(i, seq);
					mergedSeqs.set(j, maxSeq);
					maxSeq = seq;
				}
			}
		}
	}

	public boolean matchSeq(Sequence kfSeq, Sequence backSeq) {
		for(int i=0;i<kfSeq.size();i++){
			if(!kfSeq.get(i).equals(backSeq.get(i))) return false;
		}
		return true;
	}

	/**
	 * reverse the backward sequences
	 * The 
	 */
	public Sequence reverseBackwardOrder(Sequence back) {
		//for(int seq = 0; seq<backwardSeqs.size();seq++){
			//Sequence[] nbackseq = backwardSeqs.get(seq);
			//for(Sequence back:nbackseq){
				String[] forwarddata = new String[back.size()];
				for(int index = back.size()-1;index>=0;index--){
					forwarddata[back.size()-1-index] = (String) back.get(index);
					//System.out.println(forwarddata[back.size()-1-index] );
				}
				back = new ArraySequence(forwarddata);
			//}
		//}
				return back;
	}
	
	
	public void outputResults(List<Sequence[]> outputSeqs, String rsFilePath, int k) throws IOException{
		FileWriter rsFw = new FileWriter(rsFilePath);
		for (int i = 0; i < outputSeqs.size(); i++) {//sequences
			Sequence[] outputs = outputSeqs.get(i);
			
			for (int j = 0; j < outputs[0].size(); j++) {
				StringBuffer buf = new StringBuffer();
				
				for (int a = 0; a < k; a++)
					buf.append(outputs[a].get(j).toString())
							.append(" ");
				rsFw.write(buf.toString());
				rsFw.write("\n");
			}
			rsFw.write("\n");
		}
		rsFw.flush();
		rsFw.close();
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException{
		
		String featureGroup = "all3";
		String forwardModelFilePath = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/"+featureGroup+"/BioNLP-ST-2016_BB-cat+ner_train.model";
		String forwardTestFilePath ="F:/Habitat/BacteriaBiotope/experiments/CRFinputs/"+featureGroup+"/BioNLP-ST-2016_BB-cat+ner_dev.txt";
		String backwardModelFilePath = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/"+featureGroup+"/BioNLP-ST-2016_BB-cat+ner_train_rvs.model";
		String backwardTestFilePath ="F:/Habitat/BacteriaBiotope/experiments/CRFinputs/"+featureGroup+"/BioNLP-ST-2016_BB-cat+ner_dev_rvs.txt";
		
		String forrsFilePath = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/"+featureGroup+"/BioNLP-ST-2016_BB-cat+ner_dev.forrs";
		String backrsFilePath = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/"+featureGroup+"/BioNLP-ST-2016_BB-cat+ner_dev.backrs";
		String rsFilePath = "F:/Habitat/BacteriaBiotope/experiments/CRFinputs/"+featureGroup+"/BioNLP-ST-2016_BB-cat+ner_dev.birs";
		int nBest = 10;
		/**/
		BidirectionTagger bidirectTagger = new BidirectionTagger();
		bidirectTagger.tagDataset(forwardModelFilePath, forwardTestFilePath, bidirectTagger.forwardSeqs, bidirectTagger.forwardSeqWeights, nBest);
		bidirectTagger.tagDataset(backwardModelFilePath, backwardTestFilePath, bidirectTagger.backwardSeqs, bidirectTagger.backwardSeqWeights, nBest);
		System.out.println(bidirectTagger.backwardSeqs.size());
		bidirectTagger.mergeSequence();
		bidirectTagger.outputResults(bidirectTagger.fmergedSeqs,rsFilePath, 1);
		bidirectTagger.outputResults(bidirectTagger.forwardSeqs,forrsFilePath, 10);
		bidirectTagger.outputResults(bidirectTagger.backwardSeqs,backrsFilePath, 10);
		
		
		String gsdFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\"+featureGroup+"\\BioNLP-ST-2016_BB-cat+ner_dev.gsd";
		String conllEvalFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\"+featureGroup+"\\BioNLP-ST-2016_BB-cat+ner_dev.biconll";
		
		MalletResultProcessor cefGen = new MalletResultProcessor();
		cefGen.getCollEvalFile(gsdFile,rsFilePath, conllEvalFile);
		
		//Perl conlleval.pl < all3/BioNLP-ST-2016_BB-cat+ner_dev.biconll
	}
}

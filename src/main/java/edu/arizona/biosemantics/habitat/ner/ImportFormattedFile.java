package edu.arizona.biosemantics.habitat.ner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.regex.Pattern;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.FeatureVectorSequence;
import cc.mallet.types.InstanceList;




public class ImportFormattedFile {
	
	/**
	 * read line format datasets
	 * @param trainset
	 * @return
	 */
	public InstanceList readInstanceList(String trainset){
		Reader trainingFile;
		try {
			Pipe p = new SimpleTaggerSentence2FeatureVectorSequence();
			//p.getTargetAlphabet().lookupIndex();
			
			trainingFile = new FileReader(new File(trainset));
			p.setTargetProcessing(true);
			
			InstanceList trainingData = new InstanceList(p);
			trainingData.addThruPipe(new LineGroupIterator(trainingFile, Pattern.compile("^\\s*$"), true));
			
			System.out.println("Import data:"+trainingData.size());
			return trainingData;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static void main(String[] args){
		Pipe p = new SimpleTaggerSentence2FeatureVectorSequence();
		//p.getTargetAlphabet().lookupIndex();

		String trainset = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\BioNLP-ST-2016_BB-cat_train_CRF.txt";
		Reader trainingFile;
		try {
			trainingFile = new FileReader(new File(trainset));
			p.setTargetProcessing(true);
			
			InstanceList trainingData = new InstanceList(p);
			trainingData.addThruPipe(new LineGroupIterator(trainingFile, Pattern.compile("^\\s*$"), true));
			
			System.out.println("Import data:"+trainingData.size());
			System.out.println("Import data:"+trainingData.getInstanceWeight(0));
			FeatureVector fv =  ((FeatureVectorSequence) trainingData.get(0).getData()).get(0);
			System.out.println("feature size="+fv.toString(true));
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}

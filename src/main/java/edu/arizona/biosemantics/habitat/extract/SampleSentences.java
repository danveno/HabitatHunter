package edu.arizona.biosemantics.habitat.extract;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import edu.arizona.biosemantics.discourse.Sentence;
import edu.arizona.biosemantics.habitat.io.SentenceReader;

public class SampleSentences {
	private SentenceReader sentReader = new SentenceReader();
	
	/**
	 * randomly sample  sampleNum sentences and save into a file
	 * @param sourceFile source files
	 * @param destFile
	 * @param sampleNum
	 */
	public void randSample(String sourceFile, String destFile, int sampleNum){
		List<Sentence> sentences = sentReader.readThreeColumn(sourceFile);
		Set<Integer> sampleIndex = new HashSet();
		while(sampleIndex.size()<sampleNum){
			Integer sampleInt = new Random().nextInt(sentences.size());
			if(!sampleIndex.contains(sampleInt)) sampleIndex.add(sampleInt); 
		}
		try {
			FileWriter candFileWriter = new FileWriter(destFile);
			for(Integer index:sampleIndex){
				Sentence sent = sentences.get(index);
				candFileWriter.write(sent.getDocid()+"");
				candFileWriter.write("\t");
				candFileWriter.write(sent.getSentid()+"");
				candFileWriter.write("\t");
				candFileWriter.write(sent.getText());
				candFileWriter.write("\n");
			}
			candFileWriter.flush();
			candFileWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		SampleSentences samSentences = new SampleSentences();
		String sourceFile = "F:\\Habitat\\procdata\\pensoftpara_exppos.txt";
		String destFile = "F:\\Habitat\\procdata\\pensoftpara_exppos_samp50.txt";
		samSentences.randSample(sourceFile, destFile, 50);
	}
	
	
	
}

package edu.arizona.biosemantics.habitat.extract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import edu.arizona.biosemantics.discourse.Sentence;
import edu.arizona.biosemantics.habitat.sentclf.ExplicitHabClassifier;




public class ProcessExplicitHabitat {
	
	private ExplicitHabitatExtractor expHabExtractor;
	
	public ProcessExplicitHabitat(ExplicitHabitatExtractor expHabExtractor){
		this.expHabExtractor = expHabExtractor;
	}
	
	/**
	 * filter the sentences
	 * 
	 * @param folderPath
	 * @param candidateFile
	 */
	public void process(String folderPath, String candidateFile) {
		File folderFile = new File(folderPath);
		File[] files = folderFile.listFiles();

		try {
			FileWriter candFileWriter = new FileWriter(candidateFile);
			for (File file : files) {

				// read the content of the file
				BufferedReader br = new BufferedReader(new InputStreamReader(
						new FileInputStream(file)));
				String line = null;

				String fileName = file.getName();
				String docId = fileName.substring(0, fileName.indexOf("."));
				// split each paragraph
				while ((line = br.readLine()) != null) {
					// each line is a sentence
					line = line.trim();
					String[] fields = line.split("\t");
					String sentId = fields[0];
					String sentText = fields[1];
					Sentence sentence = new Sentence(new Integer(docId), new Integer(sentId), sentText);
					List<String> habitatList = expHabExtractor.extract(sentence);
					
					if (habitatList!=null) {
						for(String hab : habitatList){
							if(hab!=null){
								candFileWriter.write(docId);
								candFileWriter.write("\t");
								candFileWriter.write(sentId);
								candFileWriter.write("\t");
								candFileWriter.write(hab);
								candFileWriter.write("\n");
							}
						}
					}
				}
				br.close();
				candFileWriter.flush();
			}
			candFileWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args){
		ExplicitHabClassifier ehclassifier = new ExplicitHabClassifier();
		
		ExplicitHabitatExtractor expHabExtractor = new ExplicitHabitatExtractor(ehclassifier);
		
		ProcessExplicitHabitat processHabitat = new ProcessExplicitHabitat(expHabExtractor);
		
		String folderPath = "F:\\Habitat\\procdata\\pensoftsent";
		String candidateFile = "F:\\Habitat\\procdata\\pensoftsent_exp_habitat.txt";
		
		processHabitat.process(folderPath, candidateFile);
	}
}

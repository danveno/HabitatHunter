package edu.arizona.biosemantics.habitat.extract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.arizona.biosemantics.discourse.Sentence;
import edu.arizona.biosemantics.habitat.io.TermReader;
import edu.arizona.biosemantics.habitat.sentclf.ExplicitHabClassifier;
import edu.arizona.biosemantics.habitat.sentclf.ISentBiClassifier;
import edu.arizona.biosemantics.habitat.sentclf.SentenceFilterByTerms;

/**
 * use term lists to filter the sentences
 * 
 * @author maojin
 *
 */
public class SentenceFilter  implements ICorpSentenceFilter{
	private int totalSent = 0;
	private int candiSent = 0;
	private ISentBiClassifier sentClassifier;//SentenceFilterByTerms,
	
	public SentenceFilter(ISentBiClassifier sentClassifier){
		this.sentClassifier = sentClassifier;
	}
	
	/**
	 * filter the sentences
	 * 
	 * @param folderPath
	 * @param candidateFile
	 */
	public void filter(String folderPath, String candidateFile) {
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
				//System.out.println("current file : "+docId);
				// split each paragraph
				while ((line = br.readLine()) != null) {
					// each line is a sentence
					line = line.trim();
					String[] fields = line.split("\t");
					totalSent++;
					String sentId = fields[0];
					String sentText = fields[1];
					Sentence sentence = new Sentence(sentText);

					if (isCandidate(sentence)) {
						candFileWriter.write(docId);
						candFileWriter.write("\t");
						candFileWriter.write(sentId);
						candFileWriter.write("\t");
						candFileWriter.write(sentText);
						candFileWriter.write("\n");
						candiSent++;
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
		
		System.out.println("total sentences : "+totalSent);
		System.out.println("cand sentences : "+candiSent);
	}
	
	
	
	/**
	 * filter the sentences
	 * 
	 * @param sourceFile
	 * @param candidateFile
	 * @filterType match true or false
	 */
	public void filterFile(String sourceFile, String candidateFile, boolean filterType) {
		try {
			FileWriter candFileWriter = new FileWriter(candidateFile);
			File file = new File(sourceFile);
				// read the content of the file
				BufferedReader br = new BufferedReader(new InputStreamReader(
						new FileInputStream(file)));
				String line = null;

				String fileName = file.getName();
				//System.out.println("current file : "+docId);
				// split each paragraph
				while ((line = br.readLine()) != null) {
					// each line is a sentence
					line = line.trim();
					String[] fields = line.split("\t");
					totalSent++;
					String docId = fields[0];
					String sentId = fields[1];
					String sentText = fields[2];
					Sentence sentence = new Sentence(sentText);

					if (isCandidate(sentence)==filterType) {
						candFileWriter.write(docId);
						candFileWriter.write("\t");
						candFileWriter.write(sentId);
						candFileWriter.write("\t");
						candFileWriter.write(sentText);
						candFileWriter.write("\n");
						candiSent++;
					}
				}
				br.close();
				candFileWriter.flush();
			candFileWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("total sentences : "+totalSent);
		System.out.println("cand sentences : "+candiSent);
	}
	
	
	

	/**
	 * judge whether this sentence is a candidate sentence
	 * @param sentence
	 * @return
	 */
	public boolean isCandidate(Sentence sentence) {
		return sentClassifier.isPositive(sentence);
	}
	

	public static void main(String[] args) {
		// F:\Habitat\procdata\pensoftsent
		//String termList = "F:\\Habitat\\ontology\\HatitatOntologyCandidateTerms_bterm.txt";
		//String termList = "F:\\Habitat\\ontology\\HatitatOntologyCandidateTerms2.txt";
		//String termList ="F:\\Habitat\\ontology\\Habitat Ontology\\HabitOnto_no1levelterm.txt";
		//TermReader termReader = new TermReader();
		//SentenceFilterByTerms filterByTerms = new SentenceFilterByTerms(termReader.readTermsFromOneColumnFile(termList));
		
		
		String habitTermList = "F:\\Habitat\\procdata\\pensoftsent_exp_habitat.txt";
		
		TermReader termReader = new TermReader();
		SentenceFilterByTerms filterByTerms = new SentenceFilterByTerms(termReader.readExpHabitat(habitTermList));
		//System.out.println(filterByTerms.getRegExpList().size());
		//String folderPath = "F:\\Habitat\\procdata\\test";
		String folderPath = "F:\\Habitat\\procdata\\pensoftpara";
		String candidateFile = "F:\\Habitat\\procdata\\pensoftpara_HabitOnto_no1.txt";
		String candidate2File = "F:\\Habitat\\procdata\\pensoftpara_HabitOnto_exphab.txt";
		
		
		
		SentenceFilter sf = new SentenceFilter(filterByTerms);
		
		//sf.filter(folderPath, candidateFile);
		sf.filterFile(candidateFile,candidate2File,true);
		
		/**/
		ExplicitHabClassifier expHabClassifier = new ExplicitHabClassifier();
		SentenceFilter sfByExpHabit = new SentenceFilter(expHabClassifier);
		String expPositiveFile = "F:\\Habitat\\procdata\\pensoftpara_exppos.txt";
		sfByExpHabit.filterFile(candidate2File, expPositiveFile,false);
		
	}

}

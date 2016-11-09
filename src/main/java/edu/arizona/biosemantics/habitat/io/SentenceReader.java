package edu.arizona.biosemantics.habitat.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import edu.arizona.biosemantics.discourse.Sentence;


/**
 * different types of sentence readers
 * @author maojin
 *
 */
public class SentenceReader {
	
	
	
	/**
	 * 3992	75	Tong Kamang Noi, forest (1,000 m); Phu Kheio, Chaiyaphum Province.
	 * documentid \t sentenceid \t sentence text
	 * @param file
	 * @return
	 */
	public List<Sentence> readThreeColumn(String file){
		List<Sentence> sentList = new ArrayList();
		try {
			// read the content of the file
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String line = null;
	
			//System.out.println("current file : "+docId);
			// split each paragraph
			while ((line = br.readLine()) != null) {
				// each line is a sentence
				line = line.trim();
				String[] fields = line.split("\t");
				String docId = fields[0];
				String sentId = fields[1];
				String sentText = fields[2];
				Sentence sentence = new Sentence(new Integer(docId), new Integer(sentId),sentText);
				sentList.add(sentence);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sentList;
	}
	
	
	
}

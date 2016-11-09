package edu.arizona.biosemantics.habitat.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * read term list from different format of files
 * 
 * @author maojin
 *
 */
public class TermReader {
	
	
	/**
	 * generate a list according to one column
	 * 
	 * @param termList
	 * @return 
	 * @return
	 */
	public List<String> readTermsFromOneColumnFile(String termListFile){
		List terms = new ArrayList();
		try {
			//read the content of the file
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(termListFile)));
			String line = null;

			//split each paragraph
			while((line=br.readLine())!=null){
				//each line is a paragrph
				//store into a new file
				line = line.trim();
				terms.add(line);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return terms;
	}
	
	
	
	/**
	 * read habitat terms from the explicit habitat information
	 * 1000	25	 terrestrial
	 * @param file
	 * @return
	 */
	public List readExpHabitat(String file){
		List hablist = null;
		try {
			// read the content of the file
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String line = null;
			Set habSet = new HashSet();
			// split each paragraph
			while ((line = br.readLine()) != null) {
				// each line is a sentence
				line = line.trim();
				String[] fields = line.split("\t");
				String sentId = fields[0];
				String sentText = fields[1];
				String habitat = fields[2];
				habSet.add(habitat);
			}
			br.close();
			
			hablist = new ArrayList();
			hablist.addAll(habSet);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return hablist;
	}
	
	
	
}

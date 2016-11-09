package edu.arizona.biosemantics.habitat.ontology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Extract broad terms from the ontology term list
 * @author maojin
 *
 */
public class BroaderTermGenerator {
	/**
	 * get the term frequencies
	 * @param txtFile
	 * @return
	 */
	public Set<String> findBroadTerms(String txtFile){
		Set<String> termSet = new HashSet();
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(txtFile))));
			String line = null;
			while((line=br.readLine())!=null){
				if(!"".equals(line.trim())){
					String[] args = line.split(" ");
					termSet.add(args[args.length-1]);
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return termSet;
	}
	
	
	/**
	 * save the term list into a file
	 * @param termSet
	 * @param saveFile
	 */
	public void output(Set<String> termSet, String saveFile){
		FileWriter fw;
		try {
			fw = new FileWriter(new File(saveFile));
			for(String term:termSet){ 
			  fw.write(term+"\n"); 
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args){
		BroaderTermGenerator btermGenerator = new BroaderTermGenerator();
		String termListFile = "F:\\Habitat\\ontology\\HatitatOntologyCandidateTerms.txt";
		String outputFile = "F:\\Habitat\\ontology\\HatitatOntologyCandidateTerms_bterm.txt";
		Set termSet = btermGenerator.findBroadTerms(termListFile);
		btermGenerator.output(termSet, outputFile);
	}
	
	
}

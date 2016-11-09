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
import java.util.List;
import java.util.Map;


/**
 * stats the frequencies of terms
 * @author maojin
 *
 */
public class TermStat {
	
	/**
	 * get the term frequencies
	 * @param txtFile
	 * @return
	 */
	public Map<String, Integer> chunkTerms(String txtFile){
		Map<String, Integer> termMap = new HashMap();
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(txtFile))));
			String line = null;
			while((line=br.readLine())!=null){
				if(!"".equals(line.trim())){
					String[] args = line.split(" ");
					for(String term:args){
						Integer freq = termMap.get(term);
						if(freq==null) {
							termMap.put(term, 1);
						}else{
							termMap.put(term, 1+freq);
						}
					}
				}
				
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return termMap;
	}
	
	
	public void outputByRank(Map<String, Integer> termMap, String saveFile){
		 List<Map.Entry<String, Integer>> mappingList = new ArrayList<Map.Entry<String,Integer>>(termMap.entrySet()); 
		  Collections.sort(mappingList, new Comparator<Map.Entry<String,Integer>>(){ 
		   public int compare(Map.Entry<String,Integer> mapping1,Map.Entry<String,Integer> mapping2){ 
		    return mapping2.getValue().compareTo(mapping1.getValue()); 
		   } 
		  }); 
		  
		  FileWriter fw;
		try {
			fw = new FileWriter(new File(saveFile));
			for(Map.Entry<String, Integer> mapping:mappingList){ 
			  fw.write(mapping.getKey()+" "+mapping.getValue()+"\n"); 
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		TermStat ts = new TermStat();
		String termListFile = "F:\\Habitat\\ontology\\OntoBiotope_part.txt";
		String outputFile = "F:\\Habitat\\ontology\\OntoBiotope_freq.txt";
		Map termFreqMap = ts.chunkTerms(termListFile);
		ts.outputByRank(termFreqMap, outputFile);
	}

}
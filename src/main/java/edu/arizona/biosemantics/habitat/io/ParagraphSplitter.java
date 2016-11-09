package edu.arizona.biosemantics.habitat.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;



/**
 * split a txt into multiple paragraphs
 * each line is a paragraph
 * @author maojin
 *
 */
public class ParagraphSplitter {
	
	/**
	 * split the content of txtFile and save the sentences into a new file sentFile
	 * the format is: sentId \t sentence txt;
	 * 
	 * @param txtFile
	 * @param sentFile
	 */
	public void split(File txtFile, File sentFile){
		try {
			//read the content of the file
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(txtFile)));
			String line = null;
			Integer paragraphId = 0;
			
			FileWriter fw = new FileWriter(sentFile);
			
			//split each paragraph
			while((line=br.readLine())!=null){
				//each line is a paragrph
				//store into a new file
				line = line.trim();
				if(line.equals("")) continue;
				fw.write(paragraphId+++"\t");
				fw.write(line);
				fw.write("\n");
				fw.flush();
			}
			fw.close();
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void split(File txtFile, String sentFile){
		split(txtFile, new File(sentFile));
	}
	
	
	/**
	 * 
	 * @param txtFolder
	 * @param sentFolder
	 */
	public void splitFolder(String txtFolder, String sentFolder){
		File folderFile = new File(txtFolder);
		File[] files = folderFile.listFiles();
		for(File file:files){
			String fileName = file.getName();
			String sentTxt = sentFolder+"\\"+fileName;
			split(file, sentTxt);
		}
	}
	
	
	public static void main(String[] args){
		ParagraphSplitter paraSplitter = new ParagraphSplitter();
		
		String sourceTxt = "F:\\Habitat\\procdata\\bdjtxt";//zookeystxt phytokeystxt bdjtxt
		String paraTxt = "F:\\Habitat\\procdata\\pensoftpara";
		paraSplitter.splitFolder(sourceTxt, paraTxt);
	}
	
}
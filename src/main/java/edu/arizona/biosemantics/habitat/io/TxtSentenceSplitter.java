package edu.arizona.biosemantics.habitat.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import edu.arizona.biosemantics.nlp.ISentenceSplitter;
import edu.arizona.biosemantics.nlp.OpenNLPSentenceSplitter;
import edu.arizona.biosemantics.nlp.RuleSentenceSplitter;


/**
 * Split a txt file into sentences
 * 
 * @author maojin
 *
 */
public class TxtSentenceSplitter {
	private ISentenceSplitter sentSplitter;
	
	
	
	public void setSentSplitter(ISentenceSplitter sentSplitter) {
		this.sentSplitter = sentSplitter;
	}

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
			Integer sentId = 0;
			
			FileWriter fw = new FileWriter(sentFile);
			
			//split each paragraph
			while((line=br.readLine())!=null){
				//each line is a paragrph
				//store into a new file
				line = line.trim();
				if(line.equals("")) continue;
				if(line.length()>20){
					List<String> sentList = this.sentSplitter.split(line);
					for(String sentTxt : sentList){
						fw.write(sentId+++"\t");
						fw.write(sentTxt);
						fw.write("\n");
					}
				}else{
					fw.write(sentId+++"\t");
					fw.write(line);
					fw.write("\n");
				}
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
	
	
	//OpenNLP is better than the rule based method
	public static void main(String[] args){
		OpenNLPSentenceSplitter splitter = new OpenNLPSentenceSplitter();
		
		//RuleSentenceSplitter splitter = new RuleSentenceSplitter();
		TxtSentenceSplitter txtSentSplitter = new TxtSentenceSplitter();
		txtSentSplitter.setSentSplitter(splitter);
		
		String sourceTxt = "F:\\Habitat\\procdata\\phytokeystxt";
		String sentTxt = "F:\\Habitat\\procdata\\pensoftsent";
		txtSentSplitter.splitFolder(sourceTxt, sentTxt);
	}
	
}

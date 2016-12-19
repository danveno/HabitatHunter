package edu.arizona.biosemantics.habitat.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import edu.arizona.biosemantics.util.StringUtil;


/**
 * Generate the input file for brown cluster
 * 1) read from stanford tokenizer
 * 2) remove punctuation
 * 
 * @author maojin
 */
public class BrownClusterFile {
	
	
	/**
	 * .txt.shp
	 * get the tokens from ".txt.shp" and generate new files
	 * @param shpFolder
	 * @param tokenizedFolder
	 */
	public void obtainFromShallowParsed(String shpFolder, String tokenizedFolder){
		try {
				File folderFile = new File(shpFolder);
				File[] tokenFiles = folderFile.listFiles();
				
				for(File tokenFile : tokenFiles){
					String fileName = tokenFile.getName();
					//if(!fileName.endsWith(".shp")) continue;
					//conllx
					if(!fileName.endsWith(".conllx")) continue;
					FileWriter fw = new FileWriter(tokenizedFolder+"/"+fileName.replace(".conllx", ""));
					//FileWriter fw = new FileWriter(tokenizedFolder+"/"+fileName.replace(".shp", ""));
					List<String> lines = FileUtil.readLineFromFile(tokenFile);
					for(String line:lines){
						String[] tokens = line.split("[\\s\t]+");
						if(tokens.length>1){
							//fw.write(tokens[0]);
							fw.write(tokens[1]);
							fw.write(" ");
						}else{
							fw.write("\n");
						}
					}
					fw.flush();
					fw.close();
				}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * read from stanford tokenizer
	 * 
	 * @param inputFolders
	 * @param outputFile
	 */
	public void genInputFile(String[] inputFolders, String outputFile){
		try {
			FileWriter fw = new FileWriter(outputFile);
			for(String inputFolder:inputFolders){
				File folderFile = new File(inputFolder);
				File[] tokenFiles = folderFile.listFiles();
				
				for(File tokenFile : tokenFiles){
					List<String> lines = FileUtil.readLineFromFile(tokenFile);
					for(String line:lines){
						String[] tokens = line.split("[\\s]+");
						for(String token:tokens){
							if(token.length()>0&&!StringUtil.isPunctuation(token)){
								fw.write(token);
								fw.write(" ");
							}
						}
						fw.write("\n");
					}
					fw.flush();
				}
			}
			fw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args){
		BrownClusterFile bcf = new BrownClusterFile();
		/*
		//1, get tokenized files from shallowparsed files
		String shpfolder ="F:\\Habitat\\BacteriaBiotope\\resources\\BB3\\stanford-parser\\train/BioNLP-ST-2013_Bacteria_Biotopes_train";
		//String shpfolder ="F:\\Habitat\\BacteriaBiotope\\resources\\microbiadataset\\combined_parsed";
		String tokenizedFolder ="F:\\Habitat\\BacteriaBiotope\\resources\\microbiadataset\\bactbiotope2013_trdete";
		
		bcf.obtainFromShallowParsed(shpfolder, tokenizedFolder);
		*/
		/* 2, combine into one file */
		String[] inputFolders = {"F:\\Habitat\\BacteriaBiotope\\resources\\microbiadataset\\bactbiotope2013_trdete"};
		String brownClusterInput = "F:\\Habitat\\BacteriaBiotope\\resources\\browncluster\\browncluster_13trdete_input.txt";
		bcf.genInputFile(inputFolders, brownClusterInput);
		
	}
	

}

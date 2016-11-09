package edu.arizona.biosemantics.habitat.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.bb.BBEntity;
import edu.arizona.biosemantics.util.StringUtil;


/**
 * File Utils
 * @author maojin
 *
 */
public class FileUtil {
	
	/**
	 * 
	 * @param aFile
	 * @return
	 */
	public static List<String> readLineFromFile(File aFile){
		BufferedReader br;
		List<String> lines = new ArrayList();
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(aFile)));
			String line = null;
			while((line=br.readLine())!=null){
				lines.add(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}
	
	
	/**
	 * 
	 * @param aFile
	 * @return
	 */
	public static List<String> cleanTokenListFile(String sourceFile, String cleasedFile){
		BufferedReader br;
		List<String> lines = new ArrayList();
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile)));
			String token = null;
			FileWriter fw = new FileWriter(cleasedFile);
			while((token=br.readLine())!=null){
				
				if(token.endsWith(".")||token.endsWith(",")||token.endsWith("'")||token.endsWith("\"")||token.endsWith("]")||token.endsWith(";")||token.endsWith(":")) 
					token = token.substring(0, token.length()-1);
				if(token.endsWith(".")||token.endsWith(",")||token.endsWith("'")||token.endsWith("\"")||token.endsWith("]")||token.endsWith(";")||token.endsWith(":")) 
					token = token.substring(0, token.length()-1);
				if(token.startsWith(".")||token.startsWith(",")||token.startsWith("'")||token.startsWith("\"")||token.startsWith("[")) token = token.substring(1, token.length());
				if(token.startsWith(".")||token.startsWith(",")||token.startsWith("'")||token.startsWith("\"")||token.startsWith("[")) token = token.substring(1, token.length());
				if(token.length()>1) fw.write(token+"\n");
			}
			fw.flush();
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}
	
	/**
	 * 
	 * @param aFile
	 * @return
	 */
	public static List<String> removeFiguresTokenListFile(String sourceFile, String cleasedFile){
		BufferedReader br;
		List<String> lines = new ArrayList();
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile)));
			String token = null;
			FileWriter fw = new FileWriter(cleasedFile);
			while((token=br.readLine())!=null){
				if(!StringUtil.hasDigit(token)) fw.write(token+"\n");
			}
			fw.flush();
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}
	
	
	public static List<String> readLineFromFile(String aFilePath){
		return readLineFromFile(new File(aFilePath));
	}
	
	public static List<String> readNonEmptyLineFromFile(String aFilePath){
		List<String> lines = readLineFromFile(new File(aFilePath));
		for(int line=0;line<lines.size();){
			String lineStr = lines.get(line);
			if("".equals(lineStr.trim())){
				lines.remove(line);
			}else{
				line++;
			}
		}
		return lines;
	}
	
	
	/**
	 * read term set from a file
	 * @param aFile
	 * @return
	 */
	public static Set readTermSet(File aFile){
		BufferedReader br;
		Set termSet	= new HashSet();
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(aFile)));
			String line = null;
			while((line=br.readLine())!=null){
				termSet.add(line.trim());
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
	 * read term set from a file
	 * @param aFile
	 * @return
	 */
	public static Set readTermSet(String aFile){
		return readTermSet(new File(aFile));
	}
	
	/**
	 * 
	 * @param aFile
	 * @return
	 */
	public static List<String> readLineFromReader(BufferedReader br){
		List<String> lines = new ArrayList();
		try {
			String line = null;
			while((line=br.readLine())!=null){
				lines.add(line);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}
	
	
	public static String readContent(File file) {
		BufferedReader br;
		StringBuffer sb = new StringBuffer();
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line = null;
			while((line=br.readLine())!=null){
				sb.append(line);
				sb.append("\n");
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	
	public static void writeStr(String fileName, List<String> lines, boolean append){
		try {
			FileWriter fw = new FileWriter(new File(fileName),append);
			for(String line:lines) fw.write(line+"\n");
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void create(String fileName){
		File f = new File(fileName);
		try {
			if(f.exists()){
				f.delete();
				f.createNewFile();
			}else{
				f.createNewFile();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void createFolder(String fileName){
		File f = new File(fileName);
		f.mkdir();
	}
	
	public static void writeStr(String fileName, String content, boolean append){
		try {
			FileWriter fw = new FileWriter(new File(fileName),append);
			fw.write(content);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeStr(FileWriter fw, String content){
		try {
			fw.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * reverse the order of lines and save as a new file
	 * @param inputFile
	 * @param outputFile
	 */
	public static void reverseOrder(String inputFile, String outputFile){
		List<String> lines = readLineFromFile(inputFile);
		try {
			FileWriter fw = new FileWriter(new File(outputFile));
			for(int i=lines.size()-1;i>=0;i--) {
				String line = lines.get(i);
				fw.write(line+"\n");
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * get file name by removing file suffix
	 * @param fileName
	 * @return
	 */
	public static String getFileName(String fileName){
		int dotIndex = fileName.indexOf(".");
		return fileName.substring(0,dotIndex);
	}


	public static List<BBEntity> readTitleAndParasFromFile(
			String filePath) {
		List<BBEntity> entities = new ArrayList();
		List<String> lines = readLineFromFile(new File(filePath));
		
		String fileName = StringUtil.getFileName(filePath);
		System.out.println("filePath="+fileName);
		int offset = 0;
		for(int i=0; i<lines.size();i++){
			String line = lines.get(i);
			if(i==0){
				BBEntity titleEntity = new BBEntity();
				titleEntity.setStart(offset);
				titleEntity.seteID((i+1)+"");
				titleEntity.setDocID(fileName);
				offset+=line.length();
				titleEntity.setEnd(offset);
				titleEntity.setName(line);
				titleEntity.setType("Title");
				entities.add(titleEntity);
				offset++;
			}else{
				BBEntity titleEntity = new BBEntity();
				titleEntity.seteID((i+1)+"");
				titleEntity.setDocID(fileName);
				titleEntity.setStart(offset);
				offset+=line.length();
				titleEntity.setEnd(offset);
				titleEntity.setName(line);
				titleEntity.setType("Paragraph");
				entities.add(titleEntity);
				offset++;
			}
		}
		return entities;
	}
	
	
	
	public static void rename(String folder){
		File[] files = new File(folder).listFiles();
		for(File afile:files){
			// Rename file (or directory)
			String fileName = afile.getAbsolutePath();
			String newName = fileName.replace("-cat+", "-event+");
			boolean success = afile.renameTo(new File(newName));
		}
	}
	
	public static void main(String[] args){
//		List<BBEntity> entities = FileUtil.readTitleAndParasFromFile("F:\\Habitat\\BacteriaBiotope\\bionlp 2016\\BioNLP-ST-2016_BB-cat_dev\\BB-cat-10496597.txt");
//		for(BBEntity entity : entities){
//			System.out.println(entity.geteID()+" "+entity.getStart()+" "+entity.getEnd()+" "+entity.getName());
//		}
		String sourceFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\ontology term list\\ncbi_all_tokens2.txt";
		String cleasedFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\ontology term list\\ncbi_cleaned_tokens2.txt";
		//FileUtil.cleanTokenListFile(sourceFile, cleasedFile);
		//FileUtil.removeFiguresTokenListFile(cleasedFile, "F:\\Habitat\\BacteriaBiotope\\experiments\\ontology term list\\ncbi_fine_tokens.txt");
		
		//String trainingFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\all\\BioNLP-ST-2016_BB-cat+ner_dev.txt";
		//String trainingRsFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\all_rs\\BioNLP-ST-2016_BB-cat+ner_dev_rs.txt";
		
		String source = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\wapiti\\BioNLP-ST-2016_BB-event+ner_dev_1316_rs.rs";
		String rsFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\wapiti\\BioNLP-ST-2016_BB-event+ner_dev_1316.rs";
		FileUtil.reverseOrder(source, rsFile);
		
		//FileUtil.rename("F:\\Habitat\\BacteriaBiotope\\resources\\BB3\\cocoa\\BioNLP-ST-2016_BB-event+ner_test");
		/*
		int num = 0;
		
		File[] files = new File("F:\\Habitat\\BacteriaBiotope\\2016\\BioNLP-ST-2013_Bacteria_Biotopes_dev").listFiles();
		for(File file : files){
			if(file.getName().endsWith(".a1")){
				num+=FileUtil.readNonEmptyLineFromFile(file.getPath()).size();
			}
		}
		System.out.println(num);
		*/
	}
	
}
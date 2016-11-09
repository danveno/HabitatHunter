package edu.arizona.biosemantics.habitat.feature;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import edu.arizona.biosemantics.bb.BBEntity;
import edu.arizona.biosemantics.habitat.io.A1FormatFileUtil;
import edu.arizona.biosemantics.habitat.io.FileUtil;

/**
 * use Cocoa Web API to call
 * See: http://npjoint.com/CocoaApi.html
 * 
 * @author maojin
 */
public class CocoaAnnotator {
	private A1FormatFileUtil alReader;
	
	public CocoaAnnotator(){
		this.alReader = new A1FormatFileUtil();
	}

	/**
	 * the output could be JSON, A1, a1j, B1 format.
	 * we use A1
	 * @param text
	 */
	public List callWebAPI(String text, String docFileName){
		//List<BBEntity> entities = new ArrayList();
		List<String> lines = null;
		try {
			String urlParameters = "outputFormat=a1&apikey=1234&text="+text;
			byte[] postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
			int    postDataLength = postData.length;
			String request = "http://npjoint.com/Cocoa/api/";
			
			URL url = new URL( request );
			HttpURLConnection conn= (HttpURLConnection)url.openConnection();           
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
			conn.setUseCaches(false);
			try( DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
			   wr.write( postData );
			}
			
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

			/*
			StringBuilder sb = new StringBuilder();
	        for (int c; (c = in.read()) >= 0;)
	            sb.append((char)c);
	             String response = sb.toString();
	        */
			lines = FileUtil.readLineFromReader(in);
			
//			for(String line:lines){
//				System.out.println(line);
//				entities.add(alReader.parseLine(line, docFileName));
//			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}
	
	/**
	 * annotate all the documents in the folder and save to a new folder
	 * @param inputFolder
	 * @param outputFolder
	 * @param datasetName
	 */
	public void annAllDocs(String inputFolder, String outputFolder, String datasetName){
		String datasetInputFolder = inputFolder+"/"+datasetName;
		String datasetOutputFolder = outputFolder+"/"+datasetName;
		if(!new File(datasetOutputFolder).exists()){
			new File(datasetOutputFolder).mkdirs();
		}
		File datasetInputFolderFile = new File(datasetInputFolder);
		File[] txtFiles = datasetInputFolderFile.listFiles();
		for(File txtFile:txtFiles){
			if(txtFile.getName().endsWith(".txt")){//source file
				String fileName = FileUtil.getFileName(txtFile.getName());
				List<String> lines = FileUtil.readLineFromFile(txtFile);
				StringBuffer content = new StringBuffer();
				for(String line:lines){
					content.append(line.trim()).append(" ");
				}
				//all API
				List cocoaEntities = callWebAPI(content.toString().trim(),fileName);
				//Output entities
				String cocoaFile = datasetOutputFolder+"/"+fileName+".cocoa";
				FileUtil.writeStr(cocoaFile, cocoaEntities, false);
			}
		}
	}
	
	
	
	public static void main(String[] args){
		CocoaAnnotator cocoaAnnotator = new CocoaAnnotator();
		cocoaAnnotator.callWebAPI("A smorgasbord: Liver cancer, chromatophores and tigers.A smorgasbord: Liver cancer, chromatophores and tigers.","test");
		
		String inputFolder ="F:\\Habitat\\BacteriaBiotope\\bionlp 2016";
		String outputFolder ="F:\\Habitat\\BacteriaBiotope\\resources\\BB3\\cocoa";
		String datasetName ="BioNLP-ST-2016_BB-cat_dev";
		cocoaAnnotator.annAllDocs(inputFolder, outputFolder, datasetName);
	}
}
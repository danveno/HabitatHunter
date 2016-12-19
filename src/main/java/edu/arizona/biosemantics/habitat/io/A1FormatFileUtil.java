package edu.arizona.biosemantics.habitat.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.arizona.biosemantics.bb.BBEntity;


/**
 * read entities from existing a1 files
 * A1 format:
 * http://brat.nlplab.org/standoff.html
 * http://2011.bionlp-st.org/home/file-formats
 * 
 * 
 * TODO: entities can be discontinuous.
 * @author maojin
 *
 */
public class A1FormatFileUtil {
	
	/**
	 * read entities from a folder
	 * 
	 * @param folder
	 * @return
	 */
	public List<BBEntity> readFromFolder(String folder){
		File folderFile = new File(folder);
		
		List entityList = new ArrayList();
		if(folderFile.exists()){
			File[] files = folderFile.listFiles();
			for(File aFile : files ){
				String docName = aFile.getName();
				if(docName.endsWith(".a1")){
					BufferedReader br;
					try {
						br = new BufferedReader(new InputStreamReader(new FileInputStream(aFile)));
						String line = null;
						while((line=br.readLine())!=null){
							if(line.startsWith("T")) entityList.add(this.parseLine(line, docName));
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}				
			}
		}else{
			System.out.println(folder+" doesn't exist!");
		}
		
		return entityList;
	}
	
	
	/**
	 * read entities from a file
	 * 
	 * @param folder
	 * @return
	 */
	public List<BBEntity> readFromFile(String filePath){
		List entityList = new ArrayList();
		File aFile = new File(filePath);
		String docName = aFile.getName();
		if(docName.endsWith(".a1")){
			BufferedReader br;
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(aFile)));
				String line = null;
				while((line=br.readLine())!=null){
					entityList.add(this.parseLine(line, docName));
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return entityList;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public BBEntity parseLine(String line, String docFileName){
		//T15	Bacteria 530 541	M. ulcerans
		String[] fields = line.trim().split("\t");
		//System.out.println(line+" "+fields.length);
		String[] subLocFields = fields[1].split(";");
		
		String[] rang1Fields = subLocFields[0].split(" ");
		if(subLocFields.length>1){
			String[] rang2Fields = subLocFields[1].split(" ");
			return new BBEntity(fields[0], docFileName, fields[2], rang1Fields[0], new Integer(rang1Fields[1]), new Integer(rang1Fields[2]),new Integer(rang2Fields[0]), new Integer(rang2Fields[1]));
		}else{
			//String eID, String name, String type, int start, int end
			return new BBEntity(fields[0], docFileName, fields[2], rang1Fields[0], new Integer(rang1Fields[1]), new Integer(rang1Fields[2]));
		}
		
	}
	
	
	/*
	 * output entities to the target output folder
	 */
	public void write(String outputFolder, String fileName, List<BBEntity> entities){
		if(fileName.indexOf(".")>-1) fileName = fileName.substring(0, fileName.indexOf("."));
		try{
			FileWriter fw = new FileWriter(new File(outputFolder+"/"+fileName+".a1"));
			int eid=1;
			for(BBEntity anEntity : entities){
				if(anEntity.getName()!=null){
					fw.write("T"+(eid++)+"\t"+anEntity.getType()+" "+anEntity.getStart()+" "+anEntity.getEnd()+"\t"+anEntity.getName()+"\n");
				}
			}
			fw.flush();
			fw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * organize the entities by documents
	 * @param entities
	 * @return
	 */
	public Map<String, List> getDocEntities(List<BBEntity> entities){
		//organize entities by doc
		Map<String, List> docEntityLists = new HashMap();// docId, entity list
		for(BBEntity entity:entities){
			String docId = entity.getDocID();
			List entityList = docEntityLists.get(docId);
			if(entityList==null){
				entityList =  new ArrayList();
				docEntityLists.put(docId, entityList);
			}
			entityList.add(entity);
		}
		return docEntityLists;
	}
	
	public static void main(String[] args){
		A1FormatFileUtil entityReader = new A1FormatFileUtil();
		String folder = "F:\\Habitat\\BacteriaBiotope\\2013\\BioNLP-ST-2013_Bacteria_Biotopes_test";
		List<BBEntity> entities = entityReader.readFromFolder(folder);
		
		int bacterianum=0;
		int habitatnum=0;
		int geographicnum=0;
		for(BBEntity bbentity : entities){
			if(bbentity.getType().equals("Bacteria")){
				bacterianum++;
			}else if(bbentity.getType().equals("Habitat")){
				habitatnum++;
			}else if(bbentity.getType().equals("Geographical")){
				geographicnum++;
			}
		}
		
		System.out.println("bacterianum="+bacterianum+" habitatnum="+habitatnum+" geographicnum="+geographicnum);
	}
	
}
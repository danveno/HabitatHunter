package edu.arizona.biosemantics.habitat.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.bb.BBEntity;
import edu.arizona.biosemantics.bb.BBEntityLink;
import edu.arizona.biosemantics.habitat.ontology.OntologyTerm;
import edu.arizona.biosemantics.util.StringUtil;


/**
 * A2 format file tools --- read and write
 * 
 * @author maojin
 *
 */
public class A2FormatFileUtil {
	
	private A1FormatFileUtil a1Util = new A1FormatFileUtil();
	
	
	/**
	 * generate A2 file
	 * @param docEntityList
	 * @param entityAnnMaps
	 * @param outputFolder
	 */
	public void generateSubmission(Map<String, List> docEntityList, Map<BBEntity, OntologyTerm> entityAnnMaps, String outputFolder, boolean outputA1){
		Set docSet = docEntityList.keySet();
		Iterator docIter = docSet.iterator();
		while(docIter.hasNext()){
			String doc = (String) docIter.next();
			File file = new File(outputFolder+"/"+doc.replace(".a1", ".a2"));//.replace("-cat-", "-cat+ner-")
			
			List<BBEntity> docEntities = docEntityList.get(doc);
			
			FileWriter fw;
			try {
				fw = new FileWriter(file);
				
				if(outputA1){
					for(BBEntity anEntity : docEntities){
						if(anEntity.getName()!=null){
							fw.write(anEntity.geteID()+"\t"+anEntity.getType()+" "+anEntity.getStart()+" "+anEntity.getEnd()+"\t"+anEntity.getName()+"\n");
						}
					}
				}
				
				int num = 1;
				for(BBEntity entity : docEntities){
					OntologyTerm assignedClass = entityAnnMaps.get(entity);
					if(assignedClass!=null){
						if("NCBI_Taxonomy".equals(assignedClass.getOntology())){
							fw.write("N"+num+++"\t"+assignedClass.getOntology()+" Annotation:"+entity.geteID()+" Referent:"+assignedClass.getClassId()+"\n");
						}else{//N3	OntoBiotope Annotation:T5 Referent:OBT:002133
							fw.write("N"+num+++"\t"+assignedClass.getOntology()+" Annotation:"+entity.geteID()+" Referent:OBT:"+assignedClass.getClassId()+"\n");
						}
					}
				}
				fw.flush();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * generate A2 file
	 * @param docEntityList
	 * @param entityAnnMaps
	 * @param outputFolder
	 */
	public void generateSubmission(String docName, List<BBEntity> docEntities, Map<BBEntity, OntologyTerm> bacteriaAnnMaps, 
			Map<BBEntity, OntologyTerm> habitatAnnMaps,String outputFolder, boolean outputA1){
			File file = new File(outputFolder+"/"+docName.replace(".a1", ".a2"));//.replace("-cat-", "-cat+ner-")
			
			FileWriter fw;
			try {
				fw = new FileWriter(file);
				
				if(outputA1){
					for(BBEntity anEntity : docEntities){
						if(anEntity.getName()!=null){
							fw.write(anEntity.geteID()+"\t"+anEntity.getType()+" "+anEntity.getStart()+" "+anEntity.getEnd()+"\t"+anEntity.getName()+"\n");
						}
					}
				}
				
				int num = 1;
				for(BBEntity entity : docEntities){
					if(entity.getType().equals("Bacteria")){//bacteria
						OntologyTerm assignedClass = bacteriaAnnMaps.get(entity);
						if(assignedClass!=null){
							fw.write("N"+num+++"\tNCBI_Taxonomy Annotation:"+entity.geteID()+" Referent:"+assignedClass.getClassId()+"\n");
						}
					}else if(entity.getType().equals("Habitat")){//Habitat
						OntologyTerm assignedClass= habitatAnnMaps.get(entity);
						if(assignedClass!=null){
//							for(OntologyTerm assignedClass:assignedClasses){
								//N3	OntoBiotope Annotation:T5 Referent:OBT:002133
								System.out.println(assignedClass.getClassId());
								fw.write("N"+num+++"\tOntoBiotope Annotation:"+entity.geteID()+" Referent:OBT:"+assignedClass.getClassId()+"\n");
//							}
						}
					}
				}
				fw.flush();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	
	
	/**
	 * parse a2 file, only get the entity links
	 * 
	 * @param a2File
	 */
	public List<BBEntityLink> parseA2File(String a2File){
		List<String> lines = FileUtil.readLineFromFile(a2File);
		List<BBEntityLink> entityLinkList = new ArrayList();
		String fileName = StringUtil.getFileName(a2File);
		
		for(String line : lines){
			if(line.startsWith("N")){//parse line as:    N3	OntoBiotope Annotation:T5 Referent:OBT:002133
				String[] fields = line.split("\t");
				String linkID = fields[0];
				
				String[] cfields = fields[1].split(" ");
				String ontology = cfields[0];
				String entityField = cfields[1];
				entityField = entityField.substring(entityField.indexOf(":")+1, entityField.length());
				String referentField = cfields[2];
				referentField = referentField.substring(referentField.lastIndexOf(":")+1, referentField.length());
				
				BBEntityLink entityLink = new BBEntityLink(fileName, linkID, ontology, entityField, referentField);
				entityLinkList.add(entityLink);
			}
		}
		
		return entityLinkList;
	}
	
	/**
	 * parse entities from a2 file
	 * @param a2File
	 * @return
	 */
	public List<BBEntity> parseEntity(String a2File){
		List<String> lines = FileUtil.readLineFromFile(a2File);
		List<BBEntity> entityList = new ArrayList();
		String fileName = StringUtil.getFileName(a2File);
		
		for(String line : lines){
			if(line.startsWith("T")){//parse line as: 
				BBEntity entity = a1Util.parseLine(line, fileName);
				entityList.add(entity);
			}
		}
		
		return entityList;
	}
	
	
	/**
	 * combine A1 and A2 files under sourceFolder and save into the combined folder
	 * @param sourceFolder
	 * @param combinedFolder
	 */
	public void combineA1AndA2(String sourceFolder,String combinedFolder){
		File sourceFiles = new File(sourceFolder);
		File[] files = sourceFiles.listFiles();
		for(File a1file : files){
			if(a1file.getName().endsWith(".a1")){
				String a2FileName = a1file.getName().replace(".a1", ".a2");
				
				List<String> a1Lines = FileUtil.readLineFromFile(a1file);
				File a2file = new File(sourceFolder+"/"+a2FileName);
				List<String> a2Lines = FileUtil.readLineFromFile(a2file);
				
				File newA2file = new File(combinedFolder+"/"+a2FileName);
				try {
					FileWriter fw = new FileWriter(newA2file);
					for(String line:a1Lines){
						fw.write(line);
						fw.write("\n");
					}
					for(String line:a2Lines){
						fw.write(line);
						fw.write("\n");
					}
					fw.flush();
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String[] args){
		String a2File = "F:\\Habitat\\BacteriaBiotope\\experiments\\crfresults\\BB-cat+ner-47483.a2";
		
		A2FormatFileUtil a2FileUtil = new A2FormatFileUtil();
//		List<BBEntityLink> linkList = a2FileUtil.parseA2File(a2File);
//		for(BBEntityLink link: linkList){
//			System.out.println(link.toString());
//		}
//		
//		List<BBEntity> entityList = a2FileUtil.parseEntity(a2File);
//		for(BBEntity entity:entityList){
//			System.out.println(entity.toString());
//		}
		
		a2FileUtil.combineA1AndA2("F:\\Habitat\\BacteriaBiotope\\2016\\BioNLP-ST-2016_BB-cat+ner_dev", "F:\\Habitat\\BacteriaBiotope\\2016eval\\BioNLP-ST-2016_BB-cat+ner_dev");
	}
}

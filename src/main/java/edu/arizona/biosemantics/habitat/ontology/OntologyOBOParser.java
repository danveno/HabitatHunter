package edu.arizona.biosemantics.habitat.ontology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;


/**
 * Parse the OBO format
 * @author maojin
 *
 */
public class OntologyOBOParser extends OntologyParser{
	private String idSign="OBT";//GO,

	/**
	 * Parse the specified GO ontology file 
	 * @param goFile
	 */
	public void parse(String goFilePath){
		this.termMap = new HashMap();
		File goFile = new File(goFilePath);
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(goFile));
			BufferedReader br = new BufferedReader(read);

			
			OntologyTerm term = null;
			String curTid = null; //termId;
			//read the file by line
			String line = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if(line.startsWith("[Term]")){//if the line starts with the string [Term], initialize a new Term instance
					term = new OntologyTerm();
				}else if(line.startsWith("id:")){//if the line starts with the string "id:", it is the id field
					String tid = line.replace("id: "+idSign+":", "");
					tid = tid.trim();
					curTid = tid;
					term.setClassId(curTid);
					this.termMap.put(tid, term);
					//System.out.println("tid="+term.getTid());
				}else if(line.startsWith("name:")){//the field name
					String name = line.replace("name:", "");
					name = name.trim();
					term.setPreferredName(name);
					//System.out.println(name);
				}else if(line.startsWith("namespace:")){//the field namespace
					String namespace = line.replace("namespace:", "");
					namespace = namespace.trim();
					//term.setNamespace(namespace);
				}else if(line.startsWith("def:")){//the field def
					String def = line.replace("def:", "");
					//System.out.println(def);
					//split the String
					// def: "The ....ggg ." [GOC:go_curators, GOC:isa_complete, GOC:jl, ISBN:0198506732]
					int beginIndex = def.indexOf("\"");
					int endIndex = def.lastIndexOf("\"");
					String definition = def.substring(beginIndex+1, endIndex);
					//term.setDef(definition);
					
					def = def.substring(endIndex+1,def.length());
							
					// [GOC:go_curators, GOC:isa_complete, GOC:jl, ISBN:0198506732]
					// new DefReference
					int drBeginIndex = def.indexOf("[");
					if(drBeginIndex>-1){// DefReferences exist, otherwise, no DefReference
						int drEndIndex = def.lastIndexOf("]");
						String defRefStr = def.substring(drBeginIndex+1,drEndIndex);
						if(defRefStr.indexOf("\\,")>-1){//handle the exception for [UM-BBD_pathwayID:2\,4d]
							String[] defRefPairFields = defRefStr.split(":");
							//System.out.println(defRefStr);
							//new an instance of DefReference
							DefReference defRef = new DefReference();
							defRef.setTid(curTid);
							defRef.setSource(defRefPairFields[0].trim());//source
							defRef.setItem(defRefPairFields[1].trim());//item
							//defRefList.add(defRef);
							continue;//read next line
						}
						//System.out.println(defRefStr);
						String[] defRefs = defRefStr.split(",");
						for(String defRefPair:defRefs){
							String[] defRefPairFields = defRefPair.split(":");
							//System.out.println(defRefPair);
							//new an instance of DefReference
							DefReference defRef = new DefReference();
							defRef.setTid(curTid);
							defRef.setSource(defRefPairFields[0].trim());//source
							defRef.setItem(defRefPairFields[1].trim());//item
							//defRefList.add(defRef);
						}
					}
					//System.out.println(drBeginIndex);
					
				}else if(line.startsWith("synonym:")){//the field synonym
					String synonym = line.replace("synonym:", "");
					//split the String
					// e.g. "regulation of recombination within rDNA repeats" NARROW []
					int beginIndex = synonym.indexOf("\"");
					int endIndex = synonym.lastIndexOf("\"");
					String synonymStr = synonym.substring(beginIndex+1, endIndex);
					String[] synonyms = term.getSynonyms();
					if(synonyms==null){
						synonyms = new String[]{synonymStr};
						term.setSynonyms(synonyms);
					}else{
						String[] synonymsNew = new String[synonyms.length+1];
						for(int i=0;i<synonyms.length;i++){
							synonymsNew[i]=synonyms[i];
						}
						synonymsNew[synonymsNew.length-1] = synonymStr;
						term.setSynonyms(synonymsNew);
					}
					//System.out.println(synonymStr);
					/*
					//new an instance of Synonym
					Synonym syn =  new Synonym();
					syn.setTid(curTid);
					syn.setSynonym(synonymStr);
					
					//type_cont
					int tcBeginIndex = synonym.lastIndexOf("[");
					String type = synonym.substring(endIndex+1,tcBeginIndex).trim();// NARROW 
					syn.setType(type);
					int tcEndIndex = synonym.lastIndexOf("]");
					String typeCont = synonym.substring(tcBeginIndex+1,tcEndIndex).trim();//between [ and ]
					syn.setTypeCont(typeCont);
					*/
					//synList.add(syn);
					
				}else if(line.startsWith("is_a:")){//the field is_a which is a type of relation
					//e.g. is_a: GO:0000018 ! regulation of DNA recombination
					String isAStr = line.replace("is_a: "+idSign+":", "");
					int index = isAStr.indexOf("!");
					String targetId = isAStr.substring(0, index).trim();
					
					String[] parentIds = term.getParentId();
					if(parentIds==null){
						parentIds = new String[]{targetId};
						term.setParentId(parentIds);
					}else{
						String[] parentIdsNew = new String[parentIds.length+1];
						for(int i=0;i<parentIds.length;i++){
							parentIdsNew[i]=parentIds[i];
							System.out.print("\tparentIdsNew="+parentIdsNew[i]);
						}
						parentIdsNew[parentIdsNew.length-1] = targetId;
						term.setParentId(parentIdsNew);
					}
					System.out.println();
					
					//create an is_a relation between  current termid and targetId
//					Relation relation = new Relation();
//					relation.setSourceId(curTid);
//					relation.setTargetId(targetId);
//					relation.setType(1);// Static variable or Enumeration
					
					//relationList.add(relation);
					
				}else if(line.startsWith("intersection_of:")){//the field intersection_of
					//e.g. intersection_of: GO:0065007 ! biological regulation
					String intsecStr = line.replace("intersection_of: ", "");
					int goIndex = intsecStr.indexOf("GO:");
					String subTypeStr = intsecStr.substring(0, goIndex).trim();
					int subtype = identifySubType(subTypeStr.trim());
					
					int index = intsecStr.indexOf("!");
					String targetId = intsecStr.substring(goIndex+3, index);
					//System.out.println("targetId="+targetId);
					//create an intersection_of relation between  current termid and targetId
					Relation relation = new Relation();
					relation.setSourceId(curTid);
					relation.setTargetId(targetId);
					relation.setType(5);// Static variable or Enumeration
					relation.setSubtype(subtype);
					
					//relationList.add(relation);
					
				}else if(line.startsWith("relationship:")){//the field name
					//e.g. relationship: regulates GO:0006312 ! mitotic recombination
					String relationshipStr = line.replace("relationship:", "");
					int goIndex = relationshipStr.indexOf("GO:");
					String subTypeStr = relationshipStr.substring(0, goIndex).trim();
					int subtype = identifySubType(subTypeStr.trim());
					
					int index = relationshipStr.indexOf("!");
					String targetId = relationshipStr.substring(goIndex+3, index);
					//System.out.println("targetId="+targetId);
					//create an intersection_of relation between  current termid and targetId
					Relation relation = new Relation();
					relation.setSourceId(curTid);
					relation.setTargetId(targetId);
					relation.setType(4);// Static variable or Enumeration
					relation.setSubtype(subtype);
					
					//relationList.add(relation);
					
				}else if(line.startsWith("consider:")){//the field consider
					//consider: GO:0045950
					String consider = line.replace("consider: GO:", "");
					//create an intersection_of relation between  current termid and targetId
					Relation relation = new Relation();
					relation.setSourceId(curTid);
					relation.setTargetId(consider);
					relation.setType(3);// Static variable or Enumeration
					
					//relationList.add(relation);
					
				}else if(line.startsWith("is_obsolete:")){//the field is_obsolete
					//is_obsolete: true
					String is_obsolete = line.replace("is_obsolete: ", "");
					if("true".equals(is_obsolete.trim())){
						term.setObsolete(true);
					}
				}else if(line.startsWith("comment:")){//the field comment
					//comment: The reason th
					String comment = line.replace("comment: ", "");
					//term.setComment(comment);
				}else if(line.startsWith("created_by:")){//the field created_by:
					//created_by: se
					String createdBy = line.replace("created_by: ", "");
					//term.setCreatedBy(createdBy);
				}else if(line.startsWith("creation_date:")){//the field creation_date
					//creation_date: 2014-08-25T22:37:21Z
					String creationDate = line.replace("creation_date: ", "");
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
					Date d = null;
					try {
						d = format.parse(creationDate);
					} catch (ParseException e) {
						System.err.println("something wrong happens when parsing date time format from the string ["+creationDate+"] ");
					}
					//term.setCreationDate(d);
					
					
					// todo subset
				}else if(line.startsWith("[Typedef]")){//[Typedef]
					break;
				}
				
			}//
			//final save
			
			br.close();
		} catch (FileNotFoundException e) {
			System.err.print("The file ["+goFilePath+"] does not exist!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public void formHierachy(){
		parChildMap = new HashMap();
		Iterator classIdIter = termMap.keySet().iterator();
		while(classIdIter.hasNext()){
			OntologyTerm ontoTerm = termMap.get(classIdIter.next());
			if(ontoTerm.getParentId()!=null) putIntoHierarchy(ontoTerm.getParentId(), ontoTerm);
		}
	}
	
	/**
	 * 
	 * @param subTypeStr
	 * @return
	 */
	private int identifySubType(String subTypeStr) {
		
		if("ends_during".equals(subTypeStr)) return 2093;
		else if("happens_during".equals(subTypeStr)) return 2092;
		else if("has_part".equals(subTypeStr)) return 51;
		else if("negatively_regulates".equals(subTypeStr)) return 2212;
		else if("never_in_taxon".equals(subTypeStr)) return 2161;
		else if("occurs_in".equals(subTypeStr)) return 66;
		else if("part_of".equals(subTypeStr)) return 50;
		else if("positively_regulates".equals(subTypeStr)) return 2213;
		else if("regulates".equals(subTypeStr)) return 2211;
		else if("starts_during".equals(subTypeStr)) return 2091;
		return -1;
	}
}

package edu.arizona.biosemantics.habitat.ontology.ncbi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.arizona.biosemantics.habitat.io.FileUtil;
import edu.arizona.biosemantics.habitat.ontology.OntologyParser;
import edu.arizona.biosemantics.habitat.ontology.OntologyTerm;
import edu.arizona.biosemantics.util.StringUtil;


/**
 * parse names from names.dmp
 * 
 * Taxonomy names file (names.dmp):
	tax_id					-- the id of node associated with this name
	name_txt				-- name itself
	unique name				-- the unique variant of this name if name not unique
	name class				-- (synonym, common name, ...)
	
 * @author maojin
 *
 */
public class NamesParser extends OntologyParser{
	
	/**
	 * Parse the specified GO ontology file 
	 * @param goFile
	 */
	public void parse(String ontoFile){
		
		File goFile = new File(ontoFile);
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(goFile));
			BufferedReader br = new BufferedReader(read);

			this.termMap = new HashMap();
			
			String curTid = null; //termId;
			//read the file by line
			String line = null;
			br.readLine();
			br.readLine();
			int termid = 1;
			while ((line = br.readLine()) != null) {
				String[] fields = line.split("\\|");
				String classId = fields[0].trim();
				String name = fields[1].trim();
				String uniqueName = fields[2].trim();
				String nameClass = fields[3].trim();
				
				name = removeWildCat(name);
				
//				if("scientific name".equals(nameClass)){
//					OntologyTerm term = new OntologyTerm(); 
//					term.setClassId(classId);
//					term.setPreferredName(name);
//					this.termMap.put(classId, term);
//				}else{
				
					OntologyTerm term = this.termMap.get(classId);
					
					if(term==null){
						term = new OntologyTerm(); 
						term.setClassId(classId);
						term.setPreferredName(name);
						this.termMap.put(classId, term);
					}else{
						String[] synonyms = term.getSynonyms();
						//if("synonym".equals(nameClass)){
							if(synonyms==null){
								synonyms = new String[]{name};
								term.setSynonyms(synonyms);
							}else{
								String[] synonymsNew = new String[synonyms.length+1];
								for(int i=0;i<synonyms.length;i++){
									synonymsNew[i]=synonyms[i];
								}
								synonymsNew[synonymsNew.length-1] = name;
								term.setSynonyms(synonymsNew);
							}
						//}
					}
//				}
			}
			System.out.println("In sum:"+this.termMap.size());
		} catch (FileNotFoundException e) {
			System.err.print("The file ["+ontoFile+"] does not exist!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * keep the target set
	 * @param bacIds
	 */
	public void filter(Set<Integer> bacIds){
		Map<String, OntologyTerm> newTermMap = new HashMap();
		for(String classId:termMap.keySet()){
			if(bacIds.contains(Integer.parseInt(classId))){
				newTermMap.put(classId, termMap.get(classId));
			}
		}
		termMap = newTermMap;
	}
	
	/**
	 * filter out the target set
	 * @param bacIds
	 */
	public void filterOut(Set<Integer> bacIds){
		Map<String, OntologyTerm> newTermMap = new HashMap();
		for(String classId:termMap.keySet()){
			if(!bacIds.contains(Integer.parseInt(classId))){
				newTermMap.put(classId, termMap.get(classId));
			}
		}
		termMap = newTermMap;
	}

	/**
	 * obtain those node ids with bacteria devision(0)
	 * @param goFile
	 */
	public Set obtainDevisionIds(String nodeFile, String targetDividId){
		Set nodeSet = new HashSet();
		File goFile = new File(nodeFile);
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(goFile));
			BufferedReader br = new BufferedReader(read);

			
			
			String curTid = null; //termId;
			//read the file by line
			String line = null;
			br.readLine();
			br.readLine();
			int termid = 1;
			while ((line = br.readLine()) != null) {
				String[] fields = line.split("\\|");
				String classId = fields[0].trim();
				String parClassId = fields[1].trim();
				String rank = fields[2].trim();
				String emblCode = fields[3].trim();
				String divideId = fields[4].trim();
				if(targetDividId.equals(divideId)){
					nodeSet.add(Integer.parseInt(classId));
					//System.out.println(classId+" "+rank);
				}
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		return nodeSet;
	}
	
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public String removeWildCat(String name) {
		name = name.replace("(", "");
		name = name.replace(")", "");
		return name;
	}
	
	
	/**
	 * generate the tokens or generate the terms
	 */
	public void generateTermListFile(){
		String ncbiNamesDmp = "F:\\Habitat\\BacteriaBiotope\\resources\\NCBI Taxonomy\\names.dmp";
		String nodesDmp = "F:\\Habitat\\BacteriaBiotope\\resources\\NCBI Taxonomy\\nodes.dmp";
		this.parse(ncbiNamesDmp);
		Set nodeIds = this.obtainDevisionIds(nodesDmp,"0");
		this.filterOut(nodeIds);
		
		try {
			FileWriter ncbiTokensWriter = new FileWriter(new File("F:\\Habitat\\BacteriaBiotope\\resources\\ontology term list\\nonbacteria_all_tokens.txt"));
			Set<String> tokenSet = new HashSet();
			
			for(Entry entry:termMap.entrySet()){
				OntologyTerm term = (OntologyTerm) entry.getValue();
				List<String> tokens  = StringUtil.ruleTokenize(term.getPreferredName());
				for(String token:tokens) tokenSet.add(token);
				
				if(term.getSynonyms()!=null){
					for(String syns:term.getSynonyms()){
						tokens  = StringUtil.ruleTokenize(syns);
						for(String token:tokens) tokenSet.add(token);
					}
				}
			}
			
			for(String token:tokenSet){
				if(token.length()>1) ncbiTokensWriter.write(token+"\n");
			}
			ncbiTokensWriter.flush();
			ncbiTokensWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * seperate ontology terms into tokens
	 */
	public void getFinderToken(){
		List<String> lines = FileUtil.readLineFromFile(new File("F:\\Habitat\\BacteriaBiotope\\resources\\ontology term list\\bacteria_cleaned_tokens.txt"));
		FileWriter ncbiTokensWriter;
		try {
			ncbiTokensWriter = new FileWriter(new File("F:\\Habitat\\BacteriaBiotope\\resources\\ontology term list\\bacteria_finer_tokens.txt"));
			Set<String> tokenSet = new HashSet();
			for(String line:lines){
				List<String> tokens  = StringUtil.ruleTokenize(line.trim());
				for(String token:tokens) tokenSet.add(token);
			}
			
			for(String token:tokenSet){
				if(token.length()>1) ncbiTokensWriter.write(token+"\n");
			}
			ncbiTokensWriter.flush();
			ncbiTokensWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		NamesParser nparser = new NamesParser();
		System.out.println(nparser.removeWildCat("adfafads*(aesfasd"));
		String nodeFile = "F:\\Habitat\\BacteriaBiotope\\resources\\NCBI Taxonomy\\nodes.dmp";
		//nparser.obtainDevisionIds(nodeFile,"0");
		nparser.generateTermListFile();
		
		
	}
}

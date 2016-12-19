package edu.arizona.biosemantics.habitat.feature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.arizona.biosemantics.bb.BBEntity;
import edu.arizona.biosemantics.discourse.Token;
import edu.arizona.biosemantics.discourse.TokenAttribute;
import edu.arizona.biosemantics.habitat.io.A1FormatFileUtil;
import edu.arizona.biosemantics.habitat.io.FileUtil;
import edu.arizona.biosemantics.util.StringUtil;

/**
 * read existing resources
 * @author maojin
 *
 */
public class ResourceReader {
	
	/**
	 * read
	 * @param file
	 * @return
	 */
	public List readTokenFromConllx(File file){
		List tokenList = new ArrayList();
		try {
			//File file = new File(filePath);
			int sentId =0;
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line = null;
			while((line=br.readLine())!=null){
				if("".equals(line.trim())){
					sentId++;
					continue;
				}
				String[] fields = line.trim().split("\\t");
				Token token = new Token();
				//System.out.println(fields[0]+" "+fields[1]);
				token.setFileName(FileUtil.getFileName(file.getName()));
				token.setSentenceId(sentId);
				token.setTokenId(new Integer(fields[0]));
				
				fields[1] =  StringUtil.replaceStanfordWilds(fields[1]);
				token.setText(fields[1]);
				
				token.setAttribute(TokenAttribute.TokenType,fields[3]);
				token.setAttribute(TokenAttribute.POS,fields[4]);
				token.setAttribute(TokenAttribute.HeadId,fields[7]);
				token.setAttribute(TokenAttribute.DepRole,fields[9]);
				
				//System.out.println(token.getText());
				
				tokenList.add(token);
			}
			br.close();
			br=null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return tokenList;
	}
	
	
	
	/**
	 * read tokens from shallow parse results containing token, offset, POS tag, lemma
	 * 
	 * @param file
	 * @return
	 */
	public List readTokenFromShallowParse(String fileName){
		File file = new File(fileName);
		List tokenList = new ArrayList();
		try {
			//File file = new File(filePath);
			int sentId =0;
			int tokenId =0;
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line = null;
			while((line=br.readLine())!=null){
				if("".equals(line.trim())){
					sentId++;
					tokenId =0;
					continue;
				}
				String[] fields = line.trim().split("\\t");
				Token token = new Token();
				//System.out.println(fields[0]+" "+fields[1]+" "+fields[2]+" "+fields[3]+" "+fields[4]);
				token.setFileName(FileUtil.getFileName(file.getName()));
				token.setSentenceId(sentId);
				token.setTokenId(tokenId++);
				
				fields[0] =  StringUtil.replaceStanfordWilds(fields[0]);
				token.setText(fields[0]);
				token.setAttribute(TokenAttribute.POS, fields[1]);
				token.setOffset(Integer.parseInt(fields[2]));
				token.setOffend(Integer.parseInt(fields[3]));
				token.setLemma(fields[4]);
				//System.out.println(token.getText());
				
				tokenList.add(token);
			}
			br.close();
			br=null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return tokenList;
	}
	
	
	/**
	 * read tokens from Genia Tagger results
	 * containing finer tokens, lemma, POS, Pharse BIO, Genia_Label 
	 * 
	 * @param file
	 * @return
	 */
	public List readTokenFromGeniaTagger(String fileName){
		File file = new File(fileName);
		List tokenList = new ArrayList();
		try {
			//System.out.println(fileName);
			//File file = new File(filePath);
			int sentId =0;
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line = null;
			while((line=br.readLine())!=null){
				if("".equals(line.trim())){
					sentId++;
					continue;
				}
				String[] fields = line.trim().split("\\t");
				Token token = new Token();
				//System.out.println(fields[0]+" "+fields[1]);
				token.setFileName(FileUtil.getFileName(file.getName()));
				token.setSentenceId(sentId);
				
				fields[0] =  StringUtil.replaceStanfordWilds(fields[0]);
				token.setText(fields[0]);
				token.setLemma(fields[1]);
				token.setAttribute(TokenAttribute.POS, fields[2]);
				token.setAttribute(TokenAttribute.PhraseBIO, fields[3]);
				token.setAttribute(TokenAttribute.GeniaLabel, fields[4]);
				
				tokenList.add(token);
			}
			br.close();
			br=null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return tokenList;
	}
	
	
	/**
	 * read tokens from head 
	 * containing finer tokens, lemma, POS, Pharse BIO, Genia_Label 
	 * 
	 * @param file
	 * @return
	 */
	public List readPhraseHead(String fileName){
		File file = new File(fileName);
		List tokenList = new ArrayList();
		try {
			//File file = new File(filePath);
			int sentId =0;
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line = null;
			while((line=br.readLine())!=null){
				String[] fields = line.trim().split("\\t");
				Token token = new Token();
				//System.out.println(fields[0]+" "+fields[1]);
				fields[0] =  StringUtil.replaceStanfordWilds(fields[0]);
				token.setText(fields[0]);
				token.setOffset(Integer.parseInt(fields[1]));
				token.setOffend(Integer.parseInt(fields[2]));
				tokenList.add(token);
			}
			br.close();
			br=null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return tokenList;
	}
	
	
	/**
	 * further tokenize a token
	 * 
	 * @param tokens
	 * @return
	 */
	public List finerTokenize(List<Token> tokens){
		List newTokenList = new ArrayList();
		for(Token token : tokens){
			List<String> newTokens = StringUtil.ruleTokenize(token.getText());
			if(newTokens.size()==1) newTokenList.add(token);
			else{
				for(String nt:newTokens){
					Token ntoken;
					try {
						ntoken = (Token) token.clone();
						ntoken.setText(nt);
						newTokenList.add(ntoken);
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
					
				}
			}
		}
		return newTokenList;
	}
	
	/**
	 * read
	 * @param file
	 * @return
	 */
	public List readConllxWithEmptyLine(File file){
		List tokenList = new ArrayList();
		try {
			//File file = new File(filePath);
			int sentId =0;
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line = null;
			Token lastToken = null;
			while((line=br.readLine())!=null){
				if("".equals(line.trim())){
					sentId++;
					if(lastToken!=null) tokenList.add(null);
					lastToken = null;
					continue;
				}
				String[] fields = line.trim().split("\\t");
				Token token = new Token();
				//System.out.println(fields[0]+" "+fields[1]);
				token.setFileName(FileUtil.getFileName(file.getName()));
				token.setSentenceId(sentId);
				token.setTokenId(new Integer(fields[0]));
				token.setText(fields[1]);
				
				token.setAttribute(TokenAttribute.TokenType,fields[3]);
				token.setAttribute(TokenAttribute.POS,fields[4]);
				token.setAttribute(TokenAttribute.HeadId,fields[7]);
				token.setAttribute(TokenAttribute.DepRole,fields[9]);
				
				tokenList.add(token);
				lastToken = token;
			}
			br.close();
			br=null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return tokenList;
	}
	
	
	/**
	 * read species results
	 * although it's term, treat as tokens
	 * 
	 * @param file
	 * @return
	 */
	public List readSPECIESResults(String filePath){
		List tokenList = new ArrayList();
		try {
			File file = new File(filePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line = null;
			while((line=br.readLine())!=null){
				String[] fields = line.trim().split("\\t");
				Token token = new Token(); ////
				//System.out.println(fields[0]+" "+fields[1]);
				token.setFileName(FileUtil.getFileName(file.getName()));
				token.setText(fields[0]);
				String[] offsets = fields[1].split("[\\s]+");
				//System.out.println(offsets.length);
				token.setOffset(new Integer(offsets[0]));
				token.setOffend(new Integer(offsets[1]));
				//System.out.println("fields[2]="+fields[2]);
				token.setAttribute(TokenAttribute.NCBIID,new Integer(fields[2]));////TODO: verify whether its NCBI or not
				//System.out.println("fields[3]="+fields[3]);
				token.setAttribute(TokenAttribute.TaxonRank,fields[3]);
				if(fields.length>4) {
					//System.out.println("fields[4]="+fields[4]);
					token.setAttribute(TokenAttribute.TaxonType,fields[4]);
				}
				
				tokenList.add(token);
			}
			br.close();
			br=null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return tokenList;
	}
	
	/**
	 * read LINNAEUS results
	 * although it's term, treat as tokens
	 * 
	 * Gerner, M., Nenadic, G., & Bergman, C. M. (2010). 
	 * LINNAEUS: a species name identification system for biomedical literature. BMC bioinformatics, 11(1), 1.
	 * 
	 * #entity id	document	start	end	text	comment
	 * species:ncbi:1280	BB-cat-3074181	357	378	Staphylococcus aureus	
	 * 
	 * @param file
	 * @return
	 */
	public List readLinnaeusResults(String filePath){
		List tokenList = new ArrayList();
		try {
			File file = new File(filePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line = null;
			while((line=br.readLine())!=null){
				if(line.startsWith("#")) continue;
				String[] fields = line.trim().split("\\t");
				
				if(fields[0].indexOf("|")>-1){
					String[] multipleTokens = fields[0].split("\\|");
					for(String oneTokenField:multipleTokens){
						Token token = new Token(); ////
						String[] ncbifields = oneTokenField.split("\\:");
						//System.out.println(oneTokenField+" ncbifields="+ncbifields.length+" "+ncbifields[0]);
						token.setAttribute(TokenAttribute.NCBIID,new Integer(ncbifields[2]));////TODO: verify whether its NCBI or not
						token.setFileName(fields[1]);
						token.setOffset(new Integer(fields[2]));
						token.setOffend(new Integer(fields[3]));
						token.setText(fields[4]);
						tokenList.add(token);
					}
				}else{
					Token token = new Token(); ////
					String[] ncbifields = fields[0].split("\\:");
					if(ncbifields[2].indexOf("?")>-1){
						ncbifields[2] = ncbifields[2].substring(0,ncbifields[2].indexOf("?"));
					}
					token.setAttribute(TokenAttribute.NCBIID,new Integer(ncbifields[2]));////TODO: verify whether its NCBI or not
					token.setFileName(fields[1]);
					token.setOffset(new Integer(fields[2]));
					token.setOffend(new Integer(fields[3]));
					token.setText(fields[4]);
					
					tokenList.add(token);
				}
				
				//System.out.println(token.getText()+" "+token.getAttribute(TokenAttribute.NCBIID).toString());
			}
			br.close();
			br=null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return tokenList;
	}
	
	
	
	
	/**
	 * If read all the tokens in a folder at the same time
	 * 
	 * Based on the token list, generate a token list for each file
	 * 
	 * @param tokenList
	 * @return
	 */
	public Map<String, List> orderByFile(List<Token> tokenList){
		Map<String, List> orderFile = new HashMap();
		for(Token t:tokenList){
			String fileName = t.getFileName();
			List docList = orderFile.get(fileName);
			if(docList==null){
				docList = new ArrayList();
				orderFile.put(fileName, docList);
			}
			docList.add(t);
		}
		return orderFile;
	}
	
	
	/**
	 * read cocoa results
	 * @param filePath
	 * @return
	 */
	public List<BBEntity> readCocoa(String filePath) throws Exception{
		File file = new File(filePath);
		List<String> lineStr = FileUtil.readLineFromFile(file);
		String fileName = FileUtil.getFileName(file.getName());
		List<BBEntity> entities = new ArrayList();
		A1FormatFileUtil alReader = new A1FormatFileUtil();
		for(String line:lineStr){
			entities.add(alReader.parseLine(line,fileName));
		}
		return entities;
	}
	
	/**
	 * read annotated entities
	 * @param filePath
	 * @return
	 */
	public List<BBEntity> readAnnEntities(String filePath){
		List<BBEntity> entities = new ArrayList();
		try{
		File file = new File(filePath);
		List<String> lineStr = FileUtil.readLineFromFile(file);
		String fileName = FileUtil.getFileName(file.getName());
		
		A1FormatFileUtil alReader = new A1FormatFileUtil();
		for(String line:lineStr){
			if(!line.startsWith("T")) continue;
			BBEntity entity = alReader.parseLine(line,fileName);
			if(!"Title".equals(entity.getType())&&!"Paragraph".equals(entity.getType()))entities.add(entity);
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		return entities;
	}
	
	
	public Map<String, String> readBrownCluster(String brownClusterFile){
		File file = new File(brownClusterFile);
		List<String> lineStr = FileUtil.readLineFromFile(file);
		Map<String, String> brownCluster = new HashMap();
		for(String line : lineStr){
			String[] fields = line.split("\t");
			brownCluster.put(fields[1], fields[0]);
		}
		return brownCluster;
	}
	
	
	
	public static void main(String[] args){
		ResourceReader rr = new ResourceReader();
		//String collxFile ="F:\\Habitat\\BacteriaBiotope\\resources\\BB3\\stanford-parser\\train\\BioNLP-ST-2016_BB-cat_train\\BB-cat-448557.txt.conllx";
		//rr.readStandfordConllx(collxFile);
		
		//String speciesFile ="F:\\Habitat\\BacteriaBiotope\\resources\\BB3\\species-dictionary\\train\\BioNLP-ST-2016_BB-cat_train\\BB-cat-448557.spec";
		//rr.readSPECIESResults(speciesFile);
		
		//String linnaeusFile ="F:\\Habitat\\BacteriaBiotope\\resources\\BB3\\linnaeus\\train\\BioNLP-ST-2016_BB-cat_train\\BB-cat-448557.tags";
		//rr.readLinnaeusResults(linnaeusFile);
		
		String brownClusterFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\brown_train_dev_output.txt";
		Map brownCluster = rr.readBrownCluster(brownClusterFile);
		System.out.println("brownCluster.size()="+brownCluster.size());
	}
}
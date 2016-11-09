package edu.arizona.biosemantics.habitat.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import edu.arizona.biosemantics.discourse.Token;
import edu.arizona.biosemantics.discourse.TokenAttribute;
import edu.arizona.biosemantics.habitat.feature.ResourceReader;


/**
 * read plain texts
 * 
 * 
 * @author maojin
 *
 */
public class PlainTextReader {
	
	/**
	 * match the offset of a token
	 * 
	 * new line will add a char for "\n"
	 * 
	 * @param txtFile
	 * @param tokenList
	 */
	public List<Token> matchOffset(String txtFile, List<Token> tokenList){
		try {
			//File file = new File(filePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(txtFile)));
			String line = null;
			int lineOffset = 0;
			
			int tokenIndex =0;
			
			while((line=br.readLine())!=null){
				line = line.trim();
				int lineLength = line.length();
				int currentLineOffset = 0;
				//int currentOffend = 0;
				//System.out.println("lineLength="+lineLength+" lineOffset="+lineOffset+" currentLineOffset="+currentLineOffset);
				while(currentLineOffset<lineLength){
					Token token = tokenList.get(tokenIndex++);
					if(token == null) continue;
//					if(token.getText().indexOf("-LRB-")>-1){//"-RRB-"
//						// offend = offend-4;
//						token.setText(token.getText().replace("-LRB-","("));
//					}
//					if(token.getText().indexOf("-RRB-")>-1){//"-RRB-"
//						//  offend = offend-4;
//						token.setText(token.getText().replace("-RRB-",")"));
//					}
//					if(token.getText().indexOf("-LSB-")>-1){//"-RRB-"
//						//  offend = offend-4;
//						token.setText(token.getText().replace("-LSB-","["));
//					}
//					if(token.getText().indexOf("-RSB-")>-1){//"-RRB-"
//						//  offend = offend-4;
//						token.setText(token.getText().replace("-RSB-","]"));
//					}
//					if(token.getText().indexOf("''")>-1){//"-RRB-"
//						//  offend = offend-4;
//						token.setText(token.getText().replace("''","\""));
//					}
//					//``
//					if(token.getText().indexOf("``")>-1){//"-RRB-"
//						//  offend = offend-4;
//						token.setText(token.getText().replace("``","\""));
//					}
					
					int correctOffset = findOffset(token.getText(), line, currentLineOffset);
					token.setOffset(correctOffset+lineOffset);
					
					int offend = correctOffset+token.getText().length();
					
					token.setOffend(offend+lineOffset);
					///if(token.getText().equals("selenomethionine")){
						//System.out.println(tokenIndex+" tokens="+token.getText()+" "+token.getOffset()+" "+token.getOffend()+" "+currentLineOffset+" "+correctOffset);
					//}
					//currentOffend = offend+lineOffset;
					currentLineOffset = offend;
				}
				lineOffset = lineOffset+lineLength+1;//+1
			}
			br.close();
			br=null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tokenList;
	}

	/**
	 * find the correct offset
	 * @param token
	 * @param line
	 * @param startOffset
	 */
	public int findOffset(String token, String line, int startOffset) {
		int whitespaceNum = 0;
		String subStr = line.substring(startOffset);
		int index = subStr.indexOf(token);
		while(Character.isWhitespace(line.charAt(startOffset+whitespaceNum))) whitespaceNum++;
		//System.out.println("Find token["+token+"] in ["+subStr+"]startOffset="+startOffset+" index="+index+" whitespaceNum="+whitespaceNum);
		if(whitespaceNum>index) index = whitespaceNum;
		
		return startOffset+index;
	}
	
	public static void main(String[] args){
		//String line ="We analyzed data from 1446 inpatient cases of CDI (48.6% female; median age, 62.5 years; range, 0.1-103.7 years) at the Mayo Clinic from June 28, 2007, to June 25, 2010. Patients with severe-complicated CDI (n = 487) were identified as those who required admission to the intensive care unit or colectomy, or died, within 30 days of CDI diagnosis. Logistic regression models were used to identify variables that were independently associated with the occurrence of severe-complicated CDI in 2 cohorts. One cohort comprised all hospitalized patients; the other comprised a subset of these inpatients who were residents of Olmsted County, Minnesota to assess the association of comorbid conditions with the development of severe-complicated infection in a population-based cohort. The linear combinations of variables identified by using logistic regression models provided scores to predict the risk of developing severe-complicated CDI.nellae detection in egg products.";
		
		//System.out.println(line.charAt(209)+":"+Character.isWhitespace(line.charAt(209)));
		PlainTextReader ptr = new PlainTextReader();
		//System.out.println(ptr.findOffset("=", line, 209));
		ResourceReader resourceReader = new ResourceReader();
		
		String collxFile = "F:\\Habitat\\BacteriaBiotope\\resources\\BB3\\stanford-parser\\train\\BioNLP-ST-2016_BB-cat_train\\BB-cat-16990433.txt.conllx";
		List<Token> tokenList = resourceReader.readTokenFromConllx(new File(collxFile));
		String txtFile = "F:\\Habitat\\BacteriaBiotope\\bionlp 2016\\BioNLP-ST-2016_BB-cat_train\\BB-cat-16990433.txt";
		ptr.matchOffset(txtFile, tokenList);
		
	}
	
}

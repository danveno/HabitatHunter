package edu.arizona.biosemantics.habitat.feature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.jmcejuela.bio.jenia.JeniaTagger;
import com.jmcejuela.bio.jenia.common.Sentence;

import edu.arizona.biosemantics.habitat.io.FileUtil;
import edu.arizona.biosemantics.nlp.GeniaTaggerWrapper;
import edu.arizona.biosemantics.nlp.ParserWrapper;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;


/**
 * process the dataset by Genia Tagger
 * @author maojin
 *
 */
public class GeniaTagDataset {
	//private boolean dont_tokenize = true;
	
	private ParserWrapper parser = null;
	private GeniaTaggerWrapper geniaTagger;
	
	public GeniaTagDataset(String gtPath){
		Properties stanfordCoreProperties = new Properties();
		stanfordCoreProperties.put("annotators", "tokenize, ssplit, pos, lemma, parse");//, parse
		//stanfordCoreProperties.put("depparse.extradependencies", "MAXIMAL");
		StanfordCoreNLP sfCoreNLP = new StanfordCoreNLP(stanfordCoreProperties);
		
		LexicalizedParser lexParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		
		parser = new ParserWrapper(sfCoreNLP, lexParser);
		
		//String taggerFile = "D:/Program Files/geniatagger301/geniatagger.exe";
		geniaTagger = new GeniaTaggerWrapper(gtPath);
	}

	/**
	 * tag a file
	 * @param sourceFile
	 * @param saveFile
	 */
	public void tag(String sourceFile, String saveFile){
		List<String[]> sents = parser.tokenize(sourceFile);
		try {
			FileWriter fw = new FileWriter(saveFile);
			for(String[] line:sents){
				String[][] output = geniaTagger.doTagging(line);
				 for (String row[] : output) {
		                for (String column : row) {
		                	fw.write(column + "\t");
		                }
		                fw.write("\n");
		         }
				 fw.write("\n");
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	public void tagFolder(String datasetfolder, String rsFolder){
		File stanfordFolderFile = new File(datasetfolder);
		File[] files = stanfordFolderFile.listFiles();
		for (File file : files) {
			if(!file.getName().endsWith(".txt")) continue;
			String fileName = FileUtil.getFileName(file.getName());
			String rsFile = rsFolder+"/"+fileName+".gt";
			tag(file.getPath(), rsFile);
		}
	}
	
	
	
	public static void main(String[] args){
		String sourceFile = "F:\\Habitat\\BacteriaBiotope\\2016\\BioNLP-ST-2013_Bacteria_Biotopes_dev";
		String saveFile = "F:\\Habitat\\BacteriaBiotope\\resources\\BB3\\genia-tagger\\dev\\BioNLP-ST-2013_Bacteria_Biotopes_dev";
		
		GeniaTagDataset  gt = new GeniaTagDataset("D:/Program Files/geniatagger301/geniatagger.exe");
		//gt.tag(sourceFile, saveFile);
		gt.tagFolder(sourceFile, saveFile);
	}
}

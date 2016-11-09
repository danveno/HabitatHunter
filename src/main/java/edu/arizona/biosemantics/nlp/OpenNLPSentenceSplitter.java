package edu.arizona.biosemantics.nlp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;


/**
 * sentence splitter with OpenNLP
 * @author maojin
 *
 */
public class OpenNLPSentenceSplitter implements ISentenceSplitter {

	private String sentenceModel;//the sentence model of OpenNLP
	
	private SentenceDetectorME sdetector;//the sentence detector
	
	//"/en-sent.bin";
	public OpenNLPSentenceSplitter(){
		this.sentenceModel = "/en-sent.bin";
		
		InputStream modelIn = null;
		try {
			modelIn = getClass().getResourceAsStream(sentenceModel);
			SentenceModel model = new SentenceModel(modelIn);
			this.sdetector = new SentenceDetectorME(model);
			modelIn.close();		  
		} catch (IOException e) {
		  e.printStackTrace();
		} finally {
		  if (modelIn != null) {
		    try {
		      modelIn.close();
		    }
		    catch (IOException e) {
		    }
		  }
		}
	}
	
	
	public OpenNLPSentenceSplitter(String sentenceModel){
		this.sentenceModel = sentenceModel;
		
		InputStream modelIn = null;
		try {
			//modelIn = new FileInputStream(sentenceModel);
			modelIn = getClass().getResourceAsStream(sentenceModel);
			SentenceModel model = new SentenceModel(modelIn);
			this.sdetector = new SentenceDetectorME(model);
			modelIn.close();		  
		} catch (IOException e) {
		  e.printStackTrace();
		} finally {
		  if (modelIn != null) {
		    try {
		      modelIn.close();
		    }
		    catch (IOException e) {
		    }
		  }
		}
	}
	
	
	/**
	 * split one paragraph into multiple sentences
	 */
	public List<String> split(String paragraph) {
		List<String> result = new LinkedList<String>();		
		String subSentences[] = this.sdetector.sentDetect(paragraph);
		for ( int i = 0; i < subSentences.length; i++ ) {
			String subSent = subSentences[i];
			result.add(subSent);
		}
		return result;
	}
	

}

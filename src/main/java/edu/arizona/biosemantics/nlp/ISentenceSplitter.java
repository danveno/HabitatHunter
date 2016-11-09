package edu.arizona.biosemantics.nlp;

import java.util.List;


/**
 * sentence splitter
 * @author maojin
 *
 */
public interface ISentenceSplitter {
	
	/**
	 * split a paragraph to a list of sentences
	 * @param paragraph
	 * @return
	 */
	List<String> split(String paragraph);
	
	
}

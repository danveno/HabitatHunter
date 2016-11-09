package edu.arizona.biosemantics.nlp;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import smile.nlp.tokenizer.SimpleSentenceSplitter;


/**
 * <h2>References</h2>
 * <ol>
 * <li> Paul Clough. A Perl program for sentence splitting using rules. <li>
 * </ol>
 * @author maojin
 *
 */
public class RuleSentenceSplitter implements ISentenceSplitter {

	public List<String> split(String paragraph) {
		SimpleSentenceSplitter  ssSpliter = SimpleSentenceSplitter.getInstance();
		String[] sents = ssSpliter.split(paragraph);
		ArrayList al = new ArrayList();
		for(String sent:sents){
			al.add(sent);
		}
		return al;
	}

}

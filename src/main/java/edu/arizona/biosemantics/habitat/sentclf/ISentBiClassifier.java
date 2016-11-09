package edu.arizona.biosemantics.habitat.sentclf;

import edu.arizona.biosemantics.discourse.Sentence;


/**
 * Binary Sentence Classifier
 * @author maojin
 *
 */
public interface ISentBiClassifier {
	
	/**
	 * To test whether this sentence is a positive instance
	 * @param sent
	 * @return
	 */
	public boolean isPositive(Sentence sent);
	
	
}

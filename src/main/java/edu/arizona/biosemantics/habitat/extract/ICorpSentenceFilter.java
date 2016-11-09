package edu.arizona.biosemantics.habitat.extract;

import edu.arizona.biosemantics.discourse.Sentence;

public interface ICorpSentenceFilter {
	/**
	 * filter the corpus and then save the candidate sentences into a file
	 * @param folderPath
	 * @param candidateFile
	 */
	public void filter(String folderPath, String candidateFile);
	
	
	/**
	 * judege whether the sentence is a candidate sentence
	 * @param sentence
	 * @return
	 */
	public boolean isCandidate(Sentence sentence);
	
	
}

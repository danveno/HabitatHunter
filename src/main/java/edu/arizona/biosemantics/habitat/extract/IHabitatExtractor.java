package edu.arizona.biosemantics.habitat.extract;

import java.util.List;

import edu.arizona.biosemantics.discourse.Sentence;


public interface IHabitatExtractor {
	
	List extract(Sentence sent);
	
}
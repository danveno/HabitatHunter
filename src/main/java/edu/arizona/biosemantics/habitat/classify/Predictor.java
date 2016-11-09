package edu.arizona.biosemantics.habitat.classify;

import weka.core.Instances;

public interface Predictor {
	public Instances classify(Instances unlabeled);
}

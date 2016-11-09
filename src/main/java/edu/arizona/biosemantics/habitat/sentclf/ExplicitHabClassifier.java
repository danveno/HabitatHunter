package edu.arizona.biosemantics.habitat.sentclf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.arizona.biosemantics.discourse.Sentence;


/**
 * whether the sentence explicitly states habitat.
 * 
 * @author maojin
 *
 */
public class ExplicitHabClassifier implements ISentBiClassifier {

	/**
	 * The clues are:
	 * 1. it should contains the term "habitat"
	 * 2. a colon follows the term "habitat"
	 * TODO:    3. if there is at least one semi-comma, split to find the right habitat
	 *      4. if there is no semi-comma, split with the colon.
	 */
	public boolean isPositive(Sentence sent) {
		String regExp = "[;.,\\s][Hh]abitat[\\s]*:|^[Hh]abitat[\\s]*:";//
		Matcher matcher = Pattern.compile(regExp).matcher(sent.getText());			
		if(matcher.find()) {
			return true;
		}
		return false;
	}
	
	
	public static void main(String[] args){
		ExplicitHabClassifier ehc = new ExplicitHabClassifier();
		Sentence sent = new Sentence();
		sent.setText(" Habitat: birch forest;");
		System.out.println(ehc.isPositive(sent));
	}

}

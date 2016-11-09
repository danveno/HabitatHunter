package BioSemantics.HabitatHunter;

import edu.stanford.nlp.util.EditDistance;

public class Test {
	public static void main(String[] args){
		EditDistance d = new EditDistance();
	    System.out.println(d.score("bcdesf", "abcdes"));
	}
}

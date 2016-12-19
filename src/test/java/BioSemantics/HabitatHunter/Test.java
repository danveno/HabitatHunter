package BioSemantics.HabitatHunter;

import java.text.DecimalFormat;

import edu.stanford.nlp.util.EditDistance;

public class Test {
	public static void main(String[] args){
		double dVal = 290.1234;
		DecimalFormat dff=new DecimalFormat(".###"); //
		
		System.out.printf ("Value with 4 digits after decimal point %.4f   ", dVal);
		System.out.println(dff.format(dVal));
	}
}

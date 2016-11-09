package edu.cmu.lti.ws4j.demo;

import java.util.List;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.PorterStemmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

public class SimilarityCalculationDemo {
	
	public static PorterStemmer proterStermmer = new PorterStemmer();
	
	private static ILexicalDatabase db = new NictWordNet();
	private static RelatednessCalculator[] rcs = {
			new HirstStOnge(db), new LeacockChodorow(db), new Lesk(db),  new WuPalmer(db), 
			new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db)
			};
	
	private static void run( String word1, String word2 ) {
		WS4JConfiguration.getInstance().setMFS(true);
		for ( RelatednessCalculator rc : rcs ) {
			double s = rc.calcRelatednessOfWords(word1, word2);
			System.out.println( rc.getClass().getName()+"\t"+s );
		}
	}
	
	
	public static void calSentence(String sent1, String sent2){
		String[] sentWords1 = sent1.split("[\\s\\-]+");
		sentWords1 = proterStermmer.stemWords(sentWords1);
		String[] sentWords2 = sent2.split("[\\s\\-]+");
		sentWords2 = proterStermmer.stemWords(sentWords2);
		WS4JConfiguration.getInstance().setMFS(true);
		for ( RelatednessCalculator rc : rcs ) {
			double[][] s = rc.getSimilarityMatrix(sentWords1, sentWords2);
			System.out.println( rc.getClass().getName()+"\t"+avg(s) );
		}
	}
	
	
	
	public static double sum(double[][] s){
		double sum = 0;
		for(double[] r:s){
			for(double cell:r){
				//System.out.println(cell);
				if(cell>0&&!Double.isInfinite(cell)) sum+=cell/(s.length*s[0].length);
			}
		}
		return sum;
	}
	
	public static double avg(double[][] s){
		double sum = sum(s);
		System.out.println("s.length="+s.length*s[0].length+" "+sum);
		return sum/(s.length*s[0].length);
	}
	
	public static void test(){
		ILexicalDatabase db = new NictWordNet();
		WS4JConfiguration.getInstance().setMFS(true);
		RelatednessCalculator rc = new Lin(db);
		String word1 = "gender";
		String word2 = "sex";
		List<POS[]> posPairs = rc.getPOSPairs();
		double maxScore = -1D;

		for(POS[] posPair: posPairs) {
		    List<Concept> synsets1 = (List<Concept>)db.getAllConcepts(word1, posPair[0].toString());
		    List<Concept> synsets2 = (List<Concept>)db.getAllConcepts(word2, posPair[1].toString());

		    for(Concept synset1: synsets1) {
		        for (Concept synset2: synsets2) {
		            Relatedness relatedness = rc.calcRelatednessOfSynset(synset1, synset2);
		            double score = relatedness.getScore();
		            if (score > maxScore) { 
		                maxScore = score;
		            }
		        }
		    }
		}

		if (maxScore == -1D) {
		    maxScore = 0.0;
		}

		System.out.println("sim('" + word1 + "', '" + word2 + "') =  " + maxScore);
	}
	
	
	public static void main(String[] args) {
		
		//WS4JConfiguration ws4jConf = WS4JConfiguration.getInstance();
		
		//test();
		long t0 = System.currentTimeMillis();
		calSentence( "food","food handler" );
		long t1 = System.currentTimeMillis();
		System.out.println( "Done in "+(t1-t0)+" msec." );
	}
}
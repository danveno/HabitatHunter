package be.bagofwords.brown;

import ir.ac.iust.nlp.wordclustering.ApplyInCoNLL;

public class WordClusterUtil {
	
	public static void main(String[] args){
		String input = "F:/Habitat/BacteriaBiotope/resources/BB3/stanford-parser/train/BioNLP-ST-2016_BB-cat_train/BB-cat-448557.txt.conllx";//input conll file
		String output = "F:/Habitat/BacteriaBiotope/BB-cat-448557.txt.conllx.wc";//output conll file
		int clusterCount = 50;
		int prefixLength = -1;
		boolean word = true;//(word == true ? "Word" : "Lemm")
		System.out.println("\tUsing " + (word == true ? "Word" : "Lemm"));
          System.out.println("\tCluster Count: " + clusterCount);
          System.out.println("\tPrefix Length: " + prefixLength);
		
		 try {
			ApplyInCoNLL.Start(input, output, clusterCount, prefixLength, word);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

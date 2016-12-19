package be.bagofwords.brown;

import be.bagofwords.ui.UI;

import java.io.IOException;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 11/12/14.
 */
public class Main {

    public static void main(String[] args) throws IOException {
    	/*
    	 * BrownClusterFile to get the input of the file
    	 */
        String inputFile = "F:\\Habitat\\BacteriaBiotope\\resources\\browncluster\\browncluster_13trdete_input.txt";
       
        int minFrequencyOfPhrase = 1;
       
        boolean onlySwapMostFrequentWords = true;
        long start = System.currentTimeMillis();
        
        for(int maxNumberOfClusters = 110;maxNumberOfClusters<=300;maxNumberOfClusters=maxNumberOfClusters+10){
	        String outputFile = "F:\\Habitat\\BacteriaBiotope\\resources\\browncluster\\13trdete_"+maxNumberOfClusters+".txt";
	        new BrownClustering(inputFile, outputFile, minFrequencyOfPhrase, maxNumberOfClusters, onlySwapMostFrequentWords).run();
        }
        long end = System.currentTimeMillis();
        UI.write("Took " + (end - start) + " ms.");
    }
}

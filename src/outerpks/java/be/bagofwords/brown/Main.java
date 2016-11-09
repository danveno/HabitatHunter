package be.bagofwords.brown;

import be.bagofwords.ui.UI;

import java.io.IOException;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 11/12/14.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        String inputFile = "F:\\Habitat\\BacteriaBiotope\\resources\\browncluster\\browncluster_bb16data_input.txt";
        String outputFile = "F:\\Habitat\\BacteriaBiotope\\resources\\browncluster\\browncluster_bb16data_150c.txt";
        int minFrequencyOfPhrase = 1;
        int maxNumberOfClusters = 150;
        boolean onlySwapMostFrequentWords = true;
        long start = System.currentTimeMillis();
        new BrownClustering(inputFile, outputFile, minFrequencyOfPhrase, maxNumberOfClusters, onlySwapMostFrequentWords).run();
        long end = System.currentTimeMillis();
        UI.write("Took " + (end - start) + " ms.");
    }
}

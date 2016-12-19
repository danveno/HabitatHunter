package au.com.nicta.csp.brateval;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 
 * BRAT stand-off entity comparison
 * 
 * @author Antonio Jimeno Yepes (antonio.jimeno@nicta.com.au)
 *
 */
public class CompareEntities {
	
	/**
	 * folder1 ---test results
	 * folder2 ---goldstandard
	 * exact_match----true or false 
	 * @param argc
	 * @throws IOException
	 */
	public static void main(String argc[]) throws IOException {
		String folder1 = argc[0];
		String folder2 = argc[1];
		boolean exact_match = Boolean.parseBoolean(argc[2]);
		//boolean exact_match = true;

		// folder1 test results
		//String folder1 = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\entity5";
		
		// folder2 goldstandard
		
		//String folder2 = "F:\\Habitat\\BacteriaBiotope\\2016\\BioNLP-ST-2016_BB-cat+ner_dev\\";
		// boolean exact_match = false;

		Map<String, Integer> entityTP = new TreeMap<String, Integer>();
		Map<String, Integer> entityFP = new TreeMap<String, Integer>();
		Map<String, Integer> entityFN = new TreeMap<String, Integer>();

		Set<String> entityTypes = new TreeSet<String>();
		Set<String> excludedTypes = new TreeSet<String>();
		excludedTypes.add("Title");
		excludedTypes.add("Paragraph");
		File folder = new File(folder1);

		for (File file : folder.listFiles()) {
			if (file.getName().endsWith(".a1"))// original file is ann
			{
				Document d1 = Annotations.read(file.getAbsolutePath(), "a1");
				Document d2 = Annotations.read(folder2 + File.separator + file.getName(), "a1");

				for (Entity e : d1.getEntities()) {//d1 is the predictions
					if(excludedTypes.contains(e.getType())) continue;
					entityTypes.add(e.getType());

					Entity match = null; //find the matched entity from the gold standard

					if (exact_match) {
						match = d2.findEntity(e);
					} else {
						match = d2.findEntityOverlapC(e);
					}

					// if (d2.findEntityOverlapC(e) != null)
					if (match != null) {//correct
						if (entityTP.get(e.getType()) == null) {
							entityTP.put(e.getType(), 1);
						} else {
							entityTP.put(e.getType(),
									entityTP.get(e.getType()) + 1);
						}
						
//						System.out.println("TP: " + e + " \t\t("
//								+ file.getName() + ")");
						
					} else {//false prediction
						if (entityFP.get(e.getType()) == null) {
							entityFP.put(e.getType(), 1);
						} else {
							entityFP.put(e.getType(),
									entityFP.get(e.getType()) + 1);
						}
						//System.out.println("FP: " + e + " \t\t("
						//		+ file.getName() + ")");
					}
				}

				for (Entity e : d2.getEntities()) {
					if(excludedTypes.contains(e.getType())) continue;
					entityTypes.add(e.getType());

					Entity match = null;

					if (exact_match) {
						match = d1.findEntity(e);
					} else {
						match = d1.findEntityOverlapC(e);
					}

					if (match == null) {
						if (entityFN.get(e.getType()) == null) {
							entityFN.put(e.getType(), 1);
						} else {
							entityFN.put(e.getType(),
									entityFN.get(e.getType()) + 1);
						}

//						System.out.println("FN: " + e + " \t\t("
//								+ file.getName() + ")");
					}
				}
			}
		}

		//System.out.println("");
		//System.out.println("Summary");
		//System.out.println("\tTP" + "\tFP" + "\tFN" + "\tPrecision"
		//		+ "\tRecall" + "\tF1");

		int allTP = 0;
		int allFP = 0;
		int allFN = 0;
		// Compute Precision, Recall, F1 for each entity type
		for (String et : entityTypes) {
			int TP = (entityTP.get(et) == null ? 0 : entityTP.get(et));
			int FP = (entityFP.get(et) == null ? 0 : entityFP.get(et));
			int FN = (entityFN.get(et) == null ? 0 : entityFN.get(et));

			double precision = 0;
			double recall = 0;
			double f_measure = 0;

			allTP += TP;
			allFP += FP;
			allFN += FN;

			if (TP + FP > 0) {
				precision = (double) TP / (TP + FP);
			}

			if (TP + FN > 0) {
				recall = (double) TP / (TP + FN);
			}

			if ((precision + recall) > 0) {
				f_measure = (2 * precision * recall)
						/ (double) (precision + recall);
			}

			System.out.print("\t"+et + "\t" + TP + "\t" + FP + "\t" + FN + "\t"
					+ String.format("%1.4f", precision) + "\t"
					+ String.format("%1.4f", recall) + "\t"
					+ String.format("%1.4f", f_measure));
		}
		// Compute overall precision, recall, F1 (micro-average)
		double precision = 0;
		double recall = 0;
		double f_measure = 0;

		if (allTP + allFP > 0) {
			precision = (double) allTP / (allTP + allFP);
		}

		if (allTP + allFN > 0) {
			recall = (double) allTP / (allTP + allFN);
		}

		if ((precision + recall) > 0) {
			f_measure = (2 * precision * recall)
					/ (double) (precision + recall);
		}

		System.out.println("\tOverall" + "\t" + allTP + "\t" + allFP + "\t"
				+ allFN + "\t" + String.format("%1.4f", precision) + "\t"
				+ String.format("%1.4f", recall) + "\t"
				+ String.format("%1.4f", f_measure));

	}
}
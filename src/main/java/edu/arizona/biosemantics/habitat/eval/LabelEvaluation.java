package edu.arizona.biosemantics.habitat.eval;

import java.util.ArrayList;
import java.util.List;

import edu.arizona.biosemantics.habitat.io.FileUtil;


/**
 * different from conlleval.pl 
 * evaluate the label sequence
 * input format : BIO
 * The last two columns are the labels. 
 * The last one is assigned label sequence and the one before the last is gold standard label sequence.
 * 
 * @author maojin
 *
 */
public class LabelEvaluation {
	private String[] labels;
	private double[] correct;
	private double[] gsdNum;
	private double[] asnNum;
	private double gsdPhraseNum;
	
	
	
	public void evaluate(String conllFile){
		List<String> lines = FileUtil.readLineFromFile(conllFile);
		labels = extLabels(lines);
		correct = new double[labels.length];
		gsdNum = new double[labels.length];
		asnNum = new double[labels.length];
		
		for(String label:labels) System.out.println(label);
		
		for(String line: lines){
			String[] fields = line.split("[\\s+]");
			String secLabel = fields[fields.length-2];//the last but one
			if(secLabel.startsWith("B")) gsdPhraseNum++;
			if(secLabel.indexOf("-")>-1) secLabel = secLabel.substring(secLabel.indexOf("-")+1, secLabel.length());
			
			int gsdlabelIndex = locateLabel(labels,secLabel);
			gsdNum[gsdlabelIndex]++;
			
			String lastLabel = fields[fields.length-1];//the last
			if(lastLabel.indexOf("-")>-1) lastLabel = lastLabel.substring(lastLabel.indexOf("-")+1, lastLabel.length());
			int asgLabelIndex = locateLabel(labels,lastLabel);
			asnNum[asgLabelIndex]++;
			
			if(secLabel.equals(lastLabel)) correct[gsdlabelIndex]++;
		}
		
		
		System.out.println("Phrases:"+gsdPhraseNum);
		for(int i=0;i<labels.length;i++){
			if(!"O".equals(labels[i])){
				double precision = correct[i]/asnNum[i];
				double recall = correct[i]/gsdNum[i];
				double f1 = 2*(precision*recall)/(precision+recall);
				System.out.println(labels[i]+": precision:"+precision+" recall:"+recall+" FB1:"+f1+" "+correct[i]);
			}
		}
	}
	
	
	public int locateLabel(String[] labels, String secLabel) {
		for(int i=0;i<labels.length;i++){
			if(labels[i].equals(secLabel)) return i;
		}
		return -1;
	}


	/**
	 * get all the labels from the last field but one
	 * @param lines
	 * @return
	 */
	public String[] extLabels(List<String> lines){
		List<String> labels = new ArrayList();
		for(String line: lines){
			String[] fields = line.split("[\\s+]");
			String label = fields[fields.length-2];//the last but one
			if(label.indexOf("-")>-1) label = label.substring(label.indexOf("-")+1, label.length());
			if(!labels.contains(label)) labels.add(label);
		}
		String[] labelsArr = new String[labels.size()];
		return labels.toArray(labelsArr);
	}
	
	
	public static void main(String[] args){
		LabelEvaluation labelEval = new LabelEvaluation();
		String conllFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\BioNLP-ST-2016_BB-cat_dev_12f_clf.conll";
		//String conllFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\all\\BioNLP-ST-2016_BB-cat+ner_dev.conll";
		labelEval.evaluate(conllFile);
	}
}

package edu.arizona.biosemantics.habitat.ner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import edu.arizona.biosemantics.bb.BBEntity;
import edu.arizona.biosemantics.bb.Config;
import edu.arizona.biosemantics.discourse.Token;
import edu.arizona.biosemantics.habitat.io.A1FormatFileUtil;
import edu.arizona.biosemantics.habitat.io.FileUtil;

/**
 * 
 * @author maojin
 *
 */
public class MalletResultProcessor {
	
	public A1FormatFileUtil a1Util = new A1FormatFileUtil();
	
	/**
	 * generate input file for colleval.pl
	 * 
	 * BIO format
	 * 
	 * GSDFild format:
	 * token, ,...., label
	 * 
	 * Prediction File format:
	 * label
	 * 
	 * Generated file format:
	 * token, gsdLabel, predict Label
	 * 
	 * The blank line can be removed
	 * 
	 * @param gsdFile
	 * @param predictionFile
	 * @param conllevalFile
	 */
	public void getCollEvalFile(String gsdFile, String predictionFile, String conllevalFile){
		//List<String> gsdLines = FileUtil.readLineFromFile(gsdFile);
		List<String> gsdLines = FileUtil.readNonEmptyLineFromFile(gsdFile);
		//List<String> predLines = FileUtil.readLineFromFile(predictionFile);
		List<String> predLines = FileUtil.readNonEmptyLineFromFile(predictionFile);
		try{
			FileWriter fw = new FileWriter(conllevalFile);
			for(int lineNum=0;lineNum<gsdLines.size();lineNum++){
				String line = gsdLines.get(lineNum);
				String[] gsdFields = line.split("[\\s\t]+");
				String[] predFields = predLines.get(lineNum).split("[\\s\t]+");
				if(gsdFields.length<2){
					fw.write("\n");
				}else{
					String token = gsdFields[0];
					//String pos = gsdFields[3];
					//String[] gsd = gsdFields[gsdFields.length-1].split("\\-");
					String[] prediction = predFields[0].trim().split("[\\s\t]+");
					//the correct BIO format for colleval.pl
					fw.write(token+" "+gsdFields[gsdFields.length-1]+" "+prediction[prediction.length-1]+"\n");
					//fw.write(token+" "+gsdFields[gsdFields.length-1]+" "+predFields[0]+"\n");
					//fw.write(token+" "+pos+" "+gsdFields[gsdFields.length-1]+" "+predFields[0]+"\n");
					//fw.write(token+" "+pos+" B-"+gsd[gsd.length-1]+" B-"+prediction[prediction.length-1]+"\n");
				}
			}
			fw.flush();
			fw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public void getCollEvalFileFromSVM(String gsdFile, String predictionFile, String conllevalFile){
		//List<String> gsdLines = FileUtil.readLineFromFile(gsdFile);
		List<String> gsdLines = FileUtil.readNonEmptyLineFromFile(gsdFile);
		//List<String> predLines = FileUtil.readLineFromFile(predictionFile);
		List<String> predLines = FileUtil.readNonEmptyLineFromFile(predictionFile);
		String[] labels ={"Habitat","Bacteria","O"};
		try{
			FileWriter fw = new FileWriter(conllevalFile);
			String lastLabel = null;
			for(int lineNum=0;lineNum<gsdLines.size();lineNum++){
				String line = gsdLines.get(lineNum);
				String[] gsdFields = line.split(" ");
				String[] predFields = predLines.get(lineNum).split(" ");
				if(gsdFields.length<2){
					fw.write("\n");
				}else{
					String token = gsdFields[0];
					String pos = gsdFields[3];
					int classIndex = Integer.parseInt(predFields[0]);
					String currentLabel = labels[classIndex-1];
					//FileUtil.writeStr(fileName, label+"\n", true);
					String outputlabel = null;
					if(currentLabel.equals("Habitat")&&"Habitat".equals(lastLabel)){
						outputlabel = "I-Habitat";//I-Habitat
					}else if(currentLabel.equals("Habitat")&&!"Habitat".equals(lastLabel)){
						outputlabel = "B-Habitat";//currentLabel
					}if(currentLabel.equals("Bacteria")&&"Bacteria".equals(lastLabel)){
						outputlabel = "I-Bacteria";//I-Bacteria
					}else if(currentLabel.equals("Bacteria")&&!"Bacteria".equals(lastLabel)){
						outputlabel = "B-Bacteria";//B-Bacteria
					}else if(currentLabel.equals("O")){
						outputlabel = "O";//"O"
					}
					lastLabel = currentLabel;
					
					fw.write(token+" "+gsdFields[gsdFields.length-1]+" "+outputlabel+"\n");
					//fw.write(token+" "+pos+" "+gsdFields[gsdFields.length-1]+" "+predFields[0]+"\n");
					//fw.write(token+" "+pos+" B-"+gsd[gsd.length-1]+" B-"+prediction[prediction.length-1]+"\n");
				}
			}
			fw.flush();
			fw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 1, result file---the first field is the label of the token
	 * 2, read the token and offset
	 * 
	 */
	public void genNERA1File(String datasetName, String predictionFile, String lineFile, String outputFolder){
		MalletInputGenerator inputGen = new MalletInputGenerator();
		List<Token> allTokenSeqs = inputGen.readTokenSequences(datasetName);
		
		List<String> predLines = FileUtil.readLineFromFile(predictionFile);
		
		List<String> lineInfo = FileUtil.readLineFromFile(lineFile);
		
		//System.out.println("allTokenSeqs size = "+allTokenSeqs.size());
		//System.out.println("predLines size = "+predLines.size());
		String[] fileNames = new String[lineInfo.size()];
		Integer[] fileStartLine = new Integer[lineInfo.size()];
		for(int i=0;i<fileNames.length;i++){
			String line = lineInfo.get(i);
			String[] fields = line.split(" ");
			fileNames[i] = fields[0];
			fileStartLine[i] = new Integer(fields[1]);
		}
		
		for(int i=0;i<fileNames.length;i++){
			String fileName = fileNames[i];
			int startLine = fileStartLine[i];
			int endLine = 0;
			if(i<fileNames.length-1){
				endLine = fileStartLine[i+1]-1;
			}else{
				endLine = allTokenSeqs.size()-1;
			}
			
			List<BBEntity> entities = new ArrayList();
			
			//read title and paragaraph
			entities.addAll(FileUtil.readTitleAndParasFromFile(Config.txtFolder+"/"+fileName+".txt"));
			
			BBEntity entity = new BBEntity();
			entities.add(entity);
			for(int line = startLine-1;line<endLine; line++ ){
				Token token = allTokenSeqs.get(line);
				
				if(predLines.get(line).indexOf("Habitat")>-1){//habitat
					if(entity.getName()==null){//a new one
						entity.setName(token.getText());
						entity.setStart(token.getOffset());
						entity.setEnd(token.getOffend());
						entity.setType("Habitat");
					}else if(entity.getName()!=null&&entity.getType().equals("Habitat")){
						if(token.getOffset()-entity.getEnd()==0){
							entity.setName(entity.getName()+""+token.getText());
						}else{
							entity.setName(entity.getName()+" "+token.getText());
						}
						entity.setEnd(token.getOffend());
					}else{//the former is bacteria, add to the list and create a new one
						entity = new BBEntity();
						entities.add(entity);
						entity.setName(token.getText());
						entity.setStart(token.getOffset());
						entity.setEnd(token.getOffend());
						entity.setType("Habitat");
					}
				}else if(predLines.get(line).indexOf("Bacteria")>-1){//Bacteria
					if(entity.getName()==null){//a new one
						entity.setName(token.getText());
						entity.setStart(token.getOffset());
						entity.setEnd(token.getOffend());
						entity.setType("Bacteria");
					}else if(entity.getName()!=null&&entity.getType().equals("Bacteria")){
						if(token.getOffset()-entity.getEnd()==0){
							entity.setName(entity.getName()+""+token.getText());
						}else{
							entity.setName(entity.getName()+" "+token.getText());
						}
						entity.setEnd(token.getOffend());
					}else{//the former is bacteria, add to the list and create a new one
						entity = new BBEntity();
						entities.add(entity);
						entity.setName(token.getText());
						entity.setStart(token.getOffset());
						entity.setEnd(token.getOffend());
						entity.setType("Bacteria");
					}
				}else if(predLines.get(line).trim().equals("O")||predLines.get(line).trim().equals("")){
					entity = new BBEntity();
					entities.add(entity);
				}
			}//
			
			
			a1Util.write(outputFolder, fileName, entities);
			
		}//all files end
	}
	
	public static void main(String[] args){
		
		String featureGroup = "wapiti";//all,groin,orth, orth_morph,orth_syn
		//--orders 1,2,3
		String[] trainArgs=("--train true  --model-file F:/Habitat/BacteriaBiotope/experiments/CRFinputs/"+featureGroup+"/BioNLP-ST-2016_BB-event+ner_train.model  F:/Habitat/BacteriaBiotope/experiments/CRFinputs/"+featureGroup+"/BioNLP-ST-2016_BB-event+ner_train.txt").split("[\\s]+");
		String[] predictArgs=("--model-file F:/Habitat/BacteriaBiotope/experiments/CRFinputs/"+featureGroup+"/BioNLP-ST-2016_BB-event+ner_train.model  --rs-file F:/Habitat/BacteriaBiotope/experiments/CRFinputs/"+featureGroup+"/BioNLP-ST-2016_BB-event+ner_dev.rs F:/Habitat/BacteriaBiotope/experiments/CRFinputs/"+featureGroup+"/BioNLP-ST-2016_BB-event+ner_dev.txt").split("[\\s]+");
		try {
			//SimpleTagger.main(trainArgs);
			System.out.println("\n\n\n\n\n\n\n\n");
			//SimpleTagger.main(predictArgs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		/* */
		String gsdFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\"+featureGroup+"\\BioNLP-ST-2016_BB-event+ner_dev.gsd";
		//String rsFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\BioNLP-ST-2016_BB-cat_dev_12f_rs.txt";
		//String conllEvalFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\BioNLP-ST-2016_BB-cat_dev_12f.conll";
		
		
		//String rsFile ="F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\"+featureGroup+"\\BioNLP-ST-2016_BB-cat+ner_dev_all.wptrs";
		String rsFile ="F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\"+featureGroup+"\\BioNLP-ST-2016_BB-event+ner_dev_1316.rs";
		String conllEvalFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\"+featureGroup+"\\BioNLP-ST-2016_BB-event+ner_dev_1316.1316conll";
		
		MalletResultProcessor cefGen = new MalletResultProcessor();
		cefGen.getCollEvalFile(gsdFile,rsFile, conllEvalFile);
		//cefGen.genNERA1File(datasetName, predictionFile, lineFile, outputFolder);
		//Perl conlleval.pl < wapiti/BioNLP-ST-2016_BB-event+ner_dev.conll
		
		 try {
			 System.out.println("perl F:/Habitat/BacteriaBiotope/experiments/CRFinputs/conlleval.pl < "+conllEvalFile);
			 Process proc = Runtime.getRuntime().exec("perl F:/Habitat/BacteriaBiotope/experiments/CRFinputs/conlleval.pl < "+conllEvalFile);
			 proc.waitFor();
			 BufferedReader is = new BufferedReader
			            ( new InputStreamReader(proc.getInputStream()));
			        String sLine;
			        while ((sLine = is.readLine()) != null) {
			            System.out.println(sLine);
			        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
			 
		
		// SVM calling
		/*
		String rsFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\sqresults\\BioNLP-ST-2016_BB-cat+ner_dev.svmstrs";
		String gsdFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\CRFinputs\\all\\BioNLP-ST-2016_BB-cat+ner_dev.gsd";
		String conllEvalFile="F:\\Habitat\\BacteriaBiotope\\experiments\\classification\\eval\\BioNLP-ST-2016_BB-cat+ner_dev.svmmsconll";
		MalletResultProcessor cefGen = new MalletResultProcessor();
		cefGen.getCollEvalFileFromSVM(gsdFile,rsFile, conllEvalFile);
		 */
		//String lineFile = "F:\\Habitat\\BacteriaBiotope\\experiments\\BioNLP-ST-2016_BB-cat_dev_line.txt";
		//String outputFolder = "F:\\Habitat\\BacteriaBiotope\\experiments\\results";
		
		//cefGen.genNERA1File("BioNLP-ST-2016_BB-cat_dev", rsFile, lineFile, outputFolder);
		 
		 
	}
}

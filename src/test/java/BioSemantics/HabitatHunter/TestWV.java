package BioSemantics.HabitatHunter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.nlp.vec.VectorModel;

public class TestWV {

	public static void main(String[] args){
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream("F:/dataset/pubmed/PubMed-and-PMC-w2v.bin")));
			
			String line = br.readLine();
			
			System.out.println(line);
			line = br.readLine();
			
			System.out.println(line);
			line = br.readLine();
			
			System.out.println(line);
line = br.readLine();
			
			System.out.println(line);
			br.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

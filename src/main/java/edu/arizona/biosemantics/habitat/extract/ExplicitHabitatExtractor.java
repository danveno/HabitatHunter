package edu.arizona.biosemantics.habitat.extract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import edu.arizona.biosemantics.discourse.Sentence;
import edu.arizona.biosemantics.habitat.sentclf.ExplicitHabClassifier;


/**
 * explicit habitat extractor
 * used to extract habitat information from the sentences like this: " habitat: oak forest; Record Level: institutionCode: ZMUN "
 * @author maojin
 *
 */
public class ExplicitHabitatExtractor implements IHabitatExtractor{
	
	private ExplicitHabClassifier habClassifer;
	
	public ExplicitHabitatExtractor(ExplicitHabClassifier habClassifer){
		this.habClassifer = habClassifer;
	}
	
	
	/**
	 * extract habitat from a value pair, e.g., "habitat: birch forest"
	 * @return
	 */
	public String extractFromPair(String valuePair){
		String[] pairs = valuePair.split(":");
		if(pairs.length>1){
			return pairs[1];
		}else{
			return null;
		}
	}


	/**
	 * extract all the habitat information from the explicit statement
	 * 3. if there is at least one semi-comma, split to find the right habitat
	 * 4. if there is no semi-comma, split with the colon.
	 */
	public List extract(Sentence sent) {
		
		List habList = null;
		if(habClassifer.isPositive(sent)){
			habList = new ArrayList();
			// 3. if there is at least one semi-comma, split to find the right habitat
			// 4. if there is no semi-comma, split with the colon.
			String[] fields = sent.getText().split(";");
			if(fields.length>1){
				for(String field : fields){
					if(field.indexOf("habitat")>-1||field.indexOf("Habitat")>-1){
						String habitat = this.extractFromPair(field);
						habList.add(habitat);
					}
				}
			}else{
				String habitat = this.extractFromPair(sent.getText());
				habList.add(habitat);
			}
		}
		return habList;
	}
	
	
	
}

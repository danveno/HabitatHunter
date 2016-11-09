package edu.arizona.biosemantics.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimilarityUtil {
	private static Set<String> stopset = new HashSet();
	static{
		stopset.add("with");
		stopset.add("the");
		stopset.add("in");
		stopset.add("of");
	}
	
	/**
	 * calculate the Jaccard similarity between the two items
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static double jaccard(String str1, String str2){
		String[] str1Fields = str1.split("[\\s\\-]+");
		String[] str2Fields = str2.split("[\\s\\-]+");
		
		List<String> str1Words = new ArrayList();
		for(int i=0;i<str1Fields.length;i++){
			if(!stopset.contains(str1Fields[i]))	
				str1Words.add(str1Fields[i]);
		}
		List<String> str2Words = new ArrayList();
		for(int i=0;i<str2Fields.length;i++){
			if(!stopset.contains(str2Fields[i]))	
				str2Words.add(str2Fields[i]);
		}
		
		double overlap = 0;
		for(int i=0;i<str1Words.size();i++){
			for(int j=0;j<str2Words.size();j++){
				if(str1Words.get(i).equals(str2Words.get(j))) overlap++;
			}
		}
		
		return overlap/(str1Words.size()+str2Words.size()-overlap);
	}
	
	
	/**
	 * calculate the Jaccard similarity between the two items
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static double jaccardLemma(String str1, String str2){
		String[] str1Fields = str1.split("[\\s]+");
		String[] str2Fields = str2.split("[\\s]+");
		
		List<String> str1Words = new ArrayList();
		for(int i=0;i<str1Fields.length;i++){
			if(!stopset.contains(str1Fields[i]))	
				str1Words.add(str1Fields[i]);
		}
		List<String> str2Words = new ArrayList();
		for(int i=0;i<str2Fields.length;i++){
			if(!stopset.contains(str2Fields[i]))	
				str2Words.add(str2Fields[i]);
		}
		
		double overlap = 0;
		for(int i=0;i<str1Words.size();i++){
			for(int j=0;j<str2Words.size();j++){
				if(EditDistance.computeLevenshteinDistance(str1Words.get(i), str2Words.get(j))<=1&&str1Words.get(i).length()>4) overlap++;
			}
		}
		
		return overlap/(str1Words.size()+str2Words.size()-overlap);
		///(str1Words.size()+str2Words.size()-overlap);
	}

	
}

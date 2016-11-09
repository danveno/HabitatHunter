package edu.arizona.biosemantics.habitat.term;

/**
 * about the taxon name
 * 
 * @author maojin
 *
 */
public class TaxonNameTool {
	
	/**
	 * 1, whether it contains
	 * 2, find the genus and species name
	 * 3, if no species name, only reture genus name
	 * 
	 * @param name
	 * @return
	 */
	public static String getGenusAndSpeciesName(String name){
		char firstChar = name.charAt(0);
		if(firstChar>='A'&&firstChar<='Z'){//Genus name often capitalized
			StringBuffer gsName = new StringBuffer();
			String[] fields = name.split("[\\s\\-]+");
			gsName.append(fields[0]);//genus name
			if(fields.length>1){
				gsName.append(" ");
				gsName.append(fields[1]);
			}
			return gsName.toString();
		}
		return name;
	}
	
	/**
	 * sp., spp., gen. nov., sp.nov.
	 * @param name
	 * @return
	 */
	public static String replaceNonmolSuffixes(String name){
		String[] nomSuffixes = {"sp."," sp", "spp."," spp"," gen","gen."," nov","nov."," sp.nov","sp.nov."};
		name = name.trim();
		for(String suff: nomSuffixes){
			if(name.endsWith(suff)){
				return name.replace(suff, "").trim();
			}
		}
		return name;
	}
}

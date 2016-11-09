package edu.arizona.biosemantics.nlp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.arizona.biosemantics.bb.BBEntity;
import edu.arizona.biosemantics.discourse.Phrase;
import edu.arizona.biosemantics.habitat.term.AcronymDetector;
import edu.arizona.biosemantics.util.StringUtil;

/**
 * identify the phrase relations: acronym-long form, or anaphor
 * @author maojin
 *
 */
public class PhraseRelationExtractor {
	
	private AcronymDetector acronymDetector = new AcronymDetector();
	
	private Set stopAcrSet = new HashSet();
	{
		stopAcrSet.add("It");
		stopAcrSet.add("We");
		stopAcrSet.add("There");
		stopAcrSet.add("Food");
	}
	/**
	 * identify acronym pair
	 * @param phraseList
	 * @return
	 */
	public Map<String, String> identifyAcronymPair(List<Phrase> phraseList){
		Map<String, String> acronymMap = new HashMap();
		//Map<Phrase, Phrase> acronymMap = new HashMap();
		for(int i=0;i<phraseList.size();i++){
			Phrase ph = phraseList.get(i);
			if(acronymDetector.isAcronym(ph.getText())){//if it's an acronym
				//find from ahead entities
				for(int j=0;j<i;j++){
					Phrase longPhCand = phraseList.get(j);
					String longform = acronymDetector.findBestLongForm(ph.getText(), longPhCand.getText());
					if(longform!=null&&!ph.getText().equals(longform)){
						acronymMap.put(ph.getText(), longform);
						break;
					}
				}
				
			}
		}
		return acronymMap;
	}
	
	/**
	 * identify acronym pair
	 * @param phraseList
	 * @return
	 */
	public Map<String, Phrase> identifyAcronymPhrasePair(List<Phrase> phraseList){
		Map<String, Phrase> acronymMap = new HashMap();
		//Map<Phrase, Phrase> acronymMap = new HashMap();
		for(int i=0;i<phraseList.size();i++){
			Phrase ph = phraseList.get(i);
			if(acronymDetector.isAcronym(ph.getText())){//if it's an acronym
				//find from ahead entities
				for(int j=0;j<i;j++){
					Phrase longPhCand = phraseList.get(j);
					String longform = acronymDetector.findBestLongForm(ph.getText(), longPhCand.getText());
					if(longform!=null&&!ph.getText().equals(longform)){
						acronymMap.put(ph.getText(), longPhCand);
						break;
					}
				}
				
			}
		}
		return acronymMap;
	}
	
	
	/**
	 * find substring anaphor expressions
	 * @param phraseList
	 * @return
	 */
	public Map<String, String> findSubstringAnaphor(List<Phrase> phraseList){
		Map<String, String> substringAnaphorMap = new HashMap();
		//Map<Phrase, Phrase> acronymMap = new HashMap();
		for(int i=0;i<phraseList.size();i++){
			Phrase ph = phraseList.get(i);
			if(isAnaphorCand(ph.getText())){//if it's an anaphor
				//find from ahead entities
				for(int j=0;j<i;j++){
					Phrase longPhCand = phraseList.get(j);
					if(longPhCand.getText().indexOf(ph.getText())>-1&&!longPhCand.getText().equals(ph.getText())
							&&!"O".equals(longPhCand.getLabel())&&longPhCand.getLabel()!=null){
						substringAnaphorMap.put(ph.getText(), longPhCand.getText());
						break;
					}
				}
				
			}
		}
		return substringAnaphorMap;
	}
	
	/**
	 * find substring anaphor expressions
	 * @param phraseList
	 * @return
	 */
	public Map<String, Phrase> findSubstringAnaphorPhrase(List<Phrase> phraseList){
		Map<String, Phrase> substringAnaphorMap = new HashMap();
		//Map<Phrase, Phrase> acronymMap = new HashMap();
		for(int i=0;i<phraseList.size();i++){
			Phrase ph = phraseList.get(i);
			if(isAnaphorCand(ph.getText())){//if it's an anaphor
				//find from ahead entities
				for(int j=0;j<i;j++){
					Phrase longPhCand = phraseList.get(j);
					if(longPhCand.getText().indexOf(ph.getText())>-1&&!longPhCand.getText().equals(ph.getText())
							&&!"O".equals(longPhCand.getLabel())&&longPhCand.getLabel()!=null){
						substringAnaphorMap.put(ph.getText(), longPhCand);
						break;
					}
				}
				
			}
		}
		return substringAnaphorMap;
	}
	
	/**
	 * find substring anaphor expressions
	 * @param phraseList
	 * @return
	 */
	public BBEntity findSubstringAnaphorEntity(Phrase phrase, List<BBEntity> entityList){
		BBEntity anaphorEnitty = null;
		for(int i=0;i<entityList.size();i++){
			BBEntity entity = entityList.get(i);
			if(isAnaphorCand(phrase.getText())){//if it's an anaphor
				String text = StringUtil.removeSpecs(phrase.getText());
				text = StringUtil.removeNomenclaturals(text);
				if(text.toUpperCase().equals(text)){//abbreviations
					//text = text.toLowerCase();
					String entityName = entity.getName();//.toLowerCase();
					if((entityName.indexOf(text+" ")>-1)){//||entityName.indexOf(" "+text)>-1
						anaphorEnitty = entity;
						break;
					}
				}else{
					text = text.toLowerCase();
					String entityName = entity.getName().toLowerCase();
					if(text.length()>5&&(entityName.indexOf(text+" ")>-1)){//||entityName.indexOf(" "+text)>-1
						anaphorEnitty = entity;
						break;
					}
				}
			}
		}
		return anaphorEnitty;
	}
	
	
	public boolean isAnaphorCand(String str){
		if(StringUtil.hasDigit(str)||StringUtil.hasCapital(str)||hasSpeicialCharacters(str)){
			return true;
		}
		return false;
	}
	
	
	public boolean hasSpeicialCharacters(String content) {
		boolean flag = false;
		Pattern p = Pattern.compile(".*[\\+\\-\\(\\)].*");
		Matcher m = p.matcher(content);
		if (m.matches())
		flag = true;
		return flag;
	}
	
	
	public static void main(String[] args){
		PhraseRelationExtractor pre = new PhraseRelationExtractor();
		System.out.println(pre.hasSpeicialCharacters("Ara-"));
		System.out.println(pre.hasSpeicialCharacters("Ara+"));
		System.out.println(pre.hasSpeicialCharacters("(998)"));
	}
}

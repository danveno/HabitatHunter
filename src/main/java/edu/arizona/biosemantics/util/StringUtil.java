package edu.arizona.biosemantics.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.arizona.biosemantics.bb.Config;

public class StringUtil {
	/**
	 * check whether its a punctuation
	 * @param str
	 * @return
	 */
	public static boolean isPunctuation(String str){
		char c = str.charAt(0);
        if(str.length()==1&&(c == ','
            || c == '.'
            || c == '!'
            || c == '?'
            || c == ':'
            || c == ';'))	return true;
        return false;
	}
	
	public static boolean hasPunctuation(String str){
		Pattern p = Pattern.compile("\\p{Punct}");
		Matcher m = p.matcher(str);
		if (m.find())
			return true;
		return false;
	}
	
	public static String getFileName(String filePath){
		int lastSplashIndex = filePath.lastIndexOf("/");
		int firstDotIndex = filePath.indexOf(".");
		return filePath.substring(lastSplashIndex+1, firstDotIndex);
	}
	
	public static String replaceStanfordWilds(String str){
		str=str.replace("-LRB-","(");
		str=str.replace("-RRB-",")");
		str=str.replace("-LSB-","[");
		str=str.replace("-RSB-","]");
		str=str.replace("''","\"");
		str=str.replace("``","\"");
		return str;
	}
	
	public static boolean hasDigit(String content) {
		boolean flag = false;
		Pattern p = Pattern.compile(".*\\d+.*");
		Matcher m = p.matcher(content);
		if (m.matches())
		flag = true;
		return flag;
	}
	
	
	/**
	 * presents any digit
	 * @param str
	 * @return
	 */
	public int presentsDigit(String str){
		for(int i = 0;i < str.length();i++){
			if(Character.isDigit(str.charAt(i))){
				return 1;
			}
		}//
		return 0;
	}
	
	public static boolean hasCapital(String str) {
		for(int i = 0;i < str.length();i++){
			if(Character.isUpperCase(str.charAt(i))){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * separate white space, dot, hyphen
	 * @param str
	 * @return
	 */
	public static List<String> ruleTokenize(String str){
		List<String> tokens = new ArrayList();
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<str.length();i++){
			//if(str.charAt(i)=='-'||str.charAt(i)==' '||str.charAt(i)=='.'||str.charAt(i)=="'".charAt(0)||Character.isLetterOrDigit(ch)){
			if(!(str.charAt(i)==','||Character.isLetterOrDigit(str.charAt(i)))){
				String ntoken = sb.toString().trim();
				if(!ntoken.equals("")) tokens.add(ntoken);
				if(!Character.isWhitespace(str.charAt(i))) tokens.add(str.charAt(i)+"");
				 sb = new StringBuffer();
			}else{
				sb.append(str.charAt(i));
			}
		}
		String ntoken = sb.toString().trim();
		if(!ntoken.trim().equals("")) tokens.add(ntoken);
		return tokens;
	}
	
	/**
	 * Different typographic types
	 * replace all capitalized characters with A
	 * replace all lowercases with a
	 * shorten the string
	 * 
	 * 
	 * digits?
	 * O --- not characters
	 * @return
	 */
	public static String getTypographic(String str){
		char[] chars = str.toCharArray();
		for(int i = 0;i < chars.length;i++){
			if(Character.isUpperCase(chars[i])){
				chars[i]= 'A';
			}else if(Character.isLowerCase(chars[i])){
				chars[i]= 'a';
			}else if(Character.isDigit(chars[i])){
				chars[i]= '1';
			}
		}//
		char lastChar = chars[0];
		int length = 1;
		StringBuffer sb = new StringBuffer();
		sb.append(chars[0]);
		for(int i = 1;i < chars.length;){
			if(chars[i]==lastChar){
				if(length>2) i++;
				else{
					sb.append(chars[i]);
					length++;
					i++;
				}
			}else{
				sb.append(chars[i]);
				lastChar = chars[i];
				i++;
				length=1;
			}
			
		}
		return sb.toString();
	}
	
	
	public static String removeSpecs(String text) {
		for(String spec:Config.strainSpecs){
			if(text.startsWith(spec+" ")) text = text.replace(spec+" ", "");
		}
		return text;
	}
	
	public static String removeNomenclaturals(String text) {
		for(String nomen:Config.nomenclaturals){
			if(text.endsWith(" "+nomen)) text = text.replace(" "+nomen, "");
		}
		return text;
	}

	
	public static String matchShortTerm(String longTerm, String shortTerm){
	
		String patternString = "\\s"+shortTerm+"\\s|^"+shortTerm+"\\s|\\s"+shortTerm+"$|^"+shortTerm+"$"; // regular expression pattern
		try{
			Pattern pattern = Pattern.compile(patternString);
			Matcher matcher = pattern.matcher(longTerm);			
			if (matcher.find()) {
				return shortTerm;
			}
		}catch(Exception e){
			return null;
		}
		return null;
	}
	
	public static void main(String[] args){
		StringUtil strUtil = new StringUtil();
//		System.out.println(StringUtil.hasPunctuation("."));
//		System.out.println(StringUtil.hasPunctuation("a-b"));
//		System.out.println(StringUtil.hasPunctuation("a.b"));
//		System.out.println(StringUtil.hasDigit("12"));
//		System.out.println(StringUtil.hasDigit("a12"));
//		System.out.println(StringUtil.hasDigit("12a"));
//		System.out.println(StringUtil.hasDigit("b1a"));
//		System.out.println(StringUtil.hasDigit("ba"));
		
		System.out.println(StringUtil.getTypographic("ba"));
		System.out.println(StringUtil.getTypographic("Aba"));
		System.out.println(StringUtil.getTypographic("AAAA"));
		System.out.println(StringUtil.getTypographic("ABC"));
		System.out.println(StringUtil.getTypographic("AbC"));
		System.out.println(StringUtil.getTypographic("AbBC-1"));
		System.out.println(StringUtil.getTypographic("Horrible1234"));
		System.out.println(("M. sese").charAt(1));
		
	}

	
}

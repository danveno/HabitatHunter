package BioSemantics.HabitatHunter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.arizona.biosemantics.habitat.io.FileUtil;

public class RubusIdentify {
	public static void main(String[] args) {
		//findRubus();
		getRankInfoWithoutHierarchy();
	}
	
	
	
	/**
	 * get rank information if there are hierarchies
	 */
	public static void getRankInfoWithoutHierarchy(){
		File folderFile = new File("F:\\dataset\\Rubus\\FoCV9_Final2");
		File[] subFolders = folderFile.listFiles();
		System.out.println("contains rubus");
		Set<String> rankSet = new HashSet();
		try {
			FileWriter fw = new FileWriter("f:/rubuslist_fco.csv");
			fw.write("family,subfamily,tribe,genus,subgenus,section,species,subspecies,variety,forma,source folder, source file\n");
			for (File file : subFolders) {
				//File[] files = subFolder.listFiles();
				//for (File file : files) {
					List<String> lines = FileUtil.readLineFromFile(file);
					Map<String, String> rankMap = new HashMap();
					for (String line : lines) {
						line = line.toLowerCase();
						if (line.indexOf("taxon_name") > -1) {//hierachy
							System.out.println(line+" "+line.indexOf(">")+" "+line.indexOf("</taxon_name>"));
							String rankName = line.substring(line.indexOf("rank=")+6, line.indexOf("authority=")-2);
							String rankValue = line.substring(line.indexOf(">")+1, line.indexOf("</taxon_name>")).trim();
							System.out.println(rankName+"="+rankValue);
							rankMap.put(rankName.toLowerCase(), rankValue);
						}
						if (line.indexOf("</taxon_identification>") > -1) {//break
							//output
							String rsLine = formLine(rankMap, file.getPath());
							fw.write(rsLine);
							break;
						}
					}
				//}
			}
			fw.flush();
			fw.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * get rank information if there are hierarchies
	 */
	public static void getRankInfo(){
		File folderFile = new File("F:\\dataset\\FNATextProcessing_Rubus");
		File[] subFolders = folderFile.listFiles();
		System.out.println("contains rubus");
		Set<String> rankSet = new HashSet();
		try {
			FileWriter fw = new FileWriter("f:/rubuslist_fna.csv");
			fw.write("family,subfamily,tribe,genus,subgenus,section,species,subspecies,variety,forma,source folder, source file\n");
			for (File subFolder : subFolders) {
				File[] files = subFolder.listFiles();
				for (File file : files) {
					List<String> lines = FileUtil.readLineFromFile(file);
					for (String line : lines) {
						line = line.toLowerCase();
						if (line.indexOf("taxon_hierarchy") > -1) {
							
							String newLine = line.replace("<taxon_hierarchy>", "").replace("</taxon_hierarchy>", "");
							String[] ranks = newLine.split(";");
							Map<String, String> rankMap = new HashMap();
							for(String rank : ranks){
								System.out.println(rank);
								rank = rank.trim();
								String rankName = rank.substring(0, rank.indexOf(" "));
								rankSet.add(rankName.toLowerCase().trim());
								String rankValue = rank.substring(rank.indexOf(" ")).trim();
								rankMap.put(rankName, rankValue);
							}
							
							//output
							String rsLine = formLine(rankMap, file.getPath());
							fw.write(rsLine);
							break;
						}
					}
				}
			}
			fw.flush();
			fw.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	private static String formLine(Map<String, String> rankMap, String fileName) {
		StringBuffer sb = new StringBuffer();
		sb.append(getValue(rankMap, "family"));
		sb.append(",");
		sb.append(getValue(rankMap, "subfamily"));
		sb.append(",");
		sb.append(getValue(rankMap, "tribe"));
		sb.append(",");
		sb.append(getValue(rankMap, "genus"));
		sb.append(",");
		sb.append(getValue(rankMap, "subgenus"));
		sb.append(",");
		sb.append(getValue(rankMap, "section"));
		sb.append(",");
		sb.append(getValue(rankMap, "species"));
		sb.append(",");
		sb.append(getValue(rankMap, "subspecies"));
		sb.append(",");
		sb.append(getValue(rankMap, "variety"));
		sb.append(",");
		sb.append(getValue(rankMap, "forma"));
		sb.append(",");
		
		String newFilePath = fileName.replace("F:\\dataset\\FNATextProcessing_Rubus\\", "");
		System.out.println(newFilePath);
		sb.append(newFilePath.substring(0, newFilePath.indexOf("\\")));
		sb.append(",");
		sb.append(newFilePath.substring(newFilePath.indexOf("\\")+1));
		sb.append("\n");
		// TODO Auto-generated method stub
		return sb.toString();
	}

	
	private static String getValue(Map<String, String> rankMap, String rank) {
		String value = rankMap.get(rank);
		return value==null?"":value;
	}
	
	public static void findRubus(){
		File folderFile = new File("F:\\dataset\\FNATextProcessing");
		File[] subFolders = folderFile.listFiles();
		System.out.println("contains rubus");
		for (File subFolder : subFolders) {
			File[] files = subFolder.listFiles();
			for (File file : files) {
				List<String> lines = FileUtil.readLineFromFile(file);
				for (String line : lines) {
					line = line.toLowerCase();
					if (line.indexOf("taxon_name") > -1
							&& line.indexOf("genus") > -1
							&& line.indexOf("rubus") > -1) {
						String fileName = file.getPath();
						String newFilePath = fileName.replace("\\FNATextProcessing\\", "\\FNATextProcessing_Rubus\\");
						checkDir(newFilePath);
						Copy(fileName,newFilePath);
						break;
					}
				}
			}
		}
	}

	private static void checkDir(String newFilePath) {
		String subFolder = newFilePath.substring(0, newFilePath.lastIndexOf("\\"));
		File subFolderFile = new File(subFolder);
		if(!subFolderFile.exists()) subFolderFile.mkdirs();
	}

	public static void Copy(String oldPath, String newPath) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) {
				InputStream inStream = new FileInputStream(oldPath);
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				int length;
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread;
					//System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
			}
		} catch (Exception e) {
			System.out.println("error  ");
			e.printStackTrace();
		}
	}
}

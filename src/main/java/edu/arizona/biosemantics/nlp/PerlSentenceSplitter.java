package edu.arizona.biosemantics.nlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class PerlSentenceSplitter {

	public static void main(String args[]) {
		URL urlpath=PerlSentenceSplitter.class.getResource("/");  
        String path=urlpath.toString();  
        System.out.println(path);
        if(path.startsWith("file"))  
        {  
            path=path.substring(6);  
        }  
        path = path.replace("/", File.separator);
		String[] cmd = { "D:\\Strawberry\\perl\\bin\\perl.exe", path+"edu/arizona/biosemantics/nlp/testpl.pl", "param2" };
		StringBuffer resultStringBuffer = new StringBuffer();
		String lineToRead = "";
		// get Process to execute perl, get the output and exitValue
		int exitValue = 0;
		try {
			Process proc = Runtime.getRuntime().exec(cmd);
			
			InputStream inputStream = proc.getErrorStream();//proc.getInputStream();
			BufferedReader bufferedRreader = new BufferedReader(
					new InputStreamReader(inputStream));
			// save first line
			if ((lineToRead = bufferedRreader.readLine()) != null) {
				resultStringBuffer.append(lineToRead);
			}
			// save next lines
			while ((lineToRead = bufferedRreader.readLine()) != null) {
				resultStringBuffer.append("/r/n");
				resultStringBuffer.append(lineToRead);
			}
			// Always reading STDOUT first, then STDERR, exitValue last
			proc.waitFor(); // wait for reading STDOUT and STDERR over
			exitValue = proc.exitValue();
		} catch (Exception ex) {
			ex.printStackTrace();
			resultStringBuffer = new StringBuffer("");
			exitValue = 2;
		}
		System.out.println("exit:" + exitValue);
		System.out.println(resultStringBuffer.toString());
	}
}

package edu.arizona.biosemantics.habitat.feature;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import edu.arizona.biosemantics.habitat.io.FileUtil;


/**
 * http://swoogle.umbc.edu/SimService/api.html
 * http://swoogle.umbc.edu/SimService/
 * 
 * Lushan Han, Abhay L. Kashyap, Tim Finin, James Mayfield and Johnathan Weese, UMBC_EBIQUITY-CORE: Semantic Textual Similarity Systems, 
 * Proc. 2nd Joint Conf. on Lexical and Computational Semantics, Association for Computational Linguistics, June 2013.
 * 
 * Our statistical method is based on distributional similarity and Latent Semantic Analysis (LSA). 
 * We further complement it with semantic relations extracted from WordNet.
 * 
 * @author maojin
 *
 */
public class UMBCSim {
	/**
	 * @param text
	 */
	public double callWebAPI(String phrasea, String phraseb){
		//List<BBEntity> entities = new ArrayList();
		List<String> lines = null;
		try {
			String encodePhr1 = phrasea.replace(" ", "%20");
			String encodePhrb = phraseb.replace(" ", "%20");
			String urlParameters = "operation=api&phrase1="+encodePhr1
					+"&phrase2="+encodePhrb;
			byte[] postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
			int    postDataLength = postData.length;
			String request = "http://swoogle.umbc.edu/SimService/GetSimilarity";
			
			URL url = new URL( request );
			HttpURLConnection conn= (HttpURLConnection)url.openConnection();           
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
			conn.setUseCaches(false);
			try( DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
			   wr.write( postData );
			}
			
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

			/*
			StringBuilder sb = new StringBuilder();
	        for (int c; (c = in.read()) >= 0;)
	            sb.append((char)c);
	             String response = sb.toString();
	        */
			lines = FileUtil.readLineFromReader(in);
			
			for(String line:lines){
				return new Double(line);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
		return 0;
	}
	
	
	
	public static void main(String[] args){
		UMBCSim simtool = new UMBCSim();
		long b = System.currentTimeMillis();
		simtool.callWebAPI("patients with Helicobacter pylori-chronic active gastritis", "patients with disease");
		long e = System.currentTimeMillis();
		System.out.println((e-b));
	}
}

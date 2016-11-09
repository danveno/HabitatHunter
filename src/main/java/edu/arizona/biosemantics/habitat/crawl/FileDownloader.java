package edu.arizona.biosemantics.habitat.crawl;



import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.clapper.util.misc.MIMETypeUtil;

/**
 * a common file downloader, save the downloaded file into somewhere
 * typically can download files\images\zip files
 * bug：获取response header中Content-Disposition中filename中文乱码问题
 * @author maojin
 *
 */
public class FileDownloader {

	public int cache = 10 * 1024;
	public static final boolean isWindows;
	public static final String splash;
	public final String root;
	
	static {
		if (System.getProperty("os.name") != null && System.getProperty("os.name").toLowerCase().contains("windows")) {
			isWindows = true;
			splash = "\\";
			//root="D:";
		} else {
			isWindows = false;
			splash = "/";
			//root="/search";
		}
	}
	
	
	public FileDownloader(String root){
		this.root = root;
	}
	
	public FileDownloader(String root, int cache){
		this.root = root;
		this.cache = cache;
	}
	
	
	/**
	 * Download according to the URL, the filename is obtained from response header.
	 * @param url
	 * @return
	 */
	public String download(String url) {
		return download(url, null);
	}

	
	/**
	 * Down load from URL and save to the file of filepath
	 * @param url
	 * @param filepath
	 * @return
	 */
	public String download(String url, String filepath) {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = client.execute(httpget);

			//outHeaders(response);
			
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			if (filepath == null)
				filepath = getRandomFilePath(response);
			File file = new File(filepath);
			file.getParentFile().mkdirs();
			FileOutputStream fileout = new FileOutputStream(file);
			/**
			 * The cache parameter should be set according to the real situation
			 */
			byte[] buffer=new byte[cache];
			int ch = 0;
			while ((ch = is.read(buffer)) != -1) {
				fileout.write(buffer,0,ch);
			}
			is.close();
			fileout.flush();
			fileout.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
		
		
		/**
		 * get the default save file path
		 * @param response
		 * @return
		 */
		public String getRandomFilePath(HttpResponse response) {
			String filepath = root + splash;
			String filename = getFileName(response);

			if (filename != null) {
				filepath += filename;
			} else {
				filepath += getRandomFileName();
			}
			filepath = filepath+"."+getFileExtension(response);
			return filepath;
		}
		
		
		/**
		 * Get the file name from the Content-Disposition(filename is there) in the response header
		 * @param response
		 * @return
		 */
		public String getFileName(HttpResponse response) {
			Header contentHeader = response.getFirstHeader("Content-Disposition");
			String filename = null;
			if (contentHeader != null) {
				HeaderElement[] values = contentHeader.getElements();
				if (values.length == 1) {
					NameValuePair param = values[0].getParameterByName("filename");
					if (param != null) {
						try {
							//filename = new String(param.getValue().toString().getBytes(), "utf-8");
							//filename=URLDecoder.decode(param.getValue(),"utf-8");
							filename = param.getValue();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			return filename;
		}
		
		
		/**
		 * Get the file name from the Content-Disposition(filename is there) in the response header
		 * @param response
		 * @return
		 */
		public String getFileExtension(HttpResponse response) {
			Header contentHeader = response.getFirstHeader("Content-Type");
			String MIMEType = contentHeader.getValue();
			String fileExtension = MIMETypeUtil.fileExtensionForMIMEType(MIMEType);
			return fileExtension;
		}
		
		
		
		/**
		 * Generate a random File Name
		 * @return
		 */
		public String getRandomFileName() {
			return String.valueOf(System.currentTimeMillis());
		}
		
		
		/**
		 * show all the headers
		 * @param response
		 */
		public void outHeaders(HttpResponse response) {
			Header[] headers = response.getAllHeaders();
			for (int i = 0; i < headers.length; i++) {
				System.out.println(headers[i]);
			}
		}
		
		
		
		public static void main(String[] args) {
//			String url = "http://bbs.btwuji.com/job.php?action=download&pid=tpc&tid=320678&aid=216617";
			String url="http://tucsonfestivalofbooks.org/images/sponsors/10506_sized.png?ts=1447865483";
//			String filepath = "D:\\test\\a.torrent";
			String filepath = "D:\\test\\a.jpg";
			
			FileDownloader downloader = new FileDownloader("D:\\test");
			String dfilePath = downloader.download(url);
			System.out.println("it's downloaded into "+dfilePath);
		}

}

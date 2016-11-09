package edu.arizona.biosemantics.habitat.crawl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;





import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * fetch the lists from the list
 * URL pattern: http://bdj.pensoft.net/browse_articles?journal_name=bdj&journal_id=1&p=1
 * http://bdj.pensoft.net/browse_articles?#commonParam#&p=#p#
 * p means the page, start from 1
 * @author maojin
 *
 */
public class PensoftListCrawler {
	private String listUrlPattern;//url pattern,  like http://www.com/ada=a#commonParam"&page=#page#&eachPage=#eachpage#";
	private String commonParam;// common param in the url
	private String XMLUrlPattern;//#articleId#
	private String rootDownloadFolder;//
	
	public PensoftListCrawler(String listUrlPattern,String commonParam, String XMLUrlPattern){
		this.commonParam = commonParam;
		this.listUrlPattern = listUrlPattern.replace("#commonParam#", commonParam);
		this.XMLUrlPattern = XMLUrlPattern;
	}
	
	public String getRootDownloadFolder() {
		return rootDownloadFolder;
	}

	public void setRootDownloadFolder(String rootDownloadFolder) {
		this.rootDownloadFolder = rootDownloadFolder;
	}



	/**
	 * crawl all the data from startPage to endPage
	 * @param startPage
	 * @param endPage
	 * @return
	 */
	public List crawlList(Integer startPage, Integer endPage){
		
		for(int page = startPage; page<=endPage; page++){
			//construct the real page url
			String readUrl = listUrlPattern.replace("#p#", page+"");
			System.out.println("current download page:"+page+" "+readUrl);
			//fetch content of the page
			Hashtable headers = null;
			String content = FetchUrlContent.getContent(readUrl, headers, "utf-8");
			
			//System.out.println(content);
			//parse the content to obtain the URL list holding the XML download URLs
			List<String> articleIds = parse(content);
			
			downloadAllXML(articleIds);
			
		}
		return null;
	}
	
	
	/**
	 * download all articles
	 * @param articleIds
	 */
	public void downloadAllXML(List<String> articleIds) {
		FileDownloader downloader = new FileDownloader(rootDownloadFolder);
		for(String articleId:articleIds){
			String xmlUrl = XMLUrlPattern.replace("#articleId#", articleId);
			downloader.download(xmlUrl, rootDownloadFolder+"/"+articleId+".xml");
			
			try {
				Thread.sleep(new Random().nextInt(10000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * parse List from the content
	 * @param content
	 * @return
	 */
	public List parse(String content){
		
		//<a target="_blank" title="XML" href="/lib/ajax_srv/article_elements_srv.php?action=download_xml&amp;item_id=7159" class="browse_button">XML</a>
		Document doc = Jsoup.parse(content);
		Elements xmlLinks = doc.select("a[title=XML]"); // a with href   ^/lib/ajax_srv/article_elements_srv.php?action=download_xml
		Iterator<Element> xmlItor = xmlLinks.iterator();
		List contList = new ArrayList();
		while(xmlItor.hasNext()){
			Element xmlLink = xmlItor.next();
			String linkHref = xmlLink.attr("href");
			int equIndex = linkHref.lastIndexOf("=");
			String articleId = linkHref.substring(equIndex+1);
			contList.add(articleId);
			
			//System.out.println(articleId);
		}
		
		return contList;
	}
	
	
	public static void main(String[] args){
		
		//BDJ 0-21
		//String listUrlPattern = "http://bdj.pensoft.net/browse_articles?#commonParam#&p=#p#";
		//String commonParam = "journal_name=bdj&journal_id=1";//data journal
		//String xmlPattern ="http://bdj.pensoft.net/lib/ajax_srv/article_elements_srv.php?action=download_xml&item_id=#articleId#";
		
		//phytokeys  0-23
		//String listUrlPattern = "http://phytokeys.pensoft.net/browse_articles?#commonParam#&p=#p#";
		//String commonParam = "journal_name=phytokeys&journal_id=3";//data journal
		//String xmlPattern ="http://phytokeys.pensoft.net/lib/ajax_srv/article_elements_srv.php?action=download_xml&item_id=#articleId#";
		
		//zookeys  0-242
		String listUrlPattern = "http://zookeys.pensoft.net/browse_articles?#commonParam#&p=#p#";
		String commonParam = "journal_name=zookeys&journal_id=2";//data journal
		String xmlPattern ="http://zookeys.pensoft.net/lib/ajax_srv/article_elements_srv.php?action=download_xml&item_id=#articleId#";
		
		
		PensoftListCrawler plCrawler = new PensoftListCrawler(listUrlPattern, commonParam, xmlPattern);
		String rootDownloadFolder="F:\\Habitat\\dataset\\zookeys";
		plCrawler.setRootDownloadFolder(rootDownloadFolder);
		plCrawler.crawlList(161, 161);//start with 0
		//page 8 run errors
	}
}

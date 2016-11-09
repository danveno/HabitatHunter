package edu.arizona.biosemantics.habitat.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;

import org.clapper.util.html.HTMLUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.jsoup.Jsoup;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * parse information from Pensoft XML
 * 
 * @author maojin
 *
 */
public class PensoftXMLParse {

	// /**
	// *
	// * @param xmlFile
	// */
	// public void extract(String xmlFile){
	// try {
	// // Use a SAX builder
	// SAXBuilder builder = new SAXBuilder();
	// // build a JDOM2 Document using the SAXBuilder.
	// Document jdomDoc = null;
	// jdomDoc = builder.build(new File(xmlFile));
	// // get the document type
	// System.out.println(jdomDoc.getDocType());
	//
	// //get the root element
	// Element web_app = jdomDoc.getRootElement();
	// System.out.println(web_app.getName());
	//
	// // get the first child with the name 'servlet'
	// Element body = web_app.getChild("body");
	// System.out.println(body.getName()+" "+Jsoup.parse(body.getTextNormalize()).text());
	// /*
	// // iterate through the descendants and print non-Text and non-Comment
	// values
	// IteratorIterable<Content> contents = web_app.getDescendants();
	// while (contents.hasNext()) {
	// Content web_app_content = contents.next();
	// if (!web_app_content.getCType().equals(CType.Text) &&
	// !web_app_content.getCType().equals(CType.Comment)) {
	// System.out.println(web_app_content.toString());
	// }
	// }
	//
	// // get comments using a Comment filter
	// IteratorIterable<Comment> comments =
	// web_app.getDescendants(Filters.comment());
	// while (comments.hasNext()) {
	// Comment comment = comments.next();
	// System.out.println(comment);
	// }*/
	// } catch (JDOMException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	//
	// }

	/**
	 * parse the text from a XML file and save into a text file
	 * @param xmlFile
	 */
	public void extractTagText(File xmlFile, String saveTextFile) {
		SAXReader reader = new SAXReader();
		//reader.setValidation(false);
		reader.setEntityResolver(new EntityResolver() {//disable the dtd
	        public InputSource resolveEntity(String publicId, String systemId)
	                throws SAXException, IOException {
	            if (systemId.contains("dtd")||systemId.contains("ent")) {
	                return new InputSource(new StringReader(""));
	            } else {
	                return null;
	            }
	        }
	    });
		Document document;
		try {
			//file writer
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveTextFile), "UTF-8"));
			
			document = reader.read(xmlFile);
//			System.out.println("Root element :"
//					+ document.getRootElement().getName());

			Element classElement = document.getRootElement();
			
			//title group
			List<Node> titleNodes = document.selectNodes("/article/front/article-meta/title-group");
			if(titleNodes.size()>0){
				String titleXML = titleNodes.get(0).asXML(); 
				String titleStr = HTMLUtil.stripHTMLTags(titleXML).trim();
				//System.out.println(titleStr);
				out.write(titleStr);
			}
			
			//abstract
			List<Node> abstractNodes = document.selectNodes("/article/front/article-meta/abstract");
			if(abstractNodes.size()>0){
				String abstractXML = abstractNodes.get(0).asXML();
				String abstractStr = HTMLUtil.stripHTMLTags(abstractXML).trim();
				out.write(abstractStr);
			}
			
			//body
			List<Node> bodyNodes = document.selectNodes("/article/body");
			if(bodyNodes.size()>0){
				String bodyXML = bodyNodes.get(0).asXML();
				String bodyStr = HTMLUtil.stripHTMLTags(bodyXML);
				out.write(bodyStr);
			}
			
			//references
			List<Node> refNodes = document.selectNodes("/article/back/ref-list");///
			if(refNodes.size()>0){
				String refXML = refNodes.get(0).asXML();
				String refStr = HTMLUtil.stripHTMLTags(refXML);
				out.write(refStr);
			}
			
			//float
			List<Node> floatNodes = document.selectNodes("//article/floats-group");
			if(floatNodes.size()>0){
				String floatXML = floatNodes.get(0).asXML();
				String floatStr = HTMLUtil.stripHTMLTags(floatXML);
				out.write(floatStr);
			}
			out.flush();
			out.close();
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * extract Taxon Name
	 * @param xmlFile
	 * @param saveEntityFile
	 */
	public void extractTaxonName(File xmlFile, String saveEntityFile) {
		SAXReader reader = new SAXReader();
		//reader.setValidation(false);
		reader.setEntityResolver(new EntityResolver() {//disable the dtd
	        public InputSource resolveEntity(String publicId, String systemId)
	                throws SAXException, IOException {
	            if (systemId.contains("dtd")||systemId.contains("ent")) {
	                return new InputSource(new StringReader(""));
	            } else {
	                return null;
	            }
	        }
	    });
		Document document;
		try {
			//file writer
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveEntityFile), "UTF-8"));
			
			document = reader.read(xmlFile);
			Element classElement = document.getRootElement();
			//taxon-name group
			/**
			 * -<tp:taxon-name>

<tp:taxon-name-part taxon-name-part-type="genus">Lathrobium</tp:taxon-name-part>

<tp:taxon-name-part taxon-name-part-type="species">jiulongshanense</tp:taxon-name-part>

<object-id xlink:type="simple">urn:lsid:zoobank.org:act:558DE96D-6F1C-4A37-8BDE-C642DD835556</object-id>

<object-id xlink:type="simple">http://species-id.net/wiki/Lathrobium_jiulongshanense</object-id>

</tp:taxon-name>
			 */
			List<Node> taxonNameNodes = document.selectNodes("//tp:taxon-name");
			System.out.println(xmlFile.getName()+" "+taxonNameNodes.size());
			for(Node taxonNameNode:taxonNameNodes){
				List<Node> tnpNodes = taxonNameNode.selectNodes("/tp:taxon-name-part");
				//System.out.println(xmlFile.getName()+" "+tnpNodes.size());
				if(tnpNodes.size()>0){//have sub names
					for(Node tnpNode: tnpNodes){
						String name = tnpNode.getText();
						Element element = (Element) tnpNode;
						String type = element.attributeValue("taxon-name-part-type");
						//System.out.println(titleStr);
						if(!"".equals(name)) out.write(name.trim()+"\t"+type+"\n");
					}
				}else{
					String name = taxonNameNode.getText().trim();
					if(!"".equals(name)) out.write(name+"\tfull\n");
				}
			}
			out.flush();
			out.close();
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	
	/**
	 * process all the files in the sourceFolder
	 * @param sourceFolder
	 * @param destFolder
	 */
	public void batchParse(String sourceFolder, String destFolder){
		File sourcePath = new File(sourceFolder);
		File[] sourceFiles = sourcePath.listFiles();
		for(File xmlFile: sourceFiles){
			String name = xmlFile.getName();
			if(name.endsWith("xml")){//do not process dtd file
				String destFile = destFolder+"\\"+name.replace("xml", "txt");
				extractTagText(xmlFile, destFile);
			}
			
		}
	}
	
	/**
	 * process all the files in the sourceFolder
	 * @param sourceFolder
	 * @param destFolder
	 */
	public void batchParseTaxonName(String sourceFolder, String destFolder){
		File sourcePath = new File(sourceFolder);
		File[] sourceFiles = sourcePath.listFiles();
		for(File xmlFile: sourceFiles){
			String name = xmlFile.getName();
			if(name.endsWith("xml")){//do not process dtd file
				String destFile = destFolder+"\\"+name.replace("xml", "txt");
				extractTaxonName(xmlFile, destFile);
			}
		}
	}
	
	public static void main(String[] args) {
		PensoftXMLParse pxmlParse = new PensoftXMLParse();
		String xmlFolder = "F:\\Habitat\\dataset\\phytokeys";
		String txtFolder = "F:\\Habitat\\procdata\\zookeystxt";
		String taxonFolder = "F:\\Habitat\\procdata\\phytokeystaxon";
		//String taxonFolder = "F:\\Habitat\\procdata\\zookeystaxon";
		//String taxonFolder = "F:\\Habitat\\procdata\\zookeystaxon";
		//phytokeystxt
		//bdjtxt
		//pxmlParse.batchParse(xmlFolder, txtFolder);
		pxmlParse.batchParseTaxonName(xmlFolder, taxonFolder);
	}

}

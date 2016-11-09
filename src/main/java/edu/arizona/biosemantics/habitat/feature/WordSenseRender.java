package edu.arizona.biosemantics.habitat.feature;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.bb.Config;
import edu.arizona.biosemantics.habitat.io.FileUtil;
import edu.arizona.biosemantics.habitat.ontomap.OntologyMapping;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.IRAMDictionary;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.ILoadPolicy;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISenseEntry;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.morph.WordnetStemmer;

/**
 * handle features about wordnet
 * 
 * @author maojin
 *
 */
public class WordSenseRender {

	public Dictionary dict;
	
	public OntologyMapping ncbiOntoMapping;

	public WordnetStemmer stemmer ;
	
	public WordSenseRender() {
		try {
			this.dict = loadDictionary(Config.wordNetDir);
			Set stopwordSet = FileUtil.readTermSet(Config.stopwordFile);
			ncbiOntoMapping = new OntologyMapping(Config.ncbiCleanedTokens);
			ncbiOntoMapping.getOntologyManager().initTokenSet(Config.ncbiCleanedTokens);
			ncbiOntoMapping.setStopSet(stopwordSet);
			
			stemmer = new WordnetStemmer(dict);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void testDictionary() throws IOException {
		// construct the URL to the Wordnet dictionary directory
		// String wnhome = System.getenv(" WNHOME ");
		// String path = wnhome + File.separator + " dict ";
		String path = "D:\\Program Files\\WordNet\\dict";
		URL url = new URL("file", null, path);

		// construct the dictionary object and open it
		IDictionary dict = new Dictionary(url);
		dict.open();

		// look up first sense of the word "dog "
		IIndexWord idxWord = dict.getIndexWord("dog", POS.NOUN);
		IWordID wordID = idxWord.getWordIDs().get(0);
		IWord word = dict.getWord(wordID);
		System.out.println("Id = " + wordID);
		System.out.println(" Lemma = " + word.getLemma());
		System.out.println(" Gloss = " + word.getSynset().getGloss());
	}

	/**
	 * load the dictionary
	 * 
	 * @param dictDir
	 * @return
	 * @throws IOException
	 */
	public Dictionary loadDictionary(String dictDir) throws IOException {
		URL url = new URL("file", null, dictDir);

		// construct the dictionary object and open it
		Dictionary dict = new Dictionary(url);
		dict.open();
		return dict;
	}

	/**
	 * load the dictionary into RAM heap size: 500 MB or 1 GB.
	 * 
	 * @param dictDir
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public IRAMDictionary loadRAMDictionory(String dictDir) throws IOException,
			InterruptedException {
		// construct the dictionary object and open it
		IRAMDictionary dict = new RAMDictionary(new File(dictDir), ILoadPolicy.NO_LOAD);
		dict.open();

		// now load into memory
		dict.load(true);
		// do something slowly
		return dict;
		// trek(dict);
	}

	public void trek(IDictionary dict) {
		int tickNext = 0;
		int tickSize = 20000;
		int seen = 0;
		System.out.print(" Treking across Wordnet ");
		long t = System.currentTimeMillis();
		for (POS pos : POS.values())
			for (Iterator<IIndexWord> i = dict.getIndexWordIterator(pos); i
					.hasNext();)
				for (IWordID wid : i.next().getWordIDs()) {
					seen += dict.getWord(wid).getSynset().getWords().size();
					if (seen > tickNext) {
						System.out.print('.');
						tickNext = seen + tickSize;
					}
				}
		System.out.println(" done (%1 d msec )\n"
				+ (System.currentTimeMillis() - t));
		System.out.println("In my trek I saw " + seen + " words ");
	}

	/**
	 * obtain the non-base form
	 * 
	 * @param word
	 * @param pos
	 * @return
	 */
	public String stemWord(String word, POS pos) {
		List<String> stemWords = stemmer.findStems(word, pos);
		try{
			return stemWords.get(0);
		}catch(Exception e){
			return null;
		}
	}

	

	/**
	 * get the first high-level sense of the word
	 * 
	 * @param word
	 *            use the base form
	 * @param pos
	 * @return
	 */
	public String getSense(String word, POS pos) {
		
		//word = stemWord(word, pos);
		
		// look up first sense of the word
		IIndexWord idxWord = dict.getIndexWord(word, pos);
		String sense = null;
		try{
			///System.out.println("tag sense count = " + idxWord.getTagSenseCount());
			IWordID wordID = idxWord.getWordIDs().get(0); // 1st meaning
			
			IWord wordId = dict.getWord(wordID);
			//ISenseEntry sen = dict.getSenseEntry(wordId.getSenseKey());
			//System.out.println("Id = " + wordID);
			//System.out.println("Sense Key: " + wordId.getSenseKey());
			//System.out.println("sen: " + sen.getPOS());
			//System.out.println("getSenseNumber: " + sen.getSenseNumber());
			//System.out.println("getSenseNumber: " + wordId.getSynset().getGloss());
			ISynset synset = wordId.getSynset();
			String lexFileName = synset.getLexicalFile().getName();
			
			if(lexFileName!=null) sense = lexFileName.substring(lexFileName.indexOf(".")+1, lexFileName.length());
		}catch(NullPointerException e){
			sense=null;
		}
	    //System.out.println("sense of "+word +" = " + sense);
	    
		return sense;
	}
	
	
	
	
	
	/**
	 * get the synonyms of the word with the lemma form without itself
	 * 
	 * @param word
	 *            use the base form
	 * @param pos
	 * @return
	 */
	public List<String> getSynonyms(String word, POS pos) {
		// look up first sense of the word
		IIndexWord idxWord = dict.getIndexWord(word, pos);
		if (idxWord != null) {
			IWordID wordID = idxWord.getWordIDs().get(0); // 1st meaning
			IWord wordId = dict.getWord(wordID);
			// iterate over words associated with the synset
			ISynset synset = wordId.getSynset();

			List<String> synList = new ArrayList();
			for (IWord w : synset.getWords()) {
				if (!w.equals(wordId))
					synList.add(w.getLemma());
				//System.out.println(w.getLemma());
			}
			return synList;
		} else {
			return null;
		}

	}

	/**
	 * get the hypernyms of the word in the lemma form
	 * 
	 * @param word
	 *            use the base form
	 * @param pos
	 * @return
	 */
	public List<String> getHypernyms(String word, POS pos) {
		// look up first sense of the word
		IIndexWord idxWord = dict.getIndexWord(word, pos);
		if (idxWord != null) {
			IWordID wordID = idxWord.getWordIDs().get(0); // 1st meaning
			IWord wordId = dict.getWord(wordID);
			// iterate over words associated with the synset
			ISynset synset = wordId.getSynset();
			List<ISynsetID> hypernyms = synset
					.getRelatedSynsets(Pointer.HYPERNYM);
			List<String> hyperList = new ArrayList();

			for (ISynsetID sid : hypernyms) {
				List<IWord> words = dict.getSynset(sid).getWords();
				for (Iterator<IWord> i = words.iterator(); i.hasNext();) {
					String hyperLemma = i.next().getLemma();
					hyperList.add(hyperLemma);
					//System.out.println(hyperLemma);
				}
			}
			return hyperList;
		} else {
			return null;
		}

	}
	
	/**
	 * 
	 * @param word
	 * @param posType
	 * @return
	 */
	public String getExtendedSense(String wordLemma, String posTag){
		POS wordTag = getPOS(posTag);
		String sense = null;
		if(wordTag!=null){
			sense = getSense(wordLemma, wordTag);
		}
		if(sense==null){
			sense = ncbiOntoMapping.testSimpleToken(wordLemma)?"bacterium":null;
		}
		return sense;
	}

	/**
	 * convert Penn Tree POS Tags to WordNet tags
	 * @param posType
	 * @return
	 */
	public POS getPOS(String posType){
		if("NN".equals(posType)||"NNS".equals(posType)||"NNP".equals(posType)||"NNPS".equals(posType)){
			return POS.NOUN;
		}else if("VB".equals(posType)||"VBP".equals(posType)||"VBN".equals(posType)||"VBD".equals(posType)||"VBG".equals(posType)||"VBZ".equals(posType)){
			return POS.VERB;
		}else if("JJ".equals(posType)||"JJR".equals(posType)||"JJS".equals(posType)){
			return POS.ADJECTIVE;
		}else if("RB".equals(posType)||"RBR".equals(posType)||"RBS".equals(posType)){
			return POS.ADVERB;
		}else{
			return null;
		}
	}
	
	
	
	public static void main(String[] args) {
		String wordNetDictDir = "F:\\科研工具\\wordnet\\wn31dict";//D:\\Program Files\\WordNet\\dict";
		WordSenseRender wnr = new WordSenseRender();
		try {
			// wnr.testDictionary();
			wnr.loadDictionary(wordNetDictDir);
			// wnr.loadRAMDictionory(wordNetDictDir);
			//System.out.println(wnr.stemWord("dogs", POS.NOUN));
			//System.out.println(wnr.getSense("dog", POS.NOUN));
			System.out.println(wnr.stemWord("patients", POS.NOUN));
			//System.out.println(wnr.getExtendedSense("maintain", "VBG"));
		} catch (IOException e) {
			e.printStackTrace();
			// } catch (InterruptedException e) {
			// e.printStackTrace();
		}
	}
}

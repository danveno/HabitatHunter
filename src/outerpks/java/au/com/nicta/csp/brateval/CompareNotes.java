package au.com.nicta.csp.brateval;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 
 * BRAT stand-off attribute comparison
 * 
 *
 */
public class CompareNotes
{
	public static boolean checkEntities(Document d) {
		HashSet<String> entitiesWithNotes = new HashSet<String>();
//		HashSet<String> entitiesWithAttributes = new HashSet<String>();
		
    	for (Note n : d.getNotes()) {
    		entitiesWithNotes.add(n.getEntity());
    	}

    	for (Entity e : d.getEntities()) {
    		if (e.getFile() == null) {
    			throw new RuntimeException("Entity " + e + " has no file name");
    		}
    		if (e.getId() == null) {
    			throw new RuntimeException("Entity " + e + " has no id");
    		}
    		if (e.getString() == null) {
    			throw new RuntimeException("Entity " + e + " has no string value");
    		}
    		if (e.getType() == null) {
    			throw new RuntimeException("Entity " + e + " has no type");
    		}    	
    		if (!entitiesWithNotes.contains(e.getId())) {
    			throw new RuntimeException("Entity " + e + " has no note");
    		}
		}	
    	return true;
    	
	}
	
	
  public static void main (String argc []) throws IOException
  {
	String folder1 = argc[0];
	String folder2 = argc[1];
	boolean exact_match = Boolean.parseBoolean(argc[2]);
		
//	String folder1 = "/home/xtannier/tmp/task1b-tmp/1006003";
//	String folder2 = "/home/xtannier/tmp/task1b-tmp/1006003";
//	boolean exact_match = false;

	Map <String, Integer> noteTP = new TreeMap <String, Integer> ();
	Map <String, Integer> noteFP = new TreeMap <String, Integer> ();
	Map <String, Integer> noteFN = new TreeMap <String, Integer> ();

	Set <String> noteTypes = new TreeSet <String> ();
	
    File folder = new File(folder1);

    for (File file : folder.listFiles())
    {
      if (file.getName().endsWith(".ann"))
      {
        Document d1 = Annotations.read(file.getAbsolutePath(), "ann");
        Document d2 = Annotations.read(folder2 + File.separator +  file.getName(), "ann");
       
        // Compute true positives and false positives
        for (Note n : d1.getNotes())
        {
        //	attributeTypes.add(a.getValue());
        	// get the entity that the attribute modifies
        	String entityID = n.getEntity();
        	Entity e = d1.getEntity(entityID);
        	noteTypes.add(e.getType());

        	// look for a matching entity
        	Entity matchE = null;
        	if(e!=null){
        		if (exact_match)
        		{	matchE = d2.findEntity(e); }
        		else
        		{ 
        			matchE = d2.findEntity(e);
        			if (matchE == null)
        				matchE = d2.findEntityOverlapC(e); 
        		}
            }
        	
        	// if there is an entity match, look for an attribute match
        	boolean ok = false;
        	 if (matchE != null){
        		 ok = findNoteMatch(n.getString(), matchE, d2);
       	     }
        	 
        	 if (ok){ //if there is a match, increment true positives
    			 if (noteTP.get(matchE.getType()) == null){
    				 noteTP.put(matchE.getType(), 1);
    			 }
    			 else{
    				 noteTP.put(matchE.getType(), 
    						 noteTP.get(matchE.getType()) + 1);
    			 }
    		 }
        	 else { //if there is no match, increment false positives
        		 System.out.println(file.getAbsolutePath() + " " + e.getId());
//        		 System.out.println(matchE);
//        		 System.out.println(e);
        		 if (noteFP.get(e.getType()) == null){
    				 noteFP.put(e.getType(), 1);
    			 }
    			 else{
    				 noteFP.put(e.getType(),
    						 noteFP.get(e.getType()) + 1);
    			 }
        	 }
    	}
        
        // Compute false negatives
        for (Note n : d2.getNotes())
        {
        //	attributeTypes.add(a.getValue());
        	// get the entity that the attribute modifies
        	String entityID = n.getEntity();
        	Entity e = d2.getEntity(entityID);
        	noteTypes.add(e.getType());
        	
        	// look for a matching entity
        	Entity matchE = null;
        	if (exact_match)
            {	matchE = d1.findEntity(e); }
            else
            { 
            	matchE = d1.findEntity(e);
            	if (matchE == null)
            		matchE = d1.findEntityOverlapC(e); 
            }

        	// if there is an entity match, look for an attribute match
        	boolean ok = false;
        	 if (matchE != null){
        		 ok = findNoteMatch(n.getString(), matchE, d1);
       	     }
        	 
        	 if (!ok){ //if there is a match, increment true positives
        		 if (noteFN.get(e.getType()) == null){
    				 noteFN.put(e.getType(), 1);
    			 }
    			 else{
    				 noteFN.put(e.getType(),
    						 noteFN.get(e.getType()) + 1);
    			 }

        	 }
        }
      }
    }

    System.out.println("");
    System.out.println("Summary");
    System.out.println("\tTP"
	           + "\tFP" 
	           + "\tFN"
	           + "\tPrecision"
	           + "\tRecall"
	           + "\tF1"
	           );
    
    int allTP = 0;
    int allFP = 0;
    int allFN = 0;
    // Compute Precision, Recall, F1 for each attribute type
    for (String at : noteTypes)
    {
      int TP = (noteTP.get(at) == null ? 0 : noteTP.get(at));
      int FP = (noteFP.get(at) == null ? 0 : noteFP.get(at));
      int FN = (noteFN.get(at) == null ? 0 : noteFN.get(at));
      
      allTP+=TP;
      allFP+=FP;
      allFN+=FN;
      
      double precision = 0;
      double recall = 0;
      double f_measure = 0;
      
      if (TP+FP > 0) { precision = (double)TP/(TP+FP); }
      
      if (TP+FN > 0) { recall = (double)TP/(TP+FN); }
      
      if ((precision+recall) > 0)
      { f_measure = (2*precision*recall)/(double)(precision+recall); }
    	
      System.out.println(at
    		           + "\t" + TP 
    		           + "\t" + FP 
    		           + "\t" + FN
    		           + "\t" + String.format("%1.4f", precision)
    		           + "\t" + String.format("%1.4f", recall)
    		           + "\t" + String.format("%1.4f", f_measure)
    		           );
    }
    // Compute overall precision, recall, F1 (micro-average)
    double precision = 0;
    double recall = 0;
    double f_measure = 0;
    
    if (allTP+allFP > 0) { precision = (double)allTP/(allTP+allFP); }
    
    if (allTP+allFN > 0) { recall = (double)allTP/(allTP+allFN); }
    
    if ((precision+recall) > 0)
    { f_measure = (2*precision*recall)/(double)(precision+recall); }
  	
    System.out.println("Overall"
  		           + "\t" + allTP 
  		           + "\t" + allFP 
  		           + "\t" + allFN
  		           + "\t" + String.format("%1.4f", precision)
  		           + "\t" + String.format("%1.4f", recall)
  		           + "\t" + String.format("%1.4f", f_measure)
  		           );
    
    
    
  }
    
  private static boolean findNoteMatch (String noteValue, Entity eToCheck, 
		  Document d){
	  // Looks for attributes modifying entity 'e' that match attribute 'att'
      for (Note n : d.getNotes()) {
		  // check if attribute 'a' modifies entity 'e'
		  String entityID = n.getEntity();
		  if(entityID.equals(eToCheck.getId())) {
			  return n.getString().equals(noteValue);
		  }
	  }
	  return false;
  }
  
  
  
  
  
  
  
}
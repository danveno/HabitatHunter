package au.com.nicta.csp.brateval;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 
 * BRAT stand-off attribute comparison
 * See: https://perso.limsi.fr/pz/blah2015/
 *
 */
public class CompareAttributes
{
  public static void main (String argc []) throws IOException
  {
	String folder1 = argc[0];
	String folder2 = argc[1];

	boolean exact_match = Boolean.parseBoolean(argc[2]);

//	String folder1 = "/home/xtannier/tmp/task1b-tmp/1006003";
//	String folder2 = "/home/xtannier/tmp/task1b-tmp/1006003";
//	boolean exact_match = false;
	
	
	Map <String, Integer> attributeTP = new TreeMap <String, Integer> ();
	Map <String, Integer> attributeFP = new TreeMap <String, Integer> ();
	Map <String, Integer> attributeFN = new TreeMap <String, Integer> ();

	Set <String> attributeTypes = new TreeSet <String> ();
	
    File folder = new File(folder1);

    for (File file : folder.listFiles())
    {
      if (file.getName().endsWith(".ann"))
      {
        Document d1 = Annotations.read(file.getAbsolutePath(), "ann");
        Document d2 = Annotations.read(folder2 + File.separator +  file.getName(), "ann");
       
        // Compute true positives and false positives
        for (Attribute a : d1.getAttributes())
        {
        //	attributeTypes.add(a.getValue());
        	attributeTypes.add(a.getValue()+"_"+a.getList().get(1));
        	// get the entity that the attribute modifies
        	String entityID = a.getList().get(0);
        	Entity e = d1.getEntity(entityID);
        	
        	// look for a matching entity
        	Entity matchE = null;
        	if(e!=null){
        		if (exact_match)
        		{	matchE = d2.findEntity(e); }
        		else
        		{ matchE = d2.findEntityOverlapC(e); }
            }
        	
        	Attribute match = null;
        	// if there is an entity match, look for an attribute match
        	 if (matchE != null){
        		 match = findAttributeMatch(a, matchE,d2);
       	     }
        	 
        	 if (match != null){ //if there is a match, increment true positives
    			 if (attributeTP.get(a.getValue()+"_"+a.getList().get(1)) == null){
    				 attributeTP.put(a.getValue()+"_"+a.getList().get(1), 1);
    			 }
    			 else{
    				 attributeTP.put(a.getValue()+"_"+a.getList().get(1), 
    						 attributeTP.get(a.getValue()+"_"+a.getList().get(1)) + 1);
    			 }
    		 }
        	 else { //if there is no match, increment false positives
        		 if (attributeFP.get(a.getValue()+"_"+a.getList().get(1)) == null){
    				 attributeFP.put(a.getValue()+"_"+a.getList().get(1), 1);
    			 }
    			 else{
    				 attributeFP.put(a.getValue()+"_"+a.getList().get(1),
    						 attributeFP.get(a.getValue()+"_"+a.getList().get(1)) + 1);
    			 }
        	 }
    	}
        
        // Compute false negatives
        for (Attribute a : d2.getAttributes())
        {
        //	attributeTypes.add(a.getValue());
        	attributeTypes.add(a.getValue()+"_"+a.getList().get(1));
        	// get the entity that the attribute modifies
        	String entityID = a.getList().get(0);
        	Entity e = d2.getEntity(entityID);
        	
        	// look for a matching entity
        	Entity matchE = null;
        	if (exact_match)
            {	matchE = d1.findEntity(e); }
            else
            { matchE = d1.findEntityOverlapC(e); }

        	Attribute match = null;
        	// if there is an entity match, look for an attribute match
        	 if (matchE != null){
        		 match = findAttributeMatch(a, matchE,d1);
       	     }
        	 
        	 if(match==null) { //if there is no match, increment false negatives
        		 if (attributeFN.get(a.getValue()+"_"+a.getList().get(1)) == null){
    				 attributeFN.put(a.getValue()+"_"+a.getList().get(1), 1);
    			 }
    			 else{
    				 attributeFN.put(a.getValue()+"_"+a.getList().get(1),
    						 attributeFN.get(a.getValue()+"_"+a.getList().get(1)) + 1);
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
    for (String at : attributeTypes)
    {
      int TP = (attributeTP.get(at) == null ? 0 : attributeTP.get(at));
      int FP = (attributeFP.get(at) == null ? 0 : attributeFP.get(at));
      int FN = (attributeFN.get(at) == null ? 0 : attributeFN.get(at));
      
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
  
  private static Attribute findAttributeMatch (Attribute att, Entity e, 
		  Document d){
	  
	  Attribute match = null;
	  // Looks for attributes modifying entity 'e' that match attribute 'att'
	  for (Attribute a : d.getAttributes()){
		  // check if attribute 'a' modifies entity 'e'
		  String entityID = a.getList().get(0);
		  if(e.getId().equals(entityID)){
			  //check if attribute 'a' matches attribute 'att' (same attribute type and value)
			  if(a.getValue().equals(att.getValue()) && 
					  a.getList().get(1).equals(att.getList().get(1))){
				  match = a;
				  break;
			  }
		  }
	  }
	  return match;
  }
  
  
  
  
  
  
  
}
package au.com.nicta.csp.brateval;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 
 * BRAT stand-off relation comparison
 * 
 * @author Antonio Jimeno Yepes (antonio.jimeno@nicta.com.au)
 *
 */
public class CompareRelations
{

  public static void main (String argc []) throws IOException
  {
	String folder1 = argc[0];
	String folder2 = argc[1];	
	boolean exact_match = Boolean.parseBoolean(argc[2]);
	boolean verbose = Boolean.parseBoolean(argc[3]);	

	Set <String> relationTypes = new TreeSet <String> ();

	Map <String, Integer> relationTP = new HashMap <String, Integer> ();
	Map <String, Integer> relationFP = new HashMap <String, Integer> ();
	Map <String, Integer> relationFN = new HashMap <String, Integer> ();

	Map <String, Integer> relationMissingFP = new HashMap <String, Integer> ();
	Map <String, Integer> relationMissingFN = new HashMap <String, Integer> ();

    File folder = new File(folder1);

    for (File file : folder.listFiles())
    {
      if (file.getName().endsWith(".ann"))
      {
        Map <String, RelationComparison> relations = new TreeMap <String, RelationComparison> ();

        Document d1 = Annotations.read(file.getAbsolutePath(), "ann");
        Document d2 = Annotations.read(folder2 + File.separator +  file.getName(), "ann");

    	// TPs and FPs
  	    for (Relation rel : d1.getRelations())
    	{
  	      if (relations.get(rel.getRelationType()) == null)
  	      { relations.put(rel.getRelationType(), new RelationComparison()); }

  	      if  (exact_match)
  	      {
            if (d2.findRelation(rel) != null)
            { relations.get(rel.getRelationType()).addTP(rel); }
            else
            { relations.get(rel.getRelationType()).addFP(rel); }
   	      }
  	      else
  	      {
            if (d2.findRelationOverlap(rel) != null)
            { relations.get(rel.getRelationType()).addTP(rel); }
            else
            { relations.get(rel.getRelationType()).addFP(rel); }
  	      }
    	}

    	// FNs
  	    for (Relation rel : d2.getRelations())
    	{
  	      if (relations.get(rel.getRelationType()) == null)
   	      { relations.put(rel.getRelationType(), new RelationComparison()); }

          if (d1.findRelationOverlap(rel) == null)
          { relations.get(rel.getRelationType()).addFN(rel); }
        }

        for (Map.Entry <String, RelationComparison> entry : relations.entrySet())
        {
          relationTypes.add(entry.getKey());

          for (Relation rel : entry.getValue().getTP())
          {
            if (verbose)
            {
        	  System.out.println(file.getName());
          	  System.out.println("TP " + rel.getRelationType());
              System.out.println(rel.getEntity1());
              System.out.println(rel.getEntity2());
              System.out.println("------");
        	}
          }

          for (Relation rel : entry.getValue().getFN())
          {
            if (verbose)
            {
              System.out.println(file.getName());
        	  System.out.println("FN " + rel.getRelationType());
        	  System.out.println(rel.getEntity1());
              System.out.println(rel.getEntity2());
              System.out.println("------");
          	} 

            if (exact_match)
            {
              if (!(d1.findEntity(rel.getEntity1()) != null && d1.findEntity(rel.getEntity2()) != null))
              {
                if (relationMissingFN.get(rel.getRelationType()) == null)
                { relationMissingFN.put(rel.getRelationType(), 1); }
                else
                { relationMissingFN.put(rel.getRelationType(), relationMissingFN.get(rel.getRelationType()) + 1); }
              }
            }
            else
            {
              if (!(d1.findEntityOverlap(rel.getEntity1()) != null && d1.findEntityOverlap(rel.getEntity2()) != null))
              {
                if (relationMissingFN.get(rel.getRelationType()) == null)
                { relationMissingFN.put(rel.getRelationType(), 1); }
                else
                { relationMissingFN.put(rel.getRelationType(), relationMissingFN.get(rel.getRelationType()) + 1); }
              }
            }
          }

          for (Relation rel : entry.getValue().getFP())
          { 
            if (verbose)
            {
          	  System.out.println(file.getName());
        	  System.out.println("FP " + rel.getRelationType());
        	  System.out.println(rel.getEntity1());
              System.out.println(rel.getEntity2());
              System.out.println("------");
          	}

            if (exact_match)
            {
              if (!(d2.findEntity(rel.getEntity1()) != null && d2.findEntity(rel.getEntity2()) != null))
              {
                if (relationMissingFP.get(rel.getRelationType()) == null)
                { relationMissingFP.put(rel.getRelationType(), 1); }
                else
                { relationMissingFP.put(rel.getRelationType(), relationMissingFP.get(rel.getRelationType()) + 1); }
              }
            }
            else
            {
              if (!(d2.findEntityOverlap(rel.getEntity1()) != null && d2.findEntityOverlap(rel.getEntity2()) != null))
              {
                if (relationMissingFP.get(rel.getRelationType()) == null)
                { relationMissingFP.put(rel.getRelationType(), 1); }
                else
                { relationMissingFP.put(rel.getRelationType(), relationMissingFP.get(rel.getRelationType()) + 1); }
              }
            }
      	  }

       	  // Overall counting
          if (relationTP.get(entry.getKey()) == null)
          { relationTP.put(entry.getKey(), entry.getValue().getTP().size()); }
          else
          { relationTP.put(entry.getKey(), relationTP.get(entry.getKey()) + entry.getValue().getTP().size());}
        	
          if (relationFP.get(entry.getKey()) == null)
          { relationFP.put(entry.getKey(), entry.getValue().getFP().size()); }
          else
          { relationFP.put(entry.getKey(), relationFP.get(entry.getKey()) + entry.getValue().getFP().size());}

          if (relationFN.get(entry.getKey()) == null)
          { relationFN.put(entry.getKey(), entry.getValue().getFN().size()); }
          else
          { relationFN.put(entry.getKey(), relationFN.get(entry.getKey()) + entry.getValue().getFN().size());}
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
	           + "\tFPM"
	           + "\tFNM"
	           );
    int allTP = 0;
    int allFP = 0;
    int allFN = 0;
    for (String rt : relationTypes)
    {
      int TP = (relationTP.get(rt) == null ? 0 : relationTP.get(rt));
      int FP = (relationFP.get(rt) == null ? 0 : relationFP.get(rt));
      int FN = (relationFN.get(rt) == null ? 0 : relationFN.get(rt));

      double precision = 0;
      double recall = 0;
      double f_measure = 0;
      
      allTP+=TP;
      allFP+=FP;
      allFN+=FN;

      if (TP+FP > 0) { precision = (double)TP/(TP+FP); }

      if (TP+FN > 0) { recall = (double)TP/(TP+FN); }

      if ((precision+recall) > 0)
      { f_measure = (2*precision*recall)/(double)(precision+recall); }

      System.out.println(rt
    		           + "\t" + (relationTP.get(rt) == null ? 0 : relationTP.get(rt)) 
    		           + "\t" + (relationFP.get(rt) == null ? 0 : relationFP.get(rt)) 
    		           + "\t" + (relationFN.get(rt) == null ? 0 : relationFN.get(rt))
       		           + "\t" + String.format("%1.4f", precision)
    		           + "\t" + String.format("%1.4f", recall)
    		           + "\t" + String.format("%1.4f", f_measure)
    		           + "\t" + (relationMissingFP.get(rt) == null ? 0 : relationMissingFP.get(rt))
    		           + "\t" + (relationMissingFN.get(rt) == null ? 0 : relationMissingFN.get(rt)));    	
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
}
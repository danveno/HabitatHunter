package edu.arizona.biosemantics.habitat.ontomap;

import edu.arizona.biosemantics.habitat.ontology.OntologyTerm;


/**
 * map a term into classes of the ontology
 * @author maojin
 *
 */
public interface IOntologyMapping {
	public OntologyTerm exactMatch(String termTxt);
}

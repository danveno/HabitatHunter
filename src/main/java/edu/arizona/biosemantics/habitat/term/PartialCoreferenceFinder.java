package edu.arizona.biosemantics.habitat.term;

import java.util.List;
import java.util.Set;

import edu.arizona.biosemantics.bb.BBEntity;


/**
 * find partial coreference, refer to the same taxon using a higher and shorter taxon name
 * 
 * @author maojin
 *
 */
public class PartialCoreferenceFinder {
	
	
	/*
	 * find the original term for an acronym
	 */
	public BBEntity referredEntity(BBEntity entity, List<BBEntity> docEntityList) {
		String entityName = entity.getName();
		for(BBEntity docEntity : docEntityList){
			String docEntityName = docEntity.getName();
			if(!docEntityName.equals(entityName)){
				if(docEntityName.lastIndexOf(entityName)>-1){
					return docEntity;
				}
			}
		}
		return null;
	}
}

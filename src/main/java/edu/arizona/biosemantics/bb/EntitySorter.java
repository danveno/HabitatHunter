package edu.arizona.biosemantics.bb;

import java.util.Comparator;

public class EntitySorter implements Comparator<BBEntity>{

	@Override
	public int compare(BBEntity arg0, BBEntity arg1) {
		// TODO Auto-generated method stub
		return arg0.getStart()-arg1.getStart();
	}

}

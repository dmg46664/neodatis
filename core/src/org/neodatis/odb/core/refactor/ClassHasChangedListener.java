/**
 * 
 */
package org.neodatis.odb.core.refactor;

import org.neodatis.odb.core.event.EventResult;
import org.neodatis.odb.core.event.MetaModelHasChangedEvent;
import org.neodatis.odb.core.event.NeoDatisEvent;
import org.neodatis.odb.core.event.NeoDatisEventListener;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;

/**
 * @author olivier
 *
 */
public abstract class ClassHasChangedListener implements NeoDatisEventListener{
	
	
	/**
	 * 
	 * @param oldCI The old class info (the one stored in the database)
	 * @param newCI The new class info, the one that represents the current java class
	 * @param result The comparison result
	 * @return true to continue, false, abort
	 */
	public abstract boolean hasChanged(ClassInfo oldCI, ClassInfo newCI, ClassInfoCompareResult result);
	public abstract boolean hasNotChanged(ClassInfo oldCI, ClassInfo newCI, ClassInfoCompareResult result);

	public EventResult onEvent(NeoDatisEvent event) {
		MetaModelHasChangedEvent e = (MetaModelHasChangedEvent) event;
		boolean b = true;
		for(int i=0;i<e.getCheckMetaModelResult().size();i++){
			ClassInfoCompareResult r = e.getCheckMetaModelResult().getResults().get(i);
			
			if(r.hasChanged()){
				b = b && hasChanged(r.getOldCI(), r.getNewCI(), r);	
			}else{
				b = b && hasNotChanged(r.getOldCI(), r.getNewCI(), r);
			}
		}
		return new EventResult(b);
	}

}

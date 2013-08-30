/**
 * 
 */
package org.neodatis.odb.core.event;

import org.neodatis.odb.NeoDatisEventType;
import org.neodatis.odb.core.refactor.CheckMetaModelResult;

/**
 * @author olivier
 *
 */
public class MetaModelHasChangedEvent extends NeoDatisEvent{
	protected CheckMetaModelResult checkMetaModelResult;
	
	public MetaModelHasChangedEvent(CheckMetaModelResult result){
		super(NeoDatisEventType.META_MODEL_HAS_CHANGED);
		this.checkMetaModelResult = result;
	}

	public CheckMetaModelResult getCheckMetaModelResult() {
		return checkMetaModelResult;
	}
	
}

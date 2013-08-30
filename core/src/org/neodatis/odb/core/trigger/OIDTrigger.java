/**
 * 
 */
package org.neodatis.odb.core.trigger;

import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.session.ExecutionType;

/**
 * @author olivier
 *
 */
public abstract class OIDTrigger extends Trigger{
	abstract public void setOid(final Object o, ObjectOid oid);
	
	@Override
	public int getExecutionType() {
		return ExecutionType.CLIENT;
	}
}

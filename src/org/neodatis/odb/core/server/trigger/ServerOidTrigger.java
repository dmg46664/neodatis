package org.neodatis.odb.core.server.trigger;

import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.ObjectRepresentation;
import org.neodatis.odb.core.session.ExecutionType;
import org.neodatis.odb.core.trigger.OIDTrigger;

public abstract class ServerOidTrigger extends OIDTrigger{

	@Override
	public void setOid(Object o, ObjectOid oid) {
		if(o instanceof ObjectRepresentation){
			setOid((ObjectRepresentation)o, oid);
		}
		// there is a specific case (when using remote process invocation where server triggers may be called on user objects. This situation must be ignored. 
		// Check ExecutionType for more details
		
	}
	
	public abstract void setOid(ObjectRepresentation or, ObjectOid oid);

    @Override
    public int getExecutionType() {
    	return ExecutionType.SERVER;
    }

}

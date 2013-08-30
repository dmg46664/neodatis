/**
 * 
 */
package org.neodatis.odb.core.server;

import java.lang.reflect.Field;

import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.core.layers.layer1.ClassIntrospector;
import org.neodatis.odb.core.server.trigger.ChangedValueNotification;

/**
 * Used when a server side trigger has changed a value of an object of the server side
 * @author olivier
 *
 */
public class ChangedValueProcessor implements ReturnValueProcessor {
	protected ClassIntrospector classIntrospector;
	
	public ChangedValueProcessor(ClassIntrospector classIntrospector){
		this.classIntrospector = classIntrospector;
	}

	public void process(ReturnValue rv, Object object) throws Exception {
		Field f = null;
		ChangedValueNotification cvn = null;;
		try{
			// only manage ChangedValueNotification
			if(rv==null || !(rv instanceof ChangedValueNotification)){
				return;
			}
			cvn = (ChangedValueNotification) rv;
			
			
			
			// Get the object class
			Class c = object.getClass();
			// Get the field that is to be changed
			f = classIntrospector.getField(c, cvn.getAttributeName());
			// Tells java to let us update the field even if it private
			f.setAccessible(true);
			// set the new value
			f.set(object, cvn.getValue());
			
		}catch (IllegalArgumentException e) {
			if(cvn==null || cvn.getValue()==null){
				throw e;
			}
			throw new NeoDatisRuntimeException(e, "Class "+  object.getClass().getName()+  " : old field type is " + f.getType().getName() + " - new value type = " + cvn.getValue().getClass().getName() );
		}
	}

}

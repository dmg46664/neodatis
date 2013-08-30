package org.neodatis.odb.core.trigger;

import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;

public interface TriggerManager extends Triggers {
	public static final String ALL_CLASS_TRIGGER = "__all_class_";
	
	public abstract boolean manageInsertTriggerBefore(String className, Object object);

	public abstract void manageInsertTriggerAfter(String className, final Object object, ObjectOid oid);

	public abstract boolean manageUpdateTriggerBefore(String className, NonNativeObjectInfo oldObjectRepresentation, Object newObject, final ObjectOid oid);

	public abstract void manageUpdateTriggerAfter(String className, final NonNativeObjectInfo oldObjectRepresentation, Object newObject, final ObjectOid oid);

	public abstract boolean manageDeleteTriggerBefore(String className, final Object object, final ObjectOid oid);

	public abstract void manageDeleteTriggerAfter(String className, final Object object, final ObjectOid oid);

	public abstract void manageSelectTriggerAfter(String className, final Object object, final ObjectOid oid);


	/**
	 * used to transform object before real trigger call. This is used for
	 * example, in server side trigger where the object is encapsulated in an
	 * ObjectRepresentation instance. It is only for internal use
	 */
	public Object transform(Object object, Trigger trigger);

	public boolean manageOidTrigger(String className,  Object object, ObjectOid oid);

	/** to disable all triggers*/
	public abstract void disableTriggers();

	


}
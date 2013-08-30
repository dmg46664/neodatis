package org.neodatis.odb.core.trigger;

import java.util.List;

import org.neodatis.tool.wrappers.list.IOdbList;

/** An interface for a container of all triggers
 * 
 * @author olivier
 *
 */
public interface Triggers {


	public void addUpdateTriggerFor(String className, UpdateTrigger trigger);

	public void addInsertTriggerFor(String className, InsertTrigger trigger);

	public void addDeleteTriggerFor(String className, DeleteTrigger trigger);

	public void addSelectTriggerFor(String className, SelectTrigger trigger);
	
	public void addOidTriggerFor(String className, OIDTrigger trigger);
	
	public void removeOidTrigger(String className, OIDTrigger trigger);


	public boolean hasOidTriggersFor(String classsName);

	public boolean hasDeleteTriggersFor(String classsName);

	public boolean hasInsertTriggersFor(String className);

	public boolean hasSelectTriggersFor(String className);

	public boolean hasUpdateTriggersFor(String className);

	public IOdbList<OIDTrigger> getListOfOidTriggersFor(String className);

	/**
	 * FIXME try to cache l1+l2
	 * 
	 * @param className
	 * @return
	 */
	public IOdbList<Trigger> getListOfDeleteTriggersFor(String className);

	public IOdbList<InsertTrigger> getListOfInsertTriggersFor(String className);

	public IOdbList<Trigger> getListOfSelectTriggersFor(String className);

	public IOdbList<Trigger> getListOfUpdateTriggersFor(String className);
	
	/** Check if triggers must be called for className*/
	boolean callTriggerOnClass(String className);
	/** Resets the list of classes on which triggers are not called*/
	void resetClassesNotToCallTriggersOn();
	/** Add somes classes to the list of classes on which triggers are not called*/
	void addClassesNotToCallTriggersOn(List<Class> classes);
	

}
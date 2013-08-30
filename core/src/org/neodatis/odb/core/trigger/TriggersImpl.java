/*
 NeoDatis ODB : Native Object Database (odb.info@neodatis.org)
 Copyright (C) 2007 NeoDatis Inc. http://www.neodatis.org

 "This file is part of the NeoDatis ODB open source object database".

 NeoDatis ODB is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 NeoDatis ODB is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.neodatis.odb.core.trigger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neodatis.tool.wrappers.list.IOdbList;
import org.neodatis.tool.wrappers.list.OdbArrayList;
import org.neodatis.tool.wrappers.map.OdbHashMap;

public class TriggersImpl implements Triggers {

	static final String ALL_CLASS_TRIGGER = "__all_class_";


	/** key is class Name, value is the collection of triggers for the class */
	protected Map<String,IOdbList<Trigger>> listOfUpdateTriggers;
	/** key is class Name, value is the collection of triggers for the class */
	protected Map<String,IOdbList<InsertTrigger>> listOfInsertTriggers;
	/** key is class Name, value is the collection of triggers for the class */
	protected Map<String,IOdbList<Trigger>> listOfDeleteTriggers;
	/** key is class Name, value is the collection of triggers for the class */
	protected Map<String,IOdbList<Trigger>> listOfSelectTriggers;
	/** key is class Name, value is the collection of triggers for the class */
	protected Map<String,IOdbList<OIDTrigger>> listOfOIdTriggers;
	
	/** map with ClassName,Class to specify the classes on which we must not call triggers*/
	protected Map<String, Class> classesNotToCallTriggersOn;


	public TriggersImpl() {
		init();
	}

	/**
	 * 
	 */
	private void init() {
		listOfUpdateTriggers = new OdbHashMap<String, IOdbList<Trigger>>();
		listOfDeleteTriggers = new OdbHashMap<String, IOdbList<Trigger>>();
		listOfSelectTriggers = new OdbHashMap<String, IOdbList<Trigger>>();
		listOfInsertTriggers = new OdbHashMap<String, IOdbList<InsertTrigger>>();
		listOfOIdTriggers = new OdbHashMap<String, IOdbList<OIDTrigger>>();
		classesNotToCallTriggersOn = new HashMap<String, Class>();
	}

	public void addUpdateTriggerFor(String className, UpdateTrigger trigger) {
		if (className == null) {
			className = ALL_CLASS_TRIGGER;
		}
		IOdbList<Trigger> c =  listOfUpdateTriggers.get(className);
		if (c == null) {
			c = new OdbArrayList<Trigger>();
			listOfUpdateTriggers.put(className, c);
		}
		c.add(trigger);
	}

	public void addInsertTriggerFor(String className, InsertTrigger trigger) {
		if (className == null) {
			className = ALL_CLASS_TRIGGER;
		}
		IOdbList<InsertTrigger> c = listOfInsertTriggers.get(className);
		if (c == null) {
			c = new OdbArrayList<InsertTrigger>();
			listOfInsertTriggers.put(className, c);
		}
		c.add(trigger);
	}

	public void addDeleteTriggerFor(String className, DeleteTrigger trigger) {
		if (className == null) {
			className = ALL_CLASS_TRIGGER;
		}
		IOdbList<Trigger> c = listOfDeleteTriggers.get(className);
		if (c == null) {
			c = new OdbArrayList<Trigger>();
			listOfDeleteTriggers.put(className, c);
		}
		c.add(trigger);
	}

	public void addSelectTriggerFor(String className, SelectTrigger trigger) {
		if (className == null) {
			className = ALL_CLASS_TRIGGER;
		}
		IOdbList<Trigger> c = listOfSelectTriggers.get(className);
		if (c == null) {
			c = new OdbArrayList<Trigger>();
			listOfSelectTriggers.put(className, c);
		}
		c.add(trigger);
	}

	public boolean hasOidTriggersFor(String classsName) {
		return listOfOIdTriggers.containsKey(classsName) || listOfOIdTriggers.containsKey(ALL_CLASS_TRIGGER);
	}

	public boolean hasDeleteTriggersFor(String classsName) {
		return listOfDeleteTriggers.containsKey(classsName) || listOfDeleteTriggers.containsKey(ALL_CLASS_TRIGGER);
	}

	public boolean hasInsertTriggersFor(String className) {
		return listOfInsertTriggers.containsKey(className) || listOfInsertTriggers.containsKey(ALL_CLASS_TRIGGER);
	}

	public boolean hasSelectTriggersFor(String className) {
		return listOfSelectTriggers.containsKey(className) || listOfSelectTriggers.containsKey(ALL_CLASS_TRIGGER);
	}

	public boolean hasUpdateTriggersFor(String className) {
		return listOfUpdateTriggers.containsKey(className) || listOfUpdateTriggers.containsKey(ALL_CLASS_TRIGGER);
	}

	public IOdbList<OIDTrigger> getListOfOidTriggersFor(String className) {
		IOdbList<OIDTrigger> l1 = listOfOIdTriggers.get(className);
		IOdbList<OIDTrigger> l2 = listOfOIdTriggers.get(ALL_CLASS_TRIGGER);

		if (l2 != null) {
			int size = l2.size();
			if (l1 != null) {
				size = size + l1.size();
			}
			IOdbList<OIDTrigger> r = new OdbArrayList<OIDTrigger>(size);
			if (l1 != null) {
				r.addAll(l1);
			}
			r.addAll(l2);
			return r;
		}


		return l1;
	}
	
	/**
	 * FIXME try to cache l1+l2
	 * 
	 * @param className
	 * @return
	 */
	public IOdbList<Trigger> getListOfDeleteTriggersFor(String className) {
		IOdbList<Trigger> l1 = listOfDeleteTriggers.get(className);
		IOdbList<Trigger> l2 = listOfDeleteTriggers.get(ALL_CLASS_TRIGGER);

		if (l2 != null) {
			int size = l2.size();
			if (l1 != null) {
				size = size + l1.size();
			}
			IOdbList<Trigger> r = new OdbArrayList<Trigger>(size);
			if (l1 != null) {
				r.addAll(l1);
			}
			r.addAll(l2);
			return r;
		}

		return l1;
	}

	public IOdbList<InsertTrigger> getListOfInsertTriggersFor(String className) {
		IOdbList<InsertTrigger> l1 = listOfInsertTriggers.get(className);
		IOdbList<InsertTrigger> l2 = listOfInsertTriggers.get(ALL_CLASS_TRIGGER);

		if (l2 != null) {
			int size = l2.size();
			if (l1 != null) {
				size = size + l1.size();
			}
			IOdbList<InsertTrigger> r = new OdbArrayList<InsertTrigger>(size);
			if (l1 != null) {
				r.addAll(l1);
			}
			r.addAll(l2);
			return r;
		}


		return l1;
	}

	public IOdbList<Trigger> getListOfSelectTriggersFor(String className) {
		IOdbList<Trigger> l1 = listOfSelectTriggers.get(className);
		IOdbList<Trigger> l2 = listOfSelectTriggers.get(ALL_CLASS_TRIGGER);
		if (l2 != null) {
			int size = l2.size();
			if (l1 != null) {
				size = size + l1.size();
			}
			IOdbList<Trigger> r = new OdbArrayList<Trigger>(size);
			if (l1 != null) {
				r.addAll(l1);
			}
			r.addAll(l2);
			return r;
		}


		return l1;
	}

	public IOdbList<Trigger> getListOfUpdateTriggersFor(String className) {
		IOdbList<Trigger> l1 = listOfUpdateTriggers.get(className);
		IOdbList<Trigger> l2 = listOfUpdateTriggers.get(ALL_CLASS_TRIGGER);

		if (l2 != null) {
			int size = l2.size();
			if (l1 != null) {
				size = size + l1.size();
			}
			IOdbList<Trigger> r = new OdbArrayList<Trigger>(size);
			if (l1 != null) {
				r.addAll(l1);
			}
			r.addAll(l2);
			return r;
		}


		return l1;
	}

	public void addOidTriggerFor(String className, OIDTrigger trigger) {
		if (className == null) {
			className = ALL_CLASS_TRIGGER;
		}
		IOdbList<OIDTrigger> c =  listOfOIdTriggers.get(className);
		if (c == null) {
			c = new OdbArrayList<OIDTrigger>();
			listOfOIdTriggers.put(className, c);
		}
		c.add(trigger);
	}

	public void removeOidTrigger(String className, OIDTrigger trigger) {
		if (className == null) {
			className = ALL_CLASS_TRIGGER;
		}
		IOdbList<OIDTrigger> c =  listOfOIdTriggers.get(className);
		if (c == null) {
			return;
		}
		c.remove(trigger);		
	}
	public boolean callTriggerOnClass(String className) {
		return !classesNotToCallTriggersOn.containsKey(className);
	}

	public void resetClassesNotToCallTriggersOn(){
		classesNotToCallTriggersOn.clear();
	}
	public void addClassesNotToCallTriggersOn(List<Class> classes){
		for(Class c:classes){
			classesNotToCallTriggersOn.put(c.getName(),c);
		}
	}
	
}

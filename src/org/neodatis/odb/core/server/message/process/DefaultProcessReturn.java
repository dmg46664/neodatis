package org.neodatis.odb.core.server.message.process;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DefaultProcessReturn implements RemoteProcessReturn {
	protected Map<String, Serializable> map;
	
	public DefaultProcessReturn(){
		map = new HashMap<String, Serializable>();
	}
	public DefaultProcessReturn(String parameterName, Serializable value){
		this();
		map.put(parameterName, value);
	}
	public Serializable getValue(String parameterName){
		return map.get(parameterName);
	}

	public void setValue(String parameterName, Serializable value){
		map.put(parameterName,value);
	}

}

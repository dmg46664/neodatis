
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
package org.neodatis.odb.core.server.message;

import java.io.Serializable;

public abstract class Message implements Serializable{
	
	private int messageType;
	private long dateTime;
	private String baseIdentifier;
	private String sessionId;
	private String error;
	private transient String clientIp;
	
	public Message(int messageType, String baseId,String sessionId){
		this.dateTime = System.currentTimeMillis();
		this.messageType = messageType;
		this.baseIdentifier = baseId;
		this.sessionId = sessionId;
	}


	/**
	 * 
	 */
	public Message() {
	}


	public void setMessageType(int commandId) {
		this.messageType = commandId;
	}


	public void setBaseIdentifier(String baseIdentifier) {
		this.baseIdentifier = baseIdentifier;
	}


	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}


	public int getMessageType(){
		return messageType;
	}

	public String getBaseIdentifier() {
		return baseIdentifier;
	}

	public String getSessionId() {
		return sessionId;
	}


	public String getError() {
		return error;
	}


	public void setError(String error) {
		this.error = error;
	}
	public boolean hasError(){
		return error!=null && error.length()!=0;
	}


	public long getDateTime() {
		return dateTime;
	}


	public void setDateTime(long dateTime) {
		this.dateTime = dateTime;
	}


	public String getClientIp() {
		return clientIp;
	}


	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}
	
}

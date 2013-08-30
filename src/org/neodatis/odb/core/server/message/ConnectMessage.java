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

import org.neodatis.tool.wrappers.OdbTime;

public class ConnectMessage extends Message{

	private String ip;
	private long dateTime;
	private String user;
	private String password;
	private boolean transactional;
	/** an optional user info*/
	private String userInfo;
	public ConnectMessage(String baseId, String sessionId, String ip,String user,String password, boolean transactional) {
		super(MessageType.CONNECT,baseId,sessionId);
		this.ip = ip;
		this.dateTime = OdbTime.getCurrentTimeInMs();
		this.user = user;
		this.password = password;
		this.transactional = transactional;
	}
	/**
	 * 
	 */
	public ConnectMessage() {
		super();
	}
	public long getDateTime() {
		return dateTime;
	}
	
	public String getIp() {
		return ip;
	}
	public String toString() {
		return "Connect";
	}
	public String getPassword() {
		return password;
	}
	public String getUser() {
		return user;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public void setDateTime(long dateTime) {
		this.dateTime = dateTime;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean isTransactional() {
		return transactional;
	}
	public void setTransactional(boolean transactional) {
		this.transactional = transactional;
	}
	public String getUserInfo() {
		return userInfo;
	}
	public void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
	}
	
}

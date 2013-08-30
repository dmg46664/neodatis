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



/**
 * to retrieve files from server
 * 
 * @author olivier s
 * 
 */
public class GetFileMessageResponse extends Message {
	protected boolean fileExist;
	protected long fileSize;
	
	private String localFileName;
	private String remoteFileName;
	private boolean getFileInServerInbox;
	private boolean putFileInClientInbox;

	public GetFileMessageResponse() {
		super();
	}

	public GetFileMessageResponse(String baseId, String connectionId, String error) {
		super(MessageType.GET_FILE_RESPONSE, baseId,connectionId);
		setError(error);
	}
	public GetFileMessageResponse(String baseId, String sessionId, boolean getFileInServerInbox, String remoteFileName, boolean putFileInClientInbox, String localFileName) {
		super(MessageType.GET_FILE_RESPONSE, baseId,sessionId);
		this.localFileName = localFileName;
		this.remoteFileName = remoteFileName;
		this.getFileInServerInbox = getFileInServerInbox;
		this.putFileInClientInbox = putFileInClientInbox;
	}

	public boolean fileExist() {
		return fileExist;
	}

	public void setFileExist(boolean fileExist) {
		this.fileExist = fileExist;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}


	public String getLocalFileName() {
		return localFileName;
	}
	public void setLocalFileName(String localFileName) {
		this.localFileName = localFileName;
	}
	public String getRemoteFileName() {
		return remoteFileName;
	}
	public void setRemoteFileName(String remoteFileName) {
		this.remoteFileName = remoteFileName;
	}
	public boolean isGetFileInServerInbox() {
		return getFileInServerInbox;
	}
	public void setGetFileInServerInbox(boolean getFileInServerInbox) {
		this.getFileInServerInbox = getFileInServerInbox;
	}
	public boolean isPutFileInClientInbox() {
		return putFileInClientInbox;
	}
	public void setPutFileInClientInbox(boolean putFileInClientInbox) {
		this.putFileInClientInbox = putFileInClientInbox;
	}
	

}

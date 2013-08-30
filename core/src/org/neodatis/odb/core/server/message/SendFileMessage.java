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


/** A FileMessage used to let the NeoDatis client send o file to the server
 * 
 * @author olivier s
 *
 */
public class SendFileMessage extends Message {
	private String localFileName;
	private String remoteFileName;
	private boolean putFileInServerInbox;
	
	public SendFileMessage(){
		super();
	}
	public SendFileMessage(String localFileName, String remoteFileName, boolean putFileInServerInbox){
		super(MessageType.SEND_FILE, null,null);
		this.localFileName = localFileName;
		this.remoteFileName = remoteFileName;
		this.putFileInServerInbox = putFileInServerInbox;
	}

	public SendFileMessage(String baseId, String sessionId,String localFileName, String remoteFileName, boolean putFileInServerInbox){
		super(MessageType.SEND_FILE, baseId,sessionId);
		this.localFileName = localFileName;
		this.remoteFileName = remoteFileName;
		this.putFileInServerInbox = putFileInServerInbox;
	}
	
	public SendFileMessage(String baseId, String sessionId,String localFileName){
		super(MessageType.SEND_FILE, baseId,sessionId);
		this.localFileName = localFileName;
		this.putFileInServerInbox = true;
	}

	
	public String toString() {
		return "SendFile";
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
	public boolean isPutFileInServerInbox() {
		return putFileInServerInbox;
	}
	public void setPutFileInServerInbox(boolean putFileInServerInbox) {
		this.putFileInServerInbox = putFileInServerInbox;
	}
	
}

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
package org.neodatis.odb.core.server.message.process;

import org.neodatis.odb.core.server.message.Message;
import org.neodatis.odb.core.server.message.MessageType;

/** A message to send remote process to the server
 * 
 * @author olivier s
 *
 */
public class RemoteProcessMessage extends Message {
	private RemoteProcess process;
	private boolean isSynchronous;
	
	
	public RemoteProcessMessage(){
		super();
	}
	public RemoteProcessMessage(RemoteProcess process, boolean sync){
		super(MessageType.REMOTE_PROCESS, null,null);
		this.process = process;
		this.isSynchronous = sync;
	}

	public RemoteProcessMessage(String baseId, String sessionId, RemoteProcess process, boolean sync){
		super(MessageType.REMOTE_PROCESS, baseId,sessionId);
		this.process = process;
		this.isSynchronous = sync;
	}
	
	public String toString() {
		return "RemoteProcess "+ getClass().getName();
	}
	public RemoteProcess getProcess() {
		return process;
	}
	public void setProcess(RemoteProcess process) {
		this.process = process;
	}
	public boolean isSynchronous() {
		return isSynchronous;
	}
	public void setSynchronous(boolean isSynchronous) {
		this.isSynchronous = isSynchronous;
	}
	
}

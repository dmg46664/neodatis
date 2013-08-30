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

import org.neodatis.odb.core.query.InternalQuery;

public class GetObjectsMessage extends Message {
	private InternalQuery query;
	private int startIndex;
	private int endIndex;
	private boolean inMemory;
	public GetObjectsMessage(){
		super();
	}
	public GetObjectsMessage(String baseId, String connectionId, InternalQuery query, boolean inMemory,int startIndex, int endIndex){
		super(MessageType.GET_OBJECTS, baseId,connectionId);
		this.query = query;		
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.inMemory= inMemory;
	}

	public GetObjectsMessage(String baseId, String connectionId, InternalQuery query){
		this(baseId,connectionId,query,true,-1,-1);
	}

	public InternalQuery getQuery() {
		return query;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public boolean isInMemory() {
		return inMemory;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public String toString() {
		return "GetObjects";
	}
	/**
	 * @param q
	 */
	public void setQuery(InternalQuery q) {
		this.query = q;
	}
	/**
	 * @param startIndex2
	 */
	public void setStartIndex(int startIndex2) {
		this.startIndex = startIndex2;
		
	}
	/**
	 * @param endIndex2
	 */
	public void setEndIndex(int endIndex2) {
		this.endIndex = endIndex2;
		
	}
	/**
	 * @param inMemory2
	 */
	public void setInMemory(boolean inMemory2) {
		this.inMemory = inMemory2;
	}
}

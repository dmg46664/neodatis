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

import java.util.Collection;

import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer3.OidAndBytes;
import org.neodatis.odb.core.query.IQueryExecutionPlan;
import org.neodatis.tool.wrappers.list.IOdbList;
/**
 * A response to a GetMessage comamnd
 * @author olivier s
 *
 */

public class GetObjectsMessageResponse extends Message {
	/**  List of meta representation of the objects*/
	private Collection<IOdbList<OidAndBytes>> listOfOabs;
	private Collection<ObjectOid> objectOids;
	private boolean onlyOids;
	private IQueryExecutionPlan plan;
	public GetObjectsMessageResponse(){
		super();
	}
	public GetObjectsMessageResponse(String baseId, String sessionId, String error){
		super(MessageType.GET_OBJECTS_RESPONSE, baseId,sessionId);
		setError(error);
	}

	public GetObjectsMessageResponse(String baseId, String sessionId, Collection<IOdbList<OidAndBytes>> listOfOabs, IQueryExecutionPlan plan, boolean onlyOids){
		super(MessageType.GET_OBJECTS_RESPONSE, baseId,sessionId);
		this.listOfOabs = listOfOabs;
		this.plan = plan;
		this.onlyOids = false;
	}

	public GetObjectsMessageResponse(String baseId, String sessionId, Collection<ObjectOid> objectOids, IQueryExecutionPlan plan){
		super(MessageType.GET_OBJECTS_RESPONSE, baseId,sessionId);
		this.objectOids =objectOids;
		this.plan = plan;
		this.onlyOids = true;
	}

	public Collection<IOdbList<OidAndBytes>> getListOfOabs() {
		return listOfOabs;
	}

	public IQueryExecutionPlan getPlan() {
		return plan;
	}
	public boolean isOnlyOids() {
		return onlyOids;
	}
	public void setOnlyOids(boolean onlyOids) {
		this.onlyOids = onlyOids;
	}
	/**
	 * @param plan
	 */
	public void setQueryExecutionPlan(IQueryExecutionPlan plan) {
		this.plan = plan;
	}
	public Collection<ObjectOid> getObjectOids() {
		return objectOids;
	}
	public void setObjectOids(Collection<ObjectOid> objectOids) {
		this.objectOids = objectOids;
	}
	/**
	 * @param listOfOabs
	 */
	public void setListOfOabs(Collection<IOdbList<OidAndBytes>> listOfOabs) {
		this.listOfOabs = listOfOabs;
	}
	
}

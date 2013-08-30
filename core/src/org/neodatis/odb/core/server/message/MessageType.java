
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

public class MessageType implements Serializable{
	
	public static final int CONNECT = 10;
	public static final int CONNECT_RESPONSE = 11 ;
	public static final int GET_OBJECTS = 20;
	public static final int GET_OBJECTS_RESPONSE = 21;
	public static final int GET_OBJECT_FROM_ID = 30;
	public static final int GET_OBJECT_FROM_ID_RESPONSE = 31;
	public static final int STORE_OBJECT = 40;
	public static final int STORE_OBJECT_RESPONSE = 41;
	public static final int DELETE_OBJECT = 50;
	public static final int DELETE_OBJECT_RESPONSE = 51;
	public static final int CLOSE = 60;
	public static final int CLOSE_RESPONSE = 61;
	public static final int COMMIT = 70;
	public static final int COMMIT_RESPONSE = 71;
	public static final int ROLLBACK = 80;
	public static final int ROLLBACK_RESPONSE = 81;
	public static final int DELETE_BASE = 90;
	public static final int GET_SESSIONS = 100;
	public static final int ADD_UNIQUE_INDEX = 110;
	public static final int ADD_UNIQUE_INDEX_RESPONSE = 111;
	public static final int ADD_CLASS_INFO_LIST = 120;
	public static final int COUNT = 130;
	public static final int COUNT_RESPONSE = 131;
	public static final int GET_OBJECT_VALUES = 140;
	public static final int GET_OBJECT_HEADER_FROM_ID = 150;
	public static final int REBUILD_INDEX = 160;
	public static final int DELETE_INDEX = 170;
	public static final int CHECK_META_MODEL_COMPATIBILITY = 180;
	public static final int GET_NEXT_OBJECT_OID = 190;
	public static final int GET_NEXT_OBJECT_OID_RESPONSE = 191;
	public static final int GET_NEXT_CLASS_OID = 200;
	public static final int GET_NEXT_CLASS_OID_RESPONSE = 201;
	public static final int NEXT_CLASS_INFO_OID = 210;
	public static final int NEXT_CLASS_INFO_OID_RESPONSE = 211;
	public static final int STORE_CLASS_INFO = 220;
	public static final int STORE_CLASS_INFO_RESPONSE = 221;
	public static final int SEND_FILE = 230;
	public static final int SEND_FILE_RESPONSE = 231;
	public static final int GET_FILE = 235;
	public static final int GET_FILE_RESPONSE = 236;
	public static final int REMOTE_PROCESS = 240;
	public static final int REMOTE_PROCESS_RESPONSE = 241;
	public static final int [] types = {CONNECT,CONNECT_RESPONSE, GET_OBJECTS,GET_OBJECTS_RESPONSE, GET_OBJECT_FROM_ID,STORE_OBJECT, STORE_OBJECT_RESPONSE,DELETE_OBJECT,DELETE_OBJECT_RESPONSE, CLOSE,CLOSE_RESPONSE, COMMIT,COMMIT_RESPONSE, ROLLBACK,ROLLBACK_RESPONSE,DELETE_BASE,GET_SESSIONS,ADD_UNIQUE_INDEX,ADD_CLASS_INFO_LIST,COUNT,COUNT_RESPONSE,GET_OBJECT_VALUES,GET_OBJECT_HEADER_FROM_ID,REBUILD_INDEX,DELETE_INDEX,CHECK_META_MODEL_COMPATIBILITY, GET_NEXT_OBJECT_OID, GET_NEXT_OBJECT_OID_RESPONSE, GET_NEXT_CLASS_OID, GET_NEXT_CLASS_OID_RESPONSE, NEXT_CLASS_INFO_OID, NEXT_CLASS_INFO_OID_RESPONSE, STORE_CLASS_INFO, STORE_CLASS_INFO_RESPONSE, SEND_FILE, SEND_FILE_RESPONSE, GET_FILE, GET_FILE_RESPONSE, REMOTE_PROCESS, REMOTE_PROCESS_RESPONSE};
}

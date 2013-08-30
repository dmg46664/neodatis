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
package org.neodatis.tool;

import java.io.File;

import org.neodatis.tool.wrappers.OdbSystem;
import org.neodatis.tool.wrappers.io.OdbFile;

/**
 * Delete file function
 * 
 * @author osmadja
 * 
 */
public class IOUtil {
	public static boolean deleteFile(String fileName) {
		OdbFile file = null;
		String dataDirectory = OdbSystem.getProperty("data.directory");
		if (dataDirectory != null) {
			StringBuffer buffer = new StringBuffer(fileName.length() + 1 + dataDirectory.length());
			buffer.append(dataDirectory).append("/").append(fileName);
			file = new OdbFile(buffer.toString());
		} else {
			file = new OdbFile(fileName);
		}
		boolean deleted = false;
		if (file.isDirectory()) {
			deleted = deleteDirectory(file.getFullPath());
		} else {
			deleted = file.delete();
		}

		return deleted;
	}

	/**
	 * @param file
	 */
	public static boolean deleteDirectory(String directoryName) {
		//System.out.println("Trying to delete directory/file " + file.getFullPath());
		File file = new File(directoryName);
		if (file.isDirectory()) {
			String[] children = file.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDirectory(file.getAbsolutePath()+"/"+ children[i]);
				//System.out.println("  <= "+success);
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return file.delete();
	}

	public static boolean existFile(String fileName) {
		OdbFile file = null;
		String dataDirectory = OdbSystem.getProperty("data.directory");
		if (dataDirectory != null) {
			StringBuffer buffer = new StringBuffer(fileName.length() + 1 + dataDirectory.length());
			buffer.append(dataDirectory).append("/").append(fileName);
			file = new OdbFile(buffer.toString());
		} else {
			file = new OdbFile(fileName);
		}

		return file.exists();
	}

}

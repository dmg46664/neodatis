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
package org.neodatis.odb;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.neodatis.odb.core.IError;
import org.neodatis.tool.DLogger;
import org.neodatis.tool.wrappers.OdbString;
import org.neodatis.tool.wrappers.OdbThread;

/**
 * Generic ODB Runtime exception : Used to report all problems.
 * 
 * @author osmadja
 * 
 */
public class NeoDatisRuntimeException extends RuntimeException {
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
	private static final String url = "https://sourceforge.net/tracker/?func=add&group_id=179124&atid=887885";
	private static final String message1 = "";//String.format("\nNeoDatis has thrown an Exception, please help us filling a bug report at %s with the following error message",url);
	

	public NeoDatisRuntimeException(String error) {
		super(String.format("%s\nVersion=%s , Build=%s, Date=%s, Thread=%s\nNeoDatisError:%s\nStackTrace:" ,message1, Release.RELEASE_NUMBER , Release.RELEASE_BUILD, Release.RELEASE_DATE, OdbThread.getCurrentThreadName() , error));
	}

	
	// FIXME add a submit a bug link to SF
	public NeoDatisRuntimeException(IError error, Throwable t) {
		super(String.format("%s\nVersion=%s , Build=%s, Date=%s, Thread=%s\nNeoDatisError:%s\nStackTrace:" ,message1, Release.RELEASE_NUMBER , Release.RELEASE_BUILD, Release.RELEASE_DATE, OdbThread.getCurrentThreadName() , error.toString()), t);
	}

	public NeoDatisRuntimeException(IError error) {
		super(String.format("%s\nVersion=%s , Build=%s, Date=%s, Thread=%s\nNeoDatisError:%s\nStackTrace:" ,message1, Release.RELEASE_NUMBER , Release.RELEASE_BUILD, Release.RELEASE_DATE, OdbThread.getCurrentThreadName() , error.toString()));
	}

	public NeoDatisRuntimeException(IError error, String message) {
		super(String.format("%s\nVersion=%s , Build=%s, Date=%s, Thread=%s\nNeoDatisError:%s\nMessage:\n%s" ,message1, Release.RELEASE_NUMBER , Release.RELEASE_BUILD, Release.RELEASE_DATE, OdbThread.getCurrentThreadName() , error.toString(),message));
	}

	public void addMessageHeader(String string) {
		// TODO Auto-generated method stub

	}

	public NeoDatisRuntimeException(Exception e, String message) {
		super(String.format("%s\nVersion=%s , Build=%s, Date=%s, Thread=%s\nMessage:%s\nStackTrace:" ,message1, Release.RELEASE_NUMBER , Release.RELEASE_BUILD, Release.RELEASE_DATE, OdbThread.getCurrentThreadName() , message), e);
	}
	
	public NeoDatisRuntimeException(Exception e) {
		super(String.format("%s\nVersion=%s , Build=%s, Date=%s, Thread=%s\nStackTrace:" ,message1, Release.RELEASE_NUMBER , Release.RELEASE_BUILD, Release.RELEASE_DATE, OdbThread.getCurrentThreadName() ), e);
	}




	public void internalWriteToFile(String directory){
		try {
			String fileName = directory+ "/neodatis-error-"+dateFormat.format(new Date())+ "-"+System.nanoTime()+ ".txt";
			FileOutputStream out = new FileOutputStream(fileName);
			out.write(OdbString.exceptionToString(this, true).getBytes());
			out.close();
			DLogger.error("Exception detail has been written to " + new File(fileName).getAbsolutePath());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void writeToFile() {
		internalWriteToFile(".");
	}
	public void writeToFile(String directory) {
		internalWriteToFile(directory);
	}
}

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
package org.neodatis.odb.xml.tool;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.tool.wrappers.io.OdbFile;

public class XMLGenerator {
	private  List<NodeEventListener> listeners = new ArrayList<NodeEventListener>();
	private  String incrementalFileName;
	private  boolean writeIncremental;
	private  Writer incrementalWriter;
	private  boolean firstNode = true;

	private String encoding;
	
	public XMLGenerator(String encoding){
		this.encoding = encoding;
	}
	
	public  void addListener(NodeEventListener listener) {
		listeners.add(listener);
	}

	public  void setIncrementalWriteOn(String fileName) throws IOException {
		incrementalFileName = fileName;
		writeIncremental = true;
		incrementalWriter = getWriter(fileName);
	}

	public  void end() throws IOException {
		if (writeIncremental && incrementalWriter != null) {
			incrementalWriter.close();
		}
	}

	public  XMLNode createRoot(String name) {

		XMLNode node = new XMLNode(this,name, true);
		startOfDocument(name);

		return node;
	}

	public  void startOfDocument(String name) {
		NodeEventListener listener = null;
		if (writeIncremental) {
			try {
				incrementalWriter.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
				incrementalWriter.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new NeoDatisRuntimeException(NeoDatisError.XML_HEADER, e);
			}

		}
		for (int i = 0; i < listeners.size(); i++) {
			listener = (NodeEventListener) listeners.get(i);
			listener.startOfDocument();
		}
	}

	public  void endOfDocument(String name) {
		NodeEventListener listener = null;
		for (int i = 0; i < listeners.size(); i++) {
			listener = (NodeEventListener) listeners.get(i);
			listener.endOfDocument();
		}
	}

	public  void startOfNode(String name, XMLNode node) {
		NodeEventListener listener = null;
		for (int i = 0; i < listeners.size(); i++) {
			listener = (NodeEventListener) listeners.get(i);
			listener.startOfNode(name, node);
		}
		if (writeIncremental) {
			try {
				writeIncrementalNodeHeader(node, false);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	public  boolean endOfNode(String name, XMLNode node) {
		NodeEventListener listener = null;
		for (int i = 0; i < listeners.size(); i++) {
			listener = (NodeEventListener) listeners.get(i);
			listener.endOfNode(name, node);
		}
		if (writeIncremental) {
			try {
				if (!node.headerHasBeenWritten) {
					if (!node.hasChildren()) {
						writeIncrementalNodeHeader(node, true);
						return true;
					}
				}
				writeIncrementalNodeFooter(node);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return false;
	}

	private  void writeIncrementalNodeHeader(XMLNode node, boolean closeTag) throws IOException {
		incrementalWriter.write(node.headerToString(closeTag));
		incrementalWriter.flush();
	}

	private  void writeIncrementalNodeFooter(XMLNode node) throws IOException {
		incrementalWriter.write(node.footerToString());
		incrementalWriter.flush();
	}

	public  void writeNodeToFile(XMLNode node, String fileName) throws IOException {
		Writer writer = getWriter(fileName);
		writer.write(node.toString());
		writer.close();
	}

	private  Writer getWriter(String fileName) throws IOException {
		OdbFile f = new OdbFile(fileName);
		if (!f.exists()) {
			if (f.getParentFile() != null) {
				f.getParentFile().mkdirs();
			}
		}
		FileOutputStream out = new FileOutputStream(fileName);
		OutputStreamWriter writer = null;

		if (encoding!=null) {
			writer = new OutputStreamWriter(out, encoding);
		} else {
			writer = new OutputStreamWriter(out);
		}
		return writer;
	}

	public  void close() throws IOException {
		if (incrementalWriter != null) {
			incrementalWriter.close();
		}
	}
}

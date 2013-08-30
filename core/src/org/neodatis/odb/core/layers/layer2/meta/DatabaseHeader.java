/**
 * 
 */
package org.neodatis.odb.core.layers.layer2.meta;

import java.util.Date;

import org.neodatis.odb.DatabaseId;


/**
 * @author olivier
 *
 */
public class DatabaseHeader {
	private boolean useEncryption;
	private DatabaseId databaseId;
	private String encoding;
	private Date creation;
	private int lastCloseStatus;
	private String storageEngineClass;
	public boolean isUseEncryption() {
		return useEncryption;
	}
	public void setUseEncryption(boolean useEncryption) {
		this.useEncryption = useEncryption;
	}
	public DatabaseId getDatabaseId() {
		return databaseId;
	}
	public void setDatabaseId(DatabaseId databaseId) {
		this.databaseId = databaseId;
	}
	public String getEncoding() {
		return encoding;
	}
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	public Date getCreation() {
		return creation;
	}
	public void setCreation(Date creation) {
		this.creation = creation;
	}
	public int getLastCloseStatus() {
		return lastCloseStatus;
	}
	public void setLastCloseStatus(int lastCloseStatus) {
		this.lastCloseStatus = lastCloseStatus;
	}
	public String getStorageEngineClass() {
		return storageEngineClass;
	}
	public void setStorageEngineClass(String storageEngineClass) {
		this.storageEngineClass = storageEngineClass;
	}
	
	
}

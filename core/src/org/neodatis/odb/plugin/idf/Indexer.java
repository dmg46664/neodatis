package org.neodatis.odb.plugin.idf;

import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.neodatis.odb.OID;
import org.neodatis.odb.core.layers.layer4.ClassOidIterator;
import org.neodatis.odb.core.layers.layer4.ObjectOidIterator;

/**
 * The interface to define Indexers. There are used by all IDF plugins
 * @author olivier
 *
 */
public interface Indexer {
	
	/**
	 * Inits the indexer
	 * @param id The id of the indexer
	 * @param fileName Where the indexer saves its data
	 * @throws Exception 
	 */
	public void init(String id, String fileName) throws Exception;

	/** Stores the object location for the oid
	 * 
	 * @param oid
	 * @param objectLocation
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public abstract void put(OID oid, ObjectLocation objectLocation) throws CorruptIndexException, IOException;

	/** Retrieves the object location of the specific oid
	 * 
	 * @param oid
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public abstract ObjectLocation get(OID oid) throws ParseException, IOException;

	/** Closes the indexer
	 * 
	 * @throws IOException
	 */
	public abstract void close() throws IOException;

	public void rollback();

	public void commit();

	public ObjectLocation delete(OID oid);

	public boolean existKey(OID oid);
	
	public ObjectOidIterator getObjectOidIterator();
	public ClassOidIterator getClassOidIterator();
	public void setDebug(boolean yes);
	
	

}
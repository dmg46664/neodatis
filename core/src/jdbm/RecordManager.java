/**
 * JDBM LICENSE v1.00
 *
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "JDBM" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Cees de Groot.  For written permission,
 *    please contact cg@cdegroot.com.
 *
 * 4. Products derived from this Software may not be called "JDBM"
 *    nor may "JDBM" appear in their names without prior written
 *    permission of Cees de Groot.
 *
 * 5. Due credit should be given to the JDBM Project
 *    (http://jdbm.sourceforge.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE JDBM PROJECT AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * CEES DE GROOT OR ANY CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2000 (C) Cees de Groot. All Rights Reserved.
 * Copyright 2000-2001 (C) Alex Boisvert. All Rights Reserved.
 * Contributions are Copyright (C) 2000 by their associated contributors.
 *
 * $Id: RecordManager.java,v 1.1 2009/12/18 22:44:58 olivier_smadja Exp $
 */

package jdbm;

import java.io.IOException;

import jdbm.helper.ISerializationHandler;
import jdbm.helper.Serializer;

/**
 *  An interface to manages records, which are uninterpreted blobs of data.
 *  <p>
 *  The set of record operations is simple: fetch, insert, update and delete.
 *  Each record is identified using a "rowid" and contains a byte[] data block.
 *  Rowids are returned on inserts and you can store them someplace safe
 *  to be able to get  back to them.  Data blocks can be as long as you wish,
 *  and may have lengths different from the original when updating.
 *
 * @author <a href="mailto:boisvert@intalio.com">Alex Boisvert</a>
 * @author <a href="cg@cdegroot.com">Cees de Groot</a>
 * @version $Id: RecordManager.java,v 1.1 2009/12/18 22:44:58 olivier_smadja Exp $
 */
public interface RecordManager
{

    /**
     * Reserved slot for name directory.
     */
    public static final int NAME_DIRECTORY_ROOT = 0;


    /**
     * Recid indicating no record (e.g. null)
     */
    public static final long NULL_RECID = 0;
    
    
    /**
     * Get the underlying Record Manager or <i>this</i> if this is the
     * base record manager.
     *
     * @return underlying RecordManager or <code>null</code> if record
     * manager has been closed. 
     */
    
    public RecordManager getRecordManager();

    /**
     * Return the base underlying Record Manager or <i>this</i> if this
     * is the base record manager.
     *
     * @return base RecordManager or <code>null</code> if record
     * manager has been closed.
     */
    
    public RecordManager getBaseRecordManager();
    
    
    /**
     * Return the configured serialization handler.
     * 
     * @return The configured serialization handler and never <code>null</code>.
     * 
     * @see RecordManagerOptions#SERIALIZER
     */
    
    public ISerializationHandler getSerializationHandler();
    
    
    /**
     *  Inserts a new record using standard java object serialization.
     *
     *  @param obj the object for the new record.
     *  @return the rowid for the new record.
     *  @throws IOException when one of the underlying I/O operations fails.
     */
    public abstract long insert( Object obj )
        throws IOException;

    
    /**
     *  Inserts a new record using a custom serializer.
     *
     *  @param obj the object for the new record.
     *  @param serializer a custom serializer or <code>null</code> to use the
     *  configured serialization handler.
     *  @return the rowid for the new record.
     *  @throws IOException when one of the underlying I/O operations fails.
     */
    public abstract long insert( Object obj, Serializer serializer )
        throws IOException;


    /**
     *  Deletes a record.
     *
     *  @param recid the rowid for the record that should be deleted.
     *  @throws IOException when one of the underlying I/O operations fails.
     */
    public abstract void delete( long recid )
        throws IOException;


    /**
     *  Updates a record using standard java object serialization.
     *
     *  @param recid the recid for the record that is to be updated.
     *  @param obj the new object for the record.
     *  @throws IOException when one of the underlying I/O operations fails.
     */
    public abstract void update( long recid, Object obj )
        throws IOException;


    /**
     *  Updates a record using a custom serializer.
     *
     *  @param recid the recid for the record that is to be updated.
     *  @param obj the new object for the record.
     *  @param serializer a custom serializer or <code>null</code> to use the
     *  configured serialization handler.
     *  @throws IOException when one of the underlying I/O operations fails.
     */
    public abstract void update( long recid, Object obj, Serializer serializer )
        throws IOException;

    
    /**
     *  Fetches a record using standard java object serialization.
     *
     *  @param recid the recid for the record that must be fetched.
     *  @return the object contained in the record.
     *  @throws IOException when one of the underlying I/O operations fails.
     */
    public abstract Object fetch( long recid )
        throws IOException;


    /**
     *  Fetches a record using a custom serializer.
     *
     *  @param recid the recid for the record that must be fetched.
     *  @param serializer a custom serializer or <code>null</code> to use the
     *  configured serialization handler.
     *  @return the object contained in the record.
     *  @throws IOException when one of the underlying I/O operations fails.
     */
    public abstract Object fetch( long recid, Serializer serializer )
        throws IOException;


    /**
     *  Closes the record manager.
     *
     *  @throws IOException when one of the underlying I/O operations fails.
     */
    public abstract void close()
        throws IOException;


    /**
     *  Returns the number of slots available for "root" rowids. These slots
     *  can be used to store special rowids, like rowids that point to
     *  other rowids. Root rowids are useful for bootstrapping access to
     *  a set of data.
     */
    public abstract int getRootCount();


    /**
     *  Returns the indicated root rowid.
     *
     *  @see #getRootCount
     */
    public abstract long getRoot( int id )
        throws IOException;


    /**
     *  Sets the indicated root rowid.
     *
     *  @see #getRootCount
     */
    public abstract void setRoot( int id, long rowid )
        throws IOException;


    /**
     * Commit (make persistent) all changes since beginning of transaction.
     */
    public abstract void commit()
        throws IOException;


    /**
     * Rollback (cancel) all changes since beginning of transaction.
     */
    public abstract void rollback()
        throws IOException;




    /**
     * Obtain the record id of a named object. Returns 0 if named object
     * doesn't exist.
     */
    public abstract long getNamedObject( String name )
        throws IOException;


    /**
     * Set the record id of a named object.
     * 
     * @param The name that may be used to recover the <i>recid</i>.
     * 
     * @param The record id or <code>0</code> to remove the name from
     * the dictionary.
     */
    public abstract void setNamedObject( String name, long recid )
        throws IOException;

}

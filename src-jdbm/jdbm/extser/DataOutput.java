/**

The Notice below must appear in each file of the Source Code of any
copy you distribute of the Licensed Product.  Contributors to any
Modifications may add their own copyright notices to identify their
own contributions.

License:

The contents of this file are subject to the CognitiveWeb Open Source
License Version 1.1 (the License).  You may not copy or use this file,
in either source code or executable form, except in compliance with
the License.  You may obtain a copy of the License from

  http://www.CognitiveWeb.org/legal/license/

Software distributed under the License is distributed on an AS IS
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See
the License for the specific language governing rights and limitations
under the License.

Copyrights:

Portions created by or assigned to CognitiveWeb are Copyright
(c) 2003-2003 CognitiveWeb.  All Rights Reserved.  Contact
information for CognitiveWeb is available at

  http://www.CognitiveWeb.org

Portions Copyright (c) 2002-2003 Bryan Thompson.

Acknowledgements:

Special thanks to the developers of the Jabber Open Source License 1.0
(JOSL), from which this License was derived.  This License contains
terms that differ from JOSL.

Special thanks to the CognitiveWeb Open Source Contributors for their
suggestions and support of the Cognitive Web.

Modifications:

*/
/*
 * Created on Sep 30, 2005
 */
package jdbm.extser;

import java.io.IOException;

/**
 * Supports stream-based serialization of objects and packed integer values.
 * 
 * @author thompsonbry
 * @version $Id: DataOutput.java,v 1.1 2009/12/18 22:44:58 olivier_smadja Exp $
 */

public interface DataOutput extends java.io.DataOutput
{

    /**
     * Return the logical row id (recid) of the record being (de-)serialized
     * or zero (0L) iff the record is being inserted into the store.
     */
    abstract public long getRecid();

    /**
     * The serialization handler.
     */
    abstract public IExtensibleSerializer getSerializationHandler();

    /**
     * Applies the serialization handler to serialize the next object onto the
     * output stream.
     * <p>
     * 
     * @param obj
     * 
     * @throws IOException
     */
    
    abstract public void serialize( Object obj ) throws IOException;
    
    /**
     * Pack an object identifier onto the output stream.
     * 
     * @param oid The object identifier.
     * 
     * @throws IOException
     * 
     * @return The #of bytes in which the object identifier was packed.
     */

    abstract public int writePackedOId( long oid ) throws IOException;

    /**
     * Pack a non-negative long value onto the output stream.
     * 
     * @param val
     * 
     * @throws IOException
     * 
     * @return The #of bytes in which the long value was packed.
     */

    abstract public int writePackedLong( long val ) throws IOException;

    /**
     * Pack a non-negative int value onto the output stream.
     * 
     * @param val The int value.
     * 
     * @return The #of bytes written on the output stream.
     * 
     * @throws IOException
     */
    
    abstract public int writePackedInt( int val ) throws IOException;
    
    /**
     * Pack a non-negative short value onto the output stream.
     * 
     * @param val The short value.
     * 
     * @return The #of bytes written on the output stream.
     * 
     * @throws IOException
     */
    
    abstract public int writePackedShort( short val ) throws IOException;

}

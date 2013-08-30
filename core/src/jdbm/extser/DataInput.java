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
 * @version $Id: DataInput.java,v 1.1 2009/12/18 22:44:58 olivier_smadja Exp $
 */

public interface DataInput
    extends java.io.DataInput
{

    /**
     * Return the logical row id (recid) of the record being (de-)serialized
     * or zero (0L) iff the record is being inserted into the store.
     */

    public long getRecid();

    /**
     * The serialization handler.
     */
    
    public IExtensibleSerializer getSerializationHandler();

    /**
     * Applies the serialization handler to deserialize the next object
     * in the input stream.
     * 
     * @return The deserialized object.
     */
    
    public Object deserialize() throws IOException;
   
    /**
     * Unpacks an object identifier from the input stream.
     * 
     * @return The object identifier.
     * 
     * @throws IOException
     * 
     * @see DataOutput#writePackedOId(long)
     */
    
    public long readPackedOId() throws IOException;

    /**
     * Unpacks a long value from the input stream.
     * 
     * @return The long value.
     * 
     * @throws IOException
     * 
     * @see DataOutput#writePackedLong(long)
     */
    
    public long readPackedLong() throws IOException;

    /**
     * Reads a packed int value.
     * 
     * @return The unpacked int value.
     * 
     * @throws IOException
     * 
     * @see DataOutput#writePackedInt(int)
     */
    
    public int readPackedInt() throws IOException;
    
    /**
     * Reads a packed short value.
     * 
     * @return The unpacked short value.
     * 
     * @throws IOException
     * 
     * @see DataOutput#writePackedShort(short)
     */
    
    public short readPackedShort() throws IOException;

}

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
 * Created on Oct 24, 2005
 */
package jdbm.extser;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Packing utility for non-negative <code>short</code> values.
 *  
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id: ShortPacker.java,v 1.1 2009/12/18 22:44:58 olivier_smadja Exp $
 */

public class ShortPacker
{
    
    public ShortPacker() {
        super();
    }
    
    /**
     * Packs a non-negative short value into one or two bytes and writes them on
     * <i>os </i>. A short in [0:127] is packed into one byte. Larger values are
     * packed into two bytes. The high bit of the first byte is set if the value
     * was packed into two bytes. If the bit is set, clear the high bit, read
     * the next byte, and interpret the two bytes as a short value. Otherwise
     * interpret the byte as a short value.
     * 
     * @return The #of bytes into which the value was packed.
     */ 

    static public int packShort( DataOutput os, short v ) throws IOException
    {
    
        /*
         * You can only pack non-negative values with this method.
         */
        if( v < 0 ) {
            throw new IllegalArgumentException( "negative value: v="+v );
        }
        if( v > 127 ) {
            // the value requires two bytes.
            os.write( (byte)((0xff & (v >> 8))|0x80) ); // note: set the high bit.
            os.write( (byte)(0xff & v) );
            return 2;
        } else {
            // the value fits in one byte.
            os.write( (byte)(0xff & v) );
            return 1;
        }
    }
    
    /**
     * Unpack a non-negative short value from the input stream.
     * 
     * @param is The input stream.
     * 
     * @return The short value.
     * 
     * @throws IOException
     */
    
    static public short unpackShort( DataInput is ) throws IOException
    {
        short b = (short) is.readByte();
        short v;
        if( ( b & 0x80 ) != 0 ) {
            // high bit is set.
            v = (short) (( b & 0x7f ) << 8); // clear the high bit and shift over one byte.
            b = is.readByte(); // read the next byte.
            v |= ( b & 0xff ); // and combine it together with the high byte.
        } else {
            // high bit is clear.
            v = b; // interpret the byte as a short value.
        }
        return (short) v;
    }

    /**
     * Returns the #of bytes into which a short value was packed based on the
     * first byte.
     * 
     * @param firstByte The first byte.
     * 
     * @return The #of bytes (either one (1) or two (2)).
     */
    static public int getNBytes( byte firstByte ) {
        if( ( firstByte & 0x80 ) != 0 ) {
            return 2;
        } else {
            return 1;
        }
    }
    
}

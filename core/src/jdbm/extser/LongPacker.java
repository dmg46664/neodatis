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
 * Packing utility for non-negative <code>long</code> values.
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id: LongPacker.java,v 1.1 2009/12/18 22:44:58 olivier_smadja Exp $
 */

public class LongPacker
{
    
    public LongPacker() {
        super();
    }
    
    /**
     * Packs a non-negative long value into the minimum #of bytes in which the
     * value can be represented and writes those bytes onto the output stream.
     * The first byte determines whether or not the long value was packed and,
     * if packed, how many bytes were required to represent the packed long
     * value. When the high bit of the first byte is a one (1), then the long
     * value could not be packed and the long value is found by clearing the
     * high bit and interpreting the first byte plus the next seven (7) bytes as
     * a long. Otherwise the next three (3) bits are interpreted as an unsigned
     * integer giving the #of bytes (nbytes) required to represent the packed
     * long value. To recover the long value the high nibble is cleared and the
     * first byte together with the next nbytes are interpeted as an unsigned
     * long value whose leading zero bytes were not written.
     * 
     * <pre>
     *    
     * [0|1|2|3|4|5|6|7]
     *  1 - - -	  nbytes = 8, clear high bit and interpret this plus the next 7 bytes as a long.
     *  0 1 1 1	  nbytes = 7, clear high nibble and interpret this plus the next 6 bytes as a long. 
     *  0 1 1 0	  nbytes = 6, clear high nibble and interpret this plus the next 5 bytes as a long. 
     *  0 1 0 1	  nbytes = 5, clear high nibble and interpret this plus the next 4 bytes as a long.
     *  0 1 0 0	  nbytes = 4, clear high nibble and interpret this plus the next 3 bytes as a long.
     *  0 0 1 1	  nbytes = 3, clear high nibble and interpret this plus the next 3 bytes as a long.
     *  0 0 1 0	  nbytes = 2, clear high nibble and interpret this plus the next byte as a long.
     *  0 0 0 1	  nbytes = 1, clear high nibble.  value is the low nibble.
     *  
     * </pre>
     */ 

    static public int packLong( DataOutput os, long v ) throws IOException {
        /*
         * You can only pack non-negative long values with this method.
         */
        if( v < 0 ) {
            throw new IllegalArgumentException( "negative value: v="+v );
        }
        /*
         * If the high byte is non-zero then we will write the value as a normal
         * long and return nbytes == 8. This case handles large positive long
         * values.
         */
        if( ( v >> 56 ) != 0 ) {
            os.write( (byte)((0xff & (v >> 56))|0x80) ); // note: set the high bit.
            os.write( (byte)(0xff & (v >> 48)) );
            os.write( (byte)(0xff & (v >> 40)) );
            os.write( (byte)(0xff & (v >> 32)) );
            os.write( (byte)(0xff & (v >> 24)) );
            os.write( (byte)(0xff & (v >> 16)) );
            os.write( (byte)(0xff & (v >>  8)) );
            os.write( (byte)(0xff & v) );
            return 8;
        }
        // #of nibbles required to represent the long value.
        final int nnibbles = getNibbleLength( v );
        // Is [nnibbles] even? (If it is even then we need to pad out an extra zero
        // nibble in the first byte.)
        final boolean evenNibbleCount = ( nnibbles == ( ( nnibbles >> 1 ) << 1 ) );
        // #of bytes required to represent the long value (plus the header nibble).
        final int nbytes = ( ( nnibbles +1 ) >> 1 ) + (evenNibbleCount?1:0);
        
        int nwritten = 0;
        if( evenNibbleCount ) {
            /*
             * An even nibble count requires that we pad the low nibble of the
             * first byte with zeros.
             */
            // header byte. low nibble is empty.
            byte b = (byte) ( nbytes << 4 );
            os.write( b );
            nwritten++;
            // remaining bytes containing the packed value.
            for( int i=(nnibbles-2)<<2; i>=0; i-=8 ) {
                b = (byte) (0xff & (v >> i));
                os.write( b );
                nwritten++;
            }
        } else {
            /*
             * An odd nibble count means that we pack the first nibble of the
             * long value into the low nibble of the header byte. In this case
             * the first nibble will always be the low nibble of the first
             * non-zero byte in the long value (the high nibble of that byte
             * must be zero since there is an odd nibble count).
             */
            byte highByte = (byte) (0xff & (v >> ((nbytes-1)*8) ));
            byte b = (byte) ( ( nbytes << 4 ) | highByte );
            os.write( b );
            nwritten++;
            for( int i=(nnibbles-3)<<2; i>=0; i-=8 ) {
                b = (byte) (0xff & (v >> i));
                os.write( b );
                nwritten++;
            }
        }
        return nwritten;
    }

    /**
     * Return the #of non-zero nibbles, counting from the first non-zero nibble
     * in the long value. A value of <code>0L</code> is considered to be one
     * nibble for our purposes.
     * 
     * @param v
     *            The long value.
     * 
     * @return The #of nibbles in [1:16].
     */
    
    static protected int getNibbleLength( long v )
    {
        for( int i=56, j=16; i>=0; i-=8, j-=2 ) {
            if( (0xf0 & (v >> i)) != 0 ) return j;
            if( (0x0f & (v >> i)) != 0 ) return j-1;
        }
        if( v != 0 ) throw new AssertionError( "v="+v );
        return 1; // value is zero, which is considered to be one nibble for our purposes.
    }
    
    /**
     * Unpack a long value from the input stream.
     * 
     * @param is The input stream.
     * 
     * @return The long value.
     * 
     * @throws IOException
     */
    
    static public long unpackLong( DataInput is ) throws IOException
    {
        int b = is.readByte();
        int nbytes;
        long l;
        if( ( b & 0x80 ) != 0 ) {
            // high bit is set.
            nbytes = 8; // use 8 bytes (this one plus the next 7).
            l = b & 0x7f; // clear the high bit - the rest of the byte is the start value.
        } else {
            // high bit is clear.
            nbytes = b >> 4; // nbytes is the upper nibble. (right shift one nibble).
            l = b & 0x0f; // starting value is lower nibble (clear the upper nibble).
        }
        for( int i=1; i<nbytes; i++ ) {
            // Read the next byte.
            b = is.readByte(); // readByte( is );
            // Shift the existing value one byte left and add into the low (unsigned) byte.
            l = (l << 8) + (0xff & b);
        }
        return l;
    }

}

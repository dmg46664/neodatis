/*
 *  Primitive Collections for Java.
 *  Copyright (C) 2002  S\u00F8ren Bak
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package jdbm.helper.maps;

/**
 *  This interface represents hash functions from long values
 *  to int values. The int value result is chosen to achieve
 *  consistence with the common
 *  {@link Object#hashCode() hashCode()}
 *  method. The interface is provided to alter the hash functions used
 *  by hashing data structures, like
 *  {@link bak.pcj.map.LongKeyIntChainedHashMap LongKeyIntChainedHashMap}
 *  or
 *  {@link bak.pcj.set.LongChainedHashSet LongChainedHashSet}.
 *
 *  @see        DefaultLongHashFunction
 *
 *  @author     S\u00F8ren Bak
 *  @version    1.0     2002/29/12
 *  @since      1.0
 *  
 *  ********* JDBM Project Note *************
 *  This class was extracted from the pcj project (with permission)
 *  for use in jdbm only.  Modifications to original were performed
 *  by Kevin Day to make it work outside of the pcj class structure. 
 *  
 */
public interface LongHashFunction {

    /**
     *  Returns a hash code for a specified long value.
     *
     *  @param      v
     *              the value for which to return a hash code.
     *
     *  @return     a hash code for the specified value.
     */
    int hash(long v);

}
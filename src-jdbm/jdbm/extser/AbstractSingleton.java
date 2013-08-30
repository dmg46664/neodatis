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
 * Created on Nov 4, 2005
 */
package jdbm.extser;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Abstract base class for a {@link Stateless} singleton serialization handler
 * wrapping the semantics of a stateful {@link IExtensibleSerializer}
 * serialization handle. The use of this stateless singleton prevents multiple
 * copies of the state of the extensible serializer from being written into the
 * store.
 * <p>
 * 
 * @author thompsonbry
 * @version $Id: AbstractSingleton.java,v 1.1 2009/12/18 22:44:58 olivier_smadja Exp $
 */

abstract public class AbstractSingleton implements Stateless
{

    /**
     * Cache map from the persistence layer access object to the {@link
     * IExtensibleSerializer} instance for that access object. This is a
     * {@link WeakHashMap} so entries are transparently removed when the
     * corresponding access object is finalized by the garbage collector.
     */

    static transient final private Map _cache = new WeakHashMap();
    
    /**
     * Return the {@link IExtensibleSerializer} singleton for the store.<p>
     * 
     * @exception IllegalStateException if the serializer has not been
     * set for the store.
     * 
     * @see #setSerializer( Object obj, IExtensibleSerializer ser )
     */

    public IExtensibleSerializer getSerializer( Object obj )
        throws IllegalStateException
    {

        if( obj == null ) {
            
            throw new IllegalArgumentException();
            
        }
        
        IExtensibleSerializer ser = (IExtensibleSerializer) _cache.get
        	( obj
        	  );
        
        if( ser == null ) {
        
            throw new IllegalStateException
               ( "Not initialized."
                 );
        
        }

        return ser;
        
    }

    /**
     * This method must be used to couple the access to the persistence layer
     * with a specific instance of a stateful {@link IExtensibleSerializer}.
     * Thereafter the serialization handler reference recovered from a static
     * transient cache managed by this class.
     * <p>
     * 
     * @param accessObject
     *            The access object for the persistence layer.
     * 
     * @param ser
     *            The serialization handler for the persistence layer.
     * 
     * @exception IllegalStateException
     *                If the serialization handler has already been set.
     */

    public void setSerializer( Object accessObject, IExtensibleSerializer ser )
        throws IllegalStateException
    {
        
        if( accessObject == null || ser == null ) {
            
            throw new IllegalArgumentException();
            
        }
        
        if( _cache.get( accessObject ) != null ) {
            
            throw new IllegalStateException
               ( "Already initialized."
                 );
            
        }
        
        _cache.put( accessObject, ser );
        
    }

}

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

package jdbm.extser;

import java.io.IOException;
import java.io.Serializable;


/**
 * Simple serialization interface.
 *
 * @see IStreamSerializer
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id: ISimpleSerializer.java,v 1.1 2009/12/18 22:44:58 olivier_smadja Exp $
 */

public interface ISimpleSerializer<T>
    extends Serializable, ISerializer
{

    /**
     * Serialize the persistent state of the object.
     * 
     * @param obj
     *            The object to be serialized (non-null).
     * 
     * @return A byte[] containing the persistent state of the object.
     * 
     * @throws IllegalArgumentException
     *             If <code>obj == null</code>.
     */

    public byte[] serialize( T obj )
        throws IOException;
        
    /**
     * Deserialize the persistent data for some object.
     * 
     * @param data
     *            A byte[] containing the persistent state of the object.
     * 
     * @return The object.
     * 
     * @throws IllegalArgumentException
     *             If <code>data == null</code>.
     */

     public T deserialize( byte[] data )
        throws IOException;

}

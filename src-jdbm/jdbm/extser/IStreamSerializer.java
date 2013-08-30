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
 * Created on Oct 20, 2005
 */
package jdbm.extser;

import java.io.IOException;

/**
 * Stream-oriented serializer for complex records (not blobs). This interface
 * MUST be used in conjunction with the {@link AbstractExtensibleSerializer}.
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id: IStreamSerializer.java,v 1.1 2009/12/18 22:44:58 olivier_smadja Exp $
 * 
 * @see AbstractExtensibleSerializer
 */

public interface IStreamSerializer
    extends ISerializer
{

    /**
     * Serialize an object.
     * 
     * @param out Sink for the serialized object.
     * 
     * @param obj The object to serialize (or null).
     * 
     * @throws IOException
     */
    public void serialize( DataOutput out, Object obj )
    	throws IOException;

    /**
     * Deserialize an object.
     * 
     * @param in
     *            Source for the serialized object.
     * 
     * @param obj
     *            An instance of the appropriate class whose persistent fields
     *            need to be initialized from the input stream.
     * 
     * @return The deserialized object. If desired, you can replace the <i>obj
     *         </i> with another object by returning a different object here.
     * 
     * @throws IOException
     */
    public Object deserialize( DataInput in, Object obj )
    	throws IOException;

}

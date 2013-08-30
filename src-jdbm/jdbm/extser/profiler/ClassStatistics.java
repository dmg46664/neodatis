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
package jdbm.extser.profiler;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jdbm.extser.AbstractExtensibleSerializer;
import jdbm.extser.ISerializer;
import jdbm.extser.NativeType;
import jdbm.extser.Stateless;



/**
 * Per class statistics.
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id: ClassStatistics.java,v 1.1 2009/12/18 22:44:58 olivier_smadja Exp $
 */
public class ClassStatistics extends Statistics
{
    final public int classId;
    final public String classname;
    final public boolean stateless; // iff implements Stateless.
    
    public ClassStatistics( AbstractExtensibleSerializer serializer, int classId ) {
        super( serializer );
        this.classId = classId;
        if( classId >= NativeType.NULL && classId <= NativeType.OBJECT_ARRAY ) {
            classname = NativeType.asString( classId );
            stateless = false;
        } else {
            classname = getSerializer().getClassName( classId );
            try {
                Class cl = Class.forName( classname );
                stateless = Stateless.class.isAssignableFrom( cl );
            }
            catch( ClassNotFoundException ex ) {
                throw new RuntimeException( ex );
            }
        }
        reset();
    }

    public VersionStatistics get( short versionId, ISerializer serializer ) {
        Short versionId2 = new Short( versionId );
        VersionStatistics stats = (VersionStatistics) versionStatistics.get( versionId2 );
        if( stats == null ) {
            stats = new VersionStatistics( getSerializer(), versionId, serializer );
            versionStatistics.put( versionId2, stats);
        }
        return stats;
    }
    
    /**
     * Members are {@link VersionStatistics}. 
     */
    public Map versionStatistics;
    
    public void reset() {
        super.reset();
        versionStatistics = new HashMap(); 
    }
    
    public void read( ISerializer ser, short version, int nbytes ) {
        super.read( nbytes );
        get( version, ser ).read( nbytes );
    }

    public void write( ISerializer ser, short version, int nbytes ) {
        super.write( nbytes );
        get( version, ser ).write( nbytes );
    }

    public void writeOn( PrintStream ps ) {
        long avgPerRead = (nread == 0 ?0 :bytesRead/nread);
        long avgPerWrite = (nwritten == 0 ?0 :bytesWritten/nwritten);
        ps.println
            ( "class="+classname+(stateless?"(Stateless)":"")+
              ", classId="+classId+
              ", read("+nread+","+bytesRead+","+avgPerRead+")"+
              ", write("+nwritten+","+bytesWritten+","+avgPerWrite+")"
              );
        Iterator itr = versionStatistics.values().iterator();
        while( itr.hasNext() ) {
            VersionStatistics stats = (VersionStatistics) itr.next();
            stats.writeOn( ps );
        }
    }
    
}
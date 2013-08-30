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



/**
 * Collects statistics about serialization. You can use this to identify
 * objects for which serializers are not registered (they will show up as
 * <code>versionId == 0</code> and
 * <code>serializer == DefaultSerializer</code>, the versions of the
 * objects being read or written, etc. Objects wrapping Java primitives,
 * e.g., {@link Boolean},{@link Integer}, etc., as well as array types
 * will all have classId values less than {@link NativeType#FIRST_OBJECT_INDEX}.
 * <p>
 * 
 * Note: Objects which correspond to compound records (comprised of other
 * objects) will be double-counted by the profiler. That is, the profiler
 * will report the serialized size of both the compound record and each
 * object within that compound record.
 * <p>
 * 
 * The profiler is disabled by default. However it imposes a negligable
 * overhead. It may be enabled using a configuration option, in which case
 * the profiler will automatically report when it is finalized. Usually this
 * is not long after the recman is closed.
 * <p>
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id: Profiler.java,v 1.1 2009/12/18 22:44:58 olivier_smadja Exp $
 */

public class Profiler
	extends Statistics
{

    private final AbstractExtensibleSerializer serializer;
    private boolean enabled = false;
    
    /**
     * Enable or disable the profiler.  It is disabled by default.
     * 
     * @param val The new value.
     * 
     * @return The old value.
     */
    
    public boolean enable( boolean val ) {
        boolean tmp = enabled;
        enabled = val;
        return tmp;
    }

    /**
     * If the profiler is enabled and there has been at least one read or
     * write reported, then writes the statistics onto {@link System#err}.
     * While it is deprecated, you can use
     * {@link System#runFinalizersOnExit(boolean)} to trigger this
     * automatically.
     */
    
    protected void finalize() throws Throwable {
        super.finalize();
        if( enabled && (nread>0 || nwritten>0) ) {
            writeOn( System.err );
        }
    }
    
    public Profiler(AbstractExtensibleSerializer serializer) {
        super( serializer );
        reset();
        this.serializer = serializer;
    }
    
    public boolean isEnabled() {return enabled;}
    
    /**
     * Members are {@link ClassStatistics}.
     */
    
    private Map classStatistics; 

    public ClassStatistics get( int classId ) {
        Integer classId2 = new Integer( classId );
        ClassStatistics stats = (ClassStatistics) classStatistics.get( classId2 );
        if( stats == null ) {
            stats = new ClassStatistics( getSerializer(), classId );
            classStatistics.put( classId2, stats);
        }
        return stats;
    }
    
    synchronized public void serialized( ISerializer ser, int classId, short versionId, int nbytes )
    {
        if( enabled ) {
            nread++;
            bytesRead += nbytes;
            get( classId ).read( ser, versionId, nbytes );
        }
    }
    
    synchronized public void deserialized( ISerializer ser, int classId, short versionId, int nbytes )
    {
        if( enabled ) {
            nwritten++;
            bytesWritten += nbytes;
            get( classId ).write( ser, versionId, nbytes );
        }
    }

    /**
     * Write out the collected statistics.
     * 
     * @param ps
     * 
     * @todo Change to a tab delimited format so that you can analyze it in a
     *       spreadsheet.
     */

    public void writeOn( PrintStream ps ) {
        ps.println( "----- serialization statistics -----");
        ps.println( "read(#read,#bytesRead,avgPerRead), write(#written,#bytesWritten,avgPerWrite)");
        super.writeOn( ps );
        Iterator itr = classStatistics.values().iterator();
        while( itr.hasNext() ) {
            ClassStatistics stats = (ClassStatistics) itr.next();
            stats.writeOn( ps );
        }
        ps.println( "------------------------------------");
    }
    
    public void reset()
    {
        super.reset();
        classStatistics = new HashMap();
    }
    
}
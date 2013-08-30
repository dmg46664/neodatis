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
 * Created on Sep 26, 2005
 */
package jdbm.extser;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintStream;
import java.io.Serializable;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;

import java.lang.reflect.Field;

import jdbm.extser.profiler.Profiler;


/**
 * Abstract implementation of the {@link IExtensibleSerializer}.
 * 
 * @author thompsonbry
 * @version $Id: AbstractExtensibleSerializer.java,v 1.2 2010/02/07 18:29:49 olivier_smadja Exp $
 */

abstract public class AbstractExtensibleSerializer
    implements Externalizable, IExtensibleSerializer
{

    private static final long serialVersionUID = -8387008101267090833L;    

    /**
     * Profiler is initially disabled. It may be enabled to collect statistics
     * on the serialization of objects to/from the store.
     * 
     * @see Profiler
     */

    private Profiler _profiler = new Profiler(this);
    
    public Profiler getProfiler() {
        return _profiler;
    }
    
    /**
     * The next classId to assign.  The first classId to assign is
     * {@link NativeType#FIRST_OBJECT_INDEX}.  Non-negative values less than 
     * that are reserved for <code>null</code>, Java primitives,
     * and arrays of Java primitives.
     * 
     * @serial
     */
    
    private int m_nextClassId;
    
    /**
     * Map from {@link Integer} wrapping the classId to the class
     * name.
     * 
     * @serial
     */

    private Map m_classId = null;
    
    /**
     * Map from the class name to {@link Integer} wrapping the
     * classId.
     * 
     * @serial
     */

    private Map m_className = null;

    /**
     * Map from classId (wrapped as an {@link Integer}) to a inner {@link Map} 
     * from the versionId (wrapped as a {@link Short}) to the name of the {@link
     * AllSerializers} class registered for that class and versionId.<p>
     * 
     * The representation for the inner map was choosen since it does not require
     * us to serialize a {@link Class} object, but only the name of the serializer
     * class.  This should be more robust to CLASSPATH problems which might otherwise
     * make it impossible to restore the state of the {@link IExtensibleSerializer}.<p>
     * 
     * @serial
     */
    
    private Map m_serializer = null;

    /**
     * Transient map from Class to versionId for that class used to
     * cache information obtained by reflection on the Class.
     */

    transient private Map m_versionIds = new WeakHashMap();

    /**
     * Cache from the name of a stateless {@link AllSerializers}class to an
     * instance of that class. This wins back some of the efficiency that we
     * otherwise loose to {@link Class#forName(String name )}.
     */
    
    transient private Map _cache = new HashMap();
    
    /**
     * Deserialization constructor.
     */
    
    public AbstractExtensibleSerializer()
    {
    }
    
    /**
     * Update the state of the {@link IExtensibleSerializer}in the persistence
     * layer.
     */

    abstract protected void update();
    
    /**
     * Mask for the bit on the classId field that signals the presence
     * of a versionId in the data header.  When the masked bit is set
     * a [short] versionId follows the classId in the data header.
     */

    private static final transient int VERSION_MASK = 1<<0;

    /**
     * Mask for the bit on the classId field that signals the presence of a
     * serializerId in the data header. When the masked bit is set the
     * <em>packed long recid</em> of the persistent {@link AllSerializers} follows
     * the versionId (or the classId if there is no versionId) in the data
     * header.
     */

    private static final transient int SERIALIZER_MASK = 1<<1;

    /**
     * Mark for the bit on the classId field that signals the presence of a
     * stateless object. When the masked bit is set, no serializerId and no data
     * follows the classId field. (Marking stateless objects explicitly in the
     * output stream is done so that the deserialization will not break if
     * Stateless is declared after the type has already been serialized.)
     */
    
    private static final transient int STATELESS_MASK = 1<<2;

    /**
     * Reserved.
     * 
     * @todo Bit is reserved for a possible record compression scheme. Early
     *       evidence indicates that per-record compression can be useful if the
     *       record size is not too small. (Better performance with larger
     *       records.)
     */
    private static final transient int RESERVED_MASK = 1<<3;

    /**
     * #of bit fields in a classId when written into a data header. The bit
     * fields are all on the right edge of the classId value, which is shifted
     * this many bits to the left. This is done so that we can pack the
     * resulting value down since it will have leading zero bytes.
     */
    private static final transient int NBITFIELDS = 4;
    
    private final static transient int SIZEOF_LONG  = 8;
    private final static transient int SIZEOF_INT   = 4;
    private final static transient int SIZEOF_SHORT = 2;
    private final static transient int SIZEOF_BYTE  = 1;
    
    /**
     * Helper method writes out the data header, including setting any masked
     * bit fields and the optional versionId and serializerId fields.
     * 
     * @return The #of bytes written.
     */

    static protected int writeDataHeader
	( DataOutput os,
	  int classId,
	  short versionId,
	  boolean stateless,
	  long serializerId
	  )
	throws IOException
    {

	int tmp = classId<<NBITFIELDS;

	if( versionId != 0 ) {

	    if( stateless ) {
	        
	        throw new AssertionError();
	        
	    }

	    tmp |= VERSION_MASK;

	}

	if( serializerId != 0L ) {

	    if( stateless ) {
	        
	        throw new AssertionError();
	        
	    }
	    
	    tmp |= SERIALIZER_MASK;

	}

	if( stateless ) {

	    tmp |= STATELESS_MASK;

	}

	int nbytes = os.writePackedInt( tmp );

	if( versionId != 0 ) {

	    os.writePackedShort( versionId );
	    
	    nbytes += SIZEOF_SHORT;
	    
	}

	if( serializerId != 0L ) {

	    nbytes += os.writePackedLong( serializerId );

	}

	return nbytes;
	
    }
   
    synchronized public byte[] serialize( long recid, Object obj )
        throws IOException
    {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = getDataOutputStream( recid, baos );
//      DataOutputStream dos = new DataOutputStream
//      ( recid,
//        this,
//        baos
//        );
        dos.serialize( obj );
        dos.flush();
        dos.close();
        return baos.toByteArray();

    }
    
    synchronized public Object deserialize( long recid, byte[] serialized )
	throws IOException
    {

        // Open a data input stream on the serialized record.
        DataInputStream dis = getDataInputStream( recid, new MyByteArrayInputStream( serialized ) );
//	DataInputStream dis = new DataInputStream
//	    ( recid,
//	      this,
//	      new MyByteArrayInputStream( serialized )
//	      );
	
	// Read an object from the serialized record.
	Object obj = dis.deserialize();
	
	return obj;
	
    }

    /**
     * Creates a new instance of the class using its public zero
     * argument constructor.
     * 
     * @param cl The class.
     * 
     * @return The new instance.
     * 
     * @exception RuntimeException if something goes wrong. 
     */
    
    static protected Object newInstance( Class cl )
    {

        try {
	        
	    Object obj = cl.newInstance();

	    return obj;
	        
	}

	catch( IllegalAccessException ex ) {
	        
	    throw new RuntimeException
		( "Could not create instance: "+cl,
		  ex
		  );
	        
	}

	catch( InstantiationException ex ) {
		    
	    throw new RuntimeException
		( "Could not create instance: "+cl,
		  ex
		  );
	        
	}
	
    }
    
    synchronized public short getVersionId( Class cl )
    {
        if( cl == null ) {
            throw new IllegalArgumentException();
        }
        Short versionId = (Short) m_versionIds.get( cl );
        if( versionId == null ) {
        try {
            Field f = cl.getField( VERSION_FIELD_NAME );
            Object val = f.get(null);
            if( val instanceof Short ) {
                versionId = (Short) val;
                m_versionIds.put( cl, versionId );
            } else {
                throw new RuntimeException
                   ( "Field has wrong datatype: "+VERSION_FIELD_NAME+" on "+cl.getName()
                     );
            }
        }
        catch( NullPointerException ex ) {
            RuntimeException ex2 = new RuntimeException
            ( "Field must be static: "+VERSION_FIELD_NAME+" on "+cl.getName()
               );
            ex2.initCause( ex );
            throw ex2;
        }
        catch( NoSuchFieldException ex ) {
            return 0; // "unversioned" means versionId == 0.
        }
        catch( IllegalAccessException ex ) {
            RuntimeException ex2 = new RuntimeException
               ( "Unable to access "+VERSION_FIELD_NAME+" on "+cl.getName()
                  );
            ex2.initCause( ex );
            throw ex2;
        }
        }
        return versionId.shortValue();
    }
    
    /**
     * A cache of the versionIds associated with each Class is maintained for
     * efficiency. This method clears that cache. Normally object serialization
     * migrates transparently from the version as serialized to the current
     * versionId for the Class and the application need not concern it self with
     * the versionId cache. However an application seeking to dynamically
     * migrate among specific class versions (for whatever reason) will have to
     * clear the cache in order for the versionId information to be re-cached.
     * 
     * @param cl
     *            The class whose versionId cache entry will be invalidated.
     *            When <code>null</code> the entire versionId cache is
     *            invalidated.
     */

    synchronized public void invalidateVersionIdCache( Class cl )
    {
        if( cl == null ) {
            // clear the entire versionId cache.
            m_versionIds.clear();
        } else {
            // clear the entry for this Class.
            m_versionIds.remove( cl );
        }
    }
    
    synchronized public long getSerializerId( long recid, Object obj )
//    	throws IOException
    {
        if( obj instanceof SerializerIdProtocol ) {
            return ((SerializerIdProtocol)obj).getSerializerId( recid );
        } else {
            // The class does not support this protocol.
            return 0L;
        }
    }
    
    synchronized public int getClassCount()
    {
        
        if( m_classId == null ) {
            
            return 0;
            
        } else {
            
            return m_nextClassId;
            
        }
        
    }
    
    /**
     * Method returns the next classId to be assigned and increments the
     * internal counter. While the state of the extensible serializer is
     * changed by this, we do NOT invoke {@link #update()} here but require
     * that our caller do so on our behalf in order to avoid extra updates.
     * 
     * @return The next classId.
     */

    synchronized private int getNextClassId() {
        /* @todo Range check classId.  It must not exceed an int when right
         * shifted by NBITFIELDS.
         */
        return m_nextClassId++;
    }
    
    synchronized public int getClassId( Class cl )
//    	throws IOException
    {

        if( cl == null ) {
            
            throw new IllegalArgumentException();
            
        }

        registerSerializers();
        
        // test for className -> classId map.
	final String name = cl.getName();
	Integer classId = (Integer) m_className.get
	    ( name
	      );
	        
	if( classId == null ) {
	
	    // Not found, so register the class now.
	    boolean updated = _registerClass( cl );

	    // Get the classId now that it is registered.
	    classId = (Integer) m_className.get
		( name
		  );
	    
	    if( classId == null ) {
	        // paranoia test.
	        throw new AssertionError();
	        
	    }
	    
	    // Since we just touched the class maps we need to
	    // mark the serializer as dirty.
	    update();
	    
	}

	// return the classId.
	return classId.intValue();
	
    }

    /**
     * One-time initialization provides an opportunity for the application to
     * register its serializers. This method initializes the various member
     * fields while {@link #setupSerializers()} is invoked by this method to
     * actually register the serializers.  it is always safe to invoke this
     * method - if it has already been invoked then it returns immediately.
     */

    synchronized protected void registerSerializers()
    {
        
        if( m_classId != null ) return;

	/*
	 * The class maps do not exist - create them now.
	 */

        m_nextClassId = NativeType.FIRST_OBJECT_INDEX;
	m_classId = new HashMap();
	m_className = new HashMap();
	m_serializer = new HashMap();

	setupSerializers();
	
	//
	// Mark the object as dirty since we just updated its state.
	//

	update();
	
    }
    
    /**
     * Concrete implementation should extend this method to register any
     * serializers that they want to use. The default implementation registers
     * serialization support for {@link String}. Note that serialization
     * support for the Java classes which wrap primitive datatypes is built in,
     * so we do not register serializers for {@link Long}, etc.
     */
    
    protected void setupSerializers()
    {

        // Register a serializer for Strings.  The rest of the Java primitives
	// are handled automatically.
	_registerClass( String.class, StringSerializer.class, (short) 0 );

    }
    
    /**
     * Registers a class. This operation does not affect the serializer(s)
     * registered for that class. It just assures that a classId has been
     * assigned for the class.
     * 
     * @param cl
     *            The class.
     * 
     * @return true iff the state of the serializer was changed by this
     *         registration.
     */
    
    synchronized public boolean registerClass( Class cl )
//    	throws IOException
    {
        
        if( _registerClass( cl ) ) {
            
            update();
            
            return true;
            
        }
        
        return false;
    }
    
    /**
     * Registers a serializer against an unversioned class.
     * 
     * @param cl
     *            The class.
     * 
     * @param serializerClass
     *            The class which implements the serializer for <i>cl </i>. This
     *            class MUST have a public zero argument constructor and MUST
     *            NOT have persistent state, i.e., it should declare the
     *            {@link Stateless}interface.
     * 
     * @return true iff the state of the serializer was changed by this
     *         registration.
     */

    synchronized public boolean registerSerializer
	( Class cl, Class serializerClass
	  )
//    	throws IOException
    {

        return registerSerializer( cl, serializerClass, (short) 0 );
        
    }

    /**
     * Registers a serializer against a class. When the <i>versionId </i> is
     * non-zero, then the serializer only applies to that version of the class.
     * The version information is stored in the data header of the record. An
     * unversioned class does not store a versionId. A bit is reserved to mark a
     * record written using a serializer specific to a class version. When the
     * bit is set, then versionId is read and the correct serialized for that
     * class version is used to deserialize the record.
     * 
     * @param cl
     *            The class.
     * 
     * @param serializerClass
     *            The class which implements the serializer for <i>cl </i>. This
     *            class MUST have a public zero argument constructor and MUST
     *            NOT have persistent state, i.e., it should declare the
     *            {@link Stateless}interface.
     * 
     * @param versionId
     *            The versionId begins at one. The value of zero (0) is reserved
     *            to indicate a class that does not have a version (a zero
     *            versionId is exactly equivilent to an unversioned class). An
     *            unversioned class may be promoted to a versioned class at any
     *            time. Likewise, a class may be reverted to an earlier version
     *            or to the "unversioned" version.
     * 
     * @return true iff the state of the serializer was changed by this
     *         registration.
     */

    synchronized public boolean registerSerializer
	( Class cl, Class serializerClass, short versionId
	  )
//    	throws IOException
    {
        
        if( _registerClass( cl, serializerClass, versionId ) ) {

            update();
            
            return true;
            
        }
        
        return false;
        
    }

    /**
     * Private version registers a serializer against a class but does not call
     * {@link #update()}-- this is designed for "batch" insert of multiple
     * registrations.
     * <p>
     * 
     * Note: This will let you change the serializer registered for a versioned
     * or unversioned class. However, if you have existing instances of that
     * class written into the store with the old serializer than you may not be
     * able to recover them using the new serializer. In general, versioning the
     * class is the safe answer to updating serialization mechanisms.
     * <p>
     * 
     * @param cl
     *            The class.
     * 
     * @param serializerClass
     *            The class which implements the serializer for <i>cl </i>. This
     *            class MUST have a public zero argument constructor and MUST
     *            NOT have persistent state, i.e., it should declare the
     *            {@link Stateless}interface.
     * 
     * @param versionId
     *            The versionId begins at one. The value of zero (0) is reserved
     *            to indicate a class that does not have a version (a zero
     *            versionId is exactly equivilent to an unversioned class). An
     *            unversioned class may be promoted to a versioned class at any
     *            time. Likewise, a class may be reverted to an earlier version
     *            or to the "unversioned" version.
     * 
     * @return true iff the state of the serializer was changed by this
     *         registration (indicating that {@link #update()} should be called.
     * 
     * @see #registerSerializer( Class cl, Class serializerClass, short versionId )
     */

    synchronized protected boolean _registerClass(Class cl,
            Class serializerClass, short versionId)
    {
        
        if( cl == null ) {
            
            throw new IllegalArgumentException("class is null.");
            
        }

        if( serializerClass == null ) {
            
            throw new IllegalArgumentException("serializer class is null.");
            
        }
        
        if( ! ISerializer.class.isAssignableFrom( serializerClass ) ) {
            
            throw new IllegalArgumentException
            	( "Class does not implement "+ISerializer.class.getName()+
            	  ": serializerClass="+serializerClass.getName()
            	  );
            
        }

        // TestAll the cache for an instance of this serializer.
        ISerializer serializer = (ISerializer) _cache.get
            ( serializerClass.getName()
              );
        if( serializer == null ) {
            // Create an instance of this serializer Class and insert it
            // into _cache.
            serializer = (ISerializer) newInstance( serializerClass );
            _cache.put( serializerClass.getName(), serializer );
        }
        
        registerSerializers();

        final String name = cl.getName();
        
        Integer classId = (Integer) m_className.get( name );

        boolean updated = false;
        
        if( classId == null ) {
            
            // The class has not been registered yet.
            
            classId = new Integer( getNextClassId() );
	
            m_className.put( name, classId );
	            
            m_classId.put( classId, name );
            
            updated = true;
            
        }
        
        Map versions = (Map) m_serializer.get( classId );
        
        if( versions == null ) {

            // No serializers have been registered yet for this class.
            
            versions = new HashMap();
            
            m_serializer.put( classId, versions );
            
            // This does not count as a "change" in state unless we
            // actually insert a serializer into the inner map.
            
        }
        
        final String newClassName = serializerClass.getName();
        
        final String oldClassName = (String) versions.put
           ( new Short( versionId ),
             newClassName
             );

        if( ! newClassName.equals( oldClassName ) ) {
            
            // The state of the serializer was changed.

            updated = true;

            if( oldClassName != null ) {
            
                // Replacing an existing registration.  This is not an error per say,
                // but it may mean that you can no longer read some existing data unless
                // you restore the oldSerializer.
                
                System.err.println
                ( "WARN: Replacing serializer"+
                 ": record class="+name+
                 ", oldSerializer="+oldClassName+
                 ", newSerializer="+newClassName
                 );
                
            } else {

                // Adding a new registration.

                // @release comment out for release.
//                System.err.println
//                ( "registerClass: "+cl+" (classId="+classId+"), serializer="+serializer.getClass()+", versionId="+versionId
//                   );

            }
            
        }

        return updated;
        
    }

    /**
     * Registers a class without specifying a serializer and does NOT invoke
     * {@link #update()}.
     * <p>
     * 
     * This is useful either when the class requires Java serialization, e.g.,
     * {@link HashMap}, or when an explicit serializer is always specified for
     * that class. Such classes may be registered in advance to reduce the #of
     * times that the {@link IExtensibleSerializer} must be updated against the
     * store.
     * <p>
     * 
     * @param cl
     *            The class to be registered.
     * 
     * @return true iff the state of this serializer was changed (implying that
     *         {@link #update()} should be called).
     */
    
    synchronized protected boolean _registerClass( Class cl )
//    	throws IOException
    {

        registerSerializers();
        
        final String name = cl.getName();
        
        Integer classId = (Integer) m_className.get( name );
        
        if( classId == null ) {

            // The class has not been registered yet.
            
            classId = new Integer( getNextClassId() );
	
            m_className.put( name, classId );
	            
            m_classId.put( classId, name );
        
//            // @release comment out for release.
//            System.err.println
//            	( "New class: Assigning classId="+classId+" to "+cl.getName()
//                  );
            
            return true;
            
        }
        
        return false;

    }
	    
    synchronized public String getClassName( int classId )
//	throws IOException
    {
        
        // Note: I considered and rejected caching the Class instead of the class
        // since it would be one less lookup.  My rational is that we could then
        // see {@link ClassNotFoundException} during deserialization of the {@link
        // IExtensibleSerializer}.  However think that it is more robust that such
        // exceptions occur when trying to deserialize a specific object using
        // the serializer than when trying to deserialize the serializer itself.
	
	if( classId == NativeType.NULL ) return null;
	
        registerSerializers();
	
	String name = (String) m_classId.get
	    ( new Integer( classId )
	      );

	if( name == null ) {
	            
	    throw new IllegalArgumentException
		( "Unknown classId="+classId
		  );
	            
	}
	        
	return name;
	        
    }
    
    synchronized public Class getClass( int classId )
//        throws IOException
    {

        String className = getClassName( classId );
	    
        Class cl = null;
	    
        try {
	        
            cl = Class.forName( className );
	        
        }
	    
        catch( ClassNotFoundException ex ) {
	        
            throw new RuntimeException
               ( "class="+className,
                 ex
		     );
	        
        }

        return cl;
        
    }
	    
    synchronized public ISerializer getSerializer( int classId, short versionId )
//    	throws IOException
    {

	registerSerializers();

	Map versions = (Map) m_serializer.get
	    ( new Integer( classId )
	      );
	
	if( versions == null ) {
	    
	    return null;
	    
	}

	String className = (String) versions.get
	    ( new Short( versionId )
	      );
	
	if( className != null ) {

	    ISerializer serializer = (ISerializer) _cache.get
	        ( className
	          );
	    
	    if( serializer == null ) {
	    
	        Class cl;

	        try {
	        
	            cl = Class.forName( className );

	        }
	    
	        catch( ClassNotFoundException ex ) {
	            
	            throw new RuntimeException( ex );

	        }

	        // Create a new instance.
	        serializer = (ISerializer) newInstance( cl );
	        
	        // Cache the instance.
	        _cache.put( className, serializer );
	        
	    }
	    
	    return serializer;
	    
	}
	
	return null;
	
    }

    /**
     * Writes the state of the extensible serializer using a plain text
     * format.
     * 
     * @param ps Where to write the state of the serializer.
     * 
     * @todo Provide for reading the state of the extensible serializer from a plain
     *       text format.
     */
    
    synchronized public void writeOn( PrintStream ps )
    {
        ps.println("# Grammar: (classId -> className ([ versionId -> serializer ])*;)*");
        if( m_classId == null ) return; // state has not been initialized.
	Iterator itr = m_classId.entrySet().iterator();
	while( itr.hasNext() ) {
	    Map.Entry entry = (Map.Entry)itr.next();
	    Integer classId = (Integer) entry.getKey();
	    String className = (String) entry.getValue();
	    ps.print( classId + "\t->\t" + className );       
	    Map versions = (Map) m_serializer.get(classId);
	    if( versions != null && versions.size() > 0 ) {
	        ps.println("");
	        Iterator itr2 = versions.entrySet().iterator();
		while( itr2.hasNext() ) {
		    Map.Entry entry2 = (Map.Entry) itr2.next();
		    Short versionId = (Short) entry2.getKey();
		    String serializerClass = (String) entry2.getValue();
		    ps.println("[ "+versionId+"\t->\t"+serializerClass+" ]");
		}
		ps.println( ";" );
            } else {
                ps.println( " ;" );
            }
	}
    }

    //************************************************************
    //*********************** Externalizable *********************
    //************************************************************

    /**
     * Version# for how this class serializes itself. Any historical version may
     * be read. The {@link #VERSION} will always be written. This mechanism may
     * be used to transparently version the serialization of the extensible
     * serializer itself and to migrate among those serialization versions.
     */

    static final private transient short VERSION = 0;
    
    synchronized public void writeExternal(ObjectOutput out)
 	throws IOException
    {
	out.writeShort( VERSION );
	switch( VERSION ) {
	case 0: writeExternal0( out ); break;
	default:
	    throw new IOException( "Unknown version="+VERSION );
	}
//	// @release comment out for release.
//        System.err.println
//	    ( "Wrote state: #classes="+getClassCount()
//	      );
    }

    synchronized private void writeExternal0(ObjectOutput out)
    	throws IOException
    {
        out.writeInt( m_nextClassId );
	out.writeObject( m_classId ); // Map
	out.writeObject( m_className ); // Map
	out.writeObject( m_serializer ); // Map
    }

    synchronized public void readExternal(ObjectInput in)
 	throws IOException,
 	       ClassNotFoundException
    {
	short version = in.readShort();
	switch( version ) {
	case 0: readExternal0( in ); break;
	default:
	    throw new IOException
		( "Unknown version="+version
		  );
	}
//	// @release comment out for release.
//	System.err.println
//            ( "Restored state: "+getClassCount()+" registered classes."
//              );
//	writeOn( System.out );
    }

    private void readExternal0(ObjectInput in)
 	throws IOException,
 	       ClassNotFoundException
    {
        m_nextClassId = in.readInt();
	m_classId = (HashMap) in.readObject();
	m_className = (HashMap) in.readObject();
	m_serializer = (HashMap) in.readObject();
    }
    
    /**
     * Provides compact versionable stream-oriented serialization for compound
     * records.
     * <p>
     * A concrete implementation of this class must implement
     * {@link DataOutput#writePackedOId(long)}.
     * 
     * @author thompsonbry
     * @version $Id: AbstractExtensibleSerializer.java,v 1.2 2010/02/07 18:29:49 olivier_smadja Exp $
     */

    public abstract static class DataOutputStream
	extends java.io.DataOutputStream
        implements jdbm.extser.DataOutput
    {
        
        private final ByteArrayOutputStream _baos;
        
        private final long _recid;
        private final IExtensibleSerializer _serializer;
        
        final public long getRecid() {
            return _recid;
        }

        final public IExtensibleSerializer getSerializationHandler() {
            return _serializer;
        }

        public int writePackedLong( long val ) throws IOException {
            return LongPacker.packLong( this, val );
        }

        public int writePackedInt( int val ) throws IOException {
            return writePackedLong( (long) val );
        }

        public int writePackedShort( short val ) throws IOException {
            return ShortPacker.packShort( this, val );
        }

        protected DataOutputStream( long recid, IExtensibleSerializer serializer, ByteArrayOutputStream baos)
    	    throws IOException
        {
        
            super(baos);
            _recid = recid;
            _serializer = serializer;
            
            // Used to report on the size of the serialized data.
            _baos = baos;
            
        }

        /**
         * Implementation coordinates with the {@link IExtensibleSerializer}.
         * 
         * @param obj
         *            The object to be serialized.
         */
        
        public void serialize( Object obj )
            throws IOException
        {

    	    final IExtensibleSerializer ser = getSerializationHandler();

    	    // Handle null, Java primitives, and arrays of Java primitives. 
    	    int classId = NativeType.getNativeType( obj );
    	    if( classId >= NativeType.NULL && classId <= NativeType.OBJECT_ARRAY ) {
	        // A null, Java primitive, or array of Java primitives.
    	        final short versionId = 0;
    	        final boolean stateless = false;
    	        final long serializerId = 0L;
	        writeDataHeader( this, classId, versionId, false, serializerId );
	        NativeType.serialize( this, classId, obj );
	        ser.getProfiler().serialized( ser, classId, versionId, _baos.size() );
	        return;
	    }
    	    
    	    final long recid = getRecid();
    	    
	    // Get the classId from the object.
	    final Class cl = obj.getClass();
	    classId = ser.getClassId( cl );
	    
	    // True iff the object declares the Stateless interface.
	    final boolean stateless = obj instanceof Stateless;

	    if( stateless ) {
	        // If the object implements the Stateless interface then we
	        // only write the classId and the versionId and serializerId
	        // are always zero.
	        final short versionId = 0;
	        final long serializerId = 0L;
	        writeDataHeader( this, classId, versionId, stateless, serializerId );
	        ser.getProfiler().serialized( ser, classId, versionId, _baos.size() );
	        return;
	    }
	    
	    // Get the versionId from the object.
	    final short versionId = ser.getVersionId( cl );
	    
	    // Get the serializerId from the object.
	    final long serializerId = ser.getSerializerId( recid, obj );
	    
	    // Lookup the serializer object.
	    final ISerializer serializer =
		    ( serializerId != 0L
		      ? ser.getSerializer( serializerId )
		      : ser.getSerializer( classId, versionId )
		      );

	    if( serializerId != 0L && serializer == null ) {

	        // It is a serialization error if no serializer is found
	        // in the store but an explicit serializerId was requested
	        // by the instance.  The probable explanation is an application
	        // logic error.
		    
	        throw new IOException
		    ( "Serializer not found"+
		      ": classId="+classId+
		      ", class="+ser.getClassName(classId)+
		      ", serializerId="+serializerId
		      );

	    }

	    // Write the classId (and any masked bits) into the data header.

	    writeDataHeader( this, classId, versionId, stateless, serializerId );

	    if( serializer != null ) {

	        // Delegate the serialization of the object to its version specific serializer.
	        //
	        // Note: Just in case a class implements both the IStreamSerializer and
	        // the Serializer interface, we give preference to the IStreamSerializer
	        // interface since it is more flexible and more likely to be the desired
	        // interface when used in combination with the extensible serialization
	        // (BPage serialization depends on this).
	    
	        if( serializer instanceof IStreamSerializer ) {
	            
	            // Serialize the object onto the underlying stream.

	            ((IStreamSerializer)serializer).serialize( this, obj );
	            
		    ser.getProfiler().serialized( serializer, classId, versionId, _baos.size() );
	            
	        } else if( serializer instanceof ISimpleSerializer ) {

	            // Serialize the object.
	            byte[] data = ((ISimpleSerializer)serializer).serialize( obj );

	            // Write the #of serialized bytes onto the stream.  We need this in order
	            // to de-serialize the object when using a {@link IStreamSerializer} on a
	            // compound record.
	            final int length = data.length;
	            writePackedInt( data.length );
	            
	            // Copy the serialized data.
	            write( data );

	            ser.getProfiler().serialized( serializer, classId, versionId, _baos.size() );

	        } else {
	            
	            throw new IOException
	                ( "Unknown ISerializer family: "+serializer.getClass()
	                  );
	            
	        }

	    } else {

		// We have to fall back on some sort of general purpose serialization
	        // using an ObjectOutputStream.

		ObjectOutput oos = new java.io.ObjectOutputStream( this );

		if( obj instanceof Externalizable ) {
            
		    ((Externalizable)obj).writeExternal( oos );
                    
		} else if( obj instanceof Serializable ) {
                    
		    oos.writeObject( obj );
                    
		} else {
                    
		    throw new IOException
			( "Do not know how to serialize: "+obj.getClass()
			  );
                    
		}

		// Make sure that the serialized state is flushed!
		oos.flush();
		oos.close();

		ser.getProfiler().serialized( ser, classId, versionId, _baos.size() );

	    }

        }

    }

    /**
     * Exposes {@link ByteArrayInputStream#pos}.
     * 
     * @author thompsonbry
     */
    
    private class MyByteArrayInputStream extends ByteArrayInputStream
    {

        public MyByteArrayInputStream( byte[] data ) {
            super( data );
        }
        
        /**
         * Return the current position (#of bytes read).
         */
        int position() {return pos;}
        
    }
    
    /**
     * Provides compact versionable stream-oriented de-serialization for
     * compound records.
     * <p>
     * Concrete implementations of this class must implement
     * {@link DataInput#readPackedOId()}.
     * 
     * @author thompsonbry
     * @version $Id: AbstractExtensibleSerializer.java,v 1.2 2010/02/07 18:29:49 olivier_smadja Exp $
     */

    public abstract static class DataInputStream
        extends java.io.DataInputStream
        implements jdbm.extser.DataInput
    {

        private final MyByteArrayInputStream _bais;
        
        private final long _recid;
        private final IExtensibleSerializer _serializer;

        final public long getRecid() {
            return _recid;
        }

        final public IExtensibleSerializer getSerializationHandler() {
            return _serializer;
        }

        /**
         * 
         * @param recid The object identifier.
         * @param serializer The extensible serializer.
         * @param is The source from which to read the record to be deserialized.
         * 
         * @throws IOException
         */
        
        protected DataInputStream( long recid, IExtensibleSerializer serializer, ByteArrayInputStream is )
            throws IOException
        {
            
            super( is );

            _recid = recid;
            _serializer = serializer;
            
            /*
             * Used to report statistics to the profiler. This requires a
             * specific implementation which is always provided by the calling
             * harness.
             */
            _bais = (MyByteArrayInputStream) is;

        }

        public long readPackedLong() throws IOException {
            return LongPacker.unpackLong( this );
        }

        public int readPackedInt() throws IOException {
            return (int) readPackedLong();
        }

        public short readPackedShort() throws IOException {
            return ShortPacker.unpackShort( this );
        }

        /**
         * Implementation coordinates with the {@link AbstractExtensibleSerializer}.
         */
        
        public Object deserialize()
	    throws IOException
        {
	    
	    // The classId is a (packed) int.
	    int classId = readPackedInt();

	    final boolean hasVersionId    = ( classId & VERSION_MASK ) != 0;
	    final boolean hasSerializerId = ( classId & SERIALIZER_MASK ) != 0;
	    final boolean stateless       = ( classId & STATELESS_MASK ) != 0;

	    final short versionId =
		( hasVersionId
		  ? readPackedShort()
		  : 0
		  );

	    final long serializerId =
		( hasSerializerId
		  ? readPackedLong()
		  : 0L
		  );
    	
	    // Clear out the masked bits.
//	    classId = classId & ~( VERSION_MASK | SERIALIZER_MASK | STATELESS_MASK );
	    classId = classId>>NBITFIELDS;

	    final IExtensibleSerializer ser = getSerializationHandler();

	    if( classId >= NativeType.NULL && classId <= NativeType.OBJECT_ARRAY ) {
    	    	// Deserialize a null, Java primitive, or array of Java primitives.
	        final Object obj = NativeType.deserialize( this, classId, versionId );
	        ser.getProfiler().deserialized( ser, classId, versionId, _bais.position() );
	        return obj;
	    }
    	
	    final long recid = getRecid();
	    final int nclasses = ser.getClassCount();
    	
	    if( classId <= 0 || classId > nclasses ) {
    	    	// Sanity check.  Make sure that the classId corresponds to some known class.
		throw new IOException
		    ( "Invalid header: classId="+classId+", but only [1:"+nclasses+"] are valid."
		      );
    	    
	    }

	    if( stateless ) {
	        // If the object was serialized as "stateless" then we create
	        // a new instance using the zero-argument public constructor
	        // and we are done (Stateless objects are serialized without
	        // any state).
	        Class cl = ser.getClass( classId );
	        Object obj = newInstance( cl );
	        ser.getProfiler().deserialized( ser, classId, versionId, _bais.position() );
	        return obj;
	    }
	    
	    // The size of the data header for this record.
	    final int headerSize = 4 + // [int] classId
		( hasVersionId ?2 :0 ) + // [short] versionId
		( hasSerializerId ?8 :0 ) // [long] serializerId
		;

	    // Lookup the serializer object.
	    final ISerializer serializer =
		( serializerId != 0L
		  ? ser.getSerializer( serializerId )
		  : ser.getSerializer( classId, versionId )
		  );

	    if( serializerId != 0L && serializer == null ) {

		// It is a serialization error if no serializer is found
		// in the store but an explicit serializerId was present
		// in the header.  One explanation would be that the record
		// containing the serializer had been deleted.
    	    
		throw new IOException
		    ( "Serializer not found"+
		      ": classId="+classId+
		      ", class="+ser.getClassName(classId)+
		      ", serializerId="+serializerId
		      );

	    }

	    if( versionId != 0 && serializer == null ) {
    	 
		// It is a serialization error if no serializer is
		// registered for the current versionId when a class
		// has an explicit versionId (since you can not register
		// a 'null' serializer there must be an explicitly
		// registered serializer).  One explanation would be a
		// corrupt header.  Another would be that the state of
		// the IExtensibleSerializer has become corrupt.
    	    
		throw new IOException
		    ( "Serializer not registered"+
		      ": classId="+classId+
		      ", class="+ser.getClassName(classId)+
		      ", versionId="+versionId
		      );

	    }
    	
	    if( serializer != null ) {

		if( serializer instanceof ISimpleSerializer ) {
		    //
		    // Apply the serializer to just those bytes which contain
		    // the segment of the serialized record corresponding to
		    // the next serialized object.
		    //
		    int dataSize = readPackedInt();
		    byte[] serialized = new byte[ dataSize ];
		    readFully( serialized );
		    Object obj = ((ISimpleSerializer)serializer).deserialize( serialized );
		    ser.getProfiler().deserialized( serializer, classId, versionId, _bais.position() );
		    return obj;
    	        
		} else if( serializer instanceof IStreamSerializer ) {
		    //
		    // We know the exact class to instantiate, so we do
		    // that and hand the instance to the IStreamSerializer
		    // which is responsible for populating its fields.
		    //
		    Class cl = ser.getClass( classId );
		    Object obj = newInstance( cl );
		    obj = ((IStreamSerializer)serializer).deserialize( this, obj );
		    ser.getProfiler().deserialized( serializer, classId, versionId, _bais.position() );
		    return obj;
		    
		} else {
    	        
		    throw new IOException
			( "Unknown ISerializer family: "+serializer.getClass()
			  );
    	        
		}

	    } else {

		//
		// Lookup the Class from the classId.
		//
        
		Class cl = ser.getClass( classId );
    	    
		// At this point we need an ObjectInputStream to read the
		// state of the serialized instance.
    	    
		ObjectInput ois = new java.io.ObjectInputStream( this );

		if( Externalizable.class.isAssignableFrom( cl ) ) {

		    // Special case classes that implement Externalizable.
		    // Create an instance of the class using the required
		    // public zero argument de-serialization constructor
		    // and then read the state of the new instance using 
		    // its Externalizable implementation.

		    Object obj = newInstance( cl );
    	        
		    try {

			((Externalizable)obj).readExternal( ois );

			int nbytes = _bais.position();
			    
			ser.getProfiler().deserialized( ser, classId, versionId, nbytes );

			return obj;
    		    
		    }
    	        
		    catch( ClassNotFoundException ex ) {
    			            
			throw new RuntimeException
			    ( "While deserializing class="+ser.getClassName( classId ),
			      ex
			      );
    		        
		    }
    	        
		} else if( Serializable.class.isAssignableFrom( cl ) ) {
    	        
		    // Use default Java serialization.
		    //
		    // Note: This is the worst case scenario.  It should
		    // only happen when there is not registered
		    // serialization mechanism for an class and the class
		    // does not implement the Externalizable interface so
		    // we are left only with default Java serialization.

		    try {
    	            
			Object obj = ois.readObject();

			ser.getProfiler().deserialized( ser, classId, versionId, _bais.position() );

		        return obj;

		    }
    	        
		    catch( ClassNotFoundException ex ) {
    	        
			throw new RuntimeException
			    ( "While deserializing class="+ser.getClassName( classId ),
			      ex
			      );
    	            
		    }
    	        
		} else {
    	     
		    // We have run out of options.  This should never happen since
		    // we managed to serialize the object in the first place.
    	        
		    throw new UnsupportedOperationException
			( "Do not know how to deserialize: class="+ser.getClassName( classId )
			  );
    	        
		}

	    }
	    
        }

    }

}

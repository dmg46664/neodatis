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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;

import jdbm.extser.AbstractExtensibleSerializer.DataInputStream;
import jdbm.extser.AbstractExtensibleSerializer.DataOutputStream;
import jdbm.extser.profiler.Profiler;



/**
 * Interface for an extensible serialization handler.
 * <p>
 * 
 * The extensible serialization handler that figures out how to serialize and
 * de-serialize objects using a metadata header for for each serialized object.
 * Tuned serialization is provided for the Java classes corresponding to the
 * primitive datatypes ({@link Long},{@link String}, etc.), the
 * {@link Stateless} interface, and the {@link Externalizable} interface. Custom
 * serializers may be written using either the {@link ISimpleSerializer} or the
 * {@link IStreamSerializer} interface. Serializer registrations are persistent.
 * <p>
 * 
 * A serializer is registered for a Java Class and a versionId. The default
 * versionId is zero(0). The serializer registered against a given version is
 * always used to deserialize that version of a class. An instance of a class is
 * serialized using the default versionId (0) unless a versionId is explicitly
 * declared using:
 * 
 * <pre>
 * static public short SERIAL_VERSION_ID = { versionId };
 * </pre>
 * 
 * <p>
 * 
 * A persistent serializer may be explicitly specified on a per instance basis
 * using the {@link SerializerIdProtocol}.
 * <p>
 * 
 * @todo Replace field for versionId with VersionIdProtocol interface? This
 *       would let us drop the versionId cache and make it viable to have the
 *       versionId be specified as a class method or an instance method.
 * 
 * @author thompsonbry
 * @version $Id: IExtensibleSerializer.java,v 1.1 2009/12/18 22:44:58 olivier_smadja Exp $
 */

public interface IExtensibleSerializer extends ISerializer
{
    
    /**
     * The name of the optional public field coding the versionId on a class
     * whose instance is being (de-)serialized. When the field is not present
     * the versionId is zero (0). When present the field must be a <em>short</em>
     * value coding the versionId under which the class will be serialized.
     * 
     * <pre>
     * SERIAL_VERSION_ID
     * </pre>
     */

    static public final String VERSION_FIELD_NAME = "SERIAL_VERSION_ID";

    /**
     * Return the versionId used to serialize an instance of <i>cl </i>. A class
     * denotes the versionId used when it is serialized by making a declaration:
     * 
     * <pre>
     * static public short SERIAL_VERSION_ID = { versionId };
     * </pre>
     * 
     * The modifier <em>final</em> is normally used but not strictly required.
     * <p>
     * 
     * The versionId associated with a Class is cached.
     * <p>
     * 
     * @param cl
     *            The Class whose instance is being serialized (required).
     * 
     * @return The versionId or zero if the object is unversioned.
     * 
     * @see #VERSION_FIELD_NAME
     * @see #invalidateVersionIdCache(Class cl)
     */
    public short getVersionId(Class cl);

    /**
     * Return the recid of the {@link ISerializer} to be fetched and used to
     * (de-)serialize the object or zero (0L) if there is no serializer instance
     * to be used. A class may choose to report a non-zero <code>serializerId</code>
     * by implementing the {@link SerializerIdProtocol} interface.
     * 
     * @param obj
     *            The object being serialized. (The serializerId is read from a
     *            binary header during deserialization.)
     * 
     * @return The recid of the {@link ISerializer} to be fetched or 0L if there
     *         is no per-instance persistent serializer.
     */
    public long getSerializerId(long recid, Object obj);

    /**
     * Return the #of registered classes.  Classes may be explicitly
     * registered against one or more serializers or may be implicitly
     * registered as they are encountered during serialization.
     * 
     * @return The #of registered classes.
     */
    public int getClassCount();

    /**
     * Return a unique classId for the object. The class name for the classId
     * may be obtained using {@link #getClassName( int classId)}. If the class
     * was not already registered, then it is registered now.
     * 
     * @param cl
     *            The class (required).
     * 
     * @return A classId. Note that <code>classId == 0</code> is reserved to
     *         indicate a <code>null</code> reference.
     */
    public int getClassId(Class cl);

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
    public boolean registerClass(Class cl);

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
    public boolean registerSerializer(Class cl, Class serializerClass);

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
    public boolean registerSerializer(Class cl, Class serializerClass,
            short versionId);

    /**
     * Decodes a classId into the class name.
     * 
     * @param classId
     *            A classId.
     * 
     * @return The name of the class or <code>null</code> iff <i>classId ==
     *         {@link NativeType#NULL}</i>.
     * 
     * @see #getClassId( Class cl )
     * 
     * @exception IllegalArgumentException
     *                if <i>classId </i> was not registered.
     */
    public String getClassName(int classId);

    /**
     * Return the {@link Class} given the classId.
     * 
     * @param classId The classId.
     * 
     * @return The class.
     */
    public Class getClass(int classId);

    /**
     * Return the {@link ISerializer} registered against <i>classId </i>.
     * 
     * @param classId
     *            The classId.
     * 
     * @return The serializer or <code>null</code> if no serializer is
     *         registered for that class and <i>versionId </i>.
     */
    public ISerializer getSerializer(int classId, short versionId);

    /**
     * Return the {@link ISerializer} from the persistence layer.
     * 
     * @param recid The logical row id for the serializer.
     * 
     * @return The serializer.
     */
    public ISerializer getSerializer( long recid );

    /**
     * Return the serialization handler profiler.
     */
    public Profiler getProfiler();

    /**
     * Serialize an object.
     * 
     * @param recid The logical row id of the object and zero (0L) iff the
     * object is being inserted.
     * 
     * @param obj The object.
     * 
     * @return A serialized representation of the object.
     * 
     * @throws IOException
     */
    public byte[] serialize( long recid, Object obj )
    	throws IOException;

    /**
     * Deserialize an object.
     * 
     * @param recid The logical row id of the object.
     * 
     * @param serialized A serialized representation of the object.
     * 
     * @return The deserialized object.
     * 
     * @throws IOException
     */
    public Object deserialize( long recid, byte[] serialized )
	throws IOException;

    /**
     * Return a {@link DataOutputStream} that will be used to serialize the
     * state of the given object.
     * 
     * @param recid
     *            The object identifier.
     * 
     * @param baos The underlying stream onto which the serialized data will
     *             be written.
     * 
     * @return The {@link DataOutputStream} used to serialize the state of
     *         that object.
     */

    public DataOutputStream getDataOutputStream( long recid, ByteArrayOutputStream baos )
    	throws IOException;

    /**
     * Return a {@link DataInputStream} that will be used to deserialize an
     * object from its serialized state.
     * 
     * @param recid
     *            The object identifier.
     * 
     * @param bis
     *            The underlying source from which the serialized data will be
     *            read.
     * 
     * @return The {@link DataInputStream} used to deserialize the object state. 
     */

    public DataInputStream getDataInputStream( long recid, ByteArrayInputStream bis )
    	throws IOException;
    
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

    public void invalidateVersionIdCache( Class cl );

}

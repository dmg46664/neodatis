/*
 NeoDatis ODB : Native Object Database (odb.info@neodatis.org)
 Copyright (C) 2007 NeoDatis Inc. http://www.neodatis.org

 "This file is part of the NeoDatis ODB open source object database".

 NeoDatis ODB is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 NeoDatis ODB is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.neodatis.odb.core.layers.layer3;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.CoreProvider;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.layers.layer2.meta.AbstractObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.ArrayObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.AtomicNativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.AttributeIdentification;
import org.neodatis.odb.core.layers.layer2.meta.AttributeValuesMap;
import org.neodatis.odb.core.layers.layer2.meta.ClassAttributeInfo;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.layers.layer2.meta.CollectionObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.EnumNativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.LazyObjectReference;
import org.neodatis.odb.core.layers.layer2.meta.MapObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.MetaModel;
import org.neodatis.odb.core.layers.layer2.meta.NativeAttributeHeader;
import org.neodatis.odb.core.layers.layer2.meta.NativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeNullObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.NullNativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.ODBType;
import org.neodatis.odb.core.layers.layer2.meta.ObjectInfoHeader;
import org.neodatis.odb.core.layers.layer2.meta.ObjectReference;
import org.neodatis.odb.core.layers.layer4.StorageEngine;
import org.neodatis.odb.core.query.criteria.NnoiMatchResult;
import org.neodatis.odb.core.session.Session;
import org.neodatis.tool.DLogger;
import org.neodatis.tool.DisplayUtility;
import org.neodatis.tool.wrappers.OdbString;
import org.neodatis.tool.wrappers.list.IOdbList;
import org.neodatis.tool.wrappers.list.OdbArrayList;

import com.sun.jndi.toolkit.ctx.Continuation;

/**
 * Manage all IO writing
 * 
 * @author olivier s
 * 
 * 
 * 
 * 
 */
public class Layer3ReaderImpl extends Layer3Converter implements Layer3Reader {

	public static final String LOG_ID = "Layer3Reader";
	public static final String LOG_ID_DEBUG = "Layer3Reader.debug";

	protected DataConverter byteArrayConverter;
	protected StorageEngine layer4;
	private Session session;
	private boolean debug;

	public Layer3ReaderImpl(Session session, StorageEngine storageEngine) {
		super();
		this.session = session;
		this.layer4 = storageEngine;
		CoreProvider provider = session.getConfig().getCoreProvider();

		this.byteArrayConverter = provider.getByteArrayConverter(session.getConfig().debugLayers(), session.getConfig().getDatabaseCharacterEncoding(), session
				.getConfig());

		NATIVE_HEADER_BLOCK_SIZE_BYTE = new byte[4];
		Bytes b = new BytesImpl();
		byteArrayConverter.intToByteArray(NATIVE_HEADER_BLOCK_SIZE, b, 0, "init block size");
		NATIVE_HEADER_BLOCK_SIZE_BYTE = b.extract(0, 4);
		debug = session.getConfig().debugLayers();

	}

	public NonNativeObjectInfo metaFromBytes(OidAndBytes oab, boolean full, int depth) {
		ConversionContext cc = new ConversionContext();
		cc.setDepth(depth);
		return internalFromBytes(oab, full, cc, 1);
	}

	public NonNativeObjectInfo metaFromBytes(IOdbList<OidAndBytes> oabs, boolean full, Map<OID, OID> oidsToReplace, int depth) {
		ConversionContext cc = new ConversionContext(oabs, oidsToReplace);
		cc.setDepth(depth);
		// last element of the list represents the main object
		return internalFromBytes(oabs.get(0), full, cc, 1);
	}

	/**
	 * Converts bytes to a nonNativeObject
	 * 
	 * @param oab
	 * @param full
	 *            if false, only load the header, else load all
	 * @param context
	 * @return
	 */
	protected NonNativeObjectInfo internalFromBytes(OidAndBytes oab, boolean full, ConversionContext context, int currentDepth) {

		if (debug) {
			DLogger.debug(String.format("\n** Converting bytes of oid %s to NNOI", oab.oid));
		}

		NonNativeObjectInfo existingNnoi = context.alreadyConvertedOids.get(oab.oid);
		if (existingNnoi != null) {
			// There is a cyclic reference and the nnoi for this oid is already
			// being constructed
			return new ObjectReference(existingNnoi);
		}

		ReadSize readSize = new ReadSize();
		Bytes bytesValues = oab.bytes;
		BytesHelper bytes = new BytesHelper(bytesValues, session.getConfig().debugLayers(), session.getConfig());

		int blockType = bytes.readInt(readSize.get(), readSize, "object type");
		ObjectOid oid = bytes.readObjectoid(readSize.get(), readSize, "oid");

		// if current depth is greater that requested, just return a reference
		if (context.getDepth() != 0 && currentDepth > context.getDepth()) {
			ObjectOid ooid = (ObjectOid) oab.oid;
			return new LazyObjectReference(ooid, session.getMetaModel().getClassInfoFromId(oid.getClassOid()));
		}

		long creation = bytes.readLong(readSize.get(), readSize, "creation");
		long update = bytes.readLong(readSize.get(), readSize, "update");
		long version = bytes.readLong(readSize.get(), readSize, "version");
		boolean isSynchronized = bytes.readBoolean(readSize.get(), readSize, "is ext sync?");

		// skip 2 reserved longs
		readSize.add(LONG_SIZE * 2);

		int nbAttributes = bytes.readInt(readSize.get(), readSize, "nb atributes");
		int headerPositionInfo = bytes.readInt(readSize.get(), readSize, "header position");

		if (debug) {
			DLogger.debug("  ** Start reading attributes header at " + headerPositionInfo);
		}

		ReadSize headerReadSize = new ReadSize();
		AttributeIdentification[] attributeIdentifications = bytesToHeader(bytes.bytes, nbAttributes, headerPositionInfo, headerReadSize, true);

		long totalObjectSize = bytes.readInt(headerPositionInfo + headerReadSize.get(), headerReadSize, "object size");
		int crc = bytes.readInt(headerPositionInfo + headerReadSize.get(), headerReadSize, "crc");

		int attributesPosition = readSize.get();

		if (debug) {
			DLogger.debug("  ** Start reading attributes values at " + attributesPosition);
		}

		if (!oid.equals(oab.oid)) {
			throw new NeoDatisRuntimeException(NeoDatisError.LAYER3_OID_DOES_OT_MATCH.addParameter(oab.oid).addParameter(oid).addParameter(POS_OBJECT_OID));
		}

		// replace client oid by server oid
		oab.oid = context.replaceOid(oab.oid);
		oid = (ObjectOid) oab.oid;

		ClassInfo ci = session.getMetaModel().getClassInfoFromId(oid.getClassOid());
		if (ci == null) {
			System.out.println("debug:ci is null for oid=" + oid);
		}

		NonNativeObjectInfo mainNnoi = new NonNativeObjectInfo(ci);
		mainNnoi.setOid(oid);
		ObjectInfoHeader oih = mainNnoi.getHeader();
		oih.setCreationDate(creation);
		oih.setObjectVersion(version);
		oih.setUpdateDate(update);
		oih.setAttributesIdentification(attributeIdentifications);
		oih.setAttributePosition(attributesPosition);
		context.alreadyConvertedOids.put(oid, mainNnoi);

		if (!full) {
			return mainNnoi;
		}

		// Then read attribute identifications
		// The header already contains each attribute offsets (ai.offset)
		for (int i = 0; i < nbAttributes; i++) {
			AttributeIdentification ai = attributeIdentifications[i];

			if (ai.isNative) {
				NativeObjectInfo noi = bytesToNativeObject(bytes.bytes, ai.offset, readSize, context, currentDepth);
				mainNnoi.setAttributeValue(ai.id, noi);
			} else {
				if (ai.oid.isNull()) {
					// just read the null oid to verify
					ObjectOid aoid = bytes.readObjectoid(readSize.get(), readSize, "oid");
					if (!aoid.isNull()) {
						throw new NeoDatisRuntimeException(NeoDatisError.LAYER3_OID_DOES_OT_MATCH.addParameter("null").addParameter(aoid).addParameter(
								readSize.get()));
					}
					mainNnoi.setAttributeValue(ai.id, new NonNativeNullObjectInfo());
				} else {
					// Check if Oab (OidandBytes) is in the context
					OidAndBytes oab2 = context.getOabWithOid(ai.oid);
					if (oab2 == null) {
						oab2 = layer4.read(ai.oid, true);
					}

					ObjectOid aoid = bytes.readObjectoid(readSize.get(), readSize, "oid");
					if (!ai.oid.equals(aoid)) {
						throw new NeoDatisRuntimeException(NeoDatisError.LAYER3_OID_DOES_OT_MATCH.addParameter(ai.oid).addParameter(aoid).addParameter(
								readSize.get()).addParameter(oid).addParameter(mainNnoi.getClassInfo().getFullClassName()).addParameter(
								mainNnoi.getClassInfo().getAttributeInfoFromId(ai.id).getName()));
					}
					if (oab2 != null) {
						NonNativeObjectInfo nnoi = internalFromBytes(oab2, true, context, currentDepth + 1);
						mainNnoi.setAttributeValue(ai.id, nnoi);
					} else {
						// oab2 can be null if object with ai.id has been
						// deleted from database
						mainNnoi.setAttributeValue(ai.id, new NonNativeNullObjectInfo());
					}
				}
			}
		}

		// check crc
		int computedCrc = computeCrc(bytesValues);

		// check crc
		if (crc != computedCrc) {
			throw new NeoDatisRuntimeException(NeoDatisError.LAYER3_CRC_DOES_OT_MATCH.addParameter(oid).addParameter(crc).addParameter(computedCrc));
		}

		// Updates attributes identification in the object info header
		mainNnoi.getHeader().setAttributesIdentification(attributeIdentifications);

		if (debug) {
			DLogger.debug(String.format("  Object with OID %s has a size of %d", oid, totalObjectSize));
			DLogger.debug("  Attributes Identification of object with oid " + oid + " are " + DisplayUtility.toString(attributeIdentifications));
			DLogger.debug("** End Reading non native object with oid " + oid);
		}
		return mainNnoi;
	}

	private int computeCrc(Bytes bytesValues) {
		// TODO Auto-generated method stub
		return 0;
	}

	public AttributeValuesMap valuesFromBytes(OidAndBytes oab, HashSet<String> attributeNames, int depth) {
		ConversionContext cc = new ConversionContext();
		cc.setDepth(depth);
		return internalValuesFromBytes(oab, null, attributeNames, cc, 1);

	}

	protected AttributeValuesMap internalValuesFromBytes(OidAndBytes oab, String rootAttributeName, HashSet<String> attributeNames, ConversionContext context,
			int currentDepth) {

		if (debug) {
			DLogger.debug(String.format("Converting bytes of oid %s to Values for fields %s", oab.oid, attributeNames.toString()));
		}
		ReadSize readSize = new ReadSize();
		Bytes bytes = oab.bytes;

		ObjectOid oid = (ObjectOid) oab.oid;
		// reads the header of the object
		ObjectInfoHeader oih = internalFromBytes(oab, false, context, 0).getHeader();
		// Retrieve attribute identifications
		AttributeIdentification[] attributeIdentifications = oih.getAttributesIdentification();

		ClassInfo ci = session.getMetaModel().getClassInfoFromId(oih.getClassInfoId());
		AttributeValuesMap values = new AttributeValuesMap();
		values.setOid(oab.oid);

		// organize the attributes. group the attributes
		// for example if attributeNames=[name,email,profile.name,profile.type]
		// this will create an entry (key=null) for [name,email] and an entry
		// with profile with set[name,type]
		Map<String, HashSet<String>> group = new HashMap<String, HashSet<String>>();
		HashSet<String> set = null;
		for (String attributeName : attributeNames) {

			boolean mustNavigate = attributeName.indexOf(".") != -1;
			if (mustNavigate) {
				set = group.get(attributeName);
				int firstDotIndex = attributeName.indexOf(".");
				String relationAttributeName = OdbString.substring(attributeName, firstDotIndex + 1);
				String singleAttributeName = OdbString.substring(attributeName, 0, firstDotIndex);
				if (set == null) {
					set = new HashSet<String>();
					group.put(singleAttributeName, set);
				}
				set.add(relationAttributeName);
			} else {
				set = group.get(null);
				if (set == null) {
					set = new HashSet<String>();
					group.put(null, set);
				}
				set.add(attributeName);
			}
		}

		// prepare a map for faster access
		Map<String, AttributeIdentification> attributesByName = new HashMap<String, AttributeIdentification>();
		for (AttributeIdentification ai : attributeIdentifications) {
			ClassAttributeInfo cai = ci.getAttributeInfoFromId(ai.id);
			if (cai != null) {
				attributesByName.put(cai.getName(), ai);
			}
		}
		// now for each field
		Iterator<String> iterator = group.keySet().iterator();
		while (iterator.hasNext()) {
			String attributeName = iterator.next();
			// manage direct attributes
			if (attributeName == null) {
				HashSet<String> set2 = group.get(null);
				Iterator<String> iterator2 = set2.iterator();
				while (iterator2.hasNext()) {
					String aname = iterator2.next();
					ClassAttributeInfo cai = ci.getAttributeInfoFromName(aname);

					if (cai == null) {
						throw new NeoDatisRuntimeException(NeoDatisError.CRITERIA_QUERY_UNKNOWN_ATTRIBUTE.addParameter(aname).addParameter(
								ci.getFullClassName()));
					}

					if (cai.isNative()) {
						AttributeIdentification ai = attributesByName.get(aname);

						if (ai == null) {
							// attribute does not exist on the object (there
							// must have been a refactoring
							continue;
						}
						NativeObjectInfo noi = bytesToNativeObject(bytes, ai.offset, readSize, context, currentDepth);
						if (rootAttributeName == null) {
							values.put(aname, noi.getObject());
						} else {
							String fullAttributeName = new StringBuffer(rootAttributeName).append(".").append(aname).toString();
							values.put(fullAttributeName, noi.getObject());
						}
					} else {
						// put the oid
						AttributeIdentification ai = attributesByName.get(aname);
						if (rootAttributeName == null) {
							values.put(aname, ai.oid);
						} else {
							String fullAttributeName = new StringBuffer(rootAttributeName).append(".").append(aname).toString();
							values.put(fullAttributeName, ai.oid);
						}

					}
				}
			} else {
				HashSet<String> set2 = group.get(attributeName);
				ClassAttributeInfo cai = ci.getAttributeInfoFromName(attributeName);
				AttributeIdentification ai = attributesByName.get(attributeName);
				OID aoid = ai.oid;
				if (!aoid.isNull()) {
					OidAndBytes oab2 = layer4.read(aoid, true);

					String fullAttributeName = attributeName;
					if (rootAttributeName != null) {
						fullAttributeName = new StringBuffer(rootAttributeName).append(".").append(attributeName).toString();
					}

					values.putAll(internalValuesFromBytes(oab2, fullAttributeName, set2, context, currentDepth + 1));
				}
			}
		}
		return values;
	}

	/**
	 * @param bytes
	 * @param posAttributeDefinition
	 * @return
	 */
	private AttributeIdentification[] bytesToHeader(Bytes bytes, int nbAttributes, int offset, ReadSize readSize, boolean withId) {
		// read attributes header
		AttributeIdentification[] attributeIdentifications = new AttributeIdentification[nbAttributes];
		ReadSize lreadSize = new ReadSize();
		int position = offset;
		int id = 0;
		for (int i = 0; i < nbAttributes; i++) {
			id = -1;
			if (withId) {
				id = byteArrayConverter.byteArrayToInt(bytes, position + lreadSize.get(), lreadSize, "attribute id");
			}
			byte type = byteArrayConverter.byteArrayToByte(bytes, position + lreadSize.get(), lreadSize, "type is native?");
			switch (type) {
			case ATTRIBUTE_IS_NATIVE:
				int aoffset = byteArrayConverter.byteArrayToInt(bytes, position + lreadSize.get(), lreadSize, "attribute offset");
				// TODO check the size (-1)
				attributeIdentifications[i] = new AttributeIdentification(aoffset, -1);
				attributeIdentifications[i].id = id;
				break;
			case ATTRIBUTE_IS_NON_NATIVE:
				ObjectOid oid = byteArrayConverter.byteArrayToObjectOid(bytes, position + lreadSize.get(), lreadSize, "oid");
				attributeIdentifications[i] = new AttributeIdentification(oid);
				attributeIdentifications[i].id = id;
				break;
			default:
				throw new NeoDatisRuntimeException(NeoDatisError.LAYER3_UNKNOWN_ATTRIBUTE_TYPE.addParameter(type));
			}
		}

		readSize.add(lreadSize.get());
		return attributeIdentifications;
	}

	/**
	 * Write the header of a native attribute
	 * 
	 * @param odbTypeId
	 * @param isNull
	 * @param writeDataInTransaction
	 *            @
	 */
	protected NativeAttributeHeader bytesToNativeObjectHeader(Bytes bytes, int offset, ReadSize readSize) {
		int blockSize = byteArrayConverter.byteArrayToInt(bytes, offset, readSize, "block size");
		byte blockType = byteArrayConverter.byteArrayToByte(bytes, offset + 4, readSize, "block type");
		int odbTypeId = byteArrayConverter.byteArrayToInt(bytes, offset + 5, readSize, "odb type id");
		boolean isNull = byteArrayConverter.byteArrayToBoolean(bytes, offset + 9, readSize, "is null?");

		return new NativeAttributeHeader(blockSize, blockType, odbTypeId, isNull);
	}

	public Object bytesToAtomicNativeObject(int odbTypeId, Bytes bytes, int offset, ReadSize readSize) {

		Object o = null;

		switch (odbTypeId) {
		case ODBType.BYTE_ID:
		case ODBType.NATIVE_BYTE_ID:
			o = new Byte(byteArrayConverter.byteArrayToByte(bytes, offset, readSize, "byte value"));
			break;
		case ODBType.BOOLEAN_ID:
		case ODBType.NATIVE_BOOLEAN_ID:
			boolean b = byteArrayConverter.byteArrayToBoolean(bytes, offset, readSize, "bool value");
			if (b) {
				o = Boolean.TRUE;
			} else {
				o = Boolean.FALSE;
			}
			break;
		case ODBType.CHARACTER_ID:
		case ODBType.NATIVE_CHAR_ID:
			o = new Character(byteArrayConverter.byteArrayToChar(bytes, offset, readSize, "char value"));
			break;

		case ODBType.FLOAT_ID:
		case ODBType.NATIVE_FLOAT_ID:
			o = new Float(byteArrayConverter.byteArrayToFloat(bytes, offset, readSize, "float value"));
			break;
		case ODBType.DOUBLE_ID:
		case ODBType.NATIVE_DOUBLE_ID:
			o = new Double(byteArrayConverter.byteArrayToDouble(bytes, offset, readSize, "double value"));
			break;
		case ODBType.INTEGER_ID:
		case ODBType.NATIVE_INT_ID:
			o = new Integer(byteArrayConverter.byteArrayToInt(bytes, offset, readSize, "int value"));
			break;
		case ODBType.LONG_ID:
		case ODBType.NATIVE_LONG_ID:
			o = new Long(byteArrayConverter.byteArrayToLong(bytes, offset, readSize, "long value"));
			break;
		case ODBType.SHORT_ID:
		case ODBType.NATIVE_SHORT_ID:
			o = new Short(byteArrayConverter.byteArrayToShort(bytes, offset, readSize, "short value"));
			break;
		case ODBType.BIG_DECIMAL_ID:
			o = byteArrayConverter.byteArrayToBigDecimal(bytes, offset, readSize, "big decimal value");
			break;
		case ODBType.BIG_INTEGER_ID:
			o = byteArrayConverter.byteArrayToBigInteger(bytes, offset, readSize, "big integer value");
			break;
		case ODBType.DATE_ID:
			o = byteArrayConverter.byteArrayToDate(bytes, offset, readSize, "date value");
			break;
		case ODBType.DATE_SQL_ID:
			o = new java.sql.Date(byteArrayConverter.byteArrayToLong(bytes, offset, readSize, "date value"));
			break;
		case ODBType.DATE_TIMESTAMP_ID:
			o = new java.sql.Timestamp(byteArrayConverter.byteArrayToLong(bytes, offset, readSize, "date value"));
			break;
		case ODBType.DATE_CALENDAR_ID:
		case ODBType.DATE_GREGORIAN_CALENDAR_ID:
			Calendar c = Calendar.getInstance();
			c.setTime(byteArrayConverter.byteArrayToDate(bytes, offset, readSize, "date value"));
			o = c;
			break;

		case ODBType.OBJECT_OID_ID:
		case ODBType.OBJECT_OID_IMPL_UUID_ID:
		case ODBType.OBJECT_OID_IMPL_SEQ_ID:
			o = byteArrayConverter.byteArrayToObjectOid(bytes, offset, readSize, "object oid value");
			break;
		case ODBType.CLASS_OID_ID:
		case ODBType.CLASS_OID_IMPL_UUID_ID:
		case ODBType.CLASS_OID_IMPL_SEQ_ID:
			o = byteArrayConverter.byteArrayToClassOid(bytes, offset, readSize, "class oid value");
			break;

		case ODBType.STRING_ID:
			o = byteArrayConverter.byteArrayToString(bytes, true, offset, readSize, "string value");
			break;
		case ODBType.ENUM_ID:
			o = byteArrayConverter.byteArrayToString(bytes, false, offset, readSize, "enum value");
			break;
		}

		if (o == null) {
			throw new NeoDatisRuntimeException(NeoDatisError.NATIVE_TYPE_NOT_SUPPORTED.addParameter(odbTypeId).addParameter(ODBType.getNameFromId(odbTypeId)));
		}
		return o;
	}

	protected NativeObjectInfo bytesToAtomicNative(NativeAttributeHeader header, Bytes bytes, int offset, ReadSize readSize) {
		int odbTypeId = header.getOdbTypeId();
		Object o = bytesToAtomicNativeObject(odbTypeId, bytes, offset, readSize);
		return new AtomicNativeObjectInfo(o, odbTypeId);
	}

	protected NativeObjectInfo bytesToNativeObject(Bytes bytes, int offset, ReadSize readSize, ConversionContext context, int currentDepth) {

		try {
			if (debug) {
				DLogger.debug(" Start Reading native object at offset " + offset);
			}

			NativeAttributeHeader header = bytesToNativeObjectHeader(bytes, offset, readSize);
			int odbTypeId = header.getOdbTypeId();
			offset += NATIVE_HEADER_BLOCK_SIZE;

			if (header.isNull()) {
				return new NullNativeObjectInfo(odbTypeId);
			}

			if (ODBType.isAtomicNative(odbTypeId)) {
				return bytesToAtomicNative(header, bytes, offset, readSize);
			}

			if (ODBType.isCollection(odbTypeId)) {
				return bytesToCollection(header, bytes, offset, readSize, context, currentDepth + 1);
			}
			if (ODBType.isMap(odbTypeId)) {
				return bytesToMap(header, bytes, offset, readSize, context, currentDepth + 1);
			}
			if (ODBType.isArray(odbTypeId)) {
				return bytesToArray(header, bytes, offset, readSize, context, currentDepth + 1);
			}

			if (ODBType.isEnum(odbTypeId)) {
				return bytesToEnum(header, bytes, offset, readSize, context);
			}
			throw new NeoDatisRuntimeException(NeoDatisError.NATIVE_TYPE_NOT_SUPPORTED.addParameter(header.getOdbTypeId()));
		} finally {
			if (debug) {
				DLogger.debug(" End Reading native object at offset " + offset);
			}
		}
	}

	protected EnumNativeObjectInfo bytesToEnum(NativeAttributeHeader header, Bytes bytes, int offset, ReadSize readSize, ConversionContext context) {
		ReadSize lreadSize = new ReadSize();
		ClassOid enumClassOid = byteArrayConverter.byteArrayToClassOid(bytes, offset, lreadSize, "enum class id");
		String enumValue = byteArrayConverter.byteArrayToString(bytes, false, offset + lreadSize.get(), lreadSize, "enum value");
		readSize.add(lreadSize.get());
		return new EnumNativeObjectInfo(enumClassOid, enumValue);
	}

	/**
	 * 
	 * @param header
	 * @param bytes
	 * @param offset
	 * @param context
	 * @return
	 */
	protected CollectionObjectInfo bytesToCollection(NativeAttributeHeader header, Bytes bytes, int offset, ReadSize readSize, ConversionContext context,
			int currentDepth) {
		if (debug) {
			DLogger.debug("  <start reading collection at " + offset + ">");
		}

		ReadSize localReadSize = new ReadSize();
		String realCollectionClassName = byteArrayConverter.byteArrayToString(bytes, false, offset, localReadSize, "real collection class");
		int size = byteArrayConverter.byteArrayToInt(bytes, offset + localReadSize.get(), localReadSize, "collection size");

		ReadSize headerReadSize = new ReadSize();
		int headerPosition = byteArrayConverter.byteArrayToInt(bytes, offset + localReadSize.get(), localReadSize, "header position");

		AttributeIdentification[] attributeIdentifications = bytesToHeader(bytes, size, headerPosition, headerReadSize, false);
		Collection<AbstractObjectInfo> c = new ArrayList<AbstractObjectInfo>();
		// to keep track of non native objects
		Collection<NonNativeObjectInfo> nnois = new ArrayList<NonNativeObjectInfo>();

		for (int i = 0; i < size; i++) {

			AttributeIdentification ai = attributeIdentifications[i];

			if (ai.isNative) {
				NativeObjectInfo noi = bytesToNativeObject(bytes, offset + localReadSize.get(), localReadSize, context, currentDepth);
				c.add(noi);
			} else {
				// Check if Oab (OidandBytes) is in the context
				OidAndBytes oab = context.getOabWithOid(ai.oid);
				if (oab == null) {
					oab = layer4.read(ai.oid, true);
				}
				// ObjectOid oid =
				// byteArrayConverter.byteArrayToObjectOid(bytes,
				// offset+localReadSize.get(), localReadSize,"oid");
				// if(!oid.equals(oab.oid)){
				// throw new
				// ODBRuntimeException(NeoDatisError.LAYER3_OID_DOES_OT_MATCH.addParameter(ai.oid).addParameter(oid).addParameter(offset+localReadSize.get()-OBJECT_OID_SIZE));
				// }
				if (oab != null) {
					NonNativeObjectInfo nnoi = internalFromBytes(oab, true, context, currentDepth + 1);
					c.add(nnoi);
					nnois.add(nnoi);
				}
			}
		}

		CollectionObjectInfo coi = new CollectionObjectInfo(c, nnois);
		coi.setRealCollectionClassName(realCollectionClassName);

		int totalSize = localReadSize.get() + headerReadSize.get();
		if (debug) {
			DLogger.debug("  <end reading collection at " + offset + " size=" + totalSize + ">");
		}
		readSize.add(totalSize);
		return coi;
	}

	/**
	 * 
	 * @param header
	 * @param bytes
	 * @param offset
	 * @param context
	 * @return
	 */
	protected MapObjectInfo bytesToMap(NativeAttributeHeader header, Bytes bytes, int offset, ReadSize readSize, ConversionContext context, int currentDepth) {
		if (debug) {
			DLogger.debug("  <start reading map at " + offset + ">");
		}

		ReadSize localReadSize = new ReadSize();
		String realMapClassName = byteArrayConverter.byteArrayToString(bytes, false, offset, localReadSize, "real collection class");
		int size = byteArrayConverter.byteArrayToInt(bytes, offset + localReadSize.get(), localReadSize, "map size size");

		int headerPosition = byteArrayConverter.byteArrayToInt(bytes, offset + localReadSize.get(), localReadSize, "header position");

		ReadSize headerReadSize = new ReadSize();
		// size*2 : because a map has key/value
		AttributeIdentification[] attributeIdentifications = bytesToHeader(bytes, size * 2, headerPosition, headerReadSize, false);

		Map<AbstractObjectInfo, AbstractObjectInfo> m = new HashMap<AbstractObjectInfo, AbstractObjectInfo>();
		// to keep track of non native objects
		Collection<NonNativeObjectInfo> nnois = new ArrayList<NonNativeObjectInfo>();

		for (int i = 0; i < size; i++) {

			AttributeIdentification aiKey = attributeIdentifications[2 * i];
			AttributeIdentification aiValue = attributeIdentifications[2 * i + 1];
			AbstractObjectInfo keyAoi = null;
			AbstractObjectInfo valueAoi = null;

			// KEY
			if (aiKey.isNative) {
				keyAoi = bytesToNativeObject(bytes, offset + localReadSize.get(), localReadSize, context, currentDepth);
			} else {
				// Check if Oab (OidandBytes) is in the context
				OidAndBytes oab = context.getOabWithOid(aiKey.oid);
				if (oab == null) {
					oab = layer4.read(aiKey.oid, true);
				}
				if (oab != null) {
					keyAoi = internalFromBytes(oab, true, context, currentDepth + 1);
				}
				nnois.add((NonNativeObjectInfo) keyAoi);
			}
			// VALUE
			if (aiValue.isNative) {
				valueAoi = bytesToNativeObject(bytes, offset + localReadSize.get(), localReadSize, context, currentDepth);
			} else {
				// Check if Oab (OidandBytes) is in the context
				OidAndBytes oab = context.getOabWithOid(aiValue.oid);
				if (oab == null) {
					oab = layer4.read(aiValue.oid, true);
				}
				if (oab != null) {
					valueAoi = internalFromBytes(oab, true, context, currentDepth + 1);
				}else{
					valueAoi = new NonNativeNullObjectInfo();
				}
				nnois.add((NonNativeObjectInfo) valueAoi);
			}
			m.put(keyAoi, valueAoi);

		}

		MapObjectInfo moi = new MapObjectInfo(m, realMapClassName);
		moi.setNonNativeObjects(nnois);

		if (debug) {
			DLogger.debug("  <end reading map at " + offset + ">");
		}
		readSize.add(localReadSize.get());

		return moi;
	}

	/**
	 * <pre>
	 *                          Write an array to the database
	 *                          
	 *                          This is done by writing :
	 *                          - the array type : array
	 *                          - the array element type (String if it os a String [])
	 *                          - the position of the non native type, if element are non java / C# native
	 *                          - the number of element s and then the position of all elements.
	 *                          
	 *                          Example : an array with two string element : 'ola' and 'chico'
	 *                          write 22 : array
	 *                          write  20 : array of STRING
	 *                          write 0 : it is a java native object
	 *                          write 2 (as an int) : the number of elements
	 *                          write two times 0 (as long) to reserve the space for the elements positions
	 *                          
	 *                          then write the string 'ola', and keeps its position in the 'positions' array of long 
	 *                          then write the string 'chico' and keeps its position in the 'positions' array of long
	 *                          
	 *                          Then write back all the positions (in this case , 2 positions) after the size of the array
	 *                          
	 *                          
	 *                          Example : an array with two User element : user1 and user2
	 *                          write 22 : array
	 *                          write  23 : array of NON NATIVE Objects
	 *                          write 251 : if 250 is the position of the user class info in database
	 *                          write 2 (as an int) : the number of elements
	 *                          write two times 0 (as long) to reserve the space for the elements positions
	 *                          
	 *                          then write the user user1, and keeps its position in the 'positions' array of long 
	 *                          then write the user user2 and keeps its position in the 'positions' array of long
	 *                          &lt;pre&gt;
	 *                          &#064;param object
	 *                          &#064;param odbType
	 *                          &#064;param position
	 *                          &#064;param writeInTransaction
	 * &#064;
	 * 
	 */
	protected ArrayObjectInfo bytesToArray(NativeAttributeHeader header, Bytes bytes, int offset, ReadSize readSize, ConversionContext context, int currentDepth) {

		if (debug) {
			DLogger.debug("  <start reading array at " + offset + ">");
		}

		ReadSize localReadSize = new ReadSize();
		String realElementTypeClassName = byteArrayConverter.byteArrayToString(bytes, false, offset, localReadSize, "real array type");
		int size = byteArrayConverter.byteArrayToInt(bytes, offset + localReadSize.get(), localReadSize, "array size");

		ReadSize headerReadSize = new ReadSize();
		int headerPosition = byteArrayConverter.byteArrayToInt(bytes, offset + localReadSize.get(), localReadSize, "header position");

		AttributeIdentification[] attributeIdentifications = bytesToHeader(bytes, size, headerPosition, headerReadSize, false);

		AbstractObjectInfo[] array = new AbstractObjectInfo[size];

		for (int i = 0; i < size; i++) {

			AttributeIdentification ai = attributeIdentifications[i];

			if (ai.isNative) {
				NativeObjectInfo noi = bytesToNativeObject(bytes, offset + localReadSize.get(), localReadSize, context, currentDepth);
				array[i] = noi;
			} else {
				if (ai.oid.isNull()) {
					array[i] = new NonNativeNullObjectInfo();
				} else {
					// Check if Oab (OidandBytes) is in the context
					OidAndBytes oab = context.getOabWithOid(ai.oid);
					if (oab == null) {
						oab = layer4.read(ai.oid, true);
					}
					if (oab != null) {
						NonNativeObjectInfo nnoi = internalFromBytes(oab, true, context, currentDepth + 1);
						array[i] = nnoi;
					}
				}
			}
		}

		ArrayObjectInfo aoi = new ArrayObjectInfo(array);
		aoi.setRealArrayComponentClassName(realElementTypeClassName);

		int totalSize = localReadSize.get() + headerReadSize.get();
		if (debug) {
			DLogger.debug("  <end reading array at " + offset + " size=" + totalSize + ">");
		}
		readSize.add(totalSize);
		return aoi;
	}

	protected AttributeIdentification enumNativeObjectToBytes(EnumNativeObjectInfo anoi, Bytes bytes, int offset) {
		return null;
		/*
		 * int odbTypeId = anoi.getOdbTypeId();
		 * 
		 * int objectSize = nativeObjectHeaderToBytes(odbTypeId, anoi.isNull(),
		 * BlockTypes.BLOCK_TYPE_NATIVE_OBJECT, bytes, offset);
		 * 
		 * // Writes the Enum Class id objectSize +=
		 * byteArrayConverter.oidToByteArray(anoi.getEnumClassInfo().getId(),
		 * bytes, offset + objectSize); // Write the Enum String value
		 * objectSize +=
		 * byteArrayConverter.stringToByteArray(anoi.getObject().toString(),
		 * true, bytes, offset + objectSize); return new
		 * AttributeIdentification(offset, objectSize);
		 */

	}

	/**
	 * Read the class info header with the specific oid
	 * 
	 * @param oidAndBytes
	 *            The bytes that represent the class info
	 * @param full
	 *            If false, only class info header is read. If true, all is read
	 *            including attributes
	 * @return The read class info object
	 */
	public ClassInfo classInfoFromBytes(OidAndBytes oidAndBytes, boolean full) {

		ClassOid coid = (ClassOid) oidAndBytes.oid;
		Bytes bytes = oidAndBytes.bytes;

		if (debug) {
			DLogger.debug("reading class info with oid " + coid);
		}
		ReadSize readSize = new ReadSize();

		int checkSum = byteArrayConverter.byteArrayToInt(bytes, readSize.get(), readSize, "ci check sum");
		int blockSize = byteArrayConverter.byteArrayToInt(bytes, readSize.get(), readSize, "class info block size");
		byte blockType = byteArrayConverter.byteArrayToByte(bytes, readSize.get(), readSize, "class info block type");

		if (!BlockTypes.isClassHeader(blockType)) {
			throw new NeoDatisRuntimeException(NeoDatisError.WRONG_TYPE_FOR_BLOCK_TYPE.addParameter("Class Header").addParameter(blockType).addParameter(coid));
		}
		byte classInfoCategory = byteArrayConverter.byteArrayToByte(bytes, readSize.get(), readSize, "class info category");
		ClassInfo classInfo = new ClassInfo();
		classInfo.setClassCategory(classInfoCategory);

		ClassOid oid2 = byteArrayConverter.byteArrayToClassOid(bytes, readSize.get(), readSize, "ci oid");

		if (!coid.equals(oid2)) {
			throw new NeoDatisRuntimeException(NeoDatisError.LAYER3_OID_DOES_OT_MATCH.addParameter(coid).addParameter(oid2).addParameter(readSize.get()));
		}
		classInfo.setOid(coid);
		ClassOid superClassOid = byteArrayConverter.byteArrayToClassOid(bytes, readSize.get(), readSize, "ci super class oid");
		if (!superClassOid.isNull()) {
			classInfo.setSuperClassOid(superClassOid);
		}

		classInfo.setFullClassName(byteArrayConverter.byteArrayToString(bytes, false, readSize.get(), readSize, "ci class name"));

		classInfo.setMaxAttributeId(byteArrayConverter.byteArrayToInt(bytes, readSize.get(), readSize, "ci max attribute id"));

		if (!full) {
			return classInfo;
		}
		int nbAttributes = byteArrayConverter.byteArrayToInt(bytes, readSize.get(), readSize, "ci nb attributes");

		IOdbList<ClassAttributeInfo> attributes = new OdbArrayList<ClassAttributeInfo>((int) nbAttributes);

		for (int i = 0; i < nbAttributes; i++) {
			ClassAttributeInfo cai = classAttributeInfoFromBytes(bytes, readSize);
			if(cai==null){
				String msg = "Class " + classInfo.getFullClassName()+ " does not have attribute wth id "+ i;
			}
			cai.setOwnerClassInfoOid(coid);
			attributes.add(cai);
		}
		classInfo.setAttributes(attributes);

		if (blockSize != readSize.get()) {
			throw new NeoDatisRuntimeException(NeoDatisError.WRONG_BLOCK_SIZE.addParameter(blockSize).addParameter(readSize.get()).addParameter(
					classInfo.getAttributesDefinitionPosition()));
		}

		return classInfo;
	}

	/**
	 * Read an attribute of a class at the current position
	 * 
	 * @return The ClassAttributeInfo description of the class attribute @
	 */
	private ClassAttributeInfo classAttributeInfoFromBytes(Bytes bytes, ReadSize readSize) {
		ClassAttributeInfo cai = new ClassAttributeInfo();
		int attributeId = byteArrayConverter.byteArrayToInt(bytes, readSize.get(), readSize, "attribute id");
		boolean isNative = byteArrayConverter.byteArrayToBoolean(bytes, readSize.get(), readSize, "att is native?");
		if (isNative) {
			int attributeTypeId = byteArrayConverter.byteArrayToInt(bytes, readSize.get(), readSize, "attribute type id");
			ODBType type = ODBType.getFromId(attributeTypeId);
			// if it is an array, read also the subtype
			if (type.isArray()) {
				type = type.copy();
				int subTypeId = byteArrayConverter.byteArrayToInt(bytes, readSize.get(), readSize, "array subtype id");
				;
				ODBType subType = ODBType.getFromId(subTypeId);
				if (subType.isNonNative()) {
					subType = subType.copy();
					ClassOid coid = byteArrayConverter.byteArrayToClassOid(bytes, readSize.get(), readSize, "array subtype class id");
					// TODO do we need to set the class info here
					ClassInfo ci = session.getMetaModel().getClassInfoFromId(coid);
					subType.setName(ci.getFullClassName());
				}
				type.setSubType(subType);
			}
			cai.setAttributeType(type);
			// For enum, we get the class info id of the enum class
			if (type.isEnum()) {
				ClassOid ciId = byteArrayConverter.byteArrayToClassOid(bytes, readSize.get(), readSize, "enum ci id");
				MetaModel metaModel = session.getMetaModel();
				// ClassInfo ci = metaModel.getClassInfoFromId(ciId);
				// cai.setFullClassName(ci.getFullClassName());
				cai.setAttributeClassOid(ciId);
				// For enum, we need to create a new type just to set the real
				// enum class name
				type = type.copy();
				// type.setName(cai.getFullClassname());
				cai.setAttributeType(type);
			} else {
				cai.setClassName(cai.getAttributeType().getName());
			}
		} else {
			// This is a non native, gets the id of the type and gets it from
			// meta-model
			MetaModel metaModel = session.getMetaModel();

			ClassOid attributeCiId = byteArrayConverter.byteArrayToClassOid(bytes, readSize.get(), readSize, "attribute ci id");
			ClassInfo ci = metaModel.getClassInfoFromId(attributeCiId);
			if (attributeCiId.isNull()) {
				return null;
			}

			if (ci == null) {
				String s = "ClassInfo with with id " + attributeCiId + " does not exist in the metamodel";
				DLogger.error(s);
				throw new NeoDatisRuntimeException(s);
			}

			cai.setClassName(ci.getFullClassName());
			cai.setAttributeClassOid(attributeCiId);
			cai.setAttributeType(ODBType.getFromName(cai.getClassName()));
		}
		cai.setName(byteArrayConverter.byteArrayToString(bytes, false, readSize.get(), readSize, "attribute name"));
		cai.setId(attributeId);

		return cai;
	}

}

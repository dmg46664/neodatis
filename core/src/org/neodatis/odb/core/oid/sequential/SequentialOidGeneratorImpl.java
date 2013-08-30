/**
 * 
 */
package org.neodatis.odb.core.oid.sequential;

import java.util.HashMap;
import java.util.Map;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.DatabaseId;
import org.neodatis.odb.ExternalOID;
import org.neodatis.odb.NeoDatis;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.basics.NLong;
import org.neodatis.odb.core.layers.layer3.DataConverter;
import org.neodatis.odb.core.layers.layer3.DataConverterImpl;
import org.neodatis.odb.core.oid.ExternalOIDImpl;
import org.neodatis.odb.core.oid.OIDTypes;
import org.neodatis.odb.core.oid.OidGeneratorAdapter;
import org.neodatis.odb.core.oid.StringOid;

/**
 * @author olivier
 * 
 */
public class SequentialOidGeneratorImpl extends OidGeneratorAdapter {

	protected static final ClassOid NULL_CLASS_OID = new ClassOidImpl(0);
	protected static final ObjectOid NULL_OBJECT_OID = new ObjectOidImpl(NULL_CLASS_OID, 0);

	protected static final String PREFIX_CURRENT_OBJECT_ID = "CURRENT-OID";
	protected static DataConverter converter = new DataConverterImpl(false, null, NeoDatis.getConfig());

	/** When using cache next oids are committed on transaction commit */
	protected Map<OID, NLong> nextOids;
	protected Map<Long, ClassOid> coids;
	protected Map<ClassOid, StringOid> stringClassOid;

	public SequentialOidGeneratorImpl() {
		nextOids = new HashMap<OID, NLong>();
		coids = new HashMap<Long, ClassOid>();
		stringClassOid = new HashMap<ClassOid, StringOid>();
	}

	public synchronized ClassOid createClassOid() {
		return getNextClassOid();
	}

	public synchronized ObjectOid createObjectOid(ClassOid classOid) {
		return getNextObjectOid(classOid);
	}

	public ClassOid buildClassOID(byte[] bb) {
		if (bb.length == 0) {
			return NULL_CLASS_OID;
		}
		long lcoid = converter.byteArrayToLong(bb, 0, "coid");

		ClassOid coid = coids.get(lcoid);
		if (coid != null) {
			return coid;
		}
		coid = new ClassOidImpl(lcoid);
		coids.put(lcoid, coid);
		return coid;

	}

	public ObjectOid buildObjectOID(byte[] bb) {
		if (bb.length == 0) {
			return NULL_OBJECT_OID;
		}

		long loid = converter.byteArrayToLong(bb, 0, "oid");
		long lcoid = converter.byteArrayToLong(bb, 8, "coid");

		ClassOid coid = coids.get(lcoid);
		if (coid == null) {
			coid = new ClassOidImpl(lcoid);
			coids.put(lcoid, coid);
		}

		return new ObjectOidImpl(coid, loid);
	}

	public OID buildStringOid(String s) {
		// TODO Auto-generated method stub
		return null;
	}

	public ExternalOID toExternalOid(ObjectOid oid, DatabaseId databaseId) {
		return new ExternalOIDImpl(oid, databaseId);
	}

	public synchronized ClassOid getNextClassOid() {
		StringOidImpl soid = new StringOidImpl("class-oid");
		long l = readLong(soid, true);
		if (l == Long.MIN_VALUE) {
			l = 1;
			writeLong(soid, l);
		} else {
			l++;
			writeLong(soid, l);
		}
		return new ClassOidImpl(l);
	}

	public synchronized ObjectOid getNextObjectOid(ClassOid classOid) {
		StringOid oid = stringClassOid.get(classOid);
		if (oid == null) {
			oid = new StringOidImpl(new StringBuilder(PREFIX_CURRENT_OBJECT_ID).append(classOid.oidToString()).toString());
			stringClassOid.put(classOid, oid);
		}

		NLong l = null;
		if (useCache) {
			l = nextOids.get(oid);
		}
		if (l == null) {
			long l1 = readLong(oid, true);

			if (l1 != Long.MIN_VALUE) {
				l = new NLong(l1);
			}
			if (l == null) {
				l = new NLong(1);
			} else {
				l.add(1);
			}
		} else {
			l.add(1);
		}
		if (useCache) {
			nextOids.put(oid, l);
		} else {
			writeLong(oid, l.get());
		}

		ObjectOid ooid = new ObjectOidImpl(classOid, l.get());

		return ooid;
	}

	public ClassOid getNullClassOid() {
		return NULL_CLASS_OID;
	}

	public ObjectOid getNullObjectOid() {
		return NULL_OBJECT_OID;
	}

	public void commit() {
		if (useCache) {
			for (OID oid : nextOids.keySet()) {
				writeLong(oid, nextOids.get(oid).get());
			}
		}
	}

	public static ObjectOid oidFromString(String s) {

		String[] tokens = s.split("\\.");
		if (tokens.length != 3) {
			throw new NeoDatisRuntimeException(NeoDatisError.INVALID_OID_REPRESENTATION.addParameter(s));
		}
		int type = Integer.parseInt(tokens[0]);
		if (type != OIDTypes.TYPE_OBJECT_OID) {
			throw new NeoDatisRuntimeException(NeoDatisError.INVALID_OID_REPRESENTATION.addParameter(s));
		}

		long classId = Long.parseLong(tokens[1]);
		long objectId = Long.parseLong(tokens[2]);
		return new ObjectOidImpl(new ClassOidImpl(classId), objectId);
	}

	public ObjectOid objectOidFromString(String s) {
		return ObjectOidImpl.oidFromString(s);
	}

	public ClassOid classOidFromString(String s) {
		return classOidFromString(s);
	}

	public String getSimpleName() {
		return "sequential";
	}
}

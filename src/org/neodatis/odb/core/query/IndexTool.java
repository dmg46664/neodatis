package org.neodatis.odb.core.query;

import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.layers.layer2.meta.AbstractObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.AttributeValuesMap;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfoIndex;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.odb.core.query.criteria.CriteriaQuery;

public class IndexTool {

	public static Comparable buildIndexKey(String indexName, NonNativeObjectInfo oi, int[] fieldIds) {
		/*
		if (fieldIds.length == 1) {
			AbstractObjectInfo aoi = oi.getAttributeValueFromId(fieldIds[0]);
			try {
				// Attributes of index Must be Comparable
				Comparable c = (Comparable) aoi.getObject();
				return new SimpleCompareKey(c);
			} catch (ClassCastException e) {
				throw new ODBRuntimeException(Error.INDEX_KEYS_MUST_IMPLEMENT_COMPARABLE.addParameter(indexName).addParameter(oi.getClassInfo().getAttributeInfoFromId(fieldIds[0]).getName()).addParameter(aoi.getObject().getClass().getName()));
			}
		}*/
		Comparable[] keys = new Comparable[fieldIds.length];
		AbstractObjectInfo aoi = null;
		Comparable o = null;
		for (int i = 0; i < fieldIds.length; i++) {
			// Todo : can we assume that the object is a Comparable
			try {
				aoi = oi.getAttributeValueFromId(fieldIds[i]);
				o = (Comparable) aoi.getObject();
				// JDK1.4 restriction: Boolean is not Comparable in jdk1.4
				if (aoi.getOdbType().isBoolean()) {
					Boolean b = (Boolean) o;
					if (b.booleanValue()) {
						o = new Byte((byte) 1);
					} else {
						o = new Byte((byte) 0);
					}
				}
				// If the index is on NonNativeObjectInfo, then the key is the oid 
				// of the object
				if(aoi.isNonNativeObject()){
					NonNativeObjectInfo nnoi = (NonNativeObjectInfo) aoi;
					o = nnoi.getOid();
				}
				keys[i] = o;
			} catch (Exception e) {
				throw new NeoDatisRuntimeException(NeoDatisError.INDEX_KEYS_MUST_IMPLEMENT_COMPARABLE.addParameter(fieldIds[i]).addParameter(
						oi.getAttributeValueFromId(fieldIds[i]).getClass().getName()));
			}
		}
		if(keys.length==1){
			return keys[0];
		}
		return new ComposedCompareKey(keys);
	}

	public static Comparable buildIndexKey(String indexName, AttributeValuesMap values, String[] fields) {
		if (fields.length == 1) {
			return values.getComparable(fields[0]);
		}
		Comparable[] keys = new Comparable[fields.length];
		Comparable object = null;
		
		for (int i = 0; i < fields.length; i++) {
			// Todo : can we assume that the object is a Comparable
			try {
				object = (Comparable) values.get(fields[i]);

				// JDK1.4 restriction: Boolean is not Comparable in jdk1.4
				if (object instanceof Boolean) {
					Boolean b = (Boolean) object;
					if (b.booleanValue()) {
						object = new Byte((byte) 1);
					} else {
						object = new Byte((byte) 0);
					}
				}
				keys[i] = object;
			} catch (Exception e) {
				throw new NeoDatisRuntimeException(NeoDatisError.INDEX_KEYS_MUST_IMPLEMENT_COMPARABLE.addParameter(indexName).addParameter(fields[i]).addParameter(values.get(fields[i]).getClass().getName()));
			}
		}
		ComposedCompareKey key = new ComposedCompareKey(keys);
		return key;
	}

	/**
	 * Take the fields of the index and take value from the query
	 * 
	 * @param ci
	 *            The class info involved
	 * @param index
	 *            The index
	 * @param query
	 * @return The key of the index
	 */
	public static Comparable computeKey(ClassInfo ci, ClassInfoIndex index, CriteriaQuery query) {
		String [] attributesNames = ci.getAttributeNames(index.getAttributeIds());
		AttributeValuesMap values = query.getCriteria().getValues();
		return buildIndexKey(index.getName(), values,attributesNames);		
	}

}

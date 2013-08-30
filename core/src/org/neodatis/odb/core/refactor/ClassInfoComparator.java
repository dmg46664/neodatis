/**
 * 
 */
package org.neodatis.odb.core.refactor;

import org.neodatis.odb.core.layers.layer2.instance.ClassPool;
import org.neodatis.odb.core.layers.layer2.meta.ClassAttributeInfo;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.layers.layer2.meta.ODBType;
import org.neodatis.tool.wrappers.list.IOdbList;
import org.neodatis.tool.wrappers.list.OdbArrayList;

/**
 * @author olivier
 *
 */
public class ClassInfoComparator {
	public ClassInfoCompareResult compare(ClassInfo ci1, ClassInfo ci2, boolean update, ClassPool classPool) {
		String attributeName = null;
		ClassAttributeInfo cai1 = null;
		ClassAttributeInfo cai2 = null;
		ClassInfoCompareResult result = new ClassInfoCompareResult(ci1.getFullClassName(),ci1,ci2);
		boolean isCompatible = true;
		IOdbList<ClassAttributeInfo> attributesToRemove = new OdbArrayList<ClassAttributeInfo>(10);
		IOdbList<ClassAttributeInfo> attributesToAdd = new OdbArrayList<ClassAttributeInfo>(10);
		int nbAttributes = ci1.getAttributes().size();
		for (int id = 0; id < nbAttributes; id++) {
			// !!!WARNING : ID start with 1 and not 0
			cai1 = ci1.getAttributes().get(id);
			if (cai1 == null) {
				continue;
			}
			attributeName = cai1.getName();
			cai2 = ci2.getAttributeInfoFromId(cai1.getId());
			if (cai2 == null) {
				result.addCompatibleChange("Field '" + attributeName + "' has been removed");
				if (update) {
					// Simply remove the attribute from meta-model
					attributesToRemove.add(cai1);
				}
			} else {
				if (!ODBType.typesAreCompatible(cai1.getAttributeType(), cai2.getAttributeType(),classPool)) {
					result.addIncompatibleChange("Type of Field '" + attributeName + "' has changed : old='" + cai1.getClassName()
							+ "' - new='" + cai2.getClassName() + "'");
					isCompatible = false;
				}
			}
		}
		int nbNewAttributes = ci2.getAttributes().size();
		for (int id = 0; id < nbNewAttributes; id++) {
			// !!!WARNING : ID start with 1 and not 0
			cai2 = ci2.getAttributes().get(id);
			if (cai2 == null) {
				continue;
			}
			attributeName = cai2.getName();
			cai1 = ci1.getAttributeInfoFromId(cai2.getId());
			if (cai1 == null) {
				result.addCompatibleChange("Field '" + attributeName + "' has been added");
				if (update) {
					// Sets the right id of attribute
					cai2.setId(ci1.getMaxAttributeId()+ 1);
					ci1.setMaxAttributeId(ci1.getMaxAttributeId()+1);
					// Then adds the new attribute to the meta-model
					attributesToAdd.add(cai2);
				}

			}
		}
		ci1.getAttributes().removeAll(attributesToRemove);
		ci1.getAttributes().addAll(attributesToAdd);
		ci1.fillAttributesMap();
		return result;
	}

}

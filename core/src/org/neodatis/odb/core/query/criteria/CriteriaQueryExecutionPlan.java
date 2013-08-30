package org.neodatis.odb.core.query.criteria;

import java.util.HashSet;
import java.util.Iterator;

import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfoIndex;
import org.neodatis.odb.core.query.IQueryExecutionPlan;
import org.neodatis.tool.wrappers.OdbTime;

/**
 * A simple Criteria execution plan Check if the query can use index and tries
 * to find the best index to be used
 * 
 * @author osmadja
 * 
 */
public class CriteriaQueryExecutionPlan implements IQueryExecutionPlan {

	protected transient ClassInfo classInfo;

	protected transient CriteriaQueryImpl criteriaQuery;

	protected boolean useIndex;

	protected transient ClassInfoIndex classInfoIndex;
	/** to keep track of the start date time of the plan*/
	protected long start;
	/** to keep track of the end date time of the plan*/
	protected long end;
	/** To keep the execution detail*/
	protected String details;

	public CriteriaQueryExecutionPlan() {
	}
	public CriteriaQueryExecutionPlan(ClassInfo classInfo, CriteriaQueryImpl query) {
		this.classInfo = classInfo;
		this.criteriaQuery = query;
		this.criteriaQuery.setExecutionPlan(this);
		init();
	}

	protected void init() {
		start = 0;
		end = 0;
		// for instance, only manage index for one field query using 'equal'
		if (classInfo.hasIndex() && criteriaQuery.hasCriteria() && canUseIndex(criteriaQuery.getCriteria())) {
			HashSet<String> fields = criteriaQuery.getAllInvolvedFields();
			if (fields.isEmpty()) {
				useIndex = false;
			} else {
				int[] fieldIds = getAllInvolvedFieldIds(fields);
				classInfoIndex = classInfo.getIndexForAttributeIds(fieldIds);
				if (classInfoIndex != null) {
					useIndex = true;
				}
			}
		}
		// Keep the detail
		details = getDetails();
	}
	
	/**Transform a list of field names into a list of field ids
	 * 
	 * @param fields
	 * @return The array of field ids
	 */
	protected int[] getAllInvolvedFieldIds(HashSet<String> fields){
        int nbFields = fields.size();
        int[] fieldIds = new int[nbFields];
        Iterator<String> iterator = fields.iterator();
        int i=0;
		while(iterator.hasNext()){
			fieldIds[i++] = classInfo.getAttributeId(iterator.next());
		}
        return fieldIds;
    }


	private boolean canUseIndex(Criterion criteria) {
		return criteria.canUseIndex();
	}

	public ClassInfoIndex getIndex() {
		return classInfoIndex;
	}

	public boolean useIndex() {
		return useIndex;
	}

	public String getDetails() {
		if(details!=null){
			return details;
		}
		StringBuffer buffer = new StringBuffer();
		if (classInfoIndex == null) {
			buffer.append("No index used, Execution time=").append(getDuration()).append("ms");
			return buffer.toString();
		}
		return buffer.append("Following indexes have been used : ").append(classInfoIndex.getName()).append(", Execution time=").append(getDuration()).append("ms").toString();
	}

	public void end() {
		end = OdbTime.getCurrentTimeInMs();		
	}

	public long getDuration() {
		return (end-start);
	}

	public void start() {
		start = OdbTime.getCurrentTimeInMs();
		
	}

}

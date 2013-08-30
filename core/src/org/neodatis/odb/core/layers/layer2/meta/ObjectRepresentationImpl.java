package org.neodatis.odb.core.layers.layer2.meta;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.ObjectRepresentation;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.layers.layer1.DefaultInstrumentationCallback;
import org.neodatis.odb.core.layers.layer1.ObjectIntrospector;
import org.neodatis.odb.core.server.trigger.ChangedValueNotification;

public class ObjectRepresentationImpl extends Observable implements ObjectRepresentation {
	private final NonNativeObjectInfo nnoi;
	private final ObjectIntrospector introspector; 
	
	private Map<String, Object> changedValues;

	public ObjectRepresentationImpl(NonNativeObjectInfo nnoi, final ObjectIntrospector objectIntrospector){
		this.nnoi = nnoi;
		this.introspector = objectIntrospector;
		this.changedValues = new HashMap<String, Object>();
	}
	//neodatisee
	public Object getValueOf(String attributeName) {
		if(nnoi.isNull()){
			throw new NeoDatisRuntimeException(NeoDatisError.TRIGGER_CALLED_ON_NULL_OBJECT.addParameter(nnoi.getClassInfo().getFullClassName()).addParameter(attributeName));
		}
		AbstractObjectInfo aoi = nnoi.getMetaValueOf(attributeName);
		
		if(aoi==null){
			throw new NeoDatisRuntimeException(NeoDatisError.CLASS_INFO_DO_NOT_HAVE_THE_ATTRIBUTE.addParameter(nnoi.getClassInfo().getFullClassName()).addParameter(attributeName));
		}
		if(aoi.isAtomicNativeObject()){
			return aoi.getObject();
		}
		if(aoi.isNonNativeObject()){
			return new ObjectRepresentationImpl((NonNativeObjectInfo) aoi, introspector);
		}
		throw new NeoDatisRuntimeException(NeoDatisError.NOT_YET_IMPLEMENTED.addParameter("getValueOf for " + aoi.getOdbType().getName()));
		
	}


	public void setValueOf(String attributeName, Object value) {
		AbstractObjectInfo aoi = introspector.getGenericMetaRepresentation(value, new DefaultInstrumentationCallback());
		nnoi.setValueOf(attributeName, aoi);
		
		// to be able to send the change back to the client, notify the session
		changedValues.put(attributeName, value);
		setChanged();
		this.notifyObservers(new ChangedValueNotification(nnoi,nnoi.getOid(),attributeName,value));

	}
	public ObjectOid getObjectOid() {
		return this.nnoi.getOid();
	}
	public String getObjectClassName() {
		return nnoi.getClassInfo().getFullClassName();
	}
	public final NonNativeObjectInfo getNnoi() {
		return nnoi;
	}
	
	/**Check if the object representation has a specific id
	 * 
	 */
	public boolean hasField(String fieldName) {
		ClassAttributeInfo cai = nnoi.getClassInfo().getAttributeInfoFromName(fieldName);
		return cai!=null;
	}
	public boolean isNull() {
		return nnoi==null || nnoi.isNull();
	}
	public boolean isObjectReference() {
		return nnoi !=null && nnoi.isObjectReference();
	}
	

}

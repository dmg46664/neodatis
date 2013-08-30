package org.neodatis.odb.core.basics;


/**
 * A class to wrap a long value to be able to use it in map and with a add method to avoid having to do "l = new Long(l.longValue()+1); 
 * @author olivier
 *
 */
public class NLong {
	private long l;
	
	public NLong(long l){
		this.l = l;
	}
	public NLong(Long ll){
		this.l = ll;
	}
	public long add(int i){
		this.l+=i;
		return l;
	}
	public long get(){
		return l;
	}
	public Long getLong(){
		return new Long(l);
	}
}

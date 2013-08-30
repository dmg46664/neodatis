/**
 * 
 */
package org.neodatis.odb.core.layers.layer3;

/**
 * @author olivier
 *
 */
public class ReadSize {
	private int size;

	public ReadSize() {
		super();
	}
	
	public void add(int addSize){
		size+=addSize;
	}
	
	public int get(){
		return size;
	}
	
	public String toString() {
		return String.valueOf(size);
	}
	
	
}

/**
 * 
 */
package org.neodatis.odb.core.layers.layer3;

import java.util.ArrayList;
import java.util.List;

import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.core.NeoDatisError;

/**
 * @author olivier
 * 
 */
public class BytesImpl2 implements Bytes {
	protected int size;
	protected byte[] bytes;
	protected long offset;

	/** the max position that has been reached */
	protected int maxPosition;
	
	protected final static int MAX_SIZE_FOR_MANUEL_LOOP = 5;

	public BytesImpl2() {
		size = Layer3Converter.DEFAULT_BYTES_SIZE;
		bytes = new byte[size];
		maxPosition = -1;
	}

	/**
	 * @param byteArray
	 */
	public BytesImpl2(byte[] byteArray) {
		size = byteArray.length;
		bytes = new byte[size];
		copy(byteArray, 0, 0, size, false);
	}

	public int append(Bytes bytes) {
		return append(bytes.getByteArray());
	}

	public int append(byte[] b) {
		int l = b.length;
		checkIndex(maxPosition + l);
		System.arraycopy(b, 0, bytes, maxPosition + 1, l);
		maxPosition += l;
		//return maxPosition;
		return b.length;
	}

	public void copy(byte[] bytesToCopy, int from, int to, int length, boolean check) {
		if(check){
			checkIndex(to + length);	
		}
		System.arraycopy(bytesToCopy, from, bytes, to, length);

		int newMaxPosition = to + length - 1;
		if (maxPosition < newMaxPosition) {
			maxPosition = newMaxPosition;
		}
	}

	public byte[] extract(int offset, int length) {
		byte[] b = new byte[length];
		
		if(size<MAX_SIZE_FOR_MANUEL_LOOP){
			for(int i=0;i<size;i++){
				b[i] = bytes[offset+i];
			}
		}else{
			System.arraycopy(bytes, offset, b, 0, length);
		}
		
		return b;
	}

	public byte[] extract(byte[] bytesWhereToWrite, int offsetWhereToWrite, int offsetWhereToRead, int length) {
		System.arraycopy(bytesWhereToWrite, offsetWhereToRead, bytesWhereToWrite, offsetWhereToWrite, length);
		
		 
		return bytesWhereToWrite;
	}

	public byte get(int index) {
		return bytes[index];
	}

	public byte[] get(int index, byte[] dest, int destOffset, int size) {
		// for small arrays,it is faster to use loop to copy
		if(size<MAX_SIZE_FOR_MANUEL_LOOP){
			for(int i=0;i<size;i++){
				dest[destOffset+i] = bytes[index+i];
			}
		}else{
			System.arraycopy(bytes, index, dest, destOffset, size);
		}
		
		
		return dest;
	}

	public byte[] getByteArray() {
		byte[] b = new byte[maxPosition + 1];
		System.arraycopy(bytes, 0, b, 0, maxPosition + 1);
		return b;
	}

	public byte[] getByteArray(int index) {
		return null;
	}

	public int getNbBlocks() {
		return 1;
	}

	public long getOffset() {
		return offset;
	}

	public int getRealSize() {
		return maxPosition + 1;
	}

	public boolean hasOffset() {
		return false;
	}

	public void set(int index, byte b) {
		checkIndex(index);
		bytes[index] = b;
		if (maxPosition < index) {
			maxPosition = index;
		}
	}

	/**
	 * @param index
	 */
	private void checkIndex(int index) {
		if (index >= size) {
			int newSize = size * 3/2+1;
			if(newSize<index){
				newSize = index+1;
			}
			byte[] bytes2 = new byte[newSize];
			System.arraycopy(bytes, 0, bytes2, 0, size);
			
			size = newSize;
			bytes = bytes2;
			
		}
	}

	public void set(int index, byte[] bb) {
		int l = bb.length;
		checkIndex(index + l);
		
		// for small arrays,it is faster to use loop to copy
		if(bb.length<MAX_SIZE_FOR_MANUEL_LOOP){
			for(int i=0;i<bb.length;i++){
				bytes[index+i] = bb[i];
			}
		}else{
			System.arraycopy(bb, 0, bytes, index, l);
		}

		int newMaxPosition = index + l - 1;
		if (maxPosition < newMaxPosition) {
			maxPosition = newMaxPosition;
		}

	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

}

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
public class BytesImpl implements Bytes {
	protected static final int SIZE = Layer3Converter.DEFAULT_BYTES_SIZE;
	/** All the bytes[] of the Bytes instance */
	protected List<byte[]> listOfByteArray;

	/**
	 * The actual bytes we are working on =
	 * listOfByteArray.get(currentBlockNumber)
	 */
	protected byte[] bytes;
	/** The current block number */
	protected int currentBlockNumber;
	/** The min postion of the current block = real position of bytes[0] */
	protected int min;
	/** The max position of the current block = real position of bytes[SIZE-1] */
	protected int max;
	/** the max position that has been reached */
	protected int maxPosition;

	/** this is the difference between the real offset and the user offset. When user ask a Bytes instance
	 * she can specify an offset, but bytes are read by block so the real offset may differ from the request offset
	 * <pre>
	 * Example: By calling file.read(45,100); you expect a bytes instance with bytes starting at 45 and a length of 100, but in this case, the bytes
	 * instance will be returned with a real offset of 0 and a lenght of block size as the data requested are in the first blockk (given that block size is greater then 145).
	 * If block size is 1024, for example, the bytes instance will contain the first 1024 bytes and the deltaOffset will be 45-0 = 45.
	 * 
	 * </pre>
	 */
	protected int deltaOffset;
	protected long offset;

	public BytesImpl() {
		currentBlockNumber = -1;
		initByteArrayForIndex(0);
		maxPosition = -1;
		this.offset = -1;
		this.deltaOffset = 0;
	}
	public BytesImpl(long offset) {
		this();
		this.offset = offset;
		this.deltaOffset = 0;
	}

	public BytesImpl(byte[] array) {
		this();
		copy(array, 0, 0, array.length,true);
	}

	/**
	 * @param byteArray
	 * @param offset2
	 */
	public BytesImpl(byte[] array, long offset) {
		this(array);
		this.offset = offset;
	}

	/**
	 * @param byteArray
	 * @param offset2
	 */
	public BytesImpl(byte[] array, long offset, int delta) {
		this(array);
		this.offset = offset;
		this.deltaOffset = delta;
	}

	/**
	 * Inits the byte array to contain the start index.
	 * 
	 * @return
	 */
	void initByteArrayForIndex(int start) {
		int blockNumber = start / SIZE;

		if (blockNumber == currentBlockNumber) {
			return;
		}
		if (blockNumber == 1 && listOfByteArray == null) {
			listOfByteArray = new ArrayList<byte[]>();
			listOfByteArray.add(bytes);
		}
		this.currentBlockNumber = blockNumber;
		this.min = (blockNumber) * SIZE;
		this.max = min + SIZE - 1;

		if (listOfByteArray != null && listOfByteArray.size() > blockNumber) {
			bytes = listOfByteArray.get(blockNumber);
		} else {
			bytes = new byte[SIZE];
			if (blockNumber > 0 && listOfByteArray != null) {
				listOfByteArray.add(bytes);
			}
		}
		// System.out.println(
		// String.format("Creating block nb %d = [%d,%d]",currentBlockNumber,min,max));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.core.layers.layer3.IBytes#set(int, byte)
	 */
	public void set(int index, byte b) {
		int realIndex = index+deltaOffset;
		
		if (realIndex < min || realIndex > max) {
			initByteArrayForIndex(realIndex);
		}

		// adjust real index in the offset
		int ajustedIndex = realIndex - min;

		bytes[ajustedIndex] = b;

		if (index > maxPosition) {
			maxPosition = realIndex;
		}
	}
	
	public void set(int index, byte[] bb){
		int realIndex = index+deltaOffset;
		int length = bb.length;
		int maxPos = realIndex + length-1;
		
		// check if start is in interval
		if (realIndex < min || realIndex > max) {
			initByteArrayForIndex(realIndex);
		}
		// check if all byte array is in interval
		if (maxPos <= max) {
			// for small arrays the loop is faster than System.arraycopy 
			//System.arraycopy(bb, 0, bytes, realIndex, length);
			for(int i=0;i<bb.length;i++){
				bytes[realIndex-min+i] = bb[i];
			}
			if (maxPos > maxPosition) {
				maxPosition = maxPos;
			}
			return;
		}
		// some data is out , it only happens near a block ending. set one by one
		// We could break the array but this not sure it will be faster
		for(int i=0;i<length;i++){
			set(realIndex+i, bb[i]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.core.layers.layer3.IBytes#copy(byte[], int, int,
	 * int)
	 */
	public void copy(byte[] bytesToCopy, int from, int to, int length, boolean check) {
		int realTo = to+deltaOffset;
		// adjust real index in the offset
		int ajustedTo = realTo - min;

		if (realTo + length > maxPosition + 1) {
			maxPosition = realTo + length - 1;
		}

		int availableSpace = SIZE - ajustedTo;
		boolean toIsIn = availableSpace > 0;
		boolean sizeOk = length <= availableSpace;
		// there is enough space to write
		if (toIsIn && sizeOk) {
			System.arraycopy(bytesToCopy, from, bytes, realTo - min, length);
			return;
		}

		if (toIsIn) {
			// copy what we can in the current array
			int newLength = availableSpace;
			System.arraycopy(bytesToCopy, from, bytes, ajustedTo, newLength);
			// Init next byte array;
			initByteArrayForIndex(max + 1);
			copy(bytesToCopy, from + availableSpace, realTo + availableSpace, length - availableSpace,check);
			return;
		}

		// Init next byte array;
		initByteArrayForIndex(max + 1);
		// here we must pass to and not realTo, as it will be adjusted in the recursive call of copy method
		copy(bytesToCopy, from, to, length,check);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.core.layers.layer3.IBytes#getNbBlocks()
	 */
	public int getNbBlocks() {
		if (listOfByteArray == null) {
			return 1;
		}
		return listOfByteArray.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.core.layers.layer3.IBytes#getRealSize()
	 */
	public int getRealSize() {
		return maxPosition + 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.core.layers.layer3.IBytes#get(int)
	 */
	public byte get(int index) {
		int realIndex = index+deltaOffset;
		if (listOfByteArray == null) {
			if (realIndex > maxPosition) {
				throw new IndexOutOfBoundsException(String.format("Max size of array is %d, trying to get index %d", maxPosition, index));
			}
			return bytes[realIndex];
		}
		int blockNumber = realIndex / SIZE;
		byte[] bb = listOfByteArray.get(blockNumber);
		int adjustedPosition = realIndex - blockNumber * SIZE; 
		return bb[adjustedPosition];
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.core.layers.layer3.IBytes#get(int)
	 */
	public byte[] get(int index, byte[] dest, int destOffset, int size) {
		
		if(size==0){
			return dest;
		}
		int realIndex = index+deltaOffset;
		int maxPos = realIndex+size-1;
		if (listOfByteArray == null) {
			if (realIndex > maxPosition || maxPos > maxPosition) {
				throw new IndexOutOfBoundsException(String.format("Max size of array is %d, trying to get index %d with length %d", maxPosition, index,size));
			}
			try{
				System.arraycopy(bytes, realIndex, dest, destOffset, size);
			}catch (Exception e) {
				throw new RuntimeException(e);
			}
			return dest;
		}
		int blockNumber = realIndex / SIZE;
		byte[] bb = listOfByteArray.get(blockNumber);
		int adjustedPosition = realIndex - blockNumber * SIZE; 
		dest[destOffset] = bb[adjustedPosition];
		return get(index+1, dest, destOffset+1, size-1);		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.core.layers.layer3.IBytes#extract(int, int)
	 */
	public byte[] extract(int offset, int length) {
		int realIndex = deltaOffset + offset;
		if(realIndex+length>getRealSize()){
			throw new NeoDatisRuntimeException(NeoDatisError.LAYER3_BUFFER_OVER_FLOW.addParameter(getRealSize()).addParameter(realIndex+length));
		}
		byte[] rbytes = new byte[length];
		return internalExtract(rbytes, 0, realIndex, length);
	}

	/**
	 * 
	 * @param bytesWhereToWrite
	 *            The byte array where the extracted bytes must be written
	 * @param offsetWhereToWrite
	 *            The start where to start writing in the dest bytesWhereToWrite
	 * @param offsetWhereToRead
	 *            The position from which we must start read
	 * @param length
	 *            The length to extract
	 * @return
	 */
	public byte[] extract(byte[] bytesWhereToWrite, int offsetWhereToWrite, int offsetWhereToRead, int length) {
		int realIndex = deltaOffset + offsetWhereToRead;
		return internalExtract(bytesWhereToWrite, offsetWhereToWrite, realIndex, length);
	}

	protected byte[] internalExtract(byte[] dest, int destOffset, int offset, int length) {
		initByteArrayForIndex(offset);
		byte[] bb = bytes;
		int end = offset + length - 1;
		if (offset >= min && offset <= max && end >= min && end <= max) {
			System.arraycopy(bb, offset - min, dest, destOffset, length);
			return dest;
		}

		if (offset >= min && offset <= max) {
			// the start is here but not the end
			int l = SIZE - (offset - min);
			System.arraycopy(bb, offset - min, dest, destOffset, l);
			return internalExtract(dest, destOffset + l, offset + l, length - l);
		}
		throw new IndexOutOfBoundsException(String.format("Trying to extract byte with offset=%d and length=%d", offset, length));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.core.layers.layer3.Bytes#getByteArray()
	 */
	public byte[] getByteArray() {
		return extract(0, maxPosition + 1);
	}

	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("Offset=").append(offset).append(" - delta Offset = ").append(deltaOffset).append(" - size=").append(getRealSize());
		/*
		for (int i = 0; i < maxPosition + 1; i++) {
			b.append(i).append(":").append(get(i)).append(" ");
		}*/
		return b.toString();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.odb.core.layers.layer3.Bytes#append(org.neodatis.odb.core
	 * .layers.layer3.Bytes)
	 */
	public int append(Bytes bytes) {
		byte[] abytes = bytes.getByteArray();
		return append(bytes);
	}

	/**
	 * @param bytes
	 */
	public int append(byte[] bytes) {
		copy(bytes, 0, maxPosition + 1, bytes.length,true);
		return maxPosition;
	}

	public byte[] getByteArray(int index) {
		if (index == 0 && listOfByteArray == null) {
			return bytes;
		}
		return listOfByteArray.get(index);
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}
	public boolean hasOffset(){
		return offset!=-1;
	}
}

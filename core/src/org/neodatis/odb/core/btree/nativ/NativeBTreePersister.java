/**
 * 
 */
package org.neodatis.odb.core.btree.nativ;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.neodatis.btree.IBTree;
import org.neodatis.btree.IBTreeNode;
import org.neodatis.btree.IBTreePersister;
import org.neodatis.btree.exception.BTreeException;
import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.OID;
import org.neodatis.odb.core.layers.layer3.DataConverter;
import org.neodatis.odb.core.layers.layer3.DataConverterImpl;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.ReadSize;

/**
 * @author olivier
 * 
 */
public class NativeBTreePersister implements IBTreePersister {

	protected RandomAccessFile raf;

	protected String fileName;
	protected long maxFileSize;
	protected int btreeDegree;

	protected int nodeSize;
	protected DataConverter converter;
	protected IBTree btree;
	protected int HEADER_OFFSET = 36; 
	protected Map<Long, IBTreeNode> nodes;
	
	protected Map<Long, IBTreeNode> modifiedNodes;
	//IBTreeSingleValuePerKey modifiedNodes;
	protected boolean commit;
	protected boolean withCacheForRead;
	protected boolean withCacheForWrite;
	protected Map<String,RandomAccessFile> rafs;

	public NativeBTreePersister(String fileName, long maxFileSize, int degree, boolean debug, String characterEncoding, NeoDatisConfig config) {
		super();
		nodes = new HashMap<Long, IBTreeNode>();
		modifiedNodes = new HashMap<Long, IBTreeNode>();
		//modifiedNodes = new InMemoryBTreeSingleValuePerKey("memory",40,new InMemoryPersister());
		//modifiedNodes.setReplaceOnDuplicate(true);
		this.fileName = fileName;
		this.maxFileSize = maxFileSize;
		this.btreeDegree = degree;
		this.converter = new DataConverterImpl(debug,characterEncoding,config);
		computeNodeSize();
		try {
			raf = new RandomAccessFile(fileName, "rw");
		} catch (FileNotFoundException e) {
			throw new BTreeException("Error while opening btree file " + fileName,e);
		}
		withCacheForRead = true;
		withCacheForWrite = true;
	}

	/**
	 * Size of a node on disk = id(1 long),parentId(1 long),size(1
	 * int),size*keys(size*long),size*value (Position =
	 * 2*long+1int),(size+1)*(ids of children)=(size+1)*(long) =
	 * 1long+1long+1int+size*long+size*(2*long+1int)+(size+1)*long
	 */
	private void computeNodeSize() {
		int size = 2 * btreeDegree - 1;
		nodeSize = 8 + 8 + 4 + size * 8 + (size * Position.SIZE) + (size + 1) * 8;
	}

	public void clear() {
		// TODO Auto-generated method stub

	}

	public void close() throws Exception {
		commit = true;
		saveBTree(btree, true);
		saveModifiedNodes();
		raf.close();
	}

	/**
	 * 
	 */
	private void saveModifiedNodes() {
		Iterator<IBTreeNode> mnodes = modifiedNodes.values().iterator();
		//Iterator<IBTreeNode> mnodes = modifiedNodes.iterator(OrderByConstants.ORDER_BY_ASC);
		System.out.println("Saving "+modifiedNodes.size()+ " nodes" );
		int i=0;
		while(mnodes.hasNext()){
			saveNode(mnodes.next());
		}		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.btree.IBTreePersister#deleteNode(org.neodatis.btree.IBTreeNode
	 * )
	 */
	public Object deleteNode(IBTreeNode node) {
		System.out.println("Deleting node with id " + node.getId());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.btree.IBTreePersister#flush()
	 */
	public void flush() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.btree.IBTreePersister#loadBTree(java.lang.Object)
	 */
	public IBTree loadBTree(Object id) {
		if(btree!=null){
			return btree;
		}
		// check cache
		
		
		System.out.println("Loading Btree with id " + id);
		Long nid = (Long) id;
		byte[] bbytes = new byte[HEADER_OFFSET];
		int arraySize = 0;
		try {
			raf.seek(0);
			arraySize = raf.read(bbytes);
		} catch (Exception e) {
			throw new BTreeException("Error while loading btree with id " + id.toString(), e);
		}
		
		if(arraySize!=HEADER_OFFSET){
			throw new BTreeException("Error while reading btree header, different size, expected "+ HEADER_OFFSET+", found "+ arraySize);
		}
		Bytes bytes = BytesFactory.getBytes(bbytes);

		ReadSize readSize = new ReadSize();
		long nid2 = converter.byteArrayToLong(bytes, readSize.get(), readSize, "btree id");
		int degree = converter.byteArrayToInt(bytes, readSize.get(), readSize, "btree degree");
		long size = converter.byteArrayToLong(bytes, readSize.get(), readSize, "btree size");
		long rootId = converter.byteArrayToLong(bytes, readSize.get(), readSize, "btree root id");
		long nextNodeId = converter.byteArrayToLong(bytes, readSize.get(), readSize, "btree next node id");
		NativeBTree nbtree = new NativeBTree(new Long(nid2), degree, this, new Long(nextNodeId));
		nbtree.setSize(size);
		
		IBTreeNode root = loadNodeById(rootId);
		nbtree.setRoot(root);
		return nbtree;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.btree.IBTreePersister#saveBTree(org.neodatis.btree.IBTree)
	 */
	public OID saveBTree(IBTree tree) {
		if(true){
			return null;
		}
		if(tree.getId()==null){
			return null;
		}
		
		NativeBTree nbtree = (NativeBTree) tree;
		Bytes bytes = BytesFactory.getBytes();
		int objectSize = converter.longToByteArray(((Long) tree.getId()).longValue(), bytes, 0, "btree id");
		objectSize += converter.intToByteArray(tree.getDegree(), bytes, objectSize, "btree degree");
		objectSize += converter.longToByteArray(tree.getSize(), bytes, objectSize,"btree size");
		objectSize += converter.longToByteArray(nbtree.getNextNodeId().longValue(), bytes, objectSize, "btree next node id");

		try {
			raf.seek(0);
			raf.write(bytes.getByteArray());
			return null;
		} catch (IOException e) {
			throw new BTreeException("Error while saving btree with id " + tree.getId(), e);
		}
	}

	public OID saveBTree(IBTree tree, boolean force) {
		if(tree.getId()==null){
			return null;
		}
		
		NativeBTree nbtree = (NativeBTree) tree;
		Bytes bytes = BytesFactory.getBytes();
		int objectSize = converter.longToByteArray(((Long) tree.getId()).longValue(), bytes, 0, "btree id");
		objectSize += converter.intToByteArray(tree.getDegree(), bytes, objectSize, "btree degree");
		objectSize += converter.longToByteArray(tree.getSize(), bytes, objectSize,"btree size");
		objectSize += converter.longToByteArray(((Long)tree.getRoot().getId()), bytes, objectSize,"btree root id");
		objectSize += converter.longToByteArray(nbtree.getNextNodeId().longValue(), bytes, objectSize, "btree next node id");

		try {
			raf.seek(0);
			raf.write(bytes.getByteArray());
			return null;
		} catch (IOException e) {
			throw new BTreeException("Error while saving btree with id " + tree.getId(), e);
		}
	}

	public IBTreeNode loadNodeById(Object id) {
		
		if(withCacheForRead){
			// check cache
			IBTreeNode nodeFromCache = nodes.get(id);
			if(nodeFromCache!=null){
				return nodeFromCache;
			}
		
			nodeFromCache = (IBTreeNode) modifiedNodes.get((Comparable)id);
			if(nodeFromCache!=null){
				return nodeFromCache;
			}
		}
		
		Long nid = (Long) id;
		byte[] bbytes = new byte[nodeSize];
		int arraySize = 0;
		try {
			long position = HEADER_OFFSET+ (nid.longValue()-1) * nodeSize; 
			//System.out.println("Reading node with id " + id + " at " + position);
			raf.seek(position);
			arraySize = raf.read(bbytes);
		} catch (Exception e) {
			throw new BTreeException("Error while loading node with id " + id, e);
		}
		Bytes bytes = BytesFactory.getBytes(bbytes);

		ReadSize readSize = new ReadSize();
		long nodeId = converter.byteArrayToLong(bytes, readSize.get(), readSize, "id");
		long parentId = converter.byteArrayToLong(bytes, readSize.get(), readSize, "parent id");
		int size = converter.byteArrayToInt(bytes, readSize.get(), readSize, "size");

		Long[] keys = new Long[size];
		int nbKeys = 0;
		for (int i = 0; i < size; i++) {
			keys[i] = converter.byteArrayToLong(bytes, readSize.get(), readSize, "key");
			if(keys[i].longValue()!=-1){
				nbKeys++;
			}
		}
		Position[] positions = new Position[size];
		for (int i = 0; i < size; i++) {
			long fileId = converter.byteArrayToLong(bytes, readSize.get(), readSize, "file id");
			long blockId = converter.byteArrayToLong(bytes, readSize.get(), readSize, "block id");
			int offset = converter.byteArrayToInt(bytes, readSize.get(), readSize, "offset");
			Position p = new Position(fileId, blockId, offset);
			positions[i] = p;
		}
		Long[] childrens = new Long[size + 1];
		int nbChildren = 0;
		for (int i = 0; i < size + 1; i++) {
			childrens[i] = converter.byteArrayToLong(bytes, readSize.get(), readSize, "child");
			if(childrens[i]!=-1){
				nbChildren++;
			}
		}

		NativeNode node = new NativeNode(btree, nid, parentId, size, keys, positions, childrens,nbKeys,nbChildren);

		if(withCacheForRead){
			// put in cache
			nodes.put(nid, node);
		}
		return node;
	}

	/**
	 * Ids are long Keys are long Values are Positions Ids of values are long
	 * id(long),parentId(long),size(int),size*keys,size*value
	 * (Position),(size+1)*(ids of children)
	 * 
	 * Size of a node on disk = id(1 long),parentId(1 long),size(1
	 * int),size*keys(size*long),size*value (Position =
	 * 2*long+1int),(size+1)*(ids of children)=(size+1)*(long) =
	 * 1long+1long+1int+size*long+size*(2*long+1int)+(size+1)*long
	 */
	public Object saveNode(IBTreeNode node) {
		
		if(!commit && withCacheForWrite){
			/*
			 * if(modifiedNodes.search((Comparable) node.getId())!=null){
			 *
				modifiedNodes.delete((Comparable) node.getId(), node);
			}*/
			modifiedNodes.put((Long) node.getId(), node);
			return null;
		}
		
		Bytes bytes = BytesFactory.getBytes();
		long id = ((Long) node.getId()).longValue();
		long parentId = (node.getParentId() == null ? -1 : ((Long) node.getParentId()).longValue());
		int size = 2 * btreeDegree - 1;
		int objectSize = 0;
		objectSize += converter.longToByteArray(id, bytes, objectSize, "id");
		objectSize += converter.longToByteArray(parentId, bytes, objectSize, "parent id");
		objectSize += converter.intToByteArray(size, bytes, objectSize, "size");
		for (int i = 0; i < size; i++) {
			if (i < node.getNbKeys()) {
				objectSize += converter.longToByteArray(((Long) node.getKeyAt(i)).longValue(), bytes, objectSize, "key");
			} else {
				objectSize += converter.longToByteArray(-1, bytes, objectSize, "key");
			}
		}
		for (int i = 0; i < size; i++) {
			if (i < node.getNbKeys()) {
				Position p = (Position) node.getValueAsObjectAt(i);
				objectSize += converter.longToByteArray(p.fileId, bytes, objectSize, "file id");
				objectSize += converter.longToByteArray(p.blockId, bytes, objectSize, "block id");
				objectSize += converter.intToByteArray(p.offset, bytes, objectSize, "ofsset");
			} else {
				objectSize += converter.longToByteArray(-1, bytes, objectSize, "key");
				objectSize += converter.longToByteArray(-1, bytes, objectSize, "key");
				objectSize += converter.intToByteArray(-1, bytes, objectSize, "key");
			}
		}
		for (int i = 0; i < size + 1; i++) {
			if(i<node.getNbChildren()){
				Long childId = (Long) node.getChildIdAt(i, false);
				if(childId==null){
					System.out.println("null");
				}
				objectSize += converter.longToByteArray(childId.longValue(), bytes, objectSize, "childId");
			}else{
				objectSize += converter.longToByteArray(-1, bytes, objectSize, "childId");
			}
		}
		try {
			long position = HEADER_OFFSET + (id-1) * nodeSize;
			//System.out.println("Writing node with id " + id + " at " + position);
			raf.seek(position);
			raf.write(bytes.getByteArray());
			
			if(withCacheForRead && !commit){
				nodes.put(id, node);
			}
			
			return node.getId();
		} catch (IOException e) {
			throw new BTreeException("Error while saving node with id " + id, e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.btree.IBTreePersister#setBTree(org.neodatis.btree.IBTree)
	 */
	public void setBTree(IBTree tree) {
		this.btree = tree;

	}

}

/**
 * 
 */
package org.neodatis.odb.core.btree.nativ;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.neodatis.btree.IBTree;
import org.neodatis.btree.IBTreeNode;
import org.neodatis.btree.IBTreePersister;
import org.neodatis.btree.exception.BTreeException;
import org.neodatis.fs.NDFS;
import org.neodatis.fs.NDFSFactory;
import org.neodatis.fs.NdfsConfig;
import org.neodatis.fs.NdfsFile;
import org.neodatis.fs.transaction.NdfsTransaction;
import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.OID;
import org.neodatis.odb.core.layers.layer3.DataConverter;
import org.neodatis.odb.core.layers.layer3.DataConverterImpl;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.BytesHelper;
import org.neodatis.odb.core.layers.layer3.ReadSize;

/**
 * @author olivier
 * 
 */
public class NativeBTreePersisterWithNeoDatisFS implements IBTreePersister {

	protected String fileName;
	protected long maxFileSize;
	protected int btreeDegree;

	protected int nodeSize;
	protected DataConverter converter;
	protected IBTree btree;
	protected int HEADER_OFFSET = 40;
	protected Map<Long, IBTreeNode> nodes;

	protected Map<Long, IBTreeNode> modifiedNodes;
	// IBTreeSingleValuePerKey modifiedNodes;
	protected boolean commit;
	protected boolean withCacheForRead;
	protected boolean withCacheForWrite;
	protected Map<Long, NdfsFile> files;
	protected NDFS ndfs;
	protected NdfsTransaction transaction;

	public static long tbuildbytes = 0;
	public static long tgetfile = 0;
	public static long twrite = 0;
	protected boolean debug;
	protected String characterEncoding;
	protected NeoDatisConfig config; 
	public NativeBTreePersisterWithNeoDatisFS(String fileName, long maxFileSize, int degree,boolean debug, String characterEncoding, NeoDatisConfig config) {
		super();
		this.config = config;
		System.out.println("SplitPersisterWithNeoDatisFS");
		nodes = new HashMap<Long, IBTreeNode>();
		modifiedNodes = new TreeMap<Long, IBTreeNode>();
		// modifiedNodes = new InMemoryBTreeSingleValuePerKey("memory",40,new
		// InMemoryPersister());
		// modifiedNodes.setReplaceOnDuplicate(true);
		this.fileName = fileName;
		this.maxFileSize = maxFileSize;
		this.btreeDegree = degree;
		this.converter = new DataConverterImpl(debug,characterEncoding,config);
		computeNodeSize();
		initNdfs();
		this.debug = debug;
		this.characterEncoding = characterEncoding;
		withCacheForRead = true;
		withCacheForWrite = true;
	}

	/**
	 * @throws FileNotFoundException
	 * 
	 */
	private void initNdfs() {
		try {
			NdfsConfig config = NDFSFactory.getConfig(fileName, 4 * 1024);
			ndfs = NDFSFactory.open(config);
			transaction = ndfs.startTransaction();
			files = new HashMap<Long, NdfsFile>();
			String fileNameWithId = fileName + "0";
			NdfsFile file = transaction.getFile(null, fileNameWithId);
			files.put(new Long(0), file);
		} catch (Exception e) {
			throw new BTreeException("Error while initializing RAFs", e);
		}
	}

	protected SplitRafResult2 getFileForPosition(long position) {
		long id = position / maxFileSize;
		long adjustedPosition = position - (id * maxFileSize);
		// System.out.println(String.format("file id=%d     position=%d      adjustedPosition=%d",
		// id, position, adjustedPosition));
		try {

			Long lid = new Long(id);
			NdfsFile file = files.get(lid);
			if (file == null) {
				String fileId = fileName + id;
				file = transaction.getFile(null, fileId);
				files.put(lid, file);
				System.out.println("Creating file " + fileId);
			}
			return new SplitRafResult2(adjustedPosition, file);
		} catch (Exception e) {
			throw new BTreeException("Error while getting raf for id " + id, e);
		}

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
		long t0 = System.currentTimeMillis();
		saveBTree(btree, true);
		long t1 = System.currentTimeMillis();
		System.out.println("BTree saved " + (t1 - t0) + "ms");
		saveModifiedNodes();
		long t2 = System.currentTimeMillis();
		System.out.println("Modified Nodes saved " + (t2 - t1) + "ms");
		transaction.commit();
		long t3 = System.currentTimeMillis();
		System.out.println("Transaction committed " + (t3 - t2) + "ms");
		ndfs.close();
		long t4 = System.currentTimeMillis();
		System.out.println("NDFS closed " + (t4 - t3) + "ms");
	}

	private void saveModifiedNodes() throws IOException {
		Iterator<IBTreeNode> mnodes = modifiedNodes.values().iterator();
		// Iterator<IBTreeNode> mnodes =
		// modifiedNodes.iterator(OrderByConstants.ORDER_BY_ASC);
		while (mnodes.hasNext()) {
			IBTreeNode node = mnodes.next();
			saveNode(node);
		}
		// System.out.println("Times : buildBytes =" + tbuildbytes +
		// "      | tgetfile="+tgetfile+"      |  twrite="+twrite);
	}

	/**
	 * @throws IOException
	 * 
	 */
	private void saveModifiedNodes2() throws IOException {
		Iterator<IBTreeNode> mnodes = modifiedNodes.values().iterator();
		// Iterator<IBTreeNode> mnodes =
		// modifiedNodes.iterator(OrderByConstants.ORDER_BY_ASC);
		System.out.println("Saving " + modifiedNodes.size() + " nodes");
		int i = 0;
		long id1 = 0;
		long id2 = 0;
		int nbConcat = -1;
		Bytes mainBytes = null;
		long initPosition = HEADER_OFFSET;
		boolean firstTime = true;
		while (mnodes.hasNext()) {
			IBTreeNode node = mnodes.next();
			Bytes bytes = buildBytesForNode(node);

			id2 = ((Long) node.getId()).longValue();

			boolean canConcat = id1 != 0 && id1 == id2 - 1;
			if (!firstTime && canConcat && initPosition != -1) {
				nbConcat++;
				mainBytes.append(bytes);
			} else {
				initPosition = HEADER_OFFSET + (id2 - 1) * nodeSize;
				mainBytes = bytes;
				nbConcat = 0;
			}
			if (!firstTime && !canConcat || nbConcat > 40) {
				SplitRafResult2 result = getFileForPosition(initPosition);
				NdfsFile file = result.file;
				mainBytes.setOffset(result.adjustedPosition);
				file.write(mainBytes);
				id1 = -1;
				mainBytes = null;
				initPosition = -1;
			}
			firstTime = false;
			id1 = id2;

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
		if (btree != null) {
			return btree;
		}
		// check cache

		Long nid = (Long) id;
		Bytes bytes = null;
		int arraySize = 0;
		try {
			NdfsFile file = getFileForPosition(0).file;
			bytes = file.read(0, HEADER_OFFSET);
		} catch (Exception e) {
			throw new BTreeException("Error while loading btree with id " + id.toString(), e);
		}

		BytesHelper helper = new BytesHelper(bytes, debug,characterEncoding,config);
		ReadSize readSize = new ReadSize();
		long nid2 = helper.readLong(readSize.get(), readSize, "btree id");
		int degree = helper.readInt(readSize.get(), readSize, "btree degree");
		long size = helper.readLong(readSize.get(), readSize, "btree size");
		int height = helper.readInt(readSize.get(), readSize, "btree height");
		long rootId = helper.readLong(readSize.get(), readSize, "btree root id");
		long nextNodeId = helper.readLong(readSize.get(), readSize, "btree next node id");
		NativeBTree nbtree = new NativeBTree(new Long(nid2), degree, this, new Long(nextNodeId));
		nbtree.setSize(size);
		nbtree.setHeight(2);
		// remove the root node that has been created arbitrarily (with id 1).
		modifiedNodes.remove(nbtree.getRoot().getId());

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
		if (true) {
			return null;
		}
		if (tree.getId() == null) {
			return null;
		}

		NativeBTree nbtree = (NativeBTree) tree;
		Bytes bytes = BytesFactory.getBytes();
		int objectSize = converter.longToByteArray(((Long) tree.getId()).longValue(), bytes, 0, "btree id");
		objectSize += converter.intToByteArray(tree.getDegree(), bytes, objectSize, "btree degree");
		objectSize += converter.longToByteArray(tree.getSize(), bytes, objectSize, "btree size");
		objectSize += converter.longToByteArray(nbtree.getNextNodeId().longValue(), bytes, objectSize, "btree next node id");

		NdfsFile file = getFileForPosition(0).file;
		bytes.setOffset(0);
		file.write(bytes);
		return null;
	}

	public OID saveBTree(IBTree tree, boolean force) {
		if (tree.getId() == null) {
			return null;
		}

		NativeBTree nbtree = (NativeBTree) tree;
		Bytes bytes = BytesFactory.getBytes();
		int objectSize = converter.longToByteArray(((Long) tree.getId()).longValue(), bytes, 0, "btree id");
		objectSize += converter.intToByteArray(tree.getDegree(), bytes, objectSize, "btree degree");
		objectSize += converter.longToByteArray(tree.getSize(), bytes, objectSize, "btree size");
		objectSize += converter.intToByteArray(tree.getHeight(), bytes, objectSize, "btree height");
		objectSize += converter.longToByteArray(((Long) tree.getRoot().getId()), bytes, objectSize, "btree root id");
		objectSize += converter.longToByteArray(nbtree.getNextNodeId().longValue(), bytes, objectSize, "btree next node id");

		SplitRafResult2 result = getFileForPosition(0);
		NdfsFile file = result.file;
		bytes.setOffset(result.adjustedPosition);
		file.write(bytes);
		return null;
	}

	public IBTreeNode loadNodeById(Object id) {

		if (withCacheForRead) {
			// check cache
			IBTreeNode nodeFromCache = nodes.get(id);
			if (nodeFromCache != null) {
				return nodeFromCache;
			}

			nodeFromCache = (IBTreeNode) modifiedNodes.get((Comparable) id);
			if (nodeFromCache != null) {
				return nodeFromCache;
			}
		}

		Long nid = (Long) id;
		Bytes bytes = null;
		SplitRafResult2 result = null;
		;
		try {
			long position = HEADER_OFFSET + (nid.longValue() - 1) * nodeSize;
			result = getFileForPosition(position);
			// System.out.println("Reading node with id " + id + " at " +
			// position);
			NdfsFile file = result.file;

			bytes = file.read(result.adjustedPosition, nodeSize);
		} catch (Exception e) {
			throw new BTreeException("Error while loading node with id " + id, e);
		}

		ReadSize readSize = new ReadSize();
		int d = (int) (result.adjustedPosition - bytes.getOffset());
		// System.out.println("calculated offset= " + d + "      |  bytes=" +
		// bytes + "     | adjusted position=" + result.adjustedPosition);
		// readSize.add((int)(result.adjustedPosition-bytes.getOffset()));
		long nodeId = converter.byteArrayToLong(bytes, readSize.get(), readSize, "id");
		long parentId = converter.byteArrayToLong(bytes, readSize.get(), readSize, "parent id");
		int size = converter.byteArrayToInt(bytes, readSize.get(), readSize, "size");

		Long[] keys = new Long[size];
		int nbKeys = 0;
		for (int i = 0; i < size; i++) {
			keys[i] = converter.byteArrayToLong(bytes, readSize.get(), readSize, "key");
			if (keys[i].longValue() != -1) {
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
			if (childrens[i] != -1) {
				nbChildren++;
			}
		}

		NativeNode node = new NativeNode(btree, nid, parentId, size, keys, positions, childrens, nbKeys, nbChildren);

		if (withCacheForRead) {
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

		if (!commit && withCacheForWrite) {
			/*
			 * if(modifiedNodes.search((Comparable) node.getId())!=null){
			 * 
			 * modifiedNodes.delete((Comparable) node.getId(), node); }
			 */
			modifiedNodes.put((Long) node.getId(), node);
			return null;
		}
		long t0 = System.currentTimeMillis();
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
			if (i < node.getNbChildren()) {
				Long childId = (Long) node.getChildIdAt(i, false);
				if (childId == null) {
					System.out.println("null");
				}
				objectSize += converter.longToByteArray(childId.longValue(), bytes, objectSize, "childId");
			} else {
				objectSize += converter.longToByteArray(-1, bytes, objectSize, "childId");
			}
		}
		long position = HEADER_OFFSET + (id - 1) * nodeSize;
		long t1 = System.currentTimeMillis();
		// System.out.println("Writing node with id " + id + " at " +
		// position);
		SplitRafResult2 result = getFileForPosition(position);
		NdfsFile file = result.file;
		bytes.setOffset(result.adjustedPosition);
		long t2 = System.currentTimeMillis();
		file.write(bytes);
		long t3 = System.currentTimeMillis();

		twrite += (t3 - t2);
		tbuildbytes += (t1 - t0);
		tgetfile += (t2 - t1);

		if (withCacheForRead && !commit) {
			nodes.put(id, node);
		}

		return node.getId();

	}

	public Bytes buildBytesForNode(IBTreeNode node) {

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
			if (i < node.getNbChildren()) {
				Long childId = (Long) node.getChildIdAt(i, false);
				if (childId == null) {
					System.out.println("null");
				}
				objectSize += converter.longToByteArray(childId.longValue(), bytes, objectSize, "childId");
			} else {
				objectSize += converter.longToByteArray(-1, bytes, objectSize, "childId");
			}
		}
		return bytes;

	}

	public void setBTree(IBTree tree) {
		this.btree = tree;

	}

}

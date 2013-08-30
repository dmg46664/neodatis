package org.neodatis.odb.plugin.idf.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import org.neodatis.odb.OID;
import org.neodatis.odb.core.layers.layer4.ClassOidIterator;
import org.neodatis.odb.core.layers.layer4.ObjectOidIterator;
import org.neodatis.odb.core.oid.sequential.ClassOidImpl;
import org.neodatis.odb.core.oid.sequential.ObjectOidImpl;
import org.neodatis.odb.plugin.idf.Indexer;
import org.neodatis.odb.plugin.idf.ObjectLocation;

public class LuceneIndex implements Indexer {

	protected String id;
	protected String baseDir;
	protected String keyName;
	protected IndexWriter indexWriter;
	protected IndexReader indexReader;
	protected Directory directory;
	protected Analyzer analyzer;

	public LuceneIndex(String id, String baseDir, String keyName) throws CorruptIndexException, LockObtainFailedException, IOException {
		this.id = id;
		this.baseDir = baseDir;
		this.keyName = keyName;
		init();
	}

	private void init() throws CorruptIndexException, LockObtainFailedException, IOException {
		this.directory = FSDirectory.open(getFullIndexPath());
		analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
		indexWriter = new IndexWriter(directory, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
		indexReader = IndexReader.open(directory, false);

	}

	private File getFullIndexPath() {
		return new File(baseDir + "/" + id);
	}

	/* (non-Javadoc)
	 * @see org.neodatis.odb.plugin.lucene.Indexer#put(org.neodatis.odb.OID, org.neodatis.odb.plugin.nativ.ObjectLocation)
	 */
	public void put(OID oid, ObjectLocation objectLocation) throws CorruptIndexException, IOException {
		Document doc = new Document();
		doc.add(new Field(keyName, oid.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		byte[] bytes = objectLocation.toBytes();
		doc.add(new Field("file-id", bytes, Field.Store.YES));
		indexWriter.addDocument(doc);

		// indexWriter.commit();
		// refreshReader();
	}

	public void commitWriter() throws CorruptIndexException, IOException {
		indexWriter.commit();
	}

	private void refreshReader() throws CorruptIndexException, IOException {
		indexReader.close();
		indexReader = IndexReader.open(directory, false);
	}

	/* (non-Javadoc)
	 * @see org.neodatis.odb.plugin.lucene.Indexer#get(org.neodatis.odb.OID)
	 */
	public ObjectLocation get(OID oid) throws ParseException, IOException {
		long t0 = System.currentTimeMillis();
		try {
			IndexSearcher searcher = new IndexSearcher(indexReader);
			QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, keyName, analyzer);
			Query query = parser.parse(oid.toString());
			int hitsPerPage = 5;
			TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
			searcher.search(query, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			System.out.println("Found " + hits.length + " hits.");
			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				byte[] bytes = d.getBinaryValue("file-id");
				ObjectLocation ol = ObjectLocation.fromBytes(bytes);
				return ol;
			}

			return null;
		} finally {
			long t1 = System.currentTimeMillis();
			System.out.println((t1 - t0) + "ms");
		}

	}

	/* (non-Javadoc)
	 * @see org.neodatis.odb.plugin.lucene.Indexer#close()
	 */
	public void close() throws IOException {
		indexWriter.close();
		indexReader.close();
		directory.close();
	}

	public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
		LuceneIndex li = new LuceneIndex("1" + System.currentTimeMillis(), "unit-test-data", "oid");
		OID oid = new ObjectOidImpl(new ClassOidImpl(1), 1);

		int size = 1000000;
		for (int i = 0; i < size; i++) {
			oid = new ObjectOidImpl(new ClassOidImpl(1), i + 1);
			li.put(oid, new ObjectLocation(1, i + 1,i));
			if (i % 10 == 0) {
				System.out.println(i);
			}
		}
		li.commitWriter();
		li.refreshReader();
		// li.close();
		// li = new LuceneIndex("1", "unit-test-data", "oid" );
		ObjectLocation ol = li.get(new ObjectOidImpl(new ClassOidImpl(1), 9999));

		System.out.println(ol);
		li.close();

	}

	public void init(String id, String baseDirectory) {
		// TODO Auto-generated method stub
		
	}

	public void commit() {
		// TODO Auto-generated method stub
		
	}

	public void rollback() {
		// TODO Auto-generated method stub
		
	}

	public ObjectLocation delete(OID oid) {
		// TODO Auto-generated method stub
		return null;
		
	}

	public boolean existKey(OID oid) {
		// TODO Auto-generated method stub
		return false;
	}

	public Iterator<OID> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public ClassOidIterator getClassOidIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectOidIterator getObjectOidIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDebug(boolean yes) {
		// TODO Auto-generated method stub
		
	}
}

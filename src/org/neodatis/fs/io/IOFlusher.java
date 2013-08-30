/**
 * 
 */
package org.neodatis.fs.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.neodatis.fs.NDFS;
import org.neodatis.fs.NdfsConfig;
import org.neodatis.fs.NdfsFile;
import org.neodatis.fs.transaction.NdfsTransaction;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.tool.DLogger;

/**
 * 
 * The IO flusher takes committed transaction files and apply them the main
 * files
 * 
 * @author olivier
 * 
 */
public class IOFlusher implements Runnable {
	protected NDFS ndfs;
	protected Collection<NdfsTransaction> transactionsToFlush;
	protected boolean isFlushing;
	protected boolean debug;
	protected boolean forceClose;
	protected boolean isClosed;

	public IOFlusher(NDFS ndfs) {
		this.debug = ndfs.getConfig().debug();
		this.ndfs = ndfs;
		this.transactionsToFlush = Collections.synchronizedCollection(new ArrayList<NdfsTransaction>());
		this.isFlushing = false;
		this.forceClose = false;
		this.isClosed = false;
	}

	public synchronized void markAsFlushable(NdfsTransaction transaction) {
		transactionsToFlush.add(transaction);
	}

	public void run() {
		if (debug) {
			DLogger.debug(String.format("IOFlusher started for file system %s", ndfs.getName()));
		}

		while (!ndfs.isClosed() && !forceClose) {
			internalFlush();
			try {
				Thread.sleep(ndfs.getConfig().getTimeBetweenEachFlush());
			} catch (InterruptedException e) {
			}
		}
		if (debug) {
			DLogger.debug("IOFlusher: NDFS is closed, killing IOFlusher");
		}
		isClosed = true;
	}

	/**
	 * Check if there are transactions to flush
	 * 
	 */
	protected void internalFlush() {
		try {
			if(isClosed){
				return;
			}
			if (ndfs.canFlushCommittedTransaction()) {
				Collection<NdfsTransaction> transactions = new ArrayList<NdfsTransaction>();
				synchronized (transactionsToFlush) {
					if (!transactionsToFlush.isEmpty()) {
						// copy all transactions to flush to another list
						transactions.addAll(transactionsToFlush);
						// then clear main list
						transactionsToFlush.clear();

						isFlushing = true;
						Iterator<NdfsTransaction> iterator = transactions.iterator();
						while (iterator.hasNext()) {
							internalFlush(iterator.next());
						}
					}
				}
			}
		} finally {
			isFlushing = false;
		}
	}

	/**
	 * Flush one transaction
	 * 
	 * @param next
	 */
	private void internalFlush(NdfsTransaction transaction) {
		if (debug) {
			DLogger.debug(String.format("IOFlusher:Flushing transaction %d", transaction.getId()));
		}

		// Check if transaction data is still available in memory
		Map<NdfsFile, Collection<Bytes>> bytesPerFile = transaction.getTma().getBlocksByFile();

		if (bytesPerFile.isEmpty()) {
			DLogger.debug(String.format("\tIOFlusher:transaction %d is empty", transaction.getId()));
		}

		Iterator<NdfsFile> keys = bytesPerFile.keySet().iterator();

		while (keys.hasNext()) {
			NdfsFile file = keys.next();
			Collection<Bytes> bytes = bytesPerFile.get(file);

			if (debug) {
				DLogger.debug(String.format("\tIOFlusher:Flushing blocks of file file id=%d (%s), nb blocks = %d", file.getId(), file.getName(), bytes.size()));
			}
			SyncBlockWriter writer = ndfs.getSyncWriter(file);
			Iterator<Bytes> iterator = bytes.iterator();
			int maxBlockSize = 1024 * 1024;
			if (iterator.hasNext()) {
				Bytes b1 = iterator.next();
				Bytes b2 = null;
				while (iterator.hasNext()) {
					b2 = iterator.next();
					if (b2.getOffset() == b1.getOffset() + b1.getRealSize() && b1.getRealSize() < maxBlockSize) {
						b1.append(b2);
					} else {
						writer.writeTo(b1);
						b1 = b2;
					}
				}
				writer.writeTo(b1);
			}
		}
		transaction.markTransactionFileAsApplied();
		ndfs.release(transaction);
	}

	/**
	 * @param b
	 * 
	 */
	public void flushNow(boolean stopFlusherAfter) {
		while (isFlushing) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		internalFlush();

		if (stopFlusherAfter) {
			close();
		}
	}

	public void close() {
		forceClose = true;
	}

	public void waitForClose() {
		while (!isClosed) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
	}

}

/**
 * 
 */
package org.neodatis.odb;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.neodatis.odb.core.CoreProvider;
import org.neodatis.odb.core.config.Server;
import org.neodatis.odb.core.query.IQueryExecutorCallback;


/**The interface to define all the config parameters of NeoDatis.
 * @author olivier
 *
 */
public interface NeoDatisConfig {

	public String getBaseName();
	public NeoDatisConfig setBaseName(String baseName);
	public boolean isSessionAutoCommit();

	public NeoDatisConfig setSessionAutoCommit(boolean sessionAutoCommit);

	public Class getStorageEngineClass();

	public NeoDatisConfig setStorageEngineClass(Class storageEngineClass);

	/**
	 * @return
	 */
	public boolean reconnectObjectsToSession();

	public NeoDatisConfig setReconnectObjectsToSession(boolean reconnectObjectsToSession);

	public NeoDatisConfig addLogId(String logId);

	public NeoDatisConfig removeLogId(String logId);

	public boolean isDebugEnabled(String logId);

	public NeoDatisConfig setDebugEnabled(int level, boolean debug);

	public boolean isInfoEnabled();

	public boolean isInfoEnabled(String logId);

	public NeoDatisConfig setInfoEnabled(boolean infoEnabled);

	public long getMaxNumberOfObjectInCache();

	public NeoDatisConfig setMaxNumberOfObjectInCache(long maxNumberOfObjectInCache);

	public int getNumberOfRetryToOpenFile();

	public NeoDatisConfig setNumberOfRetryToOpenFile(int numberOfRetryToOpenFile);

	public long getRetryTimeout();
	public NeoDatisConfig setRetryTimeout(long retryTimeout);
	
	/** the socket timeout for client server mode
	 * 
	 * @return
	 */
	public int getSocketTimeoutForClientSever();

	/** To set the socket timeout for client server mode
	 * 
	 * @param t
	 * @return
	 */
	public NeoDatisConfig setClientServerSocketTimeout(int t);
	

	public boolean retryIfFileIsLocked();

	public NeoDatisConfig setRetryIfFileIsLocked(boolean retryIfFileIsLocked);

	public boolean isMultiThread();

	public NeoDatisConfig useMultiThread(boolean yes);

	public NeoDatisConfig useMultiThread(boolean yes, int numberOfThreads);

	public boolean throwExceptionWhenInconsistencyFound();

	public NeoDatisConfig setThrowExceptionWhenInconsistencyFound(boolean throwExceptionWhenInconsistencyFound);

	public boolean automaticallyIncreaseCacheSize();

	public NeoDatisConfig setAutomaticallyIncreaseCacheSize(boolean automaticallyIncreaseCache);

	/**
	 * @return Returns the debugLevel.
	 */
	public int getDebugLevel();

	/**
	 * @param debugLevel
	 *            The debugLevel to set.
	 */
	public NeoDatisConfig setDebugLevel(int debugLevel);

	public boolean checkMetaModelCompatibility();

	public NeoDatisConfig setCheckMetaModelCompatibility(boolean checkModelCompatibility);

	public boolean automaticCloseFileOnExit();

	public NeoDatisConfig setAutomaticCloseFileOnExit(boolean automaticFileClose);

	public boolean isLogAll();

	public NeoDatisConfig setLogAll(boolean logAll);

	public boolean logServerConnections();

	public NeoDatisConfig setLogServerConnections(boolean logServerConnections);

	public int getDefaultIndexBTreeDegree();

	public NeoDatisConfig setDefaultIndexBTreeDegree(int defaultIndexBTreeSize);
	
	public int getDefaultCollectionBTreeDegree();

	public NeoDatisConfig setDefaultCollectionBTreeDegree(int defaultIndexBTreeSize);

	public boolean useLazyCache();

	public NeoDatisConfig setUseLazyCache(boolean useLazyCache);

	/**
	 * @return the queryExecutorCallback
	 */
	public IQueryExecutorCallback getQueryExecutorCallback();

	/**
	 * @param queryExecutorCallback
	 *            the queryExecutorCallback to set
	 */
	public NeoDatisConfig setQueryExecutorCallback(IQueryExecutorCallback queryExecutorCallback);

	/**
	 * @return the useCache
	 */
	public boolean useCache();

	/**
	 * @param useCache
	 *            the useCache to set
	 */
	public NeoDatisConfig setUseCache(boolean useCache);

	public boolean monitoringMemory();

	public NeoDatisConfig setMonitorMemory(boolean yes);

	public boolean displayWarnings();

	public NeoDatisConfig setDisplayWarnings(boolean yesOrNo);

	public int getScaleForAverageDivision();

	public NeoDatisConfig setScaleForAverageDivision(int scaleForAverageDivision);

	public int getRoundTypeForAverageDivision();

	public NeoDatisConfig setRoundTypeForAverageDivision(int roundTypeForAverageDivision);

	public boolean enableEmptyConstructorCreation();

	public NeoDatisConfig setEnableEmptyConstructorCreation(boolean enableEmptyConstructorCreation);

	public String getEncryptionPassword();

	public CoreProvider getCoreProvider();

	public NeoDatisConfig setCoreProvider(CoreProvider coreProvider);

	public String getDatabaseCharacterEncoding();

	public NeoDatisConfig setDatabaseCharacterEncoding(String dbCharacterEncoding) throws UnsupportedEncodingException;

	public NeoDatisConfig setLatinDatabaseCharacterEncoding() throws UnsupportedEncodingException;

	public boolean hasEncoding();

	public ClassLoader getClassLoader();

	public NeoDatisConfig setClassLoader(ClassLoader cl);

	public boolean checkRuntimeVersion();

	public NeoDatisConfig setCheckRuntimeVersion(boolean checkJavaRuntimeVersion);

	/**
	 * @return
	 */
	public Class getMessageStreamerClass();

	public NeoDatisConfig setMessageStreamerClass(Class messageStreamerClass);

	public boolean logServerStartupAndShutdown();

	public NeoDatisConfig setLogServerStartupAndShutdown(boolean logServerStartup);

	public boolean useIndex();

	public NeoDatisConfig setUseIndex(boolean useIndex);

	public boolean isDebugEnabled();

	public NeoDatisConfig setDebugEnabled(boolean debugEnabled);

	public boolean shareSameVmConnectionMultiThread();

	public NeoDatisConfig setShareSameVmConnectionMultiThread(boolean shareSameVmConnectionMultiThread);

	/**
	 * 
	 */
	public NeoDatisConfig lockObjectsOnSelect(boolean yesNo);

	public boolean lockObjectsOnSelect();

	/**
	 * @return
	 */
	public boolean debugLayers();

	public NeoDatisConfig setDebugLayers(boolean yesNo);

	public String getBaseDirectory();

	public NeoDatisConfig setBaseDirectory(String baseDirectory);

	public boolean useLazyInstantiationInServerMode();

	public NeoDatisConfig setUseLazyInstantiationInServerMode(boolean useLazyInstantiationInServerMode);

	/**
	 * @return
	 */
	public long getTimeoutToAcquireMutex();

	public NeoDatisConfig registerDatabaseStartupManager(DatabaseStartupManager manager);

	public NeoDatisConfig removeDatabaseStartupManager();

	public DatabaseStartupManager getDatabaseStartupManager();

	public boolean logSchemaEvolutionAnalysis();

	public NeoDatisConfig setLogSchemaEvolutionAnalysis(boolean log);

	public boolean isTransactional();

	public NeoDatisConfig setTransactional(boolean transactional);

	public String getUser();

	public NeoDatisConfig setUser(String user);

	public String getPassword();

	public NeoDatisConfig setPassword(String password);

	/**
	 * @param homeDirectory
	 */
	public NeoDatisConfig setHomeDirectory(String homeDirectory);

	public String getHomeDirectory();

	public String getHost();

	public NeoDatisConfig setHostAndPort(String host, int port);

	public int getPort();

	public NeoDatisConfig setPort(int port);

	public boolean isLocal();

	public NeoDatisConfig setIsLocal(boolean isLocal);

	/**
	 * @return
	 */
	public NeoDatisConfig copy();

	public NeoDatisConfig addServer(String host, int port);

	public List<Server> getServers();

	public boolean useNativeConfig();

	public NeoDatisConfig setUseNativeConfig(boolean useNativeConfig);

	public int getNumberOfServers();

	public boolean debugStorageEngine();

	public NeoDatisConfig setDebugStorageEngine(boolean yesNo);

	public boolean debugMessageStreamer();

	/**
	 * @param string
	 * @param string2
	 * @return
	 */
	public NeoDatisConfig setUserAndPassword(String user, String password);

	/**
	 * @return The Directory used for Server Inbox (used for file transfer)
	 */
	public String getInboxDirectory();
	public NeoDatisConfig setInboxDirectory(String dir);
	
	/** To know if the client server mode uses ssl*/
	public boolean isSSL();
	public NeoDatisConfig setSSL(boolean ssl);

	public Class getOidGeneratorClass();
	public NeoDatisConfig setOidGeneratorClass(Class oidGeneratorClass);
	
	/** indicates if the storage engine must use its own cache or not. This can leverage performance at the cost of memory.
	 * 
	 * @param yes
	 * @return
	 */
	public NeoDatisConfig setUseStorageEngineCache(boolean yesOrNo);
	public boolean useStorageEngineCache();
	
	/** indicates if the oid generator can use cache
	 * 
	 * @param yes
	 * @return
	 */
	public NeoDatisConfig setOidGeneratorUseCache(boolean yesOrNo);
	public boolean oidGeneratorUseCache();
	
	public void setUserInfo(String info);
	public String getUserInfo();
	
}

package uk.co.devworx.spark_examples.pushdown;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.StructType;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The execution environment encapsulates a number of
 * settings for this particular environment. E.g. how to create Datasets,
 * the batch sizes etc.
 */
public abstract class ExecutionEnv
{
	private static final Logger logger = LogManager.getLogger(ExecutionEnv.class);

	public static final String GLOBAL_TMP_DATABASE = "global_temp";
	public static final String GLOBAL_TMP_PREFIX = GLOBAL_TMP_DATABASE + ".";
	public static final int DEFAULT_BUFFER_SIZE = 1_000;

	public static enum EnvType
	{
		LOCAL,
		LOCAL_H2_BACKED,
		REMOTE
	}

	private static final EnvType DEFAULT_ENV_TYPE =  EnvType.LOCAL;
	public static EnvType getDefaultEnvType()
	{
		return DEFAULT_ENV_TYPE;
	}

	private static final ConcurrentMap<EnvType, ExecutionEnv > _envs = new ConcurrentHashMap<>();
	private static final ReentrantLock _lock = new ReentrantLock();

	public static ExecutionEnv getInstance()
	{
		return getInstance(getDefaultEnvType());
	}

	public static ExecutionEnv getInstance(EnvType envType)
	{
		ExecutionEnv env = _envs.get(EnvType.LOCAL);
		if(env != null) return env;

		_lock.lock();
		try
		{
			env = _envs.get(envType);
			if(env != null) return env;

			switch(envType)
			{

			case LOCAL:
				env = new _Local_ExecutionEnv();
				break;
			case LOCAL_H2_BACKED:
				break;
			case REMOTE:
				break;
			}

			_envs.put(envType, env);
			return env;

		}
		finally
		{
			_lock.unlock();
		}
	}

	protected final SparkSession sparkSession;

	ExecutionEnv(SparkSession sparkSession)
	{
		this.sparkSession = sparkSession;
	}

	public abstract EnvType getType();

	public int getBufferSize()
	{
		return DEFAULT_BUFFER_SIZE;
	}

	public SparkSession getSparkSession()
	{
		SparkContext sparkContext = sparkSession.sparkContext();
		System.out.println("SPARK CONTEXT : " + sparkContext);

		return sparkSession;
	}

	public static String getDatasetName(String baseName, Class<?> type)
	{
		return ExecutionEnv.GLOBAL_TMP_PREFIX + getDatasetNameWithoutDatabase(baseName, type);
	}

	public static String getDatasetNameWithoutDatabase(String baseName, Class<?> type)
	{
		return baseName + "_" + type.getSimpleName();
	}

	public abstract <T> Dataset<Row> newDataset(String baseName, final List<T> initialRows);

}

class _Local_ExecutionEnv extends ExecutionEnv
{

	private static final Logger logger = LogManager.getLogger(_Local_ExecutionEnv.class);

	public _Local_ExecutionEnv()
	{
		super(SparkSession.builder()
					  .appName("_Local_ExecutionEnv")
					  .master("local[1]")
					  .config("spark.sql.orc.impl","native")
					  .config("spark.sql.datetime.java8API.enabled","true")
					  .config("spark.sql.parquet.compression.codec", "gzip")
					  .config("spark.driver.bindAddress", "127.0.0.1")
					  .getOrCreate());

		SparkSession sparkSession = getSparkSession();
	}

	@Override public EnvType getType()
	{
		return EnvType.LOCAL;
	}

	@Override
	public <T> Dataset<Row> newDataset(String baseName, final List<T> initialRows)
	{
		if(initialRows.isEmpty() == true)
		{
			throw new IllegalArgumentException("You cannot pass in an empty set of initial rows !");
		}

		T firstItem = initialRows.get(0);
		Class<T> type = (Class) firstItem.getClass();

		final Encoder<T> encoder = Encoders.bean(type);
		final StructType schema = encoder.schema();

		schema.printTreeString();

		logger.info(Arrays.toString(schema.fieldNames()));

		Dataset<T> objSet = sparkSession.createDataset(initialRows, encoder);

		logger.info(objSet.schema());

		logger.info(objSet.stat());

		Dataset<Row> dataset = sparkSession.createDataset(initialRows, encoder).toDF();

		dataset.show();

		logger.info(dataset.schema());

		final String name = getDatasetNameWithoutDatabase(baseName, type);
		dataset.createOrReplaceGlobalTempView(name);
		return dataset;
	}

}











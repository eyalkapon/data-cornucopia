package uk.co.devworx.spark_examples.elt;

import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Test;

import uk.co.devworx.spark_examples.elt.model.StockPrice;

/**
 * An example java test-case for the 01 scala example
 * 
 * @author jsteenkamp
 *
 */
public class _01_LoadCSVFileCalculateMidTest
{

	@Test
	public void test()
	{
		SparkSession sparkSession = SparkSession.builder()
												.appName("SparkTestCase")
												.master("local[1]")
											    .config("spark.driver.bindAddress", "127.0.0.1")
												.getOrCreate();
		
		Dataset<Row> df = sparkSession.read()
									  .option("header", "true")
									  .csv("src/main/resources/data/EOD-Stock-Prices-2020-02-12.csv");
		
		//Mapping function from CSV to Stock Price
		MapFunction<Row, StockPrice> mapFunction = row -> 
		{
			return StockPrice.builder()
							 .setStockId(row.getAs(1))
							 .setPriceDate(row.getAs(2))
							 .setLastOrMidPrice(row.getAs(5))
							 .setOpen(row.getAs(6))
							 .setHigh(row.getAs(7))
							 .setLow(row.getAs(8))
							 .setClose(row.getAs(9))
							 .setVolume(row.getAs(10))
							 .setAdjustedOpen(row.getAs(11))
							 .setAdjustedHigh(row.getAs(12))
							 .setAdjustedLow(row.getAs(13))
							 .setAdjustedClose(row.getAs(14))
							 .setAdjustedVolume(row.getAs(15))
							 .setDividend(row.getAs(16))
							 .build();
		};
		
		Dataset<StockPrice> df_stockPrices = df.map(mapFunction, Encoders.bean(StockPrice.class));
		
		df_stockPrices.foreach(fef -> 
		{
			double calculatedMid = fef.getCalculatedMid();
			double calculatedAdjustedMid = fef.getCalculatedAdjustedMid();
			System.out.println(fef.getStockId() + " -> calculatedMid : " + calculatedMid + ", calculatedAdjustedMid : " + calculatedAdjustedMid);
		});
		
	}
	
}

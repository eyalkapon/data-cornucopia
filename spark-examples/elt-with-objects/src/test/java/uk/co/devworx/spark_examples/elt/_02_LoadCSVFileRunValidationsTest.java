package uk.co.devworx.spark_examples.elt;

import java.util.List;

import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Test;

import uk.co.devworx.spark_examples.elt.model.BuildValidationFailure;
import uk.co.devworx.spark_examples.elt.model.StockPrice;

/**
 * An example java test-case for the 02 scala example
 * 
 * @author jsteenkamp
 *
 */
public class _02_LoadCSVFileRunValidationsTest
{

	@SuppressWarnings({ "unchecked", "rawtypes" })
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
									  .csv("src/main/resources/data/EOD-Stock-Prices-2020-02-12-Broken-Subset.csv");
		
		//Mapping function from CSV to Stock Price Builders
		MapFunction<Row, List<BuildValidationFailure>> mapFunction = row -> 
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
							 .setDividend(row.getAs(16)).getValidationFailures();
		};
		
		Dataset<List<BuildValidationFailure>> df_failures = (Dataset)df.map((MapFunction)mapFunction, Encoders.javaSerialization(List.class));
		
		df_failures.foreach(fe -> 
		{
			if(fe.size() > 0)
			{
				fe.forEach(System.out::println);
			}
		});
		
	}
	
}


























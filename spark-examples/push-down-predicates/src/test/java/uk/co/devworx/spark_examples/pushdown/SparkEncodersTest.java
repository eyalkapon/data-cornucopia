package uk.co.devworx.spark_examples.pushdown;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;

@Disabled
public class SparkEncodersTest
{
	private static final Logger logger = LogManager.getLogger(ExecutionEnv.class);

	@Test
	public void testEncoders()
	{
		SparkSession sparkSession = ExecutionEnv.getInstance().getSparkSession();
		Encoder<Simplest> enc = Encoders.bean(Simplest.class);
		enc.schema().printTreeString();

		Simplest s1 = new Simplest();
		s1.setName("Name-1");
		s1.setValue(1000);
		Simplest_Inner i1 = new Simplest_Inner();
		i1.setInnerName("Inner-Name-1");
		i1.setInnerDouble(2000.9);
		s1.setInner(i1);

		StructType schema = enc.schema();
		schema.printTreeString();

		Dataset<Simplest> dataSet = sparkSession.createDataset(Collections.singletonList(s1), enc);

		dataSet.show();

	}

	@Test
	public void testEncoders_2()
	{
		SparkSession sparkSession = ExecutionEnv.getInstance().getSparkSession();
		Encoder<OrderResult> enc = Encoders.bean(OrderResult.class);

		OrderResult s1 = new OrderResult();
		s1.setEntityId("");
		s1.setResult(BigDecimal.TEN);
		OrderResultInput in = new OrderResultInput();
		in.setCustomer_order_base_price(BigDecimal.TEN);
		in.setFilled_amount(BigDecimal.TEN);
		in.setLast_confirmed_amount(BigDecimal.TEN);
		s1.setInput(in);

		StructType schema = enc.schema();
		schema.printTreeString();

		Dataset<OrderResult> dataSet = sparkSession.createDataset(Collections.singletonList(s1), enc);

		dataSet.show();

	}



}

package org.simple4j.wsfeeler.test;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.hsqldb.server.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.simple4j.wsfeeler.model.TestSuite;
import org.simple4j.wsfeeler.pojoashttp.HTTPExposer;
import org.simple4j.wsfeeler.test.ws.UserVO;
import org.simple4j.wsfeeler.test.ws.UserWS;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import spark.Spark;

public class MainTest
{

	private static Process dbProcess;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		startDB();
		
		startWebService();
		
		exposePOJOAsHTTPService();

	}

	private static void exposePOJOAsHTTPService()
	{
		ApplicationContext ac = new ClassPathXmlApplicationContext("ws/main-appCntxt.xml");
		HTTPExposer httpExposer = new HTTPExposer(ac);
		httpExposer.expose();
	}

	private static void startWebService()
	{
		UserWS.start();
	}

	private static void startDB() throws IOException
	{
		// java -cp ../lib/hsqldb.jar org.hsqldb.server.Server --database.0 file:mydb
		// --dbname.0 xdb

		System.out.println(System.getenv());
		System.out.println("-----");
		System.out.println(System.getProperties());
		System.out.println("-----");
		//below line is just to make sure the hsqldb Server class is in the classpath
		Server s;
		ProcessBuilder pb = new ProcessBuilder("java", "-cp", System.getProperty("java.class.path"), "org.hsqldb.server.Server", "--database.0", "file:./target/mydb",
				"--dbname.0", "xdb");
		pb.inheritIO();
		System.out.println(pb.environment());
		System.out.println("-----");
		
		System.out.println("Starting hsqldb");
		dbProcess = pb.start();
		try
		{
			Thread.sleep(5000);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		System.out.println("Started hsqldb");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		dbProcess.destroy();
	}

	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void test()
	{
		TestSuite ts = new TestSuite();
		boolean success = ts.execute();
		Assert.assertTrue("Failed testcases are :" + ts.getFailedTestCases(), success);
	}

}

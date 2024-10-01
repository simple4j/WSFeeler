package org.simple4j.wsfeeler;

import org.simple4j.wsfeeler.model.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main method entry point class for stand alone execution of the test suite
 */
public class Main
{
	private static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args)
	{
		if(args.length < 1 && args[0] != null && args[0].trim().length() <=0 )
		{
			logger.error("Not enough number of parameters.");
			logger.error("Usage : org.simple4j.wsfeeler.Main <test suite root> <test case executor thread pool size>");
		}

		TestSuite ts = new TestSuite();
	
		if(args.length > 1 && args[1] != null && args[1].trim().length() > 0)
		{
			if(!args[1].trim().matches("[0-9]*"))
			{
				logger.error("Test case executor thread pool size is not a number. {}", args[1].trim());
			}
			ts.setTestCaseExecutorThreadPoolSize(Integer.parseInt(args[1].trim()));
		}

		ts.setTestSuiteRoot(args[0]);
		boolean success = ts.execute();
		
		if(success)
		{
			logger.info("Test suite execution completed successfuly");
		}
		else
		{
			logger.error("Test suite execution completed with failures:{}", ts.getFailedTestCases());
		}
	}

}

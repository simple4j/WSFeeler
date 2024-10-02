package org.simple4j.wsfeeler.model;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents an abstract test step. Concrete implementation could be for a specific connector/protocol
 */
public abstract class TestStep
{
	private static Logger logger = LoggerFactory.getLogger(TestStep.class);

	/**
	 * Name of the test step
	 */
	public String name = null;
	
	/**
	 * Short name of the test step. This will be the input properties filename without the suffix -input.properties
	 */
	public String shortName = null;
	
	/**
	 * File object of the input properties file
	 */
	public File testStepInputFile = null;
	
	/**
	 * Parent test case object
	 */
	public TestCase parent = null;
	
	/**
	 * Test suite object
	 */
	public TestSuite testSuite = null;
	
	/**
	 * Test step variables
	 */
	public Map<String, Object> testStepVariables = null;
	private Boolean success = null;
	
	/**
	 * Result of the test step execution
	 * @return - true if successful
	 */
	public Boolean getSuccess()
	{
		return success;
	}

	public void setSuccess(Boolean success)
	{
		this.success = success;
	}

	public TestStep(Map<String, Object> testStepInputVariables, File testStepInputFile, TestCase parent, TestSuite testSuite)
	{
		this.testStepVariables=testStepInputVariables;
		this.testStepInputFile = testStepInputFile;
		this.testSuite = testSuite;
		String testStepAbsolutePath = testStepInputFile.getAbsolutePath();
		this.name = testStepAbsolutePath.substring(this.testSuite.getTestSuiteDirectory().getAbsolutePath().length(),testStepAbsolutePath.length()-"input.properties".length());
		String inputFileName = testStepInputFile.getName();
		logger.info("inputFileName {}", inputFileName);
		this.shortName = inputFileName.substring(0,inputFileName.length()-"-input.properties".length());
		logger.info("shortName {}", this.shortName);
		this.parent = parent;
	}

	/**
	 * Abstract entry point method to execute the test step
	 * 
	 * @return true if success
	 */
	public abstract boolean execute();
	
	/**
	 * Get test step property for cross reference
	 * 
	 * @param key
	 * @return
	 */
	public Object getProperty(String key)
	{
		logger.info("Entering getProperty {}", key);
		Object ret = this.testStepVariables.get(key);
		logger.info("Exiting getProperty {}", ret);
		return ret;
	}

	/**
	 * Factory method to get an instance of test step
	 */
	public static TestStep getInstance(String typeOfStep, Map<String, Object> testStepInputVariables, 
			File testStepInputFile, TestCase parent, TestSuite testSuite) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException
	{
		return (TestStep) Class.forName(typeOfStep).getConstructor(Map.class, File.class, TestCase.class, TestSuite.class)
				.newInstance(testStepInputVariables, testStepInputFile, parent, testSuite);
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString()).append(" [name=").append(name).append(", shortName=").append(shortName)
				.append(", testStepInputFile=").append(testStepInputFile).append(", testStepVariables=")
				.append(testStepVariables).append(", success=").append(success).append("]");
		return builder.toString();
	}

	/**
	 * Generates report and prints out in the logger
	 * @param level
	 */
	public void generateReport(int level)
	{
		StringBuilder indentation = this.testSuite.getIndentation(level);
		if(this.success == null)
		{
			logger.info("{}SKIPPED {}", indentation, this.shortName);
		}
		else
		{
			if(this.success)
			{
				logger.info("{}PASSED  {}", indentation, this.shortName);
			}
			else
			{
				logger.info("{}FAiLED  {}", indentation, this.shortName);
			}
		}
	}

}

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
	
	public String name = null;
	public String shortName = null;
	public File testStepInputFile = null;
	public TestCase parent = null;
	public TestSuite testSuite = null;
	public Map<String, Object> testStepVariables = null;
	private Boolean success = null;
	
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
	 * 
	 * @return true if success
	 */
	public abstract boolean execute();
	
	public Object getProperty(String key)
	{
		logger.info("Entering getProperty {}", key);
		Object ret = this.testStepVariables.get(key);
		logger.info("Exiting getProperty {}", ret);
		return ret;
	}

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

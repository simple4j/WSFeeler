package org.simple4j.wsfeeler.model;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public abstract class TestStep
{

	public String name = null;
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
		this.parent = parent;
	}

	/**
	 * 
	 * @return true if success
	 */
	public abstract boolean execute();
	
	public Object getProperty(String key)
	{
		return this.testStepVariables.get(key);
	}

	public static TestStep getInstance(String typeOfStep, Map<String, Object> testStepInputVariables, File testStepInputFile, TestCase parent, TestSuite testSuite) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException
	{
		return (TestStep) Class.forName(typeOfStep).getConstructor(Map.class, File.class, TestCase.class, TestSuite.class)
				.newInstance(testStepInputVariables, testStepInputFile, parent, testSuite);
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString()).append(" [name=").append(name).append(", testStepInputFile=").append(testStepInputFile)
				.append(", testStepVariables=").append(testStepVariables).append(", success=").append(success)
				.append("]");
		return builder.toString();
	}
}

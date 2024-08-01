package org.simple4j.wsfeeler.model;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.simple4j.wsfeeler.core.ConfigLoader;
import org.simple4j.wsfeeler.core.TestCaseExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bsh.EvalError;

public class TestCase implements Callable<Boolean>
{
	private static Logger logger = LoggerFactory.getLogger(TestCase.class);
	
	public String name = null;
	public File testCaseDirectory = null;
	public TestCase parent = null;
	public TestSuite testSuite = null;
	public Map<String, Object> testCaseVariables = null;
	public Map<String, TestStep> executedTestSteps = new HashMap<String, TestStep>();
	private TestCaseExecutor testCaseExecutor = new TestCaseExecutor(this);
	private Boolean success = null;
	private List<TestCase> subTestCases = null;

	public Boolean getSuccess()
	{
		return success;
	}

	public TestCase(File testCaseDirectory, TestSuite testSuite, TestCase parent)
	{
		this.testSuite = testSuite;
		this.name = testCaseDirectory.getAbsolutePath().substring(this.testSuite.getTestSuiteDirectory().getAbsolutePath().length());
		this.testCaseDirectory = testCaseDirectory;
		this.parent = parent;
	}

	public boolean execute() throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException, InterruptedException, ExecutionException
	{
		logger.info("Inside execute:{}", name);
		if(!this.testSuite.canExecute(name))
		{
			return success;
		}
		initVariables();
		loadCustomVariables();
		
		this.executeTestSteps();
		
		if(success)
		{
			subTestCases = testCaseExecutor.execute(testCaseDirectory, testSuite);
			
			for(int i = 0 ; i < subTestCases.size() ; i++)
			{
				if(!subTestCases.get(i).success)
				{
					success = false;
					return success;
				}
			}
		}
		return success;
	}

	private void executeTestSteps() throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException
	{
        File[] testStepFiles = this.testCaseDirectory.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname)
			{
				if(!pathname.isDirectory() && pathname.exists())
				{
					return pathname.getName().endsWith("input.properties");
				}
				return false;
			}});
        
        List<File> sortTestStepFiles = sortFiles(testStepFiles);

        logger.info("found teststeps:{}", sortTestStepFiles);
        for (int i = 0; i < sortTestStepFiles.size(); i++)
        {
            File testStepFile = sortTestStepFiles.get(i);
            logger.info("Processing file:"+testStepFile);
            success = executeTestStep(testStepFile);
            if(!success)
            {
                this.testSuite.addFailedTestCases(this);
                return;
            }
        }
		
	}

    private boolean executeTestStep(File testStepFile) throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException
	{
		Map<String, Object> testStepInputVariables = ConfigLoader.loadVariables(testStepFile, this);
		logger.info("Teststep input variables:{}", testStepInputVariables);
        String typeOfStep = (String) testStepInputVariables.get("TypeOfStep");
        if(typeOfStep == null || typeOfStep.trim().length() == 0)
        {
            logger.error("TypeOfStep missing in file:"+testStepFile);
    		return false;
        }
        else
        {
			TestStep step = TestStep.getInstance(typeOfStep, testStepInputVariables, testStepFile, this, this.testSuite);
			this.executedTestSteps.put(step.shortName, step);
        	boolean ret = step.execute();
			return ret;
        }
	}

	private List<File> sortFiles(File[] testcaseDirs) {
        List<File> testcaseDirsList = null;
        if(testcaseDirs != null)
        {
            testcaseDirsList = Arrays.asList(testcaseDirs);
            Comparator<? super File> c = new Comparator<File>() {

                public int compare(File o1, File o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            };
            Collections.sort(testcaseDirsList, c );

        }
        return testcaseDirsList;
    }

	private void initVariables()
	{
		if(this.testCaseVariables == null)
		{
			this.testCaseVariables = new ConcurrentHashMap<String, Object>();
			Map<String, Object> testSuiteVariables = this.testSuite.getTestSuiteVariables();
            for (Entry<String, Object> entry : testSuiteVariables.entrySet())
			{
				this.testCaseVariables.put(entry.getKey(), entry.getValue());
			}
			testCaseVariables.put("TESTCASE/STARTTIME", ""+System.currentTimeMillis());
			testCaseVariables.put("TESTCASE/UUID", UUID.randomUUID().toString());
			testCaseVariables.put("TESTCASE/RAND5", ""+Math.round(Math.random()*99999));
			testCaseVariables.put("TESTCASE/RAND10", ""+Math.round(Math.random()*9999999999L));
		}
	}

	private void loadCustomVariables()
	{
        InputStream variablesStream = null;
        try
        {
            File tcVariablesFile = new File(this.testCaseDirectory,"tcvariables.properties");
            if(tcVariablesFile.exists())
            {
    			variablesStream = new FileInputStream(tcVariablesFile);
    			testCaseVariables = ConfigLoader.loadVariables(variablesStream , testCaseVariables, "TESTCASE/");
            }
        } catch (EvalError e)
		{
			throw new RuntimeException(e);
		} catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
    	finally
    	{
    		if(variablesStream != null)
    		{
				try
				{
					variablesStream.close();
				} catch (IOException e)
				{
					logger.warn("Error while closing cutom variable stream for case:{}", this.name, e);
				}
    		}
    	}
	}

	@Override
	public Boolean call() throws Exception
	{
		
        this.execute();
        if(success == null)
        {
            logger.info("SKIPPING: Testcase {}",this.name);
        }

        if(!success)
        {
            logger.info("FAILURE: Testcase {}",this.name);
        }
		return success;
	}

	public Object getProperty(String key)
	{
		logger.info("Entering getProperty {} {}", this.testCaseDirectory, key);
		if(key.startsWith("../"))
		{
			logger.info("Starts with ../");
			if(this.parent != null)
			{
				return this.parent.getProperty(key.substring(3));
			}
			else
			{
				throw new RuntimeException("Looking up parent property:"+key+", missing parent test case.");
			}
		}
		else
		{
			if(key.startsWith("TESTSUITE/"))
			{
				logger.info("Starts with TESTSUITE/");
				key = key.substring(10);
				return this.testSuite.getTestSuiteVariableValue(key);
			}
			else
			{
				if(key.startsWith("./"))
				{
					logger.info("Starts with ./");
					key = key.substring(2);
				}

				if(key.startsWith("TESTCASE/"))
				{
					logger.info("Starts with TESTCASE/");
					return this.testCaseVariables.get(key);
				}
				if(key.indexOf("/") > 1)
				{
					logger.info("Contains /");
					String stepName = key.substring(0,key.indexOf("/"));
					logger.info("stepName {}", stepName);
					if(stepName == null || stepName.trim().length() < 1)
						return null;
					key = key.substring(key.indexOf("/")+1);
					logger.info("key {}", key);
					if(key == null || key.trim().length() < 1)
						return null;
					TestStep testStep = this.executedTestSteps.get(stepName);
					logger.info("testStep {}", testStep);
					if(testStep == null)
						return null;
					return testStep.getProperty(key);
				}
				return this.testCaseVariables.get(key);
			}
		}
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString()).append(" [name=").append(name).append(", testCaseDirectory=").append(testCaseDirectory)
				.append(", testCaseVariables=").append(testCaseVariables).append(", executedTestSteps=").append(executedTestSteps)
				.append(", success=").append(success).append(", subTestCases=").append(subTestCases).append("]");
		return builder.toString();
	}
}

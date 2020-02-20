package org.simple4j.wsfeeler.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.simple4j.wsclient.exception.SystemException;
import org.simple4j.wsfeeler.core.ConfigLoader;
import org.simple4j.wsfeeler.core.TestCaseExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import bsh.EvalError;

public class TestSuite
{
	private static Logger logger = LoggerFactory.getLogger(TestSuite.class);
	
	private String testSuiteClasspathRoot = "/wstestsuite"; 
	private Map<String, Object> testSuiteVariables = null;
	private List<TestCase> testCases = null;
	private String includesTestCasesRegex = null;
	private String excludesTestCasesRegex = null;
	private ApplicationContext mainApplicationContext = null;
	private File testSuiteDirectory = null;
	private TestCaseExecutor testCaseExecutor = new TestCaseExecutor();
    private List<TestCase> failedTestCases = new ArrayList<TestCase>();

	public File getTestSuiteDirectory()
	{
		return testSuiteDirectory;
	}

	public void setTestSuiteDirectory(File testSuiteDirectory)
	{
		this.testSuiteDirectory = testSuiteDirectory;
	}

	public ApplicationContext getMainApplicationContext()
	{
		return mainApplicationContext;
	}

	public void setMainApplicationContext(ApplicationContext mainApplicationContext)
	{
		this.mainApplicationContext = mainApplicationContext;
	}

	public void initVariables()
	{
		try
		{
			if(this.testSuiteVariables == null)
			{
				testSuiteVariables = new HashMap<String, Object>();
				testSuiteVariables.put("TESTSUITE/HOSTNAME", InetAddress.getLocalHost().getHostName());
				testSuiteVariables.put("TESTSUITE/HOSTIP", InetAddress.getLocalHost().getHostAddress());
				testSuiteVariables.put("TESTSUITE/STARTTIME", ""+System.currentTimeMillis());
				testSuiteVariables.put("TESTSUITE/UUID", UUID.randomUUID().toString());
				testSuiteVariables.put("TESTSUITE/RAND5", Math.round(Math.random()*99999));
				testSuiteVariables.put("TESTSUITE/RAND10", Math.round(Math.random()*9999999999L));
			}
		} catch (UnknownHostException e)
		{
			throw new SystemException("", e);
		}
	}

	public void loadCustomVariables()
	{
        InputStream variablesStream = null;
        try
        {
            File testSuiteVariablesFile = new File(this.testSuiteDirectory,"/tsvariables.properties");
            if(testSuiteVariablesFile.exists())
            {
			variablesStream = new FileInputStream(testSuiteVariablesFile);
//            if(variablesStream == null)
//            {
//                logger.error("InputSteam is null when loading from classpath:/tsvariables.properties");
//                return;
//            }
			testSuiteVariables = ConfigLoader.loadVariables(variablesStream , testSuiteVariables, "TESTSUITE/");
			
//			File testSuiteVariables = ConfigLoader.getClassPathFile("/tsvariables.properties");
//			this.testSuiteDirectory = testSuiteVariables.getParentFile();
            }
        } catch (EvalError e)
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
					logger.warn("Error while closing custom variable stream for suite", e);
				}
    		}
    	}
        
	}

	public void loadConnectors()
	{
		mainApplicationContext = new ClassPathXmlApplicationContext(this.testSuiteClasspathRoot+"/connectors/main-appContext.xml");
	}

	public void execute()
	{
		this.initPath();
		this.initVariables();
		this.loadCustomVariables();
		this.loadConnectors();
		File testcasesDir = new File(this.testSuiteDirectory,"/testcases");
		this.testCases = testCaseExecutor.execute(testcasesDir, this);
        File[] testcaseDirs = testcasesDir.listFiles();
	}

	private void initPath()
	{
		this.testSuiteDirectory = ConfigLoader.getClassPathFile(this.testSuiteClasspathRoot);
	}

	public Object getTestSuiteVariableValue(String variableName)
	{
		if(this.testSuiteVariables == null)
			return null;
		
//		variableName = variableName.replaceFirst("(TESTSUITE.)", "");
		return this.testSuiteVariables.get(variableName);
	}

	public Map<String, Object> getTestSuiteVariables()
	{
		return testSuiteVariables;
	}

	public void setTestSuiteVariables(Map<String, Object> testSuiteVariables)
	{
		this.testSuiteVariables = testSuiteVariables;
	}

	public void addFailedTestCases(TestCase testCase)
	{
		failedTestCases.add(testCase);
		
	}
	
	
}

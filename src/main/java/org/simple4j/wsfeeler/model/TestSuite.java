package org.simple4j.wsfeeler.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.simple4j.wsclient.exception.SystemException;
import org.simple4j.wsfeeler.core.ConfigLoader;
import org.simple4j.wsfeeler.core.TestCaseExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import bsh.EvalError;

/**
 * This class represents the test suite and entry point to trigger execution of test suite.
 * TestSuite consists of nested hierarchy of TestCases which can contain TestSteps.
 * Sibling test cases are executed in parallel and the test steps are executed in ascending sequence.
 * 
 * More details about the usage and structure of the test suite are documented in readme files and 
 * test step configuration files under src/test/resources
 */
public class TestSuite
{
	private static Logger logger = LoggerFactory.getLogger(TestSuite.class);
	
	private String testSuiteRoot = "/wstestsuite"; 
	private Map<String, Object> testSuiteVariables = null;
	private List<TestCase> testCases = null;
	private String includesTestCasesRegex = null;
	private String excludesTestCasesRegex = null;
	private int testCaseExecutorThreadPoolSize = 5;
	private ApplicationContext connectorsApplicationContext = null;
	private File testSuiteDirectory = null;
	private boolean isClasspathTestSuiteRoot;
	private TestCaseExecutor testCaseExecutor = new TestCaseExecutor(null);
    private List<TestCase> failedTestCases = new ArrayList<TestCase>();
    private ApplicationContext testApplicationContext = null;

	public String getTestSuiteRoot()
	{
		return testSuiteRoot;
	}

	public void setTestSuiteRoot(String testSuiteRoot)
	{
		this.testSuiteRoot = testSuiteRoot;
	}

	public File getTestSuiteDirectory()
	{
		return testSuiteDirectory;
	}

	public void setTestSuiteDirectory(File testSuiteDirectory)
	{
		this.testSuiteDirectory = testSuiteDirectory;
	}

	public ApplicationContext getConnectorsApplicationContext()
	{
		return connectorsApplicationContext;
	}

	public void setConnectorsApplicationContext(ApplicationContext mainApplicationContext)
	{
		this.connectorsApplicationContext = mainApplicationContext;
	}

	public String getIncludesTestCasesRegex()
	{
		return includesTestCasesRegex;
	}

	public void setIncludesTestCasesRegex(String includesTestCasesRegex)
	{
		if(includesTestCasesRegex!= null && includesTestCasesRegex.trim().length()<=0)
			this.includesTestCasesRegex = null;
		this.includesTestCasesRegex = includesTestCasesRegex;
	}

	public String getExcludesTestCasesRegex()
	{
		return excludesTestCasesRegex;
	}

	public void setExcludesTestCasesRegex(String excludesTestCasesRegex)
	{
		if(excludesTestCasesRegex!= null && excludesTestCasesRegex.trim().length()<=0)
			this.excludesTestCasesRegex = null;
		this.excludesTestCasesRegex = excludesTestCasesRegex;
	}

	public int getTestCaseExecutorThreadPoolSize()
	{
		return testCaseExecutorThreadPoolSize;
	}

	public void setTestCaseExecutorThreadPoolSize(int testCaseExecutorThreadPoolSize)
	{
		this.testCaseExecutorThreadPoolSize = testCaseExecutorThreadPoolSize;
	}

	public ApplicationContext getTestApplicationContext()
	{
		return testApplicationContext;
	}

	public void setTestApplicationContext(ApplicationContext testApplicationContext)
	{
		this.testApplicationContext = testApplicationContext;
	}

	private void initVariables()
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
				testSuiteVariables.put("TESTSUITE/RAND5", ""+Math.round(Math.random()*99999));
				testSuiteVariables.put("TESTSUITE/RAND10", ""+Math.round(Math.random()*9999999999L));
			}
		} catch (UnknownHostException e)
		{
			throw new SystemException("", e);
		}
	}

	private void loadCustomVariables()
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

	private void loadConnectors()
	{
		if(isClasspathTestSuiteRoot)
		{
			connectorsApplicationContext = new ClassPathXmlApplicationContext(this.testSuiteRoot+"/connectors/main-appContext.xml");
		}
		else
		{
			connectorsApplicationContext = new FileSystemXmlApplicationContext("file:"+this.testSuiteDirectory.getAbsolutePath()+File.separator+"connectors"+File.separator+"main-appContext.xml");
		}
	}

	/**
	 * This is the entrypoint method to trigger execution of the test suite
	 * 
	 * @return - success boolean response
	 */
	public boolean execute()
	{
		this.initPath();
		this.initVariables();
		this.loadCustomVariables();
		logger.debug("Testcase variables after loading custom variables {}", this.testSuiteVariables);
		this.loadIncludesExcludes();
		this.loadConnectors();
		File testcasesDir = new File(this.testSuiteDirectory,"/testcases");
		this.testCases = testCaseExecutor.execute(testcasesDir, this);
		
		this.generateReport();
		if(this.failedTestCases.size() > 0)
		{
			return false;
		}
		return true;
	}

	private void generateReport()
	{
		try
		{
			logger.info("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ WSFeeler test report start ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
			int level=0;
			for (Iterator<TestCase> iterator = testCases.iterator(); iterator.hasNext();)
			{
				TestCase testCase = iterator.next();
				testCase.generateReport(level);
			}
		}
		finally
		{
			logger.info("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~  WSFeeler test report end  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
			
		}
		
	}

	private void loadIncludesExcludes()
	{
		
		Properties includesExcludes = new Properties();
		InputStream is = null;
		String filename = "includesExcludes.properties";
		try
		{
			File file = new File(this.testSuiteDirectory, filename);
			if(file.exists() && file.isFile())
			{
				is = new FileInputStream(file);
				includesExcludes.load(is);
				this.includesTestCasesRegex = includesExcludes.getProperty("includesTestCasesRegex");
				this.excludesTestCasesRegex = includesExcludes.getProperty("excludesTestCasesRegex");
			}
		} catch (IOException e)
		{
			throw new RuntimeException("Exception whiel loading "+filename, e);
		}
		finally
		{
			if(is != null)
			{
				try
				{
					is.close();
				} catch (IOException e)
				{
					logger.warn("Exception while closing "+filename, e);
				}
			}
		}
		
	}

	private void initPath()
	{
		this.isClasspathTestSuiteRoot = true;
		this.testSuiteDirectory = getClassPathFile(this.testSuiteRoot);
		if(this.testSuiteDirectory == null || !this.testSuiteDirectory.exists())
		{
			this.testSuiteDirectory = new File(this.testSuiteRoot);
			this.isClasspathTestSuiteRoot = false;
		}
		
		if(!this.testSuiteDirectory.exists())
		{
			throw new RuntimeException("Testsuite directory does not exist:"+this.testSuiteDirectory);
		}

		if(!this.testSuiteDirectory.isDirectory())
		{
			throw new RuntimeException("Testsuite root is not a directory:"+this.testSuiteDirectory);
		}
	}

	private File getClassPathFile(String path)
	{
		URL resource = ConfigLoader.class.getResource(path);
		if (resource == null || resource.getFile() == null)
			return null;
		return new File(resource.getFile());
	}

	public boolean canExecute(String testCaseName)
	{
		boolean ret = false;
		
		if(testCaseName == null)
			return ret;
		
		if(this.includesTestCasesRegex != null && testCaseName.matches(this.includesTestCasesRegex))
			ret = true;
		
		if(this.excludesTestCasesRegex != null && testCaseName.matches(this.excludesTestCasesRegex))
			ret = false;
		
		return ret;
	}
	
	public Object getTestSuiteVariableValue(String variableName)
	{
		if(this.testSuiteVariables == null)
			return null;
		
		logger.debug("getTestSuiteVariableValue {}", variableName);
		
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

	public List<TestCase> getTestCases()
	{
		return testCases;
	}

	public List<TestCase> getFailedTestCases()
	{
		return failedTestCases;
	}

	public StringBuilder getIndentation(int level)
	{
		StringBuilder indentation = new StringBuilder();
		for(int i = 0 ; i < level; i++)
			indentation.append("   ");
		
		return indentation;
	}
	
	
}

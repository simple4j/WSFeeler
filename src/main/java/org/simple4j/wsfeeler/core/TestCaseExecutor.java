package org.simple4j.wsfeeler.core;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.simple4j.wsfeeler.model.TestCase;
import org.simple4j.wsfeeler.model.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class has the executor service to manage parallel execution of test cases and collection of results.
 */
public class TestCaseExecutor
{
	private static Logger logger = LoggerFactory.getLogger(TestCaseExecutor.class);
	private ExecutorService testCasesExecutorService = null;
	private TestCase parent = null;

	/**
	 * Constructor with TestCase object as the parameter.
	 * This TestCaseExecutor object will execute child TestCases and TestSteps.
	 * TestCases will be executed in parallel and TestSteps will be executed in sequence.
	 * 
	 * @param parent - parent TestCase object
	 */
	public TestCaseExecutor(TestCase parent)
	{
		super();
		this.parent = parent;
	}
	
	private ExecutorService getTestCasesExecutorService(int threadPoolSize)
	{
		if(this.testCasesExecutorService == null)
		{
			this.testCasesExecutorService = Executors.newFixedThreadPool(threadPoolSize);
		}
		return this.testCasesExecutorService;
	}
	
	/**
	 * Entry point method for the executor.
	 * 
	 * @param parentTestCasesDir
	 * @param testSuite
	 * @return - List of sub test casees
	 */
	public List<TestCase> execute(File parentTestCasesDir, TestSuite testSuite)
	{
		logger.info("Entering execute {}", parentTestCasesDir);
		if(!parentTestCasesDir.exists() || !parentTestCasesDir.isDirectory())
		{
			throw new RuntimeException("Path does not exist or is not a directory:"+parentTestCasesDir);
		}
		try
		{
	        File[] testCaseDirs = parentTestCasesDir.listFiles(new FileFilter() {
	
				@Override
				public boolean accept(File pathname)
				{
					return pathname.isDirectory() && pathname.exists();
				}});
        
	        List<File> testCaseDirsList = Arrays.asList(testCaseDirs);
	        return this.execute(testCaseDirsList, testSuite);
		}
		finally
		{
			logger.info("Exiting execute {}", parentTestCasesDir);
		}
	}

	public List<TestCase> execute(List<File> testCaseDirectories, TestSuite testSuite)
	{
		logger.info("Entering execute {}", testCaseDirectories);
		try
		{
	        List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>(testCaseDirectories.size());
	        List<TestCase> testCases = new ArrayList<TestCase>(testCaseDirectories.size());
	        for (int i = 0; i < testCaseDirectories.size(); i++) {
	            logger.info("Submitting :"+testCaseDirectories.get(i));
	            if(testCaseDirectories.get(i).isDirectory())
	            {
	            	TestCase tc = new TestCase(testCaseDirectories.get(i), testSuite, this.parent);
	            	Future<Boolean> future = this.getTestCasesExecutorService(testSuite.getTestCaseExecutorThreadPoolSize()).submit(tc);
	            	testCases.add(tc);
	            	futures.add(future);
	            }
	        }
	        
	        for (int i = 0; i < testCaseDirectories.size(); i++) {
	            logger.info("waiting :"+testCaseDirectories.get(i));
	            try
				{
					futures.get(i).get();
				} catch (InterruptedException e)
				{
					throw new RuntimeException(e);
				} catch (ExecutionException e)
				{
					throw new RuntimeException(e);
				}
	        }
	        
	        return testCases;
		}
		finally
		{
			logger.info("Exiting execute {}", testCaseDirectories);
		}
	}

}

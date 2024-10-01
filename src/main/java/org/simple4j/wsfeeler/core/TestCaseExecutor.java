package org.simple4j.wsfeeler.core;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
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
        
        
	        List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>(testCaseDirs.length);
	        List<TestCase> testCases = new ArrayList<TestCase>(testCaseDirs.length);
	        for (int i = 0; i < testCaseDirs.length; i++) {
	            logger.info("Submitting :"+testCaseDirs[i]);
	            if(testCaseDirs[i].isDirectory())
	            {
	            	TestCase tc = new TestCase(testCaseDirs[i], testSuite, this.parent);
	            	Future<Boolean> future = this.getTestCasesExecutorService(testSuite.getTestCaseExecutorThreadPoolSize()).submit(tc);
	            	testCases.add(tc);
	            	futures.add(future);
	            }
	        }
	        
	        for (int i = 0; i < testCaseDirs.length; i++) {
	            logger.info("waiting :"+testCaseDirs[i]);
	            futures.get(i).get();
	        }
	        
	        return testCases;
		} catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		} catch (ExecutionException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			logger.info("Exiting execute {}", parentTestCasesDir);
		}
	}

}

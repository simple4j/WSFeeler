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

public class TestCaseExecutor
{
	private static Logger logger = LoggerFactory.getLogger(TestCaseExecutor.class);
	private ExecutorService testCasesExecutorService = Executors.newFixedThreadPool(10);

	public List<TestCase> execute(File parentTestCasesDir, TestSuite testSuite)
	{
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
            	TestCase tc = new TestCase(testCaseDirs[i], testSuite);
            	Future<Boolean> future = this.testCasesExecutorService.submit(tc);
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
			
		}
	}

}

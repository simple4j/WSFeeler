package org.simple4j.wsfeeler.model;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelayStep extends TestStep
{

	private static Logger logger = LoggerFactory.getLogger(DelayStep.class);

	public DelayStep(Map<String, Object> testStepInputVariables, File testStepInputFile, TestCase parent,
			TestSuite testSuite)
	{
		super(testStepInputVariables, testStepInputFile, parent, testSuite);
	}

	@Override
	public boolean execute()
	{
		logger.info("Entering execute:{}", this.name);
		try
		{
	    	Object delayMilliSecondsObj = this.testStepVariables.get("delayMilliSeconds");
	    	if(delayMilliSecondsObj != null)
	    	{
	    		long delayMilliSeconds = Long.parseLong((String) delayMilliSecondsObj);
	    		Thread.sleep(delayMilliSeconds);
	    	}
			
		} catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			logger.info("Exiting execute:{}", this.name);
		}
		this.setSuccess(true);
		return true;
	}

}

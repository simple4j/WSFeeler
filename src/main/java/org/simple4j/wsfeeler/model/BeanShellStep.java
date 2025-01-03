package org.simple4j.wsfeeler.model;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bsh.EvalError;
import bsh.Interpreter;

public class BeanShellStep extends TestStep
{

	private static Logger logger = LoggerFactory.getLogger(BeanShellStep.class);

	public BeanShellStep(Map<String, Object> testStepInputVariables, File testStepInputFile, TestCase parent,
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
	    	Object assertionExpressionnObj = this.testStepVariables.get("ASSERT");
	    	if(assertionExpressionnObj != null)
	    	{
	    		String assertionExpression = (String) assertionExpressionnObj;
		        Interpreter bsh = new Interpreter();
				bsh.setOut(System.out);
				bsh.setErr(System.err);
				bsh.set("testApplicationContext", this.testSuite.getTestApplicationContext());
				
	    		logger.info("Setting step variables");
		        for (Entry<String, Object> entry : this.testStepVariables.entrySet())
		        {
		        	try
					{
		        		logger.debug("Setting {} : {}",entry.getKey(), entry.getValue());
						bsh.set(entry.getKey(), entry.getValue());
						
					} catch (EvalError e)
					{
						logger.error("Error while setting variable for BeanShell step: {} key: {} vaue: {}", this.name, entry.getKey(), entry.getValue(), e);
					}
		        }
		
				Object result = bsh.eval(assertionExpression);
				logger.info("result={}" , result);
	            if(result instanceof Boolean)
	            {
	                if(!((Boolean)result))
	                {
	                    logger.info("FAILURE: Teststep "+ this.name +" for assertion "+assertionExpression);
	                    this.setSuccess(false);
	                    return false;
	                }
	            }
	            else
	            {
	                logger.info("FAILURE: Assertion expression "+assertionExpression+" return non-boolean value "+ result + " of type "+result.getClass());
	                this.setSuccess(false);
	                return false;
	            }
	    	}
			
		} catch (EvalError e)
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

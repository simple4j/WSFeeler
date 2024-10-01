package org.simple4j.wsfeeler.model;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.simple4j.wsclient.caller.Caller;
import org.simple4j.wsclient.caller.CallerFactory;
import org.simple4j.wsclient.exception.SystemException;
import org.simple4j.wsclient.util.CollectionsPathRetreiver;
import org.simple4j.wsfeeler.core.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import bsh.EvalError;
import bsh.Interpreter;

public class WSTestStep extends TestStep
{

	private static Logger logger = LoggerFactory.getLogger(WSTestStep.class);

	public WSTestStep(Map<String, Object> testStepInputVariables, File testStepInputFile, TestCase parent, TestSuite testSuite)
	{
		super(testStepInputVariables, testStepInputFile, parent, testSuite);
	}

	@Override
	public boolean execute()
	{
		logger.info("Entering execute:{}", this.name);
		try
		{
			Caller caller = null;
	    	Object callerBeanIdObj = this.testStepVariables.get("callerBeanId");
	    	if(callerBeanIdObj != null)
	    	{
	    		String callerBeanId = (String) callerBeanIdObj;
	            caller = getCaller(callerBeanId);
	    	}
	    	else
	    	{
	        	Object callerFactoryBeanIdObj = this.testStepVariables.get("callerFactoryBeanId");
	        	if(callerFactoryBeanIdObj != null)
	        	{
	        		String callerFactoryBeanId = (String) callerFactoryBeanIdObj;
	                caller = getCallerFactory(callerFactoryBeanId).getCaller();
	        	}
	        	else
	        	{
	        		throw new SystemException("callerBeanId-callerFactoryBeanId-missing", "Both callerBeanId and callerFactoryBeanId is missing in the file:"+this.testStepInputFile);
	        	}
	    	}
	        logger.info("Calling service");
	        Map<String, Object> response = caller.call(this.testStepVariables);
	        logger.info("response from service call:"+response);
	
	        Interpreter bsh = new Interpreter();
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
	
    		logger.info("Setting step response variables");
	        for (Entry<String, Object> entry : response.entrySet())
	        {
	        	this.testStepVariables.put(entry.getKey(), entry.getValue());
	        	try
				{
	        		logger.debug("Setting {} : {}",entry.getKey(), entry.getValue());
					bsh.set(entry.getKey(), entry.getValue());
				} catch (EvalError e)
				{
					logger.error("Error while setting output variable for BeanShell step: {} key: {} vaue: {}", this.name, entry.getKey(), entry.getValue(), e);
				}
	        }
	
	        File outputPropertiesFile = new File(this.testStepInputFile.getParentFile(),this.testStepInputFile.getName().replace("input.properties", "output.properties"));
	        String assertionExpression = null;
	        Map<String, Object> testStepOutputVariables = null;
	        if(outputPropertiesFile.exists())
	        {
	            testStepOutputVariables = ConfigLoader.loadVariables(outputPropertiesFile, this.parent);
	            logger.info("loaded properties:{}", testStepOutputVariables);
	            if(testStepOutputVariables.containsKey("ASSERT"))
	            {
		            assertionExpression = ""+testStepOutputVariables.get("ASSERT");
		            testStepOutputVariables.remove("ASSERT");
	            }
	        }
	        if(testStepOutputVariables != null)
	        {
	            for (Entry<String, Object> entry : testStepOutputVariables.entrySet()) {
	            	CollectionsPathRetreiver cpr = new CollectionsPathRetreiver();
	                List nestedProperty = cpr.getNestedProperty(response, ""+entry.getValue());
	                Object value = nestedProperty;
	                if(nestedProperty.size() == 0)
	                {
	                	continue;
	                }
	                if(nestedProperty.size() == 1)
	                {
	                	value = nestedProperty.get(0);
	                }
					try
					{
	                	logger.info("setting key to intrepreter: {} value: {}", entry.getKey(), value);
						bsh.set(entry.getKey(), ""+value);
					} catch (EvalError e)
	        		{
						logger.error("Error while setting variable for BeanShell step: {} key: {} vaue: {}", this.name, entry.getKey(), value, e);
			            this.setSuccess(false);
			            return false;
					}
	                this.testStepVariables.put(entry.getKey(), ""+value);
	            }
	        }
	        if(assertionExpression != null)
	        {
	            Object stepResult;
				try
				{
					stepResult = bsh.eval(assertionExpression);
				} catch (EvalError e)
	    		{
					logger.error("Error while evaluating ASSERT: {} in step: {}", this.name, assertionExpression, e);
		            this.setSuccess(false);
		            return false;
				}
	            if(stepResult instanceof Boolean)
	            {
	                if(!((Boolean)stepResult))
	                {
	                    logger.info("FAILURE: Teststep "+ this.name +" for assertion "+assertionExpression);
	                    logger.info("Test step variables are "+testStepOutputVariables);
	                    this.setSuccess(false);
	                    return false;
	                }
	            }
	            else
	            {
	                logger.info("FAILURE: Assertion expression "+assertionExpression+" return non-boolean value "+ stepResult + " of type "+stepResult.getClass());
	                this.setSuccess(false);
	                return false;
	            }
	        }
	        this.setSuccess(true);
	        return true;
		} catch (IOException e)
		{
			logger.error("Error while executing step {}", this.name, e);
            this.setSuccess(false);
            return false;
		}
		finally
		{
			logger.info("Exiting execute:{}", this.name);
		}
	}

	private Caller getCaller(String callerBeanId)
	{
		ApplicationContext ac = this.testSuite.getMainApplicationContext();
		return ac.getBean(callerBeanId, Caller.class);
	}

	private CallerFactory getCallerFactory(String callerFactoryBeanId)
	{
		ApplicationContext ac = this.testSuite.getMainApplicationContext();
		return ac.getBean(callerFactoryBeanId, CallerFactory.class);
	}

}

package org.simple4j.wsfeeler.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.simple4j.wsfeeler.model.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * This is a utility class with methods to load test step and test case properties, deference variables.
 */
public class ConfigLoader
{
	private static Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

	/**
	 * Used to load /tsvariables.properties, tcvariables.properties under test case directories
	 * 
	 * @param variablesStream - input stream to load the variables in properties format
	 * @param globalVariables2 - Existing variables to evaluate bean shell expression 
	 * @return - returns Map of loaded variables
	 * @throws IOException - Any IOException from the system
	 * @throws EvalError - Any BeanShell evaluation errors
	 */
	public static Map<String, Object> loadVariables(InputStream variablesStream,
			Map<String, Object> globalVariables2, String prefix) throws IOException, EvalError
	{
		logger.info("Inside loadVariables: {}, {}", globalVariables2, prefix);
		try
		{
			if (prefix == null)
				prefix = "";
			Properties loadedVariables = new Properties();
			//TODO : Need to cache loaded files and use it to avoid repeated IO
			loadedVariables.load(variablesStream);
			Map<String, Object> ret = new HashMap<String, Object>();
			if (globalVariables2 != null)
			{
				ret.putAll(globalVariables2);
			}

			Interpreter bsh = new Interpreter();
			bsh.setOut(System.out);
			bsh.setErr(System.err);
			if (globalVariables2 != null)
			{
				for (Entry<String, Object> entry : globalVariables2.entrySet())
				{
					bsh.set(entry.getKey(), entry.getValue());
				}
			}
			ArrayList sortedKeys = new ArrayList(loadedVariables.keySet());
			Collections.sort(sortedKeys);

			for (Iterator iterator = sortedKeys.iterator(); iterator.hasNext();)
			{
				String key = (String) iterator.next();

				logger.debug("processing key:" + key);

				Object eval;
				try
				{
					eval = bsh.eval("" + loadedVariables.getProperty(key));
				} catch (EvalError e)
				{
					if (e.getMessage().contains("number too big for integer type"))
					{
						eval = bsh.eval("\"" + loadedVariables.getProperty(key) + "\"");
					} else
						throw e;
				}
				logger.debug("Beanshell evaluated value:" + eval);
				if (eval != null)
				{
					ret.put(prefix + key, eval);
					bsh.set(prefix + key, eval);
					logger.debug("set evaluated:" + key + ":" + eval);
				} else
				{
					ret.put(prefix + key, loadedVariables.getProperty(key));
					bsh.set(prefix + key, loadedVariables.getProperty(key));
					logger.debug("set unevaluated:" + key + ":" + loadedVariables.getProperty(key));
				}

			}

			return ret;
		} finally
		{
		}
	}

	/**
	 * Loads step level properties without BeanShell processing
	 * @param testStepFile - this can be test step input or output properties file
	 * @param testCase - test case object under which the test step is defined
	 * @return Map of loaded properties
	 * @throws IOException - any IOException from the system
	 */
	public static Map<String, Object> loadVariables(File testStepFile, TestCase testCase) throws IOException
	{
		logger.info("Inside loadVariables: {}, {}", testStepFile, testCase);
		InputStream variablesStream = null;
		try
		{
			variablesStream = new FileInputStream(testStepFile);
			Properties loadedVariables = new Properties();
			//TODO : Need to cache loaded files and use it to avoid repeated IO
			loadedVariables.load(variablesStream);
			logger.info("loadedVariables:{}", loadedVariables);
			Map<String, Object> ret = new HashMap<String, Object>();

			ArrayList sortedKeys = new ArrayList(loadedVariables.keySet());
			Collections.sort(sortedKeys);

			for (Iterator iterator = sortedKeys.iterator(); iterator.hasNext();)
			{
				String key = (String) iterator.next();

				logger.info("processing key:" + key);

				Object eval;
					eval = testCase.getProperty("" + loadedVariables.getProperty(key));
				logger.info("Dereferenced test case property value:" + eval);
				if(eval == null)
				{
					eval=loadedVariables.getProperty(key);
				}
				ret.put("" + key, eval);

			}

			return ret;
		} finally
		{
			if (variablesStream != null)
				variablesStream.close();
		}
	}

}

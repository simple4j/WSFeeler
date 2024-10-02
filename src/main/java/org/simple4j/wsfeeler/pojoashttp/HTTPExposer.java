package org.simple4j.wsfeeler.pojoashttp;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import org.apache.commons.beanutils.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import spark.Spark;

/**
 * This class can be used to expose any method in any bean in a Spring ApplicationContext as a HTTP service
 */
public class HTTPExposer
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .serializationInclusion(Include.NON_NULL)
            .build();

    /**
     * org.springframework.context.ApplicationContext instance from which the bean methods are exposed
     */
    private ApplicationContext context;
    
    private int listenerPortNumber = -1;
    
    private int listenerThreadMax = 100;
    
    private int listenerThreadMin = 1;

    private int listenerIdleTimeoutMillis = 60000;
    
    private String urlBase = "/pojoashttp";
    
    /**
     * Constructor with org.springframework.context.ApplicationContext instance as parameter
     * @param context - org.springframework.context.ApplicationContext instance
     */
    public HTTPExposer(ApplicationContext context)
    {
    	super();
    	this.context = context;
    }
    
    /**
     * This method will turn on the listener to expose bean methods as web service
     */
    public void expose()
    {
    	if(listenerPortNumber > 0)
    	{
	        Spark.port(listenerPortNumber);
	        Spark.threadPool(listenerThreadMax, listenerThreadMin, listenerIdleTimeoutMillis);
    	}
        Spark.post(this.getUrlBase()+"/request.json", (request, response) -> 
        {
            String body = request.body();
            RequestJSON requestJSON = null;
            try
            {
                requestJSON = OBJECT_MAPPER.readValue(body, RequestJSON.class);
            }
            catch(Exception e)
            {
                throw new RuntimeException("Error parsing request body <" + body +">",  e);
            }
            
            LOGGER.info("parsed JSON");
            Object bean = context.getBean(requestJSON.getBeanId());
            LOGGER.info("got bean from Spring {}",bean);

            Class[] parameterTypes = null;
            Object[] parameterValues = null;
            MethodParameterJSON[] methodParameters = requestJSON.getMethodParameters();
            LOGGER.info("got method parameter");
            if(methodParameters != null && methodParameters.length > 0)
            {
                LOGGER.info("method parameter not null");
                parameterTypes = new Class[methodParameters.length];
                parameterValues = new Object[methodParameters.length];

                for (int i = 0; i < methodParameters.length; i++) {
                    LOGGER.info("creating instance for param:{}", methodParameters[i].getClassName());
                    LOGGER.info(":{}",methodParameters[i].getValue());
                    LOGGER.info(":{}",methodParameters[i].getValues());
                    //To call methods with primitive types, the client need to use TYPE field.
                    //Here is an example
                    //{"beanId":"someBean","methodName":"someMethod", "methodParameters" : [{"className":"java.lang.String","value":"someStringParam"},{"className":"java.lang.Integer.TYPE","value":"100"}]}
                    if(methodParameters[i].getClassName().endsWith(".TYPE"))
                    {
                        Class primitiveType = Class.forName(methodParameters[i].getClassName().replaceFirst("(\\.TYPE)$", ""));
                        parameterTypes[i] = (Class) primitiveType.getField("TYPE").get(null);
                    }
                    else
                    {
                        parameterTypes[i] = Class.forName(methodParameters[i].getClassName());
                    }
                    LOGGER.info("loaded param type {}", parameterTypes[i]);
                    if(methodParameters[i].getValue() != null)
                    {
                        if(parameterTypes[i].equals(methodParameters[i].getValue().getClass()))
                        {
                            LOGGER.info("parametertypes same as value type");
                            parameterValues[i] = methodParameters[i].getValue();
                        }
                        else
                        {
                            LOGGER.info("converting parametertype");
                            parameterValues[i] = ConvertUtils.convert(methodParameters[i].getValue(), parameterTypes[i]);
                            LOGGER.info("converting parametertype");
                        }
                    }
                    else if(methodParameters[i].getValues() != null)
                    {
                        parameterValues[i] = ConvertUtils.convert(methodParameters[i].getValues(), parameterTypes[i]);
                        parameterTypes[i] = parameterValues[i].getClass();
                    }
                    else
                    {
                        String parameterStringJSON = OBJECT_MAPPER.writeValueAsString(methodParameters[i].getValueJSON());
                        parameterValues[i] = OBJECT_MAPPER.readValue(parameterStringJSON, parameterTypes[i]);
                    }
                }
            }

            LOGGER.info("getting method instance");
            Method method = bean.getClass().getMethod(requestJSON.getMethodName(), parameterTypes);
            LOGGER.info("got it:{}", method);
            Object responseObj = method.invoke(bean, parameterValues);

            LOGGER.info("invoked it:{}",responseObj);

            String ret = OBJECT_MAPPER.writeValueAsString(responseObj);
            return "{\"returnValue\":"+ret+"}";
        });
    }

    /**
     * Port number to start the listener at
     */
	public int getListenerPortNumber()
	{
		return listenerPortNumber;
	}

	public void setListenerPortNumber(int listenerPortNumber)
	{
		this.listenerPortNumber = listenerPortNumber;
	}

    /**
     * Max thread count for the listener to serve requests
     */
	public int getListenerThreadMax()
	{
		return listenerThreadMax;
	}

	public void setListenerThreadMax(int listenerThreadMax)
	{
		this.listenerThreadMax = listenerThreadMax;
	}

    /**
     * Min thread count for the listener to serve requests
     */
	public int getListenerThreadMin()
	{
		return listenerThreadMin;
	}

	public void setListenerThreadMin(int listenerThreadMin)
	{
		this.listenerThreadMin = listenerThreadMin;
	}

    /**
     * Idle timeout in milli seconds for the listener thread to be destroyed to reach min thread count
     */
	public int getListenerIdleTimeoutMillis()
	{
		return listenerIdleTimeoutMillis;
	}

	public void setListenerIdleTimeoutMillis(int listenerIdleTimeoutMillis)
	{
		this.listenerIdleTimeoutMillis = listenerIdleTimeoutMillis;
	}

    /**
     * URL base path for the listener to expose the service
     */
	public String getUrlBase()
	{
		return urlBase;
	}

	public void setUrlBase(String urlBase)
	{
		this.urlBase = urlBase;
	}
    
}

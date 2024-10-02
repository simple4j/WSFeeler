package org.simple4j.wsfeeler.pojoashttp;

import java.util.Arrays;

/**
 * HTTPExposer web service request body structure
 */
public class RequestJSON {

	String beanId = null;

	String methodName = null;

	MethodParameterJSON[] methodParameters = null;

	/**
	 * Bean id of the bean in the application context whose method will be invoked
	 */
	public String getBeanId() {
		return beanId;
	}

	public void setBeanId(String beanId) {
		this.beanId = beanId;
	}

	/**
	 * Name of the method which will be invoked
	 */
	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * Method parameters as array of MethodParameterJSON objects
	 */
	public MethodParameterJSON[] getMethodParameters() {
		return methodParameters;
	}

	public void setMethodParameters(MethodParameterJSON[] methodParameters) {
		this.methodParameters = methodParameters;
	}

	@Override
	public String toString() {
		return "RequestJSON [beanId=" + beanId + ", methodName=" + methodName
				+ ", methodParameters=" + Arrays.toString(methodParameters)
				+ "]";
	}


}

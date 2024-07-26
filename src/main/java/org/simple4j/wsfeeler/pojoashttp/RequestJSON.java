package org.simple4j.wsfeeler.pojoashttp;

import java.util.Arrays;

public class RequestJSON {

	String beanId = null;

	String methodName = null;

	MethodParameterJSON[] methodParameters = null;

	public String getBeanId() {
		return beanId;
	}

	public void setBeanId(String beanId) {
		this.beanId = beanId;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

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

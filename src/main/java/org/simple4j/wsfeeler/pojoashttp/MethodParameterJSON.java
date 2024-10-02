package org.simple4j.wsfeeler.pojoashttp;

import java.util.Arrays;
import java.util.Map;

/**
 * HTTPExposer web service request body method parameter structure
 */
public class MethodParameterJSON {

	String className = null;

	String value = null;

	String values[] = null;

	Map valueJSON = null;

	Map valueJSONx[] = null;

	/**
	 * Fully qualified class name of the parameter type
	 * To call methods with primitive types, the client need to use TYPE field.
	 * 
	 * Here is an example
	 * {"beanId":"someBean","methodName":"someMethod", "methodParameters" : [{"className":"java.lang.String","value":"someStringParam"},{"className":"java.lang.Integer.TYPE","value":"100"}]}
	 */
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * value for the parameter when its not an array and not custom class
	 */
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * value for the parameter when its an array and not custom class
	 */
	public String[] getValues() {
		return values;
	}

	public void setValues(String[] values) {
		this.values = values;
	}

	/**
	 * value for the parameter when its not an array and is a custom class
	 */
	public Map getValueJSON() {
		return valueJSON;
	}

	public void setValueJSON(Map valueJSON) {
		this.valueJSON = valueJSON;
	}

	/**
	 * value for the parameter when its an array and is a custom class
	 */
	public Map[] getValueJSONx()
	{
		return valueJSONx;
	}

	public void setValueJSONx(Map[] valueJSONx)
	{
		this.valueJSONx = valueJSONx;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString()).append(" [className=").append(className).append(", value=").append(value)
				.append(", values=").append(Arrays.toString(values)).append(", valueJSON=").append(valueJSON)
				.append(", valueJSONx=").append(Arrays.toString(valueJSONx)).append("]");
		return builder.toString();
	}


}

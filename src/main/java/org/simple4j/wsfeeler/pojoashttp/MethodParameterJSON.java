package org.simple4j.wsfeeler.pojoashttp;

import java.util.Arrays;
import java.util.Map;

public class MethodParameterJSON {

	String className = null;

	String value = null;

	String values[] = null;

	Map valueJSON = null;

	Map valueJSONx[] = null;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String[] getValues() {
		return values;
	}

	public void setValues(String[] values) {
		this.values = values;
	}

	public Map getValueJSON() {
		return valueJSON;
	}

	public void setValueJSON(Map valueJSON) {
		this.valueJSON = valueJSON;
	}

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

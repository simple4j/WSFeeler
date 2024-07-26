package org.simple4j.wsfeeler.pojoashttp;

import java.util.Arrays;
import java.util.Map;

public class MethodParameterJSON {

	String className = null;

	String value = null;

	String values[] = null;

	Map valueJSON = null;

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

	@Override
	public String toString() {
		return "MethodParameterJSON [className=" + className + ", value="
				+ value + ", values=" + Arrays.toString(values)
				+ ", valueJSON=" + valueJSON + "]";
	}


}

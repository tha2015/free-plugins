package org.freejava.controller;

import java.util.Map;

import javax.servlet.ServletRequest;

import org.apache.commons.lang.StringUtils;

public class ControllerUtils {

	public static void addStringParams(ServletRequest request, String paramName, Map<String, Object[]> criteriaValues) {
		String[] values = request.getParameterValues(paramName);
		if (values != null) {
			Object[] result = new Object[values.length];
			for (int i = 0; i < values.length; i++) result[i] = values[i];
			criteriaValues.put(paramName, result);
		}
	}
	public static void addLongParams(ServletRequest request, String paramName, Map<String, Object[]> criteriaValues) {
		String[] values = request.getParameterValues(paramName);
		if (values != null) {
			Object[] result = new Object[values.length];
			for (int i = 0; i < values.length; i++) result[i] = Long.parseLong(values[i]);
			criteriaValues.put(paramName, result);
		}
	}
	public static int getIntParameter(ServletRequest request, String paramName, int defaultValue) {
		int value = defaultValue;
		String paramValue = request.getParameter(paramName);
		if (StringUtils.isNotBlank(paramValue))
			value = Integer.parseInt(paramValue);
		return value;
	}
}

package org.freejava.util;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodLoggingAdvice implements MethodInterceptor {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MethodLoggingAdvice.class);

	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object result;
		Method method;
		Class<?> clazz;
		String methodName;
		try {
			if (LOGGER.isDebugEnabled()) {
				method = invocation.getMethod();
				clazz = invocation.getThis().getClass();
				methodName = clazz.getSimpleName() + "::" + method.getName();
				String arguments;
				arguments = convertToString(invocation.getArguments());
				LOGGER.debug("BEGIN: " + methodName + arguments);
				result = invocation.proceed();
				if (method.getReturnType() == Void.TYPE) {
					LOGGER.debug("END: " + methodName + "(..)");
				} else {
					LOGGER.debug("END: " + methodName + "(..)=" + result);
				}
			} else {
				result = invocation.proceed();
			}
		} catch (Exception e) {
			method = invocation.getMethod();
			clazz = invocation.getThis().getClass();
			methodName = clazz.getSimpleName() + "::" + method.getName();
			if (e.getClass().getPackage().getName().startsWith("com.calgoo")) {
				LOGGER.warn("END WITH EXCEPTION: " + methodName, e);
			} else {
				LOGGER.error("END WITH EXCEPTION: " + methodName, e);
			}
			throw e;
		}
		return result;
	}

	private static String convertToString(Object[] arguments) {
		StringBuffer result;
		if (arguments.length == 0) {
			result = new StringBuffer(4);
			result.append("(void)");
		} else {
			result = null;
			for (Object object : arguments) {
				if (result == null) {
					result = new StringBuffer(100);
					result.append('(').append(object);
				} else {
					result.append(',').append(object);
				}
			}
			result.append(')');
		}
		return result.toString();
	}

}

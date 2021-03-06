/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.sling.metrics.api;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Capture the return value and create a metric name from some part of it, either using toString or a method call.
 */
public class ReturnCapture {
    
    
    private static LogServiceHolder logServiceHolder;

    public static <T> void markCaptureUsingHelper(@Nullable T o, @Nonnull String name, @Nonnull String helperName) {
        logServiceHolder.debug("Mark with method name "+o);
        MetricsUtil.mark(getMetricNameUsingHelper(o,name, helperName));
    }
    
    public static <T> void markCapture(@Nullable T o, @Nonnull String name, @Nonnull String methodName) {
        logServiceHolder.debug("Mark with method name ",o);
        MetricsUtil.mark(getMetricName(o,name, methodName));
    }

    public static <T> void markCapture(@Nullable T o, @Nonnull String name) {
        logServiceHolder.debug("Marking ",o);
        MetricsUtil.mark(getMetricName(o,name));
    }

    public static <T> void countCaptureUsingHelper(@Nullable T o, @Nonnull String name, @Nonnull String helperClass) {
        logServiceHolder.debug("Count with method name ",o);
        MetricsUtil.count(getMetricNameUsingHelper(o, name, helperClass));
    }

    public static <T> void countCapture(@Nullable T o, @Nonnull String name, @Nonnull String methodName) {
        logServiceHolder.debug("Count with method name ",o);
        MetricsUtil.count(getMetricName(o, name, methodName));
    }

    public static <T> void countCapture(@Nullable T o, @Nonnull String name) {
        
        logServiceHolder.debug("Count ",o);
        MetricsUtil.count(getMetricName(o, name));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Nonnull
    private static <T> String getMetricNameUsingHelper(@Nullable T o, @Nonnull String name, @Nonnull String helperClass) {
        if (o != null) {
            try {
                Class<?> c = ReturnCapture.class.getClassLoader().loadClass(helperClass);
                Object helper = c.newInstance();
                if (helper instanceof MetricsNameHelper) {
                    return name+((MetricsNameHelper) helper).getName(o);
                } else {
                    return name+"_error_invalid_helper";
                }
            } catch ( Exception e) {
                logServiceHolder.warn("Unable to get metric name helper class ",helperClass," with object ",o.getClass()," cause:",e.getMessage());
                return name+"_error_invalid_helper";
            }
        }
        return name+"_nullreturn";
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    private static <T> String getMetricName(@Nullable T o, @Nonnull String name, @Nonnull String methodName) {
        if (o != null) {
            try {
                Class<T> c = ((Class<T>) o.getClass());
                Method m = c.getDeclaredMethod(methodName); 
                if (!m.isAccessible()) {
                    m.setAccessible(true);
                }
                return name+String.valueOf(m.invoke(o));
            } catch (Exception e) {
                logServiceHolder.warn( "Unable to get metric name from method ",methodName," on object ",o.getClass()," cause:",e.getMessage());
                return name+"_default";
            }
        }
        return name+"_nullreturn";
    }

    @Nonnull
    private static <T> String getMetricName(@Nullable T o, @Nonnull String name) {
        if (o != null) {
                return name+String.valueOf(o);
        }
        return name+"_nullreturn";
    }

    public static void setLogServiceHolder(LogServiceHolder logServiceHolder) {
        ReturnCapture.logServiceHolder = logServiceHolder;
    }

}

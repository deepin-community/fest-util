/*
 * Created on Dec 13, 2008
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright @2008-2012 the original author or authors.
 */
package org.fest.util;

import static org.fest.util.Lists.newArrayList;

import java.util.*;

/**
 * Utility methods related to <code>{@link Throwable}</code>s.
 *
 * @author Alex Ruiz
 */
public final class Throwables {
  /**
   * Appends the stack trace of the current thread to the one in the given <code>{@link Throwable}</code>.
   *
   * @param t the given {@code Throwable}.
   * @param methodToStartFrom the name of the method used as the starting point of the current thread's stack trace.
   */
  public static void appendStackTraceInCurentThreadToThrowable(Throwable t, String methodToStartFrom) {
    List<StackTraceElement> stackTrace = newArrayList(t.getStackTrace());
    stackTrace.addAll(stackTraceInCurrentThread(methodToStartFrom));
    t.setStackTrace(stackTrace.toArray(new StackTraceElement[stackTrace.size()]));
  }

  private static List<StackTraceElement> stackTraceInCurrentThread(String methodToStartFrom) {
    List<StackTraceElement> filtered = stackTraceInCurrentThread();
    List<StackTraceElement> toRemove = new ArrayList<StackTraceElement>();
    for (StackTraceElement e : filtered) {
      if (methodToStartFrom.equals(e.getMethodName())) {
        break;
      }
      toRemove.add(e);
    }
    filtered.removeAll(toRemove);
    return filtered;
  }

  private static List<StackTraceElement> stackTraceInCurrentThread() {
    return newArrayList(Thread.currentThread().getStackTrace());
  }

  /**
   * Removes the FEST-related elements from the <code>{@link Throwable}</code> stack trace that have little value for
   * end user. Therefore, instead of seeing this:
   *
   * <pre>
   * org.junit.ComparisonFailure: expected:<'[Ronaldo]'> but was:<'[Messi]'>
   *   at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
   *   at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:39)
   *   at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:27)
   *   at java.lang.reflect.Constructor.newInstance(Constructor.java:501)
   *   at org.fest.assertions.error.ConstructorInvoker.newInstance(ConstructorInvoker.java:34)
   *   at org.fest.assertions.error.ShouldBeEqual.newComparisonFailure(ShouldBeEqual.java:111)
   *   at org.fest.assertions.error.ShouldBeEqual.comparisonFailure(ShouldBeEqual.java:103)
   *   at org.fest.assertions.error.ShouldBeEqual.newAssertionError(ShouldBeEqual.java:81)
   *   at org.fest.assertions.internal.Failures.failure(Failures.java:76)
   *   at org.fest.assertions.internal.Objects.assertEqual(Objects.java:116)
   *   at org.fest.assertions.api.AbstractAssert.isEqualTo(AbstractAssert.java:74)
   *   at examples.StackTraceFilterExample.main(StackTraceFilterExample.java:13)
   * </pre>
   *
   * We get this:
   *
   * <pre>
   * org.junit.ComparisonFailure: expected:<'[Ronaldo]'> but was:<'[Messi]'>
   *   at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
   *   at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:39)
   *   at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:27)
   *   at examples.StackTraceFilterExample.main(StackTraceFilterExample.java:20)
   * </pre>
   * @param throwable the {@code Throwable} to filter stack trace.
   */
  public static void removeFestRelatedElementsFromStackTrace(Throwable throwable) {
    List<StackTraceElement> filtered = newArrayList(throwable.getStackTrace());
    StackTraceElement previous = null;
    for (StackTraceElement element : throwable.getStackTrace()) {
      if (element.getClassName().contains("org.fest")) {
        filtered.remove(element);
        // Handle the case when FEST builds a ComparisonFailure by reflection (see ShouldBeEqual.newAssertionError
        // method), the stack trace looks like:
        //
        // java.lang.reflect.Constructor.newInstance(Constructor.java:501),
        // org.fest.assertions.error.ConstructorInvoker.newInstance(ConstructorInvoker.java:34),
        //
        // We want to remove java.lang.reflect.Constructor.newInstance element because it is related to FEST.
        if (previous != null && previous.getClassName().equals("java.lang.reflect.Constructor")
            && element.getClassName().contains("org.fest.assertions.error.ConstructorInvoker")) {
          filtered.remove(previous);
        }
      }
      previous = element;
    }
    StackTraceElement[] newStackTrace = filtered.toArray(new StackTraceElement[filtered.size()]);
    throwable.setStackTrace(newStackTrace);
  }

  private Throwables() {}
}

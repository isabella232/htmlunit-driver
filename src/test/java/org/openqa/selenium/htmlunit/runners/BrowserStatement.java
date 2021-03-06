// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.htmlunit.runners;

import java.util.Arrays;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.openqa.selenium.htmlunit.BrowserRunner;
import org.openqa.selenium.htmlunit.BrowserRunner.Alerts;
import org.openqa.selenium.htmlunit.BrowserRunner.AlertsStandards;

import com.gargoylesoftware.htmlunit.BrowserVersion;

/**
 * The Browser Statement.
 */
class BrowserStatement extends Statement {

    private Statement next_;
    private final boolean notYetImplemented_;
    private final FrameworkMethod method_;
    private final BrowserVersion browserVersion_;
    private final int tries_;

    BrowserStatement(final Statement next, final FrameworkMethod method, final boolean realBrowser,
            final boolean notYetImplemented, final int tries, final BrowserVersion browserVersion) {
        next_ = next;
        method_ = method;
        notYetImplemented_ = notYetImplemented;
        tries_ = tries;
        browserVersion_ = browserVersion;
    }

    @Override
    public void evaluate() throws Throwable {
        assertAlerts();
        for (int i = 0; i < tries_; i++) {
            try {
                evaluateSolo();
                break;
            }
            catch (final Throwable t) {
                if (notYetImplemented_) {
                    throw t;
                }
                if (BrowserVersionClassRunner.maven_) {
                    System.out.println("Failed test "
                            + method_.getDeclaringClass().getName() + '.' + method_.getName()
                            + (tries_ != 1 ? " #" + (i + 1) : ""));
                }
                if (i == tries_ - 1) {
                    throw t;
                }
            }
        }
    }

    private void assertAlerts() {
        final Alerts alerts = method_.getAnnotation(Alerts.class);
        if (alerts != null) {
            if (!BrowserVersionClassRunner.isDefined(alerts.value())) {
                assertNotEquals(alerts.IE(), alerts.DEFAULT());
                assertNotEquals(alerts.CHROME(), alerts.DEFAULT());
                assertNotEquals(alerts.FF(), alerts.DEFAULT());
                assertNotEquals(alerts.FF(), alerts.CHROME());
                assertNotEquals(alerts.IE(), alerts.CHROME());
                assertNotEquals(alerts.IE(), alerts.FF());
                assertNotEquals(alerts.FF78(), alerts.FF());
            }
        }
        final AlertsStandards alerts2 = method_.getAnnotation(AlertsStandards.class);
        if (alerts2 != null) {
            if (!BrowserVersionClassRunner.isDefined(alerts2.value())) {
                assertNotEquals(alerts2.IE(), alerts2.DEFAULT());
                assertNotEquals(alerts2.CHROME(), alerts2.DEFAULT());
                assertNotEquals(alerts2.FF(), alerts2.DEFAULT());
                assertNotEquals(alerts2.FF(), alerts2.CHROME());
                assertNotEquals(alerts2.IE(), alerts2.CHROME());
                assertNotEquals(alerts2.IE(), alerts2.FF());
                assertNotEquals(alerts2.FF78(), alerts2.FF());
            }
        }
    }

    private void assertNotEquals(final String[] value1, final String[] value2) {
        if (value1.length != 0 && !BrowserRunner.EMPTY_DEFAULT.equals(value1[0])
                && value1.length == value2.length
                && Arrays.asList(value1).toString().equals(Arrays.asList(value2).toString())) {
            throw new AssertionError("Redundant alert in "
                    + method_.getDeclaringClass().getSimpleName() + '.' + method_.getName() + "()");
        }
    }

    public void evaluateSolo() throws Throwable {
        Exception toBeThrown = null;
        try {
            next_.evaluate();
            if (notYetImplemented_) {
                final String errorMessage;
                if (browserVersion_.getNickname() == null) {
                    errorMessage = method_.getName() + " is marked as not implemented but already works";
                }
                else {
                    errorMessage = method_.getName() + " is marked as not implemented with "
                        + browserVersion_.getNickname() + " but already works";
                }
                toBeThrown = new Exception(errorMessage);
            }
        }
        catch (final Throwable e) {
            if (!notYetImplemented_) {
                throw e;
            }
        }
        if (toBeThrown != null) {
            throw toBeThrown;
        }
    }
}

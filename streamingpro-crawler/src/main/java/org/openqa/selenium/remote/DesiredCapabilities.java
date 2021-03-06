/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

package org.openqa.selenium.remote;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Platform;

import java.util.Map;
import java.util.logging.Logger;

import static org.openqa.selenium.remote.CapabilityType.*;

public class DesiredCapabilities extends MutableCapabilities {

  private static final Logger LOG = Logger.getLogger(Capabilities.class.getName());

  public DesiredCapabilities(String browser, String version, Platform platform) {
    setCapability(BROWSER_NAME, browser);
    setCapability(VERSION, version);
    setCapability(PLATFORM, platform);
  }

  public DesiredCapabilities() {
    // no-arg constructor
  }

  public DesiredCapabilities(Map<String, ?> rawMap) {
    if (rawMap == null) {
      return;
    }

    rawMap.forEach(this::setCapability);
  }

  public DesiredCapabilities(Capabilities other) {
    merge(other);
  }

  public DesiredCapabilities(Capabilities... others) {
    for (Capabilities caps : others) {
      merge(caps);
    }
  }

  public void setBrowserName(String browserName) {
    setCapability(BROWSER_NAME, browserName);
  }

  public void setVersion(String version) {
    setCapability(VERSION, version);
  }

  public void setPlatform(Platform platform) {
    setCapability(PLATFORM, platform);
  }

  public void setJavascriptEnabled(boolean javascriptEnabled) {
    setCapability(SUPPORTS_JAVASCRIPT, javascriptEnabled);
  }

  public boolean acceptInsecureCerts() {
    if (getCapability(ACCEPT_INSECURE_CERTS) != null) {
      Object raw = getCapability(ACCEPT_INSECURE_CERTS);
      if (raw instanceof String) {
        return Boolean.parseBoolean((String) raw);
      } else if (raw instanceof Boolean) {
        return ((Boolean) raw).booleanValue();
      }
    }
    return true;
  }

  public void setAcceptInsecureCerts(boolean acceptInsecureCerts) {
    setCapability(ACCEPT_INSECURE_CERTS, acceptInsecureCerts);
  }

  /**
   * Merges the extra capabilities provided into this DesiredCapabilities instance. If capabilities
   * with the same name exist in this instance, they will be overridden by the values from the
   * extraCapabilities object.
   *
   * @param extraCapabilities Additional capabilities to be added.
   * @return DesiredCapabilities after the merge
   */
  @Override
  public DesiredCapabilities merge(Capabilities extraCapabilities) {
    super.merge(extraCapabilities);
    return this;
  }

  public static DesiredCapabilities android() {
    return new DesiredCapabilities(BrowserType.ANDROID, "", Platform.ANDROID);
  }

  public static DesiredCapabilities chrome() {
    LOG.info("Using `new ChromeOptions()` is preferred to `DesiredCapabilities.chrome()`");
    return new DesiredCapabilities(BrowserType.CHROME, "", Platform.ANY);
  }

  public static DesiredCapabilities firefox() {
    LOG.info("Using `new FirefoxOptions()` is preferred to `DesiredCapabilities.firefox()`");
    DesiredCapabilities capabilities = new DesiredCapabilities(
        BrowserType.FIREFOX,
        "",
        Platform.ANY);
    capabilities.setCapability("acceptInsecureCerts", true);

    return capabilities;
  }

  public static DesiredCapabilities htmlUnit() {
    return new DesiredCapabilities(BrowserType.HTMLUNIT, "", Platform.ANY);
  }

  public static DesiredCapabilities edge() {
    LOG.info("Using `new EdgeOptions()` is preferred to `DesiredCapabilities.edge()`");
    return new DesiredCapabilities(BrowserType.EDGE, "", Platform.WINDOWS);
  }
  public static DesiredCapabilities internetExplorer() {
    DesiredCapabilities capabilities = new DesiredCapabilities(
        BrowserType.IE, "", Platform.WINDOWS);
    capabilities.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
    return capabilities;
  }

  public static DesiredCapabilities iphone() {
    return new DesiredCapabilities(BrowserType.IPHONE, "", Platform.MAC);
  }

  public static DesiredCapabilities ipad() {
    return new DesiredCapabilities(BrowserType.IPAD, "", Platform.MAC);
  }

  /**
   * @return DesiredCapabilities for opera
   * @deprecated Use #operaBlink
   */
  @Deprecated
  public static DesiredCapabilities opera() {
    return new DesiredCapabilities(BrowserType.OPERA, "", Platform.ANY);
  }

  public static DesiredCapabilities operaBlink() {
    LOG.info("Using `new OperaOptions()` is preferred to `DesiredCapabilities.operaBlink()`");
    return new DesiredCapabilities(BrowserType.OPERA_BLINK, "", Platform.ANY);
  }

  public static DesiredCapabilities safari() {
    LOG.info("Using `new SafariOptions()` is preferred to `DesiredCapabilities.safari()`");
    return new DesiredCapabilities(BrowserType.SAFARI, "", Platform.MAC);
  }

  /**
   * @deprecated PhantomJS is no longer actively developed, and support will eventually be dropped.
   */
  @Deprecated
  public static DesiredCapabilities phantomjs() {
    return new DesiredCapabilities(BrowserType.PHANTOMJS, "", Platform.ANY);
  }
}

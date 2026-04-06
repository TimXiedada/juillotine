/* SPDX-License-Identifier: Apache-2.0 */
/*
   Copyright (c) 2026 Xie Youtian. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package net.xiedada.juillotine;

import net.xiedada.juillotine.adapters.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class Service {

    private final Properties propertiesFull;
    private Options options;
    private Adapter db;
    private HostCheckers.HostChecker hostChecker;

    public Service() {
        this.propertiesFull = loadProperties();
        initialize(this.propertiesFull);
    }

    private Properties loadProperties() {
        Properties props = new Properties();

        // 1. 尝试从环境变量JUILLOTINE_PROPS读取
        String envPath = System.getenv("JUILLOTINE_PROPS");
        if (envPath != null) {
            try (InputStream is = new FileInputStream(envPath)) {
                props.load(is);
                return props;
            } catch (IOException e) {
                // 继续下一个选项
            }
        }

        // 2. 尝试从工作文件夹读取
        try (InputStream is = new FileInputStream("juillotine.properties")) {
            props.load(is);
            return props;
        } catch (IOException e) {
            // 继续下一个选项
        }

        // 3. 尝试从Tomcat配置目录读取
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null) {
            try (InputStream is = new FileInputStream(catalinaBase + "/conf/juillotine.properties")) {
                props.load(is);
                return props;
            } catch (IOException e) {
                // 继续下一个选项
            }
        }

        // 4. 尝试从Resource内置读取
        try (InputStream is = getClass().getResourceAsStream("/conf/juillotine.properties")) {
            if (is != null) {
                props.load(is);
                return props;
            }
        } catch (IOException e) {
            // 所有选项都失败
        }

        throw new RuntimeException("Failed to load configuration from any source");
    }

    public Service(Properties properties) {
        this.propertiesFull = new Properties();
        this.propertiesFull.putAll(properties);
        initialize(properties);
    }

    private void initialize(Properties properties) {
        options = Options.fromProperties(properties);
        String dbAName = properties.getProperty("juillotine.dbAdapter");
        if (dbAName == null || dbAName.isBlank()) {
            throw new IllegalArgumentException("juillotine.dbAdapter is not provided");
        }
        try {
            Class<?> clazz = Class.forName("net.xiedada.juillotine.adapters."+dbAName.trim());
            if (!Adapter.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Class " + dbAName + " does not implement Adapter interface");
            }

            @SuppressWarnings("unchecked")
            Class<? extends Adapter> adapterClass = (Class<? extends Adapter>) clazz;
            db = adapterClass.getDeclaredConstructor(Properties.class).newInstance(properties);

        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new RuntimeException("Failed to initialize database adapter", e);
        }

        this.hostChecker = HostCheckers.HostChecker.matching(options.requiredHost(), options.hostMatchingMode());
    }

    public Options options() {
        return options;
    }

    public URL ensureUrl(String str) {
        return parseUrl(str);
    }

    public URL ensureUrl(URL url) {
        return url;
    }

    private URL parseUrl(String str) {
        return db.parseUrl(str, options);
    }

    public ResponseTriplet get(String code) {
        String url = db.find(code);
        try {
            return url != null ? new ResponseTriplet(302, new HashMap<>(Map.of("Location", parseUrl(url).toURI().toASCIIString())), "") : new ResponseTriplet(404, null, "No url found for " + code);
        } catch (URISyntaxException e) {
            // What the fuck? Isn't URL a subset to URI?
            throw new RuntimeException(e);
        }
    }

    public ResponseTriplet create(String url, String code) {
        URL ensuredUrl = ensureUrl(url);
        ResponseTriplet resp;
        String actualCode;
        if ((resp = checkHost(ensuredUrl)) != null) {
            return resp;
        }
        try {
            actualCode = db.add(ensuredUrl.toString(), code, options);
            if (actualCode == null || actualCode.isEmpty()) {
                resp = new ResponseTriplet(
                        422,
                        null,
                        "Unable to shorten " + ensuredUrl
                );
            } else {
                resp = new ResponseTriplet(
                        201,
                        new HashMap<>(Map.of("Location", actualCode)),
                        ensuredUrl.toString()
                );
            }
        } catch (IllegalArgumentException e) {
            resp = new ResponseTriplet(
                    422,
                    null,
                    e.toString()
            );
        }
        return resp;
    }

    private ResponseTriplet checkHost(URL url) {
        if (!url.getProtocol().matches("^https?$"))
            return new ResponseTriplet(
                    422,
                    null,
                    "Invalid url: " + url
            );
        else return this.hostChecker.call(url);
    }

    public record Options(
            String requiredHost,
            String hostMatchingMode,
            String defaultUrl,

            int length,
            String charset,

            boolean stripQuery,
            boolean stripAnchor

    ) {


        public static Options fromProperties(Properties properties) {
            String defaultURL = properties.getProperty("juillotine.defaultURL");
            String requiredHost = properties.getProperty("juillotine.requiredHost");
            boolean stripQuery = Boolean.parseBoolean(properties.getProperty("juillotine.URLSanitization.stripQuery"));
            boolean stripAnchor = Boolean.parseBoolean(properties.getProperty("juillotine.URLSanitization.stripAnchor"));
            String lengthStr = properties.getProperty("juillotine.customShortcode.length", "0");
            int length = (lengthStr == null || lengthStr.isBlank()) ? 0 : Integer.parseInt(lengthStr);
            String charset = properties.getProperty("juillotine.customShortcode.charset", "");
            String hostMatchingMode = properties.getProperty("juillotine.hostMatcherMode", "");
            return new Options(requiredHost, hostMatchingMode, defaultURL, length, charset, stripQuery, stripAnchor);
        }

        public boolean withCharset() {
            return !charset.isEmpty() && length > 0;
        }

    }

}

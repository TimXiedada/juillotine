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

package net.xiedada.juillotine.adapters;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import net.xiedada.juillotine.Service;
import org.bson.Document;

import java.util.Properties;

public class MongoDBAdapter extends Adapter {

    private static final String DEFAULT_CONN_STRING = "mongodb://localhost:27017";
    private static final String DEFAULT_DB_NAME = "juillotine";
    private static final String DEFAULT_COLLECTION = "shortlinks";

    private final MongoClient client;
    private final MongoCollection<Document> links;

    public MongoDBAdapter(Properties props) {
        String connString = value(props, "juillotine.MongoDBAdapter.connString", DEFAULT_CONN_STRING);
        String dbName = value(props, "juillotine.MongoDBAdapter.dbName", DEFAULT_DB_NAME);
        String collection = value(props, "juillotine.MongoDBAdapter.collection", DEFAULT_COLLECTION);

        this.client = MongoClients.create(connString);
        this.links = client.getDatabase(dbName).getCollection(collection);
        // 一个 URL 只能对应一个 shortcode：数据库层唯一索引，同时支撑反查
        this.links.createIndex(Indexes.ascending("url"), new IndexOptions().unique(true));
    }

    private static String value(Properties props, String key, String defaultValue) {
        return props != null ? props.getProperty(key, defaultValue) : defaultValue;
    }

    @Override
    public String add(String url, String shortcode, Service.Options options) {
        if (url == null) {
            throw new NullPointerException("url is null");
        }
        String existingCode = codeFor(url);
        if (existingCode != null) {
            return existingCode;
        }
        String code = getCode(url, shortcode, options);
        String existingUrl = find(code);
        if (existingUrl != null) {
            if (existingUrl.equals(url)) {
                return code;
            }
            throw new IllegalArgumentException("shortcode <" + code + "> already exists");
        }
        try {
            links.insertOne(new Document("_id", code).append("url", url));
            return code;
        } catch (MongoWriteException e) {
            // 并发插入撞唯一索引：重查后给出与内存实现一致的语义
            String urlCode = codeFor(url);
            if (urlCode != null) {
                return urlCode;
            }
            throw new IllegalArgumentException("shortcode <" + code + "> already exists");
        }
    }

    @Override
    public String find(String shortcode) {
        Document doc = links.find(Filters.eq("_id", shortcode)).first();
        return doc != null ? doc.getString("url") : null;
    }

    @Override
    public String codeFor(String url) {
        Document doc = links.find(Filters.eq("url", url)).first();
        return doc != null ? doc.getString("_id") : null;
    }

    @Override
    public void clear(String shortcode) {
        links.deleteOne(Filters.eq("_id", shortcode));
    }

    @Override
    public void clearCode(String url) {
        links.deleteMany(Filters.eq("url", url));
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}

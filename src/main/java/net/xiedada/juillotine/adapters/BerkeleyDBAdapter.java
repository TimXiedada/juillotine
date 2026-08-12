package net.xiedada.juillotine.adapters;

import com.sleepycat.je.*;
import com.sleepycat.je.Environment;

import net.xiedada.juillotine.Service;

import com.sleepycat.je.DatabaseConfig;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Properties;


public class BerkeleyDBAdapter extends Adapter {
    private final Environment env;
    private final Database codeToUrlDB;
    private final SecondaryDatabase urlToCodeDB;

    public static class UrlToCodeCreator implements SecondaryKeyCreator {
        @Override
        public boolean createSecondaryKey(SecondaryDatabase secondary,
                                          DatabaseEntry key,      // 主键（shortcode）
                                          DatabaseEntry data,    // 主数据（url）
                                          DatabaseEntry result) {
            // 直接使用 url 作为二级键
            result.setData(data.getData()); // 注意：data 就是 url 的字节数组
            return true;
        }
    }

    public BerkeleyDBAdapter(Properties props) {
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setTransactional(true);
        envConfig.setLocking(true);
        env = new Environment(new File(props.getProperty("juillotine.BerkeleyDBAdapter.dbPath")), envConfig);

        // 主数据库配置
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setTransactional(true);     // 与事务环境匹配
        dbConfig.setSortedDuplicates(false); // 主键不允许重复
        codeToUrlDB = env.openDatabase(null, "codeToUrlDB", dbConfig);

        // 二级数据库配置
        SecondaryConfig secConfig = new SecondaryConfig();
        secConfig.setAllowCreate(true);
        secConfig.setTransactional(true);
        secConfig.setSortedDuplicates(false);
        secConfig.setKeyCreator(new UrlToCodeCreator()); // 自定义提取二级键
        urlToCodeDB = env.openSecondaryDatabase(null, "urlToCodeDB", codeToUrlDB, secConfig);
    }



    @Override
    public String add(String url, String shortcode, Service.Options options) {
        if (url == null) {
            throw new NullPointerException("url is null");
        }
        Transaction txn = env.beginTransaction(null, null);
        try {
            // 1. 反查二级索引：URL 已存在则幂等返回已有 shortcode
            DatabaseEntry urlKey = new DatabaseEntry(url.getBytes(StandardCharsets.UTF_8));
            DatabaseEntry existingCode = new DatabaseEntry();
            DatabaseEntry existingUrlData = new DatabaseEntry();
            if (urlToCodeDB.get(txn, urlKey, existingCode, existingUrlData, LockMode.RMW) == OperationStatus.SUCCESS) {
                txn.commit();
                return new String(existingCode.getData(), StandardCharsets.UTF_8);
            }

            // 2. 未提供 shortcode 时由基类生成（MD5/Base64URL 或自定义字符集）
            String code = getCode(url, shortcode, options);

            // 3. 检查短码冲突，与 MemoryAdapter 一致地抛 IllegalArgumentException
            DatabaseEntry codeKey = new DatabaseEntry(code.getBytes(StandardCharsets.UTF_8));
            if (codeToUrlDB.get(txn, codeKey, new DatabaseEntry(), LockMode.RMW) == OperationStatus.SUCCESS) {
                throw new IllegalArgumentException("shortcode <" + code + "> already exists");
            }

            DatabaseEntry urlData = new DatabaseEntry(url.getBytes(StandardCharsets.UTF_8));
            OperationStatus status = codeToUrlDB.put(txn, codeKey, urlData);
            if (status != OperationStatus.SUCCESS) {
                throw new RuntimeException("Put failed: " + status);
            }

            txn.commit();
            return code;
        } catch (RuntimeException e) {
            txn.abort();
            throw e;
        }
    }

    @Override
    public String find(String shortcode) {
        DatabaseEntry key = new DatabaseEntry(shortcode.getBytes(StandardCharsets.UTF_8));
        DatabaseEntry data = new DatabaseEntry();
        if (codeToUrlDB.get(null, key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            return new String(data.getData(), StandardCharsets.UTF_8);
        }
        return null;
    }

    @Override
    public String codeFor(String url) {
        DatabaseEntry urlKey = new DatabaseEntry(url.getBytes(StandardCharsets.UTF_8));
        DatabaseEntry code = new DatabaseEntry();
        DatabaseEntry urlData = new DatabaseEntry();
        if (urlToCodeDB.get(null, urlKey, code, urlData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            return new String(code.getData(), StandardCharsets.UTF_8);
        }
        return null;
    }

    @Override
    public void clear(String shortcode) {
        DatabaseEntry key = new DatabaseEntry(shortcode.getBytes(StandardCharsets.UTF_8));
        codeToUrlDB.delete(null, key);
    }

    @Override
    public void clearCode(String url) {
        String shortcode = codeFor(url);
        if (shortcode != null) {
            clear(shortcode);
        }
    }

    @Override
    public void close() {
        if (urlToCodeDB != null) urlToCodeDB.close();
        if (codeToUrlDB != null) codeToUrlDB.close();
        if (env != null) env.close();
    }
}

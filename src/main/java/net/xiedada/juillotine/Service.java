package net.xiedada.juillotine;

import net.xiedada.juillotine.adapters.Adapter;

import java.io.IOException;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class Service {

    public record Options(
            String requiredHost,
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
            int length = Integer.parseInt(properties.getProperty("juillotine.customShortcode.length"));
            String charset = properties.getProperty("juillotine.customShortcode.charset");
            return new Options(requiredHost, defaultURL, length, charset, stripQuery, stripAnchor);
        }

        public boolean withCharset() {
            return !charset.isEmpty() && length > 0;
        }

    }

    private Properties propertiesFull;
    private Options options;
    private Adapter db;
    private HostCheckers.HostChecker hostChecker;

    public Options options() {
        return options;
    }

//    public Service(Properties  properties) {
//        options = Options.fromProperties(properties);
//        db =
//    }

    public Service(){
        try (InputStream pin = this.getClass().getResourceAsStream("/juillotine.properties")) {
            propertiesFull = new Properties();
            propertiesFull.load(pin);
            options = Options.fromProperties(propertiesFull);
        }catch (IOException e){
            e.printStackTrace();
        }
        String dbAName = propertiesFull.getProperty("juillotine.dbAdapter");
        if (dbAName ==  null || dbAName.isBlank()) {
            throw new IllegalArgumentException("juillotine.dbAdapter is not provided");
        }
        try{
            Class<?> clazz = Class.forName(dbAName.trim());
            if (!Adapter.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Class " + dbAName + " does not implement Adapter interface");
            }

            @SuppressWarnings("unchecked")
            Class<? extends Adapter> adapterClass = (Class<? extends Adapter>) clazz;
            db = adapterClass.getDeclaredConstructor().newInstance(propertiesFull);

        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException |
                 InvocationTargetException e){
            e.printStackTrace();
        }

        this.hostChecker = HostCheckers.HostChecker.matching(options.requiredHost());
    }

    public URL ensureUrl(String str){
        return parseUrl(str);
    }

    public URL ensureUrl(URL url){
        return url;
    }

    private URL parseUrl(String str){
        return db.parseUrl(str, options);
    }

    public ResponseTriplet get(String code){
        String url = db.find(code);
        return url != null ? new ResponseTriplet(302, new HashMap<String,String>(Map.of("Location",parseUrl(url).toString())),"") : new ResponseTriplet(404, null, "No url found for " + code);
    }

    public ResponseTriplet create(String url, String code){
        URL ensuredUrl = ensureUrl(url);
        ResponseTriplet resp;
        String actualCode;
        if ((resp = checkHost(ensuredUrl)) != null){
            return resp;
        }
        try {
            actualCode = db.add(ensuredUrl.toString(), code, options);
            if (actualCode == null || actualCode.isEmpty()){
                resp = new ResponseTriplet(
                        422,
                        null,
                        "Unable to shorten " + ensuredUrl.toString()
                        );
            } else {
                resp = new ResponseTriplet(
                        201,
                        new HashMap<String,String>(Map.of("Location", actualCode)),
                        ensuredUrl.toString()
                );
            }
        } catch (IllegalArgumentException e){
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
                    "Invalid url: " + url.toString()
            );
        else return this.hostChecker.call(url);
    }

}

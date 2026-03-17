package net.xiedada.juillotine.adapters;

import net.xiedada.juillotine.Service.Options;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.Properties;

public class MemoryAdapter extends Adapter {


    private final BiMap<String,String> urlToCodeDB, codeToUrlDB;

    public MemoryAdapter(Properties props)
    {
        // Standard: <URL, Shortcode>
        urlToCodeDB = HashBiMap.create();
        // Inversed: <Shortcode, URL>
        codeToUrlDB = urlToCodeDB.inverse();
    }

    @Override
    public String add(String url, String shortcode, Options options) throws IllegalArgumentException, NullPointerException {
        if(url == null){
            throw new NullPointerException("url is null");
        }
        if (urlToCodeDB.get(url) != null) {
            return urlToCodeDB.get(url);
        } else {
            String code = getCode(url, shortcode, options);
            if (codeToUrlDB.get(code) != null){
                throw new IllegalArgumentException("shortcode <"+ code +"> already exists");
            } else {
                urlToCodeDB.put(url, code);
                return code;
            }
        }

    }

    @Override
    public String find(String shortcode) throws NullPointerException {
        return codeToUrlDB.get(shortcode);
    }

    @Override
    public String codeFor(String url) throws NullPointerException {
        return urlToCodeDB.get(url);
    }

    @Override
    public void clear(String shortcode) {
        codeToUrlDB.remove(shortcode);
    }

    @Override
    public void clearCode(String url) {
        urlToCodeDB.remove(url);
    }

}

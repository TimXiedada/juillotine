package net.xiedada.juillotine.adapters;
import net.xiedada.juillotine.Service;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.math.BigInteger;
import java.util.Properties;


public abstract class Adapter implements IAdapter {

    // Create a shortcode
    public abstract String add(String url, String shortcode, Service.Options options);

    // Standard query and reverse query
    public abstract String find(String shortcode);
    public abstract String codeFor(String url);

    // Remove an entry
    public abstract void clear(String shortcode);
    public abstract void clearCode(String url);

    String getCode(String url, String code, Service.Options options) {
        return code != null && !code.isEmpty()
                ? code
                : options != null && options.withCharset()
                ? shortenFixedCharset(url, options.length(), options.charset())
                : shorten(url);
    }

    // Use an algorithm to create a unique shortcode for a URL
    private String shorten(String url){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(url.getBytes(StandardCharsets.UTF_8));
            byte[] last4Bytes = new byte[4];
            System.arraycopy(digest, digest.length - 4, last4Bytes, 0, 4);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(last4Bytes);
        } catch (NoSuchAlgorithmException e) {
           throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    private String shortenFixedCharset(String url,int length,String char_set){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            BigInteger bigint = new BigInteger(md.digest(url.getBytes(StandardCharsets.UTF_8)));
            StringBuilder code = new StringBuilder();
            while (bigint.compareTo(BigInteger.ZERO) < 1 && length > 0) {
                code.append(char_set.charAt((bigint.remainder(BigInteger.valueOf(char_set.length()))).intValue()));
                bigint =  bigint.divide(BigInteger.valueOf(char_set.length()));
                length--;
            }
            return code.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    public final URL parseUrl(String url, Service.Options options) throws IllegalArgumentException {

        url = url.replaceAll("\\s+", "");
        if (options.stripQuery()) {
            url = url.replaceAll("\\?.*", "");
        }
        if (options.stripAnchor()) {
            url = url.replaceAll("#.*", "");
        }
        try {
            return new URI(url).toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid URL");
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("invalid URI");
        }

    }
}


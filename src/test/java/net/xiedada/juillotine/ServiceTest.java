package net.xiedada.juillotine;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class ServiceTest {

    private Properties testProps;

    @Before
    public void setUp() {
        testProps = new Properties();
        testProps.setProperty("juillotine.defaultURL", "https://example.com/");
        testProps.setProperty("juillotine.requiredHost", "example.com");
        testProps.setProperty("juillotine.hostMatcherMode", "");
        testProps.setProperty("juillotine.URLSanitization.stripQuery", "false");
        testProps.setProperty("juillotine.URLSanitization.stripAnchor", "false");
        testProps.setProperty("juillotine.customShortcode.charset", "");
        testProps.setProperty("juillotine.customShortcode.length", "0");
        testProps.setProperty("juillotine.dbAdapter", "MemoryAdapter");
    }

    @Test
    public void testOptionsFromProperties() {
        Properties props = new Properties();
        props.setProperty("juillotine.defaultURL", "https://test.com/");
        props.setProperty("juillotine.requiredHost", "test.com");
        props.setProperty("juillotine.hostMatcherMode", "wildcard");
        props.setProperty("juillotine.URLSanitization.stripQuery", "true");
        props.setProperty("juillotine.URLSanitization.stripAnchor", "true");
        props.setProperty("juillotine.customShortcode.length", "8");
        props.setProperty("juillotine.customShortcode.charset", "abc123");

        Service.Options options = Service.Options.fromProperties(props);

        assertEquals("test.com", options.requiredHost());
        assertEquals("wildcard", options.hostMatchingMode());
        assertEquals("https://test.com/", options.defaultUrl());
        assertTrue(options.stripQuery());
        assertTrue(options.stripAnchor());
        assertEquals(8, options.length());
        assertEquals("abc123", options.charset());
        assertTrue(options.withCharset());
    }

    @Test
    public void testOptionsFromPropertiesWithDefaultHostMatchingMode() {
        Properties props = new Properties();
        props.setProperty("juillotine.defaultURL", "https://test.com/");
        props.setProperty("juillotine.requiredHost", "test.com");
        props.setProperty("juillotine.URLSanitization.stripQuery", "false");
        props.setProperty("juillotine.URLSanitization.stripAnchor", "false");
        props.setProperty("juillotine.customShortcode.length", "0");
        props.setProperty("juillotine.customShortcode.charset", "");

        Service.Options options = Service.Options.fromProperties(props);

        assertEquals("", options.hostMatchingMode());
    }

    @Test
    public void testOptionsWithCharsetFalseWhenEmpty() {
        Properties props = new Properties();
        props.setProperty("juillotine.defaultURL", "https://test.com/");
        props.setProperty("juillotine.requiredHost", "test.com");
        props.setProperty("juillotine.URLSanitization.stripQuery", "false");
        props.setProperty("juillotine.URLSanitization.stripAnchor", "false");
        props.setProperty("juillotine.customShortcode.length", "0");
        props.setProperty("juillotine.customShortcode.charset", "");

        Service.Options options = Service.Options.fromProperties(props);
        assertFalse(options.withCharset());
    }

    @Test
    public void testCreateShortUrl() {
        Service service = new Service(testProps);
        ResponseTriplet response = service.create("https://example.com/page", "testcode");
        assertEquals(201, response.status());
        assertNotNull(response.headers());
        assertEquals("testcode", response.headers().get("Location"));
        assertEquals("https://example.com/page", response.body());
    }

    @Test
    public void testGetShortUrl() {
        Service service = new Service(testProps);
        service.create("https://example.com/page", "testcode");
        ResponseTriplet response = service.get("testcode");
        assertEquals(302, response.status());
        assertNotNull(response.headers());
        assertTrue(response.headers().get("Location").contains("https://example.com/page"));
    }

    @Test
    public void testGetNonExistent() {
        Service service = new Service(testProps);
        ResponseTriplet response = service.get("nonexistent");
        assertEquals(404, response.status());
        assertNull(response.headers());
        assertTrue(response.body().contains("No url found"));
    }

    @Test
    public void testCreateWithInvalidHost() {
        Service service = new Service(testProps);
        ResponseTriplet response = service.create("https://other.com/page", "testcode");
        assertEquals(422, response.status());
    }

    @Test
    public void testCreateWithInvalidProtocol() {
        Service service = new Service(testProps);
        ResponseTriplet response = service.create("ftp://example.com/page", "testcode");
        assertEquals(422, response.status());
        assertTrue(response.body().contains("Invalid url"));
    }

    @Test
    public void testCreateAutoShortcode() {
        Service service = new Service(testProps);
        ResponseTriplet response = service.create("https://example.com/auto", null);
        assertEquals(201, response.status());
        assertNotNull(response.headers().get("Location"));
        assertFalse(response.headers().get("Location").isEmpty());
    }

    @Test
    public void testCreateDuplicateShortcode() {
        Service service = new Service(testProps);
        service.create("https://example.com/first", "duplicate");
        ResponseTriplet response = service.create("https://example.com/second", "duplicate");
        assertEquals(422, response.status());
    }

    @Test
    public void testEnsureUrlFromString() throws Exception {
        Service service = new Service(testProps);
        String urlStr = "https://example.com/page";
        java.net.URL url = service.ensureUrl(urlStr);
        assertEquals(urlStr, url.toString());
    }

    @Test
    public void testEnsureUrlFromUrl() throws Exception {
        Service service = new Service(testProps);
        java.net.URL original = new java.net.URL("https://example.com/page");
        java.net.URL result = service.ensureUrl(original);
        assertSame(original, result);
    }

    @Test
    public void testOptionsAccessor() {
        Service service = new Service(testProps);
        Service.Options options = service.options();
        assertNotNull(options);
        assertEquals("example.com", options.requiredHost());
    }

    @Test
    public void testCreateWithCustomCharset() {
        Properties customProps = new Properties();
        customProps.setProperty("juillotine.defaultURL", "https://example.com/");
        customProps.setProperty("juillotine.requiredHost", "example.com");
        customProps.setProperty("juillotine.URLSanitization.stripQuery", "false");
        customProps.setProperty("juillotine.URLSanitization.stripAnchor", "false");
        customProps.setProperty("juillotine.customShortcode.charset", "abcdef");
        customProps.setProperty("juillotine.customShortcode.length", "5");
        customProps.setProperty("juillotine.dbAdapter", "MemoryAdapter");

        Service service = new Service(customProps);
        ResponseTriplet response = service.create("https://example.com/customcharset", null);
        assertEquals(201, response.status());
        assertNotNull(response.headers().get("Location"));
        String shortcode = response.headers().get("Location");
        assertEquals(5, shortcode.length());
        assertTrue(shortcode.matches("^[abcdef]+$"));
    }

    @Test
    public void testCreateWithCustomCharsetNumeric() {
        Properties customProps = new Properties();
        customProps.setProperty("juillotine.defaultURL", "https://example.com/");
        customProps.setProperty("juillotine.requiredHost", "example.com");
        customProps.setProperty("juillotine.URLSanitization.stripQuery", "false");
        customProps.setProperty("juillotine.URLSanitization.stripAnchor", "false");
        customProps.setProperty("juillotine.customShortcode.charset", "0123456789");
        customProps.setProperty("juillotine.customShortcode.length", "8");
        customProps.setProperty("juillotine.dbAdapter", "MemoryAdapter");

        Service service = new Service(customProps);
        ResponseTriplet response = service.create("https://example.com/numericcharset", null);
        assertEquals(201, response.status());
        String shortcode = response.headers().get("Location");
        assertEquals(8, shortcode.length());
        assertTrue(shortcode.matches("^[0123456789]+$"));
    }

    @Test
    public void testCreateWithCustomCharsetAndGet() {
        Properties customProps = new Properties();
        customProps.setProperty("juillotine.defaultURL", "https://example.com/");
        customProps.setProperty("juillotine.requiredHost", "example.com");
        customProps.setProperty("juillotine.URLSanitization.stripQuery", "false");
        customProps.setProperty("juillotine.URLSanitization.stripAnchor", "false");
        customProps.setProperty("juillotine.customShortcode.charset", "xyz789");
        customProps.setProperty("juillotine.customShortcode.length", "6");
        customProps.setProperty("juillotine.dbAdapter", "MemoryAdapter");

        Service service = new Service(customProps);
        String originalUrl = "https://example.com/roundtrip";
        ResponseTriplet createResponse = service.create(originalUrl, null);
        String shortcode = createResponse.headers().get("Location");

        ResponseTriplet getResponse = service.get(shortcode);
        assertEquals(302, getResponse.status());
        assertTrue(getResponse.headers().get("Location").contains(originalUrl));
    }

    @Test
    public void testCreateWithWildcardHostMatchingMode() {
        Properties wildcardProps = new Properties();
        wildcardProps.setProperty("juillotine.defaultURL", "https://example.com/");
        wildcardProps.setProperty("juillotine.requiredHost", "*.example.com");
        wildcardProps.setProperty("juillotine.hostMatcherMode", "wildcard");
        wildcardProps.setProperty("juillotine.URLSanitization.stripQuery", "false");
        wildcardProps.setProperty("juillotine.URLSanitization.stripAnchor", "false");
        wildcardProps.setProperty("juillotine.customShortcode.charset", "");
        wildcardProps.setProperty("juillotine.customShortcode.length", "0");
        wildcardProps.setProperty("juillotine.dbAdapter", "MemoryAdapter");

        Service service = new Service(wildcardProps);

        // Test with subdomain
        ResponseTriplet response1 = service.create("https://sub.example.com/page", "wildcard1");
        assertEquals(201, response1.status());

        // Test with exact domain
        ResponseTriplet response2 = service.create("https://example.com/page", "wildcard2");
        assertEquals(201, response2.status());

        // Test with invalid domain
        ResponseTriplet response3 = service.create("https://other.com/page", "wildcard3");
        assertEquals(422, response3.status());
    }

    @Test
    public void testCreateWithRegexHostMatchingMode() {
        Properties regexProps = new Properties();
        regexProps.setProperty("juillotine.defaultURL", "https://example.com/");
        regexProps.setProperty("juillotine.requiredHost", ".*\\.example\\.com");
        regexProps.setProperty("juillotine.hostMatcherMode", "regex");
        regexProps.setProperty("juillotine.URLSanitization.stripQuery", "false");
        regexProps.setProperty("juillotine.URLSanitization.stripAnchor", "false");
        regexProps.setProperty("juillotine.customShortcode.charset", "");
        regexProps.setProperty("juillotine.customShortcode.length", "0");
        regexProps.setProperty("juillotine.dbAdapter", "MemoryAdapter");

        Service service = new Service(regexProps);

        // Test with matching subdomain
        ResponseTriplet response1 = service.create("https://sub.example.com/page", "regex1");
        assertEquals(201, response1.status());

        // Test with another matching subdomain
        ResponseTriplet response2 = service.create("https://api.example.com/page", "regex2");
        assertEquals(201, response2.status());

        // Test with non-matching domain
        ResponseTriplet response3 = service.create("https://example.com/page", "regex3");
        assertEquals(422, response3.status());
    }

    @Test
    public void testCreateWithStringHostMatchingMode() {
        Properties stringProps = new Properties();
        stringProps.setProperty("juillotine.defaultURL", "https://example.com/");
        stringProps.setProperty("juillotine.requiredHost", "example.com");
        stringProps.setProperty("juillotine.hostMatcherMode", "string");
        stringProps.setProperty("juillotine.URLSanitization.stripQuery", "false");
        stringProps.setProperty("juillotine.URLSanitization.stripAnchor", "false");
        stringProps.setProperty("juillotine.customShortcode.charset", "");
        stringProps.setProperty("juillotine.customShortcode.length", "0");
        stringProps.setProperty("juillotine.dbAdapter", "MemoryAdapter");

        Service service = new Service(stringProps);

        // Test with exact domain
        ResponseTriplet response1 = service.create("https://example.com/page", "string1");
        assertEquals(201, response1.status());

        // Test with subdomain (should fail with string mode)
        ResponseTriplet response2 = service.create("https://sub.example.com/page", "string2");
        assertEquals(422, response2.status());
    }

    @Test
    public void testCreateWithDefaultHostMatchingMode() {
        Properties defaultProps = new Properties();
        defaultProps.setProperty("juillotine.defaultURL", "https://example.com/");
        defaultProps.setProperty("juillotine.requiredHost", "example.com");
        defaultProps.setProperty("juillotine.hostMatcherMode", "");
        defaultProps.setProperty("juillotine.URLSanitization.stripQuery", "false");
        defaultProps.setProperty("juillotine.URLSanitization.stripAnchor", "false");
        defaultProps.setProperty("juillotine.customShortcode.charset", "");
        defaultProps.setProperty("juillotine.customShortcode.length", "0");
        defaultProps.setProperty("juillotine.dbAdapter", "MemoryAdapter");

        Service service = new Service(defaultProps);

        // Test with exact domain
        ResponseTriplet response1 = service.create("https://example.com/page", "default1");
        assertEquals(201, response1.status());
    }

    @Test
    public void testCreateWithUnknownHostMatchingMode() {
        Properties unknownProps = new Properties();
        unknownProps.setProperty("juillotine.defaultURL", "https://example.com/");
        unknownProps.setProperty("juillotine.requiredHost", "example.com");
        unknownProps.setProperty("juillotine.hostMatcherMode", "unknownmode");
        unknownProps.setProperty("juillotine.URLSanitization.stripQuery", "false");
        unknownProps.setProperty("juillotine.URLSanitization.stripAnchor", "false");
        unknownProps.setProperty("juillotine.customShortcode.charset", "");
        unknownProps.setProperty("juillotine.customShortcode.length", "0");
        unknownProps.setProperty("juillotine.dbAdapter", "MemoryAdapter");

        Service service = new Service(unknownProps);

        // Should default to string mode
        ResponseTriplet response = service.create("https://example.com/page", "unknown1");
        assertEquals(201, response.status());
    }
}

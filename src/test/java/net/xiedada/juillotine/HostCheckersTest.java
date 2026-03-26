package net.xiedada.juillotine;

import org.junit.Test;
import java.net.URL;
import static org.junit.Assert.*;

public class HostCheckersTest {

    @Test
    public void testStringHostCheckerValid() throws Exception {
        HostCheckers.HostChecker checker = new HostCheckers.StringHostChecker("example.com");
        URL url = new URL("https://example.com/path");

        assertTrue(checker.valid(url));
        assertNull(checker.call(url));
    }

    @Test
    public void testStringHostCheckerInvalid() throws Exception {
        HostCheckers.HostChecker checker = new HostCheckers.StringHostChecker("example.com");
        URL url = new URL("https://other.com/path");

        assertFalse(checker.valid(url));
        ResponseTriplet response = checker.call(url);
        assertNotNull(response);
        assertEquals(422, response.status());
        assertTrue(response.body().contains("example.com"));
    }

    @Test
    public void testStringHostCheckerCaseInsensitive() throws Exception {
        HostCheckers.HostChecker checker = new HostCheckers.StringHostChecker("EXAMPLE.COM");
        URL url = new URL("https://example.com/path");

        assertTrue(checker.valid(url));
    }

    @Test
    public void testRegexHostCheckerValid() throws Exception {
        HostCheckers.HostChecker checker = new HostCheckers.RegexHostChecker(".*\\.example\\.com");
        URL url = new URL("https://sub.example.com/path");

        assertTrue(checker.valid(url));
        assertNull(checker.call(url));
    }

    @Test
    public void testRegexHostCheckerInvalid() throws Exception {
        HostCheckers.HostChecker checker = new HostCheckers.RegexHostChecker(".*\\.example\\.com");
        URL url = new URL("https://other.com/path");

        assertFalse(checker.valid(url));
        ResponseTriplet response = checker.call(url);
        assertNotNull(response);
        assertEquals(422, response.status());
    }

    @Test
    public void testWildcardHostCheckerMatchValid() throws Exception {
        assertEquals("example.com", HostCheckers.WildcardHostChecker.match("*.example.com"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWildcardHostCheckerMatchInvalidFormat() {
        HostCheckers.WildcardHostChecker.match("example.com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWildcardHostCheckerMatchEmpty() {
        HostCheckers.WildcardHostChecker.match("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWildcardHostCheckerMatchNull() {
        HostCheckers.WildcardHostChecker.match(null);
    }

    @Test
    public void testWildcardHostCheckerValidSubdomain() throws Exception {
        HostCheckers.HostChecker checker = new HostCheckers.WildcardHostChecker("*.example.com");
        URL url = new URL("https://sub.example.com/path");

        assertTrue(checker.valid(url));
    }

    @Test
    public void testWildcardHostCheckerValidExactDomain() throws Exception {
        HostCheckers.HostChecker checker = new HostCheckers.WildcardHostChecker("*.example.com");
        URL url = new URL("https://example.com/path");

        assertTrue(checker.valid(url));
    }

    @Test
    public void testWildcardHostCheckerInvalid() throws Exception {
        HostCheckers.HostChecker checker = new HostCheckers.WildcardHostChecker("*.example.com");
        URL url = new URL("https://other.com/path");

        assertFalse(checker.valid(url));
    }

    @Test
    public void testMatchingFactoryString() throws Exception {
        HostCheckers.HostChecker checker = HostCheckers.HostChecker.matching("example.com", "string");
        assertTrue(checker instanceof HostCheckers.StringHostChecker);

        URL url = new URL("https://example.com/path");
        assertTrue(checker.valid(url));
    }

    @Test
    public void testMatchingFactoryWildcard() throws Exception {
        HostCheckers.HostChecker checker = HostCheckers.HostChecker.matching("*.example.com", "wildcard");
        assertTrue(checker instanceof HostCheckers.WildcardHostChecker);

        URL url = new URL("https://sub.example.com/path");
        assertTrue(checker.valid(url));
    }

    @Test
    public void testMatchingFactoryRegex() throws Exception {
        HostCheckers.HostChecker checker = HostCheckers.HostChecker.matching(".*\\.example\\.com", "regex");
        assertTrue(checker instanceof HostCheckers.RegexHostChecker);

        URL url = new URL("https://test.example.com/path");
        assertTrue(checker.valid(url));
    }

    @Test
    public void testMatchingFactoryWithOptionString() throws Exception {
        HostCheckers.HostChecker checker = HostCheckers.HostChecker.matching("example.com", "string");
        assertTrue(checker instanceof HostCheckers.StringHostChecker);

        URL url = new URL("https://example.com/path");
        assertTrue(checker.valid(url));
    }

    @Test
    public void testMatchingFactoryWithOptionWildcard() throws Exception {
        HostCheckers.HostChecker checker = HostCheckers.HostChecker.matching("*.example.com", "wildcard");
        assertTrue(checker instanceof HostCheckers.WildcardHostChecker);

        URL url = new URL("https://sub.example.com/path");
        assertTrue(checker.valid(url));
    }

    @Test
    public void testMatchingFactoryWithOptionRegex() throws Exception {
        HostCheckers.HostChecker checker = HostCheckers.HostChecker.matching(".*\\.example\\.com", "regex");
        assertTrue(checker instanceof HostCheckers.RegexHostChecker);

        URL url = new URL("https://test.example.com/path");
        assertTrue(checker.valid(url));
    }

    @Test
    public void testMatchingFactoryWithOptionDefault() throws Exception {
        HostCheckers.HostChecker checker = HostCheckers.HostChecker.matching("example.com", "unknown");
        assertTrue(checker instanceof HostCheckers.StringHostChecker);

        URL url = new URL("https://example.com/path");
        assertTrue(checker.valid(url));
    }

    @Test
    public void testMatchingFactoryWithOptionNull() throws Exception {
        HostCheckers.HostChecker checker = HostCheckers.HostChecker.matching("example.com", null);
        assertTrue(checker instanceof HostCheckers.StringHostChecker);

        URL url = new URL("https://example.com/path");
        assertTrue(checker.valid(url));
    }

    @Test
    public void testMatchingFactoryWithOptionEmpty() throws Exception {
        HostCheckers.HostChecker checker = HostCheckers.HostChecker.matching("example.com", "");
        assertTrue(checker instanceof HostCheckers.StringHostChecker);

        URL url = new URL("https://example.com/path");
        assertTrue(checker.valid(url));
    }
}

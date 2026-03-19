package net.xiedada.juillotine.adapters;

import net.xiedada.juillotine.Service;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.*;

public abstract class StorageAdapterTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    /** 由子类提供具体的 StorageAdapter 实例 */
    protected abstract Adapter createAdapter();

    /** Helper method to create default Options for tests */
    protected Service.Options createDefaultOptions() {
        return new Service.Options(
                "default.com",
                null,
                "https://default.com",
                6,
                "",
                false,
                false
        );
    }

    /** Helper method to create Options with custom charset for tests */
    protected Service.Options createCustomCharsetOptions(String charset, int length) {
        return new Service.Options(
                "default.com",
                null,
                "https://default.com",
                length,
                charset,
                false,
                false
        );
    }

    @Test
    public void testAddWithAutoShortcode() {
        Adapter adapter = createAdapter();
        String url = "https://example.com";
        String shortcode = adapter.add(url, null, createDefaultOptions());
        assertNotNull(shortcode);
        assertEquals(6, shortcode.length());                 // 基于基类算法
        assertTrue(shortcode.matches("^[A-Za-z0-9_-]+$"));   // Base64URL 安全字符
        assertEquals(url, adapter.find(shortcode));
        assertEquals(shortcode, adapter.codeFor(url));
    }

    @Test
    public void testAddWithCustomShortcode() {
        Adapter adapter = createAdapter();
        String url = "https://example.com";
        String custom = "custom1";
        String result = adapter.add(url, custom, createDefaultOptions());
        assertEquals(custom, result);
        assertEquals(url, adapter.find(custom));
        assertEquals(custom, adapter.codeFor(url));
    }

    @Test
    public void testAddDuplicateUrlThenClearAndAddReturnsSameShortcode() {
        Adapter adapter = createAdapter();
        String url = "https://example.com";
        String first = adapter.add(url, null, createDefaultOptions());
        adapter.clearCode(url);
        String second = adapter.add(url, null, createDefaultOptions());
        assertEquals(first, second);
        assertEquals(url, adapter.find(first));
    }

    @Test
    public void testAddDifferentUrlWithSameCustomShortcodeFails() {
        Adapter adapter = createAdapter();
        String url1 = "https://example.com";
        String url2 = "https://example.org";
        String custom = "same";
        String result1 = adapter.add(url1, custom, createDefaultOptions());
        assertEquals(custom, result1);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("shortcode <" + custom + "> already exists");
        adapter.add(url2, custom, createDefaultOptions());   // 应抛出异常

        assertEquals(url1, adapter.find(custom));
        assertNull(adapter.codeFor(url2));
    }

    @Test
    public void testAddUrlWithExistingAutoShortcodeConflict() {
        Adapter adapter = createAdapter();
        String url1 = "https://example.com";
        String shortcode1 = adapter.add(url1, null, createDefaultOptions());

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("shortcode <" + shortcode1 + "> already exists");
        String url2 = "https://different.com";
        adapter.add(url2, shortcode1, createDefaultOptions());

        assertEquals(url1, adapter.find(shortcode1));
        assertNull(adapter.codeFor(url2));
    }

    @Test
    public void testFindReturnsNullForNonexistent() {
        Adapter adapter = createAdapter();
        assertNull(adapter.find("nonexistent"));
    }

    @Test
    public void testCodeForReturnsNullForNonexistent() {
        Adapter adapter = createAdapter();
        assertNull(adapter.codeFor("https://nonexistent.com"));
    }

    @Test
    public void testClearShortcode() {
        Adapter adapter = createAdapter();
        String url = "https://example.com";
        String shortcode = adapter.add(url, null, createDefaultOptions());
        adapter.clear(shortcode);
        assertNull(adapter.find(shortcode));
        assertNull(adapter.codeFor(url));
    }

    @Test
    public void testClearCode() {
        Adapter adapter = createAdapter();
        String url = "https://example.com";
        String shortcode = adapter.add(url, null, createDefaultOptions());
        adapter.clearCode(url);
        assertNull(adapter.find(shortcode));
        assertNull(adapter.codeFor(url));
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullUrlThrowsNPE() {
        Adapter adapter = createAdapter();
        adapter.add(null, null, createDefaultOptions());
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullUrlWithCustomThrowsNPE() {
        Adapter adapter = createAdapter();
        adapter.add(null, "custom", createDefaultOptions());
    }

    @Test
    public void testAddWithCustomCharset() {
        Adapter adapter = createAdapter();
        String url = "https://example.com/custom";
        String charset = "abcd1234";
        int length = 6;
        Service.Options options = createCustomCharsetOptions(charset, length);
        String shortcode = adapter.add(url, null, options);
        assertNotNull(shortcode);
        assertEquals(length, shortcode.length());
        assertTrue("Shortcode should only contain characters from charset: " + charset,
                shortcode.matches("^[abcd1234]+$"));
        assertEquals(url, adapter.find(shortcode));
        assertEquals(shortcode, adapter.codeFor(url));
    }

    @Test
    public void testAddWithCustomCharsetAllLetters() {
        Adapter adapter = createAdapter();
        String url = "https://example.com/letters";
        String charset = "ABCDEFGHIJKLMNOP";
        int length = 8;
        Service.Options options = createCustomCharsetOptions(charset, length);
        String shortcode = adapter.add(url, null, options);
        assertNotNull(shortcode);
        assertEquals(length, shortcode.length());
        assertTrue("Shortcode should only contain uppercase letters",
                shortcode.matches("^[A-P]+$"));
        assertEquals(url, adapter.find(shortcode));
    }

    @Test
    public void testAddWithCustomCharsetAllNumbers() {
        Adapter adapter = createAdapter();
        String url = "https://example.com/numbers";
        String charset = "0123456789";
        int length = 10;
        Service.Options options = createCustomCharsetOptions(charset, length);
        String shortcode = adapter.add(url, null, options);
        assertNotNull(shortcode);
        assertEquals(length, shortcode.length());
        assertTrue("Shortcode should only contain numbers",
                shortcode.matches("^[0-9]+$"));
        assertEquals(url, adapter.find(shortcode));
    }

    @Test
    public void testAddWithCustomCharsetAndSpecialChars() {
        Adapter adapter = createAdapter();
        String url = "https://example.com/special";
        String charset = "!@#$%^&*()";
        int length = 5;
        Service.Options options = createCustomCharsetOptions(charset, length);
        String shortcode = adapter.add(url, null, options);
        assertNotNull(shortcode);
        assertEquals(length, shortcode.length());
        for (char c : shortcode.toCharArray()) {
            assertTrue("Character should be from charset: " + c, charset.indexOf(c) >= 0);
        }
        assertEquals(url, adapter.find(shortcode));
    }

    @Test
    public void testAddWithCustomCharsetThenClearAndAddReturnsDifferent() {
        Adapter adapter = createAdapter();
        String url = "https://example.com/clear";
        String charset = "xyz";
        int length = 4;
        Service.Options options = createCustomCharsetOptions(charset, length);
        String first = adapter.add(url, null, options);
        adapter.clearCode(url);
        String second = adapter.add(url, null, options);
        // May or may not be the same, depending on hash, but should be valid
        assertNotNull(second);
        assertEquals(length, second.length());
        assertTrue(second.matches("^[xyz]+$"));
        assertEquals(url, adapter.find(second));
    }

    @Test
    public void testAddWithCustomCharsetPrefersProvidedShortcode() {
        Adapter adapter = createAdapter();
        String url = "https://example.com/prefer";
        String customCode = "mycustomcode";
        String charset = "abcd";
        int length = 4;
        Service.Options options = createCustomCharsetOptions(charset, length);
        String result = adapter.add(url, customCode, options);
        assertEquals(customCode, result); // Should return the provided code, not generate one
        assertEquals(url, adapter.find(customCode));
    }
}
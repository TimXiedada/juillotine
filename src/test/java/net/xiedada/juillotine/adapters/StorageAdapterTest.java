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
                null,
                "https://default.com",
                6,
                "",
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
}
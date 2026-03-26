package net.xiedada.juillotine;

import org.junit.Test;
import java.util.Map;
import java.util.HashMap;
import static org.junit.Assert.*;

public class ResponseTripletTest {

    @Test
    public void testCreateWithAllFields() {
        int status = 200;
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain");
        String body = "Hello World";

        ResponseTriplet triplet = new ResponseTriplet(status, headers, body);

        assertEquals(status, triplet.status());
        assertEquals(headers, triplet.headers());
        assertEquals(body, triplet.body());
    }

    @Test
    public void testCreateWithNullHeaders() {
        ResponseTriplet triplet = new ResponseTriplet(404, null, "Not Found");

        assertEquals(404, triplet.status());
        assertNull(triplet.headers());
        assertEquals("Not Found", triplet.body());
    }

    @Test
    public void testCreateWithNullBody() {
        ResponseTriplet triplet = new ResponseTriplet(302, Map.of("Location", "https://example.com"), null);

        assertEquals(302, triplet.status());
        assertNotNull(triplet.headers());
        assertEquals("https://example.com", triplet.headers().get("Location"));
        assertNull(triplet.body());
    }

    @Test
    public void testEquality() {
        Map<String, String> headers = Map.of("Key", "Value");
        ResponseTriplet t1 = new ResponseTriplet(200, headers, "Body");
        ResponseTriplet t2 = new ResponseTriplet(200, headers, "Body");
        ResponseTriplet t3 = new ResponseTriplet(404, null, null);

        assertEquals(t1, t2);
        assertNotEquals(t1, t3);
    }

    @Test
    public void testHashCode() {
        Map<String, String> headers = Map.of("Key", "Value");
        ResponseTriplet t1 = new ResponseTriplet(200, headers, "Body");
        ResponseTriplet t2 = new ResponseTriplet(200, headers, "Body");

        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    public void testToString() {
        ResponseTriplet triplet = new ResponseTriplet(200, null, "Test");
        String str = triplet.toString();

        assertTrue(str.contains("200"));
        assertTrue(str.contains("Test"));
    }
}

package com.kazurayam.materialstore.core;

import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Metadata;
import com.kazurayam.materialstore.util.KeyValuePair;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MetadataTest {

    Logger log = LoggerFactory.getLogger(MetadataTest.class);

    @Test
    public void testParseQuery() {
        List<KeyValuePair> list = Metadata.parseQuery("a=x&b=y&b=z&c");
        assertEquals(4, list.size());
        assertEquals("a", list.get(0).getKey());
        assertEquals("x", list.get(0).getValue());
        assertEquals("b", list.get(1).getKey());
        assertEquals("y", list.get(1).getValue());
        assertEquals("b", list.get(2).getKey());
        assertEquals("z", list.get(2).getValue());
        assertEquals("c", list.get(3).getKey());
        assertNull(list.get(3).getValue());
    }

    /**
     * #313
     */
    @Test
    public void testToURLAsString() throws MalformedURLException, MaterialstoreException {
        URL url = new URL("https://duckduckgo.com/search/");
        Metadata metadata =
                new Metadata.Builder(url)
                        .exclude("URL.protocol")
                        .build();
        assertNotEquals("file://null_object", metadata.toURLAsString());
        assertEquals(url, metadata.toURL());
        log.debug(metadata.toURL().toString());
    }

}

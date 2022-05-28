package com.kazurayam.materialstore.filesystem;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.metadata.IdentifyMetadataValues;
import com.kazurayam.materialstore.filesystem.metadata.MetadataAttribute;
import com.kazurayam.materialstore.materialize.net.data.DataURLEnabler;
import com.kazurayam.materialstore.util.JsonUtil;
import org.apache.http.NameValuePair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MetadataTest {

    private static final String url0 = "https://cdnjs.cloudflare.com/ajax/libs/jquery/1.11.3/jquery.js?q=12345";
    private static final String url1 = "https://cdnjs.cloudflare.com/ajax/libs/jquery/1.12.4/jquery.js?q=67890";
    private static final String urlT = "http://myadmin.kazurayam.com/";
    private static Metadata metadata0;
    private static Metadata metadata1;
    private static Metadata metadataT;

    @BeforeAll
    public static void beforeAll() throws MalformedURLException {
        metadata0 = Metadata.builder(new URL(url0)).put("profile", "MyAdmin_ProductionEnv").build();
        metadata1 = Metadata.builder(new URL(url1)).put("profile", "MyAdmin_DevelopmentEnv").build();
        metadataT = Metadata.builder(new URL(urlT)).put("profile", "MyAdmin_ProductionEnv").build();
    }

    @Test
    public void test_Builder_putAll() throws MalformedURLException {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8#content");
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("summer", "warm");
        map.put("winter", "cold");
        Metadata metadata = Metadata.builder(url).putAll(map).build();
        Assertions.assertNotNull(metadata);
        Assertions.assertEquals("warm", metadata.get("summer"));
        Assertions.assertEquals("cold", metadata.get("winter"));
    }

    @Test
    public void test_builder_copy() throws MalformedURLException {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8#content");
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("summer", "warm");
        map.put("winter", "cold");
        Metadata metadata = Metadata.builder(url).putAll(map).build();
        //
        Metadata copied = Metadata.builder(metadata).build();
        Assertions.assertNotNull(copied);
        Assertions.assertEquals("warm", copied.get("summer"));
        Assertions.assertEquals("cold", copied.get("winter"));
    }

    @Test
    public void test_Builder_with_URL_as_arg() throws MalformedURLException {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8#content");
        Metadata metadata = Metadata.builder(url).build();
        Assertions.assertNotNull(metadata);
        Assertions.assertEquals("https", metadata.get("URL.protocol"));
        Assertions.assertEquals("80", metadata.get("URL.port"));
        Assertions.assertEquals("baeldung.com", metadata.get("URL.host"));
        Assertions.assertEquals("/articles", metadata.get("URL.path"));
        Assertions.assertEquals("topic=java&version=8", metadata.get("URL.query"));
        Assertions.assertEquals("content", metadata.get("URL.fragment"));
        String ms = metadata.toString();
        //println ms
        Assertions.assertTrue(ms.contains("URL.protocol") && ms.contains("https"));
        Assertions.assertTrue(ms.contains("URL.host") && ms.contains("baeldung.com"));
        Assertions.assertTrue(ms.contains("URL.path") && ms.contains("/articles"));
        Assertions.assertTrue(ms.contains("URL.query") && ms.contains("topic=java&version=8"));
        Assertions.assertTrue(ms.contains("URL.fragment") && ms.contains("content"));
    }

    @Test
    public void test_Builder_with_URL_with_port() throws MalformedURLException {
        URL url = new URL("http://127.0.0.1:3000/");
        Metadata metadata = Metadata.builder(url).build();
        Assertions.assertNotNull(metadata);
        Assertions.assertEquals("http", metadata.get("URL.protocol"));
        Assertions.assertEquals("3000", metadata.get("URL.port"));
    }

    @Test
    public void test_canBeIdentified() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("profile", "MyAdmin_ProductionEnv");
        IdentifyMetadataValues identifyMetadataValues = new IdentifyMetadataValues.Builder().putAllNameRegexPairs(map).build();
        Assertions.assertTrue(metadata0.canBeIdentified("profile", identifyMetadataValues));
    }

    @Test
    public void test_canBePaired() {
        QueryOnMetadata query0 = QueryOnMetadata.builder(metadata0).build();
        Assertions.assertTrue(metadata0.canBePaired(query0, "URL.host"));
    }

    @Test
    public void test_compareTo_equals() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("profile", "X");
        Metadata metadata1 = Metadata.builder(map).build();
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("profile", "X");
        Metadata metadata2 = Metadata.builder(map1).build();
        Assertions.assertEquals(0, metadata1.compareTo(metadata2));
    }

    @Test
    public void test_compareTo_minus() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("profile", "X");
        Metadata metadata1 = Metadata.builder(map).build();
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("profile", "Y");
        Metadata metadata2 = Metadata.builder(map1).build();
        Assertions.assertEquals(-1, metadata1.compareTo(metadata2));
    }

    @Test
    public void test_compareTo_plus() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("profile", "X");
        Metadata metadata1 = Metadata.builder(map).build();
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("profile", "A");
        Metadata metadata2 = Metadata.builder(map1).build();
        Assertions.assertTrue(metadata1.compareTo(metadata2) > 0);
    }

    @Test
    public void test_constructor() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("profile", "ProductionEnv");
        Metadata metadata = Metadata.builder(map).build();
        Assertions.assertEquals("ProductionEnv", metadata.get("profile"));
    }

    @Test
    public void test_containsKey() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("profile", "ProductionEnv");
        Metadata metadata = Metadata.builder(map).build();
        Assertions.assertTrue(metadata.containsKey("profile"));
        Assertions.assertFalse(metadata.containsKey("foo"));
    }

    @Test
    public void test_equals() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("profile", "ProductionEnv");
        Metadata m1 = Metadata.builder(map).build();
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("profile", "ProductionEnv");
        Metadata m2 = Metadata.builder(map1).build();
        LinkedHashMap<String, String> map2 = new LinkedHashMap<>(1);
        map2.put("profile", "DevelopmentEnv");
        Metadata m3 = Metadata.builder(map2).build();
        LinkedHashMap<String, String> map3 = new LinkedHashMap<>(1);
        map3.put("foo", "bar");
        Metadata m4 = Metadata.builder(map3).build();
        Assertions.assertEquals(m1, m2);
        Assertions.assertNotEquals(m1, m3);
        Assertions.assertNotEquals(m1, m4);
    }

    @Test
    public void test_get() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("profile", "ProductionEnv");
        Metadata metadata = Metadata.builder(map).build();
        Assertions.assertEquals("ProductionEnv", metadata.get("profile"));
        Assertions.assertNull(metadata.get("foo"));
    }

    @Test
    public void test_getMetadataAttribute() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("profile", "ProductionEnv");
        Metadata metadata = Metadata.builder(map).build();
        MetadataAttribute profile = metadata.getMetadataAttribute("profile");
        Assertions.assertEquals("ProductionEnv", profile.getValue());
    }

    @Test
    public void test_isEmpty() {
        Metadata metadata = Metadata.builder(new LinkedHashMap<>()).build();
        Assertions.assertTrue(metadata.isEmpty());
    }

    @Test
    public void test_keySet() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("profile", "ProductionEnv");
        Metadata metadata = Metadata.builder(map).build();
        Set<String> keySet = metadata.keySet();
        Assertions.assertEquals(1, keySet.size());
        Assertions.assertTrue(keySet.contains("profile"));
    }

    @Test
    public void test_matchesByAster() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("profile", "ProductionEnv");
        Metadata metadata = Metadata.builder(map).build();
        QueryOnMetadata query = QueryOnMetadata.builder(metadata).put("*", "ProductionEnv").build();
        Assertions.assertTrue(metadata.matchesByAster(query, "profile"));
    }

    @Test
    public void test_matchesIndividually() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("profile", "ProductionEnv");
        Metadata metadata = Metadata.builder(map).build();
        QueryOnMetadata query = QueryOnMetadata.builder(metadata).build();
        Assertions.assertTrue(metadata.matchesIndividually(query, "profile"));
    }

    @Test
    public void test_parseURLQuery() {
        String query = "q=katalon&dfe=piiipfe&cxw=fcfw";
        List<NameValuePair> pairs = Metadata.parseURLQuery(query);
        Assertions.assertTrue(pairs.stream().anyMatch(nvp -> nvp.getName().equals("q")));
        Assertions.assertEquals(Collections.singletonList("katalon"),
                pairs.stream()
                        .filter(nvp -> nvp.getName().equals("q"))
                        .map(NameValuePair::getValue)
                        .collect(Collectors.toList()));
    }

    @Test
    public void test_size() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("key", "value");
        Metadata metadata = Metadata.builder(map).build();
        Assertions.assertEquals(1, metadata.size());
    }

    @Test
    public void test_toURLAsString() throws MalformedURLException, MaterialstoreException {
        String source = "https://docs.oracle.com/javase/7/docs/api/java/nio/file/Path.html#resolve(java.lang.String)";
        String sourceWithoutFragment = source.substring(0, source.indexOf("#"));
        URL url = new URL(source);
        Metadata metadata = Metadata.builder(url).build();
        System.out.println(metadata.toURL().toString());
        assert metadata.toURL().toString().equals(sourceWithoutFragment);
        assert metadata.toURLAsString().equals(source);
    }

    /**
     * In the string representation, the keys should be sorted
     */
    @Test
    public void test_toSimplifiedJson() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("b", "B");
        map.put("a", "A");
        Metadata metadata = Metadata.builder(map).build();
        String s = metadata.toSimplifiedJson();
        System.out.println(s);
        Assertions.assertEquals("{\"a\":\"A\", \"b\":\"B\"}", metadata.toSimplifiedJson());
    }

    @Test
    public void test_toSimplifiedJson_should_have_redundant_whitespaces() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(3);
        map.put("a", "A");
        map.put("b", "B");
        map.put("c", "C");
        Metadata metadata = Metadata.builder(map).build();
        String s = metadata.toSimplifiedJson();
        System.out.println(JsonUtil.prettyPrint(s));
        Assertions.assertEquals("{\"a\":\"A\", \"b\":\"B\", \"c\":\"C\"}", metadata.toSimplifiedJson());
    }

    @Test
    public void test_toTemplateModel() throws MalformedURLException {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8#content");
        Metadata metadata = Metadata.builder(url).build();
        System.out.println(JsonUtil.prettyPrint(metadata.toJson()));
        Map<String, Object> model = metadata.toTemplateModel();
        Assertions.assertEquals("https", ((Map) model.get("URL.protocol")).get("value"));
        Assertions.assertEquals("baeldung.com", ((Map) model.get("URL.host")).get("value"));
        Assertions.assertEquals("80", ((Map) model.get("URL.port")).get("value"));
        Assertions.assertEquals("/articles", ((Map) model.get("URL.path")).get("value"));
        Assertions.assertEquals("topic=java&version=8", ((Map) model.get("URL.query")).get("value"));
        Assertions.assertEquals("content", ((Map) model.get("URL.fragment")).get("value"));
        List<String> keyList = new ArrayList<String>(model.keySet());
        Collections.sort(keyList);
        Assertions.assertEquals(6, keyList.size());
        Assertions.assertEquals(Arrays.asList("URL.fragment", "URL.host", "URL.path", "URL.port", "URL.protocol", "URL.query"), keyList);
    }

    @Test
    public void test_toURL() throws MalformedURLException, MaterialstoreException {
        URL url = new URL("https://baeldung.com/articles?topic=java&version=8#content");
        Metadata metadata = Metadata.builder(url).build();
        Assertions.assertNotNull(metadata);
        URL actual = metadata.toURL();
        URL expected = new URL("https://baeldung.com/articles?topic=java&version=8");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void test_toURL_with_port() throws MaterialstoreException, MalformedURLException {
        URL url = new URL("http://127.0.0.1:3080/index");
        Metadata metadata = Metadata.builder(url).build();
        Assertions.assertNotNull(metadata);
        URL actual = metadata.toURL();
        URL expected = new URL("http://127.0.0.1:3080/index");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void test_toURL_file_scheme() throws MaterialstoreException, MalformedURLException {
        URL url = new URL("file:/c/users/foo/temp/bar");
        Metadata metadata = Metadata.builder(url).build();
        Assertions.assertNotNull(metadata);
        URL actual = metadata.toURL();
        URL expected = new URL("file:/c/users/foo/temp/bar");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void test_toURL_data_scheme() throws MaterialstoreException, MalformedURLException {
        DataURLEnabler.enableDataURL();
        String data = "data:text/html,%3Ch1%3EHello%2C%20World%21%3C%2Fh1%3E";
        URL url = new URL(data);
        Metadata metadata = Metadata.builder(url).build();
        Assertions.assertNotNull(metadata);
        URL actual = metadata.toURL();
        URL expected = new URL(data);
        Assertions.assertEquals(expected, actual);
    }

}

package freemarker_template.com.kazurayam.materialstore.reduce.differ;

import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.reduce.differ.AbstractTextDiffer;
import com.kazurayam.materialstore.report.FreeMarkerConfigurator;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MarkupSegmentsTest {

    private static final Path outputDir =
            Paths.get(".")
                    .resolve("build/tmp/testOutput")
                    .resolve(MarkupSegmentsTest.class.getName());
    private static Store store;
    private static Configuration cfg;
    private static final String TEMPLATE_PATH =
            "com/kazurayam/materialstore/reduce/differ/MarkupSegmentsTest.ftlh";

    @BeforeAll
    public static void beforeAll() {
        try {
            store = Stores.newInstance(outputDir.resolve("store"));
            cfg = FreeMarkerConfigurator.configureFreeMarker(store);
        } catch (MaterialstoreException e) {
            throw new IllegalStateException(e);
        }
    }

    public String process(List<String> segments) {
        Map<String, Object> model = new HashMap<>();
        model.put("OLD_TAG", AbstractTextDiffer.OLD_TAG);
        model.put("NEW_TAG", AbstractTextDiffer.NEW_TAG);
        model.put("segments", segments);
        Template template;
        try {
            template = cfg.getTemplate(TEMPLATE_PATH);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        Writer sw = new StringWriter();
        try {
            template.process(model, sw);
        } catch (IOException | TemplateException e) {
            throw new IllegalStateException(e);
        }
        //
        return sw.toString();
    }

    @Test
    public void test_smoke() throws MaterialstoreException {
        List<String> segments = Arrays.asList("foo", "bar");
        String actual = process(segments);
        assertNotNull(actual);
        // System.out.println(actual);
    }

    @Test
    public void test_inserted() {
        List<String> segments =
                Arrays.asList(
                        "  src: url(\"./fonts/bootstrap-icons.",
                        AbstractTextDiffer.NEW_TAG,
                        "woff2?30af91bf14e37666a085fb8a161ff36d\"",
                        AbstractTextDiffer.NEW_TAG,
                        ") format(\"woff2\"),");
        String expected = "<span class=\"blob-code-inner\"><span class=\"unchanged\">  src: url(&quot;./fonts/bootstrap-icons.</span><span class=\"insertion\">woff2?30af91bf14e37666a085fb8a161ff36d&quot;</span><span class=\"unchanged\">) format(&quot;woff2&quot;),</span></span>";
        String actual = process(segments);
        assertEquals(expected, actual);
    }
}


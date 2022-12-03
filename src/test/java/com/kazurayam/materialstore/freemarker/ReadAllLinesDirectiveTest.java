package com.kazurayam.materialstore.freemarker;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReadAllLinesDirectiveTest extends TestBase {

    public ReadAllLinesDirectiveTest() throws IOException {
        super();
    }

    @Test
    public void test_execute() throws IOException, TemplateException {
        /* Get the template (uses cache internally) */
        Template temp = cfg.getTemplate("readAllLinesDemo.ftlh");

        /* Merge data-model with template */
        Writer out = new StringWriter();
        temp.process(model, out);

        String output = out.toString();
        assertNotNull(output);
        assertTrue(output.contains("<tr><td>0</td><td>publishedDate,uri,title,link,description,author</td></tr>"));
        System.out.println(output);
    }
}

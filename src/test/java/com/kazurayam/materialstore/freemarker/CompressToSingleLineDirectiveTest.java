package com.kazurayam.materialstore.freemarker;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompressToSingleLineDirectiveTest extends TestBase {

    public CompressToSingleLineDirectiveTest() throws IOException {
        super();
    }

    @Test
    public void test_execute() throws IOException, TemplateException {
        /* set data into the model */
        List<String> segments = Arrays.asList(
                "    {\"cat\":  \"Nikolai, Marcus and Ume\",\n",
                "     \"greeting\":  \"Hello, world!\"}     \n");
        model.put("segments", segments);

        /* Get the template (uses cache internally) */
        Template temp = cfg.getTemplate("compressToSingleLineDemo.ftlh");

        /* Merge data-model with template */
        Writer out = new StringWriter();
        temp.process(model, out);

        String output = out.toString();
        assertNotNull(output);

        System.out.println("---------------------");
        System.out.println(output);
        System.out.println("---------------------");

        BufferedReader br = new BufferedReader(new StringReader(output));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            lines.add(line);
        }
        assertEquals(1, lines.size(), "should be single line");
        assertTrue(lines.get(0).startsWith("<span"),   "^\\s+ should be trimmed");
        assertTrue(output.contains("<span class=\"nochange\">    {&quot;cat"),
                "indent of text inside <span> tags should be preserved");
    }

}

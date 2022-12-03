package com.kazurayam.materialstore.freemarker;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UpperCaseDirectiveTest extends TestBase {

    public UpperCaseDirectiveTest() throws IOException {
        super();
    }

    @Test
    public void test_execute() throws IOException, TemplateException {
        /* set data into the model */
        model.put("name", "Nikolai");

        /* Get the template (uses cache internally) */
        Template temp = cfg.getTemplate("uppercaseDemo.ftlh");

        /* Merge data-model with template */
        Writer out = new StringWriter();
        temp.process(model, out);

        String output = out.toString();
        assertNotNull(output);
        System.out.println(output);
        assertTrue(output.contains("HELLO, NIKOLAI!"));
    }

}

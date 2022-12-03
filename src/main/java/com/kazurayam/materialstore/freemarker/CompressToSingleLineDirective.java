package com.kazurayam.materialstore.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.Map;

/**
 *
 */
public class CompressToSingleLineDirective implements TemplateDirectiveModel {

    public void execute(Environment env,
                        Map params,
                        TemplateModel[] loopVars,
                        TemplateDirectiveBody body) throws TemplateException, IOException {
        // Check if no parameters wore given
        if (!params.isEmpty()) {
            throw new TemplateModelException(
                    "This directive doesn't allow parameters.");
        }
        if (loopVars.length != 0) {
            throw new TemplateModelException(
                    "This directive doesn't allow loop variables."
            );
        }
        // If there is non-empty nexted content
        if (body != null) {
            // Executes the nexted body.
            // Same as <#nexted> in FTL, except that
            // we use our own writer instead of the current output writer.
            BitGreedyCompressor compressor = new BitGreedyCompressor(env.getOut());
            body.render(compressor);
            compressor.flush();
            compressor.close();
        } else {
            throw new RuntimeException("missing body");
        }
    }

    /**
     * will trim the following fragments out of the body.
     *
     * 1. Leading spaces (^\s+)
     * 2. Trailing spaces (\s+$)
     * 3. Line breaks (\n|\r)
     *
     * In other words, whitespaces in between printable characters will be preseved.
     * For example,
     *     `<span>    Hello, World!    </span>`
     * will be unchanged, while
     *     `    <span>Hello, world!</span>    \n`
     * will be compressed into
     *     `<span>Hello, world!</span>`.
     */
    private static class BitGreedyCompressor extends Writer {

        private final StringBuilder buffer = new StringBuilder();
        private final Writer out;

        BitGreedyCompressor(Writer out) {
            this.out = out;
        }

        public void write(char[] cbuf, int off, int len) throws IOException {
            buffer.append(cbuf, off, len);
        }

        public void flush() throws IOException {
            String result = compress(buffer.toString());
            out.write(result);
            out.flush();
        }

        public void close() throws IOException {
            out.close();
        }

        private String compress(String source) throws IOException {
            StringBuilder result = new StringBuilder();
            BufferedReader br = new BufferedReader(new StringReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String s = line.replaceAll("^\\s+|\\s+$", "");
                if (s.length() > 0) {
                    result.append(s);
                }
            }
            return result.toString();
        }

    }
}

package com.kazurayam.materialstore.dot;

public abstract class AbstractGraphNode {

    abstract String toGraphNode();

    protected String escapeHTML(String str) {
        return str
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;")
                .replace("<", "&lt;")
                .replace(">","&gt;");
    }

    protected String insertBR(String str) {
        return str.replace(", ", ",<BR/> ");
    }

}

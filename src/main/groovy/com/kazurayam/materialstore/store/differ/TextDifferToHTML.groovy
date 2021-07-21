package com.kazurayam.materialstore.store.differ

import com.github.difflib.text.DiffRow
import com.github.difflib.text.DiffRowGenerator
import com.kazurayam.materialstore.store.Differ
import com.kazurayam.materialstore.store.Material
import groovy.xml.MarkupBuilder

import java.nio.charset.Charset
import java.nio.file.Path
import java.util.function.Function
import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 * compiles a HTML report of diff of 2 text files
 * presents the diff information in a HTML like the GitHub History split view.
 *
 * uses java-diff-utils on GitHub
 * https://github.com/java-diff-utils/java-diff-utils
 * to make diff of 2 texts
 */
// A problem: how to deal with a long line without white spaces
//  <script  src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-datepicker/1.6.4/js/bootstrap-datepicker.min.js"></script>

class TextDifferToHTML extends AbstractTextDiffer implements Differ {

    public static final String OLD_TAG = "!_~_!"
    public static final String NEW_TAG = "!#~#!"

    TextDifferToHTML() {}

    TextDifferToHTML(Path root) {
        super(root)
    }

    @Override
    void setRoot(Path root) {
        super.setRoot(root)
    }

    @Override
    String makeContent(Path root, Material original, Material revised, Charset charset) {
        String originalText = readMaterial(root, original, charset)
        String revisedText = readMaterial(root, revised, charset)

        //build simple lists of the lines of the two text files
        List<String> originalLines = readAllLines(originalText);
        List<String> revisedLines = readAllLines(revisedText);

        // Compute the difference between two texts and print it in humann-readable markup style
        DiffRowGenerator generator =
                DiffRowGenerator.create()
                        .showInlineDiffs(true)
                        .inlineDiffByWord(true)
                        .oldTag({ f -> OLD_TAG } as Function)
                        .newTag({ f -> NEW_TAG } as Function)
                        .lineNormalizer({str ->
                                str.replaceAll("&lt;", "<")
                                        .replaceAll("&gt;",">")
                                        .replaceAll("&quot;", "\"")
                                        .replaceAll("&apos;", "\'")
                                        .replaceAll("&amp;", "&")


                        })
                        .build()

        List<DiffRow> rows = generator.generateDiffRows(originalLines, revisedLines)

        List<DiffRow> insertedRows =
                rows.stream().filter({ DiffRow dr ->
                    dr.getTag() == DiffRow.Tag.INSERT
                }).collect(Collectors.toList())

        List<DiffRow> deletedRows =
                rows.stream().filter({ DiffRow dr ->
                    dr.getTag() == DiffRow.Tag.DELETE
                }).collect(Collectors.toList())

        List<DiffRow> changedRows =
                rows.stream().filter({ DiffRow dr ->
                    dr.getTag() == DiffRow.Tag.CHANGE
                }).collect(Collectors.toList())

        List<DiffRow> equalRows =
                rows.stream().filter({ DiffRow dr ->
                    dr.getTag() == DiffRow.Tag.EQUAL
                }).collect(Collectors.toList())

        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        mb.html(lang:"en") {
            head() {
                meta(charset: "utf-8")
                title("TextDifferToHTML output")
                style(getStyle())
            }
            body() {
                div(id: "container") {
                    div(id: "inputs") {
                        h1("Original")
                        dl() {
                            dt("URL")
                            dd() {
                                a(href: "../../../" + original.getRelativeURL(),
                                        target: "Original",
                                        original.getRelativeURL())
                            }
                            dt("metadata")
                            dd(original.getIndexEntry().getMetadata().toString())
                        }
                        h1("Revised")
                        dl() {
                            dt("URL")
                            dd() {
                                a(href: "../../../" + revised.getRelativeURL(),
                                        target: "Revised",
                                        revised.getRelativeURL())
                            }
                            dt("metadata")
                            dd(revised.getIndexEntry().getMetadata().toString())
                        }
                    }
                    h2(id: "decision", ((equalRows.size() < rows.size()) ? 'are DIFFERENT' : 'are EQUAL'))
                    h3("rows")
                    ul(id: "stats") {
                        li() {
                            span("total : ")
                            span(rows.size())
                        }
                        li() {
                            span("inserted : ")
                            span(insertedRows.size())
                        }
                        li() {
                            span("deleted : ")
                            span(deletedRows.size())
                        }
                        li() {
                            span("changed : ")
                            span(changedRows.size())
                        }
                        li() {
                            span("equal : ")
                            span(equalRows.size())
                        }
                    }
                    table() {
                        colgroup() {
                            col(width:"44")
                            col()
                            col()
                        }
                        thead() {
                            tr() {
                                th("")
                                th("original")
                                th("revised")
                            }
                        }
                        tbody() {
                            rows.eachWithIndex { DiffRow row, index ->
                                tr() {
                                    th(class: getClassOfDiffRow(row), index + 1)
                                    td(class: getClassOfDiffRow(row)) {
                                        span(class:"blob-code-inner") {
                                            List<String> segments = splitStringWithOldNewTags(row.getOldLine())
                                            markupSegments(mb, segments)
                                        }
                                    }
                                    td(class: getClassOfDiffRow(row)) {
                                        span(class:"blob-code-inner") {
                                            List<String> segments = splitStringWithOldNewTags(row.getNewLine())
                                            markupSegments(mb, segments)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return sw.toString()
    }

    private static String getStyle() {
        return """
* {
    box-sizing: border-box;
}
body {
    font-family: ui-monospace,SFMono-Regular,SZ Mono,
        Menlo,Consolas,Liberation Mono,monospace;
    font-size: 12px;
    line-height: 20px;
}
div#container {
    max-width: 1280px;
    margin-left: auto;
    margin-right: auto;
}
table {
    table-layout: fixed;
    border-collapse: collapse;
    border-spacing: 0;
    border: 1px solid #ccc;
    width: 100%;
}
td, th {
    font-size: 12px;
    border-right: 1px solid #ccc;
    display: table-cell;
}
th {
    border-bottom: 1px solid #ccc;
}
.blob-code-inner {
    word-wrap: break-word;
    white-space: pre-wrap;
}
.code-change {
    background-color: #dbedff;
}
.code-delete {
    background-color: #ffeef0;
}
.code-equal {
    background-color: #ffffff;
}
.code-insert {
    background-color: #e6ffec;
}
.deletion {
    background-color: #ffdce0;
}
.insertion {
    background-color: #ccffd8;
}
"""
    }


    /**
     * "Java Split String and Keep Delimitiers"
     * https://www.baeldung.com/java-split-string-keep-delimiters
     * @param line
     * @param clazz
     * @return
     */
    private static final Pattern SPLITTER =
            Pattern.compile("((?=${OLD_TAG})|(?<=${OLD_TAG})|(?=${NEW_TAG})|(?<=${NEW_TAG}))")

    static List<String> splitStringWithOldNewTags(String line, clazz="pl") {
        List<String> segments = SPLITTER.split(line) as List
        return segments
    }


    static final String CLASS_TD_CHANGE = "code-change"
    static final String CLASS_TD_DELETE = "code-delete"
    static final String CLASS_TD_EQUAL  = "code-equal"
    static final String CLASS_TD_INSERT = "code-insert"

    static String getClassOfDiffRow(DiffRow row) {
        switch (row.getTag()) {
            case DiffRow.Tag.CHANGE:
                return CLASS_TD_CHANGE
                break
            case DiffRow.Tag.DELETE:
                return CLASS_TD_DELETE
                break
            case DiffRow.Tag.EQUAL:
                return CLASS_TD_EQUAL
                break
            case DiffRow.Tag.INSERT:
                return CLASS_TD_INSERT
                break
            default:
                throw new IllegalArgumentException("unknown row.getTag()=${row.getTag()}")
        }
    }

    static void markupSegments(MarkupBuilder mb, List<String> segments) {
        nospace(mb)
        mb.span(class: "blob-code-inner") {
            boolean inOldTag = false
            boolean inNewTag = false
            segments.each { String segment ->
                switch (segment) {
                    case OLD_TAG:
                        inOldTag = !inOldTag
                        break
                    case NEW_TAG:
                        inNewTag = !inNewTag
                        break
                    default:
                        if (inOldTag) {
                            nospace(mb)
                            mb.span(class: "deletion", segment)
                        } else if (inNewTag) {
                            nospace(mb)
                            mb.span(class: "insertion", segment)
                        } else {
                            nospace(mb)
                            mb.span(class: "unchanged", segment)
                        }
                        break
                }
            }
        }
    }

    static void nospace(MarkupBuilder mb) {
        mb.metaClass.setAttribute(mb, "nospace", true)
    }

}

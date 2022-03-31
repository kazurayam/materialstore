package com.kazurayam.materialstore.reduce.differ

import com.github.difflib.text.DiffRow
import com.github.difflib.text.DiffRowGenerator
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.report.HTMLPrettyPrintingCapable
import groovy.xml.MarkupBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser

import java.nio.charset.Charset
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

final class TextDifferToHTMLMB extends AbstractTextDiffer implements Differ, HTMLPrettyPrintingCapable {

    private boolean prettyPrinting = false;

    TextDifferToHTMLMB() {}

    TextDifferToHTMLMB(Store store) {
        super(store)
    }

    @Override
    public void enablePrettyPrinting(boolean enabled) {
        this.prettyPrinting = enabled;
    }

    @Override
    public boolean isPrettyPrintingEnabled() {
        return prettyPrinting;
    }


    @Override
    void setStore(Store store) {
        super.setStore(store)
    }

    @Override
    TextDiffContent makeTextDiffContent(Store store,
                                        Material left, Material right,
                                        Charset charset) {
        String leftText = readMaterial(store, left, charset)
        String rightText = readMaterial(store, right, charset)

        //build simple lists of the lines of the two text files
        List<String> leftLines = readAllLines(leftText)
        List<String> rightLines = readAllLines(rightText)

        // Compute the difference between two texts and print it in human-readable markup style
        DiffRowGenerator generator =
                DiffRowGenerator.create()
                        .showInlineDiffs(true)
                        .inlineDiffByWord(true)
                        .oldTag({ f -> OLD_TAG } as Function)
                        .newTag({ f -> NEW_TAG } as Function)
                        .lineNormalizer({ str ->
                            str.replaceAll("&lt;", "<")
                                    .replaceAll("&gt;", ">")
                                    .replaceAll("&quot;", "\"")
                                    .replaceAll("&apos;", "\'")
                                    .replaceAll("&amp;", "&")


                        })
                        .build()

        List<DiffRow> rows = generator.generateDiffRows(leftLines, rightLines)

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
        mb.setDoubleQuotes(true)
        mb.html(lang: "en") {
            head() {
                meta(charset: "utf-8")
                title("TextDifferToHTML output")
                style(getStyle())
            }
            body() {
                div(id: "container") {
                    div(id: "decision") {
                        //
                        Double diffRatio = DifferUtil.roundUpTo2DecimalPlaces(
                                (insertedRows.size() +
                                        deletedRows.size() +
                                        changedRows.size()
                                ) * 100.0D / rows.size()
                        )
                        String ratio = DifferUtil.formatDiffRatioAsString(diffRatio)
                        h3() {
                            if (equalRows.size() < rows.size()) {
                                span("are DIFFERENT")
                                span(style: "margin-left: 20px;", "${ratio}")
                            } else {
                                span("are EQUAL")
                            }
                        }
                        dl() {
                            dt("rows")
                            dd() {
                                ul(id: "rows-stats") {
                                    li() {
                                        span("total :")
                                        span(rows.size())
                                    }
                                    li() {
                                        span(class: "code-insert", "inserted :")
                                        span(class: "code-insert", insertedRows.size())
                                    }
                                    li() {
                                        span(class: "code-delete", "deleted :")
                                        span(class: "code-delete", deletedRows.size())
                                    }
                                    li() {
                                        span(class: "code-change", "changed :")
                                        span(class: "code-change", changedRows.size())
                                    }
                                    li() {
                                        span("equal :")
                                        span(equalRows.size())
                                    }
                                }
                            }
                        }
                    }
                    table(id: "split-diff") {
                        colgroup() {
                            col(width: "66")
                            col()
                            col()
                        }
                        thead() {
                            tr() {
                                th("")
                                th("Left")
                                th("Right")
                            }
                            tr() {
                                th("Material")
                                td() {
                                    a(href: "../../../" + left.getRelativeURL(),
                                            target: "Left",
                                            left.getRelativeURL())
                                }
                                td() {
                                    a(href: "../../../" + right.getRelativeURL(),
                                            target: "Right",
                                            right.getRelativeURL())
                                }
                            }
                            tr() {
                                th("FileType")
                                td() {
                                    span(left.getIndexEntry().getFileType().getExtension())
                                }
                                td() {
                                    span(left.getIndexEntry().getFileType().getExtension())
                                }
                            }
                            tr() {
                                th("Metadata")
                                td() {
                                    span(left.getIndexEntry().getMetadata().toString())
                                }
                                td() {
                                    span(right.getIndexEntry().getMetadata().toString())
                                }
                            }
                            tr() {
                                th("Source")
                                td() {
                                    URL url = left.getIndexEntry().getMetadata().toURL()
                                    if (url != null) {
                                        a(href: url.toExternalForm(), target: "Left", url.toExternalForm())
                                    }
                                }
                                td() {
                                    URL url = right.getIndexEntry().getMetadata().toURL()
                                    if (url != null) {
                                        a(href: url.toExternalForm(), target: "Right", url.toExternalForm())
                                    }
                                }
                            }
                            tr() {
                                th("----")
                                td()
                                td()
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

        String html = sw.toString();
        if (isPrettyPrintingEnabled()) {
            Document doc = Jsoup.parse(html, "", Parser.htmlParser());
            doc.outputSettings().indentAmount(2);
            html = doc.toString();
        }

        TextDiffContent textDiffContent =
                new TextDiffContent.Builder(html)
                        .inserted(insertedRows.size())
                        .deleted(deletedRows.size())
                        .changed(changedRows.size())
                        .equal(equalRows.size())
                        .build()
        return textDiffContent
    }


    //-----------------------------------------------------------------

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
thead tr {
    border-bottom: 1px solid #ccc;
}
td, th {
    font-size: 12px;
    border-right: 1px solid #ccc;
    display: table-cell;
    overflow-wrap: break-word;
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






    static void markupSegments(MarkupBuilder mb, List<String> segments) {
        //nospace(mb)
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
                            //nospace(mb)
                            mb.span(class: "deletion", segment)
                        } else if (inNewTag) {
                            //nospace(mb)
                            mb.span(class: "insertion", segment)
                        } else {
                            //nospace(mb)
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

package com.kazurayam.materialstore.store.reporter

import com.kazurayam.materialstore.store.JobName
import com.kazurayam.materialstore.store.Material
import groovy.json.JsonOutput
import groovy.xml.MarkupBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path
import java.nio.charset.StandardCharsets

class MaterialsBasicReporter {

    private static final Logger logger = LoggerFactory.getLogger(MaterialsBasicReporter.class)

    private final Path root_

    private final JobName jobName_

    MaterialsBasicReporter(Path root, JobName jobName) {
        Objects.requireNonNull(root)
        Objects.requireNonNull(jobName)
        if (! Files.exists(root)) {
            throw new IllegalArgumentException("${root} is not present")
        }
        this.root_ = root
        this.jobName_ = jobName
    }

    int reportList(List<Material> materials, String reportFileName = "list.html") {
        Objects.requireNonNull(materials)
        Objects.requireNonNull(reportFileName)
        //
        Path reportFile = root_.resolve(reportFileName)
        //
        int materialsCount = 0
        //
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        mb.html(lang: "en") {
            head() {
                meta(charset: "utf-8")
                meta(name: "viewport", content: "width=device-width, initial-scale=1")
                mkp.comment("Bootstrap")
                link(href: "https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta1/dist/css/bootstrap.min.css",
                        rel: "stylesheet",
                        integrity: "sha384-giJF6kkoqNQ00vy+HMDP7azOuL0xtbfIcaT9wjKHr8RbDVddVHyTfAAsrekwKmP1",
                        crossorigin: "anonymous")
                style(getStyle())
                title(jobName_.toString())
            }
            body() {
                div(class: "container") {
                    h1(jobName_.toString())
                    div(class: "accordion",
                            id:"materials-content") {
                        materials.eachWithIndex { Material material, int index ->
                            materialsCount += 1
                            div(id: "accordion${index+1}",
                                    class: "accordion-item") {
                                h2(id: "heading${index+1}",
                                        class: "accordion-header") {
                                    button(class: "accordion-button",
                                            type: "button",
                                            "data-bs-toggle": "collapse",
                                            "data-bs-target": "#collapse${index+1}",
                                            "area-expanded": "false",
                                            "aria-controls": "collapse${index+1}") {
                                        span(class: "fileType",
                                                material.getIndexEntry().getFileType().getExtension())
                                        span(class: "metadata",
                                                material.getIndexEntry().getMetadata().toString())
                                    }
                                }
                                div(id: "collapse${index+1}",
                                        class: "accordion-collapse collapse",
                                        "aria-labelledby": "heading${index+1}",
                                        "data-bs-parent": "#materials-content") {
                                    mb.div(class: "accordion-body") {
                                        makeAccordionBody(root_, mb, material)
                                    }
                                }
                            }
                        }
                    }
                }
                mkp.comment("Bootstrap")
                script(src: "https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta1/dist/js/bootstrap.bundle.min.js",
                        integrity: "sha384-ygbV9kiqUc6oa4msXn9868pTtWMgiQaeYH7/t7LECLbyPA2x65Kgf80OJFdroafW",
                        crossorigin: "anonymous", "")
            }
        }
        reportFile.toFile().text = "<!doctype html>\n" + sw.toString()
        return materialsCount
    }

    private static void makeAccordionBody(Path root, MarkupBuilder mb, Material material) {
        mb.div(class: "show-detail") {
            dl(class: "detail") {
                dt("URL")
                dd() {
                    a(href: material.getRelativeURL(),
                            target: "material",
                            material.getRelativeURL())
                }
                dt("fileType")
                dd(material.getIndexEntry().getFileType().getExtension())
                //
                String s = JsonOutput.prettyPrint(material.getIndexEntry().getMetadata().toString())
                dt("metadata")
                dd(s)
            }
            if (material.isImage()) {
                img(class: "img-fluid d-block w-75 centered",
                        alt: "image-material",
                        src: material.getRelativeURL())
            } else if (material.isText()) {
                List<String> lines =
                        Files.readAllLines(
                                material.toPath(root),
                                StandardCharsets.UTF_8)
                table(id: "text-content") {
                    colgroup() {
                        col(width: "44")
                        col()
                    }
                    thead() {
                        tr() {
                            th("line#")
                            th("content")
                        }
                    }
                    tbody() {
                        lines.eachWithIndex { String line, int index ->
                            tr() {
                                th(class: "code-equal", index+1)
                                td(class: "code-equal") {
                                    span(class: "blob-code-inner", line)
                                }
                            }
                        }
                    }
                }
            } else {
                logger.info("not a text, not an image. ${material.toString()}")
            }
        }
    }

    private static String getStyle() {
        return """
iframe {
    position: absolute;
    border: none;
    height: 100%;
    width: 100%
}
body {
    font-family: ui-monospace, SFMono-Regular,SZ Mono, Menlo, Consolas,Liberation Mono, monospace;
    font-size: 12px;
    line-height: 20px;
}
.show-detail {
    margin-top: 10px;
    margin-bottom: 40px;
}
dl dd {
    margin-left: 40px;
}
.metadata, .fileType {
    padding-top: 4px;
    padding-right: 20px;
    padding-bottom: 4px;
    padding-left: 4px;
    text-align: left;
}

.metadata {
    flex-basis: 80%;
}
.filetype {
    flex-basis: 10%;
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
.code-equal {
    background-color: #ffffff;
}

"""
    }
}

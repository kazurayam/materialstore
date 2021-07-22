package com.kazurayam.materialstore.store.reporter

import com.kazurayam.materialstore.store.DiffArtifact
import com.kazurayam.materialstore.store.DiffReporter
import com.kazurayam.materialstore.store.JobName
import com.kazurayam.materialstore.store.Material
import groovy.xml.MarkupBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path

import groovy.json.JsonOutput

class DiffReporterToHTML implements DiffReporter {

    private static final Logger logger = LoggerFactory.getLogger(DiffReporterToHTML.class)

    private final Path root_

    private final JobName jobName_

    DiffReporterToHTML(Path root, JobName jobName) {
        Objects.requireNonNull(root)
        Objects.requireNonNull(jobName)
        if (! Files.exists(root)) {
            throw new IllegalArgumentException("${root} is not present")
        }
        this.root_ = root
        this.jobName_ = jobName
    }

    @Override
    void reportDiffs(List<DiffArtifact> diffArtifacts, String reportFileName) {
        Objects.requireNonNull(diffArtifacts)
        Objects.requireNonNull(reportFileName)
        //
        Path reportFile = root_.resolve(reportFileName)
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
                            id: "diff-contents") {
                        diffArtifacts.eachWithIndex { DiffArtifact da, int index ->
                            div(id: "accordion${index+1}",
                                    class: "accordion-item") {
                                h2(id: "heading${index+1}",
                                        class: "accordion-header") {
                                    button(class: "accordion-button",
                                            type: "button",
                                            "data-bs-toggle": "collapse",
                                            "data-bs-target": "#collapse${index+1}",
                                            "area-expanded": "false",
                                            "aria-controls": "collapse${index+1}",
                                        "${da.getDescription()} ${da.getActual().getIndexEntry().getFileType().getExtension()}"
                                    )
                                }
                                div(id: "collapse${index+1}",
                                        class: "according-collapse collapse",
                                        "aria-labelledby": "heading${index+1}",
                                        "data-bs-parent": "#diff-contents"
                                ) {
                                    makeMaterialSubsection(mb, "expected", da.getExpected())
                                    makeMaterialSubsection(mb, "actual", da.getActual())
                                    makeMaterialSubsection(mb, "diff", da.getDiff())
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
    }

    private static void makeMaterialSubsection(MarkupBuilder mb, String name, Material material) {
        mb.div(class: "accordion-body") {
            h3(name)
            if (material.isImage()) {
                p() {
                    img(alt:name, src: material.getRelativeURL())
                }
            } else if (material.isText()) {
                ;
            } else {
                logger.warn("material.isImage() returned false and material.isText() returned false. What is this? ${material}")
            }
            dl(class:"detail") {
                dt("URL")
                dd() {
                    a(href: material.getRelativeURL(),
                            target: name,
                            material.getRelativeURL())
                }
                //
                dt("fileType")
                dd(material.getIndexEntry().getFileType().getExtension())
                //
                String s = JsonOutput.prettyPrint(material.getIndexEntry().getMetadata().toString())
                dt("metadata")
                dd(s)
            }
        }
    }

    private static String getStyle() {
        return """
"""
    }
}

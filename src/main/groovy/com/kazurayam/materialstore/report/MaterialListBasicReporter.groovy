package com.kazurayam.materialstore.report


import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.FileTypeDiffability
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.metadata.QueryOnMetadata
import groovy.xml.MarkupBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

final class MaterialListBasicReporter extends MaterialListReporter {

    private static final Logger logger = LoggerFactory.getLogger(MaterialListBasicReporter.class)

    private Store store_

    private JobName jobName_

    MaterialListBasicReporter(Store store, JobName jobName) {
        Objects.requireNonNull(store)
        Objects.requireNonNull(jobName)
        this.store_ = store
        this.jobName_ = jobName
    }

    /**
     * using Bootstrap 5
     *
     * @param materialList
     * @param reportFileName
     * @return
     */
    @Override
    Path report(MaterialList materialList, String reportFileName = "list.html") {
        Objects.requireNonNull(materialList)
        Objects.requireNonNull(reportFileName)
        //
        Path reportFile = store_.getRoot().resolve(reportFileName)
        //
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        mb.html(lang: "en") {
            head() {
                meta(charset: "utf-8")
                meta(name: "viewport", content: "width=device-width, initial-scale=1")
                mkp.comment("Bootstrap")
                link(href: "https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/css/bootstrap.min.css",
                        rel: "stylesheet",
                        integrity: "sha384-KyZXEAg3QhqLMpG8r+8fhAXLRk2vvoC2f3B09zVXn8CA5QIVfZOJ3BCsw2P0p/We",
                        crossorigin: "anonymous")
                style(ReporterHelper.loadStyleFromClasspath())
                title(jobName_.toString())
            }
            body() {
                div(class: "container") {
                    h1(class: "title", jobName_.toString()) {
                        button(class: "btn btn-secondary",
                                type: "button",
                                "data-bs-toggle":   "collapse",
                                "data-bs-target":   "#collapsingHeader",
                                "aria-expanded": "false",
                                "aria-controls": "collapsingHeader",
                                "About")
                    }
                    div(id: "collapsingHeader", class: "collapse header") {
                        dl() {
                            dt("Root path :")
                            dd(store_.getRoot().normalize().toString())
                            dt("JobName :")
                            dd(jobName_.toString())
                            dt("MaterialList specification")
                            dd() {
                                dl() {
                                    dt("JobTimestamp :")
                                    dd(materialList.getJobTimestamp().toString())
                                    dt("QueryOnMetadata :")
                                    dd() {
                                        materialList.getQueryOnMetadata().toSpanSequence(mb)
                                    }
                                    dt("FileType :")
                                    FileType fileType = materialList.getFileType()
                                    dd((fileType != FileType.NULL_OBJECT) ? fileType.getExtension() : "not specified")
                                }
                            }
                        }
                    }
                    div(class: "accordion", id: "accordionExample") {
                        materialList.eachWithIndex { Material material, int index ->
                            div(class: "accordion-item") {
                                h2(class: "accordion-header",
                                        id: "heading${index+1}") {
                                    button(class: "accordion-button",
                                            type: "button",
                                            "data-bs-toggle": "collapse",
                                            "data-bs-target": "#collapse${index + 1}",
                                            "area-expanded": "false",
                                            "aria-controls": "collapse${index + 1}") {
                                        span(class: "fileType",
                                                material.getIndexEntry().getFileType().getExtension())
                                        span(class: "metadata",
                                                material.getIndexEntry().getMetadata().toString())
                                    }
                                }
                                div(id: "collapse${index+1}",
                                        class: "accordion-collapse collapse",
                                        "aria-labelledby": "heading${index+1}",
                                        "data-bs-parent": "#accordionExample") {
                                    mb.div(class: "accordion-body") {
                                        makeAccordionBody(store_.getRoot(), mb, material,
                                                materialList.getQueryOnMetadata())
                                    }
                                }
                            }
                        }
                    }
                }
                mkp.comment("Bootstrap")
                script(src: "https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js",
                        integrity: "sha384-U1DAWAznBHeqEIlVSCgzq+c9gqGAJn5c/t99JyeKa9xxaYpSvHU5awsuZVVFIhvj",
                        crossorigin: "anonymous", "")
            }
        }
        reportFile.toFile().text = "<!doctype html>\n" + sw.toString()
        return reportFile
    }

    private static void makeAccordionBody(Path root, MarkupBuilder mb, Material material,
                                          QueryOnMetadata query) {
        mb.div(class: "show-detail") {
            dl(class: "detail") {
                dt("URL")
                dd() {
                    a(href: material.getRelativeURL(),
                            target: "material",
                            material.getRelativeURL())
                }
                dt("FileType")
                dd(material.getIndexEntry().getFileType().getExtension())
                //
                dt("Metadata")
                dd() {
                    material.getIndexEntry()
                            .getMetadata()
                            .toSpanSequence(mb, query)
                }
            }
            if (material.getDiffability() == FileTypeDiffability.AS_IMAGE) {
                img(class: "img-fluid d-block w-75 centered",
                        alt: "image-material",
                        src: material.getRelativeURL())
            } else if (material.getDiffability() == FileTypeDiffability.AS_TEXT) {
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

}
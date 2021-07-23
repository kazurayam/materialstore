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
                                        "${da.getDescription()}" +
                                                " ${da.getActual().getIndexEntry().getFileType().getExtension()}" +
                                                " Δ${da.getDiff().getIndexEntry().getMetadata().get("ratio")}%"
                                    )
                                }
                                div(id: "collapse${index+1}",
                                        class: "according-collapse collapse",
                                        "aria-labelledby": "heading${index+1}",
                                        "data-bs-parent": "#diff-contents"
                                ) {
                                    mb.div(class: "accordion-body") {
                                        makeModalSubsection(mb, da)
                                        makeMaterialSubsection(mb, "expected", da.getExpected())
                                        makeMaterialSubsection(mb, "actual", da.getActual())
                                        makeMaterialSubsection(mb, "diff", da.getDiff())
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
    }

    private static void makeModalSubsection(MarkupBuilder mb, DiffArtifact da) {
        Material actual = da.getActual()
        mb.div(class: "show-diff") {
            if (actual.isImage()) {
                // Show 3 images in a Modal
                mkp.comment("Button trigger modal")
                button(type: "button", class: "btn btn-primary",
                        "data-bs-toggle": "modal", "data-bs-target": "#imageModal",
                        "Show 3 images")
                mkp.comment("Modal to show 3 images: Expected/Diff/Actual")
                div(class: "modal fade", id:"imageModal", tabindex: "-1",
                        "aria-labelledby": "imageModalLabel", "aria-hidden": "true") {
                    div(class: "modal-dialog modal-fullscreen"){
                        div(class: "modal-content") {
                            div(class: "modal-header") {
                                h5(class: "modal-title",
                                        id: "imageModalLabel",
                                        da.getDescriptor()) {
                                    button(type: "button",
                                            class: "btn-close",
                                            "data-bs-dismiss": "modal",
                                            "aria-label": "Close",
                                            "")
                                }
                            }
                            div(class: "modal-body") {
                                mkp.comment("body")
                                div(id: "carouselExampleControls",
                                        class: "carousel slide",
                                        "data-bs-ride": "carousel") {
                                    div(class: "carousel-inner") {
                                        div(class: "carousel-item") {
                                            h3(class: "centered","Expected")
                                            img(class: "d-block w-75 centered",
                                                    alt: "expected",
                                                    src: da.getExpected()
                                                            .getRelativeURL())
                                        }
                                        div(class: "carousel-item active") {
                                            h3(class: "centered","Diff")
                                            img(class: "d-block w-75 centered",
                                                    alt: "diff",
                                                    src: da.getDiff()
                                                            .getRelativeURL())
                                        }
                                        div(class: "carousel-item") {
                                            h3(class: "centered","Actual")
                                            img(class: "d-block w-75 centered",
                                                    alt: "actual",
                                                    src: da.getActual()
                                                            .getRelativeURL())
                                        }
                                    }
                                    button(class: "carousel-control-prev",
                                            type: "button",
                                            "data-bs-target": "#carouselExampleControls",
                                            "data-bs-slide": "prev") {
                                        span(class: "carousel-control-prev-icon",
                                                "aria-hidden": "true","")
                                        span(class: "visually-hidden",
                                                "Previous")
                                    }
                                    button(class: "carousel-control-next",
                                            type: "button",
                                            "data-bs-target": "#carouselExampleControls",
                                            "data-bs-slide": "next") {
                                        span(class: "carousel-control-next-icon",
                                                "aria-hidden": "true","")
                                        span(class: "visually-hidden",
                                                "Next")
                                    }
                                }
                            }
                            div(class: "modal-footer") {
                                button(type: "button", class: "btn btn-secondary",
                                        "data-bs-dismiss": "modal", "Close")
                            }
                        }
                    }
                }
            } else if (actual.isText()) {
                mkp.comment("Button trigger modal")
                button(type: "button", class: "btn btn-primary",
                        "data-bs-toggle": "modal", "data-bs-target": "#textModal",
                        "Show texts diff")
                mkp.comment("Modal to show texts diff")
                div(class: "modal fade", id: "textModal", tabindex: "-1",
                        "aria-labelledby": "textModalLabel", "aria-hidden": "true") {
                    div(class: "modal-dialog modal-fullscreen") {
                        div(class: "modal-content") {
                            div(class: "modal-header") {
                                h5(class: "modal-title",
                                        id: "textModalLabel",
                                        da.getDescriptor()) {
                                    button(type: "button",
                                            class: "btn-close",
                                            "data-bs-dismiss": "modal",
                                            "aria-label": "Close",
                                            "")
                                }
                            }
                            div(class: "modal-body") {
                                mkp.comment("body")
                                iframe(src: da.getDiff().getRelativeURL(),
                                        title: "TextDiff", "")
                            }
                            div(class: "modal-footer") {
                                button(type: "button",
                                        class: "btn btn-secondary",
                                        "data-bs-dismiss": "modal",
                                        "Close")
                            }
                        }
                    }
                }
            } else {
                logger.warn("material.isImage() returned false and material.isText() returned false. What is this? ${material}")
            }
        }
    }

    private static void makeMaterialSubsection(MarkupBuilder mb, String name, Material material) {
        mb.div(class: "show-detail") {
            h3(name)
            dl(class: "detail") {
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
.centered {
    display: block;
    margin-left: auto;
    margin-right: auto;
    text-align: center;
}
.carousel-inner {
    background-color: #efefef;
}
.carousel-control-prev, .carousel-control-next {
    width: 12.5%
}
.modal-body iframe {
    position: absolute;
    border: none;
    height: 100%;
    width: 100%
}
"""
    }
}

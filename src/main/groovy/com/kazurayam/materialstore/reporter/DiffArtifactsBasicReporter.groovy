package com.kazurayam.materialstore.reporter

import com.kazurayam.materialstore.*
import com.kazurayam.materialstore.differ.DifferUtil
import groovy.xml.MarkupBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path

final class DiffArtifactsBasicReporter implements DiffReporter {

    private static final Logger logger = LoggerFactory.getLogger(DiffArtifactsBasicReporter.class)

    private Path root_

    private JobName jobName_

    private Double criteria_ = 0.0d

    DiffArtifactsBasicReporter(Path root, JobName jobName) {
        Objects.requireNonNull(root)
        Objects.requireNonNull(jobName)
        if (! Files.exists(root)) {
            throw new IllegalArgumentException("${root} is not present")
        }
        this.root_ = root
        this.jobName_ = jobName
    }

    @Override
    void setCriteria(Double criteria) {
        if (criteria < 0.0 || 100.0 < criteria) {
            throw new IllegalArgumentException("criteria(${criteria}) must be in the range of [0,100)")
        }
        this.criteria_ = criteria
    }

    @Override
    Path reportDiffs(DiffArtifacts diffArtifacts, String reportFileName = "index.html") {
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
                            dd(root_.normalize().toString())
                            dt("JobName :")
                            dd(jobName_.toString())
                            //
                            dt("Left MaterialList specification")
                            MaterialList left = diffArtifacts.getLeftMaterialList()
                            if (left != MaterialList.NULL_OBJECT) {
                                dd() {
                                    dl() {
                                        dt("JobTimestamp :")
                                        dd(left.getJobTimestamp().toString())
                                        dt("MetadataPattern :")
                                        dd() {
                                            left.getMetadataPattern().toSpanSequence(mb)
                                        }
                                        dt("FileType :")
                                        FileType fileType = left.getFileType()
                                        dd((fileType != FileType.NULL) ? fileType.getExtension() : "not specified")
                                    }
                                }
                            } else {
                                dd("not set")
                            }
                            //
                            dt("Right MaterialList specification")
                            MaterialList right = diffArtifacts.getRightMaterialList()
                            if (right != MaterialList.NULL_OBJECT) {
                                dd() {
                                    dl() {
                                        dt("JobTimestamp :")
                                        dd(right.getJobTimestamp().toString())
                                        dt("MetadataPattern :")
                                        dd() {
                                            right.getMetadataPattern().toSpanSequence(mb)
                                        }
                                        dt("FileType :")
                                        FileType fileType = right.getFileType()
                                        dd((fileType != FileType.NULL) ? fileType.getExtension() : "not specified")
                                    }
                                }
                            } else {
                                dd("not set")
                            }
                            //
                            dt("IgnoringMetadataKeys")
                            if (diffArtifacts.getIgnoringMetadataKeys() != IgnoringMetadataKeys.NULL_OBJECT) {
                                dd() {
                                    diffArtifacts.getIgnoringMetadataKeys().toSpanSequence(mb)
                                }
                            } else {
                                dd("not set")
                            }
                        }
                    }
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
                                            "aria-controls": "collapse${index+1}") {

                                        Double diffRatio = da.getDiffRatio()
                                        Boolean toBeWarned = decideToBeWarned(diffRatio, criteria_)

                                        String warningClass = getWarningClass(toBeWarned)
                                        span(class: "ratio ${warningClass}",
                                                "${DifferUtil.formatDiffRatioAsString(diffRatio)}%")
                                        span(class: "fileType",
                                                da.getRight().getIndexEntry().getFileType().getExtension())
                                        span(class: "description",
                                                da.getDescription())
                                    }
                                }
                                div(id: "collapse${index+1}",
                                        class: "according-collapse collapse",
                                        "aria-labelledby": "heading${index+1}",
                                        "data-bs-parent": "#diff-contents") {
                                    mb.div(class: "accordion-body") {
                                        makeModalSubsection(mb, da, index+1)
                                        //
                                        List<Object> context = [
                                                diffArtifacts.getLeftMaterialList().getMetadataPattern(),
                                                diffArtifacts.getRightMaterialList().getMetadataPattern(),
                                                diffArtifacts.getIgnoringMetadataKeys()
                                        ]
                                        makeMaterialSubsection(mb, "left", da.getLeft(), context)
                                        makeMaterialSubsection(mb, "right", da.getRight(), context)
                                        makeMaterialSubsection(mb, "diff", da.getDiff(), context)
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

    private static void makeModalSubsection(MarkupBuilder mb, DiffArtifact da, Integer seq) {
        Material right = da.getRight()
        mb.div(class: "show-diff") {
            if (right.isImage()) {
                String imageModalId = "imageModal${seq}"
                String imageModalTitleId = "imageModalLabel${seq}"
                String carouselId = "carouselControl${seq}"
                // Show 3 images in a Modal
                mkp.comment("Button trigger modal")
                button(type: "button", class: "btn btn-primary",
                        "data-bs-toggle": "modal",
                        "data-bs-target": "#${imageModalId}",
                        "Show diff in Modal")
                mkp.comment("Modal to show 3 images: Left/Diff/Right")
                div(class: "modal fade",
                        id:"${imageModalId}",
                        tabindex: "-1",
                        "aria-labelledby": "imageModalLabel", "aria-hidden": "true") {
                    div(class: "modal-dialog modal-fullscreen"){
                        div(class: "modal-content") {
                            div(class: "modal-header") {
                                h5(class: "modal-title",
                                        id: "${imageModalTitleId}") {
                                    span("${da.getDescriptor()} ${da.getFileTypeExtension()} ${da.getDiffRatioAsString()}%")
                                    button(type: "button",
                                            class: "btn-close",
                                            "data-bs-dismiss": "modal",
                                            "aria-label": "Close",
                                            "")
                                }
                            }
                            div(class: "modal-body") {
                                mkp.comment("body")
                                div(id: "${carouselId}",
                                        class: "carousel slide",
                                        "data-bs-ride": "carousel") {
                                    div(class: "carousel-inner") {
                                        div(class: "carousel-item") {
                                            h3(class: "centered","Left")
                                            img(class: "img-fluid d-block w-75 centered",
                                                    alt: "left",
                                                    src: da.getLeft()
                                                            .getRelativeURL())
                                        }
                                        div(class: "carousel-item active") {
                                            h3(class: "centered","Diff")
                                            img(class: "img-fluid d-block w-75 centered",
                                                    alt: "diff",
                                                    src: da.getDiff()
                                                            .getRelativeURL())
                                        }
                                        div(class: "carousel-item") {
                                            h3(class: "centered","Right")
                                            img(class: "img-fluid d-block w-75 centered",
                                                    alt: "right",
                                                    src: da.getRight()
                                                            .getRelativeURL())
                                        }
                                    }
                                    button(class: "carousel-control-prev",
                                            type: "button",
                                            "data-bs-target": "#${carouselId}",
                                            "data-bs-slide": "prev") {
                                        span(class: "carousel-control-prev-icon",
                                                "aria-hidden": "true","")
                                        span(class: "visually-hidden",
                                                "Previous")
                                    }
                                    button(class: "carousel-control-next",
                                            type: "button",
                                            "data-bs-target": "#${carouselId}",
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
            } else if (right.isText()) {
                String textModalId = "textModal${seq}"
                String textModalTitleId = "textModalLabel${seq}"
                mkp.comment("Button trigger modal")
                button(type: "button", class: "btn btn-primary",
                        "data-bs-toggle": "modal",
                        "data-bs-target": "#${textModalId}",
                        "Show diff in Modal")
                mkp.comment("Modal to show texts diff")
                div(class: "modal fade",
                        id: "${textModalId}",
                        tabindex: "-1",
                        "aria-labelledby": "textModalLabel", "aria-hidden": "true") {
                    div(class: "modal-dialog modal-fullscreen") {
                        div(class: "modal-content") {
                            div(class: "modal-header") {
                                h5(class: "modal-title",
                                        id: "${textModalTitleId}") {
                                    span("${da.getDescriptor()} ${da.getFileTypeExtension()} ${da.getDiffRatioAsString()}%")
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

    /**
     *
     * @param mb
     * @param name
     * @param material
     * @param context ["leftMetadataPattern": xxx, "rightMetadataPattern": xxx, "ignoringMetadataKeys": xxx]
     */
    private static void makeMaterialSubsection(MarkupBuilder mb, String name, Material material,
                                               List<Object> context) {
        mb.div(class: "show-detail") {
            h2(name)
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
                dt("metadata")
                //dd(material.getIndexEntry().getMetadata().toString())
                dd() {
                    material.getIndexEntry().getMetadata().toSpanSequence(
                            mb,
                            (MetadataPattern)context.get(0),
                            (MetadataPattern)context.get(1),
                            (IgnoringMetadataKeys)context.get(2)
                    )
                }
            }
        }
    }

    static Boolean decideToBeWarned(Double diffRatio, Double criteria) {
        return diffRatio > criteria
    }

    static String getWarningClass(boolean toBeWarned) {
        if (toBeWarned) {
            return "warning"
        } else {
            return ""
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
.description, .fileType, .ratio {
    padding-top: 4px;
    padding-right: 4px;
    padding-bottom: 4px;
    padding-left: 4px;
    text-align: left;
}
.ratio {
    flex-basis: 6%;
    text-align: right;
}
.filetype {
    flex-basis: 6%;
}
.description {
}
.warning {
    background-color: #e0ae00;
}
"""
    }
}

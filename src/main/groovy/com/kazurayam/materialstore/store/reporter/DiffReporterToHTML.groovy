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
                title("DiffReporterToHTML output")
                style(getStyle())
            }
            body() {
                div(id: "container") {
                    h1(jobName_.toString())
                    diffArtifacts.eachWithIndex { DiffArtifact da, int index ->
                        hr()
                        mb.div(class: "diff-artifact") {
                            h2("${da.getDescription()} ${da.getActual().getIndexEntry().getFileType().getExtension()}")
                            makeMaterialSubsection(mb, "expected", da.getExpected())
                            makeMaterialSubsection(mb, "actual", da.getActual())
                            makeMaterialSubsection(mb, "diff", da.getDiff())
                        }
                    }
                }
            }
        }
        reportFile.toFile().text = sw.toString()
    }

    private static void makeMaterialSubsection(MarkupBuilder mb, String name, Material material) {
        mb.div(class:"material") {
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
dl.detail {
    margin-left: 80px;
}
#container h1 {
    margin-bottom: 40px;
}
"""
    }
}

package com.kazurayam.materialstore.differ


import com.kazurayam.materialstore.resolvent.Artifact
import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.Jobber
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.metadata.Metadata

import freemarker.cache.ClassTemplateLoader
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import freemarker.template.Template

import java.nio.file.Path

/**
 * VoidDiffer does nothing
 */
class VoidDiffer implements Differ {

    private Path root
    private Configuration cfg

    VoidDiffer() {
        // FreeMarker Configuration
        cfg = new Configuration(Configuration.VERSION_2_3_31)
        // we will load FreeMarker templates from CLASSPATH
        cfg.setTemplateLoader(new ClassTemplateLoader(
                VoidDiffer.class.getClassLoader(),
                "freemarker_templates"
        ))
        // Recommended FreeMarker settings
        cfg.setDefaultEncoding("UTF-8")
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER)
        cfg.setLogTemplateExceptions(false)
        cfg.setWrapUncheckedExceptions(true)
        cfg.setFallbackOnNullLoopVariable(false)
    }

    @Override
    void setRoot(Path root) {
        this.root = root
    }

    @Override
    Artifact makeArtifact(Artifact artifact) {
        Objects.requireNonNull(artifact)
        Objects.requireNonNull(artifact.getLeft())
        Objects.requireNonNull(artifact.getRight())
        Material left = artifact.getLeft()
        Material right = artifact.getRight()
        //
        Map<String, Object> dataModel = new HashMap<>()
        dataModel.put("left", left.toString())
        dataModel.put("right", right.toString())
        // Get the template
        Template template = cfg.getTemplate(
                "com/kazurayam/materialstore/differ/VoidDifferTemplate.ftlh")

        // Merge data model with Template
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        Writer out = new OutputStreamWriter(baos)
        template.process(dataModel, out)
        out.close()

        byte[] diffData = baos.toByteArray()

        // materialize the byte[] into the store
        Metadata diffMetadata = Metadata.builder([
                "category": "diff",
                "left": left.getIndexEntry().getID().toString(),
                "right": right.getIndexEntry().getID().toString()])
                .build()
        assert root != null
        Jobber jobber = new Jobber(root, right.getJobName(), artifact.getResolventTimestamp())
        Material diffMaterial =
                jobber.write(diffData,
                        FileType.HTML,
                        diffMetadata,
                        Jobber.DuplicationHandling.CONTINUE)
        //
        Artifact result = new Artifact(artifact)
        result.setDiff(diffMaterial)
        return result
    }
}

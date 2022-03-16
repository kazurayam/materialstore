package com.kazurayam.materialstore.filesystem

interface TemplateReady {

    Map<String, Object> toTemplateModel()

    /**
     * for debugging purposes.
     *
     * turn the object returned by toTemplateModel() into a pretty-printed JSON text string
     */
    String toTemplateModelAsJSON()
}
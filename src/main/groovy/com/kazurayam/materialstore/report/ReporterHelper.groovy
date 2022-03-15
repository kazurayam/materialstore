package com.kazurayam.materialstore.report

import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.Metadata
import com.kazurayam.materialstore.filesystem.QueryOnMetadata
import com.kazurayam.materialstore.filesystem.metadata.IdentifyMetadataValues
import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys
import com.kazurayam.materialstore.filesystem.metadata.SemanticVersionAwareStringMatcher
import com.kazurayam.materialstore.util.JsonUtil
import groovy.xml.MarkupBuilder

import java.nio.file.Path
import java.util.regex.Matcher

class ReporterHelper {

    static final String CSS_PATH = "/com/kazurayam/materialstore/report/style.css"

    private ReporterHelper() {}

    /**
     * The "style.css" file is generated from the "style.scss" file.
     * The SCSS file is located in the src/main/resources/com/kazurayam/materialstore/reporter directory.
     * This method loads the style.css file from the runtime CLASSPATH.
     *
     * The SCSS file is compiled by the "Node scss" module and driven by IntelliJ IDEA + File Watcher.
     * See https://www.jetbrains.com/help/idea/transpiling-sass-less-and-scss-to-css.html#less_sass_scss_compiling_to_css
     *
     * @return a css content which should be embedded in the HTML file generated
     * by MaterialListBasicReporter and MProductGroupBasicReporter
     */
    static String loadStyleFromClasspath() {
        InputStream inputStream = ReporterHelper.class.getResourceAsStream(CSS_PATH)
        if (inputStream != null) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(inputStream, "UTF-8"))
            StringBuilder sb = new StringBuilder()
            String line
            while ((line = br.readLine()) != null) {
                sb.append(line)
                sb.append(System.lineSeparator())
            }
            return sb.toString()
        } else {
            throw new IllegalArgumentException("unabled to load ${CSS_PATH}")
        }
    }

    //-----------------------------------------------------------------


    static void toSpanSequence(QueryOnMetadata query, MarkupBuilder mb) {
        List<String> keyList = new ArrayList(query.keySet())
        Collections.sort(keyList)
        int count = 0
        mb.span("{")
        keyList.forEach( { String key ->
            if (count > 0) {
                mb.span(", ")
            }
            mb.span("\"${key.toString()}\":")
            mb.span("\"" + query.getAsString(key) + "\"")
            count += 1
        })
        mb.span("}")
    }

    static void toSpanSequence(IgnoreMetadataKeys ignoreMetadataKeys, MarkupBuilder mb) {
        List<String> list = new ArrayList<String>(ignoreMetadataKeys.keySet())
        Collections.sort(list)
        int count = 0
        mb.span("{")
        list.each {
            if (count > 0) {
                mb.span(", ")
            }
            mb.span(class: "ignored-key",
                    "\"" + JsonUtil.escapeAsJsonString(it) + "\"")
            count += 1
        }
        mb.span("}")
    }

    static void toSpanSequence(Metadata metadata, MarkupBuilder mb,
                        QueryOnMetadata query) {
        Objects.requireNonNull(metadata)
        Objects.requireNonNull(mb)
        Objects.requireNonNull(query)
        int count = 0
        List<String> keys = new ArrayList<String>(metadata.keySet())
        Collections.sort(keys)
        mb.span("{")
        keys.forEach {key ->
            if (count > 0) {
                mb.span(", ")
            }
            // make the <span> of the "key" part of an attribute of Metadata
            mb.span("\"${JsonUtil.escapeAsJsonString(key)}\"" + ":")

            // make the <span> of the "value" part of an attribute of Metadata
            String cssClassName = getCSSClassNameSolo(metadata, query, key)
            if (cssClassName != null) {
                mb.span(class: "matched-value",
                        "\"${JsonUtil.escapeAsJsonString(metadata.get(key))}\"")
            } else {
                mb.span("\"${JsonUtil.escapeAsJsonString(metadata.get(key))}\"")
            }
            count += 1
        }
        mb.span("}")
    }

    static String getCSSClassNameSolo(Metadata metadata, QueryOnMetadata query, String key) {
        if (matchesByAster(metadata, query, key) || matchesIndividually(metadata, query, key)) {
            return "matched-value"
        } else {
            return null
        }
    }

    static boolean matchesByAster(Metadata metadata, QueryOnMetadata query, String key) {
        return query.containsKey("*") &&
                query.get("*").matches(metadata.get(key))
    }

    static boolean matchesIndividually(Metadata metadata, QueryOnMetadata query, String key) {
        return query.containsKey(key) &&
                metadata.containsKey(key) &&
                query.get(key).matches(metadata.get(key))
    }


    static void toSpanSequence(Metadata metadata, MarkupBuilder mb,
                        QueryOnMetadata leftQuery,
                        QueryOnMetadata rightQuery,
                        IgnoreMetadataKeys ignoreMetadataKeys,
                        IdentifyMetadataValues identifyMetadataValues) {
        Objects.requireNonNull(metadata)
        Objects.requireNonNull(mb)
        Objects.requireNonNull(leftQuery)
        Objects.requireNonNull(rightQuery)
        Objects.requireNonNull(ignoreMetadataKeys)
        Objects.requireNonNull(identifyMetadataValues)
        int count = 0
        List<String> keys = new ArrayList<String>(metadata.keySet())
        Collections.sort(keys)
        mb.span("{")
        keys.forEach { key ->
            if (count > 0) {
                mb.span(", ")
            }

            // make the <span> of the "key" part of an attribute of Metadata
            if (ignoreMetadataKeys.contains(key)) {
                mb.span(class: "ignored-key", "\"${JsonUtil.escapeAsJsonString(key)}\"" + ":")
            } else {
                mb.span("\"${JsonUtil.escapeAsJsonString(key)}\"" + ":")
            }

            // make the <span> of the "value" part of an attribute of Metadata
            String cssClass = getCSSClassName(metadata, leftQuery, rightQuery,
                    key, identifyMetadataValues)
            if (cssClass != null) {
                mb.span(class: cssClass,
                        "\"${JsonUtil.escapeAsJsonString(metadata.get(key))}\"")
            } else {
                Matcher m = SemanticVersionAwareStringMatcher.straightMatcher(metadata.get(key))
                if (m.matches()) {
                    // <span>"/npm/bootstrap-icons@</span><span class='semantic-version'>1.5.0</span><span>/font/bootstrap-icons.css"</span>
                    mb.span("\"${JsonUtil.escapeAsJsonString(m.group(1))}")
                    mb.span(class: "semantic-version",
                            "${JsonUtil.escapeAsJsonString(m.group(2))}")
                    mb.span("${JsonUtil.escapeAsJsonString(m.group(4))}\"")
                } else {
                    // <span>xxxxxxx</span>
                    mb.span("\"${JsonUtil.escapeAsJsonString(metadata.get(key))}\"")
                }
            }
            //
            count += 1
        }
        mb.span("}")
    }

    static String getCSSClassName(Metadata metadata,
                                  QueryOnMetadata left,
                                  QueryOnMetadata right,
                                  String key,
                                  IdentifyMetadataValues identifyMetadataValues) {
        boolean canBePaired = canBePaired(metadata, left, right, key)
        boolean canBeIdentified = canBeIdentified(metadata, key, identifyMetadataValues)
        if (canBePaired) {
            return "matched-value"
        } else if (canBeIdentified) {
            return "identified-value"
        } else {
            return null
        }
    }

    static boolean canBePaired(Metadata metadata,
                               QueryOnMetadata left,
                               QueryOnMetadata right,
                               String key) {
        return left.containsKey("*")  && left.get("*").matches(metadata.get(key)) ||
                left.containsKey(key)      && left.get(key).matches(metadata.get(key)) ||
                right.containsKey("*") && right.get("*").matches(metadata.get(key)) ||
                right.containsKey(key)     && right.get(key).matches(metadata.get(key))
    }

    static boolean canBeIdentified(Metadata metadata, String key,
            IdentifyMetadataValues identifyMetadataValues) {

        return identifyMetadataValues.containsKey(key) &&
                identifyMetadataValues.matches(metadata)
    }
}

<#-- filesystem/MetadataTemplate.ftlh -->
<#macro toSpanSequence metadata>
    <span>{</span>
    <#list metadata as key, attribute>
            <span>"${attribute.key}":</span>
        <#if attribute.matchedByAster?? >
            <span class="matched-value">"${attribute.value}"</span>
        <#elseif attribute.matchedIndividually?? >
            <span class="matched-value">"${attribute.value}"</span>
        <#else>
            <span>"${attribute.value}"</span>
        </#if>
        <#sep>
            <span>, </span>
        </#sep>
    </#list>
    <span>}</span>
</#macro>

<#macro toSpanSequenceComplex metadata>
    <span>{</span>
    <#list metadata as key, attribute>
        <#-- make the <span> of the "key" part of an attribute of Metadata -->
        <#if attribute.ignoredByKey>
            <span class="ignored-key">${key}</span>
        <#else>
            <span>"${key}":</span>
        </#if>

        <#-- make the <span> of the "value" part of an attribute of Metadata -->
        <#if attribute.identifiedByValue>
            <span class="identified-value">"${attribute.value}"</span>
        <#elseif attribute.matchedByAster>
            <span class="matched-value">"${attribute.value}"</span>
        <#elseif attribute.matchedIndividually>
            <span class="matched-value">"${attribute.value}"</span>
        <#elseif attribute.semanticVersion??>

        <#else>
            <span>"${attribute.value}"</span>
        </#if>

        <#sep>
            <span>, </span>
        </#sep>
    </#list>
    <span>}</span>
</#macro>
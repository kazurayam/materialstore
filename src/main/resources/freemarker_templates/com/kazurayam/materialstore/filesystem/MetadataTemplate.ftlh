<#-- filesystem/MetadataTemplate.ftlh -->
<#macro toSpanSequence metadata query>
    <span>{</span>
    <#list metadata as key, attribute>
            <span>"${attribute.key}":</span>
        <#if attribute.matchedByAster?? >
            <span class='matched-value'>"${attribute.value}"</span>
        <#elseif attribute.matchedIndividually?? >
            <span class='matched-value'>"${attribute.value}"</span>
        <#else>
            <span>"${attribute.value}"</span>
        </#if>
        <#sep>
            <span>, </span>
        </#sep>
    </#list>
    <span>}</span>
</#macro>

<#macro toSpanSequenceDual metadata leftQuery rightQuery ignoreMetadataKeys identifyMeadataValues>
    <#assign count = 0 >
    <span>{</span>
    <#list metadata as key, attribute>
        <#if count gt 0>
            <span>, </span>
        </#if>>
        <#-- make the <span> of the "key" part of an attribute of Metadata -->
        <#if ignoreMetadataKeys.contains(key)>
            <span class='ignored-key'>${key}</span>
        <#else>
            <span>${key}</span>
        </#if>
        <#-- make the <span> of the "value" part of an attribute of Metadata -->

        <#assign count += 1>
    </#list>
    <span>}</span>
</#macro>
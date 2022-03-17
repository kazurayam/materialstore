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
<#-- QueryOnMetadataTemplate.ftlh -->
<#macro toSpanSequence keyValuePairs>
    <span>{</span>
    <#list keyValuePairs as key, value>
        <span>"${key}":</span>
        <span>"${value}"</span>
        <#sep><span>, </span></#sep>
    </#list>
    <span>}</span>
</#macro>
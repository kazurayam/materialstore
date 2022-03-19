<#-- filesystem/IgnoreMetadataKeysTemplate.ftlh -->
<#macro toSpanSequence keys>
    <span>[</span>
    <#list keys as key>
        <span class='ignored-key'>"${key}"</span>
        <#sep><span>, </span></#sep>
    </#list>
    <span>]</span>
</#macro>
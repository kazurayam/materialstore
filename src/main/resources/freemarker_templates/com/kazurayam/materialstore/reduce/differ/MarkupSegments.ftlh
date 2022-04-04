<#macro markupSegments segments>
    <#assign inOldTag = false>
    <#assign inNewTag = false>
    <#list segments>
        <span class="blob-code-inner">
        <#items as segment>
            <#if segment == OLD_TAG>
                <#assign inOldTag = !inOldTag>
            <#elseif segment == NEW_TAG>
                <#assign inNewTag = !inNewTag>
            <#else>
                <#if inOldTag>
                    <#assign clazz="deletion">
                <#elseif inNewTag>
                    <#assign clazz="insertion">
                <#else>
                    <#assign clazz="unchanged">
                </#if>
                <span class="${clazz}">${segment}</span>
            </#if>
        </#items>
        </span>
    </#list>
</#macro>
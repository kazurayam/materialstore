<#--<@renderQueryOnMetadataAsSpanSequence tokens=TOKENS/>-->

<#macro renderQueryOnMetadataAsSpanSequence tokens>
    <#list tokens as token>
        <#if token.class??>
                <span class="${token.class}">${token.text}</span>
        <#else>
                <span>${token.text}</span>
        </#if>
    </#list>
</#macro>
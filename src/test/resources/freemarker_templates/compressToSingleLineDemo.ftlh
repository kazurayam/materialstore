<#-- compressToSingleLineDemo.ftlh -->

<#-- sample markup text will be printed straight -->
<#--
<@sampleMarkup />
-->

<#-- custom directive name "compressToSingleLine" is defined as a shared variable. See TestVase.java. -->
<@compressToSingleLine>
  <@sampleMarkup/>
</@compressToSingleLine>

<#macro sampleMarkup>
    <#assign clazz="nochange">
    <#list segments>
      <span class="code">
          <#items as segment>
            <span class="${clazz}">${segment}</span>
          </#items>
      </span>
    </#list>
</#macro>
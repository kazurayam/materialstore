<#-- report/MaterialListBasicReporter/macros.ftl -->
<#macro makeAccordionBody material>
    <div class='show-detail'>
        <dl class='detail'>
            <dt>Material URL</dt>
            <dd>
                <a href='${material.relativeUrl}' target='material'>${material.relativeUrl}</a>
            </dd>
            <dt>FileType</dt>
            <dd>${material.fileType}</dd>
            <dt>Metadata</dt>
            <dd></dd>
        </dl>
    </div>
</#macro>
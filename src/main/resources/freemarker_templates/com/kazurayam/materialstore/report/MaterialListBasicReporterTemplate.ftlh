<#-- report/MaterialListBasicReporterTemplate.ftlh -->
<#import "../filesystem/QueryOnMetadataTemplate.ftlh" as QueryOnMetadata>
<#import "../filesystem/MetadataTemplate.ftlh" as Metadata>

<!doctype html>
<html lang='en'>
  <head>
    <meta charset='utf-8' />
    <meta name='viewport' content='width=device-width, initial-scale=1' /><!-- Bootstrap -->
    <link href='https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/css/bootstrap.min.css' rel='stylesheet' integrity='sha384-KyZXEAg3QhqLMpG8r+8fhAXLRk2vvoC2f3B09zVXn8CA5QIVfZOJ3BCsw2P0p/We' crossorigin='anonymous' />
    <style>${style}</style>
    <title>${jobName.toString()}</title>
  </head>
  <body>
    <div class='container'>
      <h1 class='title'>${title}
        <button class='btn btn-secondary' type='button' data-bs-toggle='collapse' data-bs-target='#collapsingHeader' aria-expanded='false' aria-controls='collapsingHeader'>About</button>
      </h1>
      <div id='collapsingHeader' class='collapse header'>
        <dl>
          <dt>Root path :</dt>
          <dd>${store.getRoot().normalize().toString()}</dd>
          <dt>JobName :</dt>
          <dd>${jobName.toString()}</dd>
          <dt>MaterialList specification</dt>
          <dd>
            <dl>
              <dt>JobTimestamp :</dt>
              <dd>${model.jobTimestamp}</dd>
              <dt>QueryOnMetadata :</dt>
              <dd>
                <@QueryOnMetadata.toSpanSequence keyValuePairs=model.queryOnMetadata />
              </dd>
            </dl>
          </dd>
        </dl>
      </div>
      <div class='accordion' id='accordionExample'>
        <#list model.materialList>
          <#items as material>
            <div class='accordion-item'>
              <h2 class='accordion-header' id='heading${material?counter}'>
                <button class='accordion-button' type='button' data-bs-toggle='collapse' data-bs-target='#collapse${material?counter}' area-expanded='false' aria-controls='collapse${material?counter}'>
                  <span class='fileType'>${material.fileType}</span>
                  <span class='metadata'>${material.metadataText}</span>
                </button>
              </h2>
              <div id='collapse${material?counter}' class='accordion-collapse collapse' aria-labelledby='heading${material?counter}' data-bs-parent='#accordionExample'>
                <div class='accordion-body'>
                  <@makeAccordionBody material=material query=model.queryOnMetadata />
                </div>
              </div>
            </div>
          </#items>
        </#list>
      </div>
    </div><!-- Bootstrap -->
    <script src='https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js' integrity='sha384-U1DAWAznBHeqEIlVSCgzq+c9gqGAJn5c/t99JyeKa9xxaYpSvHU5awsuZVVFIhvj' crossorigin='anonymous'></script>
  </body>
</html>



<#macro makeAccordionBody material query>
  <div class='show-detail'>
    <dl class='detail'>
      <dt>Material URL</dt>
      <dd>
        <a href='${material.relativeUrl}' target='material'>${material.relativeUrl}</a>
      </dd>
      <dt>FileType</dt>
      <dd>${material.fileType}</dd>
      <dt>Metadata</dt>
      <dd>
        <@Metadata.toSpanSequence metadata=material.metadata query=query/>
      </dd>
      <#if material.metadataURL??>
        <dt>Source URL</dt>
        <dd>
          <a href='${material.metadataURL}' target='source'>${material.metadataURL}</a>
        </dd>
      </#if>
    </dl>
    <#if material.diffability == "AS_IMAGE">
      <img class='img-fluid d-block w-75 centered' alt='image-material' src='${material.relativeURL}' />
    <#elseif material.diffability == "AS_TEXT">
      <table id='text-content'>
        <colgroup>
          <col width='44' />
          <col />
        </colgroup>
        <thead>
        <tr>
          <th>line#</th>
          <th>content</th>
        </tr>
        </thead>
        <tbody>
        <#assign x = 1 >
        <@readAllLines path="${material.relativeUrl}"; line>
          <tr>
            <th class='code-equal'>${x}</th>
            <td class='code-equal'>
              <span class='blob-code-inner'>${line}</span>
            </td>
          </tr>
          <#assign x++>
        </@readAllLines>
        </tbody>
      </table>
    <#else>
      <p>not a text, not an image.</p>
    </#if>
  </div>
</#macro>




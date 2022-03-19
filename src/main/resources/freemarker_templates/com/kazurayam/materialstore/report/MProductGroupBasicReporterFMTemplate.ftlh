<#-- report/MProductGroupBasicBasicReporterTemplate.ftlh -->
<#import "../filesystem/IgnoreMetadataKeysTemplate.ftlh" as IgnoreMetadataKeys>
<#import "../filesystem/QueryOnMetadataTemplate.ftlh" as QueryOnMetadata>
<#import "../filesystem/MetadataTemplate.ftlh" as Metadata>

<!doctype html>
<html lang='en'>
  <head>
    <meta charset='utf-8' />
    <meta name='viewport' content='width=device-width, initial-scale=1' /><!-- Bootstrap -->
    <link href='https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/css/bootstrap.min.css' rel='stylesheet' integrity='sha384-KyZXEAg3QhqLMpG8r+8fhAXLRk2vvoC2f3B09zVXn8CA5QIVfZOJ3BCsw2P0p/We' crossorigin='anonymous' />
    <style>${style}</style>
    <title>${model.jobName}</title>
  </head>
  <body>
    <div class='container'>
      <h1 class='title'>${title}
        <button class='btn btn-secondary' type='button' data-bs-toggle='collapse' data-bs-target='#collapsingHeader' aria-expanded='false' aria-controls='collapsingHeader'>About</button>
      </h1>
      <div id='collapsingHeader' class='collapse header'>
        <dl>
          <dt>Root path :</dt>
          <dd>${store}</dd>
          <dt>JobName :</dt>
          <dd>${model.jobName}</dd>
          <dt>Left MaterialList specification</dt>
          <#if model.materialList0?? >
            <dd>
              <dl>
                <dt>JobTimestamp :</dt>
                <dd>${model.materialList0.jobTimestamp}</dd>
                <dt>QueryOnMetadata :</dt>
                <dd>
                  <@QueryOnMetadata.toSpanSequence keyValuePairs=model.materialList0.queryOnMetadata />
                </dd>
              </dl>
            </dd>
          <#else>
            <dd>not set</dd>
          </#if>
          <dt>Right MaterialList specification</dt>
          <#if model.materialList1?? >
            <dd>
              <dl>
                <dt>JobTimestamp :</dt>
                <dd>${model.materialList1.jobTimestamp}</dd>
                <dt>QueryOnMetadata :</dt>
                <dd>
                  <@QueryOnMetadata.toSpanSequence keyValuePairs=model.materialList1.queryOnMetadata />
                </dd>
              </dl>
            </dd>
          <#else>
            <dd>not set</dd>
          </#if>
          <dt>IgnoreMetadataKeys</dt>
          <@IgnoreMetadataKeys.toSpanSequence keys=model.ignoreMetadataKeys />
        </dl>
      </div>
      <div class='accordion' id='diff-contents'>
        <#assign count=1>
        <#list model.mProductList>
          <#items as mProduct>
            <#if mProduct??>
                <div id='accordion${count}' class='accordion-item'>
                <h2 id='heading${count}' class='accordion-header'>
                  <button class='accordion-button' type='button' data-bs-toggle='collapse' data-bs-target='#collapse${count}' area-expanded='false' aria-controls='collapse${count}'>
                      <#if mProduct.diffRatio??>
                          <#assign ratio = mProduct.diffRatio>
                          <#if ratio gt criteria >
                              <#assign warningClass = 'warning'>
                          <#else>
                              <#assign warningClass = ''>
                          </#if>
                          <span class='ratio ${warningClass}'>${ratio}%</span>
                      <#else>
                          <h1>mProduct.diffRatio is NOT there</h1>
                      </#if>
                  </button>
                </h2>
              </div>
              <#assign count = count + 1>
            <#else>
              <h1>mProduct is null!</h1>
            </#if>
          </#items>
        </#list>
      </div>
    </div>
  </body>
</html>
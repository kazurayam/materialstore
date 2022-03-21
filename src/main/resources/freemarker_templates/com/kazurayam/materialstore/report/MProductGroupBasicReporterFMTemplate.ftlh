<#-- report/MProductGroupBasicBasicReporterTemplate.ftlh -->
<#import "../filesystem/IgnoreMetadataKeysTemplate.ftlh" as IgnoreMetadataKeys>
<#import "../filesystem/QueryOnMetadataTemplate.ftlh" as QueryOnMetadata>
<#import "../filesystem/MetadataTemplate.ftlh" as Metadata>

<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" /><!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-KyZXEAg3QhqLMpG8r+8fhAXLRk2vvoC2f3B09zVXn8CA5QIVfZOJ3BCsw2P0p/We" crossorigin="anonymous" />
    <style>${style}</style>
    <title>${mProductGroup.jobName}</title>
  </head>
  <body>
    <div class="container">
      <h1 class="title">${title}
        <button class="btn btn-secondary" type="button" data-bs-toggle="collapse" data-bs-target="#collapsingHeader" aria-expanded="false" aria-controls="collapsingHeader">About</button>
      </h1>
      <div id="collapsingHeader" class="collapse header">
        <dl>
          <dt>Root path :</dt>
          <dd>${store}</dd>
          <dt>JobName :</dt>
          <dd>${mProductGroup.jobName}</dd>
          <dt>Left MaterialList specification</dt>
          <#if mProductGroup.materialList0?? >
            <dd>
              <dl>
                <dt>JobTimestamp :</dt>
                <dd>${mProductGroup.materialList0.jobTimestamp}</dd>
                <dt>QueryOnMetadata :</dt>
                <dd>
                  <@QueryOnMetadata.toSpanSequence keyValuePairs=mProductGroup.materialList0.queryOnMetadata />
                </dd>
              </dl>
            </dd>
          <#else>
            <dd>not set</dd>
          </#if>
          <dt>Right MaterialList specification</dt>
          <#if mProductGroup.materialList1?? >
            <dd>
              <dl>
                <dt>JobTimestamp :</dt>
                <dd>${mProductGroup.materialList1.jobTimestamp}</dd>
                <dt>QueryOnMetadata :</dt>
                <dd>
                  <@QueryOnMetadata.toSpanSequence keyValuePairs=mProductGroup.materialList1.queryOnMetadata />
                </dd>
              </dl>
            </dd>
          <#else>
            <dd>not set</dd>
          </#if>
          <dt>IgnoreMetadataKeys</dt>
          <dd>
            <@IgnoreMetadataKeys.toSpanSequence keys=mProductGroup.ignoreMetadataKeys />
          </dd>
        </dl>
      </div>
      <div class="accordion" id="diff-contents">
        <#assign count=1>
        <#list mProductGroup.mProductList>
          <#items as mProduct>
            <#if mProduct??>
              <div id="accordion${count}" class="accordion-item">
                <h2 id="heading${count}" class="accordion-header">
                  <button class="accordion-button" type="button" data-bs-toggle="collapse" data-bs-target="#collapse${count}" area-expanded="false" aria-controls="collapse${count}">
                    <#if mProduct.diffRatio??>
                      <#assign ratio = mProduct.diffRatio>
                      <#if ratio gt criteria >
                        <#assign warningClass = 'warning'>
                      <#else>
                        <#assign warningClass = ''>
                      </#if>
                      <span class="ratio ${warningClass}">${ratio?string["0.00"]}%</span>
                    <#else>
                      <span class="ratio ">no diff</span>
                    </#if>
                    <span class="fileType">${mProduct.fileTypeExtension}</span>
                    <span class="description">${mProduct.description}</span>
                  </button>
                </h2>
                <div id="collapse${count}" class="according-collapse collapse" aria-labelledby="heading${count}" data-bs-parent="#diff-contents">
                  <div class="accordion-body">
                    <@makeModalSubsection mProduct count />

                    <@makeMaterialSubsection "left" mProduct.left mProductGroup />
                    <@makeMaterialSubsection "right" mProduct.right mProductGroup />
                    <@makeMaterialSubsection "diff" mProduct.diff mProductGroup />
                  </div>
                </div>
              </div>
              <#assign count = count + 1>
            <#else>
              <h1>mProduct is null!</h1>
            </#if>
          </#items>
        </#list>
      </div>
    </div><!-- Bootstrap -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js" integrity="sha384-U1DAWAznBHeqEIlVSCgzq+c9gqGAJn5c/t99JyeKa9xxaYpSvHU5awsuZVVFIhvj" crossorigin="anonymous"></script>
  </body>
</html>



<#macro makeModalSubsection mProduct count>
  <div class="show-diff"><!-- Button trigger modal -->
    <#if mProduct.left.diffability == "AS_IMAGE">
      <#assign imageModalId = "imageModal" + count >
      <#assign imageModalTitleId = "imageModalLabel" + count>
      <#assign carouselId = "carouselControl" + count>
      <#-- Show 3 images in a Modal -->
      <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#${imageModalId}">Show diff in Modal</button><!-- Modal to show 3 images: Left/Diff/Right -->
      <div class="modal fade" id="${imageModalId}" tabindex="-1" aria-labelledby="imageModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-fullscreen">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title" id="${imageModalTitleId}">
                <span>${mProduct.diffRatio?string["0.00"]}% ${mProduct.fileTypeExtension} ${mProduct.description}</span>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
              </h5>
            </div>
            <div class="modal-body">
              <!-- body -->
              <div id="${carouselId}" class="carousel slide" data-bs-ride="carousel">
                <div class="carousel-inner">
                  <div class="carousel-item">
                    <h3 class="centered">Left</h3>
                    <img class="img-fluid d-block w-75 centered" alt="left" src="${mProduct.left.relativeUrl}">
                  </div>
                  <div class="carousel-item active">
                    <h3 class="centered">Diff</h3>
                    <img class="img-fluid d-block w-75 centered" alt="diff" src="${mProduct.diff.relativeUrl}">
                  </div>
                  <div class="carousel-item">
                    <h3 class="centered">Right</h3>
                    <img class="img-fluid d-block w-75 centered" alt="right" src="${mProduct.right.relativeUrl}">
                  </div>
                </div>
                <button class="carousel-control-prev" type="button" data-bs-target="#${carouselId}" data-bs-slide="prev">
                  <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                  <span class="visually-hidden">Previous</span>
                </button>
                <button class="carousel-control-next" type="button" data-bs-target="#${carouselId}" data-bs-slide="next">
                  <span class="carousel-control-next-icon" aria-hidden="true"></span>
                  <span class="visually-hidden">Next</span>
                </button>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    <#elseif mProduct.left.diffability == "AS_TEXT">
      <#assign textModalId = "textModal" + count>
      <#assign textModalTitleId = "textModalLabel" + count>
      <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#${textModalId}">Show diff in Modal</button><!-- Modal to show texts diff -->
      <div class="modal fade" id="${textModalId}" tabindex="-1" aria-labelledby="textModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-fullscreen">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title" id="${textModalTitleId}">
                <span>${mProduct.diffRatio?string["0.00"]}% ${mProduct.fileTypeExtension} ${mProduct.description}</span>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
              </h5>
            </div>
            <div class="modal-body"><!-- body -->
              <iframe src="${mProduct.diff.relativeUrl}" title="TextDiff"></iframe>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
            </div>
          </div>
        </div>
      </div>
    <#else>
      <#-- mProduct.left.diffeablity == ${mProduct.left.diffability}; no output to be generated -->
    </#if>
  </div>
</#macro>


<#macro makeMaterialSubsection name material mProductGroup>
<div class="show-detail">
  <h2>${name}</h2>
  <dl class="detail">
    <dt>Material URL</dt>
    <dd>
      <a href="${material.relativeUrl}" target="${name}">${material.relativeUrl}</a>
    </dd>
    <dt>fileType</dt>
    <dd>${material.fileType}</dd>
    <dt>metadata</dt>
    <dd>
      <@Metadata.toSpanSequence material.metadata />
    </dd>
    <#if material.metadataURL??>
      <dt>Source URL</dt>
      <dd> <a href="${material.metadataURL}" target="source">${material.metadataURL}</a> </dd>
    </#if>
  </dl>
</div>
</#macro>
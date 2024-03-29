<#-- report/MProductGroupBasicBasicReporterTemplate.ftlh -->
<#import "../filesystem/IgnoreMetadataKeysTemplate.ftlh" as IgnoreMetadataKeys>
<#import "../filesystem/QueryOnMetadataTemplate.ftlh" as QueryOnMetadata>
<#import "../filesystem/MetadataTemplate.ftlh" as Metadata>
<#import "./AdjustImgWidthTemplate.ftlh" as AdjustImgWidth>

<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" /><!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-KyZXEAg3QhqLMpG8r+8fhAXLRk2vvoC2f3B09zVXn8CA5QIVfZOJ3BCsw2P0p/We" crossorigin="anonymous" />
    <style>${style}</style>
    <style>${accordionCustom}</style>
    <title>${mProductGroup.jobName}</title>
  </head>
  <body>
    <div class="container">
      <p id="count"><span class="warnings">${mProductGroup.countWarning?string("0")}</span>w
        <span class="ignorable">${mProductGroup.countIgnorable?string("0")}</span>i
        <span class="total">${mProductGroup.countTotal?string("0")}</span>t</p>
      <div style="display:flex">
        <h4 class="title" style="padding-top:10px">${title}</h4>
        <button style="margin-left:auto" class="btn btn-secondary" type="button" data-bs-toggle="collapse" data-bs-target="#collapsingHeader" aria-expanded="false" aria-controls="collapsingHeader">About</button>
      </div>
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
                    <@compressToSingleLine><@QueryOnMetadata.toSpanSequence keyValuePairs=mProductGroup.materialList0.queryOnMetadata /></@compressToSingleLine>
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
                  <@compressToSingleLine><@QueryOnMetadata.toSpanSequence keyValuePairs=mProductGroup.materialList1.queryOnMetadata /></@compressToSingleLine>
                </dd>
              </dl>
            </dd>
          <#else>
            <dd>not set</dd>
          </#if>
          <dt>IgnoreMetadataKeys</dt>
          <dd>
            <@compressToSingleLine><@IgnoreMetadataKeys.toSpanSequence keys=mProductGroup.ignoreMetadataKeys /></@compressToSingleLine>
          </dd>
          <dt>threshold</dt>
          <dd>
            ${mProductGroup.threshold?string["0.00"]}
          </dd>

          <dt>Sort keys :</dt>
          <dd>${sortKeys}</dd>
        </dl>
      </div>
      <div class="accordion accordion--custom" id="diff-contents">
        <#assign index=0>
        <#list mProductGroup.materialProductList>
          <#items as mProduct>
            <#if mProduct??>
              <div id="accordion${index}" class="accordion-item <#if !mProduct.fileTypeIsDiffable>filetype-non-diffable</#if>">
                <h2 id="heading${index}" class="accordion-header">
                  <#if mProduct.diffRatio?? && mProduct.diffRatio gt mProductGroup.threshold>
                    <input class="form-check-input" type="checkbox" id="ignorable${index}" value=""
                           data-index="${index}">
                  <#else>
                    <input class="form-check-input" type="checkbox" id="ignorable${index}" value=""
                           data-index="${index}" disabled>
                  </#if>
                  <button class="accordion-button" type="button" data-bs-toggle="collapse"
                          data-bs-target="#collapse${index}" area-expanded="false" aria-controls="collapse${index}">
                    <#if mProduct.diffRatio??>
                      <#assign ratio = mProduct.diffRatio>
                      <#if ratio gt threshold >
                        <#assign warningClass = 'warning'>
                      <#else>
                        <#assign warningClass = ''>
                      </#if>
                      <span class="ratio ${warningClass}">${ratio?string["0.00"]}%</span>
                    <#else>
                      <span class="ratio ">no diff</span>
                    </#if>
                    <span class="fileType">${mProduct.fileTypeExtension}</span>
                    <span class="identification">${mProduct.identification}</span>
                  </button>
                </h2>
                <div id="collapse${index}" class="according-collapse collapse" aria-labelledby="heading${index}"
                     data-bs-parent="#diff-contents">
                  <div class="accordion-body">
                    <@makeModalSubsection mProduct index mProductGroup/>

                    <@makeMaterialSubsection "left" mProduct.left mProductGroup />
                    <@makeMaterialSubsection "right" mProduct.right mProductGroup />
                    <@makeMaterialSubsection "diff" mProduct.diff mProductGroup />
                  </div>
                </div>
              </div>
              <#assign index = index + 1>
            <#else>
              <h1>MaterialProduct is null!</h1>
            </#if>
          </#items>
        </#list>
      </div>
    </div><!-- Bootstrap -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js" integrity="sha384-U1DAWAznBHeqEIlVSCgzq+c9gqGAJn5c/t99JyeKa9xxaYpSvHU5awsuZVVFIhvj" crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/1.11.3/jquery.js"></script>
    <script type="text/javascript">
        const model = ${model?no_esc};
    </script>
    <script type="text/javascript">${js?no_esc}</script>

    <script type="text/javascript">
      /*
       * registering event handlers that triggers the adjustImgWidth function
       */
      document.addEventListener("DOMContentLoaded", function() {
        // <div class="modal"> should have event-handler for 'shown.bs.modal' event
        let modalDivs = document.querySelectorAll('.modal')
        for (let modalDiv of modalDivs) {
          $(modalDiv).on('shown.bs.modal', function() {
            let imgs = modalDiv.querySelectorAll("img.img-fluid");
            for (let img of imgs) {
              //console.log("adjusting img.src=" + img.src);
              adjustImgWidth.bind(img)();
            }
          });
        }
      }, false);
    </script>

    <@AdjustImgWidth.adjustImgWidthScript />
  </body>
</html>



<#macro makeModalSubsection mProduct index mProductGroup>
  <div class="show-diff">
    <#if mProduct.left.diffability == "AS_IMAGE"  || mProduct.right.diffability == "AS_IMAGE">
      <#assign imageModalId = "imageModal" + index >
      <#assign imageModalTitleId = "imageModalLabel" + index>
      <#assign carouselId = "carouselControl" + index>
      <#-- Show 3 images in a Modal -->
      <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#${imageModalId}">Show diff in Modal</button><!-- Modal to show 3 images: Left/Diff/Right -->
      <div class="modal fade" id="${imageModalId}" tabindex="-1" aria-labelledby="imageModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-fullscreen">
          <div class="modal-content">
            <h4 style="margin: 12px 0 0 130px">${title}</h4>
            <div class="modal-header">
              <h5 class="modal-title" id="${imageModalTitleId}">
                <span>${mProduct.diffRatio?string["0.00"]}% ${mProduct.fileTypeExtension} ${mProduct.identification}</span>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
              </h5>
            </div>
            <div class="modal-body">
              <div id="${carouselId}" class="carousel slide" data-bs-ride="carousel">
                <div class="carousel-inner">
                  <div class="carousel-item">
                    <h3 class="centered">Left <#if mProductGroup.labelLeft != "">(${mProductGroup.labelLeft})</#if></h3>
                    <div>
                      <img class="img-fluid border d-block w-75 centered" alt="left" src="${mProduct.left.relativeUrl}">
                    </div>
                  </div>
                  <div class="carousel-item active">
                    <h3 class="centered">Diff</h3>
                    <div>
                      <img class="img-fluid border d-block w-75 centered" alt="diff" src="${mProduct.diff.relativeUrl}">
                    </div>
                  </div>
                  <div class="carousel-item">
                    <h3 class="centered">Right <#if mProductGroup.labelRight != "">(${mProductGroup.labelRight})</#if></h3>
                    <div>
                      <img class="img-fluid border d-block w-75 centered" alt="right" src="${mProduct.right.relativeUrl}">
                    </div>
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
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
            </div>
          </div>
        </div>
      </div>
    <#elseif mProduct.left.diffability == "AS_TEXT" || mProduct.right.diffability == "AS_TEXT">
      <#assign textModalId = "textModal" + index>
      <#assign textModalTitleId = "textModalLabel" + index>
      <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#${textModalId}">Show diff in Modal</button><!-- Modal to show texts diff -->
      <div class="modal fade" id="${textModalId}" tabindex="-1" aria-labelledby="textModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-fullscreen">
          <div class="modal-content">
            <h4 style="margin: 12px 0 0 130px">${title}</h4>
            <div class="modal-header">
              <h5 class="modal-title" id="${textModalTitleId}">
                <span>${mProduct.diffRatio?string["0.00"]}% ${mProduct.fileTypeExtension} ${mProduct.identification}</span>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
              </h5>
            </div>
            <div class="modal-body">
              <iframe src="${mProduct.diff.relativeUrl}" title="TextDiff"></iframe>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
            </div>
          </div>
        </div>
      </div>
    <#else>
      <span> </span>
      <#-- mProduct.left.diffeablity == ${mProduct.left.diffability}; no output to be generated -->
    </#if>
  </div>
</#macro>


<#macro makeMaterialSubsection name material mProductGroup>
<div class="show-detail">
  <h4>${name}
      <#if name == "left" && mProductGroup.labelLeft != "">
        (${mProductGroup.labelLeft})
      <#elseif name = "right" && mProductGroup.labelRight != "">
        (${mProductGroup.labelRight})
      </#if>
  </h4>
  <dl class="detail">
    <dt>Material URL</dt>
    <dd>
      <a href="${material.relativeUrl}" target="${name}">${material.relativeUrl}</a>
    </dd>
    <dt>fileType</dt>
    <dd>${material.fileType}</dd>
    <dt>metadata</dt>
    <dd>
      <@compressToSingleLine><@Metadata.toSpanSequence material.metadata /></@compressToSingleLine>
    </dd>
    <#if material.metadataURL??>
      <dt>Source URL</dt>
      <dd> <a href="${material.metadataURL}" target="source">${material.metadataURL}</a> </dd>
    </#if>
  </dl>
</div>
</#macro>
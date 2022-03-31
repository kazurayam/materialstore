<html lang="en">
<head>
  <meta charset="utf-8" />
  <title>${title}</title>
  <style>${style}</style>
</head>
<body>
<div id="container">
  <div id="decision">
    <h3>
        <#if equalRowsSize < rowsSize>
          <span>are DIFFERENT</span>
          <span style="margin-left: 20px;">${ratio}</span>
        <#else>
          <span>are EQUAL</span>
        </#if>
    </h3>
    <dl>
      <dt>rows</dt>
      <dd>
        <ul id="rows-stats">
          <li>
            <span>total :</span>
            <span>${rowsSize}</span>
          </li>
          <li>
            <span class="code-insert">inserted :</span>
            <span class="code-insert">${insertedRowsSize}</span>
          </li>
          <li>
            <span class="code-delete">deleted :</span>
            <span class="code-delete">${deletedRowsSize}</span>
          </li>
          <li>
            <span class="code-change">changed :</span>
            <span class="code-change">${changedRowsSize}</span>
          </li>
          <li>
            <span>equal :</span>
            <span>${equalRowsSize}</span>
          </li>
        </ul>
      </dd>
    </dl>
  </div>
  <table id="split-diff">
    <colgroup>
      <col width="66">
      <col />
      <col />
    </colgroup>
    <thead>
      <tr>
        <th></th>
        <th>Left</th>
        <th>Right</th>
      </tr>
      <tr>
        <th>Material</th>
        <td>
          <a href="../../../${leftData.relativeURL}" target="Left">${leftData.relativeURL}</a>
        </td>
        <td>
          <a href="../../../${rightData.relativeURL}" target="Right">${rightData.relativeURL}</a>
        </td>
      </tr>
      <tr>
        <th>FileType</th>
        <td>
          <span>${leftData.fileType}</span>
        </td>
        <td>
          <span>${rightData.fileType}</span>
        </td>
      </tr>
      <tr>
        <th>Metadata</th>
        <td>
          <span>${leftData.metadata}</span>
        </td>
        <td>
          <span>${rightData.metadata}</span>
        </td>
      </tr>
      <tr>
        <th>Source</th>
        <td>
          <#if leftData.url??>
            <a href="${leftData.url}" target="Left">${leftData.url}</a>
          </#if>
        </td>
        <td>
            <#if rightData.url??>
              <a href="${rightData.url}" target="Right">${rightData.url}</a>
            </#if>
        </td>
      </tr>
      <tr>
        <th>----</th>
        <td></td>
        <td></td>
      </tr>
    </thead>
    <tbody>
      <#list rows>
          <#items as row>
            <tr>
              <th class="${row.class}">${row.index}</th>
              <td class="${row.class}">
                <span class="blob-code-inner"><@markupSegments row.left_segments/></span>
              </td>
              <td class="${row.class}">
                <span class="blob-code-inner"><@markupSegments row.right_segments/></span>
              </td>
            </tr>
          </#items>
      </#list>
    </tbody>
  </table>
</div>
</body>
</html>


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
          <span class="deletion">${segment}</span>
        <#elseif inNewTag>
          <span class="insertion">${segment}</span>
        <#else>
          <span class="unchanged">${segment}</span>
        </#if>
      </#if>
    </#items>
    </span>
  </#list>
</#macro>
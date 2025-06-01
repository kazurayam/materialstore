<#import "MarkupSegments.ftlh" as com>

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
          <a href="../../../${splitData.left.relativeURL}" target="Left">${splitData.left.relativeURL}</a>
        </td>
        <td>
          <a href="../../../${splitData.right.relativeURL}" target="Right">${splitData.right.relativeURL}</a>
        </td>
      </tr>
      <tr>
        <th>FileType</th>
        <td>
          <span>${splitData.left.fileType}</span>
        </td>
        <td>
          <span>${splitData.right.fileType}</span>
        </td>
      </tr>
      <tr>
        <th>Metadata</th>
        <td>
          <span>${splitData.left.metadata}</span>
        </td>
        <td>
          <span>${splitData.right.metadata}</span>
        </td>
      </tr>
      <tr>
        <th>Source</th>
        <td>
          <#if splitData.left.url??>
            <a href="${splitData.left.url}" target="Left">${splitData.left.url}</a>
          </#if>
        </td>
        <td>
            <#if splitData.right.url??>
              <a href="${splitData.right.url}" target="Right">${splitData.right.url}</a>
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
      <#list splitData.rows>
          <#items as row>
            <tr>
              <th class="${row.class}">${row.index}</th>
              <td class="${row.class}">
                <span class="blob-code-inner"><@com.markupSegments row.left_segments/></span>
              </td>
              <td class="${row.class}">
                <span class="blob-code-inner"><@com.markupSegments row.right_segments/></span>
              </td>
            </tr>
          </#items>
      </#list>
    </tbody>
  </table>
</div>
</body>
</html>

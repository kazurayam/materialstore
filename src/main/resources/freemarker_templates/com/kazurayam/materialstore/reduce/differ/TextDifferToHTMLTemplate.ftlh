<#import "MarkupSegments.ftlh" as com>
<!doctype html>
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

  <!-- -->
  <p>Diff format:
    <label>
      <input type="radio" name="diffFormat" value="unified" required>Unified
    </label>
    <label>
      <input type="radio" name="diffFormat" value="side-by-side" checked>Side-by-side
    </label>
  </p>
  <!-- -->
  <table id="unified">
    <colgroup>
      <col width="66">
      <col />
    </colgroup>
    <thead>
      <tr>
        <th></th>
        <th>unified diff</th>
      </tr>
      <tr>
        <th>Material</th>
        <td>
          Left: <a href="../../../${splitData.left.relativeURL}" target="Left">${splitData.left.relativeURL}</a>
        </td>
      </tr>
      <tr>
        <th></th>
        <td>
          Right: <a href="../../../${splitData.right.relativeURL}" target="Right">${splitData.right.relativeURL}</a>
        </td>
      </tr>
      <tr>
        <th>Source</th>
        <td>
            <#if splitData.left.url??>
              Left: <a href="${splitData.left.url}" target="Left">${splitData.left.url}</a>
            </#if>
        </td>
      </tr>
      <tr>
        <th></th>
        <td>
            <#if splitData.right.url??>
              Right: <a href="${splitData.right.url}" target="Right">${splitData.right.url}</a>
            </#if>
        </td>
      </tr>
    </thead>
    <tbody>
      <#list unifiedDiffLines>
        <#items as line>
          <tr>
            <th class=""></th>
            <td class="">
              <span class="blob-code-inner">${line}</span>
            </td>
          </tr>
        </#items>
      </#list>
    </tbody>
  </table>

  <!-- -->
  <table id="side-by-side">
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

<script type="text/javascript">
    document.addEventListener('DOMContentLoaded', function() {
        //console.log('DOM Content has been loaded');
        // react to the radio button 'diffFormat'
        let inputElements = document.getElementsByName('diffFormat');
        for (let elm of inputElements) {
            elm.addEventListener('change', valueChange);
            if (elm.checked) {
                switchDiffFormat(elm.value)
            }
        }
    }, false);

    function valueChange(e) {
        //console.log(e.currentTarget.value + " was chosen")
        switchDiffFormat(e.currentTarget.value)
    }

    function switchDiffFormat(chosenFormat) {
        //console.log("chosenFormat:" + chosenFormat)
        if (chosenFormat === 'unified') {
            document.querySelector('table#unified').style.display = 'block';
            document.querySelector('table#side-by-side').style.display = 'none';
        } else if (chosenFormat === 'side-by-side') {
            document.querySelector('table#unified').style.display = 'none';
            document.querySelector('table#side-by-side').style.display = 'block';
        } else {
            console.log("unknown format:" + chosenFormat + " was chosen")
        }
    }
</script>
</body>
</html>

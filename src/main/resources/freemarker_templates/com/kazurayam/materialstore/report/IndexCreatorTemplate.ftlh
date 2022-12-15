<#-- report/IndexCreatorTemplate.ftlh -->

<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" /><!-- Bootstrap -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-KyZXEAg3QhqLMpG8r+8fhAXLRk2vvoC2f3B09zVXn8CA5QIVfZOJ3BCsw2P0p/We" crossorigin="anonymous" />
  <style>${style}</style>
  <style>${style2}</style>
  <title>${title}</title>
</head>
<body>
<div class="container">
  <h4 class="title">${title}</h4>
  <p>Path: ${model.store}</p>
  <table class="table table-sm table-striped">
    <thead>
      <tr>
        <th scope="col" style="width:10%">#</th>
        <th scope="col" style="width:65%">file name</th>
        <th scope="col" style="width:25%">created at</th>
      </tr>
    </thead>
    <tbody>
    <#list model.files as item>
      <tr>
        <th scope="row">${item?index + 1}</th>
        <td><a href="./${item.fileName}">${item.fileName}</a></td>
        <td>${item.lastModified}</td>
      </tr>
    </#list>
    </tbody>
  </table>
</div>
</body>
</html>
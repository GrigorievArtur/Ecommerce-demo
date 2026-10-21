<#import "partials/layout.ftl" as ui>
<@ui.page title=errorTitle description="An error occurred">
  <div class="panel auth-card center">
    <span class="eyebrow">Error ${statusCode}</span>
    <h1>${errorTitle}</h1>
    <p class="muted">${errorMessage}</p>
    <div class="actions actions-center">
      <a class="btn btn-primary" href="/">Back to home</a>
      <a class="btn btn-ghost" href="/products">Browse products</a>
    </div>
  </div>
</@ui.page>

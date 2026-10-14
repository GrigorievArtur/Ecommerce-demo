<#import "../partials/layout.ftl" as ui>
<@ui.page title="Products" description="Create, edit and remove products.">
  <div class="page-head">
    <div>
      <nav class="breadcrumb" aria-label="Breadcrumb"><a href="/admin">Dashboard</a> / Products</nav>
      <h1>Products</h1>
    </div>
    <a class="btn btn-primary" href="/admin/products/new">+ Add product</a>
  </div>

  <form class="filters" method="get" action="/admin/products" data-auto-submit>
    <input type="search" name="q" value="${(q)!''}" placeholder="Search products…" aria-label="Search products">
    <button class="btn btn-primary" type="submit">Search</button>
    <#if q?? && q?has_content><a class="btn btn-ghost" href="/admin/products">Clear</a></#if>
  </form>

  <p class="muted">${products.totalElements} product<#if products.totalElements != 1>s</#if></p>
  <div class="table-wrap">
    <table>
      <thead>
        <tr><th></th><th>Name</th><th>Category</th><th>Price</th><th>Stock</th><th></th></tr>
      </thead>
      <tbody>
      <#list products.content as p>
        <#assign pname = (p.name)!''>
        <tr>
          <td><img class="img-thumb" src="<@ui.productImage p/>" alt="" onerror="this.onerror=null;this.src='/images/placeholder.svg'"></td>
          <td><a href="/products/${p.id}"><strong>${pname}</strong></a></td>
          <td>${(p.categoryLabel)!''}</td>
          <td><@ui.money (p.price.grossPrice)!0/></td>
          <td><#if p.stock??>${p.stock}<#else><span class="muted">∞</span></#if></td>
          <td>
            <div class="table-actions">
              <a class="btn btn-ghost btn-sm" href="/admin/products/${p.id}/edit">Edit</a>
              <form class="inline" method="post" action="/admin/products/${p.id}/delete"
                    onsubmit="return confirm('Delete ${pname?js_string}?');">
                <@ui.csrfField/>
                <button class="btn btn-ghost btn-sm" type="submit">Delete</button>
              </form>
            </div>
          </td>
        </tr>
      </#list>
      </tbody>
    </table>
  </div>

  <#if products.content?size == 0>
    <p class="empty">No products matched “${(q)!''}”. <a href="/admin/products">Show all</a></p>
  </#if>

  <#assign adminQuery = "">
  <#if q?? && q?has_content><#assign adminQuery = "q=" + q?url></#if>
  <@ui.pagination products.number products.totalPages "/admin/products" adminQuery/>
</@ui.page>

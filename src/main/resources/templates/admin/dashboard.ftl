<#import "../partials/layout.ftl" as ui>
<@ui.page title="Admin dashboard" description="Store sales analytics and inventory alerts.">
  <div class="page-head">
    <h1>Dashboard</h1>
    <div class="actions">
      <a class="btn btn-ghost" href="/">View storefront</a>
      <a class="btn btn-ghost" href="/admin/products">Manage products</a>
      <a class="btn btn-primary" href="/admin/products/new">+ Add product</a>
    </div>
  </div>

  <div class="stats">
    <div class="stat"><div class="value">${summary.totalProducts}</div><div class="label">Products</div></div>
    <div class="stat"><div class="value">${summary.totalOrders}</div><div class="label">Orders</div></div>
    <div class="stat"><div class="value"><@ui.money summary.revenue/></div><div class="label">Revenue</div></div>
    <div class="stat"><div class="value"><@ui.money summary.averageOrderValue/></div><div class="label">Avg. order value</div></div>
  </div>

  <div class="panel">
    <h2 class="panel-title">Low stock (5 or fewer)</h2>
    <#if summary.lowStock?size == 0>
      <p class="muted">Everything is well stocked.</p>
    <#else>
      <div class="table-wrap">
        <table>
          <thead><tr><th>Product</th><th>Category</th><th>Stock</th><th></th></tr></thead>
          <tbody>
          <#list summary.lowStock as p>
            <tr>
              <td>${(p.name)!''}</td>
              <td>${(p.categoryLabel)!''}</td>
              <td><strong>${p.stock!0}</strong></td>
              <td><a href="/admin/products/${p.id}/edit">Restock</a></td>
            </tr>
          </#list>
          </tbody>
        </table>
      </div>
    </#if>
  </div>
</@ui.page>

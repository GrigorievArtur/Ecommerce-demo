<#import "../partials/layout.ftl" as ui>
<@ui.page title="Admin dashboard" description="Store sales analytics and inventory alerts.">
  <div class="page-head">
    <div>
      <span class="eyebrow">Admin</span>
      <h1>Dashboard</h1>
      <p>A quick read on sales and stock.</p>
    </div>
    <div class="actions">
      <a class="btn btn-ghost" href="/">View storefront</a>
      <a class="btn btn-ghost" href="/admin/products">Manage products</a>
      <a class="btn btn-primary" href="/admin/products/new">Add product</a>
    </div>
  </div>

  <div class="stats">
    <div class="stat"><div class="value">${summary.totalProducts}</div><div class="label">Products</div></div>
    <div class="stat"><div class="value">${summary.totalOrders}</div><div class="label">Orders</div></div>
    <div class="stat"><div class="value"><@ui.money summary.revenue/></div><div class="label">Revenue</div></div>
    <div class="stat"><div class="value"><@ui.money summary.averageOrderValue/></div><div class="label">Avg. order value</div></div>
  </div>

  <section class="panel">
    <div class="page-head">
      <div>
        <h2 class="panel-title">Low stock</h2>
        <p class="muted">Products with five or fewer units remaining.</p>
      </div>
    </div>
    <#if summary.lowStock?size == 0>
      <p class="muted">Everything is well stocked.</p>
    <#else>
      <div class="table-wrap">
        <table>
          <thead><tr><th>Product</th><th>Category</th><th class="numeric">Stock</th><th></th></tr></thead>
          <tbody>
          <#list summary.lowStock as p>
            <tr>
              <td><strong>${(p.name)!''}</strong></td>
              <td>${(p.categoryLabel)!''}</td>
              <td class="numeric"><span class="chip chip-sale">${p.stock!0} left</span></td>
              <td><div class="table-actions"><a class="btn btn-ghost btn-sm" href="/admin/products/${p.id}/edit">Restock</a></div></td>
            </tr>
          </#list>
          </tbody>
        </table>
      </div>
    </#if>
  </section>
</@ui.page>

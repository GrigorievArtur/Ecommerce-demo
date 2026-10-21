<#import "../partials/layout.ftl" as ui>
<@ui.page title=((mode == "edit")?then("Edit product", "New product")) description="Product editor">
  <div class="page-head">
    <div>
      <nav class="breadcrumb" aria-label="Breadcrumb"><a href="/admin">Dashboard</a> / <a href="/admin/products">Products</a> / <#if mode == "edit">Edit<#else>New</#if></nav>
      <span class="eyebrow">Admin</span>
      <h1><#if mode == "edit">Edit product<#else>New product</#if></h1>
      <p><#if mode == "edit">Update the details shoppers see.<#else>Add a new set, minifigure or accessory to the catalogue.</#if></p>
    </div>
  </div>

  <form class="panel" method="post" action="/admin/products" novalidate data-loading>
    <@ui.csrfField/>
    <#if productId??><input type="hidden" name="id" value="${productId}"></#if>

    <div class="form-section">
      <h2 class="panel-title">Product details</h2>
      <div class="form-grid">
        <div class="field full">
          <label for="name">Name</label>
          <input id="name" name="name" value="${(productForm.name)!''}" required>
          <#if fieldErrors?? && fieldErrors.name??><span class="err">${fieldErrors.name}</span></#if>
        </div>
        <div class="field full">
          <label for="description">Description</label>
          <textarea id="description" name="description" rows="4" required>${(productForm.description)!''}</textarea>
          <#if fieldErrors?? && fieldErrors.description??><span class="err">${fieldErrors.description}</span></#if>
        </div>
      </div>
    </div>

    <div class="form-section">
      <h2 class="panel-title">Pricing and stock</h2>
      <div class="form-grid">
        <div class="field">
          <label for="basePrice">Base price</label>
          <input id="basePrice" name="basePrice" type="number" step="0.01" min="0" value="${(productForm.basePrice)!''}" required>
          <#if fieldErrors?? && fieldErrors.basePrice??><span class="err">${fieldErrors.basePrice}</span></#if>
        </div>
        <div class="field">
          <label for="discountPercentage">Discount %</label>
          <input id="discountPercentage" name="discountPercentage" type="number" step="0.01" min="0" max="100" value="${(productForm.discountPercentage)!0}">
        </div>
        <div class="field">
          <label for="category">Category</label>
          <select id="category" name="category" required>
            <#list categories as c>
              <option value="${c.name}"<#if c.name == (productForm.categoryName)!''> selected</#if>>${c.label}</option>
            </#list>
          </select>
          <#if fieldErrors?? && fieldErrors.category??><span class="err">${fieldErrors.category}</span></#if>
        </div>
        <div class="field">
          <label for="stock">Stock</label>
          <input id="stock" name="stock" type="number" min="0" value="${(productForm.stock)!''}">
          <p class="field-hint">Leave blank for unlimited stock.</p>
        </div>
      </div>
    </div>

    <div class="form-section">
      <h2 class="panel-title">Media</h2>
      <div class="form-grid">
        <div class="field full">
          <label for="imageUrl">Image URL</label>
          <input id="imageUrl" name="imageUrl" value="${(productForm.imageUrl)!''}" placeholder="https://…">
          <p class="field-hint">Use a square or landscape image at least 800px wide.</p>
        </div>
      </div>
    </div>

    <div class="actions">
      <button class="btn btn-primary" type="submit">Save product</button>
      <a class="btn btn-ghost" href="/admin/products">Cancel</a>
    </div>
  </form>
</@ui.page>

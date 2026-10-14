<#import "../partials/layout.ftl" as ui>
<@ui.page title=((mode == "edit")?then("Edit product", "New product")) description="Product editor">
  <div class="page-head">
    <h1><#if mode == "edit">Edit product<#else>New product</#if></h1>
  </div>

  <form class="panel" method="post" action="/admin/products" novalidate data-loading>
    <@ui.csrfField/>
    <#if productId??><input type="hidden" name="id" value="${productId}"></#if>
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
        <label for="stock">Stock <span class="muted">(blank = unlimited)</span></label>
        <input id="stock" name="stock" type="number" min="0" value="${(productForm.stock)!''}">
      </div>
      <div class="field full">
        <label for="imageUrl">Image URL</label>
        <input id="imageUrl" name="imageUrl" value="${(productForm.imageUrl)!''}" placeholder="https://…">
      </div>
    </div>
    <div class="actions">
      <button class="btn btn-primary" type="submit">Save product</button>
      <a class="btn btn-ghost" href="/admin/products">Cancel</a>
    </div>
  </form>
</@ui.page>

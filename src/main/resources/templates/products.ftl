<#import "partials/layout.ftl" as ui>
<@ui.page title="Shop" description="Browse the full catalogue of sets, minifigures and accessories. Filter by category and sort by price or name.">
  <div class="catalog-head">
    <nav class="breadcrumb" aria-label="Breadcrumb"><a href="/">Home</a> / Shop</nav>
    <h1>
      <#if q?? && q?has_content>Results for “${q}”
      <#elseif selectedCategory??>${selectedCategory.label}
      <#else>All products
      </#if>
    </h1>
    <form class="filters" method="get" action="/products" data-auto-submit>
      <input type="search" name="q" value="${(q)!''}" placeholder="Search…" aria-label="Search products">
      <select name="category" aria-label="Filter by category">
        <option value="">All categories</option>
        <#list categories as c>
          <option value="${c.name}" <#if selectedCategory?? && selectedCategory.name == c.name>selected</#if>>${c.label}</option>
        </#list>
      </select>
      <select name="sort" aria-label="Sort products">
        <option value="newest" <#if sort == "newest">selected</#if>>Newest</option>
        <option value="price-asc" <#if sort == "price-asc">selected</#if>>Price: low to high</option>
        <option value="price-desc" <#if sort == "price-desc">selected</#if>>Price: high to low</option>
        <option value="name" <#if sort == "name">selected</#if>>Name A–Z</option>
      </select>
      <button class="btn btn-primary" type="submit">Apply</button>
      <#if (q?? && q?has_content) || selectedCategory??>
        <a class="btn btn-ghost" href="/products">Clear filters</a>
      </#if>
    </form>
  </div>

  <#if products.content?size == 0>
    <p class="empty">No products matched your search. <a href="/products">Clear filters</a></p>
  <#else>
    <p class="muted">Showing ${products.numberOfElements} of ${products.totalElements} product<#if products.totalElements != 1>s</#if></p>
    <div class="grid">
      <#list products.content as p><@ui.productCard p/></#list>
    </div>

    <#assign pageQuery = "size=" + products.size + "&sort=" + sort>
    <#if q?? && q?has_content><#assign pageQuery = pageQuery + "&q=" + q?url></#if>
    <#if selectedCategory??><#assign pageQuery = pageQuery + "&category=" + selectedCategory.name></#if>
    <@ui.pagination products.number products.totalPages "/products" pageQuery/>
  </#if>
</@ui.page>

<#import "../partials/layout.ftl" as ui>
<@ui.page title=((mode == "edit")?then("Edit address", "Add address")) description="Manage a saved delivery address.">
  <div class="page-head">
    <div>
      <span class="eyebrow">Address book</span>
      <h1><#if mode == "edit">Edit address<#else>Add address</#if></h1>
      <p>Saved addresses are offered at checkout to make ordering faster.</p>
    </div>
    <a class="btn btn-ghost" href="/account">Back to account</a>
  </div>

  <form class="panel" method="post" action="<#if mode == 'edit'>/account/addresses/${addressId}<#else>/account/addresses</#if>" novalidate data-loading>
    <@ui.csrfField/>
    <div class="form-grid">
      <div class="field">
        <label for="presetName">Address label</label>
        <input id="presetName" name="presetName" value="${(addressForm.presetName)!''}" placeholder="Home" required>
        <#if fieldErrors?? && fieldErrors.presetName??><span class="err">${fieldErrors.presetName}</span></#if>
      </div>
      <div class="field">
        <label for="fullName">Full name</label>
        <input id="fullName" name="fullName" value="${(addressForm.fullName)!''}" required autocomplete="name">
        <#if fieldErrors?? && fieldErrors.fullName??><span class="err">${fieldErrors.fullName}</span></#if>
      </div>
      <div class="field">
        <label for="email">Email</label>
        <input id="email" type="email" name="email" value="${(addressForm.email)!''}" required autocomplete="email">
        <#if fieldErrors?? && fieldErrors.email??><span class="err">${fieldErrors.email}</span></#if>
      </div>
      <div class="field">
        <label for="phoneNumber">Phone</label>
        <input id="phoneNumber" name="phoneNumber" value="${(addressForm.phoneNumber)!''}" required autocomplete="tel">
        <#if fieldErrors?? && fieldErrors.phoneNumber??><span class="err">${fieldErrors.phoneNumber}</span></#if>
      </div>
      <div class="field">
        <label for="country">Country</label>
        <input id="country" name="country" value="${(addressForm.country)!''}" required autocomplete="country-name">
        <#if fieldErrors?? && fieldErrors.country??><span class="err">${fieldErrors.country}</span></#if>
      </div>
      <div class="field">
        <label for="state">State / county</label>
        <input id="state" name="state" value="${(addressForm.state)!''}" required autocomplete="address-level1">
        <#if fieldErrors?? && fieldErrors.state??><span class="err">${fieldErrors.state}</span></#if>
      </div>
      <div class="field">
        <label for="city">City</label>
        <input id="city" name="city" value="${(addressForm.city)!''}" required autocomplete="address-level2">
        <#if fieldErrors?? && fieldErrors.city??><span class="err">${fieldErrors.city}</span></#if>
      </div>
      <div class="field">
        <label for="street">Street</label>
        <input id="street" name="street" value="${(addressForm.street)!''}" required autocomplete="address-line1">
        <#if fieldErrors?? && fieldErrors.street??><span class="err">${fieldErrors.street}</span></#if>
      </div>
      <div class="field">
        <label for="building">Building</label>
        <input id="building" name="building" value="${(addressForm.building)!''}">
      </div>
      <div class="field">
        <label for="apartment">Apartment</label>
        <input id="apartment" name="apartment" value="${(addressForm.apartment)!''}">
      </div>
      <div class="field">
        <label for="postalCode">Postal code</label>
        <input id="postalCode" name="postalCode" value="${(addressForm.postalCode)!''}" autocomplete="postal-code">
      </div>
      <div class="field full">
        <label class="checkbox"><input type="checkbox" name="isDefault" value="true" <#if addressForm.isDefault!false>checked</#if>> Make this my default address</label>
      </div>
    </div>
    <div class="actions">
      <button class="btn btn-primary" type="submit">Save address</button>
      <a class="btn btn-ghost" href="/account">Cancel</a>
    </div>
  </form>
</@ui.page>

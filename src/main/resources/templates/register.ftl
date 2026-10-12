<#import "partials/layout.ftl" as ui>
<@ui.page title="Create account" description="Create an account for faster checkout and order history.">
  <div class="panel auth-card">
    <h1 class="panel-title">Create your account</h1>
    <form method="post" action="/register" novalidate>
      <@ui.csrfField/>
      <div class="form-grid">
        <div class="field">
          <label for="firstname">First name</label>
          <input id="firstname" name="firstname" value="${(registerForm.firstname)!''}" required>
          <#if fieldErrors?? && fieldErrors.firstname??><span class="err">${fieldErrors.firstname}</span></#if>
        </div>
        <div class="field">
          <label for="lastname">Last name</label>
          <input id="lastname" name="lastname" value="${(registerForm.lastname)!''}" required>
          <#if fieldErrors?? && fieldErrors.lastname??><span class="err">${fieldErrors.lastname}</span></#if>
        </div>
        <div class="field full">
          <label for="email">Email</label>
          <input id="email" name="email" type="email" value="${(registerForm.email)!''}" required autocomplete="email">
          <#if fieldErrors?? && fieldErrors.email??><span class="err">${fieldErrors.email}</span></#if>
        </div>
        <div class="field">
          <label for="password">Password</label>
          <input id="password" name="password" type="password" required autocomplete="new-password">
          <#if fieldErrors?? && fieldErrors.password??><span class="err">${fieldErrors.password}</span></#if>
        </div>
        <div class="field">
          <label for="confirmPassword">Confirm password</label>
          <input id="confirmPassword" name="confirmPassword" type="password" required autocomplete="new-password">
          <#if fieldErrors?? && fieldErrors.confirmPassword??><span class="err">${fieldErrors.confirmPassword}</span></#if>
        </div>
      </div>
      <div class="actions">
        <button class="btn btn-primary btn-block" type="submit">Create account</button>
      </div>
    </form>
    <p class="note">Already registered? <a href="/login"><strong>Sign in</strong></a></p>
  </div>
</@ui.page>

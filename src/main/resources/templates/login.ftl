<#import "partials/layout.ftl" as ui>
<@ui.page title="Sign in" description="Sign in to your account to view orders and check out faster.">
  <div class="panel auth-card">
    <h1 class="panel-title">Sign in</h1>
    <#if loginError??><div class="alert alert-error">${loginError}</div></#if>
    <form method="post" action="/login" class="stack" data-loading>
      <@ui.csrfField/>
      <#if continueUrl??><input type="hidden" name="continue" value="${continueUrl}"></#if>
      <div class="field">
        <label for="username">Email</label>
        <input id="username" name="username" type="email" autocomplete="username" required autofocus>
      </div>
      <div class="field">
        <label for="password">Password</label>
        <input id="password" name="password" type="password" autocomplete="current-password" required>
      </div>
      <button class="btn btn-primary btn-block" type="submit">Sign in</button>
    </form>
    <p class="note">New here? <a href="/register"><strong>Create an account</strong></a></p>
  </div>
</@ui.page>

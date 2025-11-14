# Supabase Password Reset Configuration

This guide explains how to configure Supabase for password reset functionality.

## Step 1: Configure Site URL and Redirect URLs

1. Go to your Supabase project dashboard
2. Navigate to **Authentication** → **URL Configuration**
3. Set the following:

   **Site URL:**
   - Development: `http://localhost:8080` (or your dev server URL)
   - Production: `https://your-production-domain.com`

   **Redirect URLs:** (Add all these URLs)
   ```
   http://localhost:8080/reset-password
   https://your-dev-domain.com/reset-password
   https://your-production-domain.com/reset-password
   ```

   > **Note:** Supabase will only redirect to URLs listed here. Any other URLs will be rejected.

## Step 2: Configure Email Templates

1. Navigate to **Authentication** → **Email Templates**
2. Select **Reset Password** template
3. Update the template to use the correct redirect URL

   **Default template issue:** The default template might redirect to Supabase's landing page.

   **Solution:** Modify the email template to use `{{ .SiteURL }}/reset-password` or `{{ .RedirectTo }}`

   Example template:
   ```html
   <h2>Reset Password</h2>
   <p>Follow this link to reset the password for your user:</p>
   <p><a href="{{ .ConfirmationURL }}">Reset Password</a></p>
   ```

   The `{{ .ConfirmationURL }}` variable automatically includes:
   - Your redirect URL (from the API request)
   - Access token
   - Token type
   - Other necessary parameters

## Step 3: Verify Email Configuration

1. Ensure your Supabase project has email sending configured
2. For development:
   - Supabase provides a development email service (limited)
   - Check **Authentication** → **Email** for SMTP settings
3. For production:
   - Configure a custom SMTP provider (recommended)
   - Or use Supabase's built-in email service

## Step 4: Test the Flow

### From the Application:

1. Navigate to `/login`
2. Click "Forgot password?"
3. Enter your email address
4. Click "Send Reset Link"

### Expected Behavior:

1. You should receive an email with a reset link
2. The link should look like:
   ```
   https://your-project.supabase.co/auth/v1/verify?token=...&type=recovery&redirect_to=http://localhost:8080/reset-password
   ```
3. Clicking the link redirects you to: `http://localhost:8080/reset-password#access_token=...&type=recovery`
4. Enter your new password and submit
5. You should be redirected to `/login` with a success message

## Troubleshooting

### Issue: Redirects to Supabase landing page

**Cause:** The redirect URL is not in the allowed list or Site URL is not set correctly.

**Solution:**
1. Verify Site URL is set correctly
2. Add your reset-password URL to Redirect URLs list
3. Wait a few minutes for changes to propagate

### Issue: "Invalid redirect URL" error

**Cause:** The redirect URL doesn't match any URL in your allowed list.

**Solution:**
1. Check the `redirectUrl` being sent in the API request
2. Ensure it exactly matches one of the URLs in your Supabase Redirect URLs list
3. Include protocol (`http://` or `https://`)

### Issue: Email not received

**Cause:** Email configuration or rate limiting

**Solution:**
1. Check Supabase logs: **Authentication** → **Logs**
2. Verify SMTP configuration if using custom SMTP
3. Check spam folder
4. For development, use Supabase's email capture feature

### Issue: Token expired

**Cause:** Password reset tokens expire after a certain time (default: 1 hour)

**Solution:**
1. Request a new password reset
2. Check token expiration in Supabase: **Authentication** → **Policies**
3. You can adjust the expiration time if needed

## Environment-Specific Configuration

### Development Environment
```yaml
# application-develop.yaml
supabase:
  url: ${SUPABASE_URL_DEVELOP}  # e.g., https://xxxxx.supabase.co
  anon-key: ${SUPABASE_ANON_KEY_DEVELOP}
  jwt-secret: ${SUPABASE_JWT_SECRET_DEVELOP}
```

In Supabase dashboard:
- Site URL: Your development server URL
- Redirect URLs: Include your development reset-password URL

### Production Environment
```yaml
# application-prod.yaml
supabase:
  url: ${SUPABASE_URL_PROD}
  anon-key: ${SUPABASE_ANON_KEY_PROD}
  jwt-secret: ${SUPABASE_JWT_SECRET_PROD}
```

In Supabase dashboard:
- Site URL: Your production domain
- Redirect URLs: Include your production reset-password URL

## Security Best Practices

1. **Always use HTTPS in production** for redirect URLs
2. **Keep JWT secret confidential** - never commit to git
3. **Use environment variables** for all Supabase configuration
4. **Monitor authentication logs** in Supabase dashboard
5. **Set appropriate token expiration times**
6. **Consider rate limiting** password reset requests to prevent abuse

## Testing in Local Development (localh2 profile)

When using the `localh2` profile:
- Password reset is disabled (no Supabase connection)
- Users will see a message that the feature is not available
- This is intentional for local development without external dependencies

To test password reset locally:
- Use `localsupabase` profile with Supabase configuration
- Or use `develop` profile pointing to your Supabase project

## Additional Resources

- [Supabase Auth Documentation](https://supabase.com/docs/guides/auth)
- [Email Templates](https://supabase.com/docs/guides/auth/auth-email-templates)
- [URL Configuration](https://supabase.com/docs/guides/auth/redirect-urls)

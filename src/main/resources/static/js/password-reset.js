/**
 * Password reset functionality
 */

// Handle forgot password form submission
async function handleForgotPassword(event) {
    event.preventDefault();

    const email = document.getElementById('email').value;
    const submitButton = document.getElementById('submit-button');
    const loading = document.getElementById('loading');
    const errorDiv = document.getElementById('error-message');
    const successDiv = document.getElementById('success-message');

    // Hide previous messages
    errorDiv.style.display = 'none';
    successDiv.style.display = 'none';

    // Disable button and show loading
    submitButton.disabled = true;
    loading.style.display = 'block';

    try {
        const response = await fetch('/api/auth/reset-password-request', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email: email })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Failed to send reset email');
        }

        // Show success message
        successDiv.textContent = 'Password reset email sent! Please check your inbox.';
        successDiv.style.display = 'block';

        // Clear form
        document.getElementById('forgot-password-form').reset();

    } catch (error) {
        console.error('Error:', error);
        errorDiv.textContent = error.message || 'Failed to send reset email. Please try again.';
        errorDiv.style.display = 'block';
    } finally {
        submitButton.disabled = false;
        loading.style.display = 'none';
    }
}

// Handle reset password form submission
async function handleResetPassword(event) {
    event.preventDefault();

    const newPassword = document.getElementById('new-password').value;
    const confirmPassword = document.getElementById('confirm-password').value;
    const submitButton = document.getElementById('submit-button');
    const loading = document.getElementById('loading');
    const errorDiv = document.getElementById('error-message');
    const successDiv = document.getElementById('success-message');

    // Hide previous messages
    errorDiv.style.display = 'none';
    successDiv.style.display = 'none';

    // Validate passwords match
    if (newPassword !== confirmPassword) {
        errorDiv.textContent = 'Passwords do not match';
        errorDiv.style.display = 'block';
        return;
    }

    // Get access token from URL hash fragment (Supabase sends it after #)
    const hashParams = new URLSearchParams(window.location.hash.substring(1));
    const accessToken = hashParams.get('access_token');

    if (!accessToken) {
        errorDiv.textContent = 'Invalid or missing reset token';
        errorDiv.style.display = 'block';
        return;
    }

    // Disable button and show loading
    submitButton.disabled = true;
    loading.style.display = 'block';

    try {
        const response = await fetch('/api/auth/update-password', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                accessToken: accessToken,
                newPassword: newPassword
            })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Failed to reset password');
        }

        // Show success message
        successDiv.textContent = 'Password reset successful! Redirecting to login...';
        successDiv.style.display = 'block';

        // Redirect to login after delay
        setTimeout(() => {
            window.location.href = '/login';
        }, 2000);

    } catch (error) {
        console.error('Error:', error);
        errorDiv.textContent = error.message || 'Failed to reset password. Please try again.';
        errorDiv.style.display = 'block';
        submitButton.disabled = false;
        loading.style.display = 'none';
    }
}

/**
 * Authentication pages JavaScript (login, register, password reset)
 */

// Handle login form submission
async function handleLogin(event, authClient) {
    event.preventDefault();

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const loginButton = document.getElementById('login-button');
    const loading = document.getElementById('loading');
    const errorDiv = document.getElementById('error-message');
    const successDiv = document.getElementById('success-message');

    // Hide previous messages
    errorDiv.style.display = 'none';
    successDiv.style.display = 'none';

    // Check if authClient is initialized
    if (!authClient) {
        errorDiv.textContent = 'Authentication system is not initialized. Please refresh the page and check the console for errors.';
        errorDiv.style.display = 'block';
        return;
    }

    // Disable button and show loading
    loginButton.disabled = true;
    loading.style.display = 'block';

    console.log('[AUTH] Attempting login for:', email);

    try {
        const { data, error } = await authClient.auth.signInWithPassword({
            email: email,
            password: password,
        });

        if (error) {
            console.error('[AUTH] Login error:', error);
            throw error;
        }

        console.log('[AUTH] Login successful');

        // Show success and redirect
        successDiv.textContent = 'Login successful! Redirecting...';
        successDiv.style.display = 'block';

        setTimeout(() => {
            window.location.href = '/app';
        }, 1000);
    } catch (error) {
        console.error('[AUTH] Login failed:', error);
        errorDiv.textContent = error.message || 'Login failed. Please try again.';
        errorDiv.style.display = 'block';
        loginButton.disabled = false;
        loading.style.display = 'none';
    }
}

// Handle register form submission
async function handleRegister(event, authClient) {
    event.preventDefault();

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirm-password').value;
    const registerButton = document.getElementById('register-button');
    const loading = document.getElementById('loading');
    const errorDiv = document.getElementById('error-message');
    const successDiv = document.getElementById('success-message');

    // Hide previous messages
    errorDiv.style.display = 'none';
    successDiv.style.display = 'none';

    // Validate passwords match
    if (password !== confirmPassword) {
        errorDiv.textContent = 'Passwords do not match';
        errorDiv.style.display = 'block';
        return;
    }

    // Check if authClient is initialized
    if (!authClient) {
        errorDiv.textContent = 'Authentication system is not initialized. Please refresh the page.';
        errorDiv.style.display = 'block';
        return;
    }

    // Disable button and show loading
    registerButton.disabled = true;
    loading.style.display = 'block';

    console.log('[AUTH] Attempting registration for:', email);

    try {
        const { data, error } = await authClient.auth.signUp({
            email: email,
            password: password,
        });

        if (error) {
            console.error('[AUTH] Registration error:', error);
            throw error;
        }

        console.log('[AUTH] Registration successful');

        // Show success message
        successDiv.textContent = 'Registration successful! Please check your email to confirm your account.';
        successDiv.style.display = 'block';

        // Clear form
        document.getElementById('register-form').reset();

        registerButton.disabled = false;
        loading.style.display = 'none';
    } catch (error) {
        console.error('[AUTH] Registration failed:', error);
        errorDiv.textContent = error.message || 'Registration failed. Please try again.';
        errorDiv.style.display = 'block';
        registerButton.disabled = false;
        loading.style.display = 'none';
    }
}

// Check if user is already logged in and redirect to app
async function checkAndRedirectIfLoggedIn(authClient) {
    if (authClient) {
        authClient.auth.getSession().then(({ data: { session } }) => {
            if (session) {
                console.log('[AUTH] User already logged in, redirecting to app');
                window.location.href = '/app';
            }
        }).catch(error => {
            console.error('[AUTH] Session check error:', error);
        });
    }
}

// Auto-run check on page load (for login and register pages)
document.addEventListener('DOMContentLoaded', () => {
    // Check if this is a login or register page by looking for specific elements
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');

    if (loginForm || registerForm) {
        // Wait a moment for authClient to be initialized in the inline script
        setTimeout(() => {
            if (typeof authClient !== 'undefined' && authClient) {
                checkAndRedirectIfLoggedIn(authClient);
            }
        }, 100);
    }
});

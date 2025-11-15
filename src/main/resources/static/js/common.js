/**
 * Common JavaScript utilities shared across all pages
 */

/**
 * Initialize authentication client (Supabase or mock)
 * @param {boolean} useMockAuth - Whether to use mock authentication
 * @param {string} supabaseUrl - Supabase project URL
 * @param {string} supabaseAnonKey - Supabase anonymous key
 * @returns {Object} Initialized auth client
 */
function initializeAuthClient(useMockAuth, supabaseUrl, supabaseAnonKey) {
    if (useMockAuth) {
        console.log('[AUTH] Using mock authentication');
        return window.mockSupabaseClient;
    } else {
        console.log('[AUTH] Supabase URL:', supabaseUrl);
        console.log('[AUTH] Anon key present:', supabaseAnonKey ? 'yes' : 'no');

        if (!supabaseUrl || !supabaseAnonKey) {
            throw new Error('Supabase configuration is missing. Please check environment variables.');
        }

        if (typeof supabase === 'undefined') {
            throw new Error('Supabase library failed to load. Please check your internet connection.');
        }

        const { createClient } = supabase;
        const client = createClient(supabaseUrl, supabaseAnonKey);
        console.log('[AUTH] Supabase client initialized successfully');
        return client;
    }
}

/**
 * Show error message in an element
 * @param {string} elementId - ID of the error element
 * @param {string} message - Error message to display
 */
function showError(elementId, message) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = message;
        element.style.display = 'block';
    }
}

/**
 * Hide error message
 * @param {string} elementId - ID of the error element
 */
function hideError(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.style.display = 'none';
    }
}

/**
 * Show success message in an element
 * @param {string} elementId - ID of the success element
 * @param {string} message - Success message to display
 */
function showSuccess(elementId, message) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = message;
        element.style.display = 'block';
    }
}

/**
 * Hide success message
 * @param {string} elementId - ID of the success element
 */
function hideSuccess(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.style.display = 'none';
    }
}

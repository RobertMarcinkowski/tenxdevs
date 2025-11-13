/**
 * Mock Authentication Client for localh2 profile
 * Mimics Supabase authentication API for local development
 */
const mockAuth = {
    /**
     * Sign in with email and password
     */
    signInWithPassword: async function(credentials) {
        try {
            const response = await fetch('/api/mock-auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    email: credentials.email,
                    password: credentials.password
                })
            });

            const data = await response.json();

            if (!response.ok) {
                return {
                    data: null,
                    error: {
                        message: data.message || 'Login failed'
                    }
                };
            }

            // Store session in localStorage
            localStorage.setItem('mock_session', JSON.stringify(data));

            return {
                data: data,
                error: null
            };
        } catch (error) {
            return {
                data: null,
                error: {
                    message: error.message
                }
            };
        }
    },

    /**
     * Sign up with email and password
     */
    signUp: async function(credentials) {
        try {
            const response = await fetch('/api/mock-auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    email: credentials.email,
                    password: credentials.password
                })
            });

            const data = await response.json();

            if (!response.ok) {
                return {
                    data: null,
                    error: {
                        message: data.message || 'Registration failed'
                    }
                };
            }

            // Store session in localStorage
            localStorage.setItem('mock_session', JSON.stringify(data));

            return {
                data: {
                    user: data.user,
                    session: data.session
                },
                error: null
            };
        } catch (error) {
            return {
                data: null,
                error: {
                    message: error.message
                }
            };
        }
    },

    /**
     * Sign out
     */
    signOut: async function() {
        try {
            const session = this.getSession();
            if (session && session.access_token) {
                await fetch('/api/mock-auth/logout', {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${session.access_token}`
                    }
                });
            }

            // Clear local session
            localStorage.removeItem('mock_session');

            return { error: null };
        } catch (error) {
            return {
                error: {
                    message: error.message
                }
            };
        }
    },

    /**
     * Get current session
     */
    getSession: function() {
        const sessionStr = localStorage.getItem('mock_session');
        if (!sessionStr) {
            return null;
        }

        try {
            const session = JSON.parse(sessionStr);
            return {
                data: {
                    session: session.session,
                    user: session.user
                },
                error: null
            };
        } catch (error) {
            return {
                data: { session: null },
                error: null
            };
        }
    }
};

// Create a mock Supabase-like client
window.mockSupabaseClient = {
    auth: {
        signInWithPassword: (credentials) => mockAuth.signInWithPassword(credentials),
        signUp: (credentials) => mockAuth.signUp(credentials),
        signOut: () => mockAuth.signOut(),
        getSession: () => Promise.resolve(mockAuth.getSession())
    }
};

/**
 * Landing page JavaScript
 */

// Check if user is logged in and update UI accordingly
async function checkAuthStatus() {
    const loggedOutButtons = document.getElementById('logged-out-buttons');
    const loggedInButtons = document.getElementById('logged-in-buttons');
    const userStatus = document.getElementById('user-status');
    const statusMessage = document.getElementById('status-message');

    try {
        const { data: { session } } = await authClient.auth.getSession();

        if (session && session.user) {
            // User is logged in
            loggedOutButtons.style.display = 'none';
            loggedInButtons.style.display = 'flex';
            userStatus.style.display = 'block';
            userStatus.classList.add('logged-in');
            statusMessage.textContent = `Logged in as: ${session.user.email}`;
        } else {
            // User is not logged in
            loggedOutButtons.style.display = 'flex';
            loggedInButtons.style.display = 'none';
            userStatus.style.display = 'none';
        }
    } catch (error) {
        console.error('Error checking auth status:', error);
        loggedOutButtons.style.display = 'flex';
        loggedInButtons.style.display = 'none';
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    checkAuthStatus();
});

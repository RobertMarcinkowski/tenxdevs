/**
 * App page JavaScript - Notes and Trip Plans functionality
 * Main functions are defined here, called from inline script in app.html
 */

// Global variables
let currentNotes = [];
let currentDetailNoteId = null;
let currentTripPlans = [];

// Authentication and initialization
async function checkAuth() {
    const loadingDiv = document.getElementById('loading');
    const errorDiv = document.getElementById('error');
    const appContent = document.getElementById('app-content');

    try {
        const { data: { session }, error } = await authClient.auth.getSession();

        if (error || !session) {
            loadingDiv.style.display = 'none';
            errorDiv.style.display = 'block';
            setTimeout(() => {
                window.location.href = '/login';
            }, 2000);
            return;
        }

        const user = session.user;
        document.getElementById('user-email').textContent = user.email || 'Guest';

        loadingDiv.style.display = 'none';
        appContent.style.display = 'block';

    } catch (err) {
        console.error('Authentication check failed:', err);
        loadingDiv.style.display = 'none';
        errorDiv.style.display = 'block';
    }
}

// Logout handler
async function handleLogout() {
    try {
        const { error } = await authClient.auth.signOut();
        if (error) {
            console.warn('Logout warning:', error);
        }
    } catch (error) {
        console.warn('Logout error:', error);
    }

    localStorage.removeItem('supabase_token');
    localStorage.removeItem('mock_session');
    window.location.href = '/';
}

// Notes Management - Load notes
async function loadNotes() {
    const notesListDiv = document.getElementById('notes-list');

    try {
        const { data: { session } } = await authClient.auth.getSession();

        if (!session) {
            notesListDiv.innerHTML = '<div class="error">No session found. Please log in again.</div>';
            return;
        }

        const response = await fetch('/api/notes', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${session.access_token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to load notes');
        }

        currentNotes = await response.json();
        renderNotes();

    } catch (error) {
        console.error('Error loading notes:', error);
        notesListDiv.innerHTML = '<div class="error">Failed to load notes: ' + error.message + '</div>';
    }
}

// Render notes
function renderNotes() {
    const notesListDiv = document.getElementById('notes-list');

    if (currentNotes.length === 0) {
        notesListDiv.innerHTML = '<div class="empty-notes">No notes yet. Click "Add Note" to create your first travel note!</div>';
        return;
    }

    let html = '';
    for (const note of currentNotes) {
        const createdDate = new Date(note.createdAt).toLocaleDateString();
        const updatedDate = new Date(note.updatedAt).toLocaleDateString();
        const contentPreview = note.content.length > 150
            ? note.content.substring(0, 150) + '...'
            : note.content;

        html += `
            <div class="note-item" onclick="openNoteDetail(${note.id})">
                <h3>${escapeHtml(note.title)}</h3>
                <div class="note-content">${escapeHtml(contentPreview)}</div>
                <div class="note-meta">
                    Created: ${createdDate} | Last updated: ${updatedDate}
                </div>
                <div class="note-actions" onclick="event.stopPropagation()">
                    <button class="edit-btn" onclick="editNote(${note.id})">Edit</button>
                    <button class="delete-btn" onclick="deleteNote(${note.id})">Delete</button>
                </div>
            </div>
        `;
    }

    notesListDiv.innerHTML = html;
}

// Escape HTML
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Note form management
function openNoteForm(noteId = null) {
    const modal = document.getElementById('note-form-modal');
    const formTitle = document.getElementById('note-form-title');
    const noteIdInput = document.getElementById('note-id');
    const titleInput = document.getElementById('note-title-input');
    const contentInput = document.getElementById('note-content-input');

    if (noteId) {
        const note = currentNotes.find(n => n.id === noteId);
        if (note) {
            formTitle.textContent = 'Edit Note';
            noteIdInput.value = note.id;
            titleInput.value = note.title;
            contentInput.value = note.content;
        }
    } else {
        formTitle.textContent = 'Add New Note';
        noteIdInput.value = '';
        titleInput.value = '';
        contentInput.value = '';
    }

    modal.classList.add('active');
}

function closeNoteForm() {
    document.getElementById('note-form-modal').classList.remove('active');
}

async function saveNote(event) {
    event.preventDefault();

    const noteId = document.getElementById('note-id').value;
    const title = document.getElementById('note-title-input').value.trim();
    const content = document.getElementById('note-content-input').value.trim();

    if (!title || !content) {
        alert('Please fill in both title and content');
        return;
    }

    try {
        const { data: { session } } = await authClient.auth.getSession();

        if (!session) {
            alert('No session found. Please log in again.');
            return;
        }

        const url = noteId ? `/api/notes/${noteId}` : '/api/notes';
        const method = noteId ? 'PUT' : 'POST';

        const response = await fetch(url, {
            method: method,
            headers: {
                'Authorization': `Bearer ${session.access_token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ title, content })
        });

        const result = await response.json();

        if (!response.ok || !result.success) {
            throw new Error(result.message || 'Failed to save note');
        }

        closeNoteForm();
        loadNotes();
        alert(result.message || 'Note saved successfully!');

    } catch (error) {
        console.error('Error saving note:', error);
        alert('Failed to save note: ' + error.message);
    }
}

function editNote(noteId) {
    openNoteForm(noteId);
}

async function deleteNote(noteId) {
    if (!confirm('Are you sure you want to delete this note?')) {
        return;
    }

    try {
        const { data: { session } } = await authClient.auth.getSession();

        if (!session) {
            alert('No session found. Please log in again.');
            return;
        }

        const response = await fetch(`/api/notes/${noteId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${session.access_token}`,
                'Content-Type': 'application/json'
            }
        });

        const result = await response.json();

        if (!response.ok || !result.success) {
            throw new Error(result.message || 'Failed to delete note');
        }

        loadNotes();
        alert(result.message || 'Note deleted successfully!');

    } catch (error) {
        console.error('Error deleting note:', error);
        alert('Failed to delete note: ' + error.message);
    }
}

// Note detail view
function openNoteDetail(noteId) {
    const note = currentNotes.find(n => n.id === noteId);
    if (!note) return;

    currentDetailNoteId = noteId;

    const modal = document.getElementById('note-detail-modal');
    document.getElementById('note-detail-title').textContent = note.title;
    document.getElementById('note-detail-content').textContent = note.content;
    document.getElementById('note-detail-created').textContent = new Date(note.createdAt).toLocaleString();
    document.getElementById('note-detail-updated').textContent = new Date(note.updatedAt).toLocaleString();

    modal.classList.add('active');

    loadTripPlans(noteId);
    checkCanGeneratePlan(noteId);
}

function closeNoteDetail() {
    document.getElementById('note-detail-modal').classList.remove('active');
    currentDetailNoteId = null;
}

function editNoteFromDetail() {
    if (currentDetailNoteId) {
        const noteId = currentDetailNoteId;
        closeNoteDetail();
        editNote(noteId);
    }
}

async function deleteNoteFromDetail() {
    if (currentDetailNoteId) {
        const noteId = currentDetailNoteId;
        closeNoteDetail();
        await deleteNote(noteId);
    }
}

// Trip Plans functionality
async function checkCanGeneratePlan(noteId) {
    const warningDiv = document.getElementById('plan-warning');
    const infoDiv = document.getElementById('plan-info');
    const generateBtn = document.getElementById('generate-plan-btn');

    warningDiv.style.display = 'none';
    infoDiv.style.display = 'none';
    generateBtn.disabled = true;

    try {
        const { data: { session } } = await authClient.auth.getSession();

        if (!session) {
            warningDiv.innerHTML = 'Please log in to generate trip plans.';
            warningDiv.style.display = 'block';
            return;
        }

        const response = await fetch(`/api/trip-plans/can-generate?noteId=${noteId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${session.access_token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) throw new Error('Failed to check generation status');

        const result = await response.json();

        if (result.can_generate) {
            generateBtn.disabled = false;
            infoDiv.innerHTML = `You can generate ${result.remaining_usage} more plan(s) today (limit: ${result.daily_limit} per day).`;
            infoDiv.style.display = 'block';
        } else if (result.missing_preferences) {
            warningDiv.innerHTML = `${result.reason}. <a href="/profile">Go to Profile</a> to set your travel preferences.`;
            warningDiv.style.display = 'block';
        } else if (result.limit_exceeded) {
            warningDiv.innerHTML = `${result.reason}. Please try again tomorrow.`;
            warningDiv.style.display = 'block';
        } else {
            warningDiv.innerHTML = result.reason || 'Cannot generate plan at this time.';
            warningDiv.style.display = 'block';
        }

    } catch (error) {
        console.error('Error checking generation status:', error);
        warningDiv.innerHTML = 'Failed to check if you can generate a plan. Please try again.';
        warningDiv.style.display = 'block';
    }
}

async function generateTripPlan() {
    if (!currentDetailNoteId) {
        alert('No note selected');
        return;
    }

    const generateBtn = document.getElementById('generate-plan-btn');
    const generatingStatus = document.getElementById('generating-status');
    const warningDiv = document.getElementById('plan-warning');

    generateBtn.disabled = true;
    generatingStatus.style.display = 'block';
    warningDiv.style.display = 'none';

    try {
        const { data: { session } } = await authClient.auth.getSession();

        if (!session) {
            alert('Please log in to generate trip plans.');
            return;
        }

        const response = await fetch('/api/trip-plans/generate', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${session.access_token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ noteId: currentDetailNoteId })
        });

        const result = await response.json();

        if (!response.ok || !result.success) {
            throw new Error(result.message || 'Failed to generate trip plan');
        }

        alert('Trip plan generated successfully!');
        await loadTripPlans(currentDetailNoteId);
        await checkCanGeneratePlan(currentDetailNoteId);

    } catch (error) {
        console.error('Error generating trip plan:', error);
        warningDiv.innerHTML = 'Failed to generate trip plan: ' + error.message;
        warningDiv.style.display = 'block';
    } finally {
        generatingStatus.style.display = 'none';
    }
}

async function loadTripPlans(noteId) {
    const plansListDiv = document.getElementById('trip-plans-list');

    try {
        const { data: { session } } = await authClient.auth.getSession();

        if (!session) {
            plansListDiv.innerHTML = '<div class="error">Please log in to view trip plans.</div>';
            return;
        }

        const response = await fetch(`/api/trip-plans?noteId=${noteId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${session.access_token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) throw new Error('Failed to load trip plans');

        currentTripPlans = await response.json();
        renderTripPlans();

    } catch (error) {
        console.error('Error loading trip plans:', error);
        plansListDiv.innerHTML = '<div class="error">Failed to load trip plans.</div>';
    }
}

function renderTripPlans() {
    const plansListDiv = document.getElementById('trip-plans-list');

    if (currentTripPlans.length === 0) {
        plansListDiv.innerHTML = '<p style="color: #666; font-size: 14px;">No trip plans generated yet. Click "Generate Trip Plan" to create one!</p>';
        return;
    }

    let html = '';
    for (const plan of currentTripPlans) {
        const createdDate = new Date(plan.createdAt).toLocaleString();
        const rating = plan.rating || 0;

        html += `
            <div class="trip-plan-item">
                <div class="plan-header">
                    <span class="plan-date">Generated: ${createdDate}</span>
                </div>
                <div class="plan-content">${escapeHtml(plan.planContent)}</div>
                <div class="plan-rating">
                    <label>Rate this plan:</label>
                    <div class="star-rating" id="rating-${plan.id}">
                        ${renderStars(plan.id, rating)}
                    </div>
                    ${rating > 0 ? `<span style="color: #666; font-size: 13px;">(${rating}/5)</span>` : ''}
                </div>
            </div>
        `;
    }

    plansListDiv.innerHTML = html;
}

function renderStars(planId, currentRating) {
    let html = '';
    for (let i = 1; i <= 5; i++) {
        const filled = i <= currentRating ? 'filled' : '';
        html += `<span class="star ${filled}" onclick="rateTripPlan(${planId}, ${i})" onmouseover="hoverStar(${planId}, ${i})" onmouseout="unhoverStar(${planId})">★</span>`;
    }
    return html;
}

function hoverStar(planId, rating) {
    const stars = document.querySelectorAll(`#rating-${planId} .star`);
    stars.forEach((star, index) => {
        if (index < rating) {
            star.classList.add('hover');
        } else {
            star.classList.remove('hover');
        }
    });
}

function unhoverStar(planId) {
    const stars = document.querySelectorAll(`#rating-${planId} .star`);
    stars.forEach(star => {
        star.classList.remove('hover');
    });
}

async function rateTripPlan(planId, rating) {
    try {
        const { data: { session } } = await authClient.auth.getSession();

        if (!session) {
            alert('Please log in to rate trip plans.');
            return;
        }

        const response = await fetch(`/api/trip-plans/${planId}/rate`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${session.access_token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ rating: rating })
        });

        const result = await response.json();

        if (!response.ok || !result.success) {
            throw new Error(result.message || 'Failed to rate trip plan');
        }

        const plan = currentTripPlans.find(p => p.id === planId);
        if (plan) {
            plan.rating = rating;
        }
        renderTripPlans();

    } catch (error) {
        console.error('Error rating trip plan:', error);
        alert('Failed to rate trip plan: ' + error.message);
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    checkAuth().then(() => {
        loadNotes();
    });
});

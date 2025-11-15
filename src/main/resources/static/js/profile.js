/**
 * Profile/Preferences page JavaScript
 */

let accessToken = null;

async function init() {
    const loadingDiv = document.getElementById('loading');
    const errorContainer = document.getElementById('error-container');
    const contentDiv = document.getElementById('content');

    try {
        const { data: { session }, error } = await authClient.auth.getSession();

        if (error || !session) {
            loadingDiv.style.display = 'none';
            errorContainer.style.display = 'block';
            setTimeout(() => {
                window.location.href = '/login';
            }, 2000);
            return;
        }

        accessToken = session.access_token;

        // Load preference options and existing preferences
        await loadPreferenceOptions();
        await loadExistingPreferences();

        loadingDiv.style.display = 'none';
        contentDiv.style.display = 'block';

    } catch (err) {
        console.error('Initialization failed:', err);
        loadingDiv.style.display = 'none';
        errorContainer.style.display = 'block';
    }
}

async function loadPreferenceOptions() {
    try {
        const response = await fetch('/api/preferences/options', {
            headers: {
                'Authorization': `Bearer ${accessToken}`
            }
        });

        if (!response.ok) throw new Error('Failed to load options');

        const options = await response.json();

        // Populate single-choice selects
        populateSelect('budget', options.budget);
        populateSelect('pace', options.pace);
        populateSelect('accommodationStyle', options.accommodationStyle);
        populateSelect('season', options.season);

        // Populate multi-choice checkboxes
        populateCheckboxes('interests', options.interests);
        populateCheckboxes('transport', options.transport);
        populateCheckboxes('foodPreferences', options.foodPreferences);

    } catch (error) {
        console.error('Error loading options:', error);
        showError('Failed to load preference options');
    }
}

async function loadExistingPreferences() {
    try {
        const response = await fetch('/api/preferences', {
            headers: {
                'Authorization': `Bearer ${accessToken}`
            }
        });

        if (response.status === 404) {
            // No preferences saved yet, that's okay
            return;
        }

        if (!response.ok) throw new Error('Failed to load preferences');

        const prefs = await response.json();

        // Set single-choice values
        if (prefs.budget) document.getElementById('budget').value = prefs.budget;
        if (prefs.pace) document.getElementById('pace').value = prefs.pace;
        if (prefs.accommodationStyle) document.getElementById('accommodationStyle').value = prefs.accommodationStyle;
        if (prefs.season) document.getElementById('season').value = prefs.season;

        // Set multi-choice checkboxes
        if (prefs.interests) {
            prefs.interests.forEach(interest => {
                const checkbox = document.querySelector(`input[name="interests"][value="${interest}"]`);
                if (checkbox) checkbox.checked = true;
            });
        }

        if (prefs.transport) {
            prefs.transport.forEach(t => {
                const checkbox = document.querySelector(`input[name="transport"][value="${t}"]`);
                if (checkbox) checkbox.checked = true;
            });
        }

        if (prefs.foodPreferences) {
            prefs.foodPreferences.forEach(food => {
                const checkbox = document.querySelector(`input[name="foodPreferences"][value="${food}"]`);
                if (checkbox) checkbox.checked = true;
            });
        }

    } catch (error) {
        console.error('Error loading existing preferences:', error);
    }
}

function populateSelect(elementId, options) {
    const select = document.getElementById(elementId);
    options.forEach(option => {
        const optionElement = document.createElement('option');
        optionElement.value = option.value;
        optionElement.textContent = option.label;
        select.appendChild(optionElement);
    });
}

function populateCheckboxes(containerId, options) {
    const container = document.getElementById(containerId);
    options.forEach(option => {
        const div = document.createElement('div');
        div.className = 'checkbox-item';

        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.id = `${containerId}-${option.value}`;
        checkbox.name = containerId;
        checkbox.value = option.value;

        const label = document.createElement('label');
        label.htmlFor = checkbox.id;
        label.textContent = option.label;
        label.style.fontWeight = 'normal';
        label.style.marginBottom = '0';

        div.appendChild(checkbox);
        div.appendChild(label);
        container.appendChild(div);
    });
}

function getCheckedValues(name) {
    const checkboxes = document.querySelectorAll(`input[name="${name}"]:checked`);
    return Array.from(checkboxes).map(cb => cb.value);
}

function showError(message) {
    const errorDiv = document.getElementById('error-message');
    errorDiv.textContent = message;
    errorDiv.style.display = 'block';
    setTimeout(() => {
        errorDiv.style.display = 'none';
    }, 5000);
}

function showSuccess(message) {
    const successDiv = document.getElementById('success-message');
    successDiv.textContent = message;
    successDiv.style.display = 'block';
    setTimeout(() => {
        successDiv.style.display = 'none';
    }, 5000);
}

// Handle form submission
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('preferences-form').addEventListener('submit', async (e) => {
        e.preventDefault();

        const saveButton = document.getElementById('save-button');
        saveButton.disabled = true;

        const preferences = {
            budget: document.getElementById('budget').value || null,
            pace: document.getElementById('pace').value || null,
            interests: getCheckedValues('interests'),
            accommodationStyle: document.getElementById('accommodationStyle').value || null,
            transport: getCheckedValues('transport'),
            foodPreferences: getCheckedValues('foodPreferences'),
            season: document.getElementById('season').value || null
        };

        try {
            const response = await fetch('/api/preferences', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${accessToken}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(preferences)
            });

            const result = await response.json();

            if (response.ok && result.success) {
                showSuccess('Preferences saved successfully! Redirecting...');
                setTimeout(() => {
                    window.location.href = '/app';
                }, 1500);
            } else {
                throw new Error(result.message || 'Failed to save preferences');
            }

        } catch (error) {
            console.error('Error saving preferences:', error);
            showError(error.message || 'Failed to save preferences');
        } finally {
            saveButton.disabled = false;
        }
    });

    // Initialize on page load
    init();
});

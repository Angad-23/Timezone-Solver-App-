/* =========================================================
   Shared color-coding + avatar helpers.
   Used by session.html (combobox) and roster.html (tables) so
   the same person always gets the same colored initials avatar
   everywhere in the app.
   ========================================================= */

const AVATAR_COLORS = ['--avatar-1', '--avatar-2', '--avatar-3', '--avatar-4', '--avatar-5', '--avatar-6'];

function hashString(str) {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
        hash = (hash * 31 + str.charCodeAt(i)) >>> 0;
    }
    return hash;
}

function colorVarFor(key) {
    const idx = hashString(key.toLowerCase()) % AVATAR_COLORS.length;
    return `var(${AVATAR_COLORS[idx]})`;
}

function initialsFor(name) {
    const parts = name.trim().split(/\s+/).filter(Boolean);
    if (parts.length === 0) return '?';
    if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

function avatarSpan(name) {
    const span = document.createElement('span');
    span.className = 'avatar';
    span.style.background = colorVarFor(name);
    span.textContent = initialsFor(name);
    return span;
}

// Decorate any td.person-cell with a colored initials avatar + name,
// and any td.subject-cell with a colored subject pill. Safe to call
// on pages that have neither — querySelectorAll just returns nothing.
function decorateColorCoding(root = document) {
    root.querySelectorAll('td.person-cell').forEach((td) => {
        const name = td.textContent.trim();
        if (!name) return;
        td.textContent = '';
        td.appendChild(avatarSpan(name));
        const nameSpan = document.createElement('span');
        nameSpan.className = 'person-name';
        nameSpan.textContent = name;
        td.appendChild(nameSpan);
    });

    root.querySelectorAll('td.subject-cell').forEach((td) => {
        const subject = td.textContent.trim();
        if (!subject) return;
        const color = colorVarFor(subject);
        td.textContent = '';
        const pill = document.createElement('span');
        pill.className = 'pill';
        pill.textContent = subject;
        pill.style.color = color;
        pill.style.background = `color-mix(in srgb, ${color} 14%, white)`;
        td.appendChild(pill);
    });
}

document.addEventListener('DOMContentLoaded', () => decorateColorCoding());
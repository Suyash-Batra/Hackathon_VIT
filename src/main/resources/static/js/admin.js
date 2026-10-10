
const token = localStorage.getItem("wildguardToken");

const allowedRoles = [
    "USER",
    "ADMIN",
    "FOREST_OFFICER",
    "RESCUE_OFFICER"
];

const allowedStatuses = ["ACTIVE", "PENDING", "INACTIVE"];

const userTable = document.getElementById("userTable");
const message = document.getElementById("message");

if (!token) {
    window.location.href = "/login";
}

async function apiRequest(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token,
            ...(options.headers || {})
        }
    });

    if (response.status === 401 || response.status === 403) {
        throw new Error("Access denied. Please log in with an authorized admin account.");
    }

    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
        throw new Error(data.message || "Request failed");
    }

    return data;
}

function showMessage(text, isError = false) {
    message.textContent = text;
    message.style.color = isError ? "#b42318" : "#287747";
}

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, function (character) {
        return {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"
        }[character];
    });
}

function createOptions(values, selectedValue) {
    return values.map(value =>
        `<option value="${value}" ${value === selectedValue ? "selected" : ""}>${value}</option>`
    ).join("");
}


function updateStatistics(metrics) {
    document.getElementById("totalUsers").textContent =
        metrics.totalUsers ?? 0;

    document.getElementById("activeUsers").textContent =
        metrics.activeUsers ?? 0;

    document.getElementById("pendingUsers").textContent =
        metrics.pendingUsers ?? 0;
}


function renderUsers(users) {
    if (users.length === 0) {
        userTable.innerHTML =
            '<tr><td colspan="6">No users found.</td></tr>';
        return;
    }

    userTable.innerHTML = users.map(user => `
        <tr>
            <td>${escapeHtml(user.id)}</td>
            <td>${escapeHtml(user.name)}</td>
            <td>${escapeHtml(user.email)}</td>
            <td>
                <select id="role-${escapeHtml(user.id)}">
                    ${createOptions(allowedRoles, user.role)}
                </select>
            </td>
            <td>
                <select id="status-${escapeHtml(user.id)}">
                    ${createOptions(allowedStatuses, user.status)}
                </select>
            </td>
            <td>
                <button type="button" data-action="role"
                    data-id="${escapeHtml(user.id)}">Save Role</button>
                <button type="button" data-action="status"
                    data-id="${escapeHtml(user.id)}">Save Status</button>
            </td>
        </tr>
    `).join("");
}

async function loadUsers() {
    try {
        const users = await apiRequest("/api/admin/users");
        renderUsers(users);
    } catch (error) {
        showMessage(error.message, true);
        userTable.innerHTML =
            '<tr><td colspan="6">Could not load users.</td></tr>';
        return;
    }

    try {
        const metrics = await apiRequest("/api/admin/metrics");
        updateStatistics(metrics);
    } catch (error) {
        showMessage(
            "Users loaded, but dashboard metrics could not be loaded: "
            + error.message,
            true
        );
    }
}


async function saveRole(id) {
    const role = document.getElementById(`role-${id}`).value;

    try {
        await apiRequest(`/api/admin/users/${id}/role`, {
            method: "PUT",
            body: JSON.stringify({ role })
        });

        showMessage("Role updated successfully.");
        await loadUsers();
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function saveStatus(id) {
    const status = document.getElementById(`status-${id}`).value;

    try {
        await apiRequest(`/api/admin/users/${id}/status`, {
            method: "PUT",
            body: JSON.stringify({ status })
        });

        showMessage("Account status updated successfully.");
        await loadUsers();
    } catch (error) {
        showMessage(error.message, true);
    }
}

userTable.addEventListener("click", function (event) {
    const button = event.target.closest("button[data-action]");

    if (!button) {
        return;
    }

    const id = button.dataset.id;

    if (button.dataset.action === "role") {
        saveRole(id);
    } else if (button.dataset.action === "status") {
        saveStatus(id);
    }
});

document.getElementById("logoutBtn").addEventListener("click", function () {
    localStorage.removeItem("wildguardToken");
    localStorage.removeItem("wildguardUser");
    window.location.href = "/login";
});

loadUsers();

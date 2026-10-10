// Shared helper for the three conservation pages (Rescue Cases, Resources, Programmes).
// These pages are served by the platform app, but their data comes from
// conservation-service, which runs on port 8084.

const conservationToken = localStorage.getItem("wildguardToken");

if (!conservationToken) {
    window.location.href = "/login";
}

// same computer as this page, port 8084
const CONSERVATION_API =
    window.location.protocol + "//" + window.location.hostname + ":8084";

function currentRole() {
    try {
        const saved = JSON.parse(localStorage.getItem("wildguardUser") || "{}");
        return String(saved.role || "USER").toUpperCase().replace("ROLE_", "");
    } catch (error) {
        return "USER";
    }
}

// Calls conservation-service with the login token. Returns the JSON answer.
// When the service says no (400, 403, 409 ...) it throws an Error with the service's message.
async function conservationRequest(path, method, body) {
    let response;

    try {
        response = await fetch(CONSERVATION_API + path, {
            method: method || "GET",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json",
                "Authorization": "Bearer " + conservationToken
            },
            body: body ? JSON.stringify(body) : undefined
        });
    } catch (error) {
        throw new Error(
            "Conservation service is not reachable. Check that it is running on port 8084."
        );
    }

    const data = await response.json().catch(() => ({}));

    if (response.status === 401) {
        throw new Error("Your login has expired. Please sign in again.");
    }

    if (!response.ok) {
        throw new Error(data.message || "Request failed.");
    }

    return data;
}

function escapeText(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[character]);
}

// UNDER_TREATMENT -> Under treatment
function labelOf(value) {
    if (!value) {
        return "";
    }

    const text = String(value).replace(/_/g, " ").toLowerCase();
    return text.charAt(0).toUpperCase() + text.slice(1);
}

// 2026-10-06T08:05:00 -> 06 Oct 2026, 08:05
function formatDateTime(value) {
    if (!value) {
        return "";
    }

    const date = new Date(value);

    if (isNaN(date.getTime())) {
        return String(value);
    }

    return date.toLocaleString("en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });
}

function formatDate(value) {
    if (!value) {
        return "";
    }

    const date = new Date(value);

    if (isNaN(date.getTime())) {
        return String(value);
    }

    return date.toLocaleDateString("en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric"
    });
}

// green or red line at the top of the page
function showPageMessage(text, isError) {
    const box = document.getElementById("pageMessage");

    if (!box) {
        return;
    }

    box.textContent = text || "";
    box.className = isError ? "page-message error" : "page-message";

    if (text) {
        box.scrollIntoView({ behavior: "smooth", block: "nearest" });
    }
}

// a small coloured label, for example the stage of a rescue case
function badge(value) {
    return '<span class="badge badge-' + escapeText(String(value || "").toLowerCase()) + '">'
        + escapeText(labelOf(value)) + "</span>";
}

const conservationLogout = document.getElementById("logoutButton");

if (conservationLogout) {
    conservationLogout.addEventListener("click", function () {
        localStorage.removeItem("wildguardToken");
        localStorage.removeItem("wildguardUser");
        window.location.href = "/login";
    });
}

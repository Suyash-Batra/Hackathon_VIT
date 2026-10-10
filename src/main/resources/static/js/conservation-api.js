
function getConservationToken() {
    return localStorage.getItem("wildguardToken");
}

const CONSERVATION_API =
    window.location.protocol + "//" + window.location.hostname + ":8084";

// Get the logged-in user's role
function currentRole() {
    try {
        const saved = JSON.parse(
            localStorage.getItem("wildguardUser") || "{}"
        );

        return String(saved.role || "USER")
            .toUpperCase()
            .replace("ROLE_", "");
    } catch (error) {
        return "USER";
    }
}

// Send a request to conservation-service
async function conservationRequest(path, method, body) {
    const token = getConservationToken();

    if (!token) {
        window.location.href = "/login";
        throw new Error("Please log in again.");
    }

    let response;

    try {
        response = await fetch(CONSERVATION_API + path, {
            method: method || "GET",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json",
                "Authorization": "Bearer " + token
            },
            body: body !== undefined && body !== null
                ? JSON.stringify(body)
                : undefined
        });
    } catch (error) {
        throw new Error(
            "Conservation service is not reachable. Check that it is running on port 8084."
        );
    }

    const data = await response.json().catch(() => ({}));

    if (response.status === 401) {
        throw new Error(
            "Your login token was rejected. Please log in again."
        );
    }

    if (!response.ok) {
        throw new Error(
            data.message || data.error || "Request failed."
        );
    }

    return data;
}

// Escape HTML text before displaying it
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

    const text = String(value)
        .replace(/_/g, " ")
        .toLowerCase();

    return text.charAt(0).toUpperCase() + text.slice(1);
}

// Format date and time
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

// Format date
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

// Display a success or error message on the page
function showPageMessage(text, isError) {
    const box = document.getElementById("pageMessage");

    if (!box) {
        return;
    }

    box.textContent = text || "";
    box.className = isError
        ? "page-message error"
        : "page-message";

    if (text) {
        box.scrollIntoView({
            behavior: "smooth",
            block: "nearest"
        });
    }
}

// Display a coloured status label
function badge(value) {
    return '<span class="badge badge-' +
        escapeText(String(value || "").toLowerCase()) +
        '">' +
        escapeText(labelOf(value)) +
        "</span>";
}

// Logout
const conservationLogout =
    document.getElementById("logoutButton");

if (conservationLogout) {
    conservationLogout.addEventListener("click", function () {
        localStorage.removeItem("wildguardToken");
        localStorage.removeItem("wildguardUser");

        window.location.href = "/login";
    });
}

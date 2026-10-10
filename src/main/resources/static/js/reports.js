
const reportsToken = localStorage.getItem("wildguardToken");
const reportTable = document.getElementById("reportTable");
const reportMessage = document.getElementById("reportMessage");
const filterError = document.getElementById("filterError");
const resultCount = document.getElementById("resultCount");

let currentReports = [];

if (!reportsToken) {
    window.location.href = "/login";
}

function escapeReportHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[character]);
}

async function requestReports(url) {
    const response = await fetch(url, {
        headers: {
            "Authorization": "Bearer " + reportsToken,
            "Accept": "application/json"
        }
    });

    if (!response.ok) {
        throw new Error(
            response.status === 401 || response.status === 403
                ? "Access denied. Please log in with an authorized account."
                : "Could not fetch reports. Check that the incident service is running."
        );
    }

    const data = await response.json();

    if (Array.isArray(data)) {
        return data;
    }

    if (Array.isArray(data.content)) {
        return data.content;
    }

    if (Array.isArray(data.data)) {
        return data.data;
    }

    throw new Error("Unexpected reports response format.");
}

function getFilters() {
    const filters = new URLSearchParams();

    ["zone", "from", "to", "severity", "type", "status"].forEach(id => {
        const value = document.getElementById(id).value.trim();

        if (value) {
            filters.set(id, value);
        }
    });

    return filters;
}

function renderReports(reports) {
    currentReports = reports;
    resultCount.textContent = `${reports.length} matching incident(s)`;

    if (reports.length === 0) {
        reportTable.innerHTML =
            '<tr><td colspan="8">No matching incidents found.</td></tr>';
        return;
    }

    reportTable.innerHTML = reports.map(report => `
        <tr>
            <td>${escapeReportHtml(report.id)}</td>
            <td>${escapeReportHtml(report.date)}</td>
            <td>${escapeReportHtml(report.zone)}</td>
            <td>${escapeReportHtml(report.type)}</td>
            <td>${escapeReportHtml(report.severity)}</td>
            <td>${escapeReportHtml(report.status)}</td>
            <td>${escapeReportHtml(report.location)}</td>
            <td>${escapeReportHtml(report.description)}</td>
        </tr>
    `).join("");
}

async function loadReports() {
    filterError.textContent = "";
    reportMessage.textContent = "";
    reportTable.innerHTML =
        '<tr><td colspan="8">Loading reports...</td></tr>';

    const from = document.getElementById("from").value;
    const to = document.getElementById("to").value;

    if (from && to && from > to) {
        filterError.textContent =
            "The From date must be on or before the To date.";
        reportTable.innerHTML =
            '<tr><td colspan="8">Please correct the date range.</td></tr>';
        resultCount.textContent = "No results loaded.";
        currentReports = [];
        return;
    }

    const filters = getFilters();
    const query = filters.toString();
    const url = "/api/incidents" + (query ? "?" + query : "");

    try {
        const reports = await requestReports(url);
        renderReports(reports);
    } catch (error) {
        currentReports = [];
        reportTable.innerHTML =
            '<tr><td colspan="8">Reports could not be loaded.</td></tr>';
        resultCount.textContent = "No results loaded.";
        reportMessage.textContent = error.message;
    }
}

function csvValue(value) {
    let text = String(value ?? "");

    if (/^[=+\-@\t\r]/.test(text)) {
        text = "'" + text;
    }

    return '"' + text.replace(/"/g, '""') + '"';
}

function exportCsv() {
    if (currentReports.length === 0) {
        reportMessage.textContent =
            "There are no matching records to export.";
        return;
    }

    const columns = [
        ["id", "Incident ID"],
        ["date", "Date"],
        ["zone", "Zone"],
        ["type", "Type"],
        ["severity", "Severity"],
        ["status", "Status"],
        ["location", "Location"],
        ["description", "Description"]
    ];

    const rows = [
        columns.map(column => csvValue(column[1])).join(","),
        ...currentReports.map(report =>
            columns.map(column => csvValue(report[column[0]])).join(",")
        )
    ];

    const blob = new Blob(
        ["\uFEFF" + rows.join("\r\n")],
        { type: "text/csv;charset=utf-8;" }
    );

    const link = document.createElement("a");
    const objectUrl = URL.createObjectURL(blob);

    link.href = objectUrl;
    link.download = "wildguard_incident_reports.csv";
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(objectUrl);

    reportMessage.textContent =
        `${currentReports.length} matching record(s) exported.`;
}

document.getElementById("filterForm").addEventListener("submit", event => {
    event.preventDefault();
    loadReports();
});

document.getElementById("resetFilters").addEventListener("click", () => {
    document.getElementById("filterForm").reset();
    loadReports();
});

document.getElementById("exportCsv").addEventListener("click", exportCsv);

document.getElementById("logoutButton").addEventListener("click", () => {
    localStorage.removeItem("wildguardToken");
    localStorage.removeItem("wildguardUser");
    window.location.href = "/login";
});

loadReports();

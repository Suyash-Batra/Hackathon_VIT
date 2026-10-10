// Programmes page. Data comes from conservation-service (/api/programmes).
// The progress % is calculated by the service from the recorded activities.

const programmeTable = document.getElementById("programmeTable");
const programmePanel = document.getElementById("detailPanel");

const programmeRole = currentRole();
const canEditProgrammes = programmeRole === "ADMIN" || programmeRole === "FOREST_OFFICER";

let openProgrammeId = null;

async function loadProgrammes() {
    try {
        const programmes = await conservationRequest("/api/programmes");
        renderProgrammes(programmes);
    } catch (error) {
        programmeTable.innerHTML =
            '<tr><td colspan="7" class="empty-row">Programmes could not be loaded.</td></tr>';
        showPageMessage(error.message, true);
    }
}

function progressBar(programme) {
    return `
        <div class="progress">
            <div class="progress-track">
                <div class="progress-fill ${programme.overdue ? "late" : ""}"
                     style="width: ${Number(programme.progressPercent)}%"></div>
            </div>
            <span class="progress-number">${Number(programme.progressPercent)}%</span>
        </div>`;
}

function renderProgrammes(programmes) {
    if (programmes.length === 0) {
        programmeTable.innerHTML =
            '<tr><td colspan="7" class="empty-row">No programmes yet.</td></tr>';
        return;
    }

    programmeTable.innerHTML = programmes.map(programme => `
        <tr class="clickable ${programme.id === openProgrammeId ? "selected" : ""}" data-id="${programme.id}">
            <td><strong>${escapeText(programme.programmeCode)}</strong></td>
            <td>${escapeText(programme.name)}
                ${programme.team ? `<span class="muted">${escapeText(programme.team)}</span>` : ""}</td>
            <td>${badge(programme.status)} ${programme.overdue ? badge("OVERDUE") : ""}</td>
            <td>${escapeText(programme.completedQuantity)} / ${escapeText(programme.targetQuantity)}
                <span class="muted">${escapeText(programme.unit || "")}</span></td>
            <td>${progressBar(programme)}</td>
            <td>${escapeText(formatDate(programme.dueDate) || "-")}</td>
            <td>${escapeText(programme.ownerStaffName || "-")}</td>
        </tr>
    `).join("");
}

programmeTable.addEventListener("click", function (event) {
    const row = event.target.closest("tr[data-id]");

    if (row) {
        openProgramme(Number(row.dataset.id), true);
    }
});

async function openProgramme(id, scroll) {
    try {
        const programme = await conservationRequest("/api/programmes/" + id);
        openProgrammeId = id;
        renderProgramme(programme);

        programmeTable.querySelectorAll("tr").forEach(row => {
            row.classList.toggle("selected", Number(row.dataset.id) === id);
        });

        if (scroll) {
            programmePanel.scrollIntoView({ behavior: "smooth", block: "start" });
        }
    } catch (error) {
        showPageMessage(error.message, true);
    }
}

function renderProgramme(programme) {
    programmePanel.hidden = false;

    document.getElementById("detailName").textContent =
        programme.programmeCode + " " + programme.name;
    document.getElementById("detailStatus").innerHTML =
        badge(programme.status) + (programme.overdue ? " " + badge("OVERDUE") : "");

    // show the formula with the real numbers
    document.getElementById("detailFormula").textContent =
        "Progress = " + programme.completedQuantity + " completed / "
        + programme.targetQuantity + " target x 100 = " + programme.progressPercent + "%";

    const activities = programme.activities || [];
    const table = document.getElementById("activityTable");

    if (activities.length === 0) {
        table.innerHTML = '<tr><td colspan="4" class="empty-row">No work recorded yet.</td></tr>';
    } else {
        table.innerHTML = activities.map(activity => `
            <tr>
                <td>${escapeText(formatDate(activity.completedAt))}</td>
                <td>${escapeText(activity.description)}</td>
                <td>${escapeText(activity.quantityCompleted)}</td>
                <td>${escapeText(activity.recordedByName || "-")}</td>
            </tr>
        `).join("");
    }

    // work can be recorded by officers and admins, and not on a completed programme
    document.getElementById("activityCard").hidden =
        !canEditProgrammes || programme.status === "COMPLETED";
}

document.getElementById("activityForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    try {
        const programme = await conservationRequest(
            "/api/programmes/" + openProgrammeId + "/activities", "POST", {
                description: document.getElementById("activityDescription").value.trim(),
                quantityCompleted: Number(document.getElementById("quantityCompleted").value)
            });

        event.target.reset();
        showPageMessage(programme.programmeCode + " is now at " + programme.progressPercent + "%.");
        renderProgramme(programme);
        loadProgrammes();
    } catch (error) {
        showPageMessage(error.message, true);
    }
});

document.getElementById("programmeForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    try {
        const saved = await conservationRequest("/api/programmes", "POST", {
            name: document.getElementById("programmeName").value.trim(),
            description: document.getElementById("programmeDescription").value.trim() || null,
            targetQuantity: Number(document.getElementById("targetQuantity").value),
            unit: document.getElementById("unit").value.trim() || null,
            startDate: document.getElementById("startDate").value || null,
            dueDate: document.getElementById("dueDate").value || null,
            status: "PLANNED",
            ownerStaffName: document.getElementById("ownerStaffName").value.trim() || null
        });

        event.target.reset();
        showPageMessage("Programme " + saved.programmeCode + " started.");
        openProgrammeId = saved.id;
        await loadProgrammes();
        openProgramme(saved.id, false);
    } catch (error) {
        showPageMessage(error.message, true);
    }
});

if (!canEditProgrammes) {
    document.getElementById("newProgrammeCard").hidden = true;
}

loadProgrammes();

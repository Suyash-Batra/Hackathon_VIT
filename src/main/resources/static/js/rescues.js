// Rescue Cases page. Data comes from conservation-service (/api/rescues).

const rescueTable = document.getElementById("rescueTable");
const detailPanel = document.getElementById("detailPanel");
const stageFilter = document.getElementById("stageFilter");

const ALL_STAGES = [
    "REPORTED", "RESCUED", "UNDER_TREATMENT", "REHABILITATION", "RELEASED"
];

let openCaseId = null;

// ---------- list ----------

async function loadRescues() {
    let path = "/api/rescues";

    if (stageFilter.value === "ACTIVE") {
        path += "?activeOnly=true";
    } else if (stageFilter.value) {
        path += "?stage=" + encodeURIComponent(stageFilter.value);
    }

    try {
        const rescues = await conservationRequest(path);
        renderRescues(rescues);
        loadStats();
    } catch (error) {
        rescueTable.innerHTML =
            '<tr><td colspan="8" class="empty-row">Rescue cases could not be loaded.</td></tr>';
        showPageMessage(error.message, true);
    }
}

function renderRescues(rescues) {
    if (rescues.length === 0) {
        rescueTable.innerHTML =
            '<tr><td colspan="8" class="empty-row">No rescue cases found.</td></tr>';
        return;
    }

    rescueTable.innerHTML = rescues.map(rescue => `
        <tr class="clickable ${rescue.id === openCaseId ? "selected" : ""}" data-id="${rescue.id}">
            <td><strong>${escapeText(rescue.rescueCode)}</strong></td>
            <td>${escapeText(rescue.summary)}</td>
            <td>${escapeText(rescue.incidentCode || "-")}</td>
            <td>${escapeText(rescue.animalCode || "-")}</td>
            <td>${badge(rescue.stage)}</td>
            <td>${rescue.releaseReviewStatus === "NONE" ? "-" : badge(rescue.releaseReviewStatus)}</td>
            <td>${escapeText(rescue.assignedStaffName || "-")}</td>
            <td>${escapeText(rescue.daysOpen)}</td>
        </tr>
    `).join("");
}

// the three numbers at the top come from the summary of the service
async function loadStats() {
    try {
        const all = await conservationRequest("/api/rescues");
        const summary = await conservationRequest("/api/conservation/summary");

        document.getElementById("statActive").textContent = summary.activeRescues;
        document.getElementById("statReviews").textContent = summary.pendingReleaseReviews;
        document.getElementById("statClosed").textContent =
            all.filter(rescue => !rescue.active).length;
    } catch (error) {
        // the list already shows the error message
    }
}

rescueTable.addEventListener("click", function (event) {
    const row = event.target.closest("tr[data-id]");

    if (row) {
        openCase(Number(row.dataset.id));
    }
});

stageFilter.addEventListener("change", loadRescues);

// ---------- open a new case ----------

document.getElementById("rescueForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    const body = {
        incidentId: document.getElementById("incidentId").value.trim() || null,
        animalId: document.getElementById("animalId").value.trim() || null,
        assignedStaffName: document.getElementById("assignedStaffName").value.trim() || null,
        summary: document.getElementById("summary").value.trim()
    };

    try {
        const saved = await conservationRequest("/api/rescues", "POST", body);
        event.target.reset();
        showPageMessage("Rescue case " + saved.rescueCode + " opened.");
        openCaseId = saved.id;
        await loadRescues();
        renderCase(saved);
    } catch (error) {
        showPageMessage(error.message, true);
    }
});

// ---------- one case ----------

async function openCase(id) {
    try {
        const rescue = await conservationRequest("/api/rescues/" + id);
        openCaseId = id;
        renderCase(rescue);

        rescueTable.querySelectorAll("tr").forEach(row => {
            row.classList.toggle("selected", Number(row.dataset.id) === id);
        });

        detailPanel.scrollIntoView({ behavior: "smooth", block: "start" });
    } catch (error) {
        showPageMessage(error.message, true);
    }
}

function renderCase(rescue) {
    detailPanel.hidden = false;

    document.getElementById("detailCode").textContent = rescue.rescueCode;
    document.getElementById("detailStage").innerHTML = badge(rescue.stage);
    document.getElementById("detailReview").innerHTML =
        rescue.releaseReviewStatus === "NONE"
            ? ""
            : "Release review: " + badge(rescue.releaseReviewStatus);
    document.getElementById("detailSummary").textContent = rescue.summary;

    renderSteps(rescue);
    renderTimeline(rescue.timeline || []);
    renderFacts(rescue);
    renderActions(rescue);
    renderCaseResources(rescue.allocations || []);
}

// Reported > Rescued > Under treatment > Rehabilitation > Released (or Transferred)
function renderSteps(rescue) {
    const stages = rescue.stage === "TRANSFERRED"
        ? ["REPORTED", "RESCUED", "TRANSFERRED"]
        : ALL_STAGES;
    const position = stages.indexOf(rescue.stage);

    document.getElementById("stageSteps").innerHTML = stages.map((stage, index) => {
        let css = "";

        if (index < position) {
            css = "done";
        } else if (index === position) {
            css = "now";
        }

        return `<li class="${css}">${escapeText(labelOf(stage))}</li>`;
    }).join("");
}

function renderTimeline(entries) {
    const list = document.getElementById("timeline");

    if (entries.length === 0) {
        list.innerHTML = '<li><div class="note">Nothing recorded yet.</div></li>';
        return;
    }

    list.innerHTML = entries.map(entry => `
        <li class="kind-${escapeText(String(entry.kind).toLowerCase())}">
            <div class="when">${escapeText(formatDateTime(entry.at))}
                &middot; ${escapeText(entry.person || "")}
                &middot; ${escapeText(labelOf(entry.kind))}</div>
            <div class="what">${escapeText(entry.title)}</div>
            ${entry.notes ? `<div class="note">${escapeText(entry.notes)}</div>` : ""}
        </li>
    `).join("");
}

function renderFacts(rescue) {
    const facts = [
        ["Incident", rescue.incidentCode || "-"],
        ["Animal", rescue.animalCode || "-"],
        ["Responsible", rescue.assignedStaffName || "-"],
        ["Opened by", rescue.openedByName || "-"],
        ["Opened", formatDateTime(rescue.openedAt)],
        ["Days open", rescue.daysOpen],
        ["Closed", rescue.closedAt ? formatDateTime(rescue.closedAt) : "-"],
        ["Destination", rescue.destination || "-"],
        ["Outcome", rescue.outcome || "-"],
        ["Review decided by", rescue.releaseReviewedByName || "-"]
    ];

    document.getElementById("facts").innerHTML = facts.map(fact => `
        <div><dt>${escapeText(fact[0])}</dt><dd>${escapeText(fact[1])}</dd></div>
    `).join("");
}

// The service tells us what this user may do (canAddTreatment, allowedNextStages ...),
// so we only draw the forms that are allowed.
function renderActions(rescue) {
    const box = document.getElementById("actions");
    let html = "";

    if (rescue.canAddTreatment) {
        html += `
            <form class="stack-form" id="treatmentForm">
                <label>Treatment given
                    <input id="treatmentText" type="text" maxlength="500" required>
                </label>
                <label>Notes (optional)
                    <input id="treatmentNotes" type="text" maxlength="1000">
                </label>
                <div class="form-actions"><button type="submit">Add Treatment</button></div>
            </form>`;
    }

    if (rescue.allowedNextStages && rescue.allowedNextStages.length > 0) {
        html += `
            <form class="stack-form" id="stageForm">
                <label>Move to stage
                    <select id="nextStage">
                        ${rescue.allowedNextStages.map(stage =>
                            `<option value="${escapeText(stage)}">${escapeText(labelOf(stage))}</option>`
                        ).join("")}
                    </select>
                </label>
                <label>Destination (needed for a transfer)
                    <input id="stageDestination" type="text" maxlength="200">
                </label>
                <label>Notes / outcome (optional)
                    <input id="stageNotes" type="text" maxlength="500">
                </label>
                <div class="form-actions"><button type="submit">Change Stage</button></div>
            </form>`;
    }

    if (rescue.canRequestReview) {
        html += `
            <form class="stack-form" id="reviewForm">
                <label>Why is the animal ready for release?
                    <input id="reviewNotes" type="text" maxlength="1000">
                </label>
                <div class="form-actions"><button type="submit">Request Release Review</button></div>
            </form>`;
    }

    if (rescue.canDecideReview) {
        html += `
            <form class="stack-form" id="decisionForm">
                <label>Decision notes (optional)
                    <input id="decisionNotes" type="text" maxlength="1000">
                </label>
                <div class="form-actions">
                    <button type="submit" data-approved="true">Approve Release</button>
                    <button type="submit" data-approved="false" class="danger-button">Reject</button>
                </div>
            </form>`;
    }

    if (!html) {
        if (rescue.stage === "REHABILITATION" && rescue.releaseReviewStatus === "REQUESTED") {
            html = '<p class="plain-text">Waiting for a Forest Officer or Administrator to decide the release review.</p>';
        } else if (!rescue.active) {
            html = '<p class="plain-text">This case is closed.</p>';
        } else {
            html = '<p class="plain-text">No actions are available for your role.</p>';
        }
    }

    box.innerHTML = html;
    connectActionForms(rescue);
}

function connectActionForms(rescue) {
    const base = "/api/rescues/" + rescue.id;

    const treatmentForm = document.getElementById("treatmentForm");
    if (treatmentForm) {
        treatmentForm.addEventListener("submit", event => {
            event.preventDefault();
            sendAction(base + "/treatments", "POST", {
                treatment: document.getElementById("treatmentText").value.trim(),
                notes: document.getElementById("treatmentNotes").value.trim() || null
            }, "Treatment recorded.");
        });
    }

    const stageForm = document.getElementById("stageForm");
    if (stageForm) {
        stageForm.addEventListener("submit", event => {
            event.preventDefault();
            const notes = document.getElementById("stageNotes").value.trim() || null;
            sendAction(base + "/stage", "PATCH", {
                stage: document.getElementById("nextStage").value,
                destination: document.getElementById("stageDestination").value.trim() || null,
                notes: notes,
                outcome: notes
            }, "Stage changed.");
        });
    }

    const reviewForm = document.getElementById("reviewForm");
    if (reviewForm) {
        reviewForm.addEventListener("submit", event => {
            event.preventDefault();
            sendAction(base + "/release-review", "POST", {
                notes: document.getElementById("reviewNotes").value.trim() || null
            }, "Release review requested.");
        });
    }

    const decisionForm = document.getElementById("decisionForm");
    if (decisionForm) {
        decisionForm.addEventListener("submit", event => {
            event.preventDefault();
            const approved = event.submitter.dataset.approved === "true";
            sendAction(base + "/release-decision", "POST", {
                approved: approved,
                notes: document.getElementById("decisionNotes").value.trim() || null
            }, approved ? "Release approved." : "Release rejected.");
        });
    }
}

// every action answers with the updated case, so we just draw it again
async function sendAction(path, method, body, successText) {
    try {
        const rescue = await conservationRequest(path, method, body);
        showPageMessage(rescue.rescueCode + ": " + successText);
        renderCase(rescue);
        loadRescues();
    } catch (error) {
        showPageMessage(error.message, true);
    }
}

function renderCaseResources(allocations) {
    const box = document.getElementById("detailResources");

    if (allocations.length === 0) {
        box.innerHTML = 'Nothing allocated. Use the <a href="/resources">Resources</a> page to allocate.';
        return;
    }

    box.innerHTML = allocations.map(allocation => `
        <div><strong>${escapeText(allocation.resourceCode)}</strong>
            ${escapeText(allocation.resourceName)}
            <span class="muted">since ${escapeText(formatDateTime(allocation.allocatedAt))}</span></div>
    `).join("");
}

// a plain user cannot use this part, the service would answer 403
if (currentRole() === "USER") {
    document.getElementById("newRescueCard").hidden = true;
}

loadRescues();

// Resources page: vehicles and equipment, and who has them now.
// Data comes from conservation-service (/api/resources and /api/allocations).

const resourceTable = document.getElementById("resourceTable");
const historyTable = document.getElementById("historyTable");
const statusFilter = document.getElementById("statusFilter");

const role = currentRole();
const canManage = role === "ADMIN" || role === "FOREST_OFFICER";          // add, maintenance, override
const canAllocate = canManage || role === "RESCUE_OFFICER";               // allocate and release

async function loadResources() {
    let path = "/api/resources";

    if (statusFilter.value) {
        path += "?status=" + encodeURIComponent(statusFilter.value);
    }

    try {
        const resources = await conservationRequest(path);
        renderResources(resources);
        loadStatsAndHistory();
    } catch (error) {
        resourceTable.innerHTML =
            '<tr><td colspan="6" class="empty-row">Resources could not be loaded.</td></tr>';
        showPageMessage(error.message, true);
    }
}

function renderResources(resources) {
    if (resources.length === 0) {
        resourceTable.innerHTML =
            '<tr><td colspan="6" class="empty-row">No resources found.</td></tr>';
        return;
    }

    resourceTable.innerHTML = resources.map(resource => {
        const current = resource.currentAllocation;

        return `
        <tr>
            <td><strong>${escapeText(resource.resourceCode)}</strong></td>
            <td>${escapeText(resource.name)}
                ${resource.notes ? `<span class="muted">${escapeText(resource.notes)}</span>` : ""}</td>
            <td>${escapeText(labelOf(resource.category))}</td>
            <td>${badge(resource.status)}</td>
            <td>${current
                ? `<strong>${escapeText(current.targetCode)}</strong>
                   <span class="muted">${escapeText(labelOf(current.targetType))}, since
                   ${escapeText(formatDateTime(current.allocatedAt))}</span>`
                : "-"}</td>
            <td>${actionsFor(resource)}</td>
        </tr>`;
    }).join("");
}

// which buttons to show in the last column
function actionsFor(resource) {
    const current = resource.currentAllocation;
    let html = "";

    if (current && canAllocate) {
        html += `<button type="button" class="small-button light-button"
                         data-action="release" data-id="${current.id}">Release</button> `;
    }

    if (resource.status === "AVAILABLE" && canAllocate) {
        html += allocateControls(resource.id, false);
    }

    // an officer can take an item away from its current user (override)
    if (current && canManage) {
        html += allocateControls(resource.id, true);
    }

    if (resource.status === "AVAILABLE" && canManage) {
        html += `<button type="button" class="small-button light-button"
                         data-action="status" data-id="${resource.id}"
                         data-status="MAINTENANCE">Maintenance</button>`;
    }

    if (resource.status === "MAINTENANCE" && canManage) {
        html += `<button type="button" class="small-button light-button"
                         data-action="status" data-id="${resource.id}"
                         data-status="AVAILABLE">Back in service</button>`;
    }

    return html ? `<div class="allocate-row">${html}</div>` : "-";
}

function allocateControls(resourceId, override) {
    return `
        <select data-target-type aria-label="Give to">
            <option value="PATROL">Patrol</option>
            <option value="RESCUE_CASE">Rescue case</option>
        </select>
        <input data-target-id type="text" maxlength="40" placeholder="ID" aria-label="Patrol or rescue case ID">
        <button type="button" class="small-button" data-action="allocate"
                data-id="${resourceId}" data-override="${override}">
            ${override ? "Reassign" : "Allocate"}</button>`;
}

resourceTable.addEventListener("click", async function (event) {
    const button = event.target.closest("button[data-action]");

    if (!button) {
        return;
    }

    const action = button.dataset.action;

    try {
        if (action === "release") {
            const released = await conservationRequest(
                "/api/allocations/" + button.dataset.id + "/release", "PATCH");
            showPageMessage(released.resourceCode + " released from " + released.targetCode + ".");
        }

        if (action === "status") {
            const changed = await conservationRequest(
                "/api/resources/" + button.dataset.id + "/status", "PATCH",
                { status: button.dataset.status });
            showPageMessage(changed.resourceCode + " is now " + labelOf(changed.status).toLowerCase() + ".");
        }

        if (action === "allocate") {
            const cell = button.closest("td");
            const targetType = cell.querySelector("[data-target-type]").value;
            const typedId = cell.querySelector("[data-target-id]").value.trim();

            if (!typedId) {
                showPageMessage("Type the ID of the patrol or rescue case first.", true);
                return;
            }

            const targetId = targetType === "RESCUE_CASE"
                ? await rescueNumberFor(typedId)
                : typedId;

            const saved = await conservationRequest("/api/allocations", "POST", {
                resourceId: Number(button.dataset.id),
                targetType: targetType,
                targetId: targetId,
                override: button.dataset.override === "true"
            });

            showPageMessage(saved.resourceCode + " allocated to " + saved.targetCode + ".");
        }

        loadResources();
    } catch (error) {
        showPageMessage(error.message, true);
    }
});

// The user may type the case code (RES-031). The service wants the number of the case,
// so we look it up in the list of rescue cases.
async function rescueNumberFor(typed) {
    if (/^\d+$/.test(typed)) {
        return typed;
    }

    const rescues = await conservationRequest("/api/rescues");
    const match = rescues.find(rescue =>
        String(rescue.rescueCode).toUpperCase() === typed.toUpperCase());

    if (!match) {
        throw new Error("Rescue case " + typed + " was not found.");
    }

    return String(match.id);
}

async function loadStatsAndHistory() {
    try {
        const summary = await conservationRequest("/api/conservation/summary");

        document.getElementById("statAvailable").textContent = summary.availableResources;
        document.getElementById("statAllocated").textContent = summary.allocatedResources;
        document.getElementById("statMaintenance").textContent = summary.maintenanceResources;

        const allocations = await conservationRequest("/api/allocations");

        if (allocations.length === 0) {
            historyTable.innerHTML =
                '<tr><td colspan="5" class="empty-row">No allocations yet.</td></tr>';
            return;
        }

        historyTable.innerHTML = allocations.slice(0, 15).map(allocation => `
            <tr>
                <td><strong>${escapeText(allocation.resourceCode)}</strong>
                    <span class="muted">${escapeText(allocation.resourceName)}</span></td>
                <td>${escapeText(allocation.targetCode)}
                    <span class="muted">${escapeText(labelOf(allocation.targetType))}</span></td>
                <td>${escapeText(formatDateTime(allocation.allocatedAt))}</td>
                <td>${escapeText(allocation.allocatedByName || "-")}</td>
                <td>${allocation.active
                    ? badge("ALLOCATED")
                    : escapeText(formatDateTime(allocation.releasedAt))
                        + `<span class="muted">${escapeText(allocation.releasedByName || "")}</span>`}</td>
            </tr>
        `).join("");
    } catch (error) {
        // the inventory table already shows the error message
    }
}

document.getElementById("resourceForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    try {
        const saved = await conservationRequest("/api/resources", "POST", {
            resourceCode: document.getElementById("resourceCode").value.trim(),
            category: document.getElementById("category").value,
            name: document.getElementById("resourceName").value.trim(),
            notes: document.getElementById("resourceNotes").value.trim() || null
        });

        event.target.reset();
        showPageMessage("Resource " + saved.resourceCode + " added.");
        loadResources();
    } catch (error) {
        showPageMessage(error.message, true);
    }
});

statusFilter.addEventListener("change", loadResources);

// only officers and admins add resources
if (!canManage) {
    document.getElementById("newResourceCard").hidden = true;
}

loadResources();

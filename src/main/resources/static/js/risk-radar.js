const riskGrid = document.getElementById("riskGrid");
const riskMessage = document.getElementById("riskMessage");
const refreshRisk = document.getElementById("refreshRisk");

function escapeRiskText(value) {
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

function getRiskClass(level) {
    const normalized = String(level || "").toLowerCase();

    if (["low", "medium", "high", "critical"].includes(normalized)) {
        return normalized;
    }

    return "low";
}

function showRiskMessage(message, isError) {
    riskMessage.textContent = message;
    riskMessage.className = isError ? "risk-message error" : "risk-message";
}

function renderRiskZones(zones) {
    if (!Array.isArray(zones) || zones.length === 0) {
        riskGrid.innerHTML =
            '<div class="risk-empty">No zone risk data is available yet.</div>';
        showRiskMessage("No risk records were returned by the service.", false);
        return;
    }

    riskGrid.innerHTML = zones.map(function (zone) {
        const score = Math.max(0, Math.min(100, Number(zone.score) || 0));
        const level = getRiskClass(zone.level);
        const reasons = Array.isArray(zone.reasons) ? zone.reasons : [];
        const recommendation = zone.recommendedAction || "Ask an authorized officer to review this zone.";

        const reasonContent = reasons.length
            ? reasons.map(function (reason) {
                return "<li>" + escapeRiskText(reason) + "</li>";
            }).join("")
            : "<li>No contributing factors were supplied.</li>";

        return `
            <article class="risk-card">
                <div class="risk-card-header">
                    <div>
                        <h3>${escapeRiskText(zone.name || "Unnamed zone")}</h3>
                        <span class="zone-code">${escapeRiskText(zone.zoneCode || zone.zoneId || "No zone ID")}</span>
                    </div>
                    <span class="risk-level ${level}">${escapeRiskText(level.toUpperCase())}</span>
                </div>

                <div class="risk-score">
                    <strong>${score}</strong>
                    <span>/ 100 risk score</span>
                </div>

                <div class="risk-meter" role="progressbar"
                     aria-label="Risk score"
                     aria-valuemin="0"
                     aria-valuemax="100"
                     aria-valuenow="${score}">
                    <div class="risk-meter-fill ${level}" style="width: ${score}%"></div>
                </div>

                <h4>Why this zone has this score</h4>
                <ul class="risk-reasons">${reasonContent}</ul>

                <div class="risk-recommendation">
                    <strong>Recommended next step</strong>
                    ${escapeRiskText(recommendation)}
                </div>
            </article>
        `;
    }).join("");

    showRiskMessage("Loaded " + zones.length + " zone risk record(s). Scores are sorted by the backend.", false);
}

async function loadRiskZones() {
    const token = localStorage.getItem("wildguardToken");

    if (!token) {
        window.location.href = "/login";
        return;
    }

    refreshRisk.disabled = true;
    riskGrid.innerHTML = "";
    showRiskMessage("Loading risk data...", false);

    try {
        const response = await fetch("/api/risk-radar/zones", {
            method: "GET",
            headers: {
                "Authorization": "Bearer " + token,
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            localStorage.removeItem("wildguardToken");
            localStorage.removeItem("wildguardUser");
            window.location.href = "/login";
            return;
        }

        if (response.status === 403) {
            showRiskMessage("Your account does not have permission to view Risk Radar.", true);
            return;
        }

        if (!response.ok) {
            throw new Error(
                response.status === 503
                    ? "A required microservice is not available yet. Start the dependent services after their APIs are ready."
                    : "The Risk Radar request failed with status " + response.status + "."
            );
        }

        const zones = await response.json();
        renderRiskZones(zones);
    } catch (error) {
        riskGrid.innerHTML =
            '<div class="risk-empty">Risk data could not be loaded. Check the platform service and its dependent APIs.</div>';
        showRiskMessage(error.message || "Unable to load Risk Radar data.", true);
    } finally {
        refreshRisk.disabled = false;
    }
}

refreshRisk.addEventListener("click", loadRiskZones);

const logoutButton = document.getElementById("logoutButton");

if (logoutButton) {
    logoutButton.addEventListener("click", function () {
        localStorage.removeItem("wildguardToken");
        localStorage.removeItem("wildguardUser");
        window.location.href = "/login";
    });
}

loadRiskZones();
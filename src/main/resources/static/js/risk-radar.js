(function () {
    const API = "http://localhost:8083";
    const riskGrid = document.getElementById("riskGrid");
    const riskMessage = document.getElementById("riskMessage");
    const refreshRisk = document.getElementById("refreshRisk");
    const incidentRows = document.getElementById("incidentRows");
    const incidentMessage = document.getElementById("incidentMessage");
    const incidentForm = document.getElementById("incidentForm");
    let incidents = [];

    function esc(value) { return String(value ?? "").replace(/[&<>"']/g, c => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;", "'":"&#39;"}[c])); }
    function riskClass(level) { const v = String(level || "low").toLowerCase(); return ["low","medium","high","critical"].includes(v) ? v : "low"; }
    function message(el, text, error) { el.textContent = text; el.className = error ? "risk-message error" : "risk-message"; }
    async function request(path, options = {}) {
        const response = await fetch(API + path, { ...options, headers: { "Accept":"application/json", ...(options.body ? {"Content-Type":"application/json"} : {}), ...(options.headers || {}) } });
        if (!response.ok) { const body = await response.text(); throw new Error(body || `Request failed (${response.status})`); }
        return response.status === 204 ? null : response.json();
    }
    function renderZones(zones) {
        if (!zones.length) { riskGrid.innerHTML = '<div class="risk-empty">No incidents have been recorded yet. Report an incident below to start the Risk Radar.</div>'; message(riskMessage,"No incident-based risk records are available.",false); return; }
        riskGrid.innerHTML = zones.map(zone => {
            const score = Math.max(0,Math.min(100,Number(zone.score)||0)); const level = riskClass(zone.level);
            const reasons = (zone.reasons || []).map(r => `<li>${esc(r)}</li>`).join("");
            return `<article class="risk-card"><div class="risk-card-header"><div><h3>${esc(zone.name)}</h3><span class="zone-code">${esc(zone.zoneCode)} · ${Number(zone.incidentCount)||0} incident(s)</span></div><span class="risk-level ${level}">${esc(level.toUpperCase())}</span></div><div class="risk-score"><strong>${score}</strong><span>/ 100 risk score</span></div><div class="risk-meter" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="${score}"><div class="risk-meter-fill ${level}" style="width:${score}%"></div></div><h4>Contributing factors</h4><ul class="risk-reasons">${reasons || "<li>No contributing factors supplied.</li>"}</ul><div class="risk-recommendation"><strong>Recommended next step</strong>${esc(zone.recommendedAction)}</div></article>`;
        }).join("");
        message(riskMessage,`Loaded ${zones.length} location risk record(s), calculated from incident severity and status.`,false);
    }
    async function loadRisk() {
        refreshRisk.disabled = true; riskGrid.innerHTML = ""; message(riskMessage,"Calculating location risks from incident data...",false);
        try { renderZones(await request("/api/risk-radar/zones")); }
        catch (e) { riskGrid.innerHTML = '<div class="risk-empty">Risk data could not be loaded. Confirm operations-service is running on port 8083.</div>'; message(riskMessage,e.message,true); }
        finally { refreshRisk.disabled = false; }
    }
    function renderIncidents() {
        const severity = document.getElementById("filterSeverity").value;
        const status = document.getElementById("filterStatus").value;
        const rows = incidents.filter(i => (!severity || String(i.severity||"").toUpperCase()===severity) && (!status || String(i.status||"").toUpperCase()===status));
        if (!rows.length) { incidentRows.innerHTML='<tr><td class="empty-cell" colspan="7">No incidents match these filters.</td></tr>'; return; }
        incidentRows.innerHTML = rows.map(i => `<tr><td>#${i.id}</td><td><strong>${esc(i.type)}</strong><br><span>${esc(i.description)}</span></td><td><span class="severity-tag">${esc(i.severity || "UNSET")}</span></td><td>${esc(i.location)}</td><td>${esc(i.assignedOfficer || "Unassigned")}</td><td>${esc(i.status)}</td><td><div class="action-group"><input aria-label="Officer for incident ${i.id}" id="officer-${i.id}" placeholder="Officer name"><button type="button" data-action="assign" data-id="${i.id}">Assign</button><select aria-label="New status for incident ${i.id}" id="status-${i.id}"><option>REPORTED</option><option>ASSIGNED</option><option>IN_PROGRESS</option><option>RESOLVED</option><option>CLOSED</option></select><button type="button" data-action="status" data-id="${i.id}">Update</button></div></td></tr>`).join("");
    }
    async function loadIncidents() {
        message(incidentMessage,"Loading incidents...",false);
        try { incidents = await request("/api/incidents"); renderIncidents(); message(incidentMessage,`Loaded ${incidents.length} incident(s).`,false); }
        catch(e) { incidentRows.innerHTML='<tr><td class="empty-cell" colspan="7">Could not load incidents. Start operations-service on port 8083.</td></tr>'; message(incidentMessage,e.message,true); }
    }
    incidentForm.addEventListener("submit", async e => {
        e.preventDefault(); const body = { type:document.getElementById("incidentType").value.trim(), severity:document.getElementById("incidentSeverity").value, location:document.getElementById("incidentLocation").value.trim(), description:document.getElementById("incidentDescription").value.trim(), status:"REPORTED" };
        try { await request("/api/incidents",{method:"POST",body:JSON.stringify(body)}); incidentForm.reset(); message(incidentMessage,"Incident reported successfully.",false); await Promise.all([loadIncidents(),loadRisk()]); }
        catch(err) { message(incidentMessage,"Could not report incident: "+err.message,true); }
    });
    incidentRows.addEventListener("click", async e => {
        const button = e.target.closest("button[data-action]"); if (!button) return;
        const id = button.dataset.id;
        try {
            if (button.dataset.action === "assign") { const officer = document.getElementById(`officer-${id}`).value.trim(); if (!officer) { message(incidentMessage,"Enter an officer name before assigning.",true); return; } await request(`/api/incidents/${id}/assign?officer=${encodeURIComponent(officer)}`,{method:"PUT"}); }
            else { const status = document.getElementById(`status-${id}`).value; await request(`/api/incidents/${id}/status?status=${encodeURIComponent(status)}`,{method:"PUT"}); }
            message(incidentMessage,`Incident #${id} updated successfully.`,false); await Promise.all([loadIncidents(),loadRisk()]);
        } catch(err) { message(incidentMessage,"Update failed: "+err.message,true); }
    });
    document.getElementById("filterSeverity").addEventListener("change",renderIncidents);
    document.getElementById("filterStatus").addEventListener("change",renderIncidents);
    document.getElementById("refreshIncidents").addEventListener("click",()=>Promise.all([loadIncidents(),loadRisk()]));
    refreshRisk.addEventListener("click",loadRisk);
    const logout = document.getElementById("logoutButton"); if (logout) logout.addEventListener("click",function(){localStorage.removeItem("wildguardToken");localStorage.removeItem("wildguardUser");window.location.href="/login";});
    Promise.all([loadRisk(),loadIncidents()]);
})();

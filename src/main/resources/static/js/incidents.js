(function () {
    const API = "http://localhost:8083";
    const $ = id => document.getElementById(id);
    const form = $("incidentForm"), rowsEl = $("incidentRows");
    let incidents = [];
    const esc = value => String(value ?? "").replace(/[&<>"']/g, c => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;", "'":"&#39;"}[c]));
    const norm = value => String(value || "").toUpperCase();
    function setMessage(id, text, error) { const el=$(id); el.textContent=text; el.classList.toggle("error",!!error); }
    async function api(path, options={}) {
        const response = await fetch(API + path, { ...options, headers: { Accept:"application/json", ...(options.body ? {"Content-Type":"application/json"} : {}), ...(options.headers || {}) } });
        if (!response.ok) { const detail=await response.text(); throw new Error(detail || `HTTP ${response.status}`); }
        return response.status === 204 ? null : response.json();
    }
    function renderStats() {
        $("totalCount").textContent=incidents.length;
        $("openCount").textContent=incidents.filter(i=>!["RESOLVED","CLOSED"].includes(norm(i.status))).length;
        $("urgentCount").textContent=incidents.filter(i=>["HIGH","CRITICAL"].includes(norm(i.severity))).length;
        $("resolvedCount").textContent=incidents.filter(i=>["RESOLVED","CLOSED"].includes(norm(i.status))).length;
    }
    function renderRows() {
        const severity=$("filterSeverity").value, status=$("filterStatus").value;
        const filtered=incidents.filter(i=>(!severity||norm(i.severity)===severity)&&(!status||norm(i.status)===status));
        if (!filtered.length) { rowsEl.innerHTML='<tr><td class="empty-cell" colspan="7">No incidents match these filters.</td></tr>'; return; }
        rowsEl.innerHTML=filtered.map(i=>{
            const severityClass=["LOW","MEDIUM","HIGH","CRITICAL"].includes(norm(i.severity))?norm(i.severity).toLowerCase():"low";
            const statuses=["REPORTED","ASSIGNED","IN_PROGRESS","RESOLVED","CLOSED"];
            const statusOptions=statuses.map(s=>`<option value="${s}" ${norm(i.status)===s?"selected":""}>${s.replace("_"," ")}</option>`).join("");
            return `<tr><td>#${i.id}</td><td><strong>${esc(i.type||"Incident")}</strong><small>${esc(i.description||"No description supplied")}</small></td><td><span class="severity-tag ${severityClass}">${esc(norm(i.severity)||"UNSET")}</span></td><td>${esc(i.location||"Unspecified")}</td><td>${esc(i.assignedOfficer||"Unassigned")}</td><td><span class="status-tag">${esc(norm(i.status)||"REPORTED")}</span></td><td><div class="action-group"><input id="officer-${i.id}" aria-label="Officer for incident ${i.id}" placeholder="Officer name"><button type="button" data-action="assign" data-id="${i.id}">Assign</button><select id="status-${i.id}" aria-label="Status for incident ${i.id}">${statusOptions}</select><button type="button" data-action="status" data-id="${i.id}">Update</button></div></td></tr>`;
        }).join("");
    }
    async function loadIncidents() {
        setMessage("listMessage","Loading incidents…",false);
        try { incidents=await api("/api/incidents"); renderStats(); renderRows(); setMessage("listMessage",`Loaded ${incidents.length} incident(s).`,false); }
        catch(error) { rowsEl.innerHTML='<tr><td class="empty-cell" colspan="7">Could not load incidents. Confirm operations-service is running on port 8083.</td></tr>'; setMessage("listMessage",error.message,true); }
    }
    form.addEventListener("submit",async event=>{
        event.preventDefault(); const body={type:$("incidentType").value.trim(),severity:$("incidentSeverity").value,location:$("incidentLocation").value.trim(),description:$("incidentDescription").value.trim(),status:"REPORTED"};
        const submit=form.querySelector("button[type=submit]"); submit.disabled=true; setMessage("formMessage","Submitting incident…",false);
        try { await api("/api/incidents",{method:"POST",body:JSON.stringify(body)}); form.reset(); setMessage("formMessage","Incident reported successfully.",false); await loadIncidents(); }
        catch(error) { setMessage("formMessage",`Could not report incident: ${error.message}`,true); }
        finally { submit.disabled=false; }
    });
    rowsEl.addEventListener("click",async event=>{
        const button=event.target.closest("button[data-action]"); if(!button)return;
        const id=button.dataset.id;
        try {
            if(button.dataset.action==="assign") { const officer=$("officer-"+id).value.trim(); if(!officer){setMessage("listMessage","Enter an officer name before assigning.",true);return;} await api(`/api/incidents/${id}/assign?officer=${encodeURIComponent(officer)}`,{method:"PUT"}); }
            else { const status=$("status-"+id).value; await api(`/api/incidents/${id}/status?status=${encodeURIComponent(status)}`,{method:"PUT"}); }
            setMessage("listMessage",`Incident #${id} updated successfully.`,false); await loadIncidents();
        } catch(error) { setMessage("listMessage",`Update failed: ${error.message}`,true); }
    });
    $("filterSeverity").addEventListener("change",renderRows); $("filterStatus").addEventListener("change",renderRows); $("refreshIncidents").addEventListener("click",loadIncidents);
    const logout=$("logoutButton"); if(logout)logout.addEventListener("click",()=>{localStorage.removeItem("wildguardToken");localStorage.removeItem("wildguardUser");window.location.href="/login";});
    loadIncidents();
})();

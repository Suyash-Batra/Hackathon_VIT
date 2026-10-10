<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>WildGuard - Wildlife Records</title>
    <!-- QRCode CDN for browser-side QR rendering -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/qrcodejs/1.0.0/qrcode.min.js"></script>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; }
        body { display: flex; min-height: 100vh; background-color: #f4f6f3; color: #1e293b; }

        /* Left Sidebar */
        .sidebar { width: 260px; background-color: #0d3826; color: #ffffff; padding: 24px 16px; display: flex; flex-direction: column; justify-content: space-between; flex-shrink: 0; }
        .brand { display: flex; align-items: center; gap: 12px; margin-bottom: 32px; padding: 0 8px; }
        .brand-icon { width: 36px; height: 36px; background: #1b5337; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 18px; }
        .brand-text h2 { font-size: 18px; font-weight: 700; line-height: 1.1; }
        .brand-text span { font-size: 11px; color: #7fad93; }

        .nav-section-title { font-size: 10px; font-weight: 700; text-transform: uppercase; letter-spacing: 1px; color: #629075; margin-bottom: 12px; padding: 0 12px; }
        .nav-links { list-style: none; display: flex; flex-direction: column; gap: 4px; }
        .nav-links a { display: flex; align-items: center; gap: 12px; padding: 10px 14px; color: #c4d9cb; text-decoration: none; border-radius: 8px; font-size: 14px; font-weight: 500; transition: background 0.2s, color 0.2s; }
        .nav-links a:hover { background: rgba(255, 255, 255, 0.08); color: #ffffff; }
        .nav-links a.active { background: rgba(255, 255, 255, 0.16); color: #ffffff; font-weight: 600; }
        .nav-badge { margin-left: auto; font-size: 10px; color: #8cb89d; background: rgba(255, 255, 255, 0.08); padding: 2px 6px; border-radius: 4px; }

        .sidebar-footer { background: #134630; padding: 16px; border-radius: 12px; font-size: 12px; }
        .sidebar-footer strong { display: block; margin-bottom: 4px; color: #fff; }
        .sidebar-footer p { color: #8cb89d; line-height: 1.4; font-size: 11px; }

        /* Main Workspace Content */
        .main-wrapper { flex: 1; display: flex; flex-direction: column; overflow-y: auto; }
        .topbar { background: #ffffff; padding: 16px 36px; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; }
        .topbar-subtitle { font-size: 10px; font-weight: 700; letter-spacing: 1.2px; text-transform: uppercase; color: #2d6a4f; margin-bottom: 2px; }
        .topbar-title { font-size: 22px; font-weight: 800; color: #0f172a; }
        .topbar-actions { display: flex; align-items: center; gap: 12px; }
        .role-pill { background: #eef2eb; color: #1e3a29; padding: 6px 14px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .btn-signout { background: #0d3826; color: #fff; border: none; padding: 8px 18px; border-radius: 20px; font-size: 12px; font-weight: 600; cursor: pointer; text-decoration: none; }

        .content { padding: 32px 36px; max-width: 1200px; width: 100%; margin: 0 auto; }
        .welcome-header { margin-bottom: 24px; }
        .welcome-header h1 { font-size: 26px; font-weight: 800; color: #0f172a; margin-bottom: 6px; }
        .welcome-header p { font-size: 14px; color: #64748b; }

        /* Card container */
        .card { background: #ffffff; border-radius: 16px; border: 1px solid #e5e9e2; padding: 24px; margin-bottom: 24px; box-shadow: 0 1px 3px rgba(0,0,0,0.02); }
        .card-title { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 16px; display: flex; align-items: center; gap: 8px; }

        /* Form Controls */
        .form-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; }
        .form-group { display: flex; flex-direction: column; gap: 6px; }
        .form-group label { font-size: 11px; font-weight: 700; text-transform: uppercase; color: #64748b; letter-spacing: 0.5px; }
        .form-control { padding: 10px 14px; border-radius: 8px; border: 1px solid #cbd5e1; font-size: 13px; background: #fafafa; outline: none; transition: border 0.2s; }
        .form-control:focus { border-color: #2d6a4f; background: #fff; }

        .btn-primary { background: #0d3826; color: #fff; border: none; padding: 12px 20px; border-radius: 8px; font-weight: 600; font-size: 13px; cursor: pointer; transition: background 0.2s; margin-top: 16px; }
        .btn-primary:hover { background: #134630; }

        /* Filter bar */
        .filter-bar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
        .filter-bar label { font-size: 13px; font-weight: 600; color: #475569; }

        /* Tables */
        .table-responsive { overflow-x: auto; }
        table { width: 100%; border-collapse: separate; border-spacing: 0; font-size: 13px; }
        th { background: #f8faf6; color: #475569; font-weight: 600; text-transform: uppercase; font-size: 11px; letter-spacing: 0.5px; padding: 12px 16px; border-bottom: 1px solid #e2e8f0; text-align: left; }
        td { padding: 14px 16px; border-bottom: 1px solid #edf2e9; color: #1e293b; vertical-align: middle; }
        tr:last-child td { border-bottom: none; }
        tr:hover td { background-color: #fafcf9; }

        /* Health Status Badges */
        .status-badge { display: inline-block; padding: 4px 10px; border-radius: 20px; font-size: 11px; font-weight: 700; text-transform: uppercase; }
        .status-healthy { background: #e8f5e9; color: #2e7d32; }
        .status-injured { background: #fff3e0; color: #ef6c00; }
        .status-critical { background: #ffebee; color: #c62828; }
        .status-obs { background: #e3f2fd; color: #1565c0; }

        .btn-action { background: #eef2eb; color: #0d3826; border: 1px solid #cbd5e1; padding: 6px 12px; border-radius: 6px; font-size: 12px; font-weight: 600; cursor: pointer; transition: all 0.2s; }
        .btn-action:hover { background: #0d3826; color: #fff; }

        /* Modal */
        .modal { display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(13, 56, 38, 0.45); backdrop-filter: blur(3px); justify-content: center; align-items: center; z-index: 1000; }
        .modal-content { background: #ffffff; border-radius: 16px; padding: 32px; width: 340px; text-align: center; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); border: 1px solid #e2e8f0; }
        .modal-content h3 { font-size: 18px; font-weight: 800; color: #0f172a; margin-bottom: 6px; }
        .modal-content p { font-size: 12px; color: #64748b; margin-bottom: 16px; }
        .modal-qr-box { display: flex; justify-content: center; margin: 18px 0; background: #f8faf6; padding: 16px; border-radius: 12px; border: 1px dashed #cbd5e1; }
        .btn-close { width: 100%; background: #0d3826; color: #fff; border: none; padding: 10px; border-radius: 8px; font-weight: 600; cursor: pointer; font-size: 13px; }
    </style>
</head>
<body>

    <!-- Left Theme Sidebar -->
    <aside class="sidebar">
        <div>
            <div class="brand">
                <div class="brand-icon">🌿</div>
                <div class="brand-text">
                    <h2>WildGuard</h2>
                    <span>Conservation platform</span>
                </div>
            </div>

            <div class="nav-section-title">Workspace</div>
            <ul class="nav-links">
                <li><a href="/wildlife/animals" class="active"><span>🐾</span> Wildlife Records</a></li>
                <li><a href="/wildlife/habitats"><span>🌱</span> Habitats & Water</a></li>
                <li><a href="#"><span>🎯</span> Risk Radar <span class="nav-badge">Sync</span></a></li>
                <li><a href="#"><span>🛡️</span> Patrols <span class="nav-badge">Ops</span></a></li>
                <li><a href="#"><span>🚑</span> Rescue Cases <span class="nav-badge">Ops</span></a></li>
            </ul>
        </div>

        <div class="sidebar-footer">
            <strong>Protect wildlife. Preserve nature.</strong>
            <p>Every report and observation supports field conservation efforts.</p>
        </div>
    </aside>

    <!-- Main Section -->
    <div class="main-wrapper">
        <header class="topbar">
            <div>
                <div class="topbar-subtitle">Wildlife Conservation Platform</div>
                <div class="topbar-title">Animal Registry & QR Passport</div>
            </div>
            <div class="topbar-actions">
                <span class="role-pill">Role: ADMIN</span>
                <a href="#" class="btn-signout">Sign out</a>
            </div>
        </header>

        <main class="content">
            <div class="welcome-header">
                <h1>Tracked Animal Registry</h1>
                <p>Register new specimens, monitor clinical health statuses, and generate field-scannable QR passports.</p>
            </div>

            <!-- Card: Register Animal -->
            <div class="card">
                <div class="card-title"><span>📝</span> Register New Animal Record</div>
                <form action="/wildlife/animals/add" method="post">
                    <div class="form-grid">
                        <div class="form-group">
                            <label>Animal Code</label>
                            <input type="text" name="animalCode" class="form-control" placeholder="e.g. AN-1045" required />
                        </div>

                        <div class="form-group">
                            <label>Species</label>
                            <select name="speciesId" class="form-control" required>
                                <option value="">-- Select Species --</option>
                                <c:forEach var="species" items="${speciesList}">
                                    <option value="${species.id}">${species.commonName} (${species.scientificName})</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-group">
                            <label>Estimated Age</label>
                            <input type="text" name="estimatedAge" class="form-control" placeholder="e.g. 4 years" />
                        </div>

                        <div class="form-group">
                            <label>Health Status</label>
                            <select name="healthStatus" class="form-control">
                                <option value="HEALTHY">HEALTHY</option>
                                <option value="UNDER_OBSERVATION">UNDER OBSERVATION</option>
                                <option value="INJURED">INJURED</option>
                                <option value="CRITICAL">CRITICAL</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label>Designated Zone</label>
                            <select name="zoneId" class="form-control">
                                <option value="">-- Select Zone --</option>
                                <c:forEach var="zone" items="${zones}">
                                    <option value="${zone.id}">${zone.name} (${zone.zoneCode})</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-group">
                            <label>Field Notes</label>
                            <input type="text" name="notes" class="form-control" placeholder="Location or behavioral notes" />
                        </div>
                    </div>

                    <button type="submit" class="btn-primary">Save Animal Record</button>
                </form>
            </div>

            <!-- Card: Animals Table -->
            <div class="card">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                    <div class="card-title" style="margin-bottom:0;"><span>📋</span> Active Tracked Wildlife</div>

                    <!-- Health Filter Dropdown -->
                    <form action="/wildlife/animals" method="get" class="filter-bar" style="margin-bottom:0;">
                        <label>Filter Health:</label>
                        <select name="healthStatus" class="form-control" style="width: auto; padding: 6px 12px;" onchange="this.form.submit()">
                            <option value="">All Health Statuses</option>
                            <option value="HEALTHY" ${filterHealth == 'HEALTHY' ? 'selected' : ''}>HEALTHY</option>
                            <option value="UNDER_OBSERVATION" ${filterHealth == 'UNDER_OBSERVATION' ? 'selected' : ''}>UNDER OBSERVATION</option>
                            <option value="INJURED" ${filterHealth == 'INJURED' ? 'selected' : ''}>INJURED</option>
                            <option value="CRITICAL" ${filterHealth == 'CRITICAL' ? 'selected' : ''}>CRITICAL</option>
                        </select>
                    </form>
                </div>

                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th>Animal Code</th>
                                <th>Species ID</th>
                                <th>Estimated Age</th>
                                <th>Health Status</th>
                                <th>Zone ID</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="animal" items="${animals}">
                                <tr>
                                    <td><strong>${animal.animalCode}</strong></td>
                                    <td>${animal.speciesId}</td>
                                    <td>${animal.estimatedAge != null ? animal.estimatedAge : 'N/A'}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${animal.healthStatus == 'HEALTHY'}"><span class="status-badge status-healthy">Healthy</span></c:when>
                                            <c:when test="${animal.healthStatus == 'UNDER_OBSERVATION'}"><span class="status-badge status-obs">Observation</span></c:when>
                                            <c:when test="${animal.healthStatus == 'INJURED'}"><span class="status-badge status-injured">Injured</span></c:when>
                                            <c:otherwise><span class="status-badge status-critical">Critical</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>${animal.zoneId != null ? animal.zoneId : 'Unassigned'}</td>
                                    <td>
                                        <button class="btn-action" onclick="openQrModal('${animal.animalCode}', '${animal.id}', '${animal.healthStatus}')">
                                            QR Passport
                                        </button>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty animals}">
                                <tr>
                                    <td colspan="6" style="text-align: center; color: #94a3b8; padding: 24px;">No animal records found matching current criteria.</td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>

    <!-- QR Modal -->
    <div id="qrModal" class="modal">
        <div class="modal-content">
            <h3 id="modalTitle">Animal Passport</h3>
            <p id="modalHealth">Secure QR verification record</p>
            <div class="modal-qr-box">
                <div id="qrcode"></div>
            </div>
            <button class="btn-close" onclick="closeQrModal()">Close Passport</button>
        </div>
    </div>

    <script>
        let qrInstance = null;
        function openQrModal(code, id, health) {
            document.getElementById('modalTitle').innerText = 'Passport: ' + code;
            document.getElementById('modalHealth').innerText = 'Health: ' + health + ' • ID: #' + id;
            const container = document.getElementById('qrcode');
            container.innerHTML = '';
            qrInstance = new QRCode(container, {
                text: 'https://wildguard.internal/animals/' + id,
                width: 140,
                height: 140,
                colorDark: "#0d3826",
                colorLight: "#ffffff"
            });
            document.getElementById('qrModal').style.display = 'flex';
        }
        function closeQrModal() {
            document.getElementById('qrModal').style.display = 'none';
        }
    </script>
</body>
</html>
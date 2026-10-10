<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>WildGuard - Habitats & Water Watchlist</title>
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

        .card { background: #ffffff; border-radius: 16px; border: 1px solid #e5e9e2; padding: 24px; margin-bottom: 24px; box-shadow: 0 1px 3px rgba(0,0,0,0.02); }
        .card-title { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 16px; display: flex; align-items: center; gap: 8px; }

        /* Tables */
        .table-responsive { overflow-x: auto; }
        table { width: 100%; border-collapse: separate; border-spacing: 0; font-size: 13px; }
        th { background: #f8faf6; color: #475569; font-weight: 600; text-transform: uppercase; font-size: 11px; letter-spacing: 0.5px; padding: 12px 16px; border-bottom: 1px solid #e2e8f0; text-align: left; }
        td { padding: 14px 16px; border-bottom: 1px solid #edf2e9; color: #1e293b; vertical-align: middle; }
        tr:last-child td { border-bottom: none; }
        tr:hover td { background-color: #fafcf9; }

        /* Status & Toggles */
        .status-badge { display: inline-block; padding: 4px 10px; border-radius: 20px; font-size: 11px; font-weight: 700; text-transform: uppercase; }
        .status-normal { background: #e8f5e9; color: #2e7d32; }
        .status-low { background: #ffebee; color: #c62828; }

        .btn-toggle-low { background: #fef2f2; color: #b91c1c; border: 1px solid #fecaca; padding: 6px 14px; border-radius: 6px; font-size: 12px; font-weight: 600; cursor: pointer; transition: all 0.2s; }
        .btn-toggle-low:hover { background: #dc2626; color: #fff; }

        .btn-toggle-normal { background: #f0fdf4; color: #15803d; border: 1px solid #bbf7d0; padding: 6px 14px; border-radius: 6px; font-size: 12px; font-weight: 600; cursor: pointer; transition: all 0.2s; }
        .btn-toggle-normal:hover { background: #16a34a; color: #fff; }
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
                <li><a href="/wildlife/animals"><span>🐾</span> Wildlife Records</a></li>
                <li><a href="/wildlife/habitats" class="active"><span>🌱</span> Habitats & Water</a></li>
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
                <div class="topbar-title">Habitats & Water Sources Watchlist</div>
            </div>
            <div class="topbar-actions">
                <span class="role-pill">Role: ADMIN</span>
                <a href="#" class="btn-signout">Sign out</a>
            </div>
        </header>

        <main class="content">
            <div class="welcome-header">
                <h1>Ecosystem Watchlist</h1>
                <p>Monitor critical zone hydration levels and ecosystem stability parameters that feed into the Risk Radar.</p>
            </div>

            <!-- Card: Water Sources -->
            <div class="card">
                <div class="card-title"><span>💧</span> Monitored Water Sources</div>
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th>Source Name</th>
                                <th>Zone ID</th>
                                <th>Current Status</th>
                                <th>Risk Impact Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="w" items="${waterSources}">
                                <tr>
                                    <td><strong>${w.name}</strong></td>
                                    <td>Zone #${w.zoneId}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${w.status == 'LOW'}">
                                                <span class="status-badge status-low">LOW (Water Stress)</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-badge status-normal">NORMAL</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <form action="/wildlife/water-sources/${w.id}/toggle" method="post" style="display:inline;">
                                            <input type="hidden" name="currentStatus" value="${w.status}" />
                                            <c:choose>
                                                <c:when test="${w.status == 'LOW'}">
                                                    <button type="submit" class="btn-toggle-normal">Mark as NORMAL</button>
                                                </c:when>
                                                <c:otherwise>
                                                    <button type="submit" class="btn-toggle-low">Mark as LOW</button>
                                                </c:otherwise>
                                            </c:choose>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty waterSources}">
                                <tr>
                                    <td colspan="4" style="text-align: center; color: #94a3b8; padding: 24px;">No water sources recorded.</td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Card: Habitats -->
            <div class="card">
                <div class="card-title"><span>🌲</span> Zone Habitat Conditions</div>
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th>Zone ID</th>
                                <th>Habitat Type</th>
                                <th>Condition Status</th>
                                <th>Inspection Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="h" items="${habitats}">
                                <tr>
                                    <td><strong>Zone #${h.zoneId}</strong></td>
                                    <td>${h.habitatType}</td>
                                    <td><span class="status-badge status-normal">${h.conditionStatus}</span></td>
                                    <td style="color: #64748b;">${h.lastInspectedAt != null ? h.lastInspectedAt : 'Verified'}</td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty habitats}">
                                <tr>
                                    <td colspan="4" style="text-align: center; color: #94a3b8; padding: 24px;">No habitat conditions recorded.</td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>

</body>
</html>
WildGuard Member 3 - Incident Management + Risk Radar

This patch is designed for the current main-branch WildGuard theme.

Files to copy into the existing repository, preserving folder structure:
- src/main/java/com/example/Hackathon/controller/PageController.java
- src/main/java/com/example/Hackathon/config/SecurityConfig.java
- src/main/resources/static/js/shell.js
- src/main/resources/templates/incidents.html
- src/main/resources/static/css/incidents.css
- src/main/resources/static/js/incidents.js
- src/main/resources/templates/risk-radar.html
- src/main/resources/static/css/risk-radar.css
- src/main/resources/static/js/risk-radar.js

Incident UI URL: http://localhost:8081/incidents
Risk Radar URL: http://localhost:8081/risk-radar
Operations API base URL: http://localhost:8083

Run the root application in one terminal:
  .\mvnw.cmd spring-boot:run

Run the operations service in another terminal:
  .\mvnw.cmd -f .\operations-service\pom.xml spring-boot:run

Keep both terminals running. The operations service must be reachable on port 8083.

The Incident Management page supports creating incidents, listing incidents, severity/status filters,
assigning an officer, and changing status. Risk Radar groups incidents by location and displays a
rule-based risk score with contributing factors and recommended actions.

Security note: the operations-service security config in the existing patch currently permits the
incident/risk API without authentication for local development. Do not deploy this configuration
publicly without adding appropriate authentication and authorization.

Database configuration note: do not commit database passwords. Use environment variables or a local
untracked configuration file for credentials.

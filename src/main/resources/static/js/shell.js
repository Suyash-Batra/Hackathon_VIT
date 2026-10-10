(function () {
    let user = {};

    try {
        const savedUser = JSON.parse(
            localStorage.getItem("wildguardUser") || "{}"
        );
        user = savedUser.user || savedUser;
    } catch (error) {
        user = {};
    }

    let role = String(user.role || "USER").toUpperCase();
    role = role.replace("ROLE_", "");

    const roleNames = {
        ADMIN: "Admin",
        USER: "User",
        FOREST_OFFICER: "Forest Officer",
        RESCUE_OFFICER: "Rescue Officer"
    };

    const path = window.location.pathname.replace(/\/$/, "") || "/";

    const menus = {
        ADMIN: [
            { label: "Dashboard", icon: "⌂", href: "/dashboard" },
            { label: "Risk Radar", icon: "◎", href: "/risk-radar" },
            { label: "User Management", icon: "♙", href: "/admin" },
            { label: "Incidents", icon: "!", upcoming: true },
            { label: "Patrols", icon: "⌖", upcoming: true },
            { label: "Rescue Cases", icon: "♡", upcoming: true },
            { label: "Reports", icon: "▤", href: "/reports" }
        ],
        USER: [
            { label: "Dashboard", icon: "⌂", href: "/dashboard" },
            { label: "Report an Incident", icon: "!", upcoming: true },
            { label: "My Reports", icon: "▤", upcoming: true },
            { label: "Conservation Alerts", icon: "♧", upcoming: true }
        ],
        FOREST_OFFICER: [
            { label: "Dashboard", icon: "⌂", href: "/dashboard" },
            { label: "Risk Radar", icon: "◎", href: "/risk-radar" },
            { label: "Incidents", icon: "!", upcoming: true },
            { label: "Patrols", icon: "⌖", upcoming: true },
            { label: "Reports", icon: "▤", href: "/reports" }
        ],
        RESCUE_OFFICER: [
            { label: "Dashboard", icon: "⌂", href: "/dashboard" },
            { label: "Risk Radar", icon: "◎", href: "/risk-radar" },
            { label: "Rescue Cases", icon: "♡", upcoming: true },
            { label: "Incidents", icon: "!", upcoming: true },
            { label: "Reports", icon: "▤", href: "/reports" }
        ]
    };

    const activeMenus = menus[role] || menus.USER;
    const navigation = document.getElementById("sideNav");

    if (navigation) {
        navigation.innerHTML = "";

        activeMenus.forEach(function (item) {
            if (item.href) {
                const link = document.createElement("a");
                link.href = item.href;
                link.className = "nav-link";

                if (item.href === path) {
                    link.classList.add("active");
                    link.setAttribute("aria-current", "page");
                }

                const icon = document.createElement("span");
                icon.className = "nav-icon";
                icon.textContent = item.icon;

                const label = document.createElement("span");
                label.textContent = item.label;

                link.append(icon, label);
                navigation.appendChild(link);
            } else {
                const itemElement = document.createElement("div");
                itemElement.className = "nav-link nav-disabled";

                const icon = document.createElement("span");
                icon.className = "nav-icon";
                icon.textContent = item.icon;

                const label = document.createElement("span");
                label.textContent = item.label;

                const status = document.createElement("small");
                status.textContent = "Coming soon";

                itemElement.append(icon, label, status);
                navigation.appendChild(itemElement);
            }
        });
    }

    const roleBadge = document.getElementById("topRole");

    if (roleBadge) {
        roleBadge.textContent = roleNames[role] || "User";
    }

    const pageTitle = document.getElementById("pageTitle");

    if (pageTitle) {
        if (path === "/admin") {
            pageTitle.textContent = "User Management";
        } else if (path === "/reports") {
            pageTitle.textContent = "Reports";
        } else if (path === "/risk-radar") {
            pageTitle.textContent = "Wildlife Risk Radar";
        } else {
            pageTitle.textContent = "Your Dashboard";
        }
    }
})();
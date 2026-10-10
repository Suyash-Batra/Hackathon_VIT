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
            { label: "User Management", icon: "♙", href: "/admin" },
            { label: "Animals", icon: "🐘", href: "/animals" },
            { label: "Habitats", icon: "🌳", href: "/habitats" },
            { label: "Rescue Cases", icon: "♡", href: "/rescues" },
            { label: "Resources", icon: "▣", href: "/resources" },
            { label: "Programmes", icon: "♧", href: "/programmes" }
        ],

        USER: [
            { label: "Dashboard", icon: "⌂", href: "/dashboard" },
            { label: "Animals", icon: "🐘", href: "/animals" },
            { label: "Habitats", icon: "🌳", href: "/habitats" },
        ],

        FOREST_OFFICER: [
            { label: "Dashboard", icon: "⌂", href: "/dashboard" },
            { label: "Risk Radar", icon: "◎", href: "/risk-radar" },
            { label: "Animals", icon: "🐘", href: "/animals" },
            { label: "Habitats", icon: "🌳", href: "/habitats" },
            { label: "Rescue Cases", icon: "♡", href: "/rescues" },
            { label: "Resources", icon: "▣", href: "/resources" },
            { label: "Programmes", icon: "♧", href: "/programmes" },
            { label: "Reports", icon: "▤", href: "/reports" }
        ],

        RESCUE_OFFICER: [
            { label: "Dashboard", icon: "⌂", href: "/dashboard" },
            { label: "Risk Radar", icon: "◎", href: "/risk-radar" },
            { label: "Animals", icon: "🐘", href: "/animals" },
            { label: "Habitats", icon: "🌳", href: "/habitats" },
            { label: "Rescue Cases", icon: "♡", href: "/rescues" },
            { label: "Resources", icon: "▣", href: "/resources" },
            { label: "Programmes", icon: "♧", href: "/programmes" },
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

                if (
                    item.href === path ||
                    (item.href !== "/dashboard" &&
                     path.startsWith(item.href + "/"))
                ) {
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
        const pageTitles = {
            "/dashboard": "Your Dashboard",
            "/admin": "User Management",
            "/rescues": "Rescue Cases",
            "/resources": "Resources",
            "/programmes": "Programmes",
            "/animals": "Animal Management",
            "/habitats": "Habitat Management"
        };

        pageTitle.textContent = pageTitles[path] || "Your Dashboard";
    }
})();

const token = localStorage.getItem("wildguardToken");
const storedUser = localStorage.getItem("wildguardUser");

if (!token || !storedUser) {
    window.location.replace("/login");
} else {
    try {
        const user = JSON.parse(storedUser);

        document.getElementById("userName").textContent =
            user.name || "Conservationist";

        document.getElementById("userEmail").textContent =
            "Email: " + (user.email || "");

        document.getElementById("userRole").textContent =
            "Role: " + (user.role || "USER");
    } catch (error) {
        localStorage.removeItem("wildguardToken");
        localStorage.removeItem("wildguardUser");
        window.location.replace("/login");
    }
}

document.getElementById("logoutButton").addEventListener("click", function () {
    localStorage.removeItem("wildguardToken");
    localStorage.removeItem("wildguardUser");
    window.location.replace("/login");
});

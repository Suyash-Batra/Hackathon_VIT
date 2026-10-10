
const loginForm = document.getElementById("loginForm");
const signupForm = document.getElementById("signupForm");

const message = document.getElementById("message");
const submitButton = document.getElementById("submitButton");

function showMessage(text, type) {
    message.textContent = text;
    message.className = "message " + type;
}

function saveLogin(data) {
    if (!data.token) {
        throw new Error("The server did not return an authentication token.");
    }

    localStorage.setItem("wildguardToken", data.token);
    localStorage.setItem("wildguardUser", JSON.stringify({
        userId: data.userId,
        name: data.name,
        email: data.email,
        role: data.role
    }));

    if (data.role === "ADMIN") {
        window.location.href = "/admin";
    } else {
        window.location.href = "/dashboard";
    }
}

async function sendRequest(url, body) {
    const response = await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
    });

    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
        throw new Error(
            data.message || data.error || "Request failed. Please try again."
        );
    }

    return data;
}

if (signupForm) {
    signupForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const name = document.getElementById("name").value.trim();
        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;
        const confirmPassword =
            document.getElementById("confirmPassword").value;

        if (!name) {
            showMessage("Please enter your name.", "error");
            return;
        }

        if (password !== confirmPassword) {
            showMessage("The passwords do not match.", "error");
            return;
        }

        submitButton.disabled = true;
        submitButton.textContent = "Creating account...";

        try {
            const data = await sendRequest("/api/auth/signup", {
                name: name,
                email: email,
                password: password
            });

			showMessage(
			    data.message ||
			    "Account created successfully. Please wait for admin approval before logging in.",
			    "success"
			);

			signupForm.reset();
        } catch (error) {
            showMessage(error.message, "error");
        } finally {
            submitButton.disabled = false;
            submitButton.textContent = "Create account";
        }
    });
}

if (loginForm) {
    loginForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;

        submitButton.disabled = true;
        submitButton.textContent = "Signing in...";

        try {
            const data = await sendRequest("/api/auth/login", {
                email: email,
                password: password
            });

            saveLogin(data);
        } catch (error) {
            showMessage(error.message, "error");
        } finally {
            submitButton.disabled = false;
            submitButton.textContent = "Sign in";
        }
    });
}

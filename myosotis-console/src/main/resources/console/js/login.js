let username_input = document.getElementById("username_input");
let password_input = document.getElementById("password_input");
let error_wrapper = document.getElementById("error_wrapper");
let login_button = document.getElementById("login_button");

username_input.addEventListener("blur", () => {
    checkUsername();
});

password_input.addEventListener("blur", () => {
    checkPassword();
});

password_input.addEventListener("keydown", (event) => {
    if (event.code !== "Enter") {
        return;
    }
    login();
});

login_button.addEventListener("click", () => {
    login();
});

function checkUsername() {
    if (isEmpty(username_input.value)) {
        error("用户名不能为空");
        return false;
    }
    success();
    return true;
}

function checkPassword() {
    if (isEmpty(password_input.value)) {
        error("密码不能为空");
        return false;
    }
    success();
    return true;
}

function login() {
    if (!checkUsername() || !checkPassword()) {
        return;
    }
    get("/session/key?username=" + username_input.value, (json) => {
        if (json.success !== true) {
            error(json.message);
            return;
        }
        let password = password_input.value;
        password = md5(password, null, null);
        let encryptor = new JSEncrypt();
        let pubKey = "-----BEGIN PUBLIC KEY-----" + json.data + "-----END PUBLIC KEY-----";
        encryptor.setPublicKey(pubKey);
        password = encryptor.encrypt(password);
        let request = {"username": username_input.value, "password": password};
        post("/session/login", JSON.stringify(request), (json) => {
            if (json.success !== true) {
                error(json.message);
                return;
            }
            window.location.href = indexPage;
        }, (json) => {
            error(json.message);
        });
    }, (json) => {
        error(json.message);
    });
}

function error(message) {
    error_wrapper.style.display = "block";
    error_wrapper.innerText = message;
}

function success() {
    error_wrapper.style.display = "none";
    error_wrapper.innerText = "";
}
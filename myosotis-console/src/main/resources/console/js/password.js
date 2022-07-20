let old_password_input = document.getElementById("old_password_input");
let password_input = document.getElementById("password_input");
let password_re_input = document.getElementById("password_re_input");

let error_wrapper = document.getElementById("error_wrapper");
let login_button = document.getElementById("login_button");

old_password_input.addEventListener("blur", () => {
    checkOldPassword();
});

password_input.addEventListener("blur", () => {
    checkPassword();
});

password_re_input.addEventListener("blur", () => {
    checkPasswordRe();
});

login_button.addEventListener("click", () => {
    if (!checkOldPassword() || !checkPassword() || !checkPasswordRe()) {
        return;
    }
    let username = JSON.parse(localStorage.getItem(myosotis_user)).username;
    get("/session/key?username=" + username, (json) => {
        let oldPassword = old_password_input.value;
        oldPassword = md5(oldPassword, null, null);

        let password = password_input.value;
        password = md5(password, null, null);

        let encryptor = new JSEncrypt();
        let pubKey = "-----BEGIN PUBLIC KEY-----" + json.data + "-----END PUBLIC KEY-----";
        encryptor.setPublicKey(pubKey);

        oldPassword = encryptor.encrypt(oldPassword);
        password = encryptor.encrypt(password);
        let request = {"username": username, "oldPassword": oldPassword, "password": password};
        post("/session/password", JSON.stringify(request), () => {
            localStorage.clear();
            window.location.href = defaultLoginPage;
        }, (json) => {
            error(json.message);
        });
    }, (json) => {
        error(json.message);
    });
});

function checkOldPassword() {
    if (isEmpty(old_password_input.value)) {
        error("原密码不能为空");
        return false;
    }
    success();
    return true;
}

function checkPassword() {
    if (isEmpty(password_input.value)) {
        error("新密码不能为空");
        return false;
    }
    if (password_input.value === old_password_input.value) {
        error("新密码与原密码一致");
        return false;
    }
    success();
    return true;
}

function checkPasswordRe() {
    if (isEmpty(password_re_input.value)) {
        error("二次确认密码不能为空");
        return false;
    }
    if (password_input.value !== password_re_input.value) {
        error("两次输入密码不一致");
        return false;
    }
    success();
    return true;
}

function error(message) {
    error_wrapper.style.display = "block";
    error_wrapper.innerText = message;
}

function success() {
    error_wrapper.style.display = "none";
    error_wrapper.innerText = "";
}
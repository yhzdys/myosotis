const indexPage = window.location.protocol + "//" + window.location.host + "/console/index.html";
let defaultLoginPage = window.location.protocol + "//" + window.location.host + "/console/login.html";
const passwordPage = window.location.protocol + "//" + window.location.host + "/console/password.html";

const pages =
    [
        {"code": "namespace_manage", "page": "n.html"},
        {"code": "config_manage", "page": "c.html"},
        {"code": "server_manage", "page": "s.html"},
        {"code": "user_manage", "page": "u.html"},
    ];

const page_map =
    {
        "namespace_manage": "n.html",
        "config_manage": "c.html",
        "server_manage": "s.html",
        "user_manage": "u.html",
    };

const edit_map =
    {
        "addNamespace": "namespace_manage",
        "editNamespace": "namespace_manage",
        "addConfig": "config_manage",
        "editConfig": "config_manage",
        "addUser": "user_manage",
        "editUser": "user_manage",
    };

const myosotis_user = "myosotis-user";

function isEmpty(value) {
    return !value || value === "" ||
        String(value).toLowerCase() === "blank" ||
        String(value).toLowerCase() === "undefined" ||
        String(value).toLowerCase() === "null";
}

function isNotEmpty(value) {
    return !isEmpty(value);
}

function reduceString(string, limit) {
    if (isEmpty(string)) {
        return "";
    }
    if (string.length <= limit) {
        return string;
    }
    return string.substring(0, limit) + "...";
}

function errorIndex(message) {
    let error_wrapper = window.parent.document.getElementById("error_wrapper");

    error_wrapper.innerText = message;
    error_wrapper.style.display = "block";
    setTimeout(() => {
        hiddenErrorIndex();
    }, 2000);
}

function hiddenErrorIndex() {
    let error_wrapper = window.parent.document.getElementById("error_wrapper");
    error_wrapper.innerText = "";
    error_wrapper.style.display = "none";
}

function getInnerUrlParam(param) {
    let query = window.location.search.substring(1);
    let vars = query.split("&");
    for (let i = 0; i < vars.length; i++) {
        let pair = vars[i].split("=");
        if (pair[0] === param) {
            return pair[1];
        }
    }
    return null;
}

function getUrlParam(param, defaultValue) {
    let query = window.parent.location.search.substring(1);
    let vars = query.split("&");
    let value;
    for (let i = 0; i < vars.length; i++) {
        let pair = vars[i].split("=");
        if (pair[0] === param) {
            value = pair[1];
            break
        }
    }
    if (isEmpty(value) && defaultValue) {
        return defaultValue;
    }
    return value;
}

function setUrlParam(key, value) {
    let url = window.parent.location.href;
    let re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");
    let separator = url.indexOf("?") > 0 ? "&" : "?";

    let newUrl;
    if (url.match(re)) {
        newUrl = (url.replace(re, "$1" + key + "=" + value + "$2")).toString();
    } else {
        newUrl = (url + separator + key + "=" + value).toString();
    }
    window.parent.history.pushState("", "", newUrl);
}

function clearUrlParam() {
    let url = window.parent.location.href;
    let index = url.indexOf("?");
    if (index < 0) {
        return;
    }
    let newUrl = url.substring(0, index);
    window.parent.history.pushState("", "", newUrl);
}

function logout(loginPage) {
    get("/session/logout", (json) => {
        if (json.success === false) {
            errorIndex(json.message);
            return;
        }
        localStorage.clear();
        if (loginPage) {
            window.parent.location.href = loginPage;
        } else {
            window.parent.location.href = defaultLoginPage;
        }
    })
}

function get(url, callback, errorCallback) {
    request("get", url, null, callback, errorCallback);
}

function post(url, body, callback, errorCallback) {
    request("post", url, body, callback, errorCallback);
}

function request(method, url, body, callback, errorCallback) {
    let xhr = new XMLHttpRequest();
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== 4) {
            return;
        }
        if (xhr.status === 401) {
            let loginPage;
            if (xhr.responseText) {
                let json = JSON.parse(xhr.responseText);
                loginPage = json.data;
            }
            logout(loginPage);
            return;
        }
        if (xhr.status !== 200) {
            errorIndex("request failed.");
            return;
        }
        let json = JSON.parse(xhr.responseText);
        if (json.success !== true) {
            if (errorCallback) {
                errorCallback(json);
                return;
            }
            errorIndex(json.message);
            return;
        }
        if (callback) {
            callback(json);
        }
    }
    xhr.open(method, url, true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.send(body);
    setTimeout(function () {
        xhr.abort();
    }, 10000);
}

function confirmCheck(data, message) {
    let confirm_wrapper = window.parent.document.getElementById("confirm_wrapper");
    confirm_wrapper.children[0].innerText = data;
    confirm_wrapper.children[1].innerText = message;

    let confirm_container = window.parent.document.getElementById("confirm_container");
    confirm_container.style.display = "block";
}

function namespaceMenu() {
    changeFrame(page_map["namespace_manage"]);
}

function configMenu(namespace) {
    if (isEmpty(namespace)) {
        changeFrame(page_map["config_manage"]);
    } else {
        changeFrame(page_map["config_manage"] + "?namespace=" + namespace);
    }
}

function serverMenu() {
    changeFrame(page_map["server_manage"]);
}

function userMenu() {
    changeFrame(page_map["user_manage"]);
}

function changeFrame(src) {
    let content_frame = window.parent.document.getElementById("content_frame");
    content_frame.setAttribute("src", src);
    clearUrlParam();
}

function hiddenPageButton() {
    let previous_page = document.getElementById("previous_page");
    let next_page = document.getElementById("next_page");

    previous_page.style.display = "none";
    next_page.style.display = "none";
}

function displayPageButton(data) {
    let previous_page = document.getElementById("previous_page");
    let next_page = document.getElementById("next_page");

    if (data.page === 1) {
        previous_page.style.display = "none";
    } else {
        previous_page.style.display = "block";
    }
    if (data.end === true) {
        next_page.style.display = "none";
    } else {
        next_page.style.display = "block";
    }
}
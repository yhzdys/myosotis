let namespace_wrapper = document.getElementById("namespace_wrapper");
let namespace_option = document.getElementById("namespace_option");

let name_wrapper = document.getElementById("name_wrapper");
let name_input = document.getElementById("name_input");

let value_wrapper = document.getElementById("value_wrapper");
let value_input = document.getElementById("value_input");

let description_wrapper = document.getElementById("description_wrapper");
let description_input = document.getElementById("description_input");

let owners_wrapper = document.getElementById("owners_wrapper");
let owners_input = document.getElementById("owners_input");

let username_wrapper = document.getElementById("username_wrapper");
let username_input = document.getElementById("username_input");

let roles_wrapper = document.getElementById("roles_wrapper");
let role_wrapper = document.getElementById("role_wrapper");

let namespaces_wrapper = document.getElementById("namespaces_wrapper");
let namespaces_input = document.getElementById("namespaces_input");

window.addEventListener("load", () => {
    let type = getInnerUrlParam("type");
    switch (type) {
        case "addNamespace":
            init4AddNamespace();
            break;
        case "editNamespace":
            fillNamespaceData(getInnerUrlParam("id"));
            break;
        case "addConfig":
            init4AddConfig();
            break;
        case "editConfig":
            fillConfigData(getInnerUrlParam("id"));
            break;
        case "addUser":
            init4AddUser();
            break;
        case "editUser":
            fillUserData(getInnerUrlParam("id"));
            break;
        default:
            errorIndex("无效参数");
            return
    }
});

let namespace_input = document.getElementById("namespace_input");
namespace_input.addEventListener("keydown", (event) => {
    if (event.code !== "Enter") {
        return;
    }
    let options = document.querySelectorAll("option");
    if (options) {
        options.forEach(o => o.remove());
    }
    setTimeout(() => {
    }, 1000);
    let keyword = namespace_input.value;
    keyword = isEmpty(keyword) ? "" : keyword;
    get("/namespace/page?keyword=" + keyword, (json) => {
        let data = json.data;
        let list = data.list;
        for (let i = 0, l = list.length; i < l; i++) {
            let element = list[i];
            let option = document.createElement("option");
            option.innerText = element.namespace;
            namespace_option.appendChild(option);
        }
    });
});

document.getElementById("ok_button").addEventListener("click", () => {
    let type = getInnerUrlParam("type");
    if ("addNamespace" === type) {
        addNamespace();
        return;
    }
    if ("editNamespace" === type) {
        updateNamespace();
        return;
    }

    if ("addConfig" === type) {
        addConfig();
        return;
    }
    if ("editConfig" === type) {
        updateConfig();
        return;
    }

    if ("addUser" === type) {
        addUser();
        return;
    }
    if ("editUser" === type) {
        updateUser();
    }
});

document.getElementById("cancel_button").addEventListener("click", () => {
    let type = getInnerUrlParam("type");
    if ("editNamespace" === type || "addNamespace" === type) {
        namespaceMenu();
        return;
    }
    if ("editConfig" === type || "addConfig" === type) {
        configMenu();
        return;
    }
    if ("editUser" === type || "addUser" === type) {
        userMenu();
        return;
    }
    errorIndex("参数错误");
});

function fillNamespaceData(id) {
    get("/namespace/get?id=" + id, (json) => {
        let data = json.data;
        name_input.value = data.namespace;
        description_input.value = data.description;
        owners_input.value = data.owners;
        description_wrapper.style.display = "block";
        owners_wrapper.style.display = "block";
    });
}

function fillConfigData(id) {
    get("/config/get?id=" + id, (json) => {
        let data = json.data;
        namespace_input.value = data.namespace;
        name_input.value = data.configKey;
        description_input.value = data.description;
        value_input.value = data.configValue;

        description_wrapper.style.display = "block";
        value_wrapper.style.display = "block";
    });
}

function fillUserData(id) {
    initUserRole();
    get("/user/get?id=" + id, (json) => {
        let data = json.data;
        namespaces_input.value = data.namespaces;
        let roleRadio = document.getElementById(data.userRole.code);
        if (roleRadio) {
            roleRadio.checked = "true";
        }

        roles_wrapper.style.display = "block";
        namespaces_wrapper.style.display = "block";
    });
}

function initUserRole() {
    role_wrapper.innerHTML = "";
    get("/user/role", (json) => {
        let roles = json.data;
        let innerHTML = "";
        for (let i = 0, l = roles.length; i < l; i++) {
            let role = roles[i];
            let roleRadio = "<input class='role_input' id='" + role.code + "' type='radio' name='role' value='" + role.code + "'>" + role.name;
            innerHTML = innerHTML + roleRadio;
        }
        role_wrapper.innerHTML = innerHTML;
    });
}

function init4AddNamespace() {
    name_wrapper.style.display = "block";
    description_wrapper.style.display = "block";
    owners_wrapper.style.display = "block";
}

function init4AddConfig() {
    namespace_wrapper.style.display = "block";
    name_wrapper.style.display = "block";
    description_wrapper.style.display = "block";
    value_wrapper.style.display = "block";
    let namespace = getInnerUrlParam("namespace");
    if (isNotEmpty(namespace)) {
        namespace_input.value = namespace;
        return;
    }
    get("/namespace/page?page=" + 1, (json) => {
        json.data;
        let data = json.data;
        let list = data.list;
        for (let i = 0, l = list.length; i < l; i++) {
            let element = list[i];
            let option = document.createElement("option");
            option.innerText = element.namespace;
            namespace_option.appendChild(option);
        }
    });
}

function init4AddUser() {
    initUserRole();
    username_wrapper.style.display = "block";
    roles_wrapper.style.display = "block";
    namespaces_wrapper.style.display = "block";
}

function addNamespace() {
    let name = name_input.value;
    if (isEmpty(name)) {
        errorIndex("命名空间名称不能为空");
        return;
    }
    let body = {
        "namespace": name, "name": name, "description": description_input.value, "owners": owners_input.value
    };
    post("/namespace/add", JSON.stringify(body), () => {
        namespaceMenu();
    });
}

function updateNamespace() {
    let id = getInnerUrlParam("id");
    let body = {
        "id": id, "description": description_input.value, "owners": owners_input.value
    };
    post("/namespace/update", JSON.stringify(body), () => {
        namespaceMenu();
    });
}

function addConfig() {
    let namespace = namespace_input.value;
    if (isEmpty(namespace)) {
        errorIndex("命名空间不能为空");
        return;
    }
    let name = name_input.value;
    if (isEmpty(name)) {
        errorIndex("配置名称不能为空");
        return;
    }
    let value = value_input.value;
    if (isEmpty(value)) {
        errorIndex("配置值不能为空");
        return;
    }
    let body = {
        "namespace": namespace, "name": name, "value": value, "description": description_input.value
    };
    post("/config/add", JSON.stringify(body), () => {
        configMenu();
    });
}

function updateConfig() {
    let id = getInnerUrlParam("id");
    let value = value_input.value;
    if (isEmpty(value)) {
        errorIndex("配置值不能为空");
        return;
    }
    let body = {
        "id": id, "value": value, "description": description_input.value
    };
    post("/config/update", JSON.stringify(body), () => {
        configMenu();
    });
}

function addUser() {
    let username = username_input.value;
    if (isEmpty(username)) {
        errorIndex("用户名不能为空");
    }
    let userRole = getUserRole();
    if (isEmpty(userRole)) {
        errorIndex("请选择用户角色");
        return;
    }
    let body = {
        "username": username, "userRole": userRole, "namespaces": namespaces_input.value
    };
    post("/user/add", JSON.stringify(body), () => {
        userMenu();
    });
}

function updateUser() {
    let id = getInnerUrlParam("id");

    let userRole = getUserRole();
    if (isEmpty(userRole)) {
        errorIndex("请选择用户角色");
        return;
    }
    let body = {
        "id": id, "userRole": userRole, "namespaces": namespaces_input.value
    };
    post("/user/update", JSON.stringify(body), () => {
        let user = JSON.parse(localStorage.getItem(myosotis_user));
        if (user.userId.toString() === id && user.userRole.code !== userRole) {
            window.parent.location.reload();
        } else {
            userMenu();
        }
    });
}

function getUserRole() {
    let roleRadios = document.getElementsByClassName("role_input");
    let userRole;
    for (let i = 0, l = roleRadios.length; i < l; i++) {
        let radio = roleRadios[i];
        if (isNotEmpty(radio.checked) && (radio.checked === true || radio.checked === "checked")) {
            userRole = radio.value;
        }
    }
    return userRole;
}
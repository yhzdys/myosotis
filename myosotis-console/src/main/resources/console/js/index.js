window.addEventListener("load", () => {
    initIndex();
});

let user_wrapper = document.getElementById("user_wrapper");
let user_menu_wrapper = document.getElementById("user_menu_wrapper");

function initIndex() {
    get("/user/index", (json) => {
        let userIndex = json.data;
        localStorage.setItem(myosotis_user, JSON.stringify(userIndex));

        document.getElementById("username").innerText = userIndex.username;

        let elementList = window.parent.document.querySelectorAll(".menu_item");
        elementList.forEach(e => e.style.display = "none");

        let menus = userIndex.menus;
        for (let i = 0, l = menus.length; i < l; i++) {
            let menu = menus[i];
            let menu_wrapper = window.parent.document.getElementById(menu.code + "_wrapper");
            if (menu_wrapper) {
                menu_wrapper.style.display = "block";
            }
        }
        document.getElementById("username").addEventListener("click", () => {
            let display = user_menu_wrapper.style.display;
            if (display === "block") {
                user_menu_wrapper.style.display = "none";
            } else {
                user_menu_wrapper.style.display = "block";
            }
        });
    });
}

user_wrapper.addEventListener("mouseleave", () => {
    user_menu_wrapper.style.display = "none";
});

let logout_button = document.getElementById("logout_button");
logout_button.addEventListener("click", () => {
    logout();
});

let password_button = document.getElementById("password_button");
password_button.addEventListener("click", () => {
    window.location.href = passwordPage;
});


document.getElementById("namespace_manage").addEventListener("click", () => {
    namespaceMenu();
});
document.getElementById("config_manage").addEventListener("click", () => {
    configMenu();
});
document.getElementById("server_manage").addEventListener("click", () => {
    serverMenu();
});
document.getElementById("user_manage").addEventListener("click", () => {
    userMenu();
});

let content_frame = document.getElementById("content_frame");
content_frame.addEventListener("load", () => {
    let frameTitle = "";
    let content_title = document.getElementById("content_title");

    let url = new URL(content_frame.contentWindow.location.href);
    if (isEmpty(url.pathname)) {
        return;
    }
    if (url.pathname.endsWith("e.html")) {
        frameTitle = getEditFrameTitle(url);
    } else {
        let pageCode;
        for (let pageData of pages) {
            if (url.pathname.endsWith(pageData.page)) {
                pageCode = pageData.code;
                break;
            }
        }
        if (isEmpty(pageCode)) {
            errorIndex("配置页面未知");
            return;
        }
        frameTitle = getPageName(pageCode);
        if (pageCode === "config_manage") {
            let namespace = url.searchParams.get("namespace");
            if (isNotEmpty(namespace)) {
                frameTitle = frameTitle + "（" + namespace + "）";
            }
        }
    }
    content_title.innerText = frameTitle;
});

function getEditFrameTitle(url) {
    let type = url.searchParams.get("type");
    let pageName = getPageName(edit_map[type]);
    if (type.startsWith("add")) {
        return pageName + " > 新增";
    }
    if (type.startsWith("edit")) {
        return pageName + " > 编辑（" + url.searchParams.get("name") + "）";
    }
    return "unknown";
}

function getPageName(code) {
    let menus = JSON.parse(localStorage.getItem(myosotis_user)).menus;
    for (let i = 0, l = menus.length; i < l; i++) {
        let menu = menus[i];
        if (menu.code === code) {
            return menu.name;
        }
    }
    return "";
}
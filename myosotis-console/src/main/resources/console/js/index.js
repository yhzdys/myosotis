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
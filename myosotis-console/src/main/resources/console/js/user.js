window.addEventListener("load", () => {
    let page = getUrlParam("page");
    fillUserTable(page);
});

document.getElementById("search_input").addEventListener("keydown", (event) => {
    if (event.code !== "Enter") {
        return;
    }
    fillUserTable(1);
});

document.getElementById("previous_page").addEventListener("click", () => {
    let page = getUrlParam("page", 2);
    page--;
    setUrlParam("page", page);
    fillUserTable(page);
});

document.getElementById("next_page").addEventListener("click", () => {
    let page = getUrlParam("page", 1);
    page++;
    setUrlParam("page", page);
    fillUserTable(page);
});

function fillUserTable(page) {
    if (page == null || page < 1) {
        page = 1;
    }
    let elementList = document.querySelectorAll(".table_data");
    elementList.forEach(e => e.remove());
    let keyword = document.getElementById("search_input").value;
    if (isEmpty(keyword)) {
        keyword = "";
    }
    hiddenPageButton();
    get("/user/page?page=" + page + "&keyword=" + keyword, (json) => {
        if (json.success === false) {
            errorIndex(json.message);
            return;
        }
        let data = json.data;
        let list = data.list;
        let tableBody = document.getElementById("table_body");
        for (let i = 0, l = list.length; i < l; i++) {
            let element = list[i];
            let row = document.createElement("tr");
            row.className = "table_data";

            let id = document.createElement("td");
            id.innerText = element.id;
            id.style.display = "none";
            row.appendChild(id);

            let username = document.createElement("td");
            username.innerText = reduceString(element.username, 20);
            row.appendChild(username);

            let userRole = document.createElement("td");
            userRole.innerText = element.userRole.name;
            row.appendChild(userRole);

            let namespaces = document.createElement("td");
            namespaces.innerText = reduceString(element.namespaces, 60);
            row.appendChild(namespaces);

            let createTime = document.createElement("td");
            createTime.innerText = element.createTime;
            row.appendChild(createTime);

            let option = document.createElement("td");
            if (element.userRole.code === "superuser") {
                option.innerHTML = "<td>\n" +
                    "<a class='data_button' onclick='resetUser()'>重置</a>\n" +
                    "</td>";
            } else {
                option.innerHTML = "<td>\n" +
                    "<a class='data_button' onclick='editUser()'>编辑</a>\n" +
                    "<a class='data_button' onclick='resetUser()'>重置</a>\n" +
                    "<a class='data_button delete_button' onclick='deleteUser()'>删除</a>\n" +
                    "</td>";
            }
            row.appendChild(option);
            tableBody.appendChild(row);
        }
        displayPageButton(data);
    })
}

document.getElementById("add_button").addEventListener("click", () => {
    changeFrame("用户管理 > 新增", "e.html?type=addUser");
});

function editUser() {
    let row = arguments.callee.caller.arguments[0].target.parentElement.parentElement;
    let id = row.children[0].innerText;
    let namespace = row.children[1].innerText;
    changeFrame("用户管理 > 编辑（" + namespace + "）", "e.html?type=editUser&id=" + id);
    clearUrlParam();
}

function resetUser() {
    let row = arguments.callee.caller.arguments[0].target.parentElement.parentElement;
    let id = row.children[0].innerText;
    let confirmData = '{"type": "resetUser", "id": ' + id + '}';
    let message = "确认重置用户：" + row.children[1].innerText + "的密码？\n（默认密码：123456）";
    confirmCheck(confirmData, message);
}

function deleteUser() {
    let row = arguments.callee.caller.arguments[0].target.parentElement.parentElement;
    let id = row.children[0].innerText;
    let confirmData = '{"type": "deleteUser", "id": ' + id + '}';
    let message = "确认删除：" + row.children[1].innerText + "？";
    confirmCheck(confirmData, message);
}
window.addEventListener("load", () => {
    let userRole = JSON.parse(localStorage.getItem(myosotis_user)).userRole.code;
    if (isNotEmpty(userRole) && "superuser" === userRole) {
        document.getElementById("add_wrapper").style.display = "block";
    }
    let page = getUrlParam("page");
    fillNamespaceTable(page);
});

document.getElementById("search_input").addEventListener("keydown", (event) => {
    if (event.code !== "Enter") {
        return;
    }
    fillNamespaceTable(1);
});

document.getElementById("previous_page").addEventListener("click", () => {
    let page = getUrlParam("page", 2);
    page--;
    setUrlParam("page", page);
    fillNamespaceTable(page);
});

document.getElementById("next_page").addEventListener("click", () => {
    let page = getUrlParam("page", 1);
    page++;
    setUrlParam("page", page);
    fillNamespaceTable(page);
});

function fillNamespaceTable(page) {
    if (page == null || page < 1) {
        page = 1;
    }
    let elementList = document.querySelectorAll(".table_data");
    elementList.forEach(e => e.remove());
    let keyword = document.getElementById("search_input").value;
    if (isEmpty(keyword)) {
        keyword = "";
    }
    let userRole = JSON.parse(localStorage.getItem(myosotis_user)).userRole.code;
    let isSuperUser = isNotEmpty(userRole) && "superuser" === userRole;

    hiddenPageButton();
    get("/namespace/page?page=" + page + "&keyword=" + keyword, (json) => {
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

            let namespace = document.createElement("td");
            namespace.innerText = reduceString(element.namespace, 20);
            row.appendChild(namespace);

            let description = document.createElement("td");
            description.innerText = reduceString(element.description, 20);
            row.appendChild(description);

            let owners = document.createElement("td");
            owners.innerText = reduceString(element.owners, 60);
            row.appendChild(owners);

            let configCount = document.createElement("td");
            configCount.innerText = element.configCount;
            row.appendChild(configCount);

            let option = document.createElement("td");
            if (isSuperUser) {
                if (element.configCount < 1) {
                    option.innerHTML = "<td>\n" +
                        "<a class='data_button' onclick='listConfigs()'>查看</a>\n" +
                        "<a class='data_button' onclick='editNamespace()'>编辑</a>\n" +
                        "<a class='data_button delete_button' onclick='deleteNamespace()'>删除</a>\n" +
                        "</td>";
                } else {
                    option.innerHTML = "<td>\n" +
                        "<a class='data_button' onclick='listConfigs()'>查看</a>\n" +
                        "<a class='data_button' onclick='editNamespace()'>编辑</a>\n" +
                        "</td>";
                }
            } else {
                option.innerHTML = "<td>\n" +
                    "<a class='data_button' onclick='listConfigs()'>查看</a>\n" +
                    "</td>";
            }
            row.appendChild(option);
            tableBody.appendChild(row);
        }
        displayPageButton(data);
    })
}

document.getElementById("add_button").addEventListener("click", () => {
    changeFrame("e.html?type=addNamespace");
});

function listConfigs() {
    let row = arguments.callee.caller.arguments[0].target.parentElement.parentElement;
    let namespace = row.children[1].innerText;
    configMenu(namespace);
    clearUrlParam();
}

function editNamespace() {
    let row = arguments.callee.caller.arguments[0].target.parentElement.parentElement;
    let id = row.children[0].innerText;
    let namespace = row.children[1].innerText;
    changeFrame("e.html?type=editNamespace&id=" + id + "&name=" + namespace);
    clearUrlParam();
}

function deleteNamespace() {
    let row = arguments.callee.caller.arguments[0].target.parentElement.parentElement;
    let id = row.children[0].innerText;
    let confirmData = '{"type": "deleteNamespace", "id": ' + id + '}';
    let message = "确认删除：" + row.children[1].innerText + "？";
    confirmCheck(confirmData, message);
}
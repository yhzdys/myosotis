let search_input = document.getElementById("search_input");

window.addEventListener("load", () => {
    let page = getUrlParam("page");
    fillConfigTable(page);
});

search_input.addEventListener("keydown", (event) => {
    if (event.code !== "Enter") {
        return;
    }
    fillConfigTable(1);
});

document.getElementById("previous_page").addEventListener("click", () => {
    let page = getUrlParam("page", 2);
    page--;
    setUrlParam("page", page);
    fillConfigTable(page);
});

document.getElementById("next_page").addEventListener("click", () => {
    let page = getUrlParam("page", 1);
    page++;
    setUrlParam("page", page);
    fillConfigTable(page);
});

function fillConfigTable(page) {
    if (page == null || page < 1) {
        page = 1;
    }
    let elementList = document.querySelectorAll(".table_data");
    elementList.forEach(e => e.remove());

    let namespace = getInnerUrlParam("namespace");
    if (isEmpty(namespace)) {
        namespace = "";
    }
    let keyword = search_input.value;
    if (isEmpty(keyword)) {
        keyword = "";
    }
    hiddenPageButton();
    let url = "/config/page?namespace=" + namespace + "&page=" + page + "&keyword=" + keyword;
    get(url, (json) => {
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

            let configKey = document.createElement("td");
            configKey.innerText = reduceString(element.configKey, 20);
            row.appendChild(configKey);

            let description = document.createElement("td");
            description.innerText = reduceString(element.description, 20);
            row.appendChild(description);

            let configValue = document.createElement("td");
            configValue.innerText = reduceString(element.configValue, 40);
            row.appendChild(configValue);

            let option = document.createElement("td");
            option.innerHTML = "<td>\n" +
                "<a class='data_button' onclick='editConfig()'>编辑</a>\n" +
                // "<a class='data_button'>历史</a>\n" +
                "<a class='data_button delete_button' onclick='deleteConfirm()'>删除</a>\n" +
                "</td>";
            row.appendChild(option);

            tableBody.appendChild(row);
        }
        displayPageButton(data);
    })
}

document.getElementById("add_button").addEventListener("click", () => {
    let namespace = getInnerUrlParam("namespace");
    if (isEmpty(namespace)) {
        changeFrame("配置管理 > 新增", "e.html?type=addConfig");
    } else {
        changeFrame("配置管理 > 新增", "e.html?type=addConfig&namespace=" + namespace);
    }
});

function editConfig() {
    let row = arguments.callee.caller.arguments[0].target.parentElement.parentElement;
    let id = row.children[0].innerText;
    let configKey = row.children[2].innerText;
    changeFrame("配置管理 > 编辑（" + configKey + "）", "e.html?type=editConfig&id=" + id);
    clearUrlParam();
}

function deleteConfirm() {
    let row = arguments.callee.caller.arguments[0].target.parentElement.parentElement;
    let id = row.children[0].innerText;
    let confirmData = '{"type": "deleteConfig", "id": ' + id + '}';
    let message = "确认删除：" + row.children[2].innerText + "？";
    confirmCheck(confirmData, message);
}
window.addEventListener("load", () => {
    let page = getUrlParam("page");
    fillServerTable(page);
});

document.getElementById("previous_page").addEventListener("click", () => {
    let page = getUrlParam("page", 2);
    page--;
    setUrlParam("page", page);
    fillServerTable(page);
});

document.getElementById("next_page").addEventListener("click", () => {
    let page = getUrlParam("page", 1);
    page++;
    setUrlParam("page", page);
    fillServerTable(page);
});

function fillServerTable(page) {
    if (page == null || page < 1) {
        page = 1;
    }
    let elementList = document.querySelectorAll(".table_data");
    elementList.forEach(e => e.remove());
    hiddenPageButton();
    get("/server/page?page=" + page, (json) => {
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

            let address = document.createElement("td");
            address.innerText = reduceString(element.address, 60);
            row.appendChild(address);

            let lastCheckTime = document.createElement("td");
            lastCheckTime.innerText = element.lastCheckTime;
            row.appendChild(lastCheckTime);

            let health = document.createElement("td");
            health.innerText = element.health;
            row.appendChild(health);

            let failCount = document.createElement("td");
            failCount.innerText = element.failCount;
            row.appendChild(failCount);

            tableBody.appendChild(row);
        }
        displayPageButton(data);
    })
}

document.getElementById("reload_button").addEventListener("click", () => {
    get("/server/reload", () => {
        serverMenu();
    });
});

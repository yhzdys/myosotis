let confirm_container = window.parent.document.getElementById("confirm_container");
let confirm_wrapper = window.parent.document.getElementById("confirm_wrapper");

window.parent.document.getElementById("ok_button").addEventListener("click", () => {
    let confirmData = confirm_wrapper.children[0].innerText;
    if (isEmpty(confirmData)) {
        errorIndex("参数为空");
        hiddenConfirm();
        return;
    }
    let json = JSON.parse(confirmData);
    let type = json.type;
    if (isEmpty(type) || isEmpty(json.id)) {
        errorIndex("参数无效");
        hiddenConfirm();
        return;
    }
    if (type === "deleteNamespace") {
        get("/namespace/delete/" + json.id);
    } else if (type === "deleteConfig") {
        get("/config/delete/" + json.id);
    } else if (type === "resetUser") {
        get("/user/reset/" + json.id);
    } else if (type === "deleteUser") {
        get("/user/delete/" + json.id);
    }
    hiddenConfirm();
    window.parent.document.getElementById('content_frame').contentWindow.location.reload();
});

window.parent.document.getElementById("cancel_button").addEventListener("click", () => {
    hiddenConfirm();
});

function hiddenConfirm() {
    confirm_wrapper.children[0].innerText = "";
    confirm_wrapper.children[1].innerText = "";
    confirm_container.style.display = "none";
}
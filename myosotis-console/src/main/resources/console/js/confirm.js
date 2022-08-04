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
    if (isEmpty(json.type) || isEmpty(json.id)) {
        errorIndex("参数无效");
        hiddenConfirm();
        return;
    }
    switch (json.type) {
        case "deleteNamespace":
            get("/namespace/delete/" + json.id);
            break;
        case "deleteConfig":
            get("/config/delete/" + json.id);
            break;
        case "resetUser":
            get("/user/reset/" + json.id);
            break;
        case "deleteUser":
            get("/user/delete/" + json.id);
            break;
        default:
            break;
    }
    setTimeout(() => {
        window.parent.document.getElementById('content_frame').contentWindow.location.reload();
        hiddenConfirm();
    }, 1000);
});

window.parent.document.getElementById("cancel_button").addEventListener("click", () => {
    hiddenConfirm();
});

function hiddenConfirm() {
    confirm_wrapper.children[0].innerText = "";
    confirm_wrapper.children[1].innerText = "";
    confirm_container.style.display = "none";
}
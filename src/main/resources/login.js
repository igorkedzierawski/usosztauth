class UIClass {
    constructor() {
        this.redirectAnchor = document.getElementById("redirect_anchor");
        this.statusbox = document.getElementById("statusbox");
        this.deauthorizeDeviceConsumer = (x) => {};
        Object.seal(this);
    }
    error(message) {
        this.statusbox.innerText = message;
        this.statusbox.style.color = "#ff4747";
    }
    success(message) {
        this.statusbox.innerText = message;
        this.statusbox.style.color = "#4CAF50";
    }
    setRedirectUrl(url) {
        this.redirectAnchor.href = url;
    }
}
const UI = new UIClass();

function errorHandler(response) {
    try {
        UI.error(JSON.parse(response)["error"]);
    } catch(error) {
        UI.error("Nieoczekiwana odpowiedź od serwera");
    }
}

sendRequest('GET', '/auth_init').then(response => {
    try {
        const authInitResponse = JSON.parse(response);
        setCookie("sessionUuid", authInitResponse["sessionUuid"]);
        UI.setRedirectUrl(authInitResponse["authUrl"]);
    } catch(error) {
        errorHandler(response);
    }
}).catch(errorHandler);

(() => {
    const params = new URLSearchParams(window.location.search);
    if(params.get("login_failure_reason")) {
        UI.error(params.get("login_failure_reason"));
    }
})();

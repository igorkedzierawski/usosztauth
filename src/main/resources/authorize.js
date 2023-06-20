class UIClass {
    constructor() {
        this.imie = document.getElementById("imie");
        this.nazwisko = document.getElementById("nazwisko");
        this.netid = document.getElementById("netid");
        this.logoutButton = document.getElementById("logout_button");
        this.ztDevIdInput = document.getElementById("zt_dev_id_input");
        this.authButton = document.getElementById("auth_button");
        this.ztDevList = document.getElementById("zt_dev_list");
        this.refreshDevicesButton = document.getElementById("refresh_devices_button");
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
    setDeauthorizeDeviceConsumer(consumer) {
        this.deauthorizeDeviceConsumer = consumer;
    }
    updateDevicesList(devices) {
        while(UI.ztDevList.children.length > 0)
            UI.ztDevList.removeChild(UI.ztDevList.children[0]);
        for(let i = 0; i < devices.length; i++) {
            const row = UI.ztDevList.appendChild(document.createElement("tr"));
            row.appendChild(document.createElement("td"))
                .innerText = devices[i]["ztDevId"];
            row.appendChild(document.createElement("td"))
                .innerText = devices[i]["name"];
            row.appendChild(document.createElement("button"))
                .innerText = "Deautoryzuj";
            row.children[2].onclick = () => {
                const ztDevId = devices[i]["ztDevId"];
                if(this.deauthorizeDeviceConsumer)
                    this.deauthorizeDeviceConsumer(ztDevId);
            }
        }
    }
}
const UI = new UIClass();

class RequestClass {
    constructor() {
        this.errorHandler = (x) => {};
        Object.seal(this);
    }
    setErrorHandlerConsumer(consumer) {
        this.errorHandler = consumer;
    }
    async listAuthorizedDevices() {
        const response = await sendRequest(
            'GET', '/list_authorized_devices'
        ).catch(this.errorHandler);
        return JSON.parse(response)["authorizedDevices"];
    }
    async getUserInfo() {
        const response = await sendRequest(
            'GET', '/get_user_info'
        ).catch(this.errorHandler);
        return JSON.parse(response);
    }
    async getNetworkId() {
        const response = await sendRequest(
            'GET', '/get_network_id'
        ).catch(this.errorHandler);
        return JSON.parse(response)["ztNetworkId"];
    }
    async logout() {
        const response = await sendRequest(
            'DELETE', '/logout'
        ).catch(this.errorHandler);
        return JSON.parse(response);
    }
    async authorizeDevice(ztDevId) {
        const response = await sendRequest(
            'POST', '/device_authorization',
            JSON.stringify({"ztDevId": ztDevId})
        ).catch(this.errorHandler);
        return JSON.parse(response);
    }
    async deauthorizeDevice(ztDevId) {
        const response = await sendRequest(
            'DELETE', '/device_authorization',
            JSON.stringify({"ztDevId": ztDevId})
        ).catch(this.errorHandler);
        return JSON.parse(response);
    }
}
const REQUEST = new RequestClass();
REQUEST.setErrorHandlerConsumer(response => {
    try {
        UI.error(JSON.parse(response)["error"]);
    } catch(error) {
        UI.error("Nieoczekiwana odpowiedź od serwera");
    }
})

function refreshDevicesList() {
    REQUEST.listAuthorizedDevices().then(devices => {
        UI.updateDevicesList(devices);
    }).catch(x => {})
}

UI.refreshDevicesButton.onclick = () => {
    refreshDevicesList();
}
UI.authButton.onclick = () => {
    const ztDevId = UI.ztDevIdInput.value;
    REQUEST.authorizeDevice(ztDevId).then(x => {
        UI.success("Pomyślnie nadano autoryzację urządzeniu '"+ztDevId+"'");
        refreshDevicesList();
    }).catch(x => {});
}
UI.setDeauthorizeDeviceConsumer((ztDevId) => {
    REQUEST.deauthorizeDevice(ztDevId).then(x => {
        UI.success("Pomyślnie zdjęto autoryzację urządzeniu '"+ztDevId+"'");
        refreshDevicesList();
    }).catch(x => {});
});
UI.logoutButton.onclick = () => {
    REQUEST.logout().then(x => {
        window.location.href = '/login';
    }).catch(x => {});;
}

REQUEST.getUserInfo().then(userInfo => {
    UI.imie.innerText = userInfo["imie"];
    UI.nazwisko.innerText = userInfo["nazwisko"];
}).catch(x => {});
REQUEST.getNetworkId().then(ztNetworkId => {
    UI.netid.innerText = ztNetworkId;
}).catch(x => {});
refreshDevicesList();
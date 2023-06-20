function setCookie(sKey, sValue) {
    if (!sKey || /^(?:expires|max\-age|path|domain|secure)$/i.test(sKey))
        return false;
    document.cookie = encodeURIComponent(sKey) + "=" + encodeURIComponent(sValue);
    return true;
}
async function sendRequest(method, mapping, body) {
    return new Promise(function(resolve, reject) {
        const xhr = new XMLHttpRequest();
        xhr.open(method, mapping, true);
        xhr.onload = function() {
            if(xhr.status != 200) {
                reject(xhr.response);
            } else {
                resolve(xhr.response);
            }
        };
        xhr.send(method === 'GET' ? null : body);
    });
}
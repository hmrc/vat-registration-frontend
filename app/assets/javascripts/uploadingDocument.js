(function() {
    var dataTag = document.querySelector("#data")
    var submitButton = document.querySelector(".govuk-button")
    var request =  new XMLHttpRequest();

    submitButton.style.display = 'none'
    function goToErrorPage() {
        window.location.href = "/error/page/url";
    }

    function refresh() {
        request.open('GET', "/register-for-vat/file-upload/uploading-document-poll?reference=" + dataTag.dataset.reference, true);

        request.onload = function() {
            if (this.status === 200) {
                var data = JSON.parse(this.response);
                switch(data.status) {
                    case "IN_PROGRESS":
                        setTimeout(refresh, parseInt(dataTag.dataset.interval));
                        break;
                    case "ERROR":
                        goToErrorPage();
                        break;
                    case "READY":
                        window.location.href = "/register-for-vat/file-upload/summary";
                        break;
                }
            } else {
                goToErrorPage();
            }
        };
        request.send();

    }
    setTimeout(goToErrorPage, parseInt(dataTag.dataset.timeout));
    refresh();
})();
(function() {
    var dataTag = document.querySelector("#data")
    var submitButton = document.querySelector(".govuk-button")
    var uploadingForm = document.querySelector('form[action^="/register-for-vat/uploading-vat5l-document"]')
    var request =  new XMLHttpRequest();

    submitButton.style.display = 'none'
    function goToErrorPage() {
        window.location.href = "/register-for-vat/file-upload/problem-with-selected-file";
    }

    function refresh() {
        request.open('GET', "/register-for-vat/uploading-vat5l-document-poll?reference=" + dataTag.dataset.reference, true);

        request.onload = function() {
            if (this.status === 200) {
                var data = JSON.parse(this.response);
                switch(data.status) {
                    case "IN_PROGRESS":
                        setTimeout(refresh, parseInt(dataTag.dataset.interval));
                        break;
                    case "FAILED":
                        uploadingForm.submit();
                        break;
                    case "READY":
                        uploadingForm.submit();
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
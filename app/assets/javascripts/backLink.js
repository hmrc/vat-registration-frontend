var backLinks = document.querySelectorAll('a[href="#"]:not([data-module="hmrc-back-link"])')

if (backLinks.length > 0) {
    backLinks.forEach(element => element.addEventListener('click', function(e) {
        e.preventDefault();
        window.history.back();
    }))
}
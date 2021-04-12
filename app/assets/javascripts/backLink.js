var backLinks = document.querySelectorAll('#back-link[href="#"]')

if (backLinks.length > 0) {
    backLinks.forEach(element => element.addEventListener('click', function(e) {
        e.preventDefault();
        window.history.back();
    }))
}
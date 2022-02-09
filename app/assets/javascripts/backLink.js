var backLinks = document.querySelectorAll('a[href="#"][class="govuk-back-link"]')

if (backLinks.length > 0) {
    backLinks.forEach(element => element.addEventListener('click', function(e) {
        e.preventDefault();
        window.history.back();
    }))
}
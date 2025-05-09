var backLinks = document.querySelectorAll('a[href="#"]');

if (backLinks.length > 0) {
    backLinks.forEach(element => element.addEventListener('click', function(e) {
        e.preventDefault();
        if (document.referrer) {
            window.location.href = document.referrer;
        } else {
            window.history.back();
        }
    }));
}

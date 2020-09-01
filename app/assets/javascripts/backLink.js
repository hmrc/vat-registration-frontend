document.querySelector('#back-link[href="#"]').addEventListener('click', function(e) {
    e.preventDefault();
    window.history.back();
})
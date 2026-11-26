(function() {
    var printButtonDiv = document.querySelector("div[id=print-button]");
    var content = printButtonDiv.dataset.content;
    var classes = printButtonDiv.dataset.classes || "govuk-button";
    var printButton = document.createElement("button");

    printButton.setAttribute("type", "button");
    printButton.setAttribute("class", classes);
    printButton.textContent = content;

    printButton.addEventListener("click", function() {
        window.print();
    });

    printButton.addEventListener("keydown", function(event) {
        if (event.code === "Enter" || event.code === "Space") {
            window.print();
        }
    });

    printButtonDiv.appendChild(printButton);
})();
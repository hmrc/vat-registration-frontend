$(document).ready($(function () {

}));

// UI module
(function (UI, $, undefined) {
    UI.show = function (selector) {
        $(selector).removeClass("hidden");
    };

    UI.hide = function (selector) {
        $(selector).addClass("hidden");
    };
}(window.UI = window.UI || {}, jQuery));

// StartDatePage module
(function (StartDatePage, $, undefined) {
    var startDateRadioGroup = $("input[name='startDate']:radio");
    var futureDateRadio = $("#startDate-future_date");
    var futureDateHidden = $("#future_date_hidden");

    function updateState() {
        if (futureDateRadio.is(":checked")) {
            UI.show(futureDateHidden);
        } else {
            UI.hide(futureDateHidden);
        }
    }

    updateState();
    startDateRadioGroup.on("change", function () {
        updateState();
    });
}(window.StartDatePage = window.StartDatePage || {}, jQuery));
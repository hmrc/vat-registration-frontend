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
    StartDatePage.init = function() {
        console.log("StartDatePage init()");
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

// TradingName module
(function (TradingNamePage, $, undefined) {
    var tradingNameRadioGroup = $("input[name='tradingName.yesNo']:radio");
    var tradingNameRadio = $("#tradingName\\.yesNo-trading_name_yes");
    var tradingNameHidden = $("#trading_name_hidden");

    function updateState() {
        if (tradingNameRadio.is(":checked")) {
            UI.show(tradingNameHidden);
        } else {
            UI.hide(tradingNameHidden);
        }
    }

    updateState();
    tradingNameRadioGroup.on("change", function () {
        updateState();
    });
}(window.TradingNamePage = window.TradingNamePage || {}, jQuery));
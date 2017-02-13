$(document).ready($(function () {

}));

// UI module (common code)
(function (UI, $, undefined) {
    UI.show = function (selector) {
        $(selector).removeClass("hidden");
    };

    UI.hide = function (selector) {
        $(selector).addClass("hidden");
    };

    UI.hideShowOnRadioButton = function(radioGroupName, buttonToAreaMap) {
        var updateState = function(buttonMap) {
            for (var b in buttonMap) {
                if ($(b).is(":checked")) {
                    UI.show($(buttonMap[b]));
                } else {
                    UI.hide($(buttonMap[b]));
                }
            }
        };
        // on state change handler
        var radioGroup = $("input[name='"+radioGroupName+"']:radio");
        radioGroup.on("change", function () {
            updateState(buttonToAreaMap);
        }).trigger("change");
    }
}(window.UI = window.UI || {}, jQuery));

// StartDatePage module
(function (StartDatePage, $, undefined) {
    StartDatePage.init = function() {
        UI.hideShowOnRadioButton("startDateRadio",
            { "#startDateRadio-specific_date": "#specific_date_panel" });
    }
}(window.StartDatePage = window.StartDatePage || {}, jQuery));

// TradingNamePage module
(function (TradingNamePage, $, undefined) {
    TradingNamePage.init = function() {
        UI.hideShowOnRadioButton("tradingNameRadio",
            { "#tradingNameRadio-trading_name_yes": "#trading_name_panel" });
    }
}(window.TradingNamePage = window.TradingNamePage || {}, jQuery));



// VoluntaryRegistrationPage module
(function (VoluntaryRegistrationPage, $, undefined) {
    VoluntaryRegistrationPage.init = function() {
        UI.hideShowOnRadioButton("voluntaryRegistrationRadio",
            { "#voluntaryRegistrationRadio-register_no": "#no_panel" });
    }
}(window.VoluntaryRegistrationPage = window.VoluntaryRegistrationPage || {}, jQuery));


/*
 example of multiple hide/show areas
 UI.hideShowOnRadioButton("startDate",
 {   "#startDate-specific_date": "#specific_date_panel",
     "#startDate-when_registered": "#other_panel"   });
 */
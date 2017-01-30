$(document).ready($(function () {

}));

// UI module (common code)
(function (UI, $) {
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
(function (StartDatePage) {
    StartDatePage.init = function() {
        UI.hideShowOnRadioButton("startDateRadio",
            { "#startDateRadio-future_date": "#future_date_panel" });
    }
}(window.StartDatePage = window.StartDatePage || {}));

// TradingName module
(function (TradingNamePage) {
    TradingNamePage.init = function() {
        UI.hideShowOnRadioButton("tradingNameRadio",
            { "#tradingNameRadio-trading_name_yes": "#trading_name_panel" });
    }
}(window.TradingNamePage = window.TradingNamePage || {}));

/*
 example of multiple hide/show areas
 UI.hideShowOnRadioButton("startDate",
 {   "#startDate-future_date": "#future_date_panel",
     "#startDate-when_registered": "#other_panel"   });
 */
$(document).ready($(function () {
    noSectorForSICCodeToGA();
    showDeskproForm();
    initRadioOptions();
}));

/*
 example of multiple hide/show areas
 UI.hideShowOnRadioButton("startDate",
 {   "#startDate-specific_date": "#specific_date_panel",
 "#startDate-when_registered": "#other_panel"   });
 */

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
    };
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
            { "#tradingNameRadio-true": "#trading_name_panel" });
    }
}(window.TradingNamePage = window.TradingNamePage || {}, jQuery));

// FormerNamePage module
(function (FormerNamePage, $, undefined) {
    FormerNamePage.init = function() {
        UI.hideShowOnRadioButton("formerNameRadio",
            { "#formerNameRadio-true": "#former_name_panel" });
    }
}(window.FormerNamePage = window.FormerNamePage || {}, jQuery));

// FrsStartDatePage module
(function (FrsStartDatePage, $, undefined) {
    FrsStartDatePage.init = function() {
        UI.hideShowOnRadioButton("frsStartDateRadio",
            { "#frsStartDateRadio-differentdate": "#different_date_panel" });
    }
}(window.FrsStartDatePage = window.FrsStartDatePage || {}, jQuery));

var noSectorForSICCodeToGA = function() {
    if($("#noSectorForSICCode").length) {
        ga('send', 'event', {
            'eventCategory': 'vat_register',
            'eventAction': 'no_sector_for_sic_code',
            'eventLabel': $('#noSectorForSICCode').text()
        });
    }
};

var showDeskproForm =  function () {
    $("#submissionFailedReportAProblem").each(function(){
        $(".report-error__toggle").click();
        $(".report-error__toggle").hide();
    });
}

var initRadioOptions = function () {
    var radioOptions = $('input[type="radio"]');

    radioOptions.each(function () {
        var o = $(this).parent().next('.additional-option-block');
        if ($(this).prop('checked')) {
            o.show();
        } else {
            o.hide();
        }
    });

    radioOptions.on('click', function (e) {
        $('.additional-option-block').hide();
        var o = $(this).parent().next('.additional-option-block');
        if (o.index() != -1) {
            o.show();
        }
    });
}
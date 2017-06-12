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
    };

    UI.preventNonNumericInput = function(inputs) {
        //             \t,\n, 0,  1,  2,  3,  3,  5,  6,  7,  8,  9
        var allowed = [8, 9, 13, 26, 27, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 127, ];
        $.each(inputs, function(idx, inputName){
            $('input[name='+inputName+']').keypress (function(evt) {
                // if current key not found in the array of allowed key codes, ignore keypress
                if ($.inArray(evt.which, allowed) === -1) {
                    return evt.preventDefault();
                }
            });
        });
    };
}(window.UI = window.UI || {}, jQuery));

// StartDatePage module
(function (StartDatePage, $, undefined) {
    StartDatePage.init = function() {
        UI.hideShowOnRadioButton("startDateRadio",
            { "#startDateRadio-specific_date": "#specific_date_panel" });
        var numericInputs = ["startDate\\.day", "startDate\\.month", "startDate\\.year"];
        UI.preventNonNumericInput(numericInputs);
    }
}(window.StartDatePage = window.StartDatePage || {}, jQuery));

// TradingNamePage module
(function (TradingNamePage, $, undefined) {
    TradingNamePage.init = function() {
        UI.hideShowOnRadioButton("tradingNameRadio",
            { "#tradingNameRadio-trading_name_yes": "#trading_name_panel" });
    }
}(window.TradingNamePage = window.TradingNamePage || {}, jQuery));

// VoluntaryRegistrationReasonPage module
(function (VoluntaryRegistrationReasonPage, $, undefined) {
    VoluntaryRegistrationReasonPage.init = function() {
        UI.hideShowOnRadioButton("voluntaryRegistrationReasonRadio",
            { "#voluntaryRegistrationReasonRadio-neither": "#neither_panel" });
    }
}(window.VoluntaryRegistrationReasonPage = window.VoluntaryRegistrationReasonPage || {}, jQuery));

// Company Bank Account Details module
(function (CompanyBankAccountDetailsPage, $, undefined) {
    CompanyBankAccountDetailsPage.init = function() {
        var numericInputs = ["accountNumber",  "sortCode\\.part1", "sortCode\\.part2", "sortCode\\.part3"];
        UI.preventNonNumericInput(numericInputs);
    }
}(window.CompanyBankAccountDetailsPage = window.CompanyBankAccountDetailsPage || {}, jQuery));

// Estimate Vat Turnover
(function (EstimateVatTurnoverPage, $, undefined) {
    EstimateVatTurnoverPage.init = function() {
        var numericInputs = ["turnoverEstimate"];
        UI.preventNonNumericInput(numericInputs);
    }
}(window.EstimateVatTurnoverPage = window.EstimateVatTurnoverPage || {}, jQuery));


// Estimate Zero Rated Sales
(function (EstimateZeroRatedSalesPage, $, undefined) {
    EstimateZeroRatedSalesPage.init = function() {
        var numericInputs = ["zeroRatedTurnoverEstimate"];
        UI.preventNonNumericInput(numericInputs);
    }
}(window.EstimateZeroRatedSalesPage = window.EstimateZeroRatedSalesPage || {}, jQuery));

// Labour Compliance - Workers
(function (WorkersPage, $, undefined) {
    WorkersPage.init = function() {
        var numericInputs = ["numberOfWorkers"];
        UI.preventNonNumericInput(numericInputs);
    }
}(window.WorkersPage = window.WorkersPage || {}, jQuery));



// Business Contact Details Page
(function (BusinessContactDetailsPage, $, undefined) {
    BusinessContactDetailsPage.init = function() {
        var phone1error = $('#daytimePhone-error-summary');
        var phone2error = $('#mobile-error-summary');
        if (phone1error.text() === phone2error.text()) phone2error.parent('li').hide();
    }
}(window.BusinessContactDetailsPage = window.BusinessContactDetailsPage || {}, jQuery));

// Officer Contact Details Page
(function (OfficerContactDetailsPage, $, undefined) {
    OfficerContactDetailsPage.init = function() {
        var daytimePhoneError = $('#daytimePhone-error-summary');
        var mobileError = $('#mobile-error-summary');
        var emailError = $('#email-error-summary');
        if (emailError.text() === mobileError.text())
            mobileError.parent('li').hide();
        if (emailError.text() === daytimePhoneError.text())
            daytimePhoneError.parent('li').hide();
    }
}(window.OfficerContactDetailsPage = window.OfficerContactDetailsPage || {}, jQuery));

// Officer DOB page
(function (OfficerDOBPage, $, undefined) {
    OfficerDOBPage.init = function() {
        var numericInputs = ["dob\\.day", "dob\\.month", "dob\\.year"];
        UI.preventNonNumericInput(numericInputs);
    }
}(window.OfficerDOBPage = window.OfficerDOBPage || {}, jQuery));

// FormerNamePage module
(function (FormerNamePage, $, undefined) {
    FormerNamePage.init = function() {
        UI.hideShowOnRadioButton("formerNameRadio",
            { "#formerNameRadio-true": "#former_name_panel" });
    }
}(window.FormerNamePage = window.FormerNamePage || {}, jQuery));

/*
 example of multiple hide/show areas
 UI.hideShowOnRadioButton("startDate",
 {   "#startDate-specific_date": "#specific_date_panel",
     "#startDate-when_registered": "#other_panel"   });
 */

$(document).ready($(function() {

    var startDateRadioGroup = $("input[name='startDate']:radio");
    var futureDateRadio = $("#startDate-future_date");
    var futureDateHidden = $("#future_date_hidden");

    var show = function(selector) {
        $(selector).removeClass("hidden");
    }

    var hide = function(selector) {
        $(selector).addClass("hidden");
    }

    var updateState = function() {
        if(futureDateRadio.is(":checked")) {
            show(futureDateHidden);
        } else {
            hide(futureDateHidden);
        }
    }

    updateState();
    startDateRadioGroup.on("change", function () {
        updateState();
    });

}));

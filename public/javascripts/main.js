$(document).ready($(function() {

    var startDateRadioGroup = $("input[name='startDate']:radio");
    var futureDateRadio = $("#startDate-future_date");
    var futureDateHidden = $("#future_date_hidden");

    startDateRadioGroup.on("change", function () {


        if(futureDateRadio.is(":checked")) {
            console.log("future radio changed! -> " + JSON.stringify(this.value));
            futureDateHidden.show();
        } else {
            console.log("future radio changed! -> " + JSON.stringify(this.value));
            futureDateHidden.hide();
        }
    });


}));

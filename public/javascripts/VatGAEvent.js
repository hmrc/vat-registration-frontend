$(document).ready($(function () {
    submitVatPeriodsToGA();
}));

var submitVatPeriodsToGA = function() {
    if($("#vatAccountPeriodGAEventDoNotRemove").length) {
        ga('send', 'event', {
            'eventCategory': 'vat_register',
            'eventAction': 'select_vat_account_period',
            'eventLabel': $('#vatAccountPeriodGAEventDoNotRemove').text()
        });
    } 
};

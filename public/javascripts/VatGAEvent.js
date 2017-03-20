/**
 * Created by reddy on 20/03/17.
 */

$(document).ready($(function () {
    submitVatPeriodsToGA();
}));

var submitVatPeriodsToGA = function() {
    if($("#vatAccountPeriod").length) {
        ga('send', 'event', {
            'eventCategory': 'vat_register',
            'eventAction': 'select',
            'eventLabel': 'vat_account_period',
            'eventValue': $('#vatAccountPeriod').text()
        });
    } 
};

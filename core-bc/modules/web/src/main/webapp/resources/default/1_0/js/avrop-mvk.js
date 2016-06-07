function findMoreInfo(e) {
    var target = jq(e.target);
    var td = target.parents('td');
    return td.find('.more-info');
}

function findShowMoreLink(e) {
    var target = jq(e.target);
    var td = target.parents('td');
    return td.find('.show-more-link');
}

function findShowLessLink(e) {
    var target = jq(e.target);
    var td = target.parents('td');
    return td.find('.show-less-link');
}

function initCommon() {
    jq('.toggle-trigger').unbind();

    jq('.toggle-trigger').click(function (e) {
        var forId = e.target.getAttribute('data-toggle-for');
        jq('#' + forId).toggle();

        jq(e.target).toggleClass('expand');
        jq(e.target).toggleClass('minimize');
    });
}

function initOrderPage() {

    jq('.show-more-link').click(function (e) {
        e.preventDefault();

        var moreInfo = findMoreInfo(e);

        moreInfo.show();

        findShowMoreLink(e).hide();
        findShowLessLink(e).show();
    });

    jq('.show-less-link').click(function (e) {
        e.preventDefault();

        var moreInfo = findMoreInfo(e);

        moreInfo.hide();

        findShowMoreLink(e).show();
        findShowLessLink(e).hide();
    });

}

function initHomeDeliveryPage() {
    var confirmDialog = jq("#dialog").dialog({
        autoOpen: false,
        dialogClass: 'confirm-empty-door-code',
        modal: true,
        width: '80%'
    });

    jq('.to-be-confirmed-submit-button').on('click', function (e) {
        var doorCodeInput = jq('#homeDeliveryForm\\:doorCodeField');

        if (doorCodeInput.val().length == 0) {
            e.preventDefault();
            confirmDialog.dialog('open');
        }
    });

    jq('.cancel-button').on('click', function (e) {
        e.preventDefault();
        confirmDialog.dialog('close');
    });
}

function handleProgressWithSpinner(data, successCallback) {
    var ajaxStatus = data.status; // Can be "begin", "success" and "complete"

    switch (ajaxStatus) {
        case "begin": // This is called right before ajax request is been sent
            jq('.spinner').show();
            break;

        case "complete": // This is called right after ajax response is received.
            jq('.spinner').hide();
            
            break;
        case "success": // This is called when ajax response is successfully processed.
            if (successCallback) {
                successCallback();
            }
            break;
        default:
            alert(ajaxStatus);
            break;
    }
}

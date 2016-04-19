function findMoreInfo(e) {
    var target = jq(e.target);
    var td = target.parents('td')
    var moreInfo = td.find('.more-info')
    return moreInfo;
}

function findShowMoreLink(e) {
    var target = jq(e.target);
    var td = target.parents('td')
    return td.find('.show-more-link');
}

function findShowLessLink(e) {
    var target = jq(e.target);
    var td = target.parents('td')
    return td.find('.show-less-link');
}

function initCommon() {
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
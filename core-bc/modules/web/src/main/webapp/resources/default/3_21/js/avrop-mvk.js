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

    jq('.full-page-submit').click(function () {
        setTimeout(function () {
            jq('.full-page-submit').attr('disabled', 'disabled');
        }, 100);
    });

    var formWhichMayWaitToSubmit = jq('form.wait-for-ajax');

    if (formWhichMayWaitToSubmit) {
        formWhichMayWaitToSubmit.on('submit', function (e) {
            if (statusHelper.shouldWait) {
                // We wait for the change event request to finish
                e.preventDefault();

                // When ready we "click" the submit button again.
                statusHelper.callback = function () {
                    jq('.full-page-submit').removeAttr('disabled');
                    jq('.full-page-submit').click();
                };
            }
        });
    }
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

    var confirmOrderButton = jq('.confirm-order-button');

    var processLoadingText = function () {
        setTimeout(function () {
            var text = confirmOrderButton.attr('value');
            switch (text) {
                case 'Bearbetar   ':
                    confirmOrderButton.attr('value', 'Bearbetar.  ');
                    break;
                case 'Bearbetar.  ':
                    confirmOrderButton.attr('value', 'Bearbetar.. ');
                    break;
                case 'Bearbetar.. ':
                    confirmOrderButton.attr('value', 'Bearbetar...');
                    break;
                case 'Bearbetar...':
                    confirmOrderButton.attr('value', 'Bearbetar   ');
                    break;
                default:
                    break;
            }

            processLoadingText();
        }, 300);
    };

    confirmOrderButton.click(function (e) {
        var width = confirmOrderButton.css('width');
        confirmOrderButton.css('min-width', width);
        confirmOrderButton.attr('value', 'Bearbetar...');

        processLoadingText();
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
        var doorCodeInput = jq('#homeDeliveryForm\\:homeAddress\\:doorCodeField');

        if (doorCodeInput.val().length === 0) {
            e.preventDefault();
            confirmDialog.dialog('open');
        }
    });

    jq('.cancel-button').on('click', function (e) {
        e.preventDefault();
        confirmDialog.dialog('close');
        jq('#homeDeliveryForm\\:homeAddress\\:doorCodeField').focus();
    });
}

function initImagePreview() {
    var imageDialog = jq("#imageDialog").dialog({
        autoOpen: false,
        dialogClass: 'image-preview',
        modal: true,
        height: '100',
        width: '100'
    });

    jq('.image-preview-link').click(function (e) {
        e.preventDefault();
        var url = e.target.getAttribute('data-image-url');

        jq('#previewImage').remove();
        jq('#spinner').show();

        var img = jq('<img id="previewImage" src="' + url + '" style="display: none"/>');
        img.load(function(){
            jq('#spinner').hide();

            updatePositionAndSize(imageDialog, img)
            document.body.onresize = function() {updatePositionAndSize(imageDialog, img);};

            setTimeout(function () {
                img.css('display', 'block');
            }, 250);

            jq('#imageDialog').bind('click', function(){
                imageDialog.dialog('close');
            });
        });

        jq('#imageDialog').append(img);
        imageDialog.dialog('open');
        imageDialog.height(100);
        imageDialog.width(100);

        jq('.ui-widget-overlay.ui-front').bind('click', function(){
            imageDialog.dialog('close');
        });
    });
}

function updatePositionAndSize(imageDialog, img) {
    imageDialog.height(img.height() + 4);
    imageDialog.width(img.width());
    imageDialog.position({my: 'center', at: 'center', of: window});
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

var statusHelper = {
    shouldWait: false,
    callback: null
};

// This is to mitigate the problem when submit and ajax request from e.g. a change event creates a race condition. So we make sure the ajax completes first
function ajaxToBeWaitedFor(data) {
    var ajaxStatus = data.status; // Can be "begin", "success" and "complete"

    switch (ajaxStatus) {
        case "begin": // This is called right before ajax request is been sent
            statusHelper.shouldWait = true;
            break;

        case "complete": // This is called right after ajax response is received.
            break;
        case "success": // This is called when ajax response is successfully processed.
            statusHelper.shouldWait = false;
            if (statusHelper.callback) {
                statusHelper.callback();
            }

            break;
        default:
            break;
    }
}

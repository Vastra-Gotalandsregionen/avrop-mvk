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
    })
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
        var doorCodeInput = jq('#homeDeliveryForm\\:doorCodeField');

        if (doorCodeInput.val().length == 0) {
            e.preventDefault();
            confirmDialog.dialog('open');
        }
    });

    jq('.cancel-button').on('click', function (e) {
        e.preventDefault();
        confirmDialog.dialog('close');
        jq('#homeDeliveryForm\\:doorCodeField').focus();
    });
}

function initImagePreview() {
    var imageDialog = jq("#imageDialog").dialog({
        autoOpen: false,
        dialogClass: 'image-preview',
        modal: true,
        height: 'auto',
        width: 'auto'
    });

    jq('.image-preview-link').click(function (e) {
        e.preventDefault();
        console.log('click');
        var url = e.target.getAttribute('data-image-url');

        var img = jq('<img src="' + url + '" style="max-width: 500px; max-height: 400px;"/>');
        img.load(function(){
            console.log('loaded...');
            imageDialog.dialog('open');
            jq('.ui-widget-overlay.ui-front').bind('click', function(){
                imageDialog.dialog('close');
            });
        });

        jq('#imageDialog').html(img);

        jq('.ui-widget-overlay.ui-front').bind('click', function(){
            imageDialog.dialog('close');
        });
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

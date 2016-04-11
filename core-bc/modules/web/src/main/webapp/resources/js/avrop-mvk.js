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

        var showMoreLink = findShowMoreLink(e);

        findShowMoreLink(e).show();
        findShowLessLink(e).hide();
    });
}

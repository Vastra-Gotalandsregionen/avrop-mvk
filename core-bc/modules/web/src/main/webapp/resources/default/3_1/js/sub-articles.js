var realOldVal = JSON.parse(JSON.stringify(app.jsonData));

app.$watch('jsonData', function (newVal, oldVal) {
    if (JSON.stringify(newVal) === JSON.stringify(realOldVal)) {
        // Nothing's changed...
        return;
    }

    app.discrepanceItems = [];

    for (var outerIndex in newVal) {
        var sum = 0;

        for (var index in newVal[outerIndex].subArticles) {
            sum += Number(newVal[outerIndex].subArticles[index].orderCount);
        }

        app.jsonData[outerIndex].distributedNumber = sum;

        if (app.jsonData[outerIndex].distributedNumber !== app.jsonData[outerIndex].totalOrderSize) {
            app.discrepanceItems.push(app.jsonData[outerIndex].parentArticleName);
        }

        // Sync values to JSF components
        for (var index in app.jsonData[outerIndex].subArticles) {
            document.getElementsByClassName('jsf-input-' + outerIndex + '-' + index)[0].value = app.jsonData[outerIndex].subArticles[index].orderCount;
        }

        realOldVal = JSON.parse(JSON.stringify(newVal));

    }
}, {deep: true});

jq('.circle-icon').click(function (e) {
    var parentArticleIndex = e.target.getAttribute('data-parent-article-index');
    var subArticleIndex = e.target.getAttribute('data-sub-article-index');

    var relevantElement;
    if (!parentArticleIndex && !subArticleIndex) {
        // try parent (user might have clicked the child content
        parentArticleIndex = e.target.parentElement.getAttribute('data-parent-article-index');
        subArticleIndex = e.target.parentElement.getAttribute('data-sub-article-index');
        relevantElement = e.target.parentElement;
    } else {
        relevantElement = e.target;
    }

    var currentCount = app.jsonData[parentArticleIndex].subArticles[subArticleIndex].orderCount;
    var totalOrderSize = app.jsonData[parentArticleIndex].totalOrderSize;

    if (relevantElement.classList.contains('plus') && (currentCount < totalOrderSize)) {
        app.jsonData[parentArticleIndex].subArticles[subArticleIndex].orderCount = Number(currentCount) + 1;
    } else if (relevantElement.classList.contains('minus') && currentCount > 0) {
        app.jsonData[parentArticleIndex].subArticles[subArticleIndex].orderCount = Number(currentCount) - 1;
    }

    makeUnselectable(e.target);
});

jq('.circle-icon-content').click(function (e) {
    makeUnselectable(e.target);
});

// To avoid double-click
function makeUnselectable(elem) {
    if (typeof(elem) == 'string')
        elem = document.getElementById(elem);
    if (elem) {
        elem.onselectstart = function() { return false; };
        elem.style.MozUserSelect = "none";
        elem.style.KhtmlUserSelect = "none";
        elem.unselectable = "on";
    }
}
var realOldVal = JSON.parse(JSON.stringify(app.jsonData));

var subArticlesPriority = {};

app.$watch('jsonData', function (newVal, oldVal) {
    if (JSON.stringify(newVal) === JSON.stringify(realOldVal)) {
        console.log("Nothing's changed...");
        return;
    }
    for (var outerIndex in newVal) {
        var changedIndex = null;
        var diff = null;
        for (var index in newVal[outerIndex].subArticles) {
            var newOrderCount = Number(newVal[outerIndex].subArticles[index].orderCount);
            var oldOrderCount = Number(realOldVal[outerIndex].subArticles[index].orderCount);
            if (newOrderCount !== oldOrderCount) {
                changedIndex = index;
                diff = newOrderCount - oldOrderCount;
            }
        }
        if (changedIndex) {
            // Get or setup priority order for which subarticles' count which are first adjusted
            if (!subArticlesPriority[outerIndex]) {
                // We haven't setup any priority for this parent article yet
                subArticlesPriority[outerIndex] = [];
                for (var i in newVal[outerIndex].subArticles) {
                    subArticlesPriority[outerIndex].push(i);
                }
            }

            // Remove currently changed index and put last in the priority list (to make it most recent changed and thus prioritized)
            var indexOfChangedIndex = subArticlesPriority[outerIndex].indexOf(changedIndex);
            subArticlesPriority[outerIndex].splice(indexOfChangedIndex, 1);
            subArticlesPriority[outerIndex].push(changedIndex);

            // Find other range(s) to compensate for the change...
            var numbersLeftToRegulate = diff;

            var loopIndex = 0;
            while (numbersLeftToRegulate !== 0) {
                var subArticleIndexToInvestigate = subArticlesPriority[outerIndex][loopIndex];
                var orderCount = Number(app.jsonData[outerIndex].subArticles[subArticleIndexToInvestigate].orderCount);
                if (diff > 0) {
                    // We need to remove from others
                    // First check if we can remove from the first in the list and possibly go on
                    if (orderCount > 0) {
                        // We can at least take something. Check if we can take all we need.
                        if (orderCount >= numbersLeftToRegulate) {
                            app.jsonData[outerIndex].subArticles[subArticleIndexToInvestigate].orderCount = Number(orderCount) - numbersLeftToRegulate;
                            numbersLeftToRegulate -= numbersLeftToRegulate;
                        } else {
                            app.jsonData[outerIndex].subArticles[subArticleIndexToInvestigate].orderCount = Number(orderCount) - orderCount;
                            numbersLeftToRegulate -= orderCount;
                        }
                    }
                } else {
                    // We need to add to others
                    app.jsonData[outerIndex].subArticles[subArticleIndexToInvestigate].orderCount = Number(orderCount) - numbersLeftToRegulate;
                    numbersLeftToRegulate = 0;
                }

                loopIndex++;
                var subArticlesLength = newVal[outerIndex].subArticles.length;
                while (loopIndex >= subArticlesLength) {
                    loopIndex -= subArticlesLength;
                }
            }

            // Sync values to JSF components
            for (var index in app.jsonData[outerIndex].subArticles) {
                document.getElementsByClassName('jsf-input-' + outerIndex + '-' + index)[0].value = app.jsonData[outerIndex].subArticles[index].orderCount;
            }
            realOldVal = JSON.parse(JSON.stringify(newVal));
        }
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
});

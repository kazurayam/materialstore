$(document).ready(function() {
    $("input[id^='ignorable']").on("click", function () {
        let index = $(this).attr("data-index");
        let checked = $(this).prop("checked");
        let diffRatio = model.mProductList[index].diffRatio;
        console.log($(this).prop("id") +
            " with data-index=" + index +
            " with checked="  + checked);
        let ratioSpan = $(this).next("button").find("span.ratio")
        if (diffRatio > model.criteria) {
            if (checked) {
                console.log($(this).prop("id") + " is ignored. The diffRatio(" +
                    diffRatio +
                    ") is larger than the criteria(" +
                    model.criteria + ") but is checked.");
                ratioSpan.removeClass("warning");
                ratioSpan.addClass("ignored");
            } else {
                console.log($(this).prop("id") + " is warned. The diffRatio(" +
                    diffRatio +
                    ") is larger than the criteria(" +
                    model.criteria + ") and is unchecked.");
                ratioSpan.removeClass("ignored");
                ratioSpan.addClass("warning");
            };
        } else {
            console.log($(this).prop("id") + " is no problem The diffRatio(" +
                diffRatio +
                ") is smaller than or equal to the criteria(" +
                model.criteria + ").");
        }
    });
});

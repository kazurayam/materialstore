
/* onLoad driven by jQuery */
$(document).ready(function() {
    /* try to restore the model from localStorage */
    let loadedModel = loadModel();
    if (loadedModel === null || loadModel.mProductList === undefined ||
            !('mProductList' in loadModel)) {
        console.log("The model was not restored from localStorage. Use the given model as is.");
    } else {
        // restore the "checked" status of MaterialProducts in the current model with the loaded one.
        loadedModel.mProductList.forEach(storedMProduct => {
            model.mProductList.forEach(currentMProduct => {
                if (currentMProduct.fileTypeExtension === storedMProduct.fileTypeExtension &&
                    currentMProduct.description === storedMProduct.description) {
                    // restore the status
                    currentMProduct.checked = storedMProduct.checked;
                    console.log(currentMProduct.fileTypeExtension + " " +
                        currentMProduct.description + " restored the checked status to be " +
                        currentMProduct.checked
                    );
                }
            });
        });
    }

    /* iterate over the sequence of MaterialProduct to display
     * the <span class="ratio"> with appropriate class ("warning", "ignored" or none)
     * in sync with the current model
     */
    $("input[id^='ignorable']").each(function(index, value) {
        const id = $(this).prop("id");
        const checked = $(this).prop("checked");
        const diffRatio = model.mProductList[index].diffRatio;
        console.log(id + " with data-index=" + index + " with checked="  + checked);
        const ratioSpan = $(this).next("button").find("span.ratio")
        classifyRatioSpan(ratioSpan, index, id, checked, diffRatio);
    });

    /* Register onclick event handler to the <input id="ignorableX"> elements */
    $("input[id^='ignorable']").on("click", function () {
        const index = $(this).attr("data-index");
        const id = $(this).prop("id");
        const checked = $(this).prop("checked");
        const diffRatio = model.mProductList[index].diffRatio;
        console.log(id + " with data-index=" + index + " with checked="  + checked);
        const ratioSpan = $(this).next("button").find("span.ratio")
        classifyRatioSpan(ratioSpan, index, id, checked, diffRatio);
        storeModel(model);
    });
});

/* store the model into the localStorage as a JSON string */
function storeModel(mdl) {
    localStorage.setItem('model', JSON.stringify(mdl));
    console.log("stored the model into localStorage");
}

/* load the model as a JSON string from the localStorage.
 * parse the string to create a object, and return it.
 * This may return null
 */
function loadModel() {
    console.log("loading the model from localStorage");
    const json = localStorage.getItem('model');
    if (json !== null) {
        console.log("localStorage.getItem('model') returned something non-null; will parse into an object");
        return JSON.parse(json);
    } else {
        console.log("localStorage.getItem('model') returned null. ");
        return null;
    }
}

function setChecked(model, index, checked) {
    model.mProductList[index].checked = checked;
    console.log("set model.mProductList[" + index + "].checked to be " + checked);
}

function classifyRatioSpan(ratioSpan, index, id, checked, diffRatio) {
    if (diffRatio > model.criteria) {
        if (checked) {
            console.log(id + " is ignored. The diffRatio(" +
                diffRatio + ") is larger than the criteria(" +
                model.criteria + ") but is checked.");
            ratioSpan.removeClass("warning");
            ratioSpan.addClass("ignored");
            setChecked(model, index, true);
        } else {
            console.log(id + " is warned. The diffRatio(" +
                diffRatio + ") is larger than the criteria(" +
                model.criteria + ") and is unchecked.");
            ratioSpan.removeClass("ignored");
            ratioSpan.addClass("warning");
            setChecked(model, index, false);
        }
    } else {
        console.log(id + " is no problem The diffRatio(" +
            diffRatio + ") is smaller than or equal to the criteria(" +
            model.criteria + ").");
    }
}

/**
 * mProductGroup example
 *
 {
  "jobName": "MyAdmin_Main_Twins",
  "resultTimestamp": "20220409_101751",
  "criteria": 0.0,
  "isReadyToReport": true,
  "ignoreMetadataKeys": [
    "URL.host",
    "URL.port",
    "profile"
  ],
  "materialList0": {
    "jobTimestamp": "20220409_101728",
    "queryOnMetadata": {
      "profile": "MyAdmin_ProductionEnv"
    },
    "size": 2.0
  },
  "materialList1": {
    "jobTimestamp": "20220409_101742",
    "queryOnMetadata": {
      "profile": "MyAdmin_DevelopmentEnv"
    },
    "size": 2.0
  },
  "mProductList": [
    {
      "reducedTimestamp": "20220409_101751",
      "checked": false,
      "diffRatio": 15.7,
      "fileTypeExtension": "png",
      "queryOnMetadata": {
        "URL.path": "/",
        "URL.protocol": "http",
        "selector": "body"
      },
      "description": "{\"URL.path\":\"/\", \"URL.protocol\":\"http\", \"selector\":\"body\"}",
      "left": {
        "jobName": "MyAdmin_Main_Twins",
        "jobTimestamp": "20220409_101728",
        "id": "3e334c05a4be69ad5bcb3b145d78ca19d6c5fc62",
        "fileType": "png",
        "metadata": {
          "URL.host": {
            "key": "URL.host",
            "value": "myadmin.kazurayam.com",
            "ignoredByKey": true
          },
          "URL.path": {
            "key": "URL.path",
            "value": "/",
            "paired": true
          },
          "URL.port": {
            "key": "URL.port",
            "value": "80",
            "ignoredByKey": true
          },
          "URL.protocol": {
            "key": "URL.protocol",
            "value": "http",
            "paired": true
          },
          "profile": {
            "key": "profile",
            "value": "MyAdmin_ProductionEnv",
            "ignoredByKey": true,
            "matchedIndividually": true
          },
          "selector": {
            "key": "selector",
            "value": "body",
            "paired": true
          }
        },
        "metadataText": "{\"URL.host\":\"myadmin.kazurayam.com\", \"URL.path\":\"/\", \"URL.port\":\"80\", \"URL.protocol\":\"http\", \"profile\":\"MyAdmin_ProductionEnv\", \"selector\":\"body\"}",
        "metadataURL": "http://myadmin.kazurayam.com/",
        "relativeUrl": "MyAdmin_Main_Twins/20220409_101728/objects/3e334c05a4be69ad5bcb3b145d78ca19d6c5fc62.png",
        "diffability": "AS_IMAGE"
      },
      "right": {
        "jobName": "MyAdmin_Main_Twins",
        "jobTimestamp": "20220409_101742",
        "id": "1b4797af1b8bae75209fe890fb1538849a561059",
        "fileType": "png",
        "metadata": {
          "URL.host": {
            "key": "URL.host",
            "value": "devadmin.kazurayam.com",
            "ignoredByKey": true
          },
          "URL.path": {
            "key": "URL.path",
            "value": "/",
            "paired": true
          },
          "URL.port": {
            "key": "URL.port",
            "value": "80",
            "ignoredByKey": true
          },
          "URL.protocol": {
            "key": "URL.protocol",
            "value": "http",
            "paired": true
          },
          "profile": {
            "key": "profile",
            "value": "MyAdmin_DevelopmentEnv",
            "ignoredByKey": true,
            "matchedIndividually": true
          },
          "selector": {
            "key": "selector",
            "value": "body",
            "paired": true
          }
        },
        "metadataText": "{\"URL.host\":\"devadmin.kazurayam.com\", \"URL.path\":\"/\", \"URL.port\":\"80\", \"URL.protocol\":\"http\", \"profile\":\"MyAdmin_DevelopmentEnv\", \"selector\":\"body\"}",
        "metadataURL": "http://devadmin.kazurayam.com/",
        "relativeUrl": "MyAdmin_Main_Twins/20220409_101742/objects/1b4797af1b8bae75209fe890fb1538849a561059.png",
        "diffability": "AS_IMAGE"
      },
      "diff": {
        "jobName": "MyAdmin_Main_Twins",
        "jobTimestamp": "20220409_101751",
        "id": "0862eaf93aa921703b1b1c961abbe905b74eeebf",
        "fileType": "png",
        "metadata": {
          "category": {
            "key": "category",
            "value": "diff"
          },
          "left": {
            "key": "left",
            "value": "3e334c05a4be69ad5bcb3b145d78ca19d6c5fc62"
          },
          "ratio": {
            "key": "ratio",
            "value": "15.70%"
          },
          "right": {
            "key": "right",
            "value": "1b4797af1b8bae75209fe890fb1538849a561059"
          }
        },
        "metadataText": "{\"category\":\"diff\", \"left\":\"3e334c05a4be69ad5bcb3b145d78ca19d6c5fc62\", \"ratio\":\"15.70%\", \"right\":\"1b4797af1b8bae75209fe890fb1538849a561059\"}",
        "metadataURL": "file://null_object",
        "relativeUrl": "MyAdmin_Main_Twins/20220409_101751/objects/0862eaf93aa921703b1b1c961abbe905b74eeebf.png",
        "diffability": "AS_IMAGE"
      }
    },
    {
      "reducedTimestamp": "20220409_101751",
      "checked": false,
      "diffRatio": 31.04,
      "fileTypeExtension": "html",
      "queryOnMetadata": {
        "URL.path": "/",
        "URL.protocol": "http",
        "selector": "body"
      },
      "description": "{\"URL.path\":\"/\", \"URL.protocol\":\"http\", \"selector\":\"body\"}",
      "left": {
        "jobName": "MyAdmin_Main_Twins",
        "jobTimestamp": "20220409_101728",
        "id": "26b487d4b1b1eb52d18fc0df4ac9384275cf3fd9",
        "fileType": "html",
        "metadata": {
          "URL.host": {
            "key": "URL.host",
            "value": "myadmin.kazurayam.com",
            "ignoredByKey": true
          },
          "URL.path": {
            "key": "URL.path",
            "value": "/",
            "paired": true
          },
          "URL.port": {
            "key": "URL.port",
            "value": "80",
            "ignoredByKey": true
          },
          "URL.protocol": {
            "key": "URL.protocol",
            "value": "http",
            "paired": true
          },
          "profile": {
            "key": "profile",
            "value": "MyAdmin_ProductionEnv",
            "ignoredByKey": true,
            "matchedIndividually": true
          },
          "selector": {
            "key": "selector",
            "value": "body",
            "paired": true
          }
        },
        "metadataText": "{\"URL.host\":\"myadmin.kazurayam.com\", \"URL.path\":\"/\", \"URL.port\":\"80\", \"URL.protocol\":\"http\", \"profile\":\"MyAdmin_ProductionEnv\", \"selector\":\"body\"}",
        "metadataURL": "http://myadmin.kazurayam.com/",
        "relativeUrl": "MyAdmin_Main_Twins/20220409_101728/objects/26b487d4b1b1eb52d18fc0df4ac9384275cf3fd9.html",
        "diffability": "AS_TEXT"
      },
      "right": {
        "jobName": "MyAdmin_Main_Twins",
        "jobTimestamp": "20220409_101742",
        "id": "63afd374b90b1292ae011b83a26d78a77dc34287",
        "fileType": "html",
        "metadata": {
          "URL.host": {
            "key": "URL.host",
            "value": "devadmin.kazurayam.com",
            "ignoredByKey": true
          },
          "URL.path": {
            "key": "URL.path",
            "value": "/",
            "paired": true
          },
          "URL.port": {
            "key": "URL.port",
            "value": "80",
            "ignoredByKey": true
          },
          "URL.protocol": {
            "key": "URL.protocol",
            "value": "http",
            "paired": true
          },
          "profile": {
            "key": "profile",
            "value": "MyAdmin_DevelopmentEnv",
            "ignoredByKey": true,
            "matchedIndividually": true
          },
          "selector": {
            "key": "selector",
            "value": "body",
            "paired": true
          }
        },
        "metadataText": "{\"URL.host\":\"devadmin.kazurayam.com\", \"URL.path\":\"/\", \"URL.port\":\"80\", \"URL.protocol\":\"http\", \"profile\":\"MyAdmin_DevelopmentEnv\", \"selector\":\"body\"}",
        "metadataURL": "http://devadmin.kazurayam.com/",
        "relativeUrl": "MyAdmin_Main_Twins/20220409_101742/objects/63afd374b90b1292ae011b83a26d78a77dc34287.html",
        "diffability": "AS_TEXT"
      },
      "diff": {
        "jobName": "MyAdmin_Main_Twins",
        "jobTimestamp": "20220409_101751",
        "id": "c918a10a5357a45c605dd1cfdec822b6c9224025",
        "fileType": "html",
        "metadata": {
          "category": {
            "key": "category",
            "value": "diff"
          },
          "left": {
            "key": "left",
            "value": "26b487d4b1b1eb52d18fc0df4ac9384275cf3fd9"
          },
          "ratio": {
            "key": "ratio",
            "value": "31.04%"
          },
          "right": {
            "key": "right",
            "value": "63afd374b90b1292ae011b83a26d78a77dc34287"
          }
        },
        "metadataText": "{\"category\":\"diff\", \"left\":\"26b487d4b1b1eb52d18fc0df4ac9384275cf3fd9\", \"ratio\":\"31.04%\", \"right\":\"63afd374b90b1292ae011b83a26d78a77dc34287\"}",
        "metadataURL": "file://null_object",
        "relativeUrl": "MyAdmin_Main_Twins/20220409_101751/objects/c918a10a5357a45c605dd1cfdec822b6c9224025.html",
        "diffability": "AS_TEXT"
      }
    }
  ]
}
 */
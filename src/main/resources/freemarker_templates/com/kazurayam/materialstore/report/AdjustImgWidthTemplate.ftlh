<#-- report/AdjustImgWidthTemplate.ftlh -->

<#macro adjustImgWidthScript>
    <script type="text/javascript">
        /*
         * ths function adjusts the width of an img element
         */
        function adjustImgWidth() {
            // the width of browser window = width of <body> element
            let bodyWidth = document.getElementsByTagName('body')[0].clientWidth;
            //console.log("bodyWidth=" + bodyWidth);
            let tights = this.parentElement;
            let img = new Image();
            img.src = this.src;     // load the PNG to measure the width
            //console.log("img.width=" + img.width);
            if (img.width < bodyWidth * 0.75) {
                // if the PNG has smaller width than 75% of the window width,
                // make the bounding <div> to be tight
                // so that the <img> shows the graphics as is = unresized
                tights.style.width = img.width + "px";
                tights.style.margin = "0 auto";
            }
            // if the PNG has larger width than the window, Bootstrap's img-fluid class
            // will resize the image to fit in the window.
        }
    </script>
</#macro>
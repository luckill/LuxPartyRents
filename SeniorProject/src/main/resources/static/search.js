// Functionality for front page
document.addEventListener("DOMContentLoaded", function() {
    var frontPageForm = document.getElementById("front-page-form");
    if (frontPageForm) {
        frontPageForm.addEventListener("submit", function(event) {
            event.preventDefault(); // Prevent default form submission

            var kw = document.getElementById("product-search").value;
            if (kw.trim() !== "") {
                window.location.href = "rental?kw=" + encodeURIComponent(kw);
            }
        });
    }
});

// Functionality for catalog page
document.addEventListener("DOMContentLoaded", function() {
    var params = new URLSearchParams(window.location.search);
    var kw = params.get("kw");

    if (kw) {
        console.log("Performing search for keywords:", kw);
        filterItems(kw);
    }
});

function filterItems(keyword) {
     // get the text from the textbox
    const textLine = keyword.toLowerCase();
    // set the search bar to the keyword
    const searchBar = document.getElementById("product-search").value = keyword;
    const items = document.querySelectorAll("option");
    // if textLine is nul then just return
    if (textLine.length === 0) {resetAllItems(items);}
    // check wether or not the text is apart of one of the id
    items.forEach(item => {
        // if value doesn't contain anything from the text line then set display to none
        const itemName = item.value.replace(/\s+/g, '-').toLowerCase();
        if (!item.value.toLowerCase().includes(textLine)) {
            console.log(itemName);
            document.getElementById(itemName).setAttribute("style", "display: none;");
        } else { document.getElementById(itemName).setAttribute("style", "display: ;"); }
    })
}

function resetAllItems(items) {
    items.forEach(item => {
        item.setAttribute("style", "display: ;")
    })
}
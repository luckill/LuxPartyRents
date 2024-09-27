// Functionality for front page
document.addEventListener("DOMContentLoaded", function() {
    var frontPageForm = document.getElementById("other-page-form");
    if (frontPageForm) {
        frontPageForm.addEventListener("submit", function(event) {
            event.preventDefault(); // Prevent default form submission

            var kw = document.getElementById("product-search").value;
            window.location.href = "rental?kw=" + encodeURIComponent(kw);
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
// I basically need to reroute this to work with the new rental page.
function filterItems(keyword) {
     // get the text from the textbox
    const textLine = keyword.toLowerCase().replace(/\s+/g, '');
    // set the search bar to the keyword
    document.getElementById("product-search").value = keyword;
    
    //const items = document.querySelectorAll("option");
    const itemFrames = document.querySelectorAll(".item-frame");
    // if textLine is nul then just return
    if (textLine.length === 0) {resetAllItems(items);}
    // Go through each itemFrame and filter
    itemFrames.forEach(item => {
        // if value doesn't contain anything from the text line then set display to none
        const itemName = item.querySelector(".item-name").id;
        if (!itemName.toLowerCase().includes(textLine)) {
            item.classList.add("d-none");
        } else { item.classList.remove("d-none"); }
    })
}

function resetAllItems(items) {
    // set all display none
    items.forEach(item => {
        item.classList.add("d-none");
    })
}
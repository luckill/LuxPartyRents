document.querySelector(".item-search-btn").addEventListener("click", filterItems);

document.querySelector("#product-search").addEventListener("keypress", function(e) {
    if (e.key === "Enter") {
         // Cancel the default action, if needed
        e.preventDefault();
        filterItems();
    }
})

function filterItems() {
    // get the text from the textbox
    const textLine = document.querySelector("#product-search").value.toLowerCase();
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
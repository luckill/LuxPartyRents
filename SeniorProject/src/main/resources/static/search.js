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
    if (window.location.pathname === '/rental') {
        document.getElementById('filterSelector').classList.remove('d-none');
        document.getElementById('filterLabel').classList.remove('d-none');
        document.getElementById('other-page-form').classList.add('input-group');
        document.getElementById('other-page-form').classList.add('w-50');
        document.getElementById('product-search').classList.add('rounded-end-3');   
    } else {
        document.getElementById('filterSelector').classList.add('d-none');
        document.getElementById('filterLabel').classList.add('d-none');
        document.getElementById('other-page-form').classList.remove('input-group');
        document.getElementById('other-page-form').classList.remove('w-50'); 
        document.getElementById('product-search').classList.remove('rounded-end-3');    
    }
});

// Filter types local storage
document.addEventListener("DOMContentLoaded", function() {
    const savedFilter = localStorage.getItem('selectedFilter');
    const filterSelector = document.getElementById('filterSelector');
    if (document.referrer.split("?")[0] != window.location.href.split("?")[0]) { 
        localStorage.clear(); 
        return; 
    }
    // If there's a saved filter in localStorage, apply it
    if (!savedFilter) {return;}
    let index = -1; // Default index if not found

        for (let i = 0; i < filterSelector.options.length; i++) {
            if (filterSelector.options[i].text === savedFilter) { 
                index = i;
                break; // Stop the loop once found
            }
        }

        filterSelector.options.selectedIndex = index;  // Set the select to the saved value
        if (filterSelector.options[filterSelector.value] != null) {
            filterTypes(filterSelector.options[filterSelector.value].text);
        } 
})

// Functionality for catalog page
document.addEventListener("DOMContentLoaded", function() {
    searchWithKeywords();
});

function searchWithKeywords() {
    var params = new URLSearchParams(window.location.search);
    var kw = params.get("kw");

    if (kw) {
        console.log("Performing search for keywords:", kw);
        filterItems(kw);
    }
}

// Add a listener for when the filter is changed
document.getElementById('filterSelector').addEventListener('change', function() {
    var selectedOption = this.options[this.selectedIndex];  // Get the selected option
    var selectedText = selectedOption.text;  // Get the text content of the selected option
    // now we need to filter based on type
    filterTypes(selectedText);
});
// I basically need to reroute this to work with the new rental page.
function filterItems(keyword) {
     // get the text from the textbox
    const textLine = keyword.toLowerCase().replace(/\s+/g, '');
    // set the search bar to the keyword
    document.getElementById("product-search").value = keyword;
    
    const itemFrames = document.querySelectorAll(".item-frame");
    // Go through each itemFrame and filter
    // if there is a type filter, we should filter the type first
    const filterSelector = document.getElementById('filterSelector');
    const chosenFilter = filterSelector.options[filterSelector.value];
    if (chosenFilter != null) {
        filterTypes(chosenFilter.text);
        // Filter items that dont have d-none
        itemFrames.forEach(item => {
            const itemNameDiv = item.querySelector(".item-name");
            if (!item.classList.contains("d-none")) {
                const itemName = itemNameDiv.id;
                // if value doesn't contain anything from the text line then set display to none
                if (!itemName.toLowerCase().includes(textLine)) {
                    item.classList.add("d-none");
                } else { item.classList.remove("d-none"); }
            }
        }) 
    } else {
        itemFrames.forEach(item => {
            // if value doesn't contain anything from the text line then set display to none
            const itemName = item.querySelector(".item-name").id;
            if (!itemName.toLowerCase().includes(textLine)) {
                item.classList.add("d-none");
            } else { item.classList.remove("d-none"); }
        })
    }
}

function filterTypes(typeChosen) {
    const itemFrames = document.querySelectorAll(".item-frame");
    // Save the selected filter to localStorage
    localStorage.setItem('selectedFilter', typeChosen);
    // if the type chosen is None
    if (typeChosen === "None") {
        // set all display
        itemFrames.forEach(item => {
            item.classList.remove("d-none");
        })
        return;
    }
    // Go through each itemFrame and filter
    itemFrames.forEach(item => {
        // if div doesnt contain the class of type then d-none
        const itemNameDiv = item.querySelector(".item-name");
        if (!itemNameDiv.classList.contains(typeChosen)) {
            item.classList.add("d-none");
        } else { item.classList.remove("d-none"); }
    }) 
    document.getElementById("product-search").focus();

}

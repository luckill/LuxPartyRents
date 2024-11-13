// This is dedicated to handling what is shown on the featured page. 
// Realistically, the button would only show up if the person logged in is an admin.
// document.addEventListener('DOMContentLoaded', addCardClickEvents);
const maxItems = 6;
let isEditing = false;
let selectedSlotIndex = 0;
let featuredItems = [];
const userRole = localStorage.getItem('Role');
// I have an idea to make the populate based on the table instead of populating based on looping through the featured
// This will allow for more dynamic placement of items and the possiblility to swap items
// Get the card template and container
const cardTemplate = document.getElementById('featured-item-list-card');
const container = document.getElementById('featured-item-list-container');

document.addEventListener('DOMContentLoaded', function() {
    const editButton = document.getElementById("edit-button");
    createFeaturedItems();
    // Initialize the container and populate it with all items
    //populateModal();
    if (localStorage.getItem('Role') === 'ADMIN' && editButton) {
        document.getElementById('edit-button').style.display = 'block';
        editButton.addEventListener('click', handleEditButton);
    }
});

// Function to handle card click
function handleCardClick() {
    const clickedCard = this; // Reference to the clicked card
    const container = document.getElementById('featured-items-container');
     
    if (isEditing == false) {
        console.log("Sending to rentals!");
        window.location.href = "/rental";
    } else {
        // Add or Edit
        // Get all child nodes of the container
        const cards = Array.from(container.children);
        const index = cards.indexOf(clickedCard);
        selectedSlotIndex = index - 3;    // Accounting for the 2 template cards and we want 0 indexing
        //console.log(selectedSlotIndex);
        // Change values in featuredItems based on index
        
        // Show the modal
        const modal = new bootstrap.Modal(document.getElementById('item-change-modal'));
        let searchInput = document.getElementById('featured-item-search');
        modal.show();
        // Add event listener for when the modal is opened
        document.getElementById('item-change-modal').addEventListener('show.bs.modal', function () {
            populateModal();
        });
        searchInput.value = "";
        handleModalSearch();
    }
}

async function createFeaturedItems() {
    // Await the result from getFeaturedItems
    const featuredItemsGotten = await getFeaturedItems();

    // Check if featuredItems is an array
    if (Array.isArray(featuredItemsGotten)) {
        // Iterate over the featured items and add each card
        for (let i = 0; i < featuredItemsGotten.length; i++) {
            addItemCard(featuredItemsGotten[i]);
        }
    } else {
        console.error("Featured items are not in the expected format:", featuredItemsGotten);
    }
}

function createTempFeaturedItems() {
    for (let i = 0; i < featuredItems.length; i++) {
        addItemCard(featuredItems[i]);
    } 
}

function removeAllFeaturedCards() {
    // Select all cloned item cards
    const clonedItemCards = document.querySelectorAll('.item-card-clone, .empty-card-clone');
    // Loop through each cloned card and remove it from the DOM
    clonedItemCards.forEach(card => {
        card.remove();
    });
}

function addItemCard(item) {
    //console.log(item);
    
    // Select the empty card and the container
    const itemCard = document.getElementById('item-card');
    const container = document.getElementById('featured-items-container');

    // Clone the empty card
    const clonedCard = itemCard.cloneNode(true);

    // Change id to ensure it is unique
    clonedCard.id = `item-card-clone-${item.id}`; // Ensure unique ID
    clonedCard.classList.add("item-card-clone");

    // Change values
    let cardName = clonedCard.querySelector("#card-name");
    let cardDescription = clonedCard.querySelector("#card-description");
    let cardItemImage = clonedCard.querySelector("#card-item-image");

    if (!cardName || !cardDescription || !cardItemImage) {
        console.error("There was an error finding card elements!");
        return; // Exit if elements are not found
    }

    // Set values to card using properties directly from the item object
    cardName.innerHTML = item.name; // Assuming 'name' is a property of the item object
    cardDescription.innerHTML = item.description; // Assuming 'description' is a property of the item object
    cardItemImage.src = `https://d3snlw7xiuobl9.cloudfront.net/${item.name}.jpg`; // Assuming 'location' is a property of the item object

    // Remove the 'd-none' class to make it visible
    clonedCard.classList.remove('d-none');
    
    // Add event listener to the cloned card
    clonedCard.addEventListener('click', handleCardClick);
    
    // Append the cloned card to the container
    container.appendChild(clonedCard); 
}

async function getFeaturedItems() {
    try {
        const response = await fetch("/product/getFeatured", {
            method: "GET",
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (response.ok) {
            featuredItems = await response.json();
            return featuredItems;
        } else {
            console.error("Failed to fetch featured products");
            return [];
        }
    } catch (error) {
        console.error("Error fetching featured products:", error);
    }
}

async function handleEditButton() {
    let editButton = document.getElementById("edit-button");
    if (isEditing == false) {
        editButton.textContent = "Done";
        isEditing = true;
        // Show empty cells
        showEditInterface();
    } else {
        editButton.textContent = "Edit";
        isEditing = false;
        // Remove empty cells if any
        deleteEmptyCards();
        
        // Update backend
        const featuredItemIds = featuredItems.map(item => item.id);
        console.log(featuredItemIds);
        await updateFeaturedItems(featuredItemIds);
        featuredItems = [];
    }
    
}

// While in edit mode show editing interface
function showEditInterface() {
    console.log(featuredItems);
    for (let i = 0; i < (maxItems - featuredItems.length); i++) {
        duplicateEmptyCard();
    }
}

// Function to duplicate the empty card
function duplicateEmptyCard() {
    // Select the empty card and the container
    const emptyCard = document.getElementById('empty-card');
    const container = document.getElementById('featured-items-container');

    // Clone the empty card
    const clonedCard = emptyCard.cloneNode(true); // true means a deep clone

    clonedCard.classList.add("empty-card-clone");
    // Remove the 'd-none' class to make it visible
    clonedCard.classList.remove('d-none');
    // Adding click event to card
    clonedCard.addEventListener('click', handleCardClick);
    // Append the cloned card to the container
    container.appendChild(clonedCard);
}

// Function to duplicate the empty card
function deleteEmptyCards() {
    // Function to remove all elements with the ID "empty-card-clone"
    const clones = document.querySelectorAll('.empty-card-clone');
    // Loop through each element and remove it
    clones.forEach(clone => {
        clone.remove();
    });
}

async function populateModal() {
    const jwtToken = localStorage.getItem('jwtToken');
    if (!jwtToken) {
        console.error("No JWT token found.");
        return;
    }
    try {
        console.log("ran");
        const response = await fetch(`/product/searchModal?name=${encodeURIComponent('')}`, {
            method: "GET",
            headers: {
                'Content-Type': 'application/json',
                "Authorization": `Bearer ${jwtToken}`
            }
        });
        
        if (response.ok) {
            let products = await response.json();
            console.log(products);
            populateModalWithFeaturedProducts(products); 
        } else {
            console.error("Failed to fetch featured products");
        }
    } catch (error) {
        console.error("Error fetching featured products:", error);
    }
}


function populateModalWithFeaturedProducts(products) {
    // Select the card template and modal container
    const templateCard = document.getElementById("featured-item-list-card");
    const container = document.getElementById("featured-item-list-container"); // Ensure you select the container correctly

    // Clear any previously populated items
    container.innerHTML = '';

    products.forEach((product, index) => {
        // Clone the card template
        const cardClone = templateCard.cloneNode(true);
        cardClone.classList.remove("d-none");  // Make the cloned card visible

        // Populate the cloned card with product details
        cardClone.querySelector(".list-card-name").textContent = product.name;
        cardClone.querySelector(".list-card-desc").textContent = product.description || "Description not available.";
        cardClone.querySelector(".list-card-img").src = `https://d3snlw7xiuobl9.cloudfront.net/${product.name.replace(/\s+/g, '')}.jpg` || "default-image-url.jpg";  // Use product image or a default image
        // Change background color to light grey if the product is featured
        const isFeatured = featuredItems.some(item => item.id === product.id);
        if (isFeatured) {
            cardClone.classList.add("text-white");
            cardClone.style.backgroundColor = '#696969';  // Set the background color to light grey
            cardClone.querySelector(".list-card-desc").textContent = "Press again to remove as a Featured Item"; 
        } else {
            cardClone.classList.remove("text-white");
            cardClone.style.backgroundColor = '';  // Reset background color if not featured
        }
        // Add a click event listener to the cloned card
        cardClone.addEventListener('click', async () => {
            console.log(`Clicked on: ${product.name}`);

            // Check if the product is already featured
            console.log(product.featureProduct);
            if (isFeatured) {
                if (featuredItems.length == 1) {
                    return alert("You must have at least 1 item!");
                }
                console.log('Removing item');
                // Find the index of the object with the specified id
                const index = featuredItems.findIndex(item => item.id === product.id);

                // Check if the object was found
                if (index !== -1) {
                    featuredItems.splice(index, 1); // Remove the object from the array
                }
                console.log(featuredItems);
                removeAllFeaturedCards();
                createTempFeaturedItems();
                showEditInterface(); 
            } else {
                if (featuredItems.length == 6) {
                    return alert("You have 6 items already!");
                }
                console.log("Adding item");
                featuredItems.push(product);
                console.log(featuredItems);
                removeAllFeaturedCards();
                createTempFeaturedItems();
                showEditInterface();
            }
            
            // Close the modal
            const modalElement = document.getElementById('item-change-modal');
            const modalInstance = bootstrap.Modal.getInstance(modalElement);
            if (modalInstance) {
                modalInstance.hide(); // Close the modal if it was open
            }
        });

        // Append the populated card to the modal container
        container.appendChild(cardClone);
    });
}

async function updateFeaturedItems(featuredItemIds) {
    const jwtToken = localStorage.getItem('jwtToken');
    if (!jwtToken) {
        console.error("No JWT token found.");
        return false;
    }

    try {
        const response = await fetch('/product/updateFeaturedStatus', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${jwtToken}`
            },
            body: JSON.stringify(featuredItemIds)
        });
        
        if (response.ok) {
            console.log("Featured status updated successfully.");
        } else {
            console.error("Failed to update featured status.");
        }
    } catch (error) {
        console.error("Error updating featured status:", error);
    }
}

// Get the search input and search button
const searchInput = document.getElementById('featured-item-search');
const searchButton = document.getElementById('featured-item-search-btn');

// Function to handle search and filter items
async function handleModalSearch() {
    const searchValue = searchInput.value.trim().toLowerCase();
    const jwtToken = localStorage.getItem('jwtToken');
    
    if (!jwtToken) {
        console.error("No JWT token found.");
        return;
    }

    try {
        // Make the API call to fetch filtered products
        const response = await fetch(`/product/searchModal?name=${searchValue}`, {
            method: "GET",
            headers: {
                'Content-Type': 'application/json',
                "Authorization": `Bearer ${jwtToken}`
            }
        });

        if (response.ok) {
            const filteredProducts = await response.json();
            console.log("search: ");
            console.log(filteredProducts);
            populateModalWithFeaturedProducts(filteredProducts); // Use the function to populate the modal
        } else {
            console.error("Failed to fetch filtered products");
        }
    } catch (error) {
        console.error("Error fetching filtered products:", error);
    }
}

// Listen for Enter key press in the search input
searchInput.addEventListener('keydown', function(event) {
    if (event.key === 'Enter') {
        event.preventDefault(); // Prevent the default form submission if inside a form
        handleModalSearch();
    }
});

// Listen for click event on the search button
searchButton.addEventListener('click', function(event) {
    event.preventDefault(); // Prevent default form behavior (if in a form)
    handleModalSearch();
});



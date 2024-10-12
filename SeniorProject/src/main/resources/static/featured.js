// This is dedicated to handling what is shown on the featured page. 
// Realistically, the button would only show up if the person logged in is an admin.
// document.addEventListener('DOMContentLoaded', addCardClickEvents);
document.addEventListener('DOMContentLoaded', function() {
    const savedFeaturedItems = localStorage.getItem('featuredItems');
    if (savedFeaturedItems) {
        featuredItems = JSON.parse(savedFeaturedItems);
    }
    createFeaturedItems();
    // Initialize the container and populate it with all items
    populateCards(Items);
});

document.getElementById("edit-button").addEventListener('click', handleEditButton);

const maxItems = 3;
let featuredItems = ["Black Dinner Plate"]; // This will be the list that contains all featured items
let isEditing = false;
let selectedSlotIndex = null;
// Get the card template and container
const cardTemplate = document.getElementById('featured-item-list-card');
const container = document.getElementById('featured-item-list-container');

// Hard-coded for now. Need to talk to team about connecting to back-end. If we choose back-end the state will also be saved to backend.
const Items = {
    'Black Dinner Plate': {
        'Name': 'Black Dinner Plate',
        'Location': 'blackplate.jpg',
        'Description': 'Gorgeous plate lined with gold.'
    },
    'Vintage Flowervase': {
        'Name': 'Vintage Flowervase',
        'Location': 'flowervase.jpg',
        'Description': 'Gorgeous vintage flowervase.'
    },
    'Dinner Table': {
        'Name': 'Dinner Table',
        //'Location': 'plastictable.JPG',     // Plastic table location doesnt seem to work right now so we will set it to something else
        'Location': 'flowervase.jpg',
        'Description': 'A sturdy dinner table with a sleek design.'
    },
    'Glass Cups': {
        'Name': 'Glass Cups',
        'Location': 'glasscups.jpg',
        'Description': 'A beautiful set of glass cups. Useful for decor or actual drinking.'
    },
    'Goblet': {
        'Name': 'Goblet',
        'Location': 'goblet.jpg',
        'Description': 'A one-of-a-kind goblet that will make your decorations complete.'
    },
    'Wine Cups': {
        'Name': 'Wine Cups',
        'Location': 'winecup.jpg',
        'Description': 'A set of wine cups that are sturdy with a gorgeous design!'
    },
};

function createFeaturedItems() {
    for (let i = 0; i < featuredItems.length; i++) {
        addItemCard(featuredItems[i]);
    }
}

function removeAllFeaturedCards() {
    // Select all cloned item cards
    const clonedItemCards = document.querySelectorAll('#item-card-clone, #empty-card-clone');
    
    // Loop through each cloned card and remove it from the DOM
    clonedItemCards.forEach(card => {
        card.remove();
    });
}

function addItemCard(item) {
    // Select the empty card and the container
    const itemCard = document.getElementById('item-card');
    const container = document.getElementById('featured-items-container');

    // Clone the empty card
    const clonedCard = itemCard.cloneNode(true);

    // Change id
    clonedCard.id = "item-card-clone";
    // Change values
    let cardName = clonedCard.querySelector("#card-name");
    let cardDescription = clonedCard.querySelector("#card-description");
    let cardItemImage = clonedCard.querySelector("#card-item-image");
    if (!cardName || !cardDescription || !cardItemImage) {
        console.error("There was an error!");
    }
    // Set values to card
    cardName.innerHTML = Items[item].Name;
    cardDescription.innerHTML = Items[item].Description;
    cardItemImage.src = 'https://d3snlw7xiuobl9.cloudfront.net/' + Items[item].Location;
    // Remove the 'd-none' class to make it visible
    clonedCard.classList.remove('d-none');
    clonedCard.addEventListener('click', handleCardClick);
    // Append the cloned card to the container
    container.appendChild(clonedCard); 
}

function handleEditButton() {
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
        localStorage.setItem('featuredItems', JSON.stringify(featuredItems));
    }
    
}

// While in edit mode show editing interface
function showEditInterface() {
    if (featuredItems.length == 3) { return; }
    for (let i = 0; i < (3 - featuredItems.length); i++) {
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

    // Change id
    clonedCard.id = "empty-card-clone";
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
    const clones = document.querySelectorAll('#empty-card-clone');
    // Loop through each element and remove it
    clones.forEach(clone => {
        clone.remove();
    });
}
// Modal
// Function to populate the container with cards based on Items
function populateCards(items) {
    // Clear the container first
    container.innerHTML = '';

    // Loop through the items and create cards
    Object.keys(items).forEach(itemKey => {
        // Clone the card template
        const clonedCard = cardTemplate.cloneNode(true);

        // Remove the 'd-none' class to make it visible
        clonedCard.classList.remove('d-none');

        // Get the elements inside the cloned card
        const cardImg = clonedCard.querySelector('.list-card-img');
        const cardTitle = clonedCard.querySelector('.list-card-name');
        const cardDesc = clonedCard.querySelector('.list-card-desc');

        // Populate the cloned card with data from the Items object
        cardImg.src = `https://d3snlw7xiuobl9.cloudfront.net/${items[itemKey].Location}`;
        cardTitle.textContent = items[itemKey].Name;
        cardDesc.textContent = items[itemKey].Description;

        // Add a click event listener to each cloned card
        clonedCard.addEventListener('click', () => {
            // Add to featuredItems
            if (selectedSlotIndex == null) { return; }
            // if im clicking on the same image
            if (featuredItems[selectedSlotIndex] == items[itemKey].Name) {
                // Removing picture
                if (featuredItems.length == 1) {
                    let featuredItemLabel = document.getElementById("itemChangeModalLabel");
                    featuredItemLabel.innerHTML = "You must have at least one featured item!";
                    featuredItemLabel.classList.add("text-danger");
                    // Set a timeout to revert the message after 3 seconds
                    setTimeout(() => {
                        // Revert back to the original text and remove the danger class
                        featuredItemLabel.innerHTML = "Change Item:";
                        featuredItemLabel.classList.remove("text-danger");
                    }, 2000); // 2000 milliseconds = 2 seconds 
                    return;
                } else {
                    featuredItems.splice(selectedSlotIndex, 1);
                }
            } else {
                featuredItems[selectedSlotIndex] = items[itemKey].Name;
            }
            removeAllFeaturedCards();
            createFeaturedItems();
            showEditInterface();
            // Get the modal instance and hide it
            const modalElement = document.getElementById('item-change-modal');
            const modalInstance = bootstrap.Modal.getInstance(modalElement); // Get the existing modal instance
            if (modalInstance) {
                modalInstance.hide(); // Close the modal if it was open
            }
        });

        // Append the cloned card to the container
        container.appendChild(clonedCard);
    });
}

// Get the search input and search button
const searchInput = document.getElementById('featured-item-search');
const searchButton = document.getElementById('featured-item-search-btn');

// Function to handle search and filter items
function handleSearch() {
    const searchValue = searchInput.value.toLowerCase();

    // Filter the items based on the search value
    const filteredItems = Object.keys(Items).filter(itemKey => {
        return Items[itemKey].Name.toLowerCase().includes(searchValue);
    }).reduce((obj, key) => {
        obj[key] = Items[key];
        return obj;
    }, {});

    // Repopulate the container with filtered items
    populateCards(filteredItems);
}

// Listen for Enter key press in the search input
searchInput.addEventListener('keydown', function(event) {
    if (event.key === 'Enter') {
        event.preventDefault(); // Prevent the default form submission if inside a form
        handleSearch();
    }
});

// Listen for click event on the search button
searchButton.addEventListener('click', function(event) {
    event.preventDefault(); // Prevent default form behavior (if in a form)
    handleSearch();
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
        // Find the index of the clicked card
        const index = cards.indexOf(clickedCard);
        selectedSlotIndex = index - 3;    // Accounting for the 2 template cards and we want 0 indexing
        // Change values in featuredItems based on index
        // Show the modal
        const modal = new bootstrap.Modal(document.getElementById('item-change-modal'));
        let searchInput = document.getElementById('featured-item-search');
        modal.show();
        searchInput.value = "";
        handleSearch();
    }
}

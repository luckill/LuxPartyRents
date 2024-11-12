/* This is the js file for the shopping cart.
 * Updated to load products from cookies and fetch details from the server.
 * 
 * - Brandon Kue (Last edit: 9/14/24)
 * - Updated with cookie-based cart loading and product fetching
 * - Modified to use the correct endpoint for fetching product details
 */

// Divs needed 
let emptyCartDiv = document.getElementById("cartEmpty");
let emptyTotalDiv = document.getElementById("totalEmpty");
let itemCartDiv = document.getElementById("cartItemCard");
let itemTotalDiv = document.getElementById("totalItemCard");

let myCart = [];
let totalCost = 0.00;
let totalAmountOfItems = 0;
let subtotal = 0.00;
let tax = 0.00;
let totalDeposit = 0.00;
const taxRate = 0.0725; // 7.25% tax rate

// Function to parse cookies
function getCookieValue(cookieName) {
    const name = cookieName + "=";
    const decodedCookie = decodeURIComponent(document.cookie);
    const cookieArray = decodedCookie.split(';');
    for(let i = 0; i < cookieArray.length; i++) {
        let cookie = cookieArray[i];
        while (cookie.charAt(0) == ' ') {
            cookie = cookie.substring(1);
        }
        if (cookie.indexOf(name) == 0) {
            return cookie.substring(name.length, cookie.length);
        }
    }
    return "";
}

// Function to fetch product details from the server
async function fetchProductById(productId) {
    const jwtToken = localStorage.getItem('jwtToken');

    const response = await fetch(`/product/getById?id=${productId}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${jwtToken}`
        }
    });

    if (!response.ok) {
        throw new Error('Failed to fetch product');
    }

    return await response.json();
}

// Function to load cart from cookies and fetch product details
async function loadCartFromCookies() {
    const cartCookie = getCookieValue('cart');
    if (cartCookie) {
        const cartItems = JSON.parse(cartCookie);
        for (const item of cartItems) {
            const productDetails = await fetchProductById(item.productId);
            if (productDetails) {
                myCart.push({
                    id: productDetails.id,
                    name: productDetails.name,
                    price: productDetails.price,
                    description: productDetails.description,
                    deposit: productDetails.price/2, 
                    amount: item.quantity
                });
            }
        }
    }
    updateCartDisplay();
}

// Function to update the cart display
function updateCartDisplay() {
    if (myCart.length == 0) {
        emptyCartDiv.classList.remove("d-none");
        emptyTotalDiv.classList.remove("d-none");
    } else {
        emptyCartDiv.classList.add("d-none");
        emptyTotalDiv.classList.add("d-none");
        calculateTotalItems();
        myCart.forEach(item => {
            duplicateCartItem(item);
            duplicateTotalItem(item);
        });
        calculateTotalCost();
    }
}

function duplicateCartItem(item) {
    // Get the div
    let originalCartItem = document.getElementById("cartItemCard");
    
    // Make a clone
    let clonedCartItem = originalCartItem.cloneNode(true);
    
    // Set the image
    let clonedItemImage = clonedCartItem.querySelector("#itemImage");
    clonedItemImage.src = "/picture/items/" + item.name.toLowerCase() + ".jpg";
    
    // Change the ID and populate item details
    clonedCartItem.id = "cloned" + item.id + "CartItem";
    
    let clonedItemName = clonedCartItem.querySelector("#itemName");
    clonedItemName.innerHTML = item.name;

    let clonedItemDescription = clonedCartItem.querySelector("#itemDescription");
    clonedItemDescription.innerHTML = item.description;

    let clonedItemAmount = clonedCartItem.querySelector("#itemAmountInput");
    clonedItemAmount.id = "cloned" + item.id + "CartAmount";
    clonedItemAmount.value = item.amount;
    // Add these attributes to the input element
    clonedItemAmount.setAttribute("type", "number");
    clonedItemAmount.setAttribute("min", "1");
    clonedItemAmount.setAttribute("step", "1");

    let clonedItemCost = clonedCartItem.querySelector("#itemCost");
    clonedItemCost.id = "cloned" + item.id + "CartCost";
    clonedItemCost.innerHTML = "$" + item.price.toFixed(2);
    
    // Enhanced input event listener with auto-update
    clonedItemAmount.addEventListener("input", function() {
        const newValue = parseInt(this.value);
        if (newValue && newValue > 0) {
            // Update corresponding elements
            let cartItemAmountElement = document.querySelector("#cloned" + item.id + "CartAmount");
            let totalItemAmountElement = document.querySelector("#cloned" + item.id + "TotalAmount");
            
            if (cartItemAmountElement) {
                cartItemAmountElement.value = newValue;
            }
            if (totalItemAmountElement) {
                totalItemAmountElement.innerHTML = "x" + newValue;
            }

            // Update cart data and cookie
            let itemWanted = myCart.find(cartItem => cartItem.id === item.id);
            if (itemWanted) {
                itemWanted.amount = newValue;
                
                // Update cookie
                const cartCookie = myCart.map(item => ({
                    productId: item.id,
                    quantity: item.amount
                }));
                setCookie('cart', JSON.stringify(cartCookie), 7);

                // Recalculate totals
                totalAmountOfItems = 0;
                totalCost = 0;
                calculateTotalItems();
                calculateTotalCost();
                
                // Update cost display in total section
                let totalItemCostElement = document.querySelector("#cloned" + item.id + "TotalCost");
                if (totalItemCostElement) {
                    totalItemCostElement.innerHTML = "$" + (item.price * newValue).toFixed(2);
                }
            }
        }
    });

    // Set up delete button
    let deleteButton = clonedCartItem.querySelector(".deleteButton");
    if (deleteButton) {
        deleteButton.addEventListener("click", function() {
            deleteItem(item.id);
        });
    }

    // Remove the update button since we don't need it anymore
    let updateButton = clonedCartItem.querySelector(".updateButton");
    if (updateButton) {
        updateButton.remove();
    }

    // Remove the d-none class and append to container
    clonedCartItem.classList.remove("d-none");
    document.getElementById("shoppingCol").appendChild(clonedCartItem);
}
// Updated delete function with proper refresh handling
function deleteItem(productId) {
    // Remove item from myCart array
    myCart = myCart.filter(item => item.id !== productId);
    
    // Update cookie
    const cartCookie = myCart.map(item => ({
        productId: item.id,
        quantity: item.amount
    }));
    setCookie('cart', JSON.stringify(cartCookie), 7);

    // Remove items from DOM
    const cartItemToRemove = document.getElementById(`cloned${productId}CartItem`);
    const totalItemToRemove = document.getElementById(`cloned${productId}TotalItem`);
    
    if (cartItemToRemove) cartItemToRemove.remove();
    if (totalItemToRemove) totalItemToRemove.remove();

    // Update displays
    calculateTotalCost();
    calculateTotalItems();
    
    // Check if cart is empty and show appropriate display
    if (myCart.length === 0) {
        emptyCartDiv.classList.remove("d-none");
        emptyTotalDiv.classList.remove("d-none");
    }
}


// Fixed update quantity function
function updateCartQuantity(productId) {
    // Get the quantity input using the correct ID format
    const quantityInput = document.getElementById(`cloned${productId}CartAmount`);
    if (!quantityInput) {
        console.error('Quantity input not found');
        return;
    }

    const newQuantity = parseInt(quantityInput.value);
    if (isNaN(newQuantity) || newQuantity < 1) {
        alert('Please enter a valid quantity');
        return;
    }

    // Update myCart array
    const itemToUpdate = myCart.find(item => item.id === productId);
    if (itemToUpdate) {
        itemToUpdate.amount = newQuantity;
        
        // Update cookie
        const cartCookie = myCart.map(item => ({
            productId: item.id,
            quantity: item.amount
        }));
        setCookie('cart', JSON.stringify(cartCookie), 7);

        // Update total item display
        const totalAmountElement = document.getElementById(`cloned${productId}TotalAmount`);
        const totalCostElement = document.getElementById(`cloned${productId}TotalCost`);
        
        if (totalAmountElement) {
            totalAmountElement.innerHTML = `x${newQuantity}`;
        }
        if (totalCostElement) {
            totalCostElement.innerHTML = `$${(itemToUpdate.price * newQuantity).toFixed(2)}`;
        }

        // Update totals
        calculateTotalCost();
        calculateTotalItems();
    }
}

function setCookie(name, value, days) {
    const d = new Date();
    d.setTime(d.getTime() + (days * 24 * 60 * 60 * 1000));
    const expires = "expires=" + d.toUTCString();
    document.cookie = name + "=" + value + ";" + expires + ";path=/";
}

function getCookie(name) {
    const nameEQ = name + "=";
    const ca = document.cookie.split(';');
    for(let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) === ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
}

function duplicateTotalItem(item) {
    // Get the div
    let originalTotalItem = document.getElementById("totalItemCard")
    // Make a clone
    let clonedTotalItem = originalTotalItem.cloneNode(true);
    // Change the name
    clonedTotalItem.id = "cloned" + item.id + "TotalItem"
    let clonedItemName = clonedTotalItem.querySelector("#itemName");
    clonedItemName.innerHTML = item.name;

    let clonedItemAmount = clonedTotalItem.querySelector("#itemAmount");
    clonedItemAmount.id = "cloned" + item.id + "TotalAmount"
    clonedItemAmount.innerHTML = "x"+item.amount;

    let clonedItemCost = clonedTotalItem.querySelector("#itemCost");
    clonedItemCost.id = "cloned" + item.id + "TotalCost"
    clonedItemCost.innerHTML = "$"+item.price * item.amount;

    // Append
    clonedTotalItem.classList.remove("d-none");
    document.getElementById("itemCardContainer").appendChild(clonedTotalItem);
}

function calculateTotalItems() {
    totalAmountOfItems = myCart.reduce((total, item) => total + parseInt(item.amount), 0);
    document.getElementById("totalItemCount").innerHTML = "Your Cart (" + totalAmountOfItems + ")";
}

 async function calculateTotalCost() {
    subtotal = myCart.reduce((total, item) => total + (item.price * item.amount), 0);
    totalDeposit = myCart.reduce((total, item) => total + (item.deposit * item.amount), 0);
    tax = subtotal * taxRate;
    totalCost = subtotal + tax + totalDeposit;

    document.getElementById("subtotalAmount").innerHTML = "$" + subtotal.toFixed(2);
    document.getElementById("taxAmount").innerHTML = "$" + tax.toFixed(2);
    document.getElementById("depositAmount").innerHTML = "$" + totalDeposit.toFixed(2);
    document.getElementById("completeTotal").innerHTML = "$" + totalCost.toFixed(2);


}

// Load cart when the page loads
window.onload = loadCartFromCookies();


function checkout() {
     const token = localStorage.getItem('jwtToken');

    if (!token) {
        console.error("User is not logged in.");
        return;
    }

    // Fetch the customer ID from the AccountController
    fetch('/account/customerId', {
        method: 'GET',
        headers: {
            'Authorization': "Bearer " + token // Include the token as a Bearer token
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Failed to retrieve customer ID: " + response.statusText);
        }
        return response.text(); // Customer ID will be returned as plain text
    })
    .then(customerId => {
        // Proceed with creating the order
        const creationDate = new Date();
        const localDateString = creationDate.toISOString().split('T')[0]; // Format as YYYY-MM-DD
        calculateTotalCost();
        const orderData = {
            creationDate: localDateString,
            rentalTime: 1, // Set rental time as needed
            paid: 0, // Set to true if the payment is made
            price: totalCost, // Use the calculated total cost
            orderProducts: myCart.map(item => ({
                product: {
                    id: item.id // Wrap the id in an object
                },
                quantity: item.amount // This remains the same
            }))
        };

        // Create the order and redirect with orderId
        console.log(customerId);
        return fetch(`/order/create?id=${customerId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': "Bearer " + token
            },
            body: JSON.stringify(orderData)
        });
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Failed to create the order: " + response.statusText);
        }
        return response.json();  // Assuming the backend returns the order object with the orderId
    })
    .then(order => {
        // Assuming the order object returned has the orderId
        const orderId = order.id; // This depends on how the order is returned
        // Redirect to the payment page with orderId as a query parameter
        console.log(order.id);
        //window.location.href = `/checkout?orderId=${order.id || order.orderId}`; CHANGE THIS BACK
    })
    .catch(error => {
        console.error("Checkout error:", error);
        if (error.response) {
            error.response.text().then(text => {
                console.error("Response body: ", text); // Log the response body for more details
            });
        }
    });
}

document.getElementById("termsCheckbox").addEventListener("change", function() {
    document.getElementById("checkoutButton").disabled = !this.checked;
});

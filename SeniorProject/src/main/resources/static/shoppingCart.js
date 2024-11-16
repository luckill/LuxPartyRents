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
let deliveryFee = 0.00;
let address = "";
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
        alert('Failed to fetch product');
    }

    return await response.json();
}

// Function to validate cart quantities against available stock
async function validateCartQuantities() {
    const cartCookie = getCookieValue('cart');
    if (!cartCookie) return;
    
    const cartItems = JSON.parse(cartCookie);
    let quantityUpdated = false;
    let itemsToRemove = [];
    
    for (const item of cartItems) {
        try {
            const productDetails = await fetchProductById(item.productId);
            if (productDetails) {
                if (item.quantity <= 0) {
                    // Mark item for removal
                    itemsToRemove.push(item.productId);
                    quantityUpdated = true;
                } else if (item.quantity > productDetails.quantity) {
                    // Update quantity to maximum available
                    item.quantity = productDetails.quantity;
                    quantityUpdated = true;
                    
                    // Update UI if the item is already displayed
                    const cartAmountInput = document.querySelector(`#cloned${item.productId}CartAmount`);
                    if (cartAmountInput) {
                        cartAmountInput.value = productDetails.quantity;
                        cartAmountInput.max = productDetails.quantity;
                        
                        // Trigger input event to update other UI elements
                        const inputEvent = new Event('input', {
                            bubbles: true,
                            cancelable: true,
                        });
                        cartAmountInput.dispatchEvent(inputEvent);
                    }
                    
                    alert(`Quantity for ${productDetails.name} adjusted to available stock (${productDetails.quantity})`, 'warning');
                }
            }
        } catch (error) {
            console.error(`Error validating quantity for product ${item.productId}:`, error);
        }
    }
    
    // Remove items with zero quantity
    if (itemsToRemove.length > 0) {
        itemsToRemove.forEach(productId => {
            // Remove from myCart array
            myCart = myCart.filter(item => item.id !== productId);
            
            // Remove from DOM
            const cartItemToRemove = document.getElementById(`cloned${productId}CartItem`);
            const totalItemToRemove = document.getElementById(`cloned${productId}TotalItem`);
            
            if (cartItemToRemove) cartItemToRemove.remove();
            if (totalItemToRemove) totalItemToRemove.remove();
            
        });
    }
    
    if (quantityUpdated || itemsToRemove.length > 0) {
        // Update cookie with remaining items and adjusted quantities
        const updatedCartItems = cartItems.filter(item => !itemsToRemove.includes(item.productId));
        setCookie('cart', JSON.stringify(updatedCartItems), 7);
        
        // Recalculate totals
        calculateTotalItems();
        calculateTotalCost();
        
        // Show empty cart if all items were removed
        if (myCart.length === 0) {
            emptyCartDiv.classList.remove("d-none");
            emptyTotalDiv.classList.remove("d-none");
        }
    }
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
    await validateCartQuantities();
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
    let originalCartItem = document.getElementById("cartItemCard")
    // Make a clone
    let clonedCartItem = originalCartItem.cloneNode(true);
    // set the image
    let clonedItemImage = clonedCartItem.querySelector("#itemImage");
    clonedItemImage.src = "https://d3snlw7xiuobl9.cloudfront.net/" + item.name.toLowerCase() + ".jpg";
    
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
    
    // Fetch and set the maximum quantity
    fetchProductById(item.id).then(productDetails => {
        clonedItemAmount.setAttribute("max", productDetails.quantity);
        if (item.amount > productDetails.quantity) {
            clonedItemAmount.value = productDetails.quantity;
            // Trigger input event to update other UI elements
            const inputEvent = new Event('input', {
                bubbles: true,
                cancelable: true,
            });
            clonedItemAmount.dispatchEvent(inputEvent);
        }
    }).catch(error => {
        console.error(`Error fetching product details for ${item.id}:`, error);
    });

    let clonedItemCost = clonedCartItem.querySelector("#itemCost");
    clonedItemCost.id = "cloned" + item.id + "CartCost";
    clonedItemCost.innerHTML = "$"+ item.price.toFixed(2);

    // Enhanced input event listener with quantity validation
    clonedItemAmount.addEventListener("input", async function() {
        let newValue = parseInt(this.value);

         

        if (newValue && newValue > 0) {
            try {
                const productDetails = await fetchProductById(item.id);
                if (newValue > productDetails.quantity) {
                    newValue = productDetails.quantity;
                    this.value = newValue;
                    alert(`Quantity adjusted to maximum available stock (${productDetails.quantity})`, 'warning');
                }

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
            } catch (error) {
                console.error(`Error validating quantity for product ${item.id}:`, error);
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

    // Append
    clonedCartItem.classList.remove("d-none");
    document.getElementById("shoppingCol").appendChild(clonedCartItem);
}

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

async function calculateTotalCost()
{
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

    // If cart is empty after filtering, show empty cart display
    if (myCart.length === 0) {
        emptyCartDiv.classList.remove("d-none");
        emptyTotalDiv.classList.remove("d-none");
        return; // Exit checkout if cart is empty
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
            alert("Failed to retrieve customer ID: " + response.statusText);
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
            //price: subtotal + tax + deliveryFee, // Use the calculated total cost
            //deposit: totalDeposit,
            address: address,
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
            alert("Failed to create the order: " + response.statusText);
        }
        return response.json();  // Assuming the backend returns the order object with the orderId
    })
    .then(order => {
        // Assuming the order object returned has the orderId
        const orderId = order.id; // This depends on how the order is returned
        // Redirect to the payment page with orderId as a query parameter
        console.log(order.id);

          // Clear the cart cookie after successful order creation
          setCookie('cart', JSON.stringify([]), 7);

          // Optionally, update the UI to reflect the empty cart
          myCart = [];
          updateCartDisplay();
          //redirect to checkout
          window.location.href = `/checkout?orderId=${order.id || order.orderId}`;
    })
    .catch(error => {
        console.error("Checkout error:", error);
        if (error.response) {
            error.response.text().then(text => {
                console.error("Response body: ", text); // Log the response body for more details
                alert("Response body: ", text);
            });
        }
    });
}

async function CalculateDeliveryFee()
{
    const street= document.getElementById("street").value;
    const addressLine2 = document.getElementById("address-line-2").value;
    const city = document.getElementById("city").value;
    const state= document.getElementById("state").value;
    const zipCode= document.getElementById("zip").value;
    const cities = ["Sacramento",  "Elk Grove", "Citrus Heights", "Folsom", "Rancho Cordova", "Rocklin", "Roseville"]
    if(containsWordIgnoreCase(cities, city))
    {
        deliveryFee = 250.00;
        document.getElementById("deliveryFeeSAmount").innerHTML = "$" + deliveryFee.toFixed(2);
    }
    else
    {
        let address = street + " " + addressLine2 + ", " + city +" " + state + " " + zipCode;
        const placeId = await getPlaceId(address);
        if (placeId)
        {
            const deliveryFee = await getDeliveryFee(placeId);
            console.log("Delivery Fee for the address:", deliveryFee);
            document.getElementById("deliveryFeeSAmount").innerHTML = "$" + deliveryFee;
        }
    }
    totalCost += deliveryFee;
    document.getElementById("completeTotal").innerHTML = "$" + totalCost.toFixed(2);
}

function containsWordIgnoreCase(list, word)
{
    return list.some(item => item.toLowerCase() === word.toLowerCase());
}

async function getPlaceId(address)
{
    try
    {
        const response = await fetch('/map/getPlaceId', {
            method: 'POST',
            headers:
            {
                'Content-Type': 'application/json',
                'Authorization': "Bearer " + localStorage.getItem('jwtToken')
            },
            body: JSON.stringify(address)
        });

        if (response.ok) {
            const placeId = await response.text(); // assuming `result` is a plain string
            console.log("Place ID:", placeId);
            return placeId;
        }
        else
        {
            console.error("Failed to fetch Place ID");
        }
    }
    catch (error)
    {
        console.error("Error:", error);
    }
}

async function getDeliveryFee(destinationPlaceId)
{
    try
    {
        const response = await fetch(`/map/calculateDeliveryFee?destinationPlaceId=${encodeURIComponent(destinationPlaceId)}`, {
            method: 'GET',
            headers:
            {
                'Authorization': "Bearer " + localStorage.getItem('jwtToken')
            }
        });

        if (response.ok)
        {
            const deliveryFee = parseFloat(await response.text());
            console.log("Delivery Fee:", deliveryFee);
            return deliveryFee;
        }
        else
        {
            console.error("Failed to calculate delivery fee");
        }
    }
    catch (error)
    {
        console.error("Error:", error);
    }
}

document.getElementById("termsCheckbox").addEventListener("change", function() {
    document.getElementById("checkoutButton").disabled = !this.checked;
});

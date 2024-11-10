// This is your test publishable API key.
const stripe = Stripe("pk_test_51Q1YUIAyWoYCatGrzgztSBsX1ApLWvatUAuCgLhXGskD9NJr5cYn6hrGjCuVyXq4RoHUx83mx9NADWoB4EGCQF4800zrR7was9");

// The items the customer wants to buy
const items = [{ id: "xl-tshirt", amount: 1000 }];

window.onload = function()
{
    const jwtToken = localStorage.getItem("jwtToken");
    const alertContainer = document.getElementById("alert-container");
    const pageContent = document.getElementById('page-content');
    const alertHeading = document.getElementById('alert-heading');
    const alertMessage = document.getElementById('alert-message');
    const alertFooter = document.getElementById('alert-footer');

    if (jwtToken)
    {
        pageContent.style.display = 'block';
        alertContainer.style.display = 'none';
    }
    else
    {
        alertContainer.style.display = 'block'; // Show alert for unauthenticated users
        pageContent.style.display = 'none';
        console.error("No JWT token found.");
        alertHeading.textContent = 'Unauthorized';
        alertMessage.textContent = 'You need to log in to access this page.';
        alertFooter.innerHTML = 'Please <a href="/login" class="alert-link">log in</a> to continue.';
    }
}
let elements;

initialize();

document
    .querySelector("#payment-form")
    .addEventListener("submit", handleSubmit);

// Fetches a payment intent and captures the client secret
async function initialize() {

    const urlParams = new URLSearchParams(window.location.search);
    const orderId = urlParams.get('orderId');

    if (!orderId) {
        console.error("Order ID is missing.");
        return;
    }

    const response = await fetch("/api/payment/secure/create-payment-intent", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: orderId,
    });
    const { clientSecret, dpmCheckerLink } = await response.json();

    const appearance = {
        theme: 'stripe',
    };
    elements = stripe.elements({ appearance, clientSecret });

    const paymentElementOptions = {
        layout: "tabs",
    };

    const paymentElement = elements.create("payment", paymentElementOptions);
    paymentElement.mount("#payment-element");

    // [DEV] For demo purposes only
    setDpmCheckerLink(dpmCheckerLink);
}

async function handleSubmit(e) {

    const urlParams = new URLSearchParams(window.location.search);
    const orderId = urlParams.get('orderId');

    if (!orderId) {
        console.error("Order ID is missing.");
        return;
    }

    e.preventDefault();
    setLoading(true);

    const response = await fetch(`/api/payment/secure/paymentSuccess`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: orderId, // Send the orderId in the request body
    });

    // Optionally handle the response from the backend
    if (!response.ok) {
        const errorMessage = await response.text();
        console.error('Payment confirmation failed:', errorMessage);
        showMessage("Payment confirmation failed, please try again.");
    } else {
        console.log("Payment confirmed successfully.");
        // You can handle further actions after this, like redirecting to the success page
    }

    const { error } = await stripe.confirmPayment({
        elements,
        confirmParams: {

            // Make sure to change this to your payment completion page
            return_url: "http://localhost:8080/paymentGood",
        },
    });

    // This point will only be reached if there is an immediate error when
    // confirming the payment. Otherwise, your customer will be redirected to
    // your `return_url`. For some payment methods like iDEAL, your customer will
    // be redirected to an intermediate site first to authorize the payment, then
    // redirected to the `return_url`.
    if (error.type === "card_error" || error.type === "validation_error") {
        showMessage(error.message);
    } else {
        showMessage("An unexpected error occurred.");
    }

    setLoading(false);
}


// ------- UI helpers -------

function showMessage(messageText) {
    const messageContainer = document.querySelector("#payment-message");

    messageContainer.classList.remove("hidden");
    messageContainer.textContent = messageText;

    setTimeout(function () {
        messageContainer.classList.add("hidden");
        messageContainer.textContent = "";
    }, 4000);
}

// Show a spinner on payment submission
function setLoading(isLoading) {
    if (isLoading) {
        // Disable the button and show a spinner
        document.querySelector("#submit").disabled = true;
        document.querySelector("#spinner").classList.remove("hidden");
        document.querySelector("#button-text").classList.add("hidden");
    } else {
        document.querySelector("#submit").disabled = false;
        document.querySelector("#spinner").classList.add("hidden");
        document.querySelector("#button-text").classList.remove("hidden");
    }
}

function setDpmCheckerLink(url) {
    document.querySelector("#dpm-integration-checker").href = url;
}

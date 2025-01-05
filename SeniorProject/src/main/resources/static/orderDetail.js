
let deposit = 0;
let deliveryAddress  = "";
window.onload = function ()
{
    const jwtToken = localStorage.getItem("jwtToken");
    const role = localStorage.getItem("Role");
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

    if (role !== "ADMIN")
    {
        document.getElementById("changeOrderStatusBtn").style.display = "none";
        document.getElementById("returnPaymentBtn").style.display = "none";
        document.getElementById("processReturnButton").style.display = "none";

        const cancelOrderButton = document.getElementById("cancelOrderButton");
        cancelOrderButton.setAttribute("data-bs-toggle", "modal");
        cancelOrderButton.setAttribute("data-bs-target", "#cancelOrderModal");
    }

}

document.addEventListener('DOMContentLoaded', async () =>
{
    const loadingIndicator = document.getElementById('loading');
    const container = document.querySelector('.main-content');
    loadingIndicator.style.display = 'block'; // Show loading indicator

    try
    {
        const urlParams = new URLSearchParams(window.location.search);
        const id = urlParams.get("id");
        const jwtToken = localStorage.getItem('jwtToken');
        if (!jwtToken)
        {
            displayError("No valid authentication information found. Please log in again.")
            loadingIndicator.style.display = 'none'; // Hide loading indicator
            return;
        }

        const response = await fetch(`/order/getById?id=${id}`,
            {
                method: 'GET',
                headers:
                    {
                        "Content-type": "application/json",
                        "Authorization": `Bearer ${jwtToken}`
                    },
            });
        if (response.ok)
        {
            const data = await response.json();
            deposit = data.deposit;
            address = data.address;
            renderOrderInfo(data)
            renderProduct(data)
            console.log(data);
        }
        else
        {
            alert(response.text());
            displayError(response.text())
        }

        const customer = await fetch(`/order/getCustomerByOrderId?orderId=${id}`,
            {
                method: 'GET',
                headers:
                    {
                        "Content-type": "application/json",
                        "Authorization": `Bearer ${jwtToken}`
                    },
            });
        if (customer.ok)
        {
            const customerData = await customer.json();
            renderCustomerInfo(customerData)
        }
        else
        {
            displayError("Fail to load customer detail.")
            alert("Fail to load customer detail.");
        }
    }
    catch (error)
    {
        console.error('Error fetching order:', error);
        alert('Error fetching order:', error);
        displayError("An unexpected error occurred, please try again.")
    }
    finally
    {
        loadingIndicator.style.display = 'none';
    }
});

function cancelOrder()
{
    const role = localStorage.getItem("Role");
    const urlParams = new URLSearchParams(window.location.search);
    const id = urlParams.get("id");
    fetch(`/order/cancel?orderId=${id}&role=${role}`,
        {
            method: 'POST',
            headers:
                {
                    "Content-type": "application/json",
                    "Authorization": `Bearer ${localStorage.getItem('jwtToken')}`
                },
        })
        .then(response =>
        {
            if (response.ok)
            {
                alert("Order cancelled successful");
                window.location.href = `/order_detail?id=${id}`;
            }
            else
            {
                alert('Failed to cancel the order');
                throw new Error('Failed to cancel the order');
            }
        })
        .catch(error =>
        {
            alert('Error cancelling the order');
        });
}

function openModal(id)
{
    document.getElementById(id).style.display = "block";
}

function returnPayment(type)
{
    const urlParams = new URLSearchParams(window.location.search);
    const id = urlParams.get("id");
    const jwtToken = localStorage.getItem('jwtToken');

    let amount;
    let url;

    if (type === "specific")
    {
        amount = parseFloat(document.getElementById("amount").value);
        if (isNaN(amount))
        {
            alert("please enter a valid amount!!!");
            return;
        }
        if (amount > deposit)
        {
            alert("the amount you are trying to refund is greater than the security deposit for this order");
            return;
        }
        console.log("User Input:", amount);
        url = `/api/payment/secure/create-refund-intent?orderId=${id}`
    }
    else if (type === "whole")
    {
        if (typeof deposit !== 'undefined')
        {
            amount = deposit;
            url = `/api/payment/secure/create-refund-intentDepositOnly?orderId=${id}`
        }
        else
        {
            displayError("Deposit amount is not available.");
            return;
        }
    }

    if (!jwtToken)
    {
        displayError("No valid authentication information found. Please log in again.")
        return;
    }
    fetch(url,
        {
            method: 'POST',
            headers:
                {
                    "Content-type": "application/json",
                    "Authorization": `Bearer ${jwtToken}`
                },
            body: JSON.stringify(parseFloat(amount))
        })
        .then(response =>
        {
            if (response.ok)
            {
                window.location.href = `/order_detail?id=${id}`;
                alert("payment refund successful!!!!");
            }
            else
            {
                throw new Error('Failed to return payment');
            }
        })
        .catch(error =>
        {
            console.error('Error cancelling the order:', error);
            displayError("There was an error returning payment. Please try again.");
        });
}

function changeOrderStatus(status)
{
    const urlParams = new URLSearchParams(window.location.search);
    const orderId = urlParams.get("id"); // Assuming the order ID is in the URL
    const jwtToken = localStorage.getItem('jwtToken');

    if (!jwtToken)
    {
        console.error("No JWT token found.");
        return;
    }

    fetch(`/order/update?orderId=${orderId}`, {
        method: 'PUT',
        headers: {
            "Content-type": "application/json",
            "Authorization": `Bearer ${jwtToken}`
        },
        body: JSON.stringify({status}) // Sending the new status in the body
    })
        .then(response =>
        {
            if (response.ok)
            {
                if (status ==="RETURNED")
                {
                    fetch(`/order/return?orderId=${orderId}`, {
                        method: 'POST',
                        headers: {
                            "Content-type": "application/json",
                            "Authorization": `Bearer ${jwtToken}`
                        },

                    })
                        .then(response =>
                        {
                            if (response.ok)
                            {
                                alert("Returned order process successfully");
                                window.location.href = `/order_detail?id=${orderId}`;
                            }
                            else
                            {
                                throw new Error('Failed to change order status');
                                alert('Failed to process return order status');
                            }
                        })
                        .catch(error =>
                        {
                            console.error('Error changing order status:', error);
                            alert('Error changing order status:', error);
                            displayError("There was an issue changing the order status. Please try again.");
                        });
                }

                window.location.href = `/order_detail?id=${orderId}`;
            }
            else
            {
                throw new Error('Failed to change order status');
                alert('Failed to change order status');
            }
        })
        .catch(error =>
        {
            console.error('Error changing order status:', error);
            alert('Error changing order status:', error);
            displayError("There was an issue changing the order status. Please try again.");
        });
}

function renderCustomerInfo(customer)
{
    document.querySelector('.customer-summary').innerHTML = `
            <p><strong>Name:</strong> ${customer.firstName} ${customer.lastName}</p>
            <p><strong>Email:</strong> ${customer.email}</p>
            <p><strong>Phone Number:</strong> ${customer.phone}</p>
            <P><Strong>Delivery Address:</Strong> ${address}</P>
        `;
}

function renderOrderInfo(order)
{
    let status = order.status;
    status = status.toLowerCase()
        .replaceAll("_", " ")
        .split(" ") // Split the text by spaces into an array of words
        .map(word => word.charAt(0).toUpperCase() + word.slice(1)) // Capitalize the first letter of each word
        .join(" ");
    document.getElementById('order-number').textContent = order.id;
    document.getElementById('order-date').textContent = order.creationDate;

    document.querySelector('.order-summary').innerHTML = `
            <ul class="list-unstyled">
                <li><span>Current status:</span> ${status}</li>
                <li><span>Payment:</span> ${order.paid ? "Paid" : "Not Paid"}</li>
                <li><span>price:</span> <span>$${order.price.toFixed(2)}</span></li>
                <li><span>Security Deposit:</span><span> $${order.deposit.toFixed(2)}</span></li>
                <li><span>Delivery Fee:</span><span> $${order.deliveryFee.toFixed(2)}</span></li>
                <li><span>Tax:</span><span> $${order.tax.toFixed(2)}</span></li>
                <li class="total d-flex justify-content-between"><strong>Total cost: </strong><strong>$${order.subtotal.toFixed(2)}</strong></li>
            </ul>
        `;
}

function renderProduct(order)
{
    const container = document.getElementById('order-container');
    container.innerHTML = ''; // Clear previous orders
    if(order && order.orderProducts && order.orderProducts.length > 0)
    {
        order.orderProducts.forEach(item =>
        {
            const productRow = document.createElement('tr');
            const itemTotalPrice = item.product.price * item.quantity; // Calculate total price for the item
            productRow.innerHTML = `
                <td>
                    <img src="https://d3snlw7xiuobl9.cloudfront.net/${item.product.name}.jpg" alt="${item.product.name}">
                    ${item.product.name}
                </td>
                <td>${item.product.type}</td>
                <td>$${item.product.price.toFixed(2)}</td>
                <td>x${item.quantity}</td>
                <td>$${itemTotalPrice.toFixed(2)}</td>
                `;
            container.appendChild(productRow);
        });
    }
    else
    {
        const noProductsRow = document.createElement('tr');
        noProductsRow.innerHTML = `
            <td colspan="5">No products found for this order.</td>`;
        container.appendChild(noProductsRow);
    }
}

document.getElementById("CancelOrderTermsCheckbox").addEventListener("change", function()
{
    document.getElementById("confirmCancelOrderButton").disabled = !this.checked;
});

const cancelOrderButton = document.getElementById("cancelOrderButton");
if (localStorage.getItem("Role") === "ADMIN")
{
    cancelOrderButton.addEventListener("click", cancelOrder);
}

let jwtToken = localStorage.getItem("jwtToken");
let customerData = null;

window.onload = async function ()
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
        if (role === "USER")
        {
            pageContent.style.display = 'block';
            alertContainer.style.display = 'none';
        }
        else
        {
            alertContainer.style.display = 'block'; // Show the alert
            pageContent.style.display = "none"
            alertHeading.textContent = 'Access Denied';
            alertMessage.innerHTML = "<strong>Error!!!</strong> - This page is for user use only, and you don't have access to it. If you want to view all the orders that are currently active, please go to <a href=\"/Orders\" class=\"alert-link\">order page</a>.";
            alertFooter.innerHTML = 'Return to the <a href="/" class="alert-link">home page</a>.';
        }
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

    const customer = await fetch(`/customer/getCustomerInfo`,
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
        customerData = await customer.json();

        await loadOrders(customerData, "current")
    }
    else
    {
        console.error("Fail to load customer detail.")
        alert("Fail to load customer detail.");
    }
}

// Function to load current orders
async function loadOrders(customerData, type)
{

    if (!jwtToken)
    {
        console.error("No JWT token found.");
        return;
    }

    const id = customerData.id;
    const endpoint = type === 'current' ? `/order/currentOrders` : `/order/pastOrders`;
    try
    {
        const response = await fetch(`${endpoint}?customerId=${id}`, {
            method: 'GET',
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${jwtToken}`
            },
        });

        if (!response.ok)
        {
            throw new Error(`Error fetching ${type} orders`);
            alert(`Error fetching ${type} orders`);
        }

        const data = await response.json();
        renderOrders(data);
    }
    catch (error)
    {
        console.error(`Error fetching ${type} orders:`, error);
        alert(`Error fetching ${type} orders:`, error);
    }
}

function renderOrders(orders)
{
    const container = document.getElementById('orders-container');
    container.innerHTML = ''; // Clear previous orders

    // Append orders to container
    orders.forEach(order =>
    {
        const orderRow = document.createElement('tr');
        orderRow.classList.add('clickable-row');
        orderRow.innerHTML = `
                <td>${order.id}</td>
                <td>${order.creationDate}</td>
                <td>${order.paid ? 'Paid' : 'Unpaid'}</td>
                <td>${order.price}</td>
                <td>${order.pickupDate}</td>
                <td>${order.returnDate}</td>
                <td>${order.status}</td>
            `;
        orderRow.addEventListener('click', () =>
        {
            window.location.href = `/order_detail?id=${order.id}`; // Change to the desired URL
        });
        container.appendChild(orderRow);
    });
}

document.getElementById('toggleCurrentOrders').addEventListener('click', () => toggleOrders(customerData, 'current'));
document.getElementById('togglePastOrders').addEventListener('click', () => toggleOrders(customerData, 'past'));

function toggleOrders(customerData, type)
{
    loadOrders(customerData, type)
    document.getElementById('toggleCurrentOrders').classList.toggle('active', type === 'current');
    document.getElementById('togglePastOrders').classList.toggle('active', type === 'past');
}


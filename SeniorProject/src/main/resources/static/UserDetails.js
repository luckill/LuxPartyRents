window.onload = function() {
    const jwtToken = localStorage.getItem('jwtToken');
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
        return;
    }

    fetch(`/customer/getCustomerInfo`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            "Authorization": `Bearer ${jwtToken}`
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
            alert('Network response was not ok');
        }
        return response.json();
    })
    .then(account => {
        // Populate the form fields with user data
        document.getElementById('userId').value = account.id || '';
        document.getElementById('fname').value = account.firstName || '';
        document.getElementById('lname').value = account.lastName || '';
        document.getElementById('phone').value = account.phone || '';
        document.getElementById('email').value = account.email || '';
    })
    .catch(error => {
        console.error('Error fetching user info:', error);
        alert('Error fetching user info:', error);
    });

}

function uploadInfo() {
    const jwtToken = localStorage.getItem('jwtToken');
    if (!jwtToken) {
        console.error("No JWT token found.");
        return;
    }
    const form = document.getElementById('user_details');

    // Collecting form data
    const customerData = {
        id: form.userId.value,
        firstName: form.fname.value,
        lastName: form.lname.value,
        phone: form.phone.value,
        email: form.email.value, // Assuming you want to send this too
    };
    // Sending data to the server
    fetch(`/customer/updateCustomer`, {
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            "Authorization": `Bearer ${jwtToken}`
        },
        body: JSON.stringify(customerData),
    })
    .then(response => {
        if (!response.ok) {
            // Log the status code and status text
            console.error('Response status:', response.status);
            console.error('Response status text:', response.statusText);
            alert('Response status text:', response.statusText);
            throw new Error('Network response was not ok');
        }
    })
    .then(data => {
        console.log('Success:', data);
        alert('Customer updated successfully!');
        window.location.href = '/UserDetails_page';
    })
    .catch((error) => {
        console.error('Error:', error);
        alert('Failed to update customer.');
    });
}

function deleteAccount() {

    const jwtToken = localStorage.getItem('jwtToken');
    if (!jwtToken) {
        console.error("No JWT token found.");
        return;
    }
    const form = document.getElementById('user_details');

    // Collecting email from the form
    const customerData = {
        email: form.email.value,
        firstName: form.fname.value,
        lastName: form.lname.value
    };

    // Sending delete request to the server
    fetch(`/customer/deleteCustomer`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            "Authorization": `Bearer ${jwtToken}`
        },
        body: JSON.stringify(customerData),
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
            alert('Network response was not ok');
        }
    })
    .then(data => {
        console.log('Success:', data);
        alert('Account deleted successfully!');
        window.location.href = '/';
        // Optionally, redirect or clear the form
        form.reset();
    })
    .catch((error) => {
        console.error('Error:', error);
        alert('Failed to delete account.');
    });
}


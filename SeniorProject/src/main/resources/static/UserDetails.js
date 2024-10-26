window.onload = function() {
    const jwtToken = localStorage.getItem('jwtToken');
    if (!jwtToken) {
        console.error("No JWT token found.");
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
    .catch(error => console.error('Error fetching user info:', error));
};

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
    fetch('/customer/updateCustomer', {
        method: 'PUT',
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
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => {
        console.log('Success:', data);
        alert('Customer updated successfully!');
    })
    .catch((error) => {
        console.error('Error:', error);
        alert('Failed to update customer.');
    });
}

// Adding event listener to the button
document.getElementById('button1').addEventListener('click', uploadInfo);

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
        }
        return response.json();
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

// Adding event listener for the delete button
document.getElementById('del_acc').addEventListener('click', deleteAccount);

function resetPassword()
{
    const jwtToken = localStorage.getItem('jwtToken');
    if (!jwtToken) {
        console.error("No JWT token found.");
        return;
    }
    const email = document.getElementById('email').value;

    fetch('/send-reset-token', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            "Authorization": `Bearer ${jwtToken}`
        },
        body: JSON.stringify({ email: email }),
    })
    .then(response => {
        if (!response.ok) throw new Error('Network response was not ok');
        alert('Password reset token sent to your email!');
        document.getElementById('reset_password_div').style.display = 'block'; // Show the reset password form
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Failed to send password reset token.');
    });
}

document.getElementById('pass_res').addEventListener('click', resetPassword);
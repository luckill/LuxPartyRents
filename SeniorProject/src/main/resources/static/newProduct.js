window.onload = function () {
    const jwtToken = localStorage.getItem("jwtToken");
    const role = localStorage.getItem("Role");
    const alertContainer = document.getElementById("alert-container");
    const pageContent = document.getElementById('page-content');
    const alertHeading = document.getElementById('alert-heading');
    const alertMessage = document.getElementById('alert-message');
    const alertFooter = document.getElementById('alert-footer');

    if (jwtToken) {
        if (role === "ADMIN") {
            pageContent.style.display = 'block';
            alertContainer.style.display = 'none';
        } else {
            alertContainer.style.display = 'block'; // Show the alert
            pageContent.style.display = "none"
            alertHeading.textContent = 'Access Denied';
            alertMessage.innerHTML = '<strong>Error!!!</strong> - This page is for admin use only, and you are not authorized to access it. If you believe you should have access, please contact your administrator.';
            alertFooter.innerHTML = 'Return to the <a href="/" class="alert-link">home page</a>.';
        }
    } else {
        alertContainer.style.display = 'block'; // Show alert for unauthenticated users
        pageContent.style.display = 'none';
        console.error("No JWT token found.");
        alertHeading.textContent = 'Unauthorized';
        alertMessage.textContent = 'You need to log in to access this page.';
        alertFooter.innerHTML = 'Please <a href="/login" class="alert-link">log in</a> to continue.';
    }
};

function addProduct() {
    const form = document.getElementById('inventoryForm'); // Use the correct form ID
    // Create a FormData object
    let formData = new FormData(form)
    // Log FormData contents for debugging
    const name = formData.get('name');
    const checkbox = document.getElementById('deliveryOnlyCheckBox');
    const isChecked = checkbox.checked; // returns true/false
    formData.append('deliveryOnly', isChecked);
    event.preventDefault();

    // Send the data using fetch
    fetch('/product/addProduct', {
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            "Authorization": `Bearer ${localStorage.getItem('jwtToken')}`
            // Do not set Content-Type
        },
        body: JSON.stringify(Object.fromEntries(formData))
    })
        .then(function (response) {
            if (response.ok) {
                window.location.href = `/uploadPicture?name=${name}`;
            }
            else
            {
                console.error('Error:', response.statusText);
                alert('Error:', response.statusText);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error:', error);
        });
}

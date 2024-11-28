window.onload = function() {
    const params = new URLSearchParams(window.location.search);
    const productId = params.get('id'); // Get the product ID from the URL
    const imageView = document.getElementById("img-view");

    const jwtToken = localStorage.getItem("jwtToken");
    const role = localStorage.getItem("Role");
    const alertContainer = document.getElementById("alert-container");
    const pageContent = document.getElementById('page-content');
    const alertHeading = document.getElementById('alert-heading');
    const alertMessage = document.getElementById('alert-message');
    const alertFooter = document.getElementById('alert-footer');

    if (jwtToken)
    {
        if (role === "ADMIN")
        {
            pageContent.style.display = 'block';
            alertContainer.style.display = 'none';
        }
        else
        {
            alertContainer.style.display = 'block'; // Show the alert
            pageContent.style.display="none"
            alertHeading.textContent = 'Access Denied';
            alertMessage.innerHTML = '<strong>Error!!!</strong> - This page is for admin use only, and you are not authorized to access it. If you believe you should have access, please contact your administrator.';
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

    if (productId)
    {
        fetch(`/product/getById?id=${productId}`, {
            method: "GET",
            headers: {
                'Content-Type': 'application/json',
                "Authorization": `Bearer ${localStorage.getItem('jwtToken')}`
            }
        })
        .then(response => response.json())
        .then(product => {
            // Populate the form fields with the product details
            document.querySelector('.id').value = product.id;
            document.querySelector('.name').value = product.name;
            document.querySelector('.quantity').value = product.quantity;
            document.querySelector('.price').value = product.price;
            document.querySelector('.type').value = product.type;
            document.querySelector('.description').value = product.description;

            let imgLink = "https://d3snlw7xiuobl9.cloudfront.net/";
            let imageName = product.name
            imgLink = imgLink.concat(imageName.replace(/ /g, "%20"), ".jpg");// Assuming product.imageUrl contains the URL of the image
            console.log(imgLink);
            imageView.style.backgroundImage = `url(${imgLink})`;

        })
        .catch(error => {
            console.log('Error fetching product details:', error)
            //alert('Error fetching product details:', error);
        });
    }
};
function updateProduct() {
    const form = document.getElementById('updateForm');
    let formData = new FormData(form);
    const checkbox = document.getElementById('deliveryOnlyCheckBox');
    const isChecked = checkbox.checked; // returns true/false
    formData.append('deliveryOnly', isChecked);
    // Prevent form submission
    event.preventDefault();

    fetch('/product/update', {
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            "Authorization": `Bearer ${localStorage.getItem('jwtToken')}`
        },
        body: JSON.stringify(Object.fromEntries(formData))
    })
        .then(function (response) {
            if (response.ok) {
                window.location.href = '/products';
            } else {
                // Handle errors
                console.error('Error:', response.statusText);
                alert('Error:', response.statusText);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error:', error);
        });
}

function deleteProduct() {
    const id = document.querySelector('.id').value; // Get the product ID from the hidden field

    fetch(`/product/delete?id=${id}`, {
        method: 'DELETE',
        headers: {
            "Authorization": `Bearer ${localStorage.getItem('jwtToken')}`
        }
    })
    .then(response => {
        if (response.ok) {
            window.location.href = '/products'; // Redirect on success
        } else {
            console.error('Error:', response.statusText);
            alert('Failed to delete product. Please try again.');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('An error occurred. Please try again later.');
    });
}
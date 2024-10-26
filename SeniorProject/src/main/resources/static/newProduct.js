window.onload = function()
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
    // Fetch and display products
    fetchProducts()

    // Add click event listeners for sorting
    document.getElementById('sort-id').addEventListener('click', () => sortTable('id'));
    document.getElementById('sort-name').addEventListener('click', () => sortTable('name'));
    document.getElementById('sort-price').addEventListener('click', () => sortTable('price'));
    document.getElementById('sort-quantity').addEventListener('click', () => sortTable('quantity'));
    document.getElementById('sort-type').addEventListener('click', () => sortTable('type'));

    // Event listeners for search functionality
    document.getElementById("searchInput").addEventListener("keydown", function (event) {
        if (event.key === "Enter") {
            searchProducts();
        }
    });
    document.getElementById("searchButton").addEventListener("click", searchProducts);
};
function addProduct() {
    const form = document.getElementById('inventoryForm'); // Use the correct form ID
    // Create a FormData object
    let formData = new FormData(form)
    // Log FormData contents for debugging
    const name = formData.get('name');
    const inputFile = document.getElementById("input-file");
    let file = inputFile.files[0];
    if (file) {
        let newName = name.concat(".jpg")
        const newFileName = newName; // Specify your new file name
        file = renameFile(file, newFileName);
        console.log(file); // Log the renamed file
    }

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
            uploadPic(file);
            window.location.href = '/products';
        } else {
            // Handle errors
            console.error('Error:', response.statusText);
        }
    })
    .catch(error => {
        console.error('Error:', error);
    });

}

function renameFile(originalFile, newName) {
    // Create a new File object with the new name
    return new File([originalFile], newName, {
        type: originalFile.type,
        lastModified: originalFile.lastModified
    });
}

function uploadPic(file)
{
    console.log(file)
    const imgData = new FormData();
    imgData.append('file', file);
    imgData.forEach((value, key) => {
        console.log(`${key}:`, value);
    });
    fetch('/uploadFile',
    {
        method: 'POST',
        headers:
        {
            "Authorization": `Bearer ${localStorage.getItem('jwtToken')}`
        },
        body: imgData
    })
    .then(response => response.text())
    .then(result => {
        if (result.startsWith('Error')) {
            responseMessage.innerHTML = `<div class="alert alert-danger">${result}</div>`;
        }
        else {
            responseMessage.innerHTML = `<div class="alert alert-success">Success: ${result}</div>`;
        }
    })
    .catch(error => {
        responseMessage.innerHTML = `<div class="alert alert-danger">Error: ${error.message}</div>`;
    });

}
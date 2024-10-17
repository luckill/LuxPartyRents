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
const dropArea = document.getElementById("drop-area");
const inputFile = document.getElementById("input-file");
const imageView = document.getElementById("img-view");
const placeholderImg = document.getElementById("placeholder-img");

inputFile.addEventListener("change", uploadImage);
dropArea.addEventListener("dragover", (event) => {
    event.preventDefault(); // Prevent default behavior
});
dropArea.addEventListener("drop", (event) => {
    event.preventDefault();
    const files = event.dataTransfer.files;
    if (files.length) {
        inputFile.files = files; // Assign dropped files to the input
        uploadImage(); // Call uploadImage directly
    }
});

function uploadImage() {
    const file = inputFile.files[0];
    const fileError = document.getElementById("fileError");
    const responseMessage = document.getElementById("responseMessage");
    if (fileError) fileError.textContent = 'none';
    if (responseMessage) responseMessage.textContent = 'none';

    const jwtToken = localStorage.getItem('jwtToken');
    if (!jwtToken) {
        console.error("No JWT token found.");
        responseMessage.textContent = "Please log in to upload an image.";
        return;
    }

    if (!file) {
        fileError.textContent = "No file selected.";
        return;
    }

    if (file.type !== "image/jpeg") {
        fileError.textContent = "Error: invalid file type. Only .jpg files are allowed.";
        return;
    }

    if (file.size > 50 * 1024 * 1024) {
        fileError.textContent = "File size exceeds 50MB.";
        return;
    }

    const imgLink = URL.createObjectURL(file);
    imageView.style.backgroundImage = `url(${imgLink})`;
    imageView.textContent = ""; // Clear placeholder text
    imageView.style.border = 0;
    placeholderImg.style.display = "none"; // Hide the placeholder image
}

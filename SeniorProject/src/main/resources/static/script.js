document.addEventListener('DOMContentLoaded', function() {
    const loginStatus = false; // Simulate login status

    // Login display logic
    const loginDisplay = document.querySelector('.login-display');
    if (loginStatus) {
        loginDisplay.innerHTML = `<span>Welcome, User!</span>
                                  <a href="#profile">Profile</a>
                                  <a href="#logout">Log Out</a>`;
    } else {
        loginDisplay.innerHTML = `<a href="#login">Log In</a>`;
    }

    // Simulate fetching gallery images
    const gallerySection = document.querySelector('.gallery');
    const images = [
        'picture/Image1.jpg',
        'picture/Image2.jpg',
        'picture/Image3.jpg',
        'picture/Image4.jpg',
        'picture/Image5.jpg',
    
        // Add as many paths as needed
    ];

    const displayImages = (imageArray) => {
        gallerySection.innerHTML = ''; // Clear current images
        imageArray.forEach(image => {
            const imgElement = document.createElement('img');
            imgElement.src = image;
            imgElement.alt = "Gallery image";
            gallerySection.appendChild(imgElement);
        });
    };

    // Initially display all images
    displayImages(images);

    // Search functionality
    const searchInput = document.querySelector('.nav-bar input[type="text"]');
    searchInput.addEventListener('keyup', (e) => {
        const filteredImages = images.filter(image => image.includes(e.target.value));
        displayImages(filteredImages);
    });
});

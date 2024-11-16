document.addEventListener('DOMContentLoaded', function() {
    const gallerySection = document.querySelector('.gallery');
    const images = [
        'https://d3snlw7xiuobl9.cloudfront.net/Image1.jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/Image2.jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/Image3.jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/Image4.jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/Image5.jpg',
    ];

    // Function to display images in the gallery
    const displayImages = (imageArray) => {
        gallerySection.innerHTML = '';
        imageArray.forEach(image => {
            const imgElement = document.createElement('img');
            imgElement.src = image;
            imgElement.alt = "Gallery image";
            imgElement.classList.add('gallery-image');
            imgElement.addEventListener('click', openModal);
            gallerySection.appendChild(imgElement);
        });
    };

    // Initially display all images
    displayImages(images);

    // Modal functionality
    function openModal(event) {
        const modal = document.getElementById("imageModal");
        const modalImg = document.getElementById("expandedImage");

        modal.style.display = "block";
        modalImg.src = event.target.src; // Set modal image source to the clicked image's source
    }

    function closeModal() {
        const modal = document.getElementById("imageModal");
        modal.style.display = "none";
    }

    // Prevent the modal from closing when clicking on the expanded image
    document.getElementById("expandedImage").addEventListener("click", function(event) {
        event.stopPropagation();
    });

    // Expose closeModal function globally for the modal's onclick event
    window.closeModal = closeModal;
});

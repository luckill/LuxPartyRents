document.addEventListener('DOMContentLoaded', function() {
    const gallerySection = document.querySelector('.gallery');
    const images = [
        'https://d3snlw7xiuobl9.cloudfront.net/gallery25(1).jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/gallery22(1).jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/gallery21(1).jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/gallery20(1).jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/gallery18(1).jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/gallery17(1).jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/gallery14(1).jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/gallery11(1).jpg',
         'https://d3snlw7xiuobl9.cloudfront.net/gallery10(1).jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/gallery7(1).jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/gallery2(1).jpg',
         'https://d3snlw7xiuobl9.cloudfront.net/gallery6(1).jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/gallery5(1).jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/gallery24(1).jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/gallery26(1).jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/Gallery1.jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/gallery13.jpg',
        'https://d3snlw7xiuobl9.cloudfront.net/gallery15.jpeg',
        'https://d3snlw7xiuobl9.cloudfront.net/gallery16.jpeg',
        'https://d3snlw7xiuobl9.cloudfront.net/gallery23(1).jpg',
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

// This mainly handles filtering
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
// This function allows fetching products w/ filtering by given column
function fetchProducts(sortBy = '', searchType = '', searchTerm = '') {
  const jwtToken = localStorage.getItem('jwtToken');
  if (!jwtToken) {
    console.error("No JWT token found.");
    return;
  }
  
  let url = '/product/getAll';
  if (sortBy) {
    url += `?sortBy=${sortBy}`; // Add sorting query parameter if present
  }
  // Add search parameters if present
  if (searchType && searchTerm) {
    url += (sortBy ? '&' : '?') + `searchType=${searchType}&searchTerm=${searchTerm}`;
  }
 
  fetch(url, {
    method: "GET",
    headers: {
      'Content-Type': 'application/json',
      "Authorization": `Bearer ${jwtToken}`
    }
  })
  .then(response => response.json())
  .then(data => {
    let rows = '';
    data.forEach(product => {
      rows += `<tr data-id="${product.id}">
                        <td>${product.id}</td>
                        <td>${product.name}</td>
                        <td>${product.price}</td>
                        <td>${product.quantity}</td>
                        <td>${product.type}</td>
                     </tr>`;
    });
    document.getElementById('tableRows').innerHTML = rows;

    // Add click event listeners to each row
    const tableRows = document.querySelectorAll('#tableRows tr');
    tableRows.forEach(row => {
        row.addEventListener('click', function() {
            console.log("Ran");
            const productId = this.dataset.id; // Get the product ID
            window.location.href = `/theProduct?id=${productId}`; // Redirect to the product page
        });
    });
  })
  .catch(error => console.log('Error fetching data:', error));
}

// Function to search products
function searchProducts() {
  const searchType = document.getElementById("col-type-dropdown").value.toLowerCase();
  const searchTerm = document.getElementById("searchInput").value;
  
  // Return base and skip the search
  if (!searchTerm) {
    fetchProducts();
    return; 
  }

  const jwtToken = localStorage.getItem('jwtToken');
  if (!jwtToken) {
      console.error("No JWT token found.");
      return;
  }

  let url = `/product/search?type=${searchType}&term=${searchTerm}`;

  fetch(url, {
      method: "GET",
      headers: {
          'Content-Type': 'application/json',
          "Authorization": `Bearer ${jwtToken}`
      }
  })
  .then(response => response.json())
  .then(data => {
      let rows = '';
      data.forEach(product => {
          rows += `<tr data-id="${product.id}">
                      <td>${product.id}</td>
                      <td>${product.name}</td>
                      <td>${product.price}</td>
                      <td>${product.quantity}</td>
                      <td>${product.type}</td>
                   </tr>`;
      });
      document.getElementById('tableRows').innerHTML = rows;
  })
  .catch(error => console.log('Error fetching search results:', error));
}

// Function to sort table based on column
function sortTable(column) {
  const searchType = document.getElementById("col-type-dropdown").value;
  const searchTerm = document.getElementById("searchInput").value;

  fetchProducts(column, searchType, searchTerm); // Fetch sorted data based on the column clicked
}

function updateProduct() {
  const form = document.getElementById('theProduct');
  let formData = new FormData(form);
  const jwtToken = localStorage.getItem('jwtToken');
  if (!jwtToken)
  {
        console.error("No JWT token found.");
        return;
  }
  fetch('/product/update',
    {
      method: 'POST',
      headers:
      {
        'Content-Type': 'application/json',
        "Authorization": `Bearer ${jwtToken}`
      },
      body: JSON.stringify(Object.fromEntries(formData))
    })
    .then(function (response) {
      if (response.status === 200) {
        window.location.href = '/products';
      } else {
        //error handling
      }
    })
    .catch(error => {
      console.error('Error:', error);
    });

}


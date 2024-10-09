// This mainly handles filtering
window.onload = function() {
    const jwtToken = localStorage.getItem('jwtToken');
    if (!jwtToken)
    {
        console.error("No JWT token found.");
        return;
    }
    
    // Fetch and display products
    fetchProducts()

    // Add click event listeners for sorting
    document.getElementById('sort-id').addEventListener('click', () => sortTable('id'));
    document.getElementById('sort-name').addEventListener('click', () => sortTable('name'));
    document.getElementById('sort-price').addEventListener('click', () => sortTable('price'));
    document.getElementById('sort-quantity').addEventListener('click', () => sortTable('quantity'));
    document.getElementById('sort-type').addEventListener('click', () => sortTable('type'));
};
// This function allows fetching products w/ filtering by given column
function fetchProducts(sortBy = '') {
  const jwtToken = localStorage.getItem('jwtToken');
  if (!jwtToken) {
    console.error("No JWT token found.");
    return;
  }
  
  let url = '/products/getAll';
  if (sortBy) {
    url += `?sortBy=${sortBy}`; // Add sorting query parameter if present
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
  })
  .catch(error => console.log('Error fetching data:', error));
}

function sortTable(column) {
  fetchProducts(column); // Fetch sorted data based on the column clicked
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


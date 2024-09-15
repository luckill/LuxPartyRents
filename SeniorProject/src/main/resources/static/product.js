window.onload = function(){
    fetch('/product/getAll')
      .then(response => response.json())
      .then(res => {
        const data = res;
        let rows = ``;
        data.forEach(product => {
          rows += `<tr data-id="${product.id}">
                    <td>${product.id}</td>
                    <td>${product.name}</td>
                    <td>${product.price}</td>
                    <td>${product.quantity}</td>
                    <td>${product.type}</td>
                    </tr>`;
        });
        console.log(rows);
        document.getElementById('tableRows').innerHTML = rows;
      })
      .catch(error => console.log('Error fetching data:',error));
};



$(document).ready(function () {
  $(document.body).on("click", "tr[data-id]", function () {
    const rowCont = $(this).text();
    var lines = rowCont.split('\n');
    const id = lines[1];
    let name = lines[2];
    name = name.trim();
    const price = lines[3];
    const quantity = lines[4];
    let type = lines[5];
    type = type.trim();
    let location = lines[6];
    location = location.trim();
    let description = lines[7];
    description = description.trim();
    let content = `
                  <div class="modal-dialog" role="document">
                    <div class="modal-content">
                      <div class="modal-header">
                        <h5 class="modal-title" id="exampleModalLabel">Product</h5>
                      </div>
                      <div class="modal-body">
                        <form id="theProduct">
                          <div class="form-group" style="display: none;">
                            <input type="number" id="name" name="id" value=${id}>
                          </div>
                          <div class="form-group">
                            <label for="name">Name:</label>
                            <input type="text" class="form-control" id="name" name="name" value="${name}">
                          </div>
                          <div class="form-group">
                            <label for="price">Price:</label>
                            <input type="number" id="price" name="price" value=${price}>
                          </div>
                          <div class="form-group">
                            <label for="quantity">Amount:</label>
                            <input type="number" id="quantity" name="quantity" value=${quantity}>
                          </div>
                          <div class="form-group">
                            <label for="type">Type:</label>
                            <input type="text" class="form-control" id="type" name="type" value="${type}">
                          </div>
                          <div class="form-group">
                            <label for="location">Locations available:</label>
                            <input type="text" class="form-control" id="location" name="location"  value="${location}">
                          </div>
                          <div class="form-group">
                            <label for="description">Description:</label>
                            <input type="text" class="form-control" id="description" name="description" value="${description}">
                          </div>
                        </form>
                      </div>
                      <div class="modal-footer">
                        <button type="submit" class="btn btn-primary" onclick="updateProduct()">Save</button>
                        <button type="button" class="btn btn-primary" onclick="deleteProduct(id)">Delete</button>
                        <button type="button" class="btn btn-secondary" onclick="closeForm()">Close</button>
                      </div>
                    </div>
                  </div>`;
    document.getElementById('myModal').innerHTML = content;
    $("#myModal").modal("show");
  });
});



function closeForm() {
  $("#myModal").modal("hide");
}
function updateProduct() {
  const form = document.getElementById('theProduct');
  let formData = new FormData(form);
  fetch('/product/update',
    {
      method: 'POST',
      headers:
      {
        'Content-Type': 'application/json',
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

function deleteProduct(id) {
  const form = document.getElementById('theProduct');
  let formData = new FormData(form);
  fetch('/product/delete',
    {
      method: 'POST',
      headers:
      {
        'Content-Type': 'application/json',
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
function addForm() {
    window.location.href = "/newProduct";
  /*let content = `
                  <div class="modal-dialog" role="document">
                    <div class="modal-content">
                      <div class="modal-header">
                        <h5 class="modal-title" id="exampleModalLabel">Product</h5>
                      </div>
                      <div class="modal-body">
                        <form id="theProduct">
                          <div class="form-group">
                            <label for="name">Name:</label>
                            <input type="text" class="form-control" id="name" name="name" value="">
                          </div>
                          <div class="form-group">
                            <label for="price">Price:</label>
                            <input type="number" id="price" name="price" value=>
                          </div>
                          <div class="form-group">
                            <label for="quantity">Amount:</label>
                            <input type="number" id="quantity" name="quantity" value=>
                          </div>
                          <div class="form-group">
                            <label for="type">Type:</label>
                            <input type="text" class="form-control" id="type" name="type" value="">
                          </div>
                          <div class="form-group">
                            <label for="location">Locations available:</label>
                            <input type="text" class="form-control" id="location" name="location"  value="">
                          </div>
                          <div class="form-group">
                            <label for="description">Description:</label>
                            <input type="text" class="form-control" id="description" name="description" value="">
                          </div>
                        </form>
                      </div>
                      <div class="modal-footer">
                        <button type="submit" class="btn btn-primary" onclick="addProduct()">Add</button>
                        <button type="button" class="btn btn-secondary" onclick="closeForm()">Close</button>
                      </div>
                    </div>
                  </div>`;
  document.getElementById('myModal').innerHTML = content;
  $("#myModal").modal("show");*/
}

function addProduct() {
  const form = document.getElementById('theProduct');
  let formData = new FormData(form);
  fetch('/product/addProduct',
    {
      method: 'POST',
      headers:
      {
        'Content-Type': 'application/json',
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
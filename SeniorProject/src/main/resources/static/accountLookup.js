function checkAccountByEmail()
{
    const formData = new FormData();
    let email = document.getElementById("email").value
    formData.append('email', email);
    fetch('/customer/findAccount', {
        method: 'POST',
        body: formData
    })
        .then(function(response)
        {
            if (response.status === 200)
            {
                document.getElementById("password").style.display = 'block';
                document.getElementById("reEnteredPassword").style.display = 'block';
                document.getElementById("submitButton").style.display = "block"
                document.getElementById("findAccountButton").style.display = 'none';
                document.getElementById('error').innerText = "";

            }
            else if(response.status === 404)
            {
                document.getElementById("password").style.display = 'none';
                document.getElementById("reEnteredPassword").style.display = 'none';
                document.getElementById("submitButton").style.display = "none"
                document.getElementById("findAccountButton").style.display = 'block';
                document.getElementById('error').innerText = "we did not find an account associate with the email in our record";
            }
            else
            {
                console.error("something went wrong")
            }
        })
        .catch(error => {
            console.error('Error:', error);
        });
}
![cheezits](https://github.com/user-attachments/assets/bdf4e75b-93ee-4707-add4-7da8e5c7d6d5)
# Project Name: LuxPartyRents Website



# Synopsis
Potapov Wedding is an equipment rental website. This project aims to help improve the efficiency and productivity of the client’s rental process while also providing an online store to help automate more of the client’s work that the client is doing manually.


# Features
* Feature 1: An admin page for the client to personalize the website and keep track of rentals and inventory.
* Feature 2: A login page and a personal profile showing customers their orders and what’s in their shopping cart.
* Feature 3: A clean, professional, and easy-to-navigate shop and home page for customers to use.


# Technologies Used
* Frontend:
	- Programming Language: JavaScript, HTML, CSS
* Framework: Bootstrap, SpringBoot
	- Interactive Development Environment (IDE): VSCode, IntelliJ
	- Software Development Kits (SDKs): Java Development Kit 17(JDK 17)
* Backend:
	- Programming Language: Java, 
	- Framework: java Spring
* Database:
	- Type: Relational
	- MySQL
* Deployment: 
	- AWS.
* Payment service:
 	- Stripe API
* location Service:
 	- Google Map API


# Installation
1. Setup your stripe account
2. Setup your google map API account
1. Clone the repository.
2. Navigate to the project directory.
3. Navigate to SeniorProject/src/main/resources
4. Add an application.properties
5. Add into application.properties for email server
	- spring.mail.host=smtp.gmail.com
	- spring.mail.port=587
	- spring.mail.username=#yo0ur email address
	- pring.mail.password=# app password for your gmail account
	- spring.mail.properties.mail.smtp.auth=true
	- spring.mail.properties.mail.smtp.starttls.enable=true
	- For how to create and app password for your gmail account, visit https://support.google.com/mail/answer/185833?hl=en
6. Add into application.properties for database
	- spring.jpa.hibernate.ddl-auto=update
	- spring.datasource.url=jdbc: #your connection string
	- spring.datasource.username=# your username
	- spring.datasource.password=#your password
	- spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
	- spring.jpa.show-sql: true(this can be set to false)
7. add into application.properties for AWS SDK
   	- aws.accessKeyId=#your AWS access key id
   	- aws.secretKey=#your AWS scret key
   	- if you help regrading how to setup AWS access key, please visit https://docs.aws.amazon.com/IAM/latest/UserGuide/id_root-user_manage_add-key.html
   	- spring.servlet.multipart.enabled=true
	- spring.servlet.multipart.max-file-size=50MB
	- spring.servlet.multipart.max-request-size=50MB
8. add stripe APi key into appicaiton.properties
	- stripe.api.key=#your private stripe api key that you get from stripe
9. add into application.properties for JWT Token
 	- security.jwt.secret-key=#your JWT token sgning key
	- security.jwt.expiration-time=the expiraiton time of your JWT Token that You want to set to(in millisecond)
10. open the project in your favorite IDE and start developing!!!


# ERD
<img width="1243" alt="ERD" src="https://github.com/user-attachments/assets/3b77e517-121c-46a3-adc7-848ba63505ad">

# Finished product
![home](https://github.com/user-attachments/assets/c5e003be-d61e-4fa5-af08-8c359758dc50)

![login](https://github.com/user-attachments/assets/7bb94c65-5f58-4099-8595-dd5f7c3f028d)

![rental](https://github.com/user-attachments/assets/e9736148-0bdd-474d-9b49-228c142a6350)

![rental2](https://github.com/user-attachments/assets/ecd4d5af-30df-409d-b817-2c93ad33069e)

# Testing
Please refer to the System Test Report for more info.
[System Test Report](https://docs.google.com/document/d/1c9pWq9uKmi4EhvE2_iEGuktmAVEnjuVMACaJC3oXmmI/edit?usp=sharing)


# Deployment
Please refer to the deployment section in our maintainance manual.
[Mantainance Manual](https://docs.google.com/document/d/1a3PEmkMXdZsAMS51TGbTEGXiO1MJa6Q2/edit?usp=sharing&ouid=114157817892068461033&rtpof=true&sd=true)


# Developer Instructions
1. Setting Up Git
	-Before using Git, you need to set up your identity:
		-git config --global user.name "Your Name"
		-git config --global user.email "youremail@example.com"
2. Initializing a New Repository
	-git init
3. Cloning an Existing Repository
	-git clone https://github.com/luckill/SeniorProject.git
4. Use an IDE to make changes to the code
5. Adding Changes
	-git add <filename>
6. Committing Changes
	-git commit
   	-Add comment of what is changeing and a brief description
7. Pushing Changes
	-git push origin <branch-name>

# Timeline
![jira_timeline](https://github.com/user-attachments/assets/fc00f330-7dcf-4027-8a7e-b1709408591b)

## What is done:
- Milestone 1: Creating a mock-up for visualization and researching our stack and database.
- Milestone 2: Code each of the pages with a consistent navbar based on our mock-up
- Milestone 3: Adding and linking with a database to test logging in and signing up.
- Milestone 4: Adding the CRUD functions for the products for the admin.
- Milestone 5: Adding functionality to each page, link the frontend web page with backend using javascript.
- Milestone 6: Add pay functionality with stripe API, admin account functionality, customer reminder function.
- Milestone 7: Create a pdf generator using java.
- Milestone 8: Create a task scheduler that can run tasks automatically at a specified time and date.
- Milestone 9: Add authentication and authorization for each page
- Milestone 10: Calculate delivery charge with google map API.

# Contributors
Team Members:
- Heamandeep Kaur (github name: heamandeepkaur)
- Anish Chouhan (github name: achouhan0214 )
- Andreas Zignago (github name: Wojtek)
- Balraj Kalathil (github name: Bkalathil)
- Kanan Shah (github name: Kinfernopixel)
- Zhijun Li (github name: luckill)
- Eric Brutskiy (github name: EricBrute)
- Brandon Kue (github name: BKueCS)

# License

THE SOFTWARE IS PROVIDED TO OUR CLIENT. THIS WEBSITE CANNOT BE USED WITHOUT THE PERMISSION OF THE PROVIDED CLIENT.

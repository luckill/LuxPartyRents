P![cheezits](https://github.com/heamandeepkaur/SeniorProject/assets/114961336/055fb94f-e32e-4e8a-bcb1-d06dfa84b93c)
roject Name: Equipment Rental Website
By Cheezits


Synopsis
Potapov Wedding is an equipment rental website. This project aims to help improve the efficiency and productivity of the client’s rental process while also providing an online store to help automate more of the client’s work that the client is doing manually.
Features
Feature 1: An admin page for the client to personalize the website and keep track of rentals and inventory.
Feature 2: A login page and a personal profile showing customers their orders and what’s in their shopping cart.
Feature 3: A clean, professional, and easy-to-navigate shop and home page for customers to use.
Technologies Used
Frontend:
Programming Language: JavaScript, HTML, CSS
Framework: Bootstrap, SpringBoot
Interactive Development Environment (IDE): VSCode, IntelliJ
Software Development Kits (SDKs): Java Development Kit 17(JDK 17)
Backend:
Programming Language: Java, 
Framework: java Spring
Database:
Type: Relational
MySQL
Deployment: 
Digital Ocean Server and Digital ocean MySQL database.

Installation
Clone the repository.
Navigate to the project directory.
Navigate to SeniorProject/src/main/resources
Add an application.properties
Add into application properties for email server
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=#yo0ur email address
pring.mail.password=# app password for your gmail account
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
For how to create and app password for your gmail account, visit https://support.google.com/mail/answer/185833?hl=en
Add into application properties for database
spring.jpa.hibernate.ddl-auto=update
spring.datasource.url=jdbc: #your connection string
spring.datasource.username=# your username
spring.datasource.password=#your password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.show-sql: true(this can be set to false) 

ERD
![ERD](https://github.com/heamandeepkaur/SeniorProject/assets/114961336/e40a376b-79ba-4caa-bf8d-1f68b42b19e2)


Prototype
![home](https://github.com/heamandeepkaur/SeniorProject/assets/114961336/f6db66d4-432b-45a2-b8a9-71bf5c116996)

![login](https://github.com/heamandeepkaur/SeniorProject/assets/114961336/f2171b84-fbcd-48ed-96e9-27e283aab438)

![rental](https://github.com/heamandeepkaur/SeniorProject/assets/114961336/3804fb0a-4bbb-468a-b618-689210659dfc)

![rental2](https://github.com/heamandeepkaur/SeniorProject/assets/114961336/b02c8eee-6c50-4e70-809c-74d2bb4a510a)



Testing
[Placeholder for testing instructions. To be filled in CSC 191.]
Deployment
[Placeholder for deployment instructions. To be filled in CSC 191.]
Developer Instructions
[Placeholder for developer instructions. To be filled in CSC 191.]


Timeline
![time1](https://github.com/heamandeepkaur/SeniorProject/assets/114961336/579878d3-b7cc-42d7-9441-cc6dc4ba223b)
	What is done:
Milestone 1: Creating a mock-up for visualization and researching our stack and database.
Milestone 2: Code each of the pages with a consistent navbar based on our mock-up
Milestone 3: Adding and linking with a database to test logging in and signing up.
Milestone 4: Adding the CRUD functions for the products for the admin.

![time2](https://github.com/heamandeepkaur/SeniorProject/assets/114961336/d7491127-1ed5-43da-a563-e9269e1fbd81)
	What is expected to be done:
Milestone 5: Adding functionality to each page, link the frontend web page with backend using javascript.
Milestone 6: Add pay functionality with stripe API, admin account functionality, customer reminder function.
Milestone 7: Create a pdf generator using java.
Milestone 8: Create a task scheduler that can run tasks automatically at a specified time and date.
Milestone 9: Add authentication and authorization for each page
Milestone 10: Calculate delivery charge with google map API.

Contributors
  Team Members:
Heamandeep Kaur
Anish Chouhan
Andreas Zignago
Balraj Kalathil
Kanan Shah
Zhijun Li
Eric Brutskiy
Brandon Kue

License

THE SOFTWARE IS PROVIDED TO OUR CLIENT. THIS WEBSITE CANNOT BE USED WITHOUT THE PERMISSION OF THE PROVIDED CLIENT.

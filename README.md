📦 Transport Management System


📌 Description

Transport Management System is a Java-based desktop application designed to manage transport and delivery operations for logistics companies.

The application allows administrators, clients, and drivers to manage deliveries, trucks, tariffs, and invoices through an intuitive JavaFX graphical interface.

This project was developed as part of a software engineering academic project.


🎯 Features

The system provides several important features:

👨‍💼 Admin
```
- Manage users
- Manage trucks
- Manage deliveries
- Manage tariffs
- Monitor system dashboard
```

🚚 Driver
```
- View assigned deliveries
- Update delivery status
- Confirm completed deliveries
```

👤 Client
```
- Request transport services
- Track deliveries
- View transport tariffs
- Access invoices
```

🏗️ Project Architecture

The project follows a layered architecture:

1. Model
Contains the main entities of the system:
```
- User
- Delivery
- Invoice
- Tariff
- Truck
```

2. DAO Layer
Responsible for database operations:
```
- Insert
- Update
- Delete
- Retrieve data
```

3. Service Layer
Contains the business logic of the application.

4.UI Layer
Built using JavaFX and FXML to provide an interactive graphical interface.


🛠️ Technologies Used
```
- Java
- JavaFX
- FXML
- CSS
- SQL Database
- DAO Pattern
- MVC Architecture
```

📂 Project Structure
```
src/
 ├── model
 ├── dao
 ├── service
 ├── ui
 ├── utils
 └── resources
```

🚀 How to Run the Project

1. Clone the repository :
```
git clone https://github.com/yourusername/transport-management-system-java.git
```
2. Open the project in IntelliJ IDEA or Eclipse
3. Configure the database connection in:
```
DBConnection.java
```
4. Run the main application class.
   

📊 Future Improvements

Possible future improvements:
```
- Web version using Spring Boot
- Mobile application
- Real-time delivery tracking
- Payment integration
- Advanced analytics dashboard
```

👨‍💻 Author
```
Developed by Mohamed Yawina a Computer Engineering student at EMSI interested in:

- Web & Mobile & Software Development
- Cloud Computing
- DevOps
```

📜 License
```
This project is for educational purposes.
```
diagramme UML

architecture

Cela rend le projet beaucoup plus professionnel.

# 🚚 Transport Management System

A **Java desktop application** designed to manage transport and delivery operations for logistics companies.
The system provides tools to manage **deliveries, trucks, users, tariffs, and invoices** through a modern **JavaFX graphical interface**.

This project was developed as part of a **Software Engineering academic project**.

---

# 📌 Description

The **Transport Management System** simplifies the management of logistics operations by centralizing all transport activities into one platform.

The application allows different types of users to interact with the system:

* Administrators manage the system
* Drivers handle deliveries
* Clients request and track transport services

The system is built using **Java and JavaFX**, following a **layered architecture** to ensure maintainability and scalability.

---

# 🎯 Features

## 👨‍💼 Admin

Administrators have full access to system management.

```
✔ Manage users
✔ Manage trucks
✔ Manage deliveries
✔ Manage tariffs
✔ Monitor system dashboard
```

---

## 🚚 Driver

Drivers can manage and update their delivery tasks.

```
✔ View assigned deliveries
✔ Update delivery status
✔ Confirm completed deliveries
```

---

## 👤 Client

Clients can interact with the transport services.

```
✔ Request transport services
✔ Track deliveries
✔ View transport tariffs
✔ Access invoices
```

---

# 🏗️ Project Architecture

The application follows a **Layered Architecture**, which separates the system into multiple logical layers.

## 1️⃣ Model Layer

Represents the core entities of the system.

```
User
Delivery
Invoice
Tariff
Truck
```

---

## 2️⃣ DAO Layer (Data Access Object)

Handles communication with the database.

```
Insert data
Update records
Delete records
Retrieve data
```

---

## 3️⃣ Service Layer

Contains the **business logic** of the application and acts as a bridge between the user interface and the database layer.

---

## 4️⃣ UI Layer

The graphical interface built with **JavaFX and FXML**, allowing users to interact with the system in an intuitive way.

---

# 🛠️ Technologies Used

```
Java
JavaFX
FXML
CSS
SQL Database
DAO Pattern
MVC Architecture
```

---

# 📂 Project Structure

```
src/
 ├── model
 ├── dao
 ├── service
 ├── ui
 ├── utils
 └── resources
```

---

# 🚀 How to Run the Project

### 1️⃣ Clone the repository

```
git clone https://github.com/yourusername/transport-management-system-java.git
```

### 2️⃣ Open the project

Open the project using:

* IntelliJ IDEA
* Eclipse

### 3️⃣ Configure the database connection

Edit the configuration file:

```
DBConnection.java
```

### 4️⃣ Run the application

Run the main launcher class of the project.

---

# 📊 Future Improvements

Possible improvements for future versions:

```
✔ Web version using Spring Boot
✔ Mobile application
✔ Real-time delivery tracking
✔ Payment integration
✔ Advanced analytics dashboard
```

---

# 👨‍💻 Author

**Mohamed Yawina**
Computer Engineering Student at **EMSI**

Interested in:

```
Web Development
Mobile Development
Software Engineering
Cloud Computing
DevOps
```

---

# 📜 License

This project is developed **for educational purposes**.

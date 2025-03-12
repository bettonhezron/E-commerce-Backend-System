# E-commerce Backend System

## Overview
An e-commerce backend system built with Spring Boot, featuring user management, product catalog, shopping cart, and order processing functionalities.

## Technology Stack

- **Java 21**
- **Spring Boot 3.4.3**
- **Spring Data JPA**
- **Spring Security**
- **PostgreSQL**
- **JWT Authentication**
- **OpenAPI (Swagger)**

## Project Structure

```
📦 ecommerce
 ┣ 📂 src/main/java/com/hezron/ecommerce
 ┃ ┣ 📂 config
 ┃ ┃ ┣ 📜 SecurityConfig.java
 ┃ ┃ ┗ 📜 SwaggerConfig.java
 ┃ ┣ 📂 controller
 ┃ ┃ ┣ 📜 ProductController.java
 ┃ ┃ ┣ 📜 OrderController.java
 ┃ ┃ ┣ 📜 CartController.java
 ┃ ┃ ┗ 📜 UserController.java
 ┃ ┣ 📂 model
 ┃ ┃ ┣ 📜 Product.java
 ┃ ┃ ┣ 📜 Category.java
 ┃ ┃ ┣ 📜 User.java
 ┃ ┃ ┣ 📜 Order.java
 ┃ ┃ ┣ 📜 OrderItem.java
 ┃ ┃ ┣ 📜 Cart.java
 ┃ ┃ ┗ 📜 CartItem.java
 ┃ ┣ 📂 repository
 ┃ ┃ ┣ 📜 ProductRepository.java
 ┃ ┃ ┣ 📜 OrderRepository.java
 ┃ ┃ ┗ 📜 UserRepository.java
 ┃ ┣ 📂 service
 ┃ ┃ ┣ 📜 ProductService.java
 ┃ ┃ ┣ 📜 OrderService.java
 ┃ ┃ ┗ 📜 UserService.java
 ┃ ┣ 📂 dto
 ┃ ┃ ┣ 📜 ProductDTO.java
 ┃ ┃ ┣ 📜 OrderDTO.java
 ┃ ┃ ┗ 📜 UserDTO.java
 ┃ ┣ 📂 exception
 ┃ ┃ ┣ 📜 ResourceNotFoundException.java
 ┃ ┃ ┗ 📜 GlobalExceptionHandler.java
 ┃ ┗ 📜 EcommerceApplication.java
 ┣ 📂 src/main/resources
 ┃ ┗ 📜 application.properties
 ┗ 📜 pom.xml

```


## Database Schema

The system uses the following core entities:

- **Users, Role & Addresses**
- Role
- **Products & Categories**
- **Orders, OrderItems & order status**
- **Cart & CartItems**

### Entity Relationships

- A **User** can have multiple **Addresses**
- A **User** can have multiple **Orders**
- A **User** has one **Cart**
- A **Product** belongs to one **Category**
- An **Order** contains multiple **OrderItems**
- A **Cart** contains multiple **CartItems**

## Getting Started

### Prerequisites

Ensure you have the following installed:

- **JDK 21**
- **Maven**
- **PostgreSQL**

### Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE ecommerce;
```

Update `.env` with your database credentials:

```env
DB_URL=jdbc:postgresql://localhost:5432/dbname
DB_USER=username
DB_PASS=password
```
```

### Running the Application

1. **Clone the repository**

   ```sh
   git clone `https://github.com/bettonhezron/E-commerce-Backend-System.git`
   
   cd ecommerce
   ```

2. **Build and run the application**

   ```sh
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8000`

## API Documentation

Once the application is running, you can access the API documentation at:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8000/swagger-ui.html)
- **OpenAPI Docs**: [http://localhost:8080/api-docs](http://localhost:8000/api-docs)

## Current Implementation Status

✅ Entity models defined  
✅ DTO impementaion
✅ Repository layer implemented  
✅ Service layer implementation  
✅Controller implementations  
✅ Security configuration  
✅ API documentation

## License

This project is licensed under the **MIT License**.

## Author

[Hezron Bett ](https://github.com/bettonhezron)

---

Happy Coding! 🚀

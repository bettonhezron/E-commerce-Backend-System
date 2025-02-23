# E-commerce Backend System

A robust e-commerce backend system built with Spring Boot, featuring user management, product catalog, shopping cart, and order processing functionalities.

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
com.hezron.ecommerce
├── entity          # Domain models
├── repository      # Data access layer
├── service         # Business logic
├── controller      # REST endpoints
├── config          # Configuration classes
├── security        # Security configurations
├── exception       # Custom exceptions
└── dto             # Data transfer objects
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

Update `application.properties` with your database credentials:

```properties
server.port = 8000
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce
spring.datasource.username=your_username
spring.datasource.password=your_password
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
✅ Repository layer implemented  
⬜️ Service layer implementation  
⬜️ Controller implementations  
⬜️ Security configuration  
⬜️ API documentation

## License

This project is licensed under the **MIT License**.

## Author

[Hezron Bett ](https://github.com/bettonhezron)

---

Happy Coding! 🚀

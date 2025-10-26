# ERP System

## Project Overview
A comprehensive Enterprise Resource Planning (ERP) system designed to streamline business operations including inventory management, order processing, customer management, invoicing, and payment processing.

## Version Information
- **Version:** 0.0.1-SNAPSHOT
- **Java Version:** 17
- **Spring Boot Version:** 3.1.0

## Technology Stack

### Backend
- **Framework:** Spring Boot 3.1.0
- **Build Tool:** Maven
- **Database:** MySQL 8.0
- **ORM:** Hibernate (Spring Data JPA)
- **Security:** Spring Security with JWT (0.11.5)
- **API Documentation:** SpringDoc OpenAPI 2.1.0
- **PDF Generation:** iText 5.5.13.3
- **Lombok:** For reducing boilerplate code

### Frontend
- **Framework:** React.js
- **UI Library:** Material-UI
- **State Management:** Redux
- **HTTP Client:** Axios

## Modules and Features

### 1. User Management
- Role-based access control (ADMIN, INVENTORY_MANAGER, SALES_MANAGER, etc.)
- JWT-based authentication and authorization
- User profile management

### 2. Product Management
- Product CRUD operations
- Category management with hierarchical structure
- Stock level tracking and alerts
- Supplier association
- Product attributes (SKU, price, dimensions, etc.)

### 3. Inventory Management
- Real-time stock tracking
- Low stock alerts based on reorder levels
- Stock adjustment functionality
- Inventory reports

### 4. Order Management
- Order creation and processing
- Order status tracking
- Order history
- Multi-item orders

### 5. Customer Management
- Customer information management
- Customer order history
- Customer communication

### 6. Invoicing
- Automated invoice generation
- Invoice tracking
- PDF invoice export
- Invoice payment status

### 7. Payment Processing
- Multiple payment methods
- Payment tracking
- Payment receipts

### 8. Reporting
- Sales reports
- Inventory reports
- Financial reports
- Custom report generation

## Setup and Installation

### Prerequisites
- JDK 17
- MySQL 8.0
- Maven
- Node.js and npm (for frontend)

### Backend Setup
1. Clone the repository
2. Configure MySQL database in `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/erp_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=root
   ```
3. Navigate to the backend directory:
   ```bash
   cd erp-system/backend
   ```
4. Build the project:
   ```bash
   mvn clean install
   ```
5. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd erp-system/frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm start
   ```

## API Documentation
- Access the Swagger UI at: `http://localhost:8080/swagger-ui.html`
- API endpoints are secured with JWT authentication
- Use the `/api/auth/login` endpoint to obtain a JWT token
- Include the token in the Authorization header for subsequent requests

### API Endpoints

#### Authentication
- **Login**: POST `/api/auth/login`
- **Register**: POST `/api/auth/register`

#### User Management
- **Get All Users**: GET `/api/users`
- **Get User by ID**: GET `/api/users/{id}`
- **Create User**: POST `/api/users`
- **Update User**: PUT `/api/users/{id}`
- **Delete User**: DELETE `/api/users/{id}`

#### Product Management
- **Get All Products**: GET `/api/products?page=0&size=10&sortBy=id&direction=asc&activeOnly=true`
- **Get Product by ID**: GET `/api/products/{id}`
- **Get Products by Category**: GET `/api/products/category/{categoryId}?includeSubcategories=true`
- **Get Products by Supplier**: GET `/api/products/supplier/{supplierId}`
- **Get Low Stock Products**: GET `/api/products/low-stock`
- **Create Product**: POST `/api/products`
- **Update Product**: PUT `/api/products/{id}`
- **Update Product Stock**: PATCH `/api/products/{id}/stock?quantity=10`
- **Delete Product**: DELETE `/api/products/{id}`

#### Category Management
- **Get All Categories**: GET `/api/categories`
- **Get Active Categories**: GET `/api/categories/active`
- **Get Category by ID**: GET `/api/categories/{id}`
- **Get Category by Name**: GET `/api/categories/name/{name}`
- **Get Subcategories**: GET `/api/categories/{id}/subcategories`
- **Create Category**: POST `/api/categories`
- **Update Category**: PUT `/api/categories/{id}`
- **Delete Category**: DELETE `/api/categories/{id}`

#### Supplier Management
- **Get All Suppliers**: GET `/api/suppliers?page=0&size=10&sortBy=id&direction=asc`
- **Get Supplier by ID**: GET `/api/suppliers/{id}`
- **Create Supplier**: POST `/api/suppliers`
- **Update Supplier**: PUT `/api/suppliers/{id}`
- **Delete Supplier**: DELETE `/api/suppliers/{id}`

#### Customer Management
- **Get All Customers**: GET `/api/customers?page=0&size=10&sortBy=id&direction=asc`
- **Get Customer by ID**: GET `/api/customers/{id}`
- **Search Customers**: GET `/api/customers/search?query=searchTerm`
- **Create Customer**: POST `/api/customers`
- **Update Customer**: PUT `/api/customers/{id}`
- **Delete Customer**: DELETE `/api/customers/{id}`

#### Order Management
- **Get All Orders**: GET `/api/orders?page=0&size=10&sortBy=id&direction=desc`
- **Get Order by ID**: GET `/api/orders/{id}`
- **Get Orders by Customer**: GET `/api/orders/customer/{customerId}`
- **Get Orders by Status**: GET `/api/orders/status/{status}`
- **Create Order**: POST `/api/orders`
- **Update Order**: PUT `/api/orders/{id}`
- **Update Order Status**: PATCH `/api/orders/{id}/status?status=COMPLETED`
- **Delete Order**: DELETE `/api/orders/{id}`

#### Invoice Management
- **Get All Invoices**: GET `/api/invoices?page=0&size=10`
- **Get Invoice by ID**: GET `/api/invoices/{id}`
- **Get Invoices by Customer**: GET `/api/invoices/customer/{customerId}`
- **Get Invoices by Status**: GET `/api/invoices/status/{status}`
- **Generate Invoice PDF**: GET `/api/invoices/{id}/pdf`
- **Create Invoice**: POST `/api/invoices`
- **Update Invoice**: PUT `/api/invoices/{id}`
- **Delete Invoice**: DELETE `/api/invoices/{id}`

#### Payment Management
- **Get All Payments**: GET `/api/payments?page=0&size=10`
- **Get Payment by ID**: GET `/api/payments/{id}`
- **Get Payments by Invoice**: GET `/api/payments/invoice/{invoiceId}`
- **Create Payment**: POST `/api/payments`
- **Update Payment**: PUT `/api/payments/{id}`
- **Delete Payment**: DELETE `/api/payments/{id}`

## Database Schema
The system uses a relational database with the following main entities:
- Users
- Products
- Categories
- Suppliers
- Customers
- Orders
- OrderItems
- Invoices
- Payments

## Security
- JWT-based authentication
- Password encryption
- Role-based access control
- Secure API endpoints

## Development Guidelines
- Follow RESTful API design principles
- Use DTOs for data transfer
- Implement proper exception handling
- Write unit and integration tests
- Follow clean code principles

## License
This project is licensed under the Apache License 2.0

## Contact
- Email: support@erpsystem.com
- Website: https://erpsystem.com

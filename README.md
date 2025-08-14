# Trio Chemist Pharmacy Management System

A comprehensive pharmacy management system built with Java Swing and MySQL.

## Features

- **Dashboard** - Overview of pharmacy statistics with visual indicators
- **Medicine Management** - Add, update, delete medicines with intuitive forms
- **Inventory Tracking** - Check stock levels and expiry dates with color-coded status
- **Invoice Generation** - Create and print invoices with professional formatting
- **Search Functionality** - Find medicines by name or formula with visual search interface
- **Status Monitoring** - Track expired and low stock items with icon indicators

## Project Structure

```
smstrProject/
├── src/smstrProject/          # Source Java files
├── bin/smstrProject/          # Compiled class files
├── lib/                       # External libraries (MySQL connector)
├── assets/                    # UI images and icons (search, cart, receipt)
├── setup_database.sql         # Database setup script
└── run.bat                    # Application launcher
```

## Prerequisites

- Java 8 or higher
- MySQL Server
- MySQL Connector/J (included in lib/)

## Database Setup

1. Start MySQL server
2. Run the database setup script:
   ```sql
   source setup_database.sql
   ```
   
Or manually create:
```sql
CREATE DATABASE test;
USE test;

CREATE TABLE testMed (
    id INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    formula VARCHAR(255) NOT NULL,
    expiry_date DATE,
    quantity INT NOT NULL,
    price DOUBLE NOT NULL
);
```

## Configuration

Update database credentials in `src/smstrProject/DatabaseOperations.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/test";
private static final String USERNAME = "root";
private static final String PASSWORD = "your_password_here";
```

## Running the Application

1. **Windows**: Double-click `run.bat`
2. **Command Line**: 
   ```bash
   java -cp "bin;lib\mysql-connector-j-8.0.33.jar" smstrProject.Main
   ```

## Technologies Used

- **Frontend**: Java Swing with custom styling
- **Backend**: Java with JDBC
- **Database**: MySQL
- **Build**: Standard Java compilation

## Contributors

- **[M Zain Ul Abideen -Me 😎]** - Backend Development, Database Design, System Integration
- **[Salar Abdullah , Rehmat-Gull]** - Frontend Design, UI/UX, Component Styling

## License

This project is for educational purposes.

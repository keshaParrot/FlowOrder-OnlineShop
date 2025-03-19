# FlowOrder

FlowOrder is the backend part of an online store designed to automate the order management process. The system provides comfortable order management for store owners (viewing purchased orders, adding new products, processing refund requests) as well as a user-friendly interface for consumers to view products, sort them by properties or categories, and make purchases.

## Technologies

- **Programming Language:** Java
- **Framework:** Spring Boot
- **Dependencies and Libraries:**
  - Firebase JWT for security (using pure FirebaseJWT)
  - JJWT for token generation on the backend side
  - JUnit and Mockito for testing
  - MapStruct for object mapping
  - Spring Boot Validation for data validation
- **Database:** MariaDB

## Installation and Running

The project uses Maven for building and running. Follow these steps:

1. **Clone the repository:**

   ```bash
   git clone https://github.com/username/FlowOrder.git
   ```
2. **Navigate to the project directory:**

  ```bash
  cd FlowOrder
  ```
3. **Run the project using Maven:**

  ```bash
  mvn spring-boot:run
  ```
_ _Note: Ensure you have JDK and Maven installed._ _

## API Documentation



## Architecture

The project is implemented as a monolith with logic separated according to the MVC pattern:

- **Model:** Contains DTO classes used for data transfer between the server and the user.
- **Controllers:** Handle user requests and call the corresponding services.
- **Services:** Contain the business logic of the application.

## Tests

The project includes tests implemented using JUnit and Mockito.  
To run the tests, use Maven:

  ```bash
  mvn test
  ```

## License
FlowOrder is distributed under the terms of a standard open-source software license.

Tasks
Payconiq Stocks Application

Swagger endpoint :: http://localhost:8080/swagger-ui.html
EndPoints :: 

    GET /api/stocks - get the all stocks details
    GET /api/stocks/{id} - get the  stock details by id
    GET /api/stocks/{id}/history - To get price history for stock by id
    PATCH /api/stocks/{id} - To update particular stock price.
    POST /api/stocks - Add new Stock to application
   

How to build and start server

Note: Java 11 is required

Command to build with maven:

    mvn clean install

Or if you want to skip tests:

    mvn clean install -DskipTests

Command to start after build (on port 8080):

    java -jar target/payconiq-assignment-1.0.jar

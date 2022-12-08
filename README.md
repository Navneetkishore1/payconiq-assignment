Tasks
Payconiq Stocks Application

Swagger endpoint :: http://localhost:8080/swagger-ui.html
EndPoints :: 

    GET /api/stocks - get the all stocks details
    GET /api/stocks/{id} - get the  stock details by id
    GET /api/stocks/{id}/history - To get price history for stock by id
    PATCH /api/stocks/{id} - To update particular stock price.
    POST /api/stocks - Add new Stock to application
    DELETE /api/stocks/{id} - Delete the stock by id
   

How to build and start server

Note: Java 11 is required

Command to build with maven:

    mvn clean install

Or if you want to skip tests:

    mvn clean install -DskipTests

Command to start after build (on port 8080):

    java -jar target/payconiq-assignment-1.0.jar
    
Or from eclipse run the Application.java file which contains main method with option java application

App can be run through docker
Please go through the Dockerfile
Download docker and run below commands to build and run app on the docker on project directory

docker build -t marco/pay:1.0-SNAPSHOT . 

docker run -d -p 8080:8080 marco/pay:1.0-SNAPSHOT                                              





   

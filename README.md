# money-transfer
This is implementation of test task for Revolut:
```
Design and implement a RESTful API (including data model and the backing implementation) for money transfers between accounts.
```

##Used frameworks

* Java 8 (programming language)
* Maven (build tool)
* Jersey (JAX-RS implementation)
* Jetty (lightweight HTTP server)
* SLF4J (logging)
* Lombok (to remove boilerplate code)
* JUnit (unit tests)

##How to build
```
mvn clean install
```

##How to run
```
java -jar target\money-transfer-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```
The application will be listening to the port 8080 (You can change the port in application.properties)

In-memory repositories start empty on application launch. All data needs to be created through REST.

## Service lifecycle
There are three services:
* `AccountService` manages accounts (create, retrieve, delete)
* `TransactionService` creates new transactions and stores them into `TransactionRepository`.
* `TransactionProcessor` is responsible for transaction processing. It creates several threads. Each of these threads reads the transactions from `TransactionRepository` and process them. 

I separated the creation and processing transactions to achieve more flexibility.

## Available endpoints

### Account
```
GET /account/{id}
```
Return an account info for specified account ID

```
GET /account/all
```
Return an account info for all available accounts

```
PUT /create
```
Create a new account

```
DELETE /account/{id}
```
Delete an account with specified account ID

### Transaction
```
PUT /transaction/deposit?accountId=XXX&amount=ZZZ
```
Add a DEPOSIT transaction with specified account ID and amount.

This means that ZZZ money will be added to the account with the identifier XXX.

```
PUT /transaction/withdraw?accountId=XXX&amount=ZZZ
```
Add a WITHDRAW transaction with specified account ID and amount.

This means that ZZZ money will be withdrawn from the account with identifier XXX.

```
PUT /transaction/transfer?sourceAccountId=XXX&destinationAccountId=XXX&amount=ZZZ
```
Add a TRANSFER transaction with specified account IDs and amount.

This means that ZZZ money will be withdrawn from the account with identifier XXX and will be added to the account with the identifier YYY.

# TODOs

I decided not to implement the functionality below to keep the API simple. However, my design makes it easy to implement this.
* Support users 
* Support currency 
* Support cancel of transaction
* Support rollback of transaction
* Security
* Store completed and invalid transactions
* Use UUIDs as identifiers

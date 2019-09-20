# REST API for money transfers between accounts.

Endpoints:

1.  `GET /users?limit={limit}&offset={offset}` - list of users
2.  `GET /users/{userId}` - user info
3.  `POST /users` - create new user
4.  `PUT /users/{userId}` - update user 
5.  `DELETE /users/{userId}` - delete user by user id

6.  `GET /accounts/{number}` - get account info by number
7.  `POST /users/{userId}/accounts` - create new account for user
8.  `GET /users/{userId}/accounts` - accounts of user
9.  `DELETE /accounts/{number}` - delete account by number
10. `POST /accounts/{number}/cash-in?amount={amount}` - put money
11. `POST /accounts/{number}/withdraw?amount={amount}` - withdraw money
12. `GET /accounts/{number}/balance` - account balance

13. `POST /transfers?from={fromAccountNumber}&to={toAccountNumber}&amount={amount}` - transfer money between accounts
14. `GET /transfers?limit={limit}&offset={offset}` - list of transfers
15. `DELETE /transfers/{transferId}` - delete transaction by id

Build
`gradle jar`

Run app
`java -jar build/libs/app.jar`

Default port 8080
In-memory db H2
# REST API for money transfers between accounts.

Endpoints:

`GET /users?limit={limit}&offset={offset}` - list of users

`GET /users/{userId}` - user info

`POST /users` - create new user
```$xslt
curl -X POST \
  http://localhost:8080/users \
  -H 'Content-Type: application/json' \
  -d '{"fullName": "John Smith"}'
```

`PUT /users/{userId}` - update user 
```$xslt
curl -X POST \
  http://localhost:8080/users/{userId} \
  -H 'Content-Type: application/json' \
  -d '{"fullName": "John Doe"}'
```

`DELETE /users/{userId}` - delete user by user id

`GET /accounts/{number}` - get account info by number

`POST /users/{userId}/accounts` - create new account for user
```$xslt
curl -X POST \
  http://localhost:8080/users/{userId}/accounts \
  -H 'Content-Type: application/json' \
  -d '{"currency": "USD"}'
```

`GET /users/{userId}/accounts` - accounts of user

`DELETE /accounts/{number}` - delete account by number

`POST /accounts/{number}/cash-in` - put money
```$xslt
curl -X POST \
  http://localhost:8080/accounts/{number}/cash-in \
  -H 'Content-Type: application/json' \
  -d '{"amount": 100}'
```

`POST /accounts/{number}/withdraw` - withdraw money
```$xslt
curl -X POST \
  http://localhost:8080/accounts/{number}/withdraw \
  -H 'Content-Type: application/json' \
  -d '{"amount": 100}'
```

`GET /accounts/{number}/balance` - account balance

`POST /transfers` - transfer money between accounts
```$xslt
curl -X POST \
  http://localhost:8080/transfers \
  -H 'Content-Type: application/json' \
  -d '{"from": "KZ00000000000000001","to": "KZ00000000000000002","amount": 20}'
```

`GET /transfers?limit={limit}&offset={offset}` - list of transfers

`DELETE /transfers/{transferId}` - delete transaction by id

Build
`gradle jar`

Run app
`java -jar build/libs/app.jar`

Default port 8080
In-memory db H2

TEST github action
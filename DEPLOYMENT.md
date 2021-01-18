# Deployment

## Server

### Compilation

Compile the server with maven:
```
mvn package
```

Then deploy the server jar (`server/target/safetynet-alerts-server.jar`) on a
Java 8+ execution environment.

### Configuration

The only supported and tested SQL database is MariaDB 10.5!

Configure the server with environment variables:
- `SERVER_PORT`: The http listening port (default: 8081)
- `CLIENT_URL`: The client url (for CORS - default: `http://localhost:3000`)
- `MYSQL_HOST`: The SQL database host (default: localhost)
- `MYSQL_PORT`: The SQL database port (default: 14373)
- `MYSQL_DATABASE`: The SQL database name (default: pmb)
- `MYSQL_USERNAME`: The SQL database username
- `MYSQL_PASSWORD`: The SQL database password
- `REDIS_HOST`:  The Redis host (default: localhost)
- `REDIS_PORT`:  The Redis port (default: 14374)
- `REDIS_DATABASE`: The Redis database number (default: 0)
- `REDIS_PASSWORD`: The Redis password

### Execution

Run the server jar:
```
java -jar <path/to/paymybuddy-server.jar>
```

Important: It is recommended to use a reverse proxy or API gateway to publicly
serve the HTTP endpoints (to setup TLS, authorization, logging, ...).

## Client

### Configuration

- `CLIENT_API_URL`: Public path to the server API (default: `http://127.0.0.1:8081/`)
- `SERVER_API_URL`: Internal path to the server API (default to the `CLIENT_API_URL` value)

### Build and start

Deploy the client on a Node.js execution environment, build and start it:
```
npm i
nm run build
npm run start
```

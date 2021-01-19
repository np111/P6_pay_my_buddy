# [![PayMyBuddy](client/src/assets/img/logo_32@2x.png?raw=true)](https://github.com/np111/P6_pay_my_buddy)
[![Build Status](https://travis-ci.com/np111/P6_pay_my_buddy.svg?branch=master)](https://travis-ci.com/np111/P6_pay_my_buddy) [![codecov.io](https://codecov.io/github/np111/P6_pay_my_buddy/coverage.svg?branch=master)](https://codecov.io/github/np111/P6_pay_my_buddy?branch=master)

PayMyBuddy is a web application to easily send money to your friends!

[![PayMyBuddy Presentation (Youtube)](.readme/video_thumbnail.png?raw=true)](https://www.youtube.com/watch?v=QXXFEUVUaGM)

## Documentation

- [REST API](https://np111.github.io/P6_pay_my_buddy/)

## Getting started

These instructions will get you a copy of the project up and running on your
local machine for development.

### Prerequisites

- Install [Maven 3.6+](https://maven.apache.org/download.cgi)
- Install [Java 8+](https://adoptopenjdk.net/?variant=openjdk8&jvmVariant=hotspot)
- Install [Docker](https://docs.docker.com/get-docker/) and [Docker Compose](https://docs.docker.com/compose/install/)

### Running App

Start the docker development environment:
```bash
./dev.sh docker up -d
./dev.sh db-seed  #(optional: feed the database with tests data)
```

Compile the server with maven and run it:
```bash
mvn package
cd server/target/
java -jar paymybuddy-server.jar
```

Then compile the client and run it:
```bash
cd client/
npm i
npm run build
npm run start
```

### Testing

To run the tests, type:
```bash
mvn verify
```

## Deployment

See [DEPLOYMENT.md](./DEPLOYMENT.md).

## Notes

This is a school project (for OpenClassrooms).

The goal is to create a java web application.

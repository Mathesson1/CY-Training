# CY Training

Welcome to the CY Training's project!

Our goal is to make an interractive platform to enable student to create their own questions and exams, and for everyone to answer them.

## Table of contents
- [CY Training](#cy-training)
  - [Table of contents](#table-of-contents)
- [Developping the project](#developping-the-project)
  - [Requirements](#requirements)
  - [Installation](#installation)
    - [PoestgreSQL setup](#poestgresql-setup)
      - [Manually](#manually)
      - [Using Docker](#using-docker)
    - [jOOQ](#jooq)
  - [Running](#running)
  - [Building and production](#building-and-production)

# Developping the project

Section for developers, explaining how to run, build or participate in the project.

The frontend is made with **VueJS** in TypeScript. The backend is made with **Javalin**, with the **Maven** build tool. And the databse uses **PostgreSQL**, with **jOOQ**.

Further documentation is available in [**Documentation**](/documentation/).

## Requirements

You will need, for the backend:
- [Java Development Kit 25](https://www.oracle.com/java/technologies/downloads/)
- [Maven](https://maven.apache.org/install.html)
- [PostgreSQL](https://www.postgresql.org/download/)

And for the frontend:
- [NodeJS](https://nodejs.org/en/download/current)

**For Debian / Ubuntu**:
```sh
apt-get update
apt-get install jdk-25 postgresql maven npm
npm install -g n
n stable
```

## Installation

> [!IMPORTANT]
> Do not forget to setup the [./backend/.env](./backend/.env) file. Duplicate the [./backend/.env.example](./backend/.env.example) and replace the value as they are described.  
> Going further into the installation, we will suppose this file exists.
>
> You might also want to change some of the values in your newly created [./backend/.env](./backend/.env), such as `DB_PASS` and `ADMIN_PASS`.

- **For Debian / Ubuntu**:
```sh
cd ./frontend
npm install
cd ../backend
./mvnw.sh install
cd ..
```

- **For Windows** (via powershell):
```sh
cd .\frontend
npm install
cd ..\backend
./mvnw.cmd install
cd ..
```

### PoestgreSQL setup

#### Manually

First, as superuser (or with a privileged enough account), create the database:

- **For Debian / Ubuntu**:
```sh
# connection to PostgreSQL as superuser
sudo -u postgres psql
```
- **For Windows**: Just open the PostgreSQL application.

Then:

```pgsql
-- Create the database
CREATE DATABASE cytraining;

-- Create the user and assign permissions
CREATE USER cytraining WITH PASSWORD 'awesomepassword';

-- Go into the database and initialise the tables
\c cytraining
\i ./database/setup.sql
-- Initialise sample data
-- \i ./database/sample.sql

-- For development purpose and peace of mind
GRANT ALL PRIVILEGES ON DATABASE cytraining TO cytraining;

-- For security when on production
GRANT CONNECT ON DATABASE cytraining TO cytraining;
GRANT USAGE ON SCHEMA public TO cytraining;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO cytraining;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO cytraining;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO cytraining;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT USAGE, SELECT ON SEQUENCES TO cytraining;
```

> [!IMPORTANT]
> Do not forget to put the password of the **cytraining** user into the [./backend/.env](./backend/.env) file under `DATABASE_PASS`.
> If you change the backend URL or port, mirror the changes in [./frontend/src/.env.development](./frontend/src/.env.development) and [./frontend/src/.env.production](./frontend/src/.env.production).

#### Using Docker

If you run PostgreSQL from Docker (with the [compose](./docker-compose.yaml) file), you only need to grant the privileges:
```sh
# connect to the PostgreSQL inside Docker
docker compose exec cytraining-db psql -U cytraining
# then grant all the privileges from above
```

To run SQL files, do this:
```sh
# run the setup.sql file
# replace this path with any path you want, relative to your current folder
docker compose exec -T database psql -U cytraining < ./database/setup.sql
```

If you want to reset your database, do this, then redo all the previous steps:
```sh
docker compose down -v
```

### jOOQ

We use jOOQ to have type safe PostgreSQL queries. To do that, jOOQ will generate code for each tables inside the *cytraining* database, into the [model](./backend/src/main/java/org/cytraining/backend/model/) folder.

It is, by default, automatically generated before each execution. You can howver change that behavior in [jooq-setup.properties](./backend/src/main/resources/jooq-setup.properties).

If you want to generate the code manually, run:
```sh
cd backend
mvn exec:java@manual-jooq-setup
```

## Running

To launch the application, you need **two** processes:
- One for the frontend.
- One for the backend.

To launch the frontend:
```sh
cd ./frontend
npm run dev
```

To launch the backend:
```sh
cd ./backend
# Debiant / Ubuntu
./mvnw.sh compile exec:java
# Windows
./mvnw.cmd compile exec:java
```

You do not need the `compile` argument if you already ran jOOQ setup earlier, either manually or automatically. But if you changed your database, jOOQ's generated code will be outdated!

## Building and production

To set this project in production, you must first build the [frontend](./frontend/):
```sh
cd frontend
npm run build
```

It will test the code, and build the project into a [dist](./frontend/dist/) folder. If any errors/warning appears, it would be preferable to fix them before building.

Also note that if you push a code that shows error on build, your merge request will be denied.

For the backend:
```sh
cd backend
mvn clean package
```

The jar file will be in the [target](./backend/target/) folder, under the name "*cytraining-VERSION.jar*".

To then run the jar file:
```sh
java -Dmode=production -jar ./target/cytraining-VERSION.jar
```

> [!IMPORTANT]
> The `-Dmode=production` tells Java that this run is in production mode, whatever the [.env](/backend/.env) "APP_MODE" is.  
> If you are not sure, you will see in the logs a message telling which mode the application is running in.

Remember to change the privileges on the PostgreSQL user when in production mode, or use another user.
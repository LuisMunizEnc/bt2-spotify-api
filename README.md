# Breakable Toy 2

## Spotify API

This project is a full-stack web application that interacts with the Spotify API, allowing users to authenticate with their Spotify accounts and access various functionalities, such as searching for music, viewing their top artists and tracks, exploring albums. The project is divided into two main components: a backend developed with Spring Boot (Java) and a frontend (React + Vite).

The main objective is to create a personalized platform for the user's Spotify experience. The backend acts as a secure proxy, managing the complex OAuth 2.0 authentication flow with Spotify, securely storing user access and refresh tokens, and making all calls to the Spotify API on behalf of the user. This frees the frontend from the burden of directly handling authentication and sensitive token management, allowing it to focus on the user interface and user experience.

## Run the App

To get this application up and running, follow these steps:

### 1. Spotify Dashboard Application Setup
Before running the project, you need to register your application on the Spotify Developer Dashboard.

- Log in or create a Spotify developer account.

- Click on "Create an app".

-Fill in your application details (name, description, etc.).

Once created, you will be provided with a Client ID and a Client Secret. Make a note of these values, you will need them for the backend configuration.

It will also ask you for the application redirect url.

In the "Redirect URIs" section, add the following URL:

- http://localhost:8080/login/oauth2/code/spotify (for the Spring Security authentication flow in the backend)

### 2. Environment Variable Configuration
To keep your credentials secure, the project is configured to read the Spotify Client ID and Client Secret from environment variables. You will also need to generate a secret key for your application's internal JWTs.

Rename the `application-example.yml` to `application.yml`

CLIENT_ID: Paste the Client ID you obtained from the Spotify Dashboard.

CLIENT_SECRET: Paste the Client Secret you obtained from the Spotify Dashboard.

JWT_SECRET: Generate a long, random string and Base64 encode it. You can use an online tool like https://www.base64encode.org/

Paste the output of this command here.

### 3. Running with Docker Compose
Once you have configured the environment variables, you can build and run the application using Docker Compose.

Ensure Docker and Docker Compose are installed on your system.

Navigate to the root of your project where the docker-compose.yml file is located (if you have one, or your backend's Dockerfile).

Build the Docker images:

```
docker-compose build
```

This will compile your Spring Boot application and create the Docker image for the backend (and any other services defined in your docker-compose.yml, such as the frontend if it's also configured for Docker).

Start the services:
```
docker-compose up
```

This will bring up your backend and frontend containers. The backend application should be accessible at http://localhost:8080.

That's it! Your Spotify application should now be running.
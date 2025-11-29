NOTES: 
- This project's services are able to be tested in a mono-repo fashion via docker-compose.

- All microservices written in java (payments-service & promotions-service) require Gradle to Build+Test+Run for class compilation before each run of the entire web application.

- Frontend requires running npm and vite to register changes made. 

- All services are fitted with GitLab CI/CD Pipeline Scripts for automated testing and deployment via GitLab. Our project does not support CI/CD via GitHub Actions.

- All secrets & passwords in the docker-compose are meant for local development, please change them in a real production environment.
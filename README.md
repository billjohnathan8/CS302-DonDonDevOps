# DonDonDevOps
A Web Application for Point-of-Sales Transactions in Retail Stores.

# Status
Current Project Status: Retired
Award: Tied for Best Project Presentation

# Tech Stack: 
Architecture: Microservices Architecture
Cloud Vendor: AWS
Frameworks: Next.js, FastAPI, Micronaut, Springboot
CI/CD (Before Retirement): GitLab (Production VCS), SonarQube
Toolchain: Docker, Docker Compose, Gradle, GitHub (Retired Documentation & VCS), Terraform, Terraformer
Languages: TypeScript, Python, Java
Third-Party Software Integration: Stripe API

# Pulling from the Repo
- Pull from each individual microservice folder directory.
- When making changes, remember to use API Versioning for Naming (e.g. XYZ/v1)
- Remember to follow the API Contract when mocking/stubbing other services under shared/api-contracts/
- Remember to edit the API Contract for your own service if making major changes to the service under shared/api-contracts/
- Read the Documentation under shared/docs/ , Ping in Telegram for Major Changes

# New Service Setup
- Add a Dockerfile and requirements.txt for each service
- Create your own CI/CD Pipeline for each service you take on Gitlab
- Add a /health endpoint for RESTful API Testing your service

# Project Sponsor & Source
Built for Singapore Management University's (SMU) CS302 - Information Technology Lifecycle Management (DevOps) Module Project Submission (under G1T8).

# Contributors: (G1 Team 8 (Don Don Donki) aka Team International Chefs)
- [ALSON SIM WEI JIE](https://github.com/Xskullibur)
- [BILL JOHNATHAN](https://github.com/billjohnathan8)
- [CODY JEOW TENG XIANG](https://github.com/Codyjtx)
- [JEREMY LIM JIN ZHAI](https://github.com/JLJZ)
- [NG GUO FENG ERIC](https://github.com/theofficialericng)
- [SIM KAH HONG](https://github.com/kahhong) 
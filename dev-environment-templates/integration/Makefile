.PHONY: help init up down restart logs clean rebuild ps health

.DEFAULT_GOAL := help

help: ## Show this help message
	@echo "Integration Environment Commands"
	@echo "================================="
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-15s\033[0m %s\n", $$1, $$2}'

init: ## Initialize integration environment
	@echo "Initializing integration environment..."
	@if [ ! -f .env ]; then cp .env.example .env && echo "✓ .env created"; fi
	@echo "Done! Update .env and docker-compose.yml with your services"

up: ## Start all services
	@echo "Starting integration environment..."
	@docker-compose up -d
	@echo "Integration environment is running!"

down: ## Stop all services
	@echo "Stopping integration environment..."
	@docker-compose down
	@echo "Environment stopped"

restart: ## Restart all services
	@echo "Restarting integration environment..."
	@docker-compose restart
	@echo "Environment restarted"

restart-service: ## Restart specific service (use SERVICE=name)
	@docker-compose restart $(SERVICE)

logs: ## View logs (use SERVICE=name for specific service)
	@docker-compose logs -f $(SERVICE)

clean: ## Stop and remove all containers, networks, and volumes
	@echo "⚠️  This will remove all containers, networks, and volumes!"
	@read -p "Are you sure? [y/N] " -n 1 -r; \
	echo; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		docker-compose down -v; \
		echo "Environment cleaned"; \
	else \
		echo "Cancelled"; \
	fi

rebuild: ## Rebuild and restart all services
	@echo "Rebuilding all services..."
	@docker-compose up -d --build
	@echo "Rebuild complete!"

rebuild-service: ## Rebuild specific service (use SERVICE=name)
	@docker-compose up -d --build $(SERVICE)

ps: ## Show running containers
	@docker-compose ps

health: ## Check all services health
	@echo "Checking services health..."
	@echo ""
	@docker-compose ps
	@echo ""
	@echo "LocalStack health:"
	@curl -s http://localhost:4566/_localstack/health | jq '.' || echo "LocalStack not responding"
	@echo ""
	@echo "PostgreSQL health:"
	@docker-compose exec postgres pg_isready -U postgres || echo "PostgreSQL not responding"
	@echo ""
	@echo "Redis health:"
	@docker-compose exec redis redis-cli ping || echo "Redis not responding"

shell: ## Access service shell (use SERVICE=name)
	@docker-compose exec $(SERVICE) bash

network-inspect: ## Inspect the microservices network
	@docker network inspect microservices-network

test-connectivity: ## Test connectivity between services
	@echo "Testing inter-service connectivity..."
	@echo "This requires services to be configured in docker-compose.yml"

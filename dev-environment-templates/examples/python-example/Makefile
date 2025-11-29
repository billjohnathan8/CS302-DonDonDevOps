.PHONY: help init up down restart logs shell clean rebuild test

.DEFAULT_GOAL := help

SERVICE_NAME ?= microservice

help: ## Show this help message
	@echo "Development Environment Commands"
	@echo "================================="
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-15s\033[0m %s\n", $$1, $$2}'

init: ## Initialize development environment
	@echo "Initializing development environment..."
	@bash ../../shared/scripts/dev-init.sh
	@echo "Done!"

up: ## Start development environment
	@echo "Starting development environment..."
	@docker-compose up -d
	@echo "Environment is running!"
	@echo "Run 'make logs' to view logs"

down: ## Stop development environment
	@echo "Stopping development environment..."
	@docker-compose down
	@echo "Environment stopped"

restart: ## Restart development environment
	@echo "Restarting development environment..."
	@docker-compose restart
	@echo "Environment restarted"

logs: ## View logs (use ARGS=service_name for specific service)
	@docker-compose logs -f $(ARGS)

shell: ## Access container shell
	@docker-compose exec dev bash

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

rebuild: ## Rebuild and restart containers
	@echo "Rebuilding containers..."
	@docker-compose up -d --build
	@echo "Rebuild complete!"

test: ## Run tests inside container
	@docker-compose exec dev pytest

install: ## Install dependencies
	@docker-compose exec dev pip install -r requirements.txt

lint: ## Run linter
	@docker-compose exec dev flake8 .

format: ## Format code with black
	@docker-compose exec dev black .

type-check: ## Run type checker
	@docker-compose exec dev mypy .

ps: ## Show running containers
	@docker-compose ps

health: ## Check service health
	@echo "Checking service health..."
	@docker-compose ps
	@echo ""
	@echo "LocalStack health:"
	@curl -s http://localhost:4566/_localstack/health | jq '.' || echo "LocalStack not responding"

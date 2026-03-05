.PHONY: help build up down start stop restart logs clean clean-all ps backend-logs frontend-logs db-logs mongo-logs shell-backend shell-frontend shell-db shell-mongo rebuild test

# Default target
.DEFAULT_GOAL := help

# Project name
PROJECT_NAME := dev-portal

# Docker compose command
DOCKER_COMPOSE := docker compose -f docker-compose.yml -p $(PROJECT_NAME)

help: ## Show this help message
	@echo "Dev Portal - Available Make Commands:"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## Build all Docker images
	$(DOCKER_COMPOSE) build

up: ## Start all services in detached mode
	$(DOCKER_COMPOSE) up -d

down: ## Stop and remove all containers
	$(DOCKER_COMPOSE) down

start: ## Start existing containers
	$(DOCKER_COMPOSE) start

stop: ## Stop running containers without removing them
	$(DOCKER_COMPOSE) stop

restart: ## Restart all services
	$(DOCKER_COMPOSE) restart

logs: ## Show logs from all services
	$(DOCKER_COMPOSE) logs -f

clean: ## Stop and remove containers, networks
	$(DOCKER_COMPOSE) down -v

clean-all: ## Remove containers, networks, volumes, and images
	$(DOCKER_COMPOSE) down -v --rmi all

ps: ## List running containers
	$(DOCKER_COMPOSE) ps

# Service-specific logs
backend-logs: ## Show backend service logs
	$(DOCKER_COMPOSE) logs -f backend

frontend-logs: ## Show frontend service logs
	$(DOCKER_COMPOSE) logs -f frontend

db-logs: ## Show PostgreSQL logs
	$(DOCKER_COMPOSE) logs -f postgres

mongo-logs: ## Show MongoDB logs
	$(DOCKER_COMPOSE) logs -f mongodb

# Shell access
shell-backend: ## Access backend container shell
	$(DOCKER_COMPOSE) exec backend sh

shell-frontend: ## Access frontend container shell
	$(DOCKER_COMPOSE) exec frontend sh

shell-db: ## Access PostgreSQL container shell
	$(DOCKER_COMPOSE) exec postgres psql -U postgres -d devportal_new

shell-mongo: ## Access MongoDB container shell
	$(DOCKER_COMPOSE) exec mongodb mongosh devportal_files

# Rebuild specific services
rebuild-backend: ## Rebuild and restart backend service
	$(DOCKER_COMPOSE) up -d --build --no-deps backend

rebuild-frontend: ## Rebuild and restart frontend service
	$(DOCKER_COMPOSE) up -d --build --no-deps frontend

rebuild: ## Rebuild and restart all services
	$(DOCKER_COMPOSE) up -d --build

# Development helpers
dev: build up ## Build and start all services (full setup)

dev-fresh: clean-all build up ## Clean everything and start fresh

health: ## Check health status of all services
	@echo "=== Service Health Status ==="
	@$(DOCKER_COMPOSE) ps

# Database operations
db-backup: ## Backup PostgreSQL database
	@mkdir -p ./backups
	$(DOCKER_COMPOSE) exec -T postgres pg_dump -U postgres devportal_new > ./backups/db_backup_$$(date +%Y%m%d_%H%M%S).sql
	@echo "Database backup created in ./backups/"

db-restore: ## Restore PostgreSQL database (usage: make db-restore FILE=./backups/backup.sql)
	@if [ -z "$(FILE)" ]; then echo "Error: Please specify FILE=path/to/backup.sql"; exit 1; fi
	$(DOCKER_COMPOSE) exec -T postgres psql -U postgres devportal_new < $(FILE)
	@echo "Database restored from $(FILE)"

# Testing
test: ## Run tests (placeholder - add your test commands)
	@echo "Running tests..."
	@echo "Add your test commands here"

# Monitoring
stats: ## Show container resource usage
	docker stats --no-stream $$($(DOCKER_COMPOSE) ps -q)

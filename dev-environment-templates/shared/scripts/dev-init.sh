#!/bin/bash

# Development Environment Initialization Script
# This script helps initialize a development environment from a template

set -e

echo "========================================="
echo "Development Environment Setup"
echo "========================================="
echo ""

# Check if .env exists
if [ ! -f .env ]; then
    if [ -f .env.example ]; then
        echo "Creating .env file from .env.example..."
        cp .env.example .env
        echo "✓ .env file created"
        echo "⚠️  Please review and update .env with your configuration"
    else
        echo "⚠️  No .env.example found. Please create a .env file manually."
    fi
else
    echo "✓ .env file already exists"
fi

echo ""
echo "Environment initialized successfully!"
echo ""
echo "Next steps:"
echo "  1. Review and update .env file"
echo "  2. Run 'make up' or 'docker-compose up' to start services"
echo "  3. Check 'make help' for available commands"
echo ""

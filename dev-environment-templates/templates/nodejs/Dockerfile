# Node.js Development Environment
# This Dockerfile creates a complete development environment for Node.js/TypeScript microservices

FROM node:20-bullseye

# Set environment variables
ENV DEBIAN_FRONTEND=noninteractive \
    NODE_ENV=development \
    AWS_ACCESS_KEY_ID=test \
    AWS_SECRET_ACCESS_KEY=test \
    AWS_DEFAULT_REGION=us-east-1 \
    AWS_ENDPOINT_URL=http://localstack:4566

# Install system dependencies and development tools
RUN apt-get update && apt-get install -y \
    git \
    curl \
    wget \
    vim \
    nano \
    less \
    jq \
    zip \
    unzip \
    make \
    python3 \
    python3-pip \
    postgresql-client \
    redis-tools \
    && rm -rf /var/lib/apt/lists/*

# Install AWS CLI v2
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" \
    && unzip awscliv2.zip \
    && ./aws/install \
    && rm -rf aws awscliv2.zip

# Install LocalStack AWS CLI wrapper
RUN pip3 install awscli-local

# Install global Node.js development tools
RUN npm install -g \
    typescript \
    ts-node \
    nodemon \
    eslint \
    prettier \
    npm-check-updates \
    yarn \
    pnpm

# Create working directory
WORKDIR /workspace

# Configure git to trust the workspace directory (for Dev Containers)
RUN git config --global --add safe.directory /workspace

# Set up non-root user for better security
ARG USERNAME=node
ARG USER_UID=1000
ARG USER_GID=$USER_UID

# Ensure the node user has the correct UID/GID
RUN groupmod --gid $USER_GID node \
    && usermod --uid $USER_UID --gid $USER_GID node \
    && chown -R node:node /home/node

# Switch to non-root user
USER node

# Default command
CMD ["bash"]

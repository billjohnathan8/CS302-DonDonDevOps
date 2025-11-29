# Python Development Environment
# This Dockerfile creates a complete development environment for Python microservices

FROM python:3.11-bullseye

# Set environment variables
ENV DEBIAN_FRONTEND=noninteractive \
    PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    PIP_NO_CACHE_DIR=1 \
    PIP_DISABLE_PIP_VERSION_CHECK=1 \
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
    build-essential \
    postgresql-client \
    redis-tools \
    && rm -rf /var/lib/apt/lists/*

# Install AWS CLI v2
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" \
    && unzip awscliv2.zip \
    && ./aws/install \
    && rm -rf aws awscliv2.zip

# Install LocalStack AWS CLI wrapper and common Python tools
RUN pip install --upgrade pip setuptools wheel && \
    pip install \
    awscli-local \
    poetry \
    pipenv \
    black \
    flake8 \
    pylint \
    mypy \
    pytest \
    pytest-cov \
    ipython \
    ipdb

# Create working directory
WORKDIR /workspace

# Configure git to trust the workspace directory (for Dev Containers)
RUN git config --global --add safe.directory /workspace

# Set up non-root user for better security
ARG USERNAME=python
ARG USER_UID=1000
ARG USER_GID=$USER_UID

RUN groupadd --gid $USER_GID $USERNAME \
    && useradd --uid $USER_UID --gid $USER_GID -m $USERNAME \
    && chown -R $USERNAME:$USERNAME /workspace

# Switch to non-root user
USER $USERNAME

# Set PATH for user-installed packages
ENV PATH="/home/$USERNAME/.local/bin:$PATH"

# Default command
CMD ["bash"]

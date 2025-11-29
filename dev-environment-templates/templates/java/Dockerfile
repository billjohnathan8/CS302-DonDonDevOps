# Java/Kotlin Development Environment
# This Dockerfile creates a complete development environment for Java/Kotlin microservices

FROM eclipse-temurin:17-jdk-jammy

# Set environment variables
ENV DEBIAN_FRONTEND=noninteractive \
    AWS_ACCESS_KEY_ID=test \
    AWS_SECRET_ACCESS_KEY=test \
    AWS_DEFAULT_REGION=us-east-1 \
    AWS_ENDPOINT_URL=http://localstack:4566 \
    GRADLE_USER_HOME=/workspace/.gradle \
    MAVEN_CONFIG=/workspace/.m2

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
    postgresql-client \
    redis-tools \
    python3 \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

# Install AWS CLI v2
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" \
    && unzip awscliv2.zip \
    && ./aws/install \
    && rm -rf aws awscliv2.zip

# Install LocalStack AWS CLI wrapper
RUN pip3 install awscli-local

# Install Gradle
ARG GRADLE_VERSION=8.5
RUN wget https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -P /tmp \
    && unzip -d /opt/gradle /tmp/gradle-${GRADLE_VERSION}-bin.zip \
    && rm /tmp/gradle-${GRADLE_VERSION}-bin.zip
ENV GRADLE_HOME=/opt/gradle/gradle-${GRADLE_VERSION}
ENV PATH=$PATH:$GRADLE_HOME/bin

# Install Maven
ARG MAVEN_VERSION=3.9.6
RUN wget https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz -P /tmp \
    && tar xf /tmp/apache-maven-${MAVEN_VERSION}-bin.tar.gz -C /opt \
    && rm /tmp/apache-maven-${MAVEN_VERSION}-bin.tar.gz
ENV MAVEN_HOME=/opt/apache-maven-${MAVEN_VERSION}
ENV PATH=$PATH:$MAVEN_HOME/bin

# Install Kotlin compiler (optional)
ARG KOTLIN_VERSION=1.9.22
RUN wget https://github.com/JetBrains/kotlin/releases/download/v${KOTLIN_VERSION}/kotlin-compiler-${KOTLIN_VERSION}.zip -P /tmp \
    && unzip /tmp/kotlin-compiler-${KOTLIN_VERSION}.zip -d /opt \
    && rm /tmp/kotlin-compiler-${KOTLIN_VERSION}.zip
ENV PATH=$PATH:/opt/kotlinc/bin

# Create working directory
WORKDIR /workspace

# Configure git to trust the workspace directory (for Dev Containers)
RUN git config --global --add safe.directory /workspace

# Set up non-root user for better security
ARG USERNAME=devuser
ARG USER_UID=1000
ARG USER_GID=$USER_UID

RUN groupadd --gid $USER_GID $USERNAME \
    && useradd --uid $USER_UID --gid $USER_GID -m $USERNAME \
    && chown -R $USERNAME:$USERNAME /workspace

# Switch to non-root user
USER $USERNAME

# Default command
CMD ["bash"]

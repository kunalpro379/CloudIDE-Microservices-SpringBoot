FROM ubuntu:22.04

# Set environment variables
ENV DEBIAN_FRONTEND=noninteractive
ENV NODE_VERSION=18

# Install system dependencies
RUN apt-get update && apt-get install -y \
     # openjdk-17-jdk removed per user request \ 
     curl \
     unzip \
     git \
     bash \
     build-essential \
     python3 \
     python3-pip \
     wget \
     vim \
     nano \
     ca-certificates \
     && rm -rf /var/lib/apt/lists/*

# Install Node.js
RUN curl -fsSL https://deb.nodesource.com/setup_${NODE_VERSION}.x | bash - \
     && apt-get install -y nodejs

# Install AWS CLI
RUN pip3 install awscli


# Create directories
RUN mkdir -p /app /server

# Copy server files
COPY src/ /server/
WORKDIR /server

# Install Node.js dependencies
RUN npm install

# Create startup script
COPY scripts/startup.sh /startup.sh
RUN chmod +x /startup.sh

# Expose WebSocket port
EXPOSE 3000

# Set the startup command
CMD ["/startup.sh"]

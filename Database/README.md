# Cloud Code Editor - Authentication & Database System

## Overview

This project implements a comprehensive authentication and user management system for a cloud-based code editor. It includes:

- **JWT-based authentication** with access and refresh tokens
- **OAuth integration** with Google and GitHub
- **Role-based access control** for workspaces
- **Audit logging** for security and compliance
- **PostgreSQL** for user data, permissions, and audit logs
- **MongoDB** for workspace data and file management

## Architecture

```
flowchart TD
  A[User] --> B[Authentication Service]
  B --> C[Session & Access Management Service]
  C --> D[WebSocket Service<br/>(Chatting, CRDT, Code, etc.)]
  C --> E[Session Deployer]
  E --> F[Image Storage]
  E --> G[CodeBase Storage]
  E --> H[Azure Container App Worker]
  D --> I[Nginx<br/>(routing based on /:sessionId/:portName)] --> H
```

## Database Schema

### PostgreSQL Tables

#### Users Table
```sql
CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    profile_image_url VARCHAR(500),
    email_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    oauth_provider VARCHAR(50), -- 'google', 'github', 'local'
    oauth_provider_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);
```

#### Permissions Table
```sql
CREATE TABLE permissions (
    ws_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL CHECK(role IN ('owner','write','read')),
    granted_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(ws_id, user_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
```

#### Audit Logs Table
```sql
CREATE TABLE audit_logs (
    log_id SERIAL PRIMARY KEY,
    ws_id UUID,
    user_id UUID,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50), -- 'workspace', 'file', 'permission', etc.
    resource_id VARCHAR(255),
    old_value JSONB,
    new_value JSONB,
    ip_address INET,
    user_agent TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);
```

#### Refresh Tokens Table
```sql
CREATE TABLE refresh_tokens (
    token_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP,
    device_info VARCHAR(500),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
```

### MongoDB Collections

#### Workspaces Collection
```javascript
{
  _id: ObjectId,
  ws_id: UUID,
  owner_id: UUID,
  name: String,
  description: String,
  settings: {
    language: String,
    theme: String,
    font_size: Number,
    tab_size: Number,
    word_wrap: Boolean,
    auto_save: Boolean,
    auto_save_interval: Number
  },
  files: [
    {
      file_id: UUID,
      name: String,
      path: String,
      type: String, // 'file' or 'directory'
      content: String,
      size: Number,
      mime_type: String,
      created_at: Date,
      updated_at: Date,
      parent_id: UUID,
      permissions: {
        read: Boolean,
        write: Boolean,
        execute: Boolean
      }
    }
  ],
  environment: {
    runtime: String,
    version: String,
    dependencies: Array,
    environment_variables: Object,
    startup_command: String,
    port: Number
  },
  deployment: {
    status: String,
    container_id: String,
    image_name: String,
    ports: Array,
    last_deployed: Date,
    deployment_logs: Array
  },
  collaboration: {
    active_users: Array,
    chat_history: Array
  },
  created_at: Date,
  updated_at: Date,
  last_active: Date
}
```

## API Endpoints

### Authentication Endpoints

- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `POST /api/auth/refresh` - Refresh access token
- `POST /api/auth/logout` - Logout user
- `POST /api/auth/logout-all` - Logout all user sessions
- `GET /api/auth/oauth/{provider}/url` - Get OAuth authorization URL
- `POST /api/auth/oauth/{provider}/callback` - Handle OAuth callback
- `POST /api/auth/verify-email` - Verify email address
- `POST /api/auth/forgot-password` - Request password reset
- `POST /api/auth/reset-password` - Reset password
- `POST /api/auth/change-password` - Change password
- `GET /api/auth/me` - Get current user
- `POST /api/auth/validate-token` - Validate JWT token
- `POST /api/auth/revoke-token` - Revoke refresh token

### User Management Endpoints

- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user
- `POST /api/users/search` - Search users
- `POST /api/users/{id}/activate` - Activate user
- `POST /api/users/{id}/deactivate` - Deactivate user

### Workspace Permission Endpoints

- `POST /api/permissions/grant` - Grant workspace permission
- `DELETE /api/permissions/revoke` - Revoke workspace permission
- `PUT /api/permissions/update` - Update workspace permission
- `GET /api/permissions/user/{userId}` - Get user permissions
- `GET /api/permissions/workspace/{wsId}` - Get workspace permissions

### Audit Endpoints

- `GET /api/audit/workspace/{wsId}` - Get workspace audit logs
- `GET /api/audit/user/{userId}` - Get user audit logs
- `GET /api/audit/actions/{action}` - Get audit logs by action
- `GET /api/audit/range` - Get audit logs by date range

## Security Features

### JWT Authentication
- **Access tokens**: Short-lived (15 minutes) for API access
- **Refresh tokens**: Long-lived (7 days) for token renewal
- **Token rotation**: New refresh token on each refresh
- **Secure storage**: Refresh tokens are hashed in database

### OAuth Integration
- **Google OAuth**: Full profile and email access
- **GitHub OAuth**: User profile and email access
- **State validation**: CSRF protection for OAuth flows
- **Account linking**: Associate OAuth accounts with existing users

### Role-Based Access Control
- **Owner**: Full control over workspace
- **Write**: Can edit workspace files and settings
- **Read**: Can view workspace content only

### Audit Logging
- **Comprehensive tracking**: All user actions logged
- **Metadata capture**: IP address, user agent, timestamps
- **Change tracking**: Before/after values for modifications
- **Compliance ready**: Structured for regulatory requirements

## Configuration

### Environment Variables
```bash
# Database
DATABASE_URL=postgresql://username:password@localhost:5432/cloudcodeeditor
MONGODB_URI=mongodb://localhost:27017/cloudcodeeditor_workspaces

# JWT
JWT_SECRET=your-secret-key
JWT_ACCESS_TOKEN_EXPIRATION=900000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# OAuth
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret

# Application
APP_FRONTEND_URL=http://localhost:3000
```

### Application Properties
```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/api

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/cloudcodeeditor
spring.datasource.username=postgres
spring.datasource.password=password

# MongoDB Configuration
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=cloudcodeeditor_workspaces

# JWT Configuration
jwt.secret=your-secret-key
jwt.access-token-expiration=900000
jwt.refresh-token-expiration=604800000

# OAuth2 Configuration
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.github.client-id=${GITHUB_CLIENT_ID}
spring.security.oauth2.client.registration.github.client-secret=${GITHUB_CLIENT_SECRET}
```

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- MongoDB 4.4+

### Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd Backend
```

2. **Set up databases**
```bash
# Create PostgreSQL database
createdb cloudcodeeditor

# Run database schema
psql -d cloudcodeeditor -f Database/schema.sql

# Start MongoDB
mongod --dbpath /your/mongo/data/path
```

3. **Configure environment**
```bash
# Copy example config
cp application.properties.example application.properties

# Edit configuration
nano src/main/resources/application.properties
```

4. **Build and run**
```bash
# Build project
mvn clean install

# Run application
mvn spring-boot:run
```

### OAuth Setup

#### Google OAuth
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing
3. Enable Google+ API
4. Create OAuth 2.0 credentials
5. Add redirect URI: `http://localhost:8080/api/oauth2/callback/google`

#### GitHub OAuth
1. Go to [GitHub Developer Settings](https://github.com/settings/developers)
2. Create a new OAuth App
3. Set Authorization callback URL: `http://localhost:8080/api/oauth2/callback/github`

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn test -Dtest=*IntegrationTest
```

### Manual Testing
Use the provided Postman collection or curl commands:

```bash
# Register user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'

# Login user
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

## Security Considerations

1. **Password Security**: Passwords are hashed using BCrypt
2. **Token Security**: JWT tokens are signed with HS256
3. **Rate Limiting**: Implement rate limiting for auth endpoints
4. **HTTPS**: Use HTTPS in production
5. **CORS**: Configure CORS for your frontend domains
6. **Input Validation**: All inputs are validated
7. **SQL Injection**: Using parameterized queries
8. **XSS Protection**: Proper output encoding

## Performance Optimizations

1. **Database Indexes**: Proper indexing on frequently queried columns
2. **Connection Pooling**: Database connection pooling configured
3. **Caching**: Redis caching for frequently accessed data
4. **Async Processing**: Audit logging is asynchronous
5. **Pagination**: Large datasets are paginated

## Monitoring and Maintenance

1. **Health Checks**: `/actuator/health` endpoint
2. **Metrics**: Micrometer metrics integration
3. **Logging**: Structured logging with correlation IDs
4. **Cleanup Jobs**: Scheduled cleanup of expired tokens
5. **Audit Retention**: Configurable audit log retention

## Deployment

### Docker
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: auth-service
  template:
    metadata:
      labels:
        app: auth-service
    spec:
      containers:
      - name: auth-service
        image: your-registry/auth-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
```

## Contributing

1. Fork the repository
2. Create feature branch
3. Write tests for new functionality
4. Ensure all tests pass
5. Submit pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 
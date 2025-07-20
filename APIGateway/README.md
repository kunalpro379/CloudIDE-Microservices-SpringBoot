# API Gateway - Cloud Code Editor

This API Gateway serves as the single entry point for all client requests in the Cloud Code Editor microservices architecture.

## Architecture Overview

The API Gateway provides:
- **Centralized Authentication**: JWT token validation for all protected endpoints
- **Service Routing**: Routes requests to appropriate microservices
- **Rate Limiting**: Prevents API abuse using Redis-based rate limiting
- **Circuit Breaker**: Fallback responses when services are unavailable
- **CORS Support**: Cross-origin resource sharing configuration
- **Load Balancing**: Distributes requests across service instances

## Services Integration

### Authentication Service (Port 8081)
- **Route**: `/api/auth/**`
- **Public Endpoints**: 
  - `/api/auth/register`
  - `/api/auth/authenticate`
  - `/api/auth/refresh`
- **Protected Endpoints**: 
  - `/api/auth/user-info`
  - `/api/auth/logout`

### Session Manager Service (Port 8082)
- **Route**: `/api/session/**`
- **All endpoints are protected and require JWT authentication**
- **Examples**:
  - `/api/session/workspaces` - Workspace management
  - `/api/session/workspaces/invite` - User invitations
  - `/api/session/health` - Health check

## Features

### 1. JWT Authentication
- Validates access tokens for protected endpoints
- Extracts user information (userId, username, email) from tokens
- Adds user context to downstream service requests via headers:
  - `X-User-Id`: User UUID
  - `X-Username`: Username
  - `X-User-Email`: User email

### 2. Rate Limiting
- **Default Configuration**:
  - 10 requests per minute per IP (replenish rate)
  - 20 requests burst capacity
  - Uses Redis for distributed rate limiting

### 3. Circuit Breaker
- **Resilience4j Integration**:
  - 50% failure rate threshold
  - 30-second wait duration in open state
  - 10 sliding window size
  - 5 minimum number of calls

### 4. Fallback Responses
- **Authentication Service Fallback**: `/fallback/auth`
- **Session Manager Fallback**: `/fallback/session`
- Returns proper HTTP 503 responses with error details

## Configuration

### Environment Variables
```yaml
# Eureka Configuration
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://eureka-server:8761/eureka/

# Redis Configuration
SPRING_REDIS_HOST: redis
SPRING_REDIS_PORT: 6379

# JWT Configuration
JWT_SECRET: your-jwt-secret-key
JWT_ACCESS_TOKEN_EXPIRATION: 900000
JWT_REFRESH_TOKEN_EXPIRATION: 604800000
```

### Rate Limiting Configuration
```yaml
rate-limit:
  replenish-rate: 10      # Requests per minute
  burst-capacity: 20      # Maximum burst requests
  requested-tokens: 1     # Tokens per request
```

## API Routes

### Authentication Routes
```
POST   /api/auth/register           # User registration
POST   /api/auth/authenticate       # User login
POST   /api/auth/refresh           # Token refresh
POST   /api/auth/logout            # User logout
GET    /api/auth/user-info         # Get user information [Protected]
GET    /api/auth/validate          # Validate token [Protected]
```

### Session Management Routes
```
POST   /api/session/workspaces                    # Create workspace [Protected]
GET    /api/session/workspaces/{wsId}             # Get workspace [Protected]
PUT    /api/session/workspaces/{wsId}             # Update workspace [Protected]
DELETE /api/session/workspaces/{wsId}             # Delete workspace [Protected]
GET    /api/session/workspaces/my-workspaces      # Get user's workspaces [Protected]
POST   /api/session/workspaces/invite             # Invite users [Protected]
POST   /api/session/workspaces/invitations/{id}/accept # Accept invitation [Protected]
```

### Health Check Routes
```
GET    /actuator/health            # Gateway health check
GET    /actuator/gateway/routes    # View configured routes
```

## Running the API Gateway

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker (optional)
- Redis server
- Eureka server

### Local Development
```bash
# Clone the repository
git clone <repository-url>
cd APIGateway

# Build the project
mvn clean package

# Run the application
java -jar target/api-gateway-0.0.1-SNAPSHOT.jar
```

### Docker Deployment
```bash
# Build Docker image
docker build -t api-gateway .

# Run with Docker Compose (recommended)
docker-compose up -d
```

## Usage Examples

### 1. User Registration
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### 2. User Authentication
```bash
curl -X POST http://localhost:8080/api/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'
```

### 3. Create Workspace (Protected)
```bash
curl -X POST http://localhost:8080/api/session/workspaces \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt-token>" \
  -d '{
    "name": "My Workspace",
    "description": "A sample workspace",
    "isPublic": false
  }'
```

### 4. Get User Workspaces (Protected)
```bash
curl -X GET http://localhost:8080/api/session/workspaces/my-workspaces \
  -H "Authorization: Bearer <jwt-token>"
```

## Monitoring and Debugging

### Health Checks
- **Gateway Health**: `http://localhost:8080/actuator/health`
- **Service Routes**: `http://localhost:8080/actuator/gateway/routes`
- **Metrics**: `http://localhost:8080/actuator/metrics`

### Logging
- **Gateway Logs**: `DEBUG` level for `com.example.apigateway`
- **Spring Cloud Gateway**: `DEBUG` level for routing details
- **Circuit Breaker**: Monitor circuit states in logs

### Common Issues
1. **Service Discovery**: Ensure Eureka server is running
2. **Redis Connection**: Check Redis connectivity for rate limiting
3. **JWT Validation**: Verify JWT secret matches across services
4. **CORS Issues**: Check allowed origins in gateway configuration

## Security Considerations

1. **JWT Secret**: Use a strong, unique secret key
2. **Rate Limiting**: Adjust limits based on expected traffic
3. **CORS**: Configure specific origins in production
4. **Circuit Breaker**: Fine-tune thresholds for your use case
5. **Logging**: Avoid logging sensitive information

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 
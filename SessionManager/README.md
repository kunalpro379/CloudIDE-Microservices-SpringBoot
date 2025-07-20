# Session Manager Service

The Session Manager Service is responsible for managing user sessions, deployments, and participant access control in the Cloud Code Editor platform.

## Features

### Session Management
- **Session Creation**: Users can create sessions with language, framework, and dynamic/static configuration
- **Session Limits**: Free users can create only one session (Pro plan required for more)
- **Participant Management**: Add, remove, and manage participant roles (Owner, Editor, Viewer)
- **Access Control**: Role-based permissions for session operations

### Deployment Management
- **Session Deployment**: Deploy sessions to runnable environments
- **Deployment Tracking**: Track deployment status, runtime, and history
- **Past Deployments**: Maintain history of previous deployments for redeployment
- **Auto-join**: Users automatically join existing deployments when trying to deploy

### Security & Authentication
- **JWT Integration**: Secure authentication with JWT tokens
- **Role-based Access**: Fine-grained permission control
- **User Validation**: Integration with Authentication Service

## Database Schema

### Core Tables
- `sessions`: Main session information
- `session_participants`: Session participants and their roles
- `session_invitations`: Pending session invitations
- `deployments`: Active session deployments
- `deployment_participants`: Participants in deployed sessions
- `past_deployments`: Historical deployment data

## API Endpoints

### Session Management

#### Create Session
```http
POST /api/session/sessions
Content-Type: application/json
Authorization: Bearer <jwt_token>

{
  "name": "My Session",
  "description": "A collaborative coding session",
  "language": "javascript",
  "framework": "react",
  "isDynamic": true,
  "participants": [
    {
      "email": "user@example.com",
      "role": "EDITOR",
      "message": "Join my session!"
    }
  ]
}
```

#### Get Session
```http
GET /api/session/sessions/{sessionId}
Authorization: Bearer <jwt_token>
```

#### Update Session
```http
PUT /api/session/sessions/{sessionId}
Content-Type: application/json
Authorization: Bearer <jwt_token>

{
  "name": "Updated Session Name",
  "description": "Updated description",
  "language": "python",
  "framework": "django",
  "isDynamic": false
}
```

#### Delete Session
```http
DELETE /api/session/sessions/{sessionId}
Authorization: Bearer <jwt_token>
```

#### Archive Session
```http
POST /api/session/sessions/{sessionId}/archive
Authorization: Bearer <jwt_token>
```

### Participant Management

#### Add Participant
```http
POST /api/session/sessions/{sessionId}/participants?email=user@example.com&role=EDITOR
Authorization: Bearer <jwt_token>
```

#### Update Participant Role
```http
PUT /api/session/sessions/{sessionId}/participants/{participantId}/role?newRole=VIEWER
Authorization: Bearer <jwt_token>
```

#### Remove Participant
```http
DELETE /api/session/sessions/{sessionId}/participants/{participantId}
Authorization: Bearer <jwt_token>
```

#### Get Session Participants
```http
GET /api/session/sessions/{sessionId}/participants
Authorization: Bearer <jwt_token>
```

### Deployment Management

#### Deploy Session
```http
POST /api/session/sessions/{sessionId}/deploy
Authorization: Bearer <jwt_token>
```

#### Join Deployed Session
```http
POST /api/session/sessions/{sessionId}/join
Authorization: Bearer <jwt_token>
```

#### Stop Session
```http
POST /api/session/sessions/{sessionId}/stop
Authorization: Bearer <jwt_token>
```

#### Restart Session
```http
POST /api/session/sessions/{sessionId}/restart
Authorization: Bearer <jwt_token>
```

#### Get Session Status
```http
GET /api/session/sessions/{sessionId}/status
Authorization: Bearer <jwt_token>
```

### Session Queries

#### Get User Sessions
```http
GET /api/session/sessions/my-sessions
Authorization: Bearer <jwt_token>
```

#### Get Owned Sessions
```http
GET /api/session/sessions/owned
Authorization: Bearer <jwt_token>
```

#### Get Active Deployments
```http
GET /api/session/sessions/active-deployments
Authorization: Bearer <jwt_token>
```

## Response Format

### Session Response
```json
{
  "sessionId": "uuid",
  "name": "Session Name",
  "description": "Session description",
  "language": "javascript",
  "framework": "react",
  "isDynamic": true,
  "ownerId": "uuid",
  "status": "CREATED",
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00",
  "lastActive": "2024-01-01T00:00:00",
  "participants": [
    {
      "participantId": "uuid",
      "userId": "uuid",
      "email": "user@example.com",
      "role": "OWNER",
      "joinedAt": "2024-01-01T00:00:00",
      "lastActive": "2024-01-01T00:00:00",
      "isActive": true
    }
  ],
  "deploymentInfo": {
    "deploymentId": "uuid",
    "deploymentUrl": "http://localhost:8080/session/uuid",
    "containerId": "container-123",
    "port": 8080,
    "status": "RUNNING",
    "createdAt": "2024-01-01T00:00:00",
    "startedAt": "2024-01-01T00:00:00",
    "lastActive": "2024-01-01T00:00:00"
  }
}
```

## Error Handling

The service includes comprehensive error handling with appropriate HTTP status codes:

- `400 Bad Request`: Invalid request data or user limit exceeded
- `401 Unauthorized`: Missing or invalid authentication
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Session not found
- `500 Internal Server Error`: Unexpected server errors

## Configuration

### Application Properties
```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/cloud_code_editor
spring.datasource.username=postgres
spring.datasource.password=password

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true

# Authentication Service
feign.client.config.authentication-service.url=http://localhost:8081

# Server Configuration
server.port=8082
```

## Security

### Authentication
- JWT token validation via Authentication Service
- Role-based access control
- Session ownership validation

### Authorization Levels
- **OWNER**: Full control over session and participants
- **EDITOR**: Can modify session content and manage participants
- **VIEWER**: Read-only access to session

## Business Rules

1. **Session Limits**: Free users can create only one active session
2. **Deployment Logic**: If session is already deployed, users join existing deployment
3. **Participant Access**: Only invited users can join sessions
4. **Role Hierarchy**: OWNER > EDITOR > VIEWER
5. **Deployment History**: Past deployments are preserved for redeployment

## Integration Points

- **Authentication Service**: User validation and JWT token verification
- **API Gateway**: Request routing and load balancing
- **Database**: PostgreSQL for data persistence
- **Future**: Integration with container orchestration for actual deployments 
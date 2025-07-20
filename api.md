# Cloud Code Editor API Documentation

This document provides comprehensive API documentation for the Cloud Code Editor microservices architecture, including test endpoints and sample JSON data.

## Table of Contents

1. [Overview](#overview)
2. [Base URLs](#base-urls)
3. [Authentication](#authentication)
4. [API Gateway](#api-gateway)
5. [Authentication Service](#authentication-service)
6. [Session Manager Service](#session-manager-service)
7. [Testing Examples](#testing-examples)

## Overview

The Cloud Code Editor consists of three main services:
- **API Gateway** (Port 8233): Central entry point with authentication, rate limiting, and routing
- **Authentication Service** (Port 8009): User registration, login, and JWT token management
- **Session Manager Service** (Port 8082): Workspace management and user sessions

## Base URLs

```yaml
API Gateway: http://localhost:8233
Authentication Service: http://localhost:8009/api/auth
Session Manager Service: http://localhost:8082/api/session
```

## Authentication

All protected endpoints require a JWT Bearer token in the Authorization header:

```bash
Authorization: Bearer <your-jwt-token>
```

## API Gateway

### Health Check
```bash
GET http://localhost:8233/actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "discoveryComposite": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

### Gateway Routes
```bash
GET http://localhost:8233/actuator/gateway/routes
```

### Fallback Endpoints
```bash
GET http://localhost:8233/fallback/auth
GET http://localhost:8233/fallback/session
GET http://localhost:8233/fallback/general
```

**Response:**
```json
{
  "error": "Service Unavailable",
  "message": "Authentication service is temporarily unavailable. Please try again later.",
  "service": "authentication-service",
  "timestamp": 1703123456789
}
```

## Authentication Service

### 1. User Registration
```bash
POST http://localhost:8233/api/auth/register
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "john_doe",
  "email": "john.doe@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "profileImageUrl": "https://example.com/avatar.jpg"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "profileImageUrl": "https://example.com/avatar.jpg",
    "emailVerified": false,
    "isActive": true,
    "oauthProvider": "local",
    "createdAt": "2024-01-15T10:30:00",
    "lastLogin": "2024-01-15T10:30:00"
  }
}
```

### 2. User Authentication
```bash
POST http://localhost:8233/api/auth/authenticate
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "password123"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "profileImageUrl": "https://example.com/avatar.jpg",
    "emailVerified": false,
    "isActive": true,
    "oauthProvider": "local",
    "createdAt": "2024-01-15T10:30:00",
    "lastLogin": "2024-01-15T10:35:00"
  }
}
```

### 3. Token Refresh
```bash
POST http://localhost:8233/api/auth/refresh
Content-Type: application/json
```

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

### 4. Token Validation
```bash
GET http://localhost:8233/api/auth/validate
Authorization: Bearer <access-token>
```

**Response:**
```json
{
  "valid": true
}
```

### 5. Get User Info
```bash
GET http://localhost:8233/api/auth/user-info
Authorization: Bearer <access-token>
```

**Response:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "profileImageUrl": "https://example.com/avatar.jpg",
  "emailVerified": false,
  "isActive": true
}
```

### 6. Logout
```bash
POST http://localhost:8233/api/auth/logout
Authorization: Bearer <access-token>
```

**Response:**
```http
HTTP/1.1 200 OK
```

### 7. Logout All Sessions
```bash
POST http://localhost:8233/api/auth/logout-all
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "john_doe"
}
```

**Response:**
```http
HTTP/1.1 200 OK
```

## Session Manager Service

### 1. Health Check
```bash
GET http://localhost:8233/api/session/health
```

**Response:**
```json
{
  "status": "UP",
  "service": "session-manager",
  "timestamp": "2024-01-15T10:30:00",
  "message": "SessionManager service is running"
}
```

### 2. Create Workspace
```bash
POST http://localhost:8233/api/session/workspaces
Authorization: Bearer <access-token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "My First Workspace",
  "description": "A sample workspace for testing",
  "isPublic": false,
  "isTemplate": false
}
```

**Response:**
```json
{
  "wsId": "660e8400-e29b-41d4-a716-446655440001",
  "name": "My First Workspace",
  "description": "A sample workspace for testing",
  "ownerId": "550e8400-e29b-41d4-a716-446655440000",
  "isPublic": false,
  "isTemplate": false,
  "status": "ACTIVE",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "lastActive": "2024-01-15T10:30:00"
}
```

### 3. Get Workspace
```bash
GET http://localhost:8233/api/session/workspaces/{wsId}
Authorization: Bearer <access-token>
```

**Response:**
```json
{
  "wsId": "660e8400-e29b-41d4-a716-446655440001",
  "name": "My First Workspace",
  "description": "A sample workspace for testing",
  "ownerId": "550e8400-e29b-41d4-a716-446655440000",
  "isPublic": false,
  "isTemplate": false,
  "status": "ACTIVE",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "lastActive": "2024-01-15T10:30:00"
}
```

### 4. Update Workspace
```bash
PUT http://localhost:8233/api/session/workspaces/{wsId}
Authorization: Bearer <access-token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Updated Workspace Name",
  "description": "Updated description for the workspace",
  "isPublic": true,
  "isTemplate": false
}
```

**Response:**
```json
{
  "wsId": "660e8400-e29b-41d4-a716-446655440001",
  "name": "Updated Workspace Name",
  "description": "Updated description for the workspace",
  "ownerId": "550e8400-e29b-41d4-a716-446655440000",
  "isPublic": true,
  "isTemplate": false,
  "status": "ACTIVE",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:35:00",
  "lastActive": "2024-01-15T10:30:00"
}
```

### 5. Delete Workspace
```bash
DELETE http://localhost:8233/api/session/workspaces/{wsId}
Authorization: Bearer <access-token>
```

**Response:**
```http
HTTP/1.1 204 No Content
```

### 6. Archive Workspace
```bash
POST http://localhost:8233/api/session/workspaces/{wsId}/archive
Authorization: Bearer <access-token>
```

**Response:**
```http
HTTP/1.1 200 OK
```

### 7. Get User Workspaces
```bash
GET http://localhost:8233/api/session/workspaces/my-workspaces
Authorization: Bearer <access-token>
```

**Response:**
```json
[
  {
    "wsId": "660e8400-e29b-41d4-a716-446655440001",
    "name": "My First Workspace",
    "description": "A sample workspace for testing",
    "ownerId": "550e8400-e29b-41d4-a716-446655440000",
    "isPublic": false,
    "isTemplate": false,
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00",
    "lastActive": "2024-01-15T10:30:00"
  },
  {
    "wsId": "660e8400-e29b-41d4-a716-446655440002",
    "name": "Another Workspace",
    "description": "Another workspace description",
    "ownerId": "550e8400-e29b-41d4-a716-446655440000",
    "isPublic": true,
    "isTemplate": false,
    "status": "ACTIVE",
    "createdAt": "2024-01-15T11:00:00",
    "updatedAt": "2024-01-15T11:00:00",
    "lastActive": "2024-01-15T11:00:00"
  }
]
```

### 8. Get Owned Workspaces
```bash
GET http://localhost:8233/api/session/workspaces/owned
Authorization: Bearer <access-token>
```

**Response:**
```json
[
  {
    "wsId": "660e8400-e29b-41d4-a716-446655440001",
    "name": "My First Workspace",
    "description": "A sample workspace for testing",
    "ownerId": "550e8400-e29b-41d4-a716-446655440000",
    "isPublic": false,
    "isTemplate": false,
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00",
    "lastActive": "2024-01-15T10:30:00"
  }
]
```

### 9. Get Public Workspaces
```bash
GET http://localhost:8233/api/session/workspaces/public
Authorization: Bearer <access-token>
```

**Response:**
```json
[
  {
    "wsId": "660e8400-e29b-41d4-a716-446655440002",
    "name": "Public Workspace",
    "description": "A public workspace for everyone",
    "ownerId": "550e8400-e29b-41d4-a716-446655440000",
    "isPublic": true,
    "isTemplate": false,
    "status": "ACTIVE",
    "createdAt": "2024-01-15T11:00:00",
    "updatedAt": "2024-01-15T11:00:00",
    "lastActive": "2024-01-15T11:00:00"
  }
]
```

### 10. Invite Users to Workspace
```bash
POST http://localhost:8233/api/session/workspaces/invite
Authorization: Bearer <access-token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "wsId": "660e8400-e29b-41d4-a716-446655440001",
  "emails": ["jane.doe@example.com", "bob.smith@example.com"],
  "role": "EDITOR",
  "message": "You're invited to collaborate on this workspace!"
}
```

**Response:**
```json
[
  {
    "invitationId": "770e8400-e29b-41d4-a716-446655440001",
    "wsId": "660e8400-e29b-41d4-a716-446655440001",
    "inviterId": "550e8400-e29b-41d4-a716-446655440000",
    "inviteeEmail": "jane.doe@example.com",
    "role": "EDITOR",
    "status": "PENDING",
    "message": "You're invited to collaborate on this workspace!",
    "createdAt": "2024-01-15T10:30:00",
    "expiresAt": "2024-01-22T10:30:00"
  },
  {
    "invitationId": "770e8400-e29b-41d4-a716-446655440002",
    "wsId": "660e8400-e29b-41d4-a716-446655440001",
    "inviterId": "550e8400-e29b-41d4-a716-446655440000",
    "inviteeEmail": "bob.smith@example.com",
    "role": "EDITOR",
    "status": "PENDING",
    "message": "You're invited to collaborate on this workspace!",
    "createdAt": "2024-01-15T10:30:00",
    "expiresAt": "2024-01-22T10:30:00"
  }
]
```

### 11. Accept Invitation
```bash
POST http://localhost:8233/api/session/workspaces/invitations/{invitationId}/accept
Authorization: Bearer <access-token>
```

**Response:**
```http
HTTP/1.1 200 OK
```

### 12. Reject Invitation
```bash
POST http://localhost:8233/api/session/workspaces/invitations/{invitationId}/reject
Authorization: Bearer <access-token>
```

**Response:**
```http
HTTP/1.1 200 OK
```

### 13. Get User Invitations
```bash
GET http://localhost:8233/api/session/workspaces/invitations
Authorization: Bearer <access-token>
```

**Response:**
```json
[
  {
    "invitationId": "770e8400-e29b-41d4-a716-446655440003",
    "wsId": "660e8400-e29b-41d4-a716-446655440002",
    "inviterId": "550e8400-e29b-41d4-a716-446655440000",
    "inviteeEmail": "john.doe@example.com",
    "role": "VIEWER",
    "status": "PENDING",
    "message": "Join our public workspace!",
    "createdAt": "2024-01-15T11:00:00",
    "expiresAt": "2024-01-22T11:00:00"
  }
]
```

### 14. Get Workspace Permissions
```bash
GET http://localhost:8233/api/session/workspaces/{wsId}/permissions
Authorization: Bearer <access-token>
```

**Response:**
```json
[
  {
    "permissionId": "880e8400-e29b-41d4-a716-446655440001",
    "wsId": "660e8400-e29b-41d4-a716-446655440001",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "role": "ADMIN",
    "grantedAt": "2024-01-15T10:30:00"
  },
  {
    "permissionId": "880e8400-e29b-41d4-a716-446655440002",
    "wsId": "660e8400-e29b-41d4-a716-446655440001",
    "userId": "550e8400-e29b-41d4-a716-446655440003",
    "role": "EDITOR",
    "grantedAt": "2024-01-15T10:35:00"
  }
]
```

### 15. Remove User from Workspace
```bash
DELETE http://localhost:8233/api/session/workspaces/{wsId}/users/{targetUserId}
Authorization: Bearer <access-token>
```

**Response:**
```http
HTTP/1.1 200 OK
```

### 16. Deploy Workspace
```bash
POST http://localhost:8233/api/session/workspaces/{wsId}/deploy
Authorization: Bearer <access-token>
```

**Response:**
```json
{
  "message": "Workspace deployment initiated",
  "status": "deploying"
}
```

### 17. Stop Workspace
```bash
POST http://localhost:8233/api/session/workspaces/{wsId}/stop
Authorization: Bearer <access-token>
```

**Response:**
```json
{
  "message": "Workspace stopped",
  "status": "stopped"
}
```

### 18. Restart Workspace
```bash
POST http://localhost:8233/api/session/workspaces/{wsId}/restart
Authorization: Bearer <access-token>
```

**Response:**
```json
{
  "message": "Workspace restarted",
  "status": "running"
}
```

### 19. Get Workspace Status
```bash
GET http://localhost:8233/api/session/workspaces/{wsId}/status
Authorization: Bearer <access-token>
```

**Response:**
```json
{
  "wsId": "660e8400-e29b-41d4-a716-446655440001",
  "status": "running"
}
```

## Testing Examples

### Complete User Flow Example

1. **Register a new user:**
```bash
curl -X POST http://localhost:8233/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

2. **Login with the user:**
```bash
curl -X POST http://localhost:8233/api/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

3. **Create a workspace (using the token from login):**
```bash
curl -X POST http://localhost:8233/api/session/workspaces \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Workspace",
    "description": "A test workspace",
    "isPublic": false
  }'
```

4. **Get user's workspaces:**
```bash
curl -X GET http://localhost:8233/api/session/workspaces/my-workspaces \
  -H "Authorization: Bearer <access-token>"
```

### Error Response Examples

**Invalid Credentials:**
```json
{
  "error": "Unauthorized",
  "message": "Invalid username or password"
}
```

**Missing Token:**
```json
{
  "error": "Unauthorized",
  "message": "Missing or invalid Authorization header"
}
```

**Invalid Token:**
```json
{
  "error": "Unauthorized",
  "message": "Invalid or expired JWT token"
}
```

**Rate Limit Exceeded:**
```json
{
  "error": "Rate Limit Exceeded",
  "message": "Too many requests. Please try again later.",
  "timestamp": 1703123456789
}
```

**Service Unavailable:**
```json
{
  "error": "Service Unavailable",
  "message": "Authentication service is temporarily unavailable. Please try again later.",
  "service": "authentication-service",
  "timestamp": 1703123456789
}
```

### Testing with Postman

1. **Environment Variables:**
   - `base_url`: `http://localhost:8233`
   - `auth_token`: (will be set after login)

2. **Pre-request Script for Login:**
```javascript
pm.sendRequest({
    url: pm.environment.get("base_url") + "/api/auth/authenticate",
    method: 'POST',
    header: {
        'Content-Type': 'application/json'
    },
    body: {
        mode: 'raw',
        raw: JSON.stringify({
            username: "testuser",
            password: "password123"
        })
    }
}, function (err, response) {
    if (response.code === 200) {
        const responseJson = response.json();
        pm.environment.set("auth_token", responseJson.accessToken);
    }
});
```

3. **Authorization Header:**
```
Authorization: Bearer {{auth_token}}
```

## Notes

- All timestamps are in ISO 8601 format
- UUIDs are used for all IDs
- JWT tokens expire after 15 minutes (900 seconds)
- Refresh tokens are valid for 7 days
- Rate limiting is set to 10 requests per minute per IP
- All services use MySQL and MongoDB as configured
- CORS is enabled for localhost development 
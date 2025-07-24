const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const path = require('path');
const fs = require('fs-extra');
const jwt = require('jsonwebtoken');
const util = require('util');
const exec = util.promisify(require('child_process').exec);

const terminalService = require('./services/terminalService');
const fileService = require('./services/fileService');
const s3Service = require('./services/s3Service');

const app = express();
const server = http.createServer(app);
const io = socketIo(server, {
     cors: {
          origin: "*",
          methods: ["GET", "POST"],
          credentials: true
     }
});

const PORT = process.env.PORT || 3000;
const JWT_SECRET = process.env.JWT_SECRET || 'your-secret-key';
const IDLE_TIMEOUT = parseInt(process.env.IDLE_TIMEOUT) || 1800000; // 30 minutes

// Middleware
app.use(express.json());
app.use((req, res, next) => {
     res.header('Access-Control-Allow-Origin', '*');
     res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
     res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept, Authorization');
     if (req.method === 'OPTIONS') {
          res.sendStatus(200);
     } else {
          next();
     }
});

// Store active sessions and terminals
const activeSessions = new Map();
const activeTerminals = new Map();
const projectWorkspace = '/app';

// Idle timeout tracking
let lastActivity = Date.now();
let idleTimer;

function updateActivity() {
     lastActivity = Date.now();
     if (idleTimer) {
          clearTimeout(idleTimer);
     }
     idleTimer = setTimeout(() => {
          console.log('Container idle timeout reached. Shutting down...');
          process.exit(0);
     }, IDLE_TIMEOUT);
}

// Initialize project from S3 URL if provided at startup
async function initializeProject() {
     const s3Url = process.env.S3_URL;
     if (s3Url) {
          try {
               console.log(`Initializing project from S3: ${s3Url}`);
               const projectName = await s3Service.downloadAndExtract(s3Url, projectWorkspace);
               console.log(`✅ Project initialized: ${projectName}`);
               return projectName;
          } catch (error) {
               console.error(`❌ Failed to initialize project from S3: ${error.message}`);
          }
     }
     return null;
}

// JWT middleware for socket authentication (optional)
function authenticateSocket(socket, next) {
     const token = socket.handshake.auth.token;

     if (!token) {
          // Allow anonymous connections for now
          socket.userId = `anonymous_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
          return next();
     }

     try {
          const decoded = jwt.verify(token, JWT_SECRET);
          socket.userId = decoded.userId;
          next();
     } catch (err) {
          next(new Error('Authentication error'));
     }
}

// Socket.io middleware
io.use(authenticateSocket);

// Handle socket connections
io.on('connection', (socket) => {
     console.log(`User connected: ${socket.userId}`);
     updateActivity();

     // Join user to a default room (project-based rooms)
     const projectRoom = 'default-project';
     socket.join(projectRoom);
     socket.projectRoom = projectRoom;

     // Send server info to client
     socket.emit('server:info', {
          workspaceBase: projectWorkspace,
          javaVersion: process.env.JAVA_HOME,
          nodeVersion: process.version
     });

     // Handle terminal connections
     socket.on('terminal:start', (data) => {
          updateActivity();
          const terminalId = data.terminalId || `terminal_${socket.userId}_${Date.now()}`;

          if (!activeTerminals.has(terminalId)) {
               const terminal = terminalService.createTerminal(terminalId, projectWorkspace);
               activeTerminals.set(terminalId, terminal);

               // Handle terminal output
               terminal.onData((data) => {
                    io.to(projectRoom).emit('terminal:output', {
                         terminalId,
                         data
                    });
               });

               // Handle terminal exit
               terminal.onExit(() => {
                    activeTerminals.delete(terminalId);
                    io.to(projectRoom).emit('terminal:exit', { terminalId });
               });
          }

          socket.terminalId = terminalId;
          socket.emit('terminal:started', { terminalId });
     });

     // Handle terminal input
     socket.on('terminal:input', (data) => {
          updateActivity();
          const terminal = activeTerminals.get(data.terminalId);
          if (terminal) {
               terminal.write(data.input);
          }
     });

     // Handle file operations
     socket.on('file:read', async (data) => {
          updateActivity();
          try {
               const result = await fileService.readFile(projectWorkspace, data.path);
               socket.emit('file:read:response', {
                    success: true,
                    path: data.path,
                    content: result.content,
                    isDirectory: result.isDirectory,
                    files: result.files
               });
          } catch (error) {
               socket.emit('file:read:response', {
                    success: false,
                    path: data.path,
                    error: error.message
               });
          }
     });

     socket.on('file:write', async (data) => {
          updateActivity();
          try {
               await fileService.writeFile(projectWorkspace, data.path, data.content);
               socket.emit('file:write:response', {
                    success: true,
                    path: data.path
               });

               // Broadcast file change to all users in the room
               socket.to(projectRoom).emit('file:changed', {
                    path: data.path,
                    userId: socket.userId
               });
          } catch (error) {
               socket.emit('file:write:response', {
                    success: false,
                    path: data.path,
                    error: error.message
               });
          }
     });

     socket.on('file:create', async (data) => {
          updateActivity();
          try {
               await fileService.createFile(projectWorkspace, data.path, data.isDirectory);
               socket.emit('file:create:response', {
                    success: true,
                    path: data.path
               });

               // Broadcast file creation to all users in the room
               socket.to(projectRoom).emit('file:created', {
                    path: data.path,
                    isDirectory: data.isDirectory,
                    userId: socket.userId
               });
          } catch (error) {
               socket.emit('file:create:response', {
                    success: false,
                    path: data.path,
                    error: error.message
               });
          }
     });

     socket.on('file:delete', async (data) => {
          updateActivity();
          try {
               await fileService.deleteFile(projectWorkspace, data.path);
               socket.emit('file:delete:response', {
                    success: true,
                    path: data.path
               });

               // Broadcast file deletion to all users in the room
               socket.to(projectRoom).emit('file:deleted', {
                    path: data.path,
                    userId: socket.userId
               });
          } catch (error) {
               socket.emit('file:delete:response', {
                    success: false,
                    path: data.path,
                    error: error.message
               });
          }
     });

     // Handle project listing
     socket.on('project:list', async () => {
          updateActivity();
          try {
               const projects = await fileService.listProjects(projectWorkspace);
               socket.emit('project:list:response', {
                    success: true,
                    projects
               });
          } catch (error) {
               socket.emit('project:list:response', {
                    success: false,
                    error: error.message
               });
          }
     });

     // Handle disconnection
     socket.on('disconnect', () => {
          console.log(`User disconnected: ${socket.userId}`);

          // Clean up terminal if it was the only user
          if (socket.terminalId) {
               const terminal = activeTerminals.get(socket.terminalId);
               if (terminal) {
                    // Check if other users are still connected to this terminal
                    const connectedUsers = Array.from(io.sockets.sockets.values())
                         .filter(s => s.terminalId === socket.terminalId);

                    if (connectedUsers.length === 0) {
                         terminal.kill();
                         activeTerminals.delete(socket.terminalId);
                    }
               }
          }
     });
});

// REST API endpoint for S3 download
app.post('/api/download-project', async (req, res) => {
     updateActivity();
     try {
          const { s3Url } = req.body;
          if (!s3Url) {
               return res.status(400).json({ error: 'S3 URL is required' });
          }

          const projectName = await s3Service.downloadAndExtract(s3Url, projectWorkspace);

          // Notify all connected clients about the new project
          io.emit('project:downloaded', {
               projectName,
               path: path.join(projectWorkspace, projectName)
          });

          res.json({
               success: true,
               projectName,
               message: 'Project downloaded and extracted successfully'
          });
     } catch (error) {
          console.error('Error downloading project:', error);
          res.status(500).json({
               success: false,
               error: error.message
          });
     }
});

// Health check endpoint
app.get('/health', (req, res) => {
     res.json({
          status: 'healthy',
          uptime: process.uptime(),
          lastActivity: new Date(lastActivity).toISOString(),
          activeConnections: io.sockets.sockets.size,
          activeTerminals: activeTerminals.size,
          projectWorkspace,
          javaVersion: process.env.JAVA_HOME,
          nodeVersion: process.version
     });
});

// Server info endpoint
app.get('/api/info', (req, res) => {
     res.json({
          serverType: 'Cloud IDE Server',
          version: '1.0.0',
          workspaceBase: projectWorkspace,
          capabilities: ['terminal', 'files', 's3-download', 'java', 'nodejs'],
          protocols: ['websocket', 'rest']
     });
});

// Test route to build and verify Java projects
app.get('/test', async (req, res) => {
    try {
        const base = projectWorkspace + '/JavaContainer';
        await exec(`cd ${base}/simple-maven ; mvn clean package -q`);
        await exec(`cd ${base}/spring-boot ; mvn clean package -q`);
        await exec(`cd ${base}/gradle-kotlin ; gradle build -q --stacktrace`);
        return res.json({ success: true, message: 'All projects built successfully' });
    } catch (err) {
        return res.status(500).json({
            success: false,
            message: 'Build failed',
            error: err.message,
            stdout: err.stdout,
            stderr: err.stderr
        });
    }
});

// Start server
async function startServer() {
     try {
          // Initialize project from S3 if provided
          await initializeProject();

          server.listen(PORT, () => {
               console.log(`🚀 Cloud IDE Server running on port ${PORT}`);
               console.log(`📁 Project workspace: ${projectWorkspace}`);
               console.log(`⏱️  Idle timeout: ${IDLE_TIMEOUT}ms`);
               console.log(`☕ Java: ${process.env.JAVA_HOME}`);
               console.log(`🟢 Node.js: ${process.version}`);

               // Initialize activity timer
               updateActivity();
          });
     } catch (error) {
          console.error('Failed to start server:', error);
          process.exit(1);
     }
}

startServer();

// Handle graceful shutdown
process.on('SIGTERM', () => {
     console.log('SIGTERM received, shutting down gracefully');

     // Close all terminals
     activeTerminals.forEach(terminal => terminal.kill());

     server.close(() => {
          console.log('Process terminated');
          process.exit(0);
     });
});

module.exports = { app, server, io };

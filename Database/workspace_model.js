// MongoDB Collection: workspaces
// This defines the structure for workspace documents in MongoDB

const workspaceSchema = {
     _id: "ObjectId", // MongoDB generated ID
     ws_id: "UUID", // UUID for compatibility with SQL tables
     owner_id: "UUID", // References users.user_id from SQL
     name: "String", // Workspace name
     description: "String", // Optional description

     // Workspace settings
     settings: {
          language: "String", // Default language
          theme: "String", // Editor theme
          font_size: "Number",
          tab_size: "Number",
          word_wrap: "Boolean",
          auto_save: "Boolean",
          auto_save_interval: "Number" // in seconds
     },

     // File structure - hierarchical
     files: [
          {
               file_id: "UUID",
               name: "String", // filename
               path: "String", // full path from root
               type: "String", // 'file' or 'directory'
               content: "String", // file content (only for files)
               size: "Number", // file size in bytes
               mime_type: "String",
               created_at: "Date",
               updated_at: "Date",
               parent_id: "UUID", // for nested structure
               permissions: {
                    read: "Boolean",
                    write: "Boolean",
                    execute: "Boolean"
               }
          }
     ],

     // Environment configuration
     environment: {
          runtime: "String", // 'node', 'python', 'java', etc.
          version: "String",
          dependencies: [
               {
                    name: "String",
                    version: "String",
                    type: "String" // 'npm', 'pip', 'maven', etc.
               }
          ],
          environment_variables: {
               // key-value pairs
          },
          startup_command: "String",
          port: "Number"
     },

     // Container/deployment status
     deployment: {
          status: "String", // 'stopped', 'starting', 'running', 'error'
          container_id: "String",
          image_name: "String",
          ports: [
               {
                    internal: "Number",
                    external: "Number",
                    name: "String" // e.g., 'web', 'api', 'websocket'
               }
          ],
          last_deployed: "Date",
          deployment_logs: [
               {
                    timestamp: "Date",
                    level: "String", // 'info', 'warn', 'error'
                    message: "String"
               }
          ]
     },

     // Git integration
     git: {
          repository_url: "String",
          branch: "String",
          commit_hash: "String",
          last_sync: "Date",
          sync_enabled: "Boolean"
     },

     // Collaboration features
     collaboration: {
          active_users: [
               {
                    user_id: "UUID",
                    username: "String",
                    cursor_position: {
                         file_id: "UUID",
                         line: "Number",
                         column: "Number"
                    },
                    selection: {
                         start: { line: "Number", column: "Number" },
                         end: { line: "Number", column: "Number" }
                    },
                    last_activity: "Date",
                    session_id: "String"
               }
          ],
          chat_history: [
               {
                    message_id: "UUID",
                    user_id: "UUID",
                    username: "String",
                    message: "String",
                    timestamp: "Date",
                    reply_to: "UUID" // optional, for threaded replies
               }
          ]
     },

     // Metadata
     template_id: "UUID", // if created from template
     is_public: "Boolean", // public workspace
     is_template: "Boolean", // can be used as template
     tags: ["String"], // for categorization

     // Timestamps
     created_at: "Date",
     updated_at: "Date",
     last_active: "Date", // last user activity

     // Analytics
     analytics: {
          total_files: "Number",
          total_size: "Number", // in bytes
          language_stats: {
               // language: line_count
          },
          activity_stats: {
               daily_active_users: "Number",
               total_edits: "Number",
               total_runs: "Number"
          }
     }
};

// Indexes for MongoDB workspace collection
const workspaceIndexes = [
     { ws_id: 1 }, // unique index
     { owner_id: 1 },
     { name: 1 },
     { "collaboration.active_users.user_id": 1 },
     { created_at: -1 },
     { last_active: -1 },
     { is_public: 1 },
     { tags: 1 }
];


/*
// Sample workspace document
const sampleWorkspace = {
     _id: "ObjectId('...')",
     ws_id: "550e8400-e29b-41d4-a716-446655440000",
     owner_id: "550e8400-e29b-41d4-a716-446655440001",
     name: "My React App",
     description: "A sample React application",

     settings: {
          language: "javascript",
          theme: "dark",
          font_size: 14,
          tab_size: 2,
          word_wrap: true,
          auto_save: true,
          auto_save_interval: 30
     },

     files: [
          {
               file_id: "550e8400-e29b-41d4-a716-446655440002",
               name: "package.json",
               path: "/package.json",
               type: "file",
               content: "{ \"name\": \"react-app\", \"version\": \"1.0.0\" }",
               size: 45,
               mime_type: "application/json",
               created_at: "2024-01-01T00:00:00Z",
               updated_at: "2024-01-01T00:00:00Z",
               parent_id: null,
               permissions: {
                    read: true,
                    write: true,
                    execute: false
               }
          }
     ],

     environment: {
          runtime: "node",
          version: "18.17.0",
          dependencies: [
               {
                    name: "react",
                    version: "18.2.0",
                    type: "npm"
               }
          ],
          environment_variables: {
               NODE_ENV: "development",
               PORT: "3000"
          },
          startup_command: "npm start",
          port: 3000
     },

     deployment: {
          status: "stopped",
          container_id: null,
          image_name: "node:18-alpine",
          ports: [
               {
                    internal: 3000,
                    external: 8080,
                    name: "web"
               }
          ],
          last_deployed: null,
          deployment_logs: []
     },

     collaboration: {
          active_users: [],
          chat_history: []
     },

     created_at: "2024-01-01T00:00:00Z",
     updated_at: "2024-01-01T00:00:00Z",
     last_active: "2024-01-01T00:00:00Z",

     analytics: {
          total_files: 1,
          total_size: 45,
          language_stats: {
               javascript: 10,
               json: 5
          },
          activity_stats: {
               daily_active_users: 1,
               total_edits: 0,
               total_runs: 0
          }
     }
};

module.exports = {
     workspaceSchema,
     workspaceIndexes,
     sampleWorkspace
}; 
*/
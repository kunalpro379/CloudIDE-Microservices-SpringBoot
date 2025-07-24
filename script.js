// Application State
let appState = {
    sessionId: null,
    userId: null,
    username: null,
    email: null,
    chatSocket: null,
    crdtSocket: null,
    isConnected: false,
    participants: new Map(),
    typingUsers: new Set()
};

// Chat WebSocket Client
class ChatClient {
    constructor() {
        this.socket = null;
        this.stompClient = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
    }

    connect() {
        const socket = new SockJS('/ws'); // Proxied through API Gateway/Nginx
        this.stompClient = Stomp.over(socket);
        
        this.stompClient.connect({}, 
            (frame) => this.onConnect(frame),
            (error) => this.onError(error)
        );
    }

    onConnect(frame) {
        console.log('Connected to Chat Service:', frame);
        appState.isConnected = true;
        updateConnectionStatus('connected');
        
        // Subscribe to session messages
        this.stompClient.subscribe(`/topic/chat/${appState.sessionId}`, (message) => {
            this.handleChatMessage(JSON.parse(message.body));
        });

        // Subscribe to typing indicators
        this.stompClient.subscribe(`/topic/chat/${appState.sessionId}/typing`, (message) => {
            this.handleTypingIndicator(JSON.parse(message.body));
        });

        // Subscribe to participant updates
        this.stompClient.subscribe(`/topic/chat/${appState.sessionId}/participants`, (message) => {
            this.handleParticipantUpdate(JSON.parse(message.body));
        });

        // Join session
        this.joinSession();
        this.reconnectAttempts = 0;
    }

    onError(error) {
        console.error('Chat WebSocket error:', error);
        appState.isConnected = false;
        updateConnectionStatus('disconnected');
        
        // Attempt reconnection
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            setTimeout(() => {
                this.reconnectAttempts++;
                console.log(`Reconnecting to chat... Attempt ${this.reconnectAttempts}`);
                this.connect();
            }, 3000);
        }
    }

    joinSession() {
        const joinMessage = {
            sessionId: appState.sessionId,
            userId: appState.userId,
            username: appState.username,
            email: appState.email
        };

        this.stompClient.send(`/app/chat/${appState.sessionId}/join`, {}, JSON.stringify(joinMessage));
    }

    sendMessage(content, messageType = 'TEXT') {
        if (!this.stompClient || !appState.isConnected) {
            console.warn('Not connected to chat service');
            return;
        }

        const message = {
            sessionId: appState.sessionId,
            userId: appState.userId,
            username: appState.username,
            content: content,
            messageType: messageType
        };

        this.stompClient.send(`/app/chat/${appState.sessionId}/send`, {}, JSON.stringify(message));
    }

    sendTypingIndicator(isTyping) {
        if (!this.stompClient || !appState.isConnected) return;

        const typingMessage = {
            sessionId: appState.sessionId,
            userId: appState.userId,
            username: appState.username,
            isTyping: isTyping
        };

        this.stompClient.send(`/app/chat/${appState.sessionId}/typing`, {}, JSON.stringify(typingMessage));
    }

    handleChatMessage(message) {
        console.log('Received chat message:', message);
        displayChatMessage(message);
    }

    handleTypingIndicator(typingInfo) {
        console.log('Typing indicator:', typingInfo);
        updateTypingIndicators(typingInfo);
    }

    handleParticipantUpdate(participantInfo) {
        console.log('Participant update:', participantInfo);
        updateParticipants(participantInfo);
    }

    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect();
            appState.isConnected = false;
            updateConnectionStatus('disconnected');
        }
    }
}

// CRDT Client (Collaborative Editing)
class CRDTClient {
    constructor() {
        this.socket = null;
        this.isConnected = false;
        this.documentState = '';
        this.operations = [];
    }

    connect() {
        // Connect to CRDT service via WebSocket
        this.socket = new WebSocket('/crdt/connect'); // Proxied through API Gateway/Nginx
        
        this.socket.onopen = () => {
            console.log('Connected to CRDT Service');
            this.isConnected = true;
            this.joinDocument();
        };

        this.socket.onmessage = (event) => {
            const message = JSON.parse(event.data);
            this.handleCRDTMessage(message);
        };

        this.socket.onclose = () => {
            console.log('Disconnected from CRDT Service');
            this.isConnected = false;
            setTimeout(() => this.connect(), 3000); // Reconnect
        };

        this.socket.onerror = (error) => {
            console.error('CRDT WebSocket error:', error);
        };
    }

    joinDocument() {
        const joinMessage = {
            type: 'JOIN_DOCUMENT',
            sessionId: appState.sessionId,
            userId: appState.userId,
            username: appState.username
        };
        
        this.socket.send(JSON.stringify(joinMessage));
    }

    sendOperation(operation) {
        if (!this.isConnected) return;

        const message = {
            type: 'OPERATION',
            sessionId: appState.sessionId,
            userId: appState.userId,
            operation: operation
        };

        this.socket.send(JSON.stringify(message));
    }

    handleCRDTMessage(message) {
        switch (message.type) {
            case 'DOCUMENT_STATE':
                this.applyDocumentState(message.content);
                break;
            case 'OPERATION':
                this.applyOperation(message.operation);
                break;
            case 'USER_CURSOR':
                this.updateUserCursor(message);
                break;
            default:
                console.log('Unknown CRDT message:', message);
        }
    }

    applyDocumentState(content) {
        const editor = document.getElementById('codeEditor');
        editor.value = content;
        this.documentState = content;
        updateEditorStatus('Document loaded');
    }

    applyOperation(operation) {
        // Apply collaborative editing operation
        const editor = document.getElementById('codeEditor');
        // Simple implementation - in production, use proper CRDT algorithms
        console.log('Applying operation:', operation);
        updateEditorStatus('Document updated');
    }

    disconnect() {
        if (this.socket) {
            this.socket.close();
            this.isConnected = false;
        }
    }
}

// Global clients
let chatClient = new ChatClient();
let crdtClient = new CRDTClient();

// DOM Event Handlers
document.addEventListener('DOMContentLoaded', function() {
    // Initialize login modal
    showLoginModal();

    // Chat input handlers
    const chatInput = document.getElementById('chatInput');
    const sendButton = document.getElementById('sendButton');
    const sendCodeButton = document.getElementById('sendCodeButton');
    
    let typingTimer;
    let isTyping = false;

    chatInput.addEventListener('input', () => {
        if (!isTyping) {
            isTyping = true;
            chatClient.sendTypingIndicator(true);
        }
        
        clearTimeout(typingTimer);
        typingTimer = setTimeout(() => {
            isTyping = false;
            chatClient.sendTypingIndicator(false);
        }, 1000);
    });

    chatInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            sendChatMessage();
        }
    });

    sendButton.addEventListener('click', sendChatMessage);
    sendCodeButton.addEventListener('click', sendCodeSnippet);

    // Code editor handlers
    const codeEditor = document.getElementById('codeEditor');
    let editorChangeTimer;

    codeEditor.addEventListener('input', () => {
        clearTimeout(editorChangeTimer);
        editorChangeTimer = setTimeout(() => {
            const content = codeEditor.value;
            const operation = {
                type: 'INSERT',
                position: codeEditor.selectionStart,
                content: content,
                timestamp: Date.now()
            };
            crdtClient.sendOperation(operation);
        }, 500);
    });

    // Login form handler
    const loginForm = document.getElementById('loginForm');
    loginForm.addEventListener('submit', handleLogin);

    // Chat controls
    document.getElementById('clearChat').addEventListener('click', clearChat);
    document.getElementById('toggleTyping').addEventListener('click', toggleTyping);
});

function showLoginModal() {
    document.getElementById('loginModal').style.display = 'flex';
}

function hideLoginModal() {
    document.getElementById('loginModal').style.display = 'none';
}

function handleLogin(e) {
    e.preventDefault();
    
    const username = document.getElementById('username').value;
    const email = document.getElementById('email').value;
    let sessionIdInput = document.getElementById('sessionIdInput').value;
    
    if (!username || !email) {
        alert('Please enter username and email');
        return;
    }

    // Generate user ID and session ID if needed
    appState.userId = generateUUID();
    appState.username = username;
    appState.email = email;
    appState.sessionId = sessionIdInput || generateUUID();

    // Update UI
    document.getElementById('sessionId').textContent = `Session: ${appState.sessionId}`;
    document.getElementById('userInfo').textContent = `User: ${username}`;

    hideLoginModal();

    // Connect to services
    chatClient.connect();
    crdtClient.connect();
}

function sendChatMessage() {
    const chatInput = document.getElementById('chatInput');
    const content = chatInput.value.trim();
    
    if (!content) return;

    chatClient.sendMessage(content, 'TEXT');
    chatInput.value = '';
}

function sendCodeSnippet() {
    const codeEditor = document.getElementById('codeEditor');
    const selectedText = codeEditor.value.substring(
        codeEditor.selectionStart, 
        codeEditor.selectionEnd
    ) || codeEditor.value;

    if (!selectedText.trim()) {
        alert('Please select some code or add code to the editor');
        return;
    }

    chatClient.sendMessage(selectedText, 'CODE_SNIPPET');
}

function displayChatMessage(message) {
    const chatMessages = document.getElementById('chatMessages');
    
    const messageElement = document.createElement('div');
    messageElement.className = 'chat-message';
    
    const messageHeader = document.createElement('div');
    messageHeader.className = 'message-header';
    
    const username = document.createElement('span');
    username.className = 'message-username';
    username.textContent = message.username;
    
    const time = document.createElement('span');
    time.className = 'message-time';
    time.textContent = new Date(message.createdAt || Date.now()).toLocaleTimeString();
    
    messageHeader.appendChild(username);
    messageHeader.appendChild(time);
    
    const messageContent = document.createElement('div');
    messageContent.className = `message-content ${message.messageType?.toLowerCase().replace('_', '-') || ''}`;
    messageContent.textContent = message.content;
    
    messageElement.appendChild(messageHeader);
    messageElement.appendChild(messageContent);
    
    chatMessages.appendChild(messageElement);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function updateTypingIndicators(typingInfo) {
    const typingIndicators = document.getElementById('typingIndicators');
    
    if (typingInfo.isTyping && typingInfo.userId !== appState.userId) {
        appState.typingUsers.add(typingInfo.username);
    } else {
        appState.typingUsers.delete(typingInfo.username);
    }
    
    if (appState.typingUsers.size > 0) {
        const typingArray = Array.from(appState.typingUsers);
        let typingText = '';
        
        if (typingArray.length === 1) {
            typingText = `${typingArray[0]} is typing...`;
        } else if (typingArray.length === 2) {
            typingText = `${typingArray[0]} and ${typingArray[1]} are typing...`;
        } else {
            typingText = `${typingArray.slice(0, -1).join(', ')} and ${typingArray[typingArray.length - 1]} are typing...`;
        }
        
        typingIndicators.innerHTML = `<span class="typing-indicator">${typingText}</span>`;
    } else {
        typingIndicators.innerHTML = '';
    }
}

function updateParticipants(participantInfo) {
    const participantsList = document.getElementById('participantsList');
    const activeUsers = document.getElementById('activeUsers');
    
    if (participantInfo.type === 'JOIN') {
        appState.participants.set(participantInfo.userId, participantInfo);
    } else if (participantInfo.type === 'LEAVE') {
        appState.participants.delete(participantInfo.userId);
    }
    
    // Update participants list
    participantsList.innerHTML = '';
    activeUsers.innerHTML = '';
    
    appState.participants.forEach((participant) => {
        // Participants panel
        const participantElement = document.createElement('div');
        participantElement.className = `participant ${participant.isOnline ? 'online' : ''}`;
        
        const avatar = document.createElement('div');
        avatar.className = 'participant-avatar';
        avatar.textContent = participant.username.charAt(0).toUpperCase();
        
        const info = document.createElement('div');
        info.className = 'participant-info';
        
        const name = document.createElement('div');
        name.className = 'participant-name';
        name.textContent = participant.username;
        
        const status = document.createElement('div');
        status.className = 'participant-status';
        status.textContent = participant.isOnline ? 'Online' : 'Offline';
        
        info.appendChild(name);
        info.appendChild(status);
        
        participantElement.appendChild(avatar);
        participantElement.appendChild(info);
        
        participantsList.appendChild(participantElement);
        
        // Active users in header
        if (participant.isOnline) {
            const userAvatar = document.createElement('div');
            userAvatar.className = 'user-avatar';
            userAvatar.textContent = participant.username.charAt(0).toUpperCase();
            userAvatar.title = participant.username;
            activeUsers.appendChild(userAvatar);
        }
    });
}

function updateConnectionStatus(status) {
    const connectionStatus = document.getElementById('connectionStatus');
    connectionStatus.textContent = status.charAt(0).toUpperCase() + status.slice(1);
    connectionStatus.className = `status ${status}`;
}

function updateEditorStatus(message) {
    const editorStatus = document.getElementById('editorStatus');
    editorStatus.textContent = message;
    
    setTimeout(() => {
        editorStatus.textContent = 'Ready';
    }, 3000);
}

function clearChat() {
    document.getElementById('chatMessages').innerHTML = '';
}

function toggleTyping() {
    // Demo function for testing typing indicators
    chatClient.sendTypingIndicator(Math.random() > 0.5);
}

// Utility functions
function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        const r = Math.random() * 16 | 0;
        const v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

// Handle page unload
window.addEventListener('beforeunload', () => {
    if (chatClient.stompClient) {
        chatClient.disconnect();
    }
    if (crdtClient.socket) {
        crdtClient.disconnect();
    }
});

// Handle page visibility for connection management
document.addEventListener('visibilitychange', () => {
    if (document.hidden) {
        // Page is hidden, can pause some operations
        console.log('Page hidden');
    } else {
        // Page is visible, ensure connections are active
        console.log('Page visible');
        if (!appState.isConnected && appState.sessionId) {
            chatClient.connect();
            crdtClient.connect();
        }
    }
});

// Export for debugging
window.appState = appState;
window.chatClient = chatClient;
window.crdtClient = crdtClient;

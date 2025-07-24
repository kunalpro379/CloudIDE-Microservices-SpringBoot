class CollaborativeEditor {
     constructor() {
          this.socket = null;
          this.username = '';
          // Use a fixed document ID for all users in testing
          this.documentId = 'default';

          // DOM Elements
          this.usernameInput = document.getElementById('username');
          this.joinBtn = document.getElementById('join-btn');
          this.editorContainer = document.getElementById('editor-container');
          this.editor = document.getElementById('collaborative-editor');
          this.userList = document.getElementById('user-list');
          this.boldBtn = document.getElementById('bold-btn');
          this.italicBtn = document.getElementById('italic-btn');

          this.setupEventListeners();
     }

     setupEventListeners() {
          this.joinBtn.addEventListener('click', () => this.joinDocument());
          this.editor.addEventListener('input', (e) => this.handleLocalEdit(e));
          this.boldBtn.addEventListener('click', () => this.applyFormatting('bold'));
          this.italicBtn.addEventListener('click', () => this.applyFormatting('italic'));
     }

     joinDocument() {
          this.username = this.usernameInput.value.trim();
          if (!this.username) {
               alert('Please enter a username');
               return;
          }

          // Establish WebSocket connection
          // Use current host and port dynamically
          const wsUrl = `${window.location.protocol.replace('http','ws')}//${window.location.host}/collaborative-editing`;
          this.socket = new WebSocket(wsUrl);

          this.socket.onopen = () => {
               // Send join message
               this.socket.send(JSON.stringify({
                    type: 'JOIN',
                    username: this.username,
                    documentId: this.documentId || null
               }));

               // Show editor
               this.editorContainer.style.display = 'block';
          };

          this.socket.onmessage = (event) => {
               const message = JSON.parse(event.data);
               this.handleServerMessage(message);
          };

          this.socket.onerror = (error) => {
               console.error('WebSocket Error:', error);
               alert('Connection error. Please try again.');
          };
     }

     handleServerMessage(message) {
          switch (message.type) {
               case 'DOCUMENT_INIT':
                    this.documentId = message.documentId;
                    this.editor.value = message.content;
                    this.updateUserList(message.users);
                    break;
               case 'USER_LIST_UPDATE':
                    this.updateUserList(message.users);
                    break;
               case 'REMOTE_EDIT':
                    this.applyRemoteEdit(message);
                    break;
          }
     }

     handleLocalEdit(event) {
          if (!this.socket) return;

          const content = event.target.value;
          const position = event.target.selectionStart;

          // Send edit to server
          this.socket.send(JSON.stringify({
               type: 'LOCAL_EDIT',
               username: this.username,
               documentId: this.documentId,
               content: content,
               position: position
          }));
     }

     applyRemoteEdit(editMessage) {
          // Implement CRDT merge logic
          // This is a simplified version and would need more complex CRDT implementation
          if (editMessage.username !== this.username) {
               this.editor.value = editMessage.content;
          }
     }

     applyFormatting(type) {
          const selectedText = window.getSelection().toString();
          if (!selectedText) return;

          // Send formatting request to server
          this.socket.send(JSON.stringify({
               type: 'FORMATTING',
               username: this.username,
               documentId: this.documentId,
               formatting: type,
               selectedText: selectedText
          }));
     }

     updateUserList(users) {
          this.userList.innerHTML = '';
          users.forEach(user => {
               const li = document.createElement('li');
               li.textContent = user;
               this.userList.appendChild(li);
          });
     }
}

// Initialize the collaborative editor
new CollaborativeEditor();
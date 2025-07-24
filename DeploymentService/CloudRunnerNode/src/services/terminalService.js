const pty = require('node-pty');
const os = require('os');

class TerminalService {
     constructor() {
          this.terminals = new Map();
     }

     createTerminal(terminalId, workingDirectory = '/app') {
          if (this.terminals.has(terminalId)) {
               return this.terminals.get(terminalId);
          }

          // Determine shell based on OS
          const shell = os.platform() === 'win32' ? 'powershell.exe' : 'bash';

          const terminal = pty.spawn(shell, [], {
               name: 'xterm-color',
               cols: 80,
               rows: 24,
               cwd: workingDirectory,
               env: {
                    ...process.env,
                    TERM: 'xterm-256color',
                    COLORTERM: 'truecolor'
               }
          });

          // Store terminal reference
          this.terminals.set(terminalId, terminal);

          // Handle terminal exit
          terminal.onExit(() => {
               this.terminals.delete(terminalId);
               console.log(`Terminal ${terminalId} exited`);
          });

          console.log(`Terminal ${terminalId} created with PID ${terminal.pid}`);
          return terminal;
     }

     getTerminal(terminalId) {
          return this.terminals.get(terminalId);
     }

     killTerminal(terminalId) {
          const terminal = this.terminals.get(terminalId);
          if (terminal) {
               terminal.kill();
               this.terminals.delete(terminalId);
               console.log(`Terminal ${terminalId} killed`);
               return true;
          }
          return false;
     }

     resizeTerminal(terminalId, cols, rows) {
          const terminal = this.terminals.get(terminalId);
          if (terminal) {
               terminal.resize(cols, rows);
               console.log(`Terminal ${terminalId} resized to ${cols}x${rows}`);
               return true;
          }
          return false;
     }

     listTerminals() {
          return Array.from(this.terminals.keys());
     }

     killAllTerminals() {
          this.terminals.forEach((terminal, terminalId) => {
               terminal.kill();
               console.log(`Terminal ${terminalId} killed during cleanup`);
          });
          this.terminals.clear();
     }
}

// Export singleton instance
const terminalService = new TerminalService();

module.exports = {
     createTerminal: (terminalId, workingDirectory) =>
          terminalService.createTerminal(terminalId, workingDirectory),
     getTerminal: (terminalId) =>
          terminalService.getTerminal(terminalId),
     killTerminal: (terminalId) =>
          terminalService.killTerminal(terminalId),
     resizeTerminal: (terminalId, cols, rows) =>
          terminalService.resizeTerminal(terminalId, cols, rows),
     listTerminals: () =>
          terminalService.listTerminals(),
     killAllTerminals: () =>
          terminalService.killAllTerminals()
}; 
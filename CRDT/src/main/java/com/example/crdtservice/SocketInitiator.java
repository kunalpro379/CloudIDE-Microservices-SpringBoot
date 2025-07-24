
package com.example.codeeditorservice;

// import com.corundumstudio.socketio.SocketIOServer; // Uncomment if you have this dependency
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Slf4j
public class SocketInitiator implements CommandLineRunner {
    // private final SocketIOServer server; // Uncomment if you have this dependency
    // @Autowired
    // public SocketInitiator(SocketIOServer server) {
    // this.server = server;
    // }
    @Override
    public void run(String... args) throws Exception {
        // if (server != null) server.start(); // Uncomment if you have this dependency
    }
}
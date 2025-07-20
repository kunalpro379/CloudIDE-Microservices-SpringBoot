
package com.example.codeeditorservice;
import com.corundumstudio.socketio.SocketIOServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@slf4j
public class SocketInitiator implements  CommandLineRunner{
    //ComandlineRunner is a spring boot callback interface that can be used to execute code after the application context is loaded.
    //server start hotehi socket bhi start hoga
    private final SocketIOServer server;
    @Autowired
    public  SocketInitiator(SocketIOServer server){
        this.server=server;
    }
    @Override
    public void run(Sring... args) throws Exception{
        server.start();
    }



}
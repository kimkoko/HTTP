/*
    COMP2322 Project
    Kim Songi 19085436D

    This project is to develop a multi-threaded web server.
    I took references from
    https://www2.seas.gwu.edu/~cheng/6431/Projects/Project1WebServer/webserver.html and
    Lab 2 and Lab 3 materials of COMP2322

 */

import java.io.*;
import java.net.*;
import java.util.*;

public class HTTP {

    public static void main(String[] args) throws Exception{
        //listen socket with the port number 5600
        ServerSocket listenSocket =  new ServerSocket(5600);

        //make new logfile.txt in the current directory everytime it starts the connection
        FileWriter logFile = new FileWriter("logfile.txt");

        //infinite loop enabling multiple threads
        while (true) {
            //TCP connection
            Socket socket = listenSocket.accept();

            //HTTPConnection method to process the request message
            HTTPConnection connection = new HTTPConnection(socket);

            //thread starts
            connection.start();

        }
    }
}

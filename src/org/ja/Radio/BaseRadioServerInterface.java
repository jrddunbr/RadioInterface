
package org.ja.Radio;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author jared
 */
public class BaseRadioServerInterface {

    private static final int port = 80;
    private static int number;
    private static final String buttonCode = "<form action=\".\" method=\"get\">\n" +
"            <input type=\"hidden\" name=\" \" value=\"`\"><br>\n" +
"            <input type=\"submit\" value=\"~\" id=\"submit\">\n" +
"        </form>\n";
    private static ArrayList<String> songs;
    
    /** Main
     * 
     * The main program..
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Thread run = new Thread(new Runnable() {

            @Override
            public void run() {
                while(true) {
                    try{
                        runHTTP(port);
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        run.start();
        Scanner reader = new Scanner(System.in);
        while(true) {
            if(reader.hasNext()) {
                System.exit(0);
            }
        }
    }
    /** runHTTP()
     * 
     * Runs a mini HTTP server for web interface
     * 
     * @param port the server port to use for the web interface
     * @throws IOException 
     */
    private static void runHTTP(int port) throws IOException {
        ServerSocket socket = new ServerSocket(8080);
        while(true) {
            Socket accept = socket.accept();
            Scanner in = new Scanner(accept.getInputStream());
            PrintStream out = new PrintStream(accept.getOutputStream());
            String command = "";
            if(in.hasNextLine()) {
                command = in.nextLine();
            }
            System.out.println("[" + command + "]");
            if(command.isEmpty()) {
                accept.close();
            }else{
                String mode = command.substring(0, command.indexOf(" "));
                String path = command.substring(command.indexOf(" ") + 1, command.indexOf(" ", command.indexOf(" ") + 1));
                System.out.println(mode + " " + path);
                
                if(path.equals("/?+=1")) {
                    number++;
                }else if(path.equals("/?+=-1")) {
                    number--;
                }else{
                    
                }
                
                String message = 
"<!DOCTYPE html>\n" +
"<html>\n" +
"    <head>\n" +
"        <meta charset=\"UTF-8\">\n" +
"        <title></title>\n" +
"<style type=\"text/css\">#submit {width:120px; height:120px; border:none;background:blue;color:white}</style>" +                        
"    </head>\n" +
"    <body>\n" + "~" +
"    </body>\n" +
"</html>\n";
                String songs = "";
                if(number > 0) {
                    for(int i = 0; i < number; i++) {
                        songs += "<div>" + (i + 1) + "</div>";
                    }
                }
                message = message.replaceAll("~", songs);
                if(path.equals("/")) {
                    out.println("HTTP/1.1 200 OK");
                out.println("Connection: close");
                out.println("Content-Type: text/html");
                out.println("Content-Length: " + message.length());
                out.println();
                out.println(message);
                }else{
                    out.println("HTTP/1.1 302 Found");
                    out.println("Location: /");
                }
                
                
                accept.close();
            }
        }
    }
}


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

    private static final int PORT = 8080;
    private static final String BUTTON_CODE = "<form action=\".\" method=\"get\">\n" +
"            <input type=\"hidden\" name=\" \" value=\"`\"><br>\n" +
"            <input type=\"submit\" value=\"~\" id=\"submit\">\n" +
"        </form>\n";
    private static ArrayList<String> songs;
    private static final String BASE_MESSAGE = "<!DOCTYPE html>\n" +
"<html>\n" +
"    <head>\n" +
"        <meta charset=\"UTF-8\">\n" +
"        <title></title>\n" +
"<style type=\"text/css\">#submit {width:120px; height:120px; border:none;background:blue;color:white}</style>" +                        
"    </head>\n" +
"    <body>\n" + "~" +
"    </body>\n" +
"</html>\n";
    private static final String ROOT_RESPONSE_URL = "/?+=";
    
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
                        runHTTP(PORT);
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
            String response = "";
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
                
                if(path.equals(ROOT_RESPONSE_URL + "")) {
                    //what to do with that url
                }
                
                response = response.replaceAll("~", "");
                
                /* 
If we are in the root action directory (or approved directory),
then return some info, otherwise do a 302 redirect to known territory

Typically we leave known territory when making requests, and they
bump us right back to the root thanks to the 302. The browser gives us the
ability to collect data thanks to the redirect since it calls a function
from the path that we specified in the initial redirect link that we click.
                */
                if(path.equals("/")) {
                    out.println("HTTP/1.1 200 OK");
                    out.println("Connection: close");
                    out.println("Content-Type: text/html");
                    out.println("Content-Length: " + response.length());
                    out.println();
                    out.println(response);
                }else if(path.equals("/favicon.ico")) {
                    //well browsers like to get favicons so let's just not.
                    out.println("HTTP/1.1 400 NOT FOUND");
                }else{
                    out.println("HTTP/1.1 302 Found");
                    out.println("Location: /");
                }
                
                
                accept.close();
            }
        }
    }
}

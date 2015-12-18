
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
    private static final String BUTTON_CODE = 
"<a href=\"~\" class=\"btn\">`</a>\n";
    //button code from http://css3buttongenerator.com/
    private static ArrayList<String> songs;
    private static final String BASE_MESSAGE = "<!DOCTYPE html>\n" +
"<html>\n" +
"    <head>\n" +
"        <meta charset=\"UTF-8\">\n" +
"        <title></title>\n" +   
"        <style>.btn {\n" +
"  -webkit-border-radius: 4;\n" +
"  -moz-border-radius: 4;\n" +
"  border-radius: 4px;\n" +
"  color: #ffffff;\n" +
"  font-size: 20px;\n" +
"  background: #6cc0f7;\n" +
"  padding: 10px 20px 10px 20px;\n" +
"  border: solid #4893c2 2px;\n" +
"  text-decoration: none;\n" +
"}\n" +
"\n" +
".btn:hover {\n" +
"  background: #3cb0fd;\n" +
"  text-decoration: none;\n" +
"}\n" + "</style>" +
"    </head>\n" +
"    <body>\n" + "<div style=\"padding: 20px 10px 20px 10px;\">~</div>" +
"    </body>\n" +
"</html>\n";
    
    /** Main
     * 
     * The main program..
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        songs = new ArrayList<>();
        songs.add("Hello World");
        songs.add("123");
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
            String response = BASE_MESSAGE;
            if(in.hasNextLine()) {
                command = in.nextLine();
            }
            //System.out.println("[" + command + "]");//raw headers from browser
            if(command.isEmpty()) {
                System.out.println("Closing Connection - no data.");
                accept.close();
            }else{
                String mode = command.substring(0, command.indexOf(" "));
                String path = command.substring(command.indexOf(" ") + 1, command.indexOf(" ", command.indexOf(" ") + 1));
                path = path.substring(1).trim();
                //print out anything that is a command.
                if(!path.equalsIgnoreCase("favicon.ico") && !path.isEmpty()) {
                    System.out.println(mode + " " + path);
                }
                
                //deal with command inputs! Yay!
                if(path.equals("123")) {
                    System.out.println("Yay!");
                }
                
                String output = "";
                
                for(String a:songs) {
                    //I found not to use | for this. It gets nasty.. ` and ~ work though just fine!
                    output += BUTTON_CODE.replaceAll("~", a).replaceAll("`", a);
                }

                response = response.replaceAll("~", output);
                
                /* 
If we are in the root action directory (or approved directory),
then return some info, otherwise do a 302 redirect to known territory

Typically we leave known territory when making requests, and they
bump us right back to the root thanks to the 302. The browser gives us the
ability to collect data thanks to the redirect since it calls a function
from the path that we specified in the initial redirect link that we click.
                */
                if(path.isEmpty()) {
                    //basic HTTP response with success. Makes the browser happy.
                    out.println("HTTP/1.1 200 OK");
                    out.println("Connection: close");
                    out.println("Content-Type: text/html");
                    out.println("Content-Length: " + response.length());
                    out.println();
                    out.println(response);
                }else if(path.equals("favicon.ico")) {
                    //well, browsers like to get favicons so let's just not.
                    out.println("HTTP/1.1 400 NOT FOUND");
                }else{
                    //redirect to the root directory within the browser,
                    //the user doesn't see anything.
                    out.println("HTTP/1.1 302 Found");
                    out.println("Location: /");
                }
                
                
                accept.close();
            }
        }
    }
}

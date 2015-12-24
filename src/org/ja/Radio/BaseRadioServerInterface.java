package org.ja.Radio;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author jared
 */
public class BaseRadioServerInterface {

    private static double FREQ = 103.3;
    private static String converter = "";
    private static final int PORT = 80;
    private static final String BUTTON_CODE
            = "<a href=\"~\" class=\"btn\">`</a>\n";
    //button code from http://css3buttongenerator.com/
    private static ArrayList<String> songs;
    private static final String BASE_MESSAGE = "<!DOCTYPE html>\n"
            + "<html>\n"
            + "    <head>\n"
            + "        <meta charset=\"UTF-8\">\n"
            + "        <title></title>\n"
            + "        <style>.btn {\n"
            + "  -webkit-border-radius: 4;\n"
            + "  -moz-border-radius: 4;\n"
            + "  border-radius: 4px;\n"
            + "  color: #ffffff;\n"
            + "  font-size: 20px;\n"
            + "  background: #6cc0f7;\n"
            + "  padding: 10px 20px 10px 20px;\n"
            + "  border: solid #4893c2 2px;\n"
            + "  text-decoration: none;\n"
            + "}\n"
            + "\n"
            + ".btn:hover {\n"
            + "  background: #3cb0fd;\n"
            + "  text-decoration: none;\n"
            + "}\n" + "</style>"
            + "    </head>\n"
            + "    <body>\n" + "<div style=\"padding: 20px 10px 20px 10px;\">~</div>"
            + "    </body>\n"
            + "</html>\n";

    /**
     * Main
     *
     * The main program..
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 1) {
            try {
                double fr = Double.parseDouble(args[0]);
                //make sure it's a legal US frequency.
                if ((fr < 107.9) && (fr > 87.9)) {
                    FREQ = fr;
                }
            } catch (Exception e) {
                System.err.println("Invalid Args! Enter a double between 87.5 and 108.0");
            }
        }
        try {
            if (!checkForConverter()) {
                System.err.println("Error - cannot find a supported converter.");
                System.exit(1);
            }
        } catch (Exception e) {
            //this should not really happen...
        }
        songs = new ArrayList<>();
        findMusic();
        Thread run = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        runHTTP(PORT);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        run.start();
        Scanner reader = new Scanner(System.in);
        while (true) {
            if (reader.hasNext()) {
                System.exit(0);
            }
        }
    }

    /**
     * runHTTP()
     *
     * Runs a mini HTTP server for web interface
     *
     * @param port the server port to use for the web interface
     * @throws IOException
     */
    private static void runHTTP(int port) throws IOException {
        ServerSocket socket = new ServerSocket(PORT);
        while (true) {
            Socket accept = socket.accept();
            Scanner in = new Scanner(accept.getInputStream());
            PrintStream out = new PrintStream(accept.getOutputStream());
            String command = "";
            String response = BASE_MESSAGE;
            if (in.hasNextLine()) {
                command = in.nextLine();
            }
            //System.out.println("[" + command + "]");//raw headers from browser
            if (command.isEmpty()) {
                System.out.println("Closing Connection - no data.");
                accept.close();
            } else {
                String mode = command.substring(0, command.indexOf(" "));
                String path = command.substring(command.indexOf(" ") + 1, command.indexOf(" ", command.indexOf(" ") + 1));
                path = path.substring(1).trim();
                //print out anything that is a command.
                if (!path.equalsIgnoreCase("favicon.ico") && !path.isEmpty()) {
                    System.out.println(mode + " " + path);
                }

                //deal with command inputs! Yay!
                for (String a : songs) {
                    if (path.equalsIgnoreCase(a)) {
                        try {
                            doMusic(a);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (path.equalsIgnoreCase("stop")) {
                    try {
                        stopMusic();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                if (path.equalsIgnoreCase("refresh")) {
                    findMusic();
                }

                String output = "";
                for (String a : songs) {
                    //I found not to use | for this. It gets nasty.. ` and ~ work though just fine!
                    output += BUTTON_CODE.replaceAll("~", a).replaceAll("`", a);
                    output += "<br/><br/><br/>";
                }

                output += BUTTON_CODE.replaceAll("~", "stop").replaceAll("`", "stop");
                output += BUTTON_CODE.replaceAll("~", "refresh").replaceAll("`", "refresh");

                response = response.replaceAll("~", output);

                /* 
If we are in the root action directory (or approved directory),
then return some info, otherwise do a 302 redirect to known territory

Typically we leave known territory when making requests, and they
bump us right back to the root thanks to the 302. The browser gives us the
ability to collect data thanks to the redirect since it calls a function
from the path that we specified in the initial redirect link that we click.
                 */
                if (path.isEmpty()) {
                    //basic HTTP response with success. Makes the browser happy.
                    out.println("HTTP/1.1 200 OK");
                    out.println("Connection: close");
                    out.println("Content-Type: text/html");
                    out.println("Content-Length: " + response.length());
                    out.println();
                    out.println(response);
                } else if (path.equals("favicon.ico")) {
                    //well, browsers like to get favicons so let's just not.
                    out.println("HTTP/1.1 400 NOT FOUND");
                } else {
                    //redirect to the root directory within the browser,
                    //the user doesn't see anything.
                    out.println("HTTP/1.1 302 Found");
                    out.println("Location: /");
                }

                accept.close();
            }
        }
    }

    /**
     * checkForConverter
     *
     * Checks to see if a converter is available to convert the audio.
     *
     * Currently only checks for avconv since that's all that runs on the pi, I
     * don't think I can get ffmpeg for it, besides from source.
     *
     * @return If a conversion tool exists, returns true and sets the variable
     * with the prefered software.
     */
    private static boolean checkForConverter() throws IOException, InterruptedException {
        ProcessBuilder build = new ProcessBuilder("avconv");
        Process start = build.start();
        Scanner reader = new Scanner(start.getErrorStream());
        String parse = "";
        if (reader.hasNextLine()) {
            parse = reader.nextLine();
        }
        System.out.println(parse);
        if (parse.contains("avconv")) {
            converter = "avconv";
            return true;
        }
        return false;
    }

    private static void doMusic(String path) throws IOException, InterruptedException {
        String shFile = path.substring(0, path.lastIndexOf(".")) + ".sh";
        try {
            PrintWriter writer = new PrintWriter(new File(shFile));
            writer.println("avconv -i 'music/" + path + "' -ac 1 -ar 22050 -b 352k -f wav - | sudo ./pifm - " + FREQ);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Making sh file at " + shFile);
        ProcessBuilder songPlayer;
        songPlayer = new ProcessBuilder("sh", shFile);
        songPlayer.start();
    }

    private static void stopMusic() throws IOException, InterruptedException {
        ProcessBuilder songStopper = new ProcessBuilder("pkill", "avconv");
        songStopper.start();
    }

    private static void findMusic() {
        File dir = new File("music");
        if (dir.isDirectory()) {
            songs.clear();
            for (File f : dir.listFiles()) {
                songs.add(f.getName());
            }
        }
    }
}

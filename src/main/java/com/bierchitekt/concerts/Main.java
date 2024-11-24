package com.bierchitekt.concerts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String... args) throws IOException, InterruptedException {

        Runtime rt = Runtime.getRuntime();
        String[] commands = {"/home/bierchitekt/dev/concerts/feierwerk-downloader.sh"};

        Process proc = rt.exec(commands);

        if(!proc.waitFor(1, TimeUnit.SECONDS)) {
            //timeout - kill the process.
            proc.destroy(); // consider using destroyForcibly instead
        }

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(proc.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(proc.getErrorStream()));

// Read the output from the command
        System.out.println("Here is the standard output of the command:\n");
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

// Read any errors from the attempted command
        System.out.println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
    }
}

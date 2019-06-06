import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Executor {

    public static String executeWith(Runtime runtime, String command) {
        return execute(runtime, command);
    }

    private static String execute(Runtime runtime, String command) {
        String[] commands = new String[3];
        commands[0] = "/bin/bash";
        commands[1] = "-c";
        commands[2] = command;
        try {
            InputStream is = runtime.exec(commands).getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader buff = new BufferedReader(isr);
            String st;
            StringBuilder stringBuilder = new StringBuilder();
            while ((st = buff.readLine()) != null)
                stringBuilder.append(st + "\n");
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String execute(String command) {
        return execute(Runtime.getRuntime(), command);
    }

}

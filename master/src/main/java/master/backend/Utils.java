package master.backend;

import master.Params;
import me.myself.grpc.Task;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class Utils {


    public static LinkedList<Task> loadPreviousTasks() throws Exception {

        LinkedList<Task> tasks = new LinkedList<>();

        String content = readFile( Params.savefileFilename);

        try{
            JSONObject json = new JSONObject(content);
        } catch(JSONException e){
            System.err.println("ERROR: error in JSON file " + Params.savefileFilename + "\n\n" + e);
            throw e;
        }

        return tasks;
    }

    private static String readFile(String filename) throws Exception {

        StringBuilder content;

        try (BufferedReader file = new BufferedReader(new FileReader( filename ))) {

            content = new StringBuilder();
            String line;
            while ((line = file.readLine()) != null)
                content.append(line);

        } catch (FileNotFoundException e) {
            System.err.println("ERROR: file " + filename + " was not found.\n\n" + e);
            throw e;
        } catch (IOException e) {
            System.err.println("ERROR: while reading the file " + filename + "\n\n" + e);
            throw e;
        }

        return content.toString();
    }
}

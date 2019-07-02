package nl.zakarias.constellation.edgeinference.modelServing;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class API {

    public static class Content{
        String signature_name;
        int[][] instances;

        private Content(String signature_string, byte[][] image){
            this.signature_name = signature_string;

            instances = new int[image.length][image[0].length];

            for (int i=0; i<image.length; i++){
                for(int x=0; x<image[i].length; x++){
                    instances[i][x] = image[i][x] & 0xff; // Convert to int
                }
            }
        }
    }


    private static String responseError(HttpURLConnection con, int code) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder result = new StringBuilder();

        result.append(String.format("Response code %d \n", code));

        while ((inputLine = in.readLine()) != null) {
            result.append(inputLine);
        }
        in.close();

        return result.toString();
    }

    public static String predict(int port, String modelName, int version, byte[][] image) throws Exception {
        URL url = new URL("http://localhost:" + port + "/v1/models/" + modelName + ":predict");
        if (version > 0){
            url = new URL("http://localhost:" + port + "/v1/models/" + modelName + "/versions/" + version + ":predict");
        }
        Content data = new Content("predict_images", image);
        Gson gson = new Gson();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type","application/json");

        connection.setDoOutput(true);
        connection.setDoInput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(gson.toJson(data));
        wr.flush();
        wr.close();

        int responseCode = connection.getResponseCode();

        if (!(responseCode == 200 || responseCode == 202)) {
            throw new Error(responseError(connection, responseCode));
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder result = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            result.append(inputLine);
        }
        in.close();

        return result.toString();
    }

    public static String getStatus(int port, String modelName, int version) throws Exception {
        StringBuilder result = new StringBuilder();

        URL url = new URL("http://localhost:" + port + "/v1/models/" + modelName);
        if (version > 0){
            url = new URL("http://localhost:" + port + "/v1/models/" + modelName + "/versions/" + version);
        }
        URLConnection connection = url.openConnection();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            result.append(inputLine);
        in.close();

        return result.toString();
    }

    public static String getModelMetadata(int port, String modelName, int version) throws Exception {
        StringBuilder result = new StringBuilder();

        URL url = new URL("http://localhost:" + port + "/v1/models/" + modelName + "/metadata");
        if (version > 0){
            url = new URL("http://localhost:" + port + "/v1/models/" + modelName + "/versions/" + version + "/metadata");
        }
        URLConnection connection = url.openConnection();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            result.append(inputLine);
        in.close();

        return result.toString();
    }
}

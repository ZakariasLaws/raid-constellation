package nl.zakarias.constellation.edgeinference.modelServing;

import com.google.gson.Gson;

import javax.net.ssl.HttpsURLConnection;
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
        byte[][] instances;

        private void createInstances(byte[][] image){
            instances = new byte[image.length][784];
            for(int i=0; i<image.length; i++){
                System.arraycopy(image[0], 0, instances[i], 0, 784);
            }
        }

        private Content(String signature_string, byte[][] image){
            this.signature_name = signature_string;
            createInstances(image);
        }
    }


    private static String handleResponse(HttpURLConnection con){
        return "";
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
        String test = gson.toJson(data);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(test);
        wr.flush();
        wr.close();

        int responseCode = connection.getResponseCode();

        // TODO Extend this to cover more codes and display more meaningful errors
        if (responseCode == 404){
            throw new Error("Received 404 on TF serving predict with model " + modelName + "/" + version);
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
//        GET http://host:port/v1/models/${MODEL_NAME}[/versions/${MODEL_VERSION}]/metadata

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

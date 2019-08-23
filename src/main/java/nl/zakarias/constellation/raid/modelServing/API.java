package nl.zakarias.constellation.raid.modelServing;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * An API allowing the user to interface with the TensorFlow model server, in order to make predictions.
 */
public class API {

    /**
     * Inner class used for automatic JSON <-> String conversion and access
     */
    interface Content {}

    public static class Content_single implements Content{
        String signature_name;
        int[][] instances;

        private Content_single(String signature_string, byte[][] image){
            this.signature_name = signature_string;

            instances = new int[image.length][image[0].length];

            for (int i=0; i<image.length; i++){
                for(int x=0; x<image[i].length; x++){
                    instances[i][x] = image[i][x] & 0xff; // Convert to int
                }
            }
        }
    }

    public static class Content_1D implements Content{
        String signature_name;
        int[][] instances;

        private Content_1D(String signature_string, byte[][] image){
            this.signature_name = signature_string;

            instances = new int[image.length][image[0].length];

            for (int batch=0; batch<image.length; batch++){
                for(int row=0; row<image[0].length; row++){
                    instances[batch][row] = image[batch][row] & 0xff; // Convert to int
                }
            }
        }
    }

    public static class Content_2D implements Content{
        String signature_name;
        int[][][] instances;

        private Content_2D(String signature_string, byte[][][] image){
            this.signature_name = signature_string;

            instances = new int[image.length][image[0].length][image[0][0].length];

            for (int batch=0; batch<image.length; batch++){
                for(int row=0; row<image[0].length; row++){
                    for(int col=0; col<image[0][0].length; col++) {
                            instances[batch][row][col] = image[batch][row][col] & 0xff; // Convert to int
                        }
                }
            }
        }
    }

    public static class Content_3D implements Content{
        String signature_name;
        int[][][][] instances;

        private Content_3D(String signature_string, byte[][][][] image){
            this.signature_name = signature_string;

            instances = new int[image.length][image[0].length][image[0][0].length][image[0][0][0].length];

            for (int batch=0; batch<image.length; batch++){
                for(int row=0; row<image[0].length; row++){
                    for(int col=0; col<image[0][0].length; col++) {
                        for(int inner=0; inner<image[0][0][0].length; inner++) {
                            instances[batch][row][col][inner] = image[batch][row][col][inner] & 0xff; // Convert to int
                        }
                    }
                }
            }
        }
    }


    /**
     * @param con Connection to tensorflow model serving
     * @param code Response code of the request
     *
     * @return A String containing the result of the response
     * @throws IOException If something goes wrong with the connection to the server
     */
    private static String responseError(HttpURLConnection con, int code) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        String inputLine;
        StringBuilder result = new StringBuilder();

        result.append(String.format("HTTP Error Response Code %d \n", code));

        while ((inputLine = in.readLine()) != null) {
            result.append(inputLine);
        }
        in.close();
        result.append("\n");

        return result.toString();
    }

    /**
     * Take a batch of images and make a API call to TensorFlow Serving to get a prediction. Wait for the results, check
     * the response code for errors and return the prediction as a string in JSON format that needs to be converted to
     * JSON format.
     *
     * @param url URL to access the correct TensorfFlow serving model
     * @param data The data to classify
     * @return Returns a JSON string containing the results of the predictions (can be a batch of predictions)
     * @throws IOException Thrown in case we cannot access the URL
     */
    private static String makePrediction(URL url, Content data) throws IOException {

        long startTime = System.nanoTime();
        Gson gson = new Gson();

        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e){
            throw new Error("Could not contact TF model server, check that the version number and modelName is correct\n" + e.getMessage());
        }
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type","application/json");

        connection.setDoOutput(true);
        connection.setDoInput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(gson.toJson(data));
        wr.flush();
        wr.close();

        int responseCode = connection.getResponseCode();

        if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
            throw new Error(responseError(connection, responseCode));
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder result = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            result.append(inputLine);
        }
        in.close();

        long endTime = System.nanoTime();
        long duration = ((endTime - startTime) / 1000000);  //divide by 1000000 to get milliseconds.
        System.out.println("Classification took: " + duration + "ms or " + duration / 1000. + "s");


        return result.toString();
    }

    /**
     * Make a prediction using tensorflow serving and the given model. Any number of images can be supplied as a batch,
     * with the first dimension representing each individual image. For example,
     * batch size 1 would correspond to the following image: byte[1][rows*cols] image.
     *
     * @param port Port number on which the tensorflow model server is listening
     * @param modelName Model name
     * @param version Model version number
     * @param image A batch of images to classify, each in a 1 dimensional array
     *
     * @return A String containing the result of the response.
     * @throws IOException If something goes wrong with the connection to the server
     */
    public static String predict(int port, String modelName, int version, byte[][] image, String signatureString) throws IOException {
        URL url = new URL("http://localhost:" + port + "/v1/models/" + modelName + ":predict");
        if (version > 0){
            url = new URL("http://localhost:" + port + "/v1/models/" + modelName + "/versions/" + version + ":predict");
        }
        Content_single data = new Content_single(signatureString, image);

        return makePrediction(url, data);
    }

    /**
     * Make a prediction using tensorflow serving and the given model. Any number of images can be supplied as a batch,
     * with the first dimension representing each individual image. For example,
     * batch size 1 would correspond to the following image: byte[1][rows][cols][values] image.
     *
     * @param port Port number on which the tensorflow model server is listening
     * @param modelName Model name
     * @param version Model version number
     * @param image A batch of images to classify, each in a 3 dimensional array
     *
     * @return A String containing the result of the response.
     * @throws IOException If something goes wrong with the connection to the server
     */
    public static String predict(int port, String modelName, int version, byte[][][][] image, String signatureString) throws IOException {
        URL url = new URL("http://localhost:" + port + "/v1/models/" + modelName + ":predict");
        if (version > 0){
            url = new URL("http://localhost:" + port + "/v1/models/" + modelName + "/versions/" + version + ":predict");
        }
        Content_3D data = new Content_3D(signatureString, image);

        return makePrediction(url, data);
    }

    /**
     * Get the status of a certain model running on the tensorflow model serving
     *
     * @param port Port number on which the tensorflow model server is listening
     * @param modelName Model name
     * @param version Model version number
     *
     * @return Status of the model
     * @throws IOException If something goes wrong with the connection to the server
     */
    public static String getStatus(int port, String modelName, int version) throws IOException {
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

    /**
     * Get model metadata, containing information on model input tensor dimensions, status, output format etc
     *
     * @param port Port number on which the tensorflow model server is listening
     * @param modelName Model name
     * @param version Model version number
     *
     * @return Metadata of the model
     * @throws IOException If something goes wrong with the connection to the server
     */
    public static String getModelMetadata(int port, String modelName, int version) throws IOException {
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

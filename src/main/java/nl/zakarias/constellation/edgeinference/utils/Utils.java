package nl.zakarias.constellation.edgeinference.utils;

import ibis.constellation.*;
import nl.zakarias.constellation.edgeinference.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Utils {
    public static Logger logger = LoggerFactory.getLogger(Utils.class);

    public static final String DEFAULT_OUTPUT_FILE = Configuration.DEFAULT_OUTPUT_FILE;
    public static final int CIFAR_10_FILE_LENGTH = Configuration.CIFAR_10_FILE_LENGTH;
    public static final int CIFAR_IMAGE_WIDTH = Configuration.CIFAR_IMAGE_WIDTH;
    public static final int CIFAR_IMAGE_HEIGHT = Configuration.CIFAR_IMAGE_HEIGHT;

    public static String printArray(String[] contexts){
        StringBuilder result = new StringBuilder();

        for (String context : contexts) {
            result.append(context).append(" ");
        }

        return result.toString();
    }

    public static String printArray(Context[] contexts){
        StringBuilder result = new StringBuilder();

        for (Context context : contexts) {
            result.append(context.toString()).append(" ");
        }

        return result.toString();
    }

    public static byte[] readAllBytes(Path path) throws IOException {
        return Files.readAllBytes(path);
    }

    public static void prettyPrintMnist(byte[] image){
        System.out.println("\n");
        for (int i=0; i<image.length; i++){
            if (i % 28 == 0){
                System.out.print("\n");
            }
            if ((int)image[i] == 0){
                System.out.print("0");
            } else {
                System.out.print("1");
            }
        }
    }

    public static void prettyPrintRGB(byte[][][] image){
        System.out.println("\n");
        for (byte[][] bytes : image) {
            for (int x = 0; x < bytes.length; x++) {
                System.out.print("[");
                for (int z = 0; z < bytes[x].length; z++) {
                    System.out.print(String.format("%d,", bytes[x][z]));
                }
                if (x == bytes[x].length - 1) {
                    System.out.print("]");
                } else {
                    System.out.print("],");
                }
            }
        }
        System.out.print("\n");
    }


    public static byte[][] readMnist_1D(String filePath) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(filePath));

        dataInputStream.readInt(); // Magic number
        int imageCount = dataInputStream.readInt();
        int rows = dataInputStream.readInt();
        int cols = dataInputStream.readInt();

        byte[][] images = new byte[imageCount][rows*cols];

        for(int i=0; i<imageCount; i++){
            for(int x=0; x<rows*cols; x++){
                images[i][x] = (byte) dataInputStream.readUnsignedByte();
            }
        }

        dataInputStream.close();

        return images;
    }

    public static byte[][][][] readMnist_3D(String filePath) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(filePath));

        dataInputStream.readInt(); // Magic number
        int imageCount = dataInputStream.readInt();
        int rows = dataInputStream.readInt();
        int cols = dataInputStream.readInt();

        // Data must be structured as a matrix, with each number in a 1-digit array
        byte[][][][] images = new byte[imageCount][rows][cols][1];

        for(int image=0; image<imageCount; image++){
            for(int row=0; row<rows; row++){
                for(int col=0; col<cols; col++) {
                    images[image][row][col][0] = (byte) dataInputStream.readUnsignedByte();
                }
            }
        }

        dataInputStream.close();

        return images;
    }

    public static byte[] readLabelsMnist(String filePath) throws IOException {
        DataInputStream labelInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));

        labelInputStream.readInt(); // Magic number
        int labelCount = labelInputStream.readInt();

        byte[] labels = new byte[labelCount];

        for(int i=0; i<labelCount; i++){
            labels[i] = (byte) labelInputStream.readUnsignedByte();
        }

        return labels;
    }

    public static byte[][][][] readCifar10(String filePath) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(filePath));

        byte[][][][] images = new byte[CIFAR_10_FILE_LENGTH][CIFAR_IMAGE_WIDTH][CIFAR_IMAGE_HEIGHT][3]; //RGB

        for(int i=0; i<CIFAR_10_FILE_LENGTH; i++) {
            byte label = (byte) dataInputStream.readUnsignedByte();

            // Read red values
            for(int rgb = 0; rgb < 3; rgb++) {
                for(int row = 0; row < CIFAR_IMAGE_HEIGHT; row++){
                    for (int col = 0; col < CIFAR_IMAGE_WIDTH; col++) {
                        images[i][row][col][rgb] = (byte) dataInputStream.readUnsignedByte();
                    }
                }
            }
        }

        return images;
    }

    public static BufferedImage readJPG(String filePath, int rows, int cols) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(filePath));

        // Read an RGB image
        byte[][][] image = new byte[rows][cols][3];

        BufferedImage bImage = ImageIO.read(new File(filePath));

        return bImage;
    }

    public static int imageIdentifier(byte[] data){
        return Arrays.hashCode(data);
    }
    public static int imageIdentifier(byte[][] data){
        return Arrays.deepHashCode(data);
    }
    public static int imageIdentifier(byte[][][] data){
        return Arrays.deepHashCode(data);
    }
    public static int imageIdentifier(byte[][][][] data){
        return Arrays.deepHashCode(data);
    }
}

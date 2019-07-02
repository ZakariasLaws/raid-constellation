package nl.zakarias.constellation.edgeinference.models;

import java.io.*;

public class MnistFileParser {
    public static byte[][] readDataFile(String filePath) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));

        dataInputStream.readInt(); // Magic number
        int imageCount = dataInputStream.readInt();
        int rows = dataInputStream.readInt();
        int cols = dataInputStream.readInt();

        byte[][] images = new byte[imageCount][rows*cols];

        for(int i=0; i<imageCount; i++){
            for(int x=0; x<rows; x++){
                for(int y=0; y<cols; y++){
                   images[i][(x*cols) + y] = (byte) dataInputStream.readUnsignedByte();
                }
            }
        }

        dataInputStream.close();

        return images;
    }

    public static byte[] readLabelFile(String filePath) throws IOException {
        DataInputStream labelInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));

        labelInputStream.readInt(); // Magic number
        int labelCount = labelInputStream.readInt();

        byte[] labels = new byte[labelCount];

        for(int i=0; i<labelCount; i++){
            labels[i] = (byte) labelInputStream.readUnsignedByte();
        }

        return labels;
    }
}

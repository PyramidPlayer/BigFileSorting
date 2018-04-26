package me.toroptsev;

import java.io.IOException;

public class Application {

    public static void main(String[] args) {
        String fileName = "/Users/toroptsev/bigFile.txt";
        String outputFile = "/Users/toroptsev/sortedBigFile.txt";

        long start = System.currentTimeMillis();
        try {
            BigFileGenerator.generate(fileName, 1000000, 150);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("File generated in " + (System.currentTimeMillis() - start) + " millis");

        start = System.currentTimeMillis();
        ExternalMergeSorter sorter = new ExternalMergeSorter(fileName);
        try {
            sorter.sort(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("File sorted in " + (System.currentTimeMillis() - start) + " millis");
    }
}

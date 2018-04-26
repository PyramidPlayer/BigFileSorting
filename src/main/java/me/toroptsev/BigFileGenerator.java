package me.toroptsev;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class BigFileGenerator {

    public static void generate(String fileName, int linesCount, int lineLength) throws IOException {
        File file = new File(fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int i = 0; i < linesCount; i++) {
                String line = RandomStringUtils.randomAlphanumeric(lineLength);
                writer.write(line);
                writer.newLine();
            }
        }
    }
}

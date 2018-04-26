package me.toroptsev;

import java.io.BufferedReader;
import java.io.IOException;

class BinaryFileBuffer {

    private BufferedReader reader;
    private String cache;

    public BinaryFileBuffer(BufferedReader r) throws IOException {
        this.reader = r;
        readLineInCache();
    }

    public void close() throws IOException {
        this.reader.close();
    }

    public boolean empty() {
        return this.cache == null;
    }

    public String peek() {
        return this.cache;
    }

    public String pop() throws IOException {
        String answer = peek();// make a copy
        readLineInCache();
        return answer;
    }

    private void readLineInCache() throws IOException {
        cache = reader.readLine();
    }

}

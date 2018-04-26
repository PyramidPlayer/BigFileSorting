package me.toroptsev;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class ExternalMergeSorter {



    private String fileName;
    private int maxTmpFiles;
    private Comparator<String> comparator;

    public ExternalMergeSorter(String fileName) {
        this(fileName, Comparator.naturalOrder());
    }

    public ExternalMergeSorter(String fileName, Comparator<String> comparator) {
        this(fileName, comparator, 1000);
    }

    public ExternalMergeSorter(String fileName, Comparator<String> comparator, int maxTmpFiles) {
        this.fileName = fileName;
        this.comparator = comparator;
        this.maxTmpFiles = maxTmpFiles;
    }



    public void sort(String outputFileName) throws IOException {
        List<File> sortedFiles = sortInTmpFiles();
        System.out.println("Created " + sortedFiles.size() + " sorted temp files");
        mergeSortedFiles(sortedFiles, outputFileName);
    }

    private List<File> sortInTmpFiles() throws IOException {
        File file = new File(fileName);
        long maxMemory = EstimationUtils.estimateAvailableMemory();
        long blockSize = estimateBestSizeOfBlocks(file.length(), maxTmpFiles, maxMemory);
        List<File> sortedFiles = new ArrayList<>();

        List<String> tmpList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.defaultCharset()))) {
            String line = "";
                while (line != null) {
                    long currentBlockSize = 0;
                    while (currentBlockSize < blockSize && (line = reader.readLine()) != null) {
                        tmpList.add(line);
                        currentBlockSize += EstimationUtils.estimatedSizeOf(line);
                    }
                    // it's possible to add parallelism there
                    sortedFiles.add(sortAndSave(tmpList));
                    tmpList.clear();
                }
        } catch (EOFException e) {
            if (tmpList.size() > 0) {
                sortedFiles.add(sortAndSave(tmpList));
                tmpList.clear();
            }
        }

        return sortedFiles;
    }

    private File sortAndSave(List<String> content) throws IOException {
        content = content.parallelStream().sorted(comparator).collect(Collectors.toList());
        File newTmpFile = File.createTempFile("tmpBFS", "sorted", null);
        newTmpFile.deleteOnExit();

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(newTmpFile), Charset.defaultCharset()))) {
            for (String line : content) {
                writer.write(line);
                writer.newLine();
            }
        }
        return newTmpFile;
    }

    private void mergeSortedFiles(List<File> sortedFiles, String outputFileName) throws IOException {
        List<BinaryFileBuffer> buffers = new ArrayList<>();

        for (File file : sortedFiles) {
            InputStream in = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.defaultCharset()));
            BinaryFileBuffer buffer = new BinaryFileBuffer(reader);
            buffers.add(buffer);
        }

        FileOutputStream outputStream = new FileOutputStream(outputFileName, false);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, Charset.defaultCharset()));

        mergeSortedFiles(writer, buffers);

        for (File f : sortedFiles) {
            f.delete();
        }
    }

    private void mergeSortedFiles(BufferedWriter writer, List<BinaryFileBuffer> buffers) throws IOException {
        PriorityQueue<BinaryFileBuffer> queue = new PriorityQueue<>((a1, a2) -> comparator.compare(a1.peek(), a2.peek()));
        for (BinaryFileBuffer buffer : buffers) {
            if (!buffer.empty()) {
                queue.add(buffer);
            }
        }

        try {
            while (!queue.isEmpty()) {
                BinaryFileBuffer buffer = queue.poll();
                String line = buffer.pop();
                writer.write(line);
                writer.newLine();
                if (buffer.empty()) {
                    buffer.close();
                } else {
                    queue.add(buffer); // add it back
                }
            }
        } finally {
            writer.close();
            for (BinaryFileBuffer buffer : queue) {
                buffer.close();
            }
        }
    }

    private long estimateBestSizeOfBlocks(final long sizeOfFile, final int maxTmpFiles, final long maxMemory) {
        // we don't want to open up much more than maxTmpFiles temporary files, better run out of memory first.
        long blockSize = sizeOfFile / maxTmpFiles + (sizeOfFile % maxTmpFiles == 0 ? 0 : 1);

        // on the other hand, we don't want to create many temporary files for naught. If blockSize is smaller than
        // half the free memory, grow it.
        if (blockSize < maxMemory / 2) {
            blockSize = maxMemory / 2;
        }
        return blockSize;
    }

}

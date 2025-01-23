package by.it.a_khmelev.lesson15;

import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SourceScannerB  {

    public static void main(String[] args) {
        // ��������� ���� � �������� src
        String src = System.getProperty("user.dir") + File.separator + "src" + File.separator;
        Path srcPath = Paths.get(src);

        // ������ ��� �������� ���������� � ������
        List<FileInfo> fileInfos = new ArrayList<>();

        try {
            // ����������� ����� ���� ������ .java
            Files.walk(srcPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            // ������ ���������� �����
                            String content = Files.readString(path, StandardCharsets.UTF_8);

                            // ���������� �����, ���������� @Test ��� org.junit.Test
                            if (content.contains("@Test") || content.contains("org.junit.Test")) {
                                return;
                            }

                            // ������������ ����� �����
                            String processedContent = processContent(content);

                            // ������������ ������ ������ � ������
                            byte[] contentBytes = processedContent.getBytes(StandardCharsets.UTF_8);
                            int size = contentBytes.length;

                            // ��������� ���������� � ����� � ������
                            String relativePath = srcPath.relativize(path).toString();
                            fileInfos.add(new FileInfo(relativePath, size));
                        } catch (MalformedInputException e) {
                            // ���������� ������ ������
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        // ��������� ����� �� �������, � ����� ����������������� �� ����
        fileInfos.sort(Comparator.comparingInt(FileInfo::getSize)
                .thenComparing(FileInfo::getPath));

        // ������� ���������� � ������ � �������
        fileInfos.forEach(fileInfo ->
                System.out.println(fileInfo.getSize() + " " + fileInfo.getPath()));
    }

    // ������������ ���������� �����
    private static String processContent(String content) {
        // ������� ������ package � ��� �������
        String[] lines = content.split("\n");
        StringBuilder result = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("package") && !trimmed.startsWith("import")) {
                result.append(line).append("\n");
            }
        }

        // ������� ��� ����������� �� O(n)
        String withoutComments = removeComments(result.toString());

        // ������� ������� � ����� <33 � ������ � �����
        String trimmedContent = trimLowAscii(withoutComments);

        // ������� ������ ������
        StringBuilder finalResult = new StringBuilder();
        for (String line : trimmedContent.split("\n")) {
            if (!line.trim().isEmpty()) {
                finalResult.append(line).append("\n");
            }
        }

        return finalResult.toString();
    }

    // ������� ����������� �� ������ �� O(n)
    private static String removeComments(String content) {
        StringBuilder result = new StringBuilder();
        boolean inBlockComment = false;
        boolean inLineComment = false;

        for (int i = 0; i < content.length(); i++) {
            if (inBlockComment) {
                if (i + 1 < content.length() && content.charAt(i) == '*' && content.charAt(i + 1) == '/') {
                    inBlockComment = false;
                    i++; // ���������� '/'
                }
            } else if (inLineComment) {
                if (content.charAt(i) == '\n') {
                    inLineComment = false;
                }
            } else {
                if (i + 1 < content.length() && content.charAt(i) == '/' && content.charAt(i + 1) == '*') {
                    inBlockComment = true;
                    i++; // ���������� '*'
                } else if (i + 1 < content.length() && content.charAt(i) == '/' && content.charAt(i + 1) == '/') {
                    inLineComment = true;
                    i++; // ���������� '/'
                } else {
                    result.append(content.charAt(i));
                }
            }
        }

        return result.toString();
    }

    // ������� ������� � ����� <33 � ������ � ����� ������
    private static String trimLowAscii(String content) {
        int start = 0;
        int end = content.length();

        while (start < end && content.charAt(start) < 33) {
            start++;
        }
        while (end > start && content.charAt(end - 1) < 33) {
            end--;
        }
        return content.substring(start, end);
    }

    // ��������������� ����� ��� �������� ���������� � �����
    static class FileInfo {
        private final String path;
        private final int size;

        public FileInfo(String path, int size) {
            this.path = path;
            this.size = size;
        }

        public String getPath() {
            return path;
        }

        public int getSize() {
            return size;
        }
    }
}
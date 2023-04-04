package github.kasuminova.serverhelper.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {
    public static void extractJarFile(String src, Path dest) throws IOException {
        InputStream input = FileUtils.class.getResourceAsStream(src);
        if (input == null) {
            throw new IOException(src + " is not found in jar!");
        }
        OutputStream output = Files.newOutputStream(dest);
        try {
            byte[] buf = new byte[1024];
            int len;
            while ((len = input.read(buf)) > 0) {
                output.write(buf, 0, len);
            }
        } finally {
            input.close();
            output.close();
        }
    }
}

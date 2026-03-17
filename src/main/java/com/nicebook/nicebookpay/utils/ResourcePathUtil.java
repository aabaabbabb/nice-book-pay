package com.nicebook.nicebookpay.utils;

import org.springframework.core.io.ClassPathResource;

import java.io.*;

public class ResourcePathUtil {
    public static String getResourcePath(String classpathLocation) throws IOException {
        ClassPathResource resource = new ClassPathResource(classpathLocation);

        try (InputStream is = resource.getInputStream()) {
            File tempFile = File.createTempFile("temp_resource", null);
            tempFile.deleteOnExit();

            try (OutputStream os = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
            }

            return tempFile.getAbsolutePath();
        }
    }
}

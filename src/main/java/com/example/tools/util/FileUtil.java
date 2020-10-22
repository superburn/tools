package com.example.tools.util;

import baidu.acg.ts.diagnose2.exception.PreparationException;
import baidu.acg.ts.diagnose2.exception.ProcessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;


@Slf4j
public class FileUtil {
    public static void prepareLogDirectory(File file) throws PreparationException {
        if (!file.exists()) {
            try {
                Files.createDirectories(file.toPath());
            } catch (IOException e) {
                log.error("failed to create log directory {}", file);
                throw new PreparationException(e);
            }
        } else if (!file.isDirectory()) {
            throw new PreparationException("log directory "
                        + file.toString() + " is not directory!");
        }
    }

    public static void tearDown(Path localPath, String suffix) {
        try {
            Files.deleteIfExists(localPath);
            String gzFile = localPath.toString();
            Files.deleteIfExists(Paths.get(gzFile.substring(0, gzFile.length() - suffix.length())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String gunzip(String gzFile, String suffix) throws ProcessException {
        byte[] buffer = new byte[1024];
        String unGzFile = gzFile.substring(0, gzFile.length() - suffix.length());

        try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(gzFile));
             FileOutputStream fos = new FileOutputStream(unGzFile)) {
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new ProcessException("failed to gunzip log file " + gzFile, e);
        }

        log.info("succeeded to unzip {} to {}", gzFile, unGzFile);

        return unGzFile;
    }

    public static void download(URL from, File to) {
        try {
            FileUtils.copyURLToFile(from, to, 10 * 1000, 30 * 1000);
            log.info("succeeded to download log from {} to {}", from, to);
        } catch (IOException e) {
            log.warn("there is no log at {}", from);
        }
    }
}

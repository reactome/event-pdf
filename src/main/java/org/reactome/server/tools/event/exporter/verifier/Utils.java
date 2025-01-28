package org.reactome.server.tools.event.exporter.verifier;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.reactome.release.verifier.FileUtils.downloadFileFromS3;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 1/25/2025
 */
public class Utils {

    public static void downloadEventPDFFilesAndSizesListFromS3(int versionNumber) {
        if (Files.notExists(Paths.get(getEventPDFFilesAndSizesListName()))) {
            downloadFileFromS3("reactome", getEventPDFFilesAndSizesListPathInS3(versionNumber));
        }
    }

    private static String getEventPDFFilesAndSizesListPathInS3(int versionNumber) {
        return String.format("private/releases/%d/event_pdf/data/%s",
            versionNumber, getEventPDFFilesAndSizesListName()
        );
    }

    static String getEventPDFFilesAndSizesListName() {
        return "files_and_sizes.txt";
    }
}

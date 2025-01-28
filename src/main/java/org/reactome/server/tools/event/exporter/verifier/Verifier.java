package org.reactome.server.tools.event.exporter.verifier;

import com.martiansoftware.jsap.*;
import org.reactome.release.verifier.TooSmallFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.reactome.release.verifier.TooSmallFile.currentFileIsSmaller;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 12/18/2024
 */
public class Verifier {
    private String outputDirectory;
    private int releaseNumber;

    public static void main(String[] args) throws JSAPException, IOException {
        Verifier verifier = new Verifier();
        verifier.parseCommandLineArgs(args);
        verifier.run();
    }

    public void parseCommandLineArgs(String[] args) throws JSAPException {
        SimpleJSAP jsap = new SimpleJSAP(Verifier.class.getName(), "Verify Event-PDF ran correctly",
            new Parameter[]{
                new FlaggedOption("output", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, 'o', "output", "The folder where the results are written to."),
                new FlaggedOption("releaseNumber", JSAP.INTEGER_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, 'r', "releaseNumber", "The most recent Reactome release version")
            }
        );

        JSAPResult config = jsap.parse(args);
        if (jsap.messagePrinted()) System.exit(1);

        this.outputDirectory = config.getString("output");
        this.releaseNumber = config.getInt("releaseNumber");
    }

    public void run() throws IOException {
        List<String> errorMessages = verifyEventPDFRanCorrectly();
        if (errorMessages.isEmpty()) {
            System.out.println("Event PDF has run correctly!");
        } else {
            errorMessages.forEach(System.err::println);
            System.exit(1);
        }
    }

    private List<String> verifyEventPDFRanCorrectly() throws IOException {
        List<String> errorMessages = new ArrayList<>();

        errorMessages.addAll(verifyEventPDFFolderExists());
        if (errorMessages.isEmpty()) {
            errorMessages.addAll(verifyEventPDFFilesExist());
            errorMessages.addAll(verifyEventPDFFileSizesComparedToPreviousRelease());
        }

        return errorMessages;
    }

    private List<String> verifyEventPDFFolderExists() {
        return !Files.exists(Paths.get(this.outputDirectory)) ?
            Arrays.asList(this.outputDirectory + " does not exist; Expected event-pdf output files at this location") :
            new ArrayList<>();
    }

    private List<String> verifyEventPDFFilesExist() throws IOException {
        List<String> errorMessages = new ArrayList<>();

        Utils.downloadEventPDFFilesAndSizesListFromS3(getPreviousReleaseNumber());
        for (String eventPDFFileName : getEventPDFFileNames()) {
            Path eventPDFFilePath = Paths.get(this.outputDirectory, eventPDFFileName);
            if (!Files.exists(eventPDFFilePath)) {
                errorMessages.add("File " + eventPDFFilePath + " does not exist");
            }
        }

        return errorMessages;
    }

    private List<String> verifyEventPDFFileSizesComparedToPreviousRelease() throws IOException {
        Utils.downloadEventPDFFilesAndSizesListFromS3(getPreviousReleaseNumber());
        List<TooSmallFile> tooSmallFiles = new ArrayList<>();
        for (String eventPDFFileName : getEventPDFFileNames()) {
            Path eventPDFFilePath = Paths.get(this.outputDirectory, eventPDFFileName);

            if (Files.exists(eventPDFFilePath) && currentFileIsSmaller(eventPDFFilePath)) {
                tooSmallFiles.add(new TooSmallFile(eventPDFFilePath));
            }
        }

        return tooSmallFiles
            .stream()
            .map(TooSmallFile::toString)
            .collect(Collectors.toList());
    }

    private List<String> getEventPDFFileNames() throws IOException {
        return Files.lines(Paths.get(Utils.getEventPDFFilesAndSizesListName()))
            .map(this::getFileName)
            .collect(Collectors.toList());
    }

    private String getFileName(String line) {
        return line.split("\t")[1];
    }

    private int getPreviousReleaseNumber() {
        return this.releaseNumber - 1;
    }
}

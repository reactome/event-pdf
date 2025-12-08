package org.reactome.server.tools.event.exporter.verifier;

import com.martiansoftware.jsap.*;
import org.reactome.release.verifier.DefaultVerifier;
import org.reactome.release.verifier.Verifier;

import java.io.IOException;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 12/18/2024
 */
public class EventPdfVerifier {

    public static void main(String[] args) throws JSAPException, IOException {
        Verifier verifier = new DefaultVerifier("event-pdf");
        verifier.parseCommandLineArgs(args);
        verifier.run();
    }
}

package org.reactome.server.tools.event.exporter.section;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import org.reactome.server.analysis.core.result.model.AnalysisSummary;
import org.reactome.server.tools.event.exporter.AnalysisData;
import org.reactome.server.tools.event.exporter.DocumentContent;
import org.reactome.server.tools.event.exporter.profile.PdfProfile;
import org.reactome.server.tools.event.exporter.util.Texts;
import org.reactome.server.tools.event.exporter.util.html.HtmlProcessor;

import java.util.LinkedList;
import java.util.List;

/**
 * Section ParameterAndResultSummary contains analysis parameter in the analysis
 * result, fireworks for this analysis.
 *
 * @author Chuan-Deng dengchuanbio@gmail.com
 */
public class PropertiesSection implements Section {

    private final static String PROJECTED = "/documentation/inferred-events";
    private final static String ANALYSIS_PATH = "/user/guide/analysis";
    private final static String GSA_ANALYSIS_PATH = "/user/guide/analysis/gsa";

    @Override
    public void render(Document document, DocumentContent content) {
        if (content.getAnalysisData() == null) return;
        final PdfProfile profile = content.getPdfProfile();
        final AnalysisData analysisData = content.getAnalysisData();
        document.add(new AreaBreak());
        document.add(profile.getH1("Analysis properties").setDestination("properties"));
        final List<Paragraph> paragraphs = new LinkedList<>();

        final AnalysisSummary summary = analysisData.getResult().getSummary();
        final int found = analysisData.getResult().getAnalysisIdentifiers().size();
        final int notFound = analysisData.getResult().getNotFound().size();
        final String serverName = analysisData.getServerName();
        final String analysisTypeDescription;
        final String gsaMethod = summary.getGsaMethod();
        final Text analysisTypeSeeMoreLink;

        if (gsaMethod == null) {
            analysisTypeSeeMoreLink = profile.getLink("See more", serverName + ANALYSIS_PATH);
            String analysisType = summary.getType().toLowerCase();
            analysisTypeDescription = Texts.getProperty(analysisType);
        } else {
            analysisTypeSeeMoreLink = profile.getLink("See more", serverName + GSA_ANALYSIS_PATH);
            analysisTypeDescription = Texts.getProperty(gsaMethod.toLowerCase());
        }

        if (analysisTypeDescription != null && analysisTypeSeeMoreLink != null) {
            paragraphs.add(HtmlProcessor.createParagraph(analysisTypeDescription, profile)
                    .add(" ")
                    .add(analysisTypeSeeMoreLink));
        }

        paragraphs.add(profile.getParagraph(String.format(Texts.getProperty("identifiers.found"),
                found, found + notFound, analysisData.getResult().getPathways().size())));

        if (analysisData.isProjection())
            paragraphs.add(profile.getParagraph(Texts.getProperty("projected"))
                    .add(" ")
                    .add(profile.getLink("\u2197", serverName + PROJECTED)));

        if (analysisData.isInteractors())
            paragraphs.add(profile.getParagraph(Texts.getProperty("interactors")));

        paragraphs.add(profile.getParagraph((Texts.getProperty("target.species.resource", analysisData.getSpecies(), analysisData.getBeautifiedResource()))));

        paragraphs.add(profile.getParagraph(String.format(Texts.getProperty("token"), summary.getToken())));

        document.add(profile.getList(paragraphs));
    }

}

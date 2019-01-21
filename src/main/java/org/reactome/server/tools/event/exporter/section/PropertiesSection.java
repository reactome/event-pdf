package org.reactome.server.tools.event.exporter.section;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import org.reactome.server.analysis.core.result.model.AnalysisSummary;
import org.reactome.server.tools.event.exporter.AnalysisData;
import org.reactome.server.tools.event.exporter.DocumentContent;
import org.reactome.server.tools.event.exporter.profile.PdfProfile;
import org.reactome.server.tools.event.exporter.util.HtmlParser;
import org.reactome.server.tools.event.exporter.util.Images;
import org.reactome.server.tools.event.exporter.util.Texts;

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

	@Override
	public void render(Document document, DocumentContent content) {
		if (content.getAnalysisData() == null) return;
		final PdfProfile profile = content.getPdfProfile();
		final AnalysisData analysisData = content.getAnalysisData();
		document.add(new AreaBreak());
		document.add(profile.getH1("Analysis properties").setDestination("properties"));
		final List<Paragraph> list = new LinkedList<>();

		final String text = Texts.getProperty(analysisData.getResult().getSummary().getType().toLowerCase());
		final int found = analysisData.getResult().getAnalysisIdentifiers().size();
		final int notFound = analysisData.getResult().getNotFound().size();
		final AnalysisSummary summary = analysisData.getResult().getSummary();


		final String serverName = analysisData.getServerName();
		list.add(HtmlParser.parseParagraph(profile, text)
				.add(" ")
				.add(profile.getLink("See more", serverName + ANALYSIS_PATH)));
//				.add(Images.getLink(serverName + ANALYSIS_PATH, profile.getFontSize())));

		list.add(profile.getParagraph(String.format(Texts.getProperty("identifiers.found"),
				found, found + notFound, analysisData.getResult().getPathways().size())));

		if (analysisData.isProjection())
			list.add(profile.getParagraph(Texts.getProperty("projected"))
					.add(" ")
					.add(Images.getLink(serverName + PROJECTED, profile.getFontSize())));

		if (analysisData.isInteractors())
			list.add(profile.getParagraph(Texts.getProperty("interactors")));

		list.add(profile.getParagraph((Texts.getProperty("target.species.resource", analysisData.getSpecies(), analysisData.getBeautifiedResource()))));

		list.add(profile.getParagraph(String.format(Texts.getProperty("token"), summary.getToken())));

		document.add(profile.getList(list));
	}

}

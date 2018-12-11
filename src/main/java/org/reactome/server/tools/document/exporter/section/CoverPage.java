package org.reactome.server.tools.document.exporter.section;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.property.HorizontalAlignment;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.tools.document.exporter.AnalysisData;
import org.reactome.server.tools.document.exporter.DocumentArgs;
import org.reactome.server.tools.document.exporter.style.Images;
import org.reactome.server.tools.document.exporter.style.PdfProfile;

import java.text.SimpleDateFormat;

/**
 * @author Chuan-Deng dengchuanbio@gmail.com
 */
public class CoverPage implements Section {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

	@Override
	public void render(Document document, PdfProfile profile, AnalysisData analysisData, Event event, DocumentArgs args) {
		document.add(Images.getLogo().scaleToFit(100, 100).setHorizontalAlignment(HorizontalAlignment.LEFT));
		// Empty space
		document.add(profile.getTitle(""));
		document.add(profile.getTitle(event.getDisplayName()));
		document.add(profile.getTitle(""));

//		final String link = analysisData.getServerName() + "/PathwayBrowser/#/ANALYSIS=" + analysisData.getResult().getSummary().getToken();

//		final String text = PdfUtils.getProperty("cover.page",
//				analysisData.getName(),
//				AnalysisData.getDBVersion(),
//				DATE_FORMAT.format(new Date()),
//				link, link);

//		final Collection<Paragraph> paragraphs = HtmlParser.parseText(profile, text);
//		for (Paragraph paragraph : paragraphs)
//			document.add(paragraph.setTextAlignment(TextAlignment.CENTER));
	}


}

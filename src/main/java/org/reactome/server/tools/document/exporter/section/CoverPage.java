package org.reactome.server.tools.document.exporter.section;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.tools.document.exporter.DocumentContent;
import org.reactome.server.tools.document.exporter.style.Images;
import org.reactome.server.tools.document.exporter.style.PdfProfile;
import org.reactome.server.tools.document.exporter.util.HtmlParser;
import org.reactome.server.tools.document.exporter.util.PdfUtils;

import java.text.SimpleDateFormat;

/**
 * @author Chuan-Deng dengchuanbio@gmail.com
 */
public class CoverPage implements Section {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

	@Override
	public void render(Document document, DocumentContent content) {
		document.add(Images.getLogo().scaleToFit(100, 100).setHorizontalAlignment(HorizontalAlignment.LEFT));
		final PdfProfile profile = content.getPdfProfile();
		final Event event = content.getEvent();
		// Empty space
		document.add(profile.getTitle(""));
		document.add(profile.getTitle(event.getDisplayName()));
		document.add(profile.getTitle(""));
		for (Paragraph paragraph : HtmlParser.parseText(profile, PdfUtils.getProperty("cover.page.disclaimer"))) {
			document.add(paragraph.setTextAlignment(TextAlignment.CENTER));
		}

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

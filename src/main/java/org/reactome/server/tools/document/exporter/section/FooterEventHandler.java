package org.reactome.server.tools.document.exporter.section;

import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.reactome.server.tools.document.exporter.profile.PdfProfile;

/**
 * Event handler to add the footer text and page number.
 *
 * @author Chuan Deng dengchuanbio@gmail.com
 */
public class FooterEventHandler implements IEventHandler {

	private final Document document;
	private PdfProfile profile;
	private String serverName;

	public FooterEventHandler(Document document, PdfProfile profile, String serverName) {
		this.document = document;
		this.profile = profile;
		this.serverName = serverName;
	}

	@Override
	public void handleEvent(Event event) {
		final PdfDocumentEvent documentEvent = (PdfDocumentEvent) event;
		final PdfPage page = documentEvent.getPage();

		final int pageNumber = document.getPdfDocument().getPageNumber(page);
		if (pageNumber <= 2) return;  // Cover page and table of content
		final String paging = String.format("Page %d", pageNumber - 1);
		final float yCenter = document.getBottomMargin() * 0.5f;
		final float width = page.getMediaBox().getWidth();
		final float pagingWidth = profile.getRegularFont().getWidth(paging, profile.getFontSize()) + 1;
		final float linkWidth = profile.getRegularFont().getWidth(serverName, profile.getFontSize()) + 1;
		final Paragraph p = profile.getParagraph(serverName)
				.setFontColor(profile.getLinkColor())
				.setAction(PdfAction.createURI(serverName))
				.setFixedPosition(document.getLeftMargin(), yCenter, linkWidth);
		document.add(p);

		final Paragraph pagePosition = profile.getParagraph(paging)
				.setFixedPosition(width - document.getRightMargin() - pagingWidth, yCenter, pagingWidth);
		document.add(pagePosition);
	}
}

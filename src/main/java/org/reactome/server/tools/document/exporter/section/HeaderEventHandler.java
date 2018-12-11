package org.reactome.server.tools.document.exporter.section;

import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import org.reactome.server.tools.document.exporter.DocumentProperties;
import org.reactome.server.tools.document.exporter.style.PdfProfile;

import java.util.List;

public class HeaderEventHandler implements IEventHandler {

	private final Document document;
	private DocumentProperties properties;

	public HeaderEventHandler(Document document, DocumentProperties properties) {
		this.document = document;
		this.properties = properties;
	}

	@Override
	public void handleEvent(Event event) {
		final PdfDocumentEvent documentEvent = (PdfDocumentEvent) event;
		final PdfPage page = documentEvent.getPage();
		final List<org.reactome.server.graph.domain.model.Event> nav = properties.getNav();
		if (nav == null) return;
		if (nav.isEmpty()) return;
		final PdfProfile profile = properties.getPdfProfile();
		final Paragraph paragraph = profile.getParagraph("");
		for (int i = 0; i < nav.size(); i++) {
			if (i > 0) paragraph.add(" > ");
			org.reactome.server.graph.domain.model.Event ev = nav.get(i);
			final Text text = new Text(ev.getDisplayName()).setAction(PdfAction.createGoTo(ev.getStId())).setFontColor(profile.getLinkColor());
			paragraph.add(text);
		}
		final float width = page.getMediaBox().getWidth();
		final float baseline = page.getMediaBox().getHeight() - document.getTopMargin();
		paragraph.setFixedPosition(document.getLeftMargin(), baseline, width);
		document.add(paragraph);
	}
}

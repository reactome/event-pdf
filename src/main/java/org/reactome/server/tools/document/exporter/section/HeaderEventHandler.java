package org.reactome.server.tools.document.exporter.section;

import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.layout.Document;
import org.reactome.server.tools.document.exporter.DocumentProperties;
import org.reactome.server.tools.document.exporter.style.PdfProfile;

import java.util.List;

public class HeaderEventHandler implements IEventHandler {

	private final Document document;
	private final PdfProfile profile;
	private DocumentProperties properties;

	public HeaderEventHandler(Document document, PdfProfile profile, DocumentProperties properties) {
		this.document = document;
		this.profile = profile;
		this.properties = properties;
	}

	@Override
	public void handleEvent(Event event) {
		final PdfDocumentEvent documentEvent = (PdfDocumentEvent) event;
		final PdfPage page = documentEvent.getPage();
		final List<org.reactome.server.graph.domain.model.Event> nav = properties.getNav();
		if (nav == null) return;


	}
}

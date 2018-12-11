package org.reactome.server.tools.document.exporter;

import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.tools.document.exporter.style.PdfProfile;

import java.util.List;

public class DocumentProperties {
	private AnalysisData analysisData;
	private final PdfProfile pdfProfile;
	private final Event event;
	private final DocumentArgs args;
	private List<Event> nav;

	public DocumentProperties(AnalysisData analysisData, PdfProfile pdfProfile, Event event, DocumentArgs args) {
		this.analysisData = analysisData;
		this.pdfProfile = pdfProfile;
		this.event = event;
		this.args = args;
	}


	public void setNav(List<Event> nav) {
		this.nav = nav;
	}

	public List<Event> getNav() {
		return nav;
	}

	public AnalysisData getAnalysisData() {
		return analysisData;
	}

	public PdfProfile getPdfProfile() {
		return pdfProfile;
	}

	public Event getEvent() {
		return event;
	}

	public DocumentArgs getArgs() {
		return args;
	}
}

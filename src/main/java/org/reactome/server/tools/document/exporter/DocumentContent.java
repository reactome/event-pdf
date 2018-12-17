package org.reactome.server.tools.document.exporter;

import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.tools.document.exporter.profile.PdfProfile;

public class DocumentContent {
	private AnalysisData analysisData;
	private final PdfProfile pdfProfile;
	private final Event event;
	private final DocumentArgs args;
	private String server;

	public DocumentContent(AnalysisData analysisData, PdfProfile pdfProfile, Event event, DocumentArgs args) {
		this.analysisData = analysisData;
		this.pdfProfile = pdfProfile;
		this.event = event;
		this.args = args;
		this.server = analysisData != null
				? analysisData.getServerName()
				: "https://reactome.org";
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

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}
}

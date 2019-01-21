package org.reactome.server.tools.event.exporter;

import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.tools.event.exporter.profile.PdfProfile;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Wrapper for the contents of the document: events, analysis, pdf profile, args, root event
 */
public class DocumentContent {
	private final PdfProfile pdfProfile;
	private final Event event;
	private final DocumentArgs args;
	private AnalysisData analysisData;
	private String server;
	private Set<Event> events;

	public DocumentContent(AnalysisData analysisData, PdfProfile pdfProfile, Event event, DocumentArgs args) {
		this.analysisData = analysisData;
		this.pdfProfile = pdfProfile;
		this.event = event;
		this.args = args;
		this.server = analysisData != null
				? analysisData.getServerName()
				: "https://reactome.org";
		events = Collections.unmodifiableSet(collectEvents(event, args.getMaxLevel(), 0));
	}

	private Set<Event> collectEvents(Event event, int maxLevel, int level) {
		final Set<Event> rtn = new TreeSet<>();
		rtn.add(event);
		if (event instanceof Pathway && level < maxLevel) {
			for (Event ev : ((Pathway) event).getHasEvent()) {
				rtn.addAll(collectEvents(ev, maxLevel, level + 1));
			}
		}
		return rtn;
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

	/**
	 * Get a plain list of unique events contained in this document
	 *
	 * @return the list of all unique events that appear in the document
	 */
	public Set<Event> getEvents() {
		return events;
	}
}

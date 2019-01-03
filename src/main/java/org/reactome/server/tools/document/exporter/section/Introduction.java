package org.reactome.server.tools.document.exporter.section;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.VerticalAlignment;
import org.reactome.server.graph.domain.model.LiteratureReference;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.ReactionLikeEvent;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.tools.document.exporter.DocumentContent;
import org.reactome.server.tools.document.exporter.profile.PdfProfile;
import org.reactome.server.tools.document.exporter.util.HtmlParser;
import org.reactome.server.tools.document.exporter.util.References;
import org.reactome.server.tools.document.exporter.util.Texts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Section Introduction contains the analysis introduction and Reactome relative
 * publications
 */
public class Introduction implements Section {

	private static final String INTRODUCTION = Texts.getProperty("introduction");
	private GeneralService generalService;

	public Introduction(GeneralService generalService) {
		this.generalService = generalService;
	}

	@Override
	public void render(Document document, DocumentContent content) {
		final PdfProfile profile = content.getPdfProfile();
		document.add(new AreaBreak());
		document.add(profile.getH2("Introduction").setDestination("introduction"));
		final Collection<Paragraph> intro = HtmlParser.parseText(profile, INTRODUCTION);
		intro.forEach(document::add);

		addReferences(document, profile);
		addBottomText(document, content, profile);
	}

	private void addReferences(Document document, PdfProfile profile) {
		document.add(profile.getH3("Literature references"));
		final List<LiteratureReference> references = new ArrayList<>(References.getReactomeReferences());
		references.sort(Comparator.comparingInt(LiteratureReference::getYear));
		for (LiteratureReference reference : references) {
			document.add(References.getPublication(profile, reference));
		}
	}

	private void addBottomText(Document document, DocumentContent content, PdfProfile profile) {
		final long pathways = content.getEvents().stream().filter(Pathway.class::isInstance).count();
		final long reactions = content.getEvents().stream().filter(ReactionLikeEvent.class::isInstance).count();
		final String counter = getCounterText(pathways, reactions);
		final String version = "Reactome graph database version: " + generalService.getDBInfo().getVersion();
		document.add(new Div()
				.add(profile.getParagraph(version))
				.add(profile.getParagraph(counter)
						.add(" (")
						.add(profile.getGoTo("see Table of Contents", "toc"))
						.add(")"))
				.setFillAvailableArea(true)
				.setVerticalAlignment(VerticalAlignment.BOTTOM));
	}

	private String getCounterText(long pathways, long reactions) {
		final StringBuilder counter = new StringBuilder("This document contains ");
		final List<String> things = new ArrayList<>();
		if (pathways > 0) things.add(String.format("%d pathway%s", pathways, getPlural(pathways)));
		if (reactions > 0) things.add(String.format("%d reaction%s", reactions, getPlural(reactions)));
		counter.append(String.join(" and ", things));
		return counter.toString();
	}

	private String getPlural(long count) {
		return count == 1 ? "" : "s";
	}

}

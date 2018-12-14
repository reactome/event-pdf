package org.reactome.server.tools.document.exporter.section;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.VerticalAlignment;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.ReactionLikeEvent;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.tools.document.exporter.DocumentContent;
import org.reactome.server.tools.document.exporter.style.PdfProfile;
import org.reactome.server.tools.document.exporter.util.HtmlParser;
import org.reactome.server.tools.document.exporter.util.PdfUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Section Introduction contains the analysis introduction and Reactome relative
 * publications
 */
public class Introduction implements Section {

	private static final String INTRODUCTION = PdfUtils.getProperty("introduction");
	private static final List<Reference> PUBLICATIONS = PdfUtils.getText(Introduction.class.getResourceAsStream("/texts/references.txt"))
			.stream()
			.filter(line -> !line.isEmpty())
			.map(s -> s.split("\t"))
			.map(line -> new Reference(line[0], line[1]))
			.collect(Collectors.toList());
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

		document.add(profile.getH3("Literature references"));
		for (Reference publication : PUBLICATIONS) {
			document.add(profile.getCitation(publication.text, publication.link));
		}
		final int pathways = countPathways(content.getEvent(), 0, content.getArgs().getMaxLevel());
		final int reactions = countReactions(content.getEvent(), 0, content.getArgs().getMaxLevel());
		final String counts = String.format("This document contains %d pathway%s and %d reaction%s", pathways, getPlural(pathways), reactions, getPlural(reactions));
		final String version = "Reactome graph database version: " + generalService.getDBInfo().getVersion();
		document.add(
				new Div().setFillAvailableArea(true)
						.add(profile.getParagraph(version))
						.add(profile.getParagraph(counts)
								.add(" (")
								.add(profile.getGoTo("see Table of Contents", "toc"))
								.add(")"))
						.setVerticalAlignment(VerticalAlignment.BOTTOM));
	}

	private String getPlural(int count) {
		return count == 1 ? "" : "s";
	}

	private int countPathways(Event event, int level, int maxLevel) {
		int pathways = 0;
		if (event instanceof Pathway) {
			pathways = 1;
			if (level < maxLevel) {
				for (Event ev : ((Pathway) event).getHasEvent()) {
					pathways += countPathways(ev, level + 1, maxLevel);
				}
			}
		}
		return pathways;
	}

	private int countReactions(Event event, int level, int maxLevel) {
		if (event instanceof ReactionLikeEvent) {
			return 1;
		}
		int reactions = 0;
		if (level < maxLevel) {
			for (Event ev : ((Pathway) event).getHasEvent()) {
				reactions += countReactions(ev, level + 1, maxLevel);
			}
		}
		return reactions;
	}

	private static class Reference {
		String text;
		String link;

		Reference(String text, String link) {
			this.text = text;
			this.link = link;
		}
	}
}

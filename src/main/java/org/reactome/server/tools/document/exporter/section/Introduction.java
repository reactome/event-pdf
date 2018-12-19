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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Section Introduction contains the analysis introduction and Reactome relative
 * publications
 */
public class Introduction implements Section {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

	private static final String INTRODUCTION = Texts.getProperty("introduction");
//	private static final List<Reference> PUBLICATIONS = Texts.getText(Introduction.class.getResourceAsStream("/texts/references.txt"))
//			.stream()
//			.filter(line -> !line.isEmpty())
//			.map(s -> s.split("\t"))
//			.map(line -> new Reference(line[0], line[1]))
//			.collect(Collectors.toList());
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
		for (LiteratureReference reference : References.getReactomeReferences()) {
			document.add(References.getPublication(profile, reference));
		}
//		for (Reference publication : PUBLICATIONS) {
//			document.add(profile.getCitation()
//					.add(publication.text)
//					.add(" ")
//					.add(profile.getLink("pubmed", publication.link)));
//		}
		final long pathways = content.getEvents().stream().filter(Pathway.class::isInstance).count();
		final long reactions = content.getEvents().stream().filter(ReactionLikeEvent.class::isInstance).count();
		final StringBuilder counter = new StringBuilder("This document contains ");
		final List<String> things = new ArrayList<>();
		if (pathways > 0) things.add(String.format("%d pathway%s", pathways, getPlural(pathways)));
		if (reactions > 0) things.add(String.format("%d reaction%s", reactions, getPlural(reactions)));
		counter.append(String.join(" and ", things));
		final String version = "Reactome graph database version: " + generalService.getDBInfo().getVersion();
		document.add(
				new Div().setFillAvailableArea(true)
						.add(profile.getParagraph(DATE_FORMAT.format(new Date())))
						.add(profile.getParagraph(version))
						.add(profile.getParagraph(counter.toString())
								.add(" (")
								.add(profile.getGoTo("see Table of Contents", "toc"))
								.add(")"))
						.setVerticalAlignment(VerticalAlignment.BOTTOM));
	}

	private String getPlural(long count) {
		return count == 1 ? "" : "s";
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

package org.reactome.server.tools.document.exporter.section;

import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Tab;
import com.itextpdf.layout.element.TabStop;
import com.itextpdf.layout.property.TabAlignment;
import com.itextpdf.layout.property.TextAlignment;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.tools.document.exporter.DocumentArgs;
import org.reactome.server.tools.document.exporter.DocumentContent;
import org.reactome.server.tools.document.exporter.style.Images;
import org.reactome.server.tools.document.exporter.style.PdfProfile;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TableOfContent implements Section {

	private static final java.util.List<String> classOrder = Arrays.asList("Pathway", "Reaction", "BlackBoxEvent");
	private Map<Long, Integer> pages;

	public TableOfContent(Map<Long, Integer> pages) {
		this.pages = pages;
	}

	@Override
	public void render(Document document, DocumentContent content) {
		final PdfProfile profile = content.getPdfProfile();
		final Event event = content.getEvent();
		final DocumentArgs args = content.getArgs();
		document.add(new AreaBreak());
		document.add(profile.getH1("Table of Contents", false).setDestination("toc"));
		document.add(profile.getH3("Introduction").setAction(PdfAction.createGoTo("introduction")).setFontColor(profile.getLinkColor()));
		if (content.getAnalysisData() != null)
			document.add(profile.getH3("Analysis properties").setAction(PdfAction.createGoTo("properties")).setFontColor(profile.getLinkColor()));
		document.add(profile.getH3("Details").setAction(PdfAction.createGoTo("details")).setFontColor(profile.getLinkColor()));
		addToToc(document, profile, event, 1, args.getMaxLevel());
	}

	private void addToToc(Document document, PdfProfile profile, Event pathway, int level, int maxLevel) {
		writeTocEntry(document, profile, level, pathway);
		if (pathway instanceof Pathway && level + 1 < maxLevel) {
			final List<Event> events = ((Pathway) pathway).getHasEvent();
			events.sort(Comparator.comparingInt(o -> classOrder.indexOf(o.getSchemaClass())));
			for (Event ev : events) {
				addToToc(document, profile, ev, level + 1, maxLevel);
			}
		}
	}

	private void writeTocEntry(Document document, PdfProfile profile, int level, Event event) {
		final Paragraph paragraph = profile.getParagraph("")
				.setPaddingLeft(level * 10)
				.addTabStops(new TabStop(1000, TabAlignment.RIGHT))
				.setTextAlignment(TextAlignment.LEFT)
				.setMultipliedLeading(1f)
				.add(Images.get(event.getSchemaClass(), profile.getFontSize() - 1))
				.add(" ")
				.add(event.getDisplayName())
				.setAction(PdfAction.createGoTo(event.getStId()))
				.setFontColor(profile.getLinkColor())
				.add(new Tab())
				.add(String.valueOf(pages.get(event.getId())));
		document.add(paragraph);
	}

}

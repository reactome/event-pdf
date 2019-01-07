package org.reactome.server.tools.document.exporter.section;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.TextAlignment;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.tools.document.exporter.DocumentArgs;
import org.reactome.server.tools.document.exporter.DocumentContent;
import org.reactome.server.tools.document.exporter.profile.PdfProfile;
import org.reactome.server.tools.document.exporter.util.Images;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TableOfContent implements Section {

	private static final java.util.List<String> classOrder = Arrays.asList(
			"TopLevelPathway",
			"Pathway",
			"Reaction",
			"Depolymerisation",
			"Polymerisation",
			"BlackBoxEvent",
			"FailedReaction"
	);
	private Map<Long, Integer> pages;

	public TableOfContent(Map<Long, Integer> pages) {
		this.pages = pages;
	}

	@Override
	public void render(Document document, DocumentContent content) {
		if (content.getArgs().getMaxLevel() == 0) return;  // skip when only one element
		final PdfProfile profile = content.getPdfProfile();
		final Event event = content.getEvent();
		final DocumentArgs args = content.getArgs();
		document.add(new AreaBreak());
		final int page = document.getPdfDocument().getPageNumber(document.getPdfDocument().getLastPage());
		document.add(profile.getH1("Table of Contents").setDestination("toc"));
		final Table table = new Table(2);
		table.setBorder(Border.NO_BORDER);
		writeFreeTocEntry(table, profile, "Introduction", "introduction", 1);
		if (content.getAnalysisData() != null) {
			writeFreeTocEntry(table, profile, "Analysis properties", "properties", 2);
			writeFreeTocEntry(table, profile, "Details", "details", 3);
		} else {
			writeFreeTocEntry(table, profile, "Details", "details", 2);
		}
		addToToc(table, profile, event, 0, args.getMaxLevel());
		writeFreeTocEntry(table, profile, "Table of Contents", "toc", page - 1);
		document.add(table);
	}

	private void addToToc(Table table, PdfProfile profile, Event pathway, int level, int maxLevel) {
		writeEventTocEntry(table, profile, level, pathway);
		if (pathway instanceof Pathway && level < maxLevel) {
			final List<Event> events = ((Pathway) pathway).getHasEvent();
			events.sort(Comparator.comparingInt(o -> classOrder.indexOf(o.getSchemaClass())));
			for (Event ev : events) {
				addToToc(table, profile, ev, level + 1, maxLevel);
			}
		}
	}

	private void writeFreeTocEntry(Table table, PdfProfile profile, String text, String destination, int page) {
		final Paragraph p = profile.getParagraph()
				.setFontSize(profile.getFontSize() - 1)
				.setMultipliedLeading(1.0f)
				.setTextAlignment(TextAlignment.LEFT)
				.add(profile.getGoTo(text, destination));
		final Paragraph p2 = profile.getParagraph()
				.setFontSize(profile.getFontSize() - 1)
				.setMultipliedLeading(1.0f)
				.setTextAlignment(TextAlignment.RIGHT)
				.add(profile.getGoTo(String.valueOf(page), destination));
		table.addCell(new Cell().add(p).setBorder(Border.NO_BORDER).setPaddingRight(10f))
				.addCell(new Cell().add(p2).setBorder(Border.NO_BORDER));
	}

	private void writeEventTocEntry(Table table, PdfProfile profile, int level, Event event) {
		final Image icon = Images.get(event.getSchemaClass(), profile.getFontSize() - 1);
		// This sub-table will manage to keep the text tabbed from the icon, even if the text has more than one line
		// so the hierarchy is: table(t1(icon, p1), p2)
		final Table t1 = new Table(2).setBorder(Border.NO_BORDER);
		final Paragraph p1 = profile.getParagraph()
				.setFontSize(profile.getFontSize() - 1)
				.setMultipliedLeading(1.0f)
				.setTextAlignment(TextAlignment.LEFT)
				.add(profile.getGoTo(event.getDisplayName(), event.getStId()));
		t1.addCell(new Cell().add(icon).setBorder(Border.NO_BORDER))
				.addCell(new Cell().add(p1).setBorder(Border.NO_BORDER));
		final Paragraph p2 = profile.getParagraph()
				.setFontSize(profile.getFontSize() - 1)
				.setMultipliedLeading(1.0f)
				.setTextAlignment(TextAlignment.RIGHT)
				.add(profile.getGoTo(String.valueOf(pages.get(event.getId())), event.getStId()));
		table.addCell(new Cell().add(t1).setBorder(Border.NO_BORDER).setPaddingRight(20f).setPaddingLeft(10 * level))
				.addCell(new Cell().add(p2).setBorder(Border.NO_BORDER));
	}

}

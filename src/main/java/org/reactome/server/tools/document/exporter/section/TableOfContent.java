package org.reactome.server.tools.document.exporter.section;

import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.List;
import com.itextpdf.layout.element.ListItem;
import com.itextpdf.layout.element.Paragraph;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.tools.document.exporter.DocumentArgs;
import org.reactome.server.tools.document.exporter.DocumentProperties;
import org.reactome.server.tools.document.exporter.style.Images;
import org.reactome.server.tools.document.exporter.style.PdfProfile;

import java.util.Arrays;
import java.util.Comparator;

public class TableOfContent implements Section {

	private static final java.util.List<String> classOrder = Arrays.asList("Pathway", "Reaction", "BlackBoxEvent");

	@Override
	public void render(Document document, DocumentProperties properties) {
		final PdfProfile profile = properties.getPdfProfile();
		final Event event = properties.getEvent();
		final DocumentArgs args = properties.getArgs();
		document.add(new AreaBreak());
		document.add(profile.getH1("Table of Contents", false));
		document.add(profile.getH3("Introduction").setAction(PdfAction.createGoTo("introduction")).setFontColor(profile.getLinkColor()));

		addToc(document, profile, event, args.getMaxLevel());
	}

	private void addToc(Document document, PdfProfile profile, Event event, int maxLevel) {
		document.add(profile.getH3("")
				.add(Images.get(event.getSchemaClass()))
				.add(" ")
				.add(event.getDisplayName())
				.setAction(PdfAction.createGoTo(event.getStId()))
				.setFontColor(profile.getLinkColor()));
		if (event instanceof Pathway) {
			final Pathway pathway = (Pathway) event;
			final List list = getList(profile, pathway, 0, maxLevel);
			document.add(list);
		}
	}

	private List getList(PdfProfile profile, Pathway pathway, int level, int maxLevel) {
		final java.util.List<Event> events = pathway.getHasEvent();
		events.sort(Comparator.comparingInt(o -> classOrder.indexOf(o.getSchemaClass())));
		final List list = new List()
				.setSymbolIndent(10)
				.setListSymbol("")
				.setFontColor(profile.getLinkColor());
		for (Event ev : events) {
			final Paragraph paragraph = profile.getParagraph("")
					.setMultipliedLeading(1f)
					.add(Images.get(ev.getSchemaClass(), profile.getFontSize() - 1))
					.add(" ")
					.add(ev.getDisplayName())
					.setAction(PdfAction.createGoTo(ev.getStId()));
			final ListItem listItem = new ListItem();
			listItem.add(paragraph);
			list.add(listItem);
			if (ev instanceof Pathway && level + 1 < maxLevel) {
				final ListItem item = new ListItem();
				item.add(getList(profile, (Pathway) ev, level + 1, maxLevel));
				listItem.add(item);
			}
		}
		return list;
	}
}

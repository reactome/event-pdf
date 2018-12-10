package org.reactome.server.tools.document.exporter.section;

import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.List;
import com.itextpdf.layout.element.ListItem;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.ListNumberingType;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.ReactionLikeEvent;
import org.reactome.server.tools.document.exporter.AnalysisData;
import org.reactome.server.tools.document.exporter.style.PdfProfile;

import java.util.Arrays;
import java.util.Comparator;

public class TableOfContent implements Section {

	private static final java.util.List<String> classOrder = Arrays.asList("Pathway", "Reaction", "BlackBoxEvent");

	@Override
	public void render(Document document, PdfProfile profile, AnalysisData analysisData, Event event) {
		document.add(new AreaBreak());
		document.add(profile.getH1("Table of Contents", false));

		document.add(profile.getH3(String.format("[%s] %s", event.getSchemaClass(), event.getDisplayName())).setAction(PdfAction.createGoTo(event.getStId())).setFontColor(profile.getLinkColor()));
		if (event instanceof Pathway) {
			final Pathway pathway = (Pathway) event;
			final java.util.List<Event> events = pathway.getHasEvent();
			events.sort(Comparator.comparingInt(o -> classOrder.indexOf(o.getSchemaClass())));
			final List list = new List(ListNumberingType.DECIMAL)
					.setSymbolIndent(10)
					.setFontColor(profile.getLinkColor());
			for (Event ev : events) {
				final Paragraph paragraph = profile.getParagraph(String.format("[%s] %s", ev.getSchemaClass(), ev.getDisplayName())).setAction(PdfAction.createGoTo(ev.getStId()));
				final ListItem listItem = new ListItem();
				listItem.add(paragraph);
				list.add(listItem);
			}
			document.add(list);
		} else if (event instanceof ReactionLikeEvent) {
			final ReactionLikeEvent reaction = (ReactionLikeEvent) event;

		}
	}
}

package org.reactome.server.tools.document.exporter.section;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.domain.model.InstanceEdit;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.Person;
import org.reactome.server.tools.document.exporter.DocumentContent;
import org.reactome.server.tools.document.exporter.style.Images;
import org.reactome.server.tools.document.exporter.style.PdfProfile;
import org.reactome.server.tools.document.exporter.util.HtmlParser;
import org.reactome.server.tools.document.exporter.util.PdfUtils;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Chuan-Deng dengchuanbio@gmail.com
 */
public class CoverPage implements Section {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

	@Override
	public void render(Document document, DocumentContent content) {
		document.add(Images.getLogo().scaleToFit(100, 100).setHorizontalAlignment(HorizontalAlignment.LEFT));
		final PdfProfile profile = content.getPdfProfile();
		final Event event = content.getEvent();
		// Empty space
		document.add(profile.getTitle(""));
		document.add(profile.getTitle(event.getDisplayName()));
		document.add(profile.getTitle(""));
		final List<String> authors = collectAuthors(event).stream().map(this::getName).sorted().collect(Collectors.toList());
		final String auth = String.join(", ", authors);
		document.add(profile.getParagraph(auth).setTextAlignment(TextAlignment.CENTER));

		for (Paragraph paragraph : HtmlParser.parseText(profile, PdfUtils.getProperty("cover.page.disclaimer"))) {
			document.add(paragraph.setTextAlignment(TextAlignment.CENTER));
		}
	}

	private String getName(Person person) {
		return person.getDisplayName() + ".";
	}

	private Collection<Person> collectAuthors(Event event) {
		final Set<Person> authors = new HashSet<>();
		for (InstanceEdit instanceEdit : event.getAuthored()) authors.addAll(instanceEdit.getAuthor());
		for (InstanceEdit instanceEdit : event.getEdited()) authors.addAll(instanceEdit.getAuthor());
		for (InstanceEdit instanceEdit : event.getReviewed()) authors.addAll(instanceEdit.getAuthor());
		for (InstanceEdit instanceEdit : event.getRevised()) authors.addAll(instanceEdit.getAuthor());
		if (event.getCreated() != null) authors.addAll(event.getCreated().getAuthor());
		if (event.getModified() != null) authors.addAll(event.getModified().getAuthor());
		if (event instanceof Pathway) {
			final Pathway pathway = (Pathway) event;
			for (Event hasEvent : pathway.getHasEvent()) authors.addAll(collectAuthors(hasEvent));
		}
		return authors;
	}


}

package org.reactome.server.tools.document.exporter.section;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.domain.model.InstanceEdit;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.Person;
import org.reactome.server.tools.document.exporter.DocumentContent;
import org.reactome.server.tools.document.exporter.profile.PdfProfile;
import org.reactome.server.tools.document.exporter.util.Diagrams;
import org.reactome.server.tools.document.exporter.util.HtmlParser;
import org.reactome.server.tools.document.exporter.util.Images;
import org.reactome.server.tools.document.exporter.util.Texts;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * @author Chuan-Deng dengchuanbio@gmail.com
 */
public class CoverPage implements Section {

	@Override
	public void render(Document document, DocumentContent content) {
		document.add(Images.getLogo().scaleToFit(100, 100).setHorizontalAlignment(HorizontalAlignment.LEFT));
		final PdfProfile profile = content.getPdfProfile();
		final Event event = content.getEvent();
		// Empty space
		document.add(profile.getTitle(""));
		document.add(profile.getTitle(event.getDisplayName()));
		document.add(profile.getTitle(""));
		if (event instanceof Pathway)
			Diagrams.insertDiagram(event.getStId(), content.getAnalysisData(), document);
		document.add(profile.getTitle(""));
		final Collection<Person> people = collectAuthors(event);
		final String authors = people.stream()
				.map(this::getName)
				.sorted()
				.distinct()
				.map(this::getIndivisibleString)
				.collect(Collectors.joining(", "));
		document.add(profile.getParagraph(authors).setTextAlignment(TextAlignment.CENTER));
//		final String affiliations = people.stream()
//				.map(Person::getAffiliation)
//				.flatMap(Collection::stream)
//				.map(Affiliation::getDisplayName)
//				.distinct()
//				.sorted()
//				.collect(Collectors.joining("\n"));
//		document.add(profile.getParagraph(affiliations).setTextAlignment(TextAlignment.CENTER));

		document.add(HtmlParser.parseParagraph(profile, Texts.getProperty("cover.page.institutions"))
				.setTextAlignment(TextAlignment.CENTER));

		final Paragraph disclaimer = HtmlParser.parseParagraph(profile, Texts.getProperty("cover.page.disclaimer"))
				.setTextAlignment(TextAlignment.CENTER);
		document.add(new Div().setVerticalAlignment(VerticalAlignment.BOTTOM).setFillAvailableArea(true).add(disclaimer));

	}

	private String getIndivisibleString(String string) {
		final StringJoiner joiner = new StringJoiner("\u2060");
		for (int i = 0; i < string.length(); i++)
			joiner.add(Character.toString(string.charAt(i)));
		return joiner.toString().replaceAll("\\s+", "\u00A0");
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

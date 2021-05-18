package org.reactome.server.tools.event.exporter.section;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.tools.event.exporter.DocumentContent;
import org.reactome.server.tools.event.exporter.profile.PdfProfile;
import org.reactome.server.tools.event.exporter.util.Diagrams;
import org.reactome.server.tools.event.exporter.util.Images;
import org.reactome.server.tools.event.exporter.util.Texts;
import org.reactome.server.tools.event.exporter.util.html.HtmlProcessor;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Pascual Lorente (plorente@ebi.ac.uk)
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
		if (event instanceof Pathway)
			Diagrams.insertDiagram(event.getStId(), content.getAnalysisData(), document, content.getArgs());
		document.add(profile.getTitle(""));
		final List<Person> people = new ArrayList<>(collectAuthors(event, 0, content.getArgs().getMaxLevel()));
		people.sort(Comparator.comparing(Person::getDisplayName));
		final String authors = people.stream()
				.map(this::getName)
				.distinct()
				.map(this::getIndivisibleString)
				.collect(Collectors.joining(", "));
		document.add(profile.getParagraph(authors).setTextAlignment(TextAlignment.CENTER));

//		addAuthorsWithAffiliation(document, profile, people);

		final Div bottomDiv = new Div()
				.setKeepTogether(true)
				.setFillAvailableArea(true)
				.setVerticalAlignment(VerticalAlignment.BOTTOM)
				.setTextAlignment(TextAlignment.CENTER);
		Paragraph p;
		p = HtmlProcessor.createParagraph(Texts.getProperty("cover.page.institutions"), profile);
		bottomDiv.add(p.setTextAlignment(TextAlignment.CENTER));
		p = HtmlProcessor.createParagraph(Texts.getProperty("cover.page.disclaimer"), profile);
		bottomDiv.add(p.setTextAlignment(TextAlignment.CENTER));
		bottomDiv.add(profile.getParagraph(DATE_FORMAT.format(new Date())).setTextAlignment(TextAlignment.CENTER));
		document.add(bottomDiv);
	}

	private void addAuthorsWithAffiliation(Document document, PdfProfile profile, List<Person> people) {
		final Paragraph paragraph = profile.getParagraph();

		final List<String> names = new ArrayList<>();
		final List<String> affiliations = new ArrayList<>();
		for (int i = 0; i < people.size(); i++) {
			final Person person = people.get(i);
			if (!names.contains(person.getDisplayName())) {
				final List<Integer> affIndex = new ArrayList<>();
				final StringJoiner aff = new StringJoiner(", ", "(", ")").setEmptyValue("");
				for (Affiliation affiliation : person.getAffiliation()) {
					if (!affiliations.contains(affiliation.getDisplayName())) {
						affiliations.add(affiliation.getDisplayName());
					}
					affIndex.add(1 + affiliations.indexOf(affiliation.getDisplayName()));
					aff.add(String.valueOf(1 + affiliations.indexOf(affiliation.getDisplayName())));
				}
				names.add(getIndivisibleString(person.getDisplayName() + "." + aff.toString()));
				paragraph.add(new Text(getIndivisibleString(person.getDisplayName() + ".")));
				if (!affIndex.isEmpty()) {
					affIndex.sort(Comparator.naturalOrder());
					paragraph.add(new Text(getIndivisibleString(affIndex.stream().map(String::valueOf).collect(Collectors.joining(", "))))
							.setTextRise(5).setFontSize(6));
				}
			}
			if (i < people.size() - 1) paragraph.add(", ");

		}
		document.add(paragraph);

		final StringJoiner joiner = new StringJoiner("\n");
		for (int i = 0; i < affiliations.size(); i++) {
			String affiliation = affiliations.get(i);
			joiner.add(String.format("%d. %s", i + 1, affiliation));
		}
		document.add(profile.getParagraph(joiner.toString()));
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

	private Collection<Person> collectAuthors(Event event, int level, int maxLevel) {
		final Set<Person> authors = new HashSet<>();
		if (event.getAuthored() != null) {
			for (InstanceEdit instanceEdit : event.getAuthored()) authors.addAll(instanceEdit.getAuthor());
		}
		if (event.getEdited() != null) {
			for (InstanceEdit instanceEdit : event.getEdited()) authors.addAll(instanceEdit.getAuthor());
		}
		if (event.getReviewed() != null) {
			for (InstanceEdit instanceEdit : event.getReviewed()) authors.addAll(instanceEdit.getAuthor());
		}
		if (event.getRevised() != null) {
			for (InstanceEdit instanceEdit : event.getRevised()) authors.addAll(instanceEdit.getAuthor());
		}
		if (event instanceof Pathway && level < maxLevel) {
			final Pathway pathway = (Pathway) event;
			for (Event hasEvent : pathway.getHasEvent()) authors.addAll(collectAuthors(hasEvent, level + 1, maxLevel));
		}
		return authors;
	}


}

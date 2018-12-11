package org.reactome.server.tools.document.exporter.section;

import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.List;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.ListNumberingType;
import org.reactome.server.analysis.core.result.model.FoundEntities;
import org.reactome.server.analysis.core.result.model.FoundInteractors;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.tools.document.exporter.AnalysisData;
import org.reactome.server.tools.document.exporter.DocumentArgs;
import org.reactome.server.tools.document.exporter.DocumentProperties;
import org.reactome.server.tools.document.exporter.style.Images;
import org.reactome.server.tools.document.exporter.style.PdfProfile;
import org.reactome.server.tools.document.exporter.util.ApaStyle;
import org.reactome.server.tools.document.exporter.util.HtmlParser;
import org.reactome.server.tools.document.exporter.util.ImageFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Section PathwaysDetails contains the detail info for top hit pathways(sorted by
 * p-value), include the overlay diagram image, entities mapped and description
 * for each pathway.
 *
 * @author Chuan-Deng dengchuanbio@gmail.com
 */
public class PathwaysDetails implements Section {

	private static final String CONTENT_DETAIL = "/content/detail/";
	private static final java.util.List<String> classOrder = Arrays.asList("Pathway", "Reaction", "BlackBoxEvent");

	@Override
	public void render(Document document, DocumentProperties properties) {
		details(document, properties, properties.getEvent(), Collections.emptyList(), 1, 0);
		properties.setNav(null);
	}

	private void details(Document document, DocumentProperties properties, Event event, java.util.List<Event> nav, int index, int level) {
		final PdfProfile profile = properties.getPdfProfile();
		final AnalysisData analysisData = properties.getAnalysisData();
		final DocumentArgs args = properties.getArgs();
		properties.setNav(nav);
		document.add(new AreaBreak());
		document.add(getTitle(profile, event, index, level));
		if (event instanceof Pathway) {
			ImageFactory.insertDiagram(event.getStId(), analysisData, document);
		} else if (event instanceof ReactionLikeEvent) {
			ImageFactory.insertReaction(event.getStId(), analysisData, document);
		}
		addDatabaseObjectList(document, "Cellular compartments", event.getCompartment(), profile);
		addRelatedDiseases(document, event, profile);
		addDatabaseObjectList(document, "Inferred from", event.getInferredFrom(), profile);
//
		addSummations(document, event, profile);
		addReferences(document, event, profile);
		addEditTable(document, event, profile);

		if (event instanceof Pathway && level < args.getMaxLevel()) {
			final Pathway pathway = (Pathway) event;
			final java.util.List<Event> events = pathway.getHasEvent();
			events.sort(Comparator.comparingInt(ev -> classOrder.indexOf(ev.getSchemaClass())));
			final ArrayList<Event> nav2 = new ArrayList<>(nav);
			nav2.add(event);
			for (int i = 0; i < events.size(); i++) {
				details(document, properties, events.get(i), nav2, i + 1, level + 1);
			}
		}
	}

	private BlockElement getTitle(PdfProfile profile, Event event, int index, int level) {
		if (level == 0) {
			return profile.getH3(String.format("[%s] %s", event.getSchemaClass(), event.getDisplayName()))
					.add(" (")
					.add(new Text(event.getStId())
							.setAction(PdfAction.createURI("http://reactome.org" + CONTENT_DETAIL + event.getStId()))
							.setFontColor(profile.getLinkColor()))
					.add(")")
					.setDestination(event.getStId());
		}
		final List list = new List(ListNumberingType.DECIMAL)
				.setItemStartIndex(index)
				.setFontSize(profile.getFontSize() + 2)
				.setBold()
				.setSymbolIndent(10);
		final ListItem item = new ListItem();
		final Paragraph paragraph = new Paragraph(String.format("[%s] %s", event.getSchemaClass(), event.getDisplayName()))
				.add(" (")
				.add(new Text(event.getStId())
						.setAction(PdfAction.createURI("http://reactome.org" + CONTENT_DETAIL + event.getStId()))
						.setFontColor(profile.getLinkColor()))
				.add(")")
				.setDestination(event.getStId());
		item.add(paragraph);
		list.add(item);
		return list;
	}

	private void addFoundElements(Document document, AnalysisData analysisData, Pathway pathway, PdfProfile profile) {
		final FoundEntities foundEntities = analysisData.getResult().getFoundEntities(pathway.getStId());
		if (foundEntities.getIdentifiers().isEmpty()) return;
		document.add(profile.getH3(String.format("Entities found in this pathway (%d)", foundEntities.getIdentifiers().size())));
		for (String resource : analysisData.getResources()) {
			document.add(profile.getParagraph(""));
			addIdentifiers(document, foundEntities.filter(resource), resource, profile);
		}
	}

	private void addIdentifiers(Document document, FoundEntities elements, String resource, PdfProfile profile) {
		if (elements.getIdentifiers().isEmpty()) return;
		final Table identifiersTable = elements.getExpNames() == null || elements.getExpNames().isEmpty()
				? Tables.createEntitiesTable(elements.getIdentifiers(), resource, profile)
				: Tables.getExpressionTable(elements.getIdentifiers(), resource, profile, elements.getExpNames());
		document.add(identifiersTable);
	}

	private void addFoundInteractors(Document document, AnalysisData analysisData, Pathway pathway, PdfProfile profile) {
		final FoundInteractors interactors = analysisData.getResult().getFoundInteractors(pathway.getStId());
		if (interactors.getIdentifiers().isEmpty()) return;
		document.add(profile.getH3(String.format("Interactors found in this pathway (%d)", interactors.getIdentifiers().size())));
		for (String resource : analysisData.getResources()) {
			addInteractorsTable(document, interactors.filter(resource), resource, profile);
		}
	}

	private void addInteractorsTable(Document document, FoundInteractors interactors, String resource, PdfProfile profile) {
		if (interactors.getIdentifiers().isEmpty()) return;
		final Table table = (interactors.getExpNames() == null || interactors.getExpNames().isEmpty())
				? Tables.getInteractorsTable(interactors.getIdentifiers(), resource, profile)
				: Tables.getInteractorsExpressionTable(interactors.getIdentifiers(), resource, profile, interactors.getExpNames());
		document.add(table);
	}

	private void addSummations(Document document, Event event, PdfProfile profile) {
		event.getSummation().stream()
				.map(summation -> HtmlParser.parseText(profile, summation.getText()))
				.flatMap(Collection::stream)
				.forEach(document::add);
	}

	private void addRelatedDiseases(Document document, Event event, PdfProfile profile) {
		if (event.getDisease() != null) {
			final java.util.List<Disease> diseases = event.getDisease().stream()
					.filter(disease -> !disease.getDisplayName().equals("disease"))
					.collect(Collectors.toList());
			addDatabaseObjectList(document, "Diseases", diseases, profile);
		}
	}

	private void addDatabaseObjectList(Document document, String title, Collection<? extends DatabaseObject> objects, PdfProfile profile) {
		if (objects != null && !objects.isEmpty()) {
			final java.util.List<String> list = objects.stream()
					.map(DatabaseObject::getDisplayName)
					.collect(Collectors.toList());
			final String body = String.join(", ", list) + ".";
			final Paragraph paragraph = profile.getParagraph("")
					.add(new Text(title + ": ").setFont(profile.getBoldFont()))
					.add(body);
			document.add(paragraph);
		}
	}

	private void addReferences(Document document, Event event, PdfProfile profile) {
		if (event.getLiteratureReference() != null) {
			document.add(profile.getH3("References"));
			event.getLiteratureReference().stream()
					.limit(5)
					.map(publication -> createPublication(publication, profile))
					.forEach(document::add);
		}
	}

	private Paragraph createPublication(Publication publication, PdfProfile profile) {
		final java.util.List<Text> texts = ApaStyle.toApa(publication);
		final Paragraph paragraph = profile.getParagraph("")
				.setFirstLineIndent(-15)
				.setPaddingLeft(15)
				.setMultipliedLeading(1);
		texts.forEach(paragraph::add);
		if (publication instanceof LiteratureReference) {
			final LiteratureReference reference = (LiteratureReference) publication;
			if (reference.getUrl() != null)
				paragraph.add(" ").add(Images.getLink(reference.getUrl(), profile.getFontSize() - 1f));
		} else if (publication instanceof URL) {
			final URL url = (URL) publication;
			paragraph.add(Images.getLink(url.getUniformResourceLocator(), profile.getFontSize() - 1f));
		}
		return paragraph;
	}

	private void addEditTable(Document document, Event event, PdfProfile profile) {
		document.add(profile.getH3("Edit history"));
		final java.util.List<Edition> editions = new LinkedList<>();
		if (event.getCreated() != null)
			editions.add(new Edition("Created", event.getCreated()));
		if (event.getModified() != null)
			editions.add(new Edition("Modified", event.getModified()));
		if (event.getAuthored() != null)
			event.getAuthored().forEach(instanceEdit -> editions.add(new Edition("Authored", instanceEdit)));
		if (event.getEdited() != null)
			event.getEdited().forEach(instanceEdit -> editions.add(new Edition("Edited", instanceEdit)));
		if (event.getReviewed() != null)
			event.getReviewed().forEach(instanceEdit -> editions.add(new Edition("Reviewed", instanceEdit)));
		if (event.getRevised() != null)
			event.getRevised().forEach(instanceEdit -> editions.add(new Edition("Revised", instanceEdit)));
		editions.sort(Comparator.comparing(Edition::getDate));
		final java.util.List<java.util.List<Edition>> edits = new ArrayList<>();
		ArrayList<Edition> current = new ArrayList<>();
		current.add(editions.get(0));
		edits.add(current);
		for (int i = 1; i < editions.size(); i++) {
			if (!editions.get(i).getDate().equals(editions.get(i - 1).getDate())
					|| !editions.get(i).getAuthors().equals(editions.get(i - 1).getAuthors())) {
				current = new ArrayList<>();
				current.add(editions.get(i));
				edits.add(current);
			} else {
				current.add(editions.get(i));
			}
		}

		final Table table = new Table(new float[]{0.2f, 0.2f, 1f});
		table.useAllAvailableWidth();
		table.setBorder(Border.NO_BORDER);
		table.addHeaderCell(profile.getHeaderCell("Date"));
		table.addHeaderCell(profile.getHeaderCell("Action"));
		table.addHeaderCell(profile.getHeaderCell("Author"));
		for (int row = 0; row < edits.size(); row++) {
			java.util.List<Edition> list = edits.get(row);
			final String date = list.get(0).getDate();
			final String action = list.stream().map(Edition::getType).distinct().collect(Collectors.joining(", "));
			final String authors = asString(list.stream().map(Edition::getAuthors).flatMap(Collection::stream).collect(Collectors.toSet()));
			table.addCell(profile.getBodyCell(date, row));
			table.addCell(profile.getBodyCell(action, row));
			table.addCell(profile.getBodyCell(authors, row));
		}
		document.add(table);

	}

	private String asString(Collection<Person> persons) {
		return asString(persons, 5);
	}

	private String asString(Collection<Person> persons, int maxAuthors) {
		if (persons == null) return "";
		String text = String.join(", ", persons.stream()
				.limit(maxAuthors)
				.map(this::compileName)
				.collect(Collectors.toList()));
		if (persons.size() > maxAuthors) text += " et al.";
		return text;
	}

	private String compileName(Person person) {
		if (person.getSurname() != null && person.getInitial() != null)
			return person.getSurname() + " " + person.getInitial();
		if (person.getSurname() != null && person.getFirstname() != null)
			return person.getSurname() + " " + initials(person.getFirstname());
		if (person.getSurname() != null) return person.getSurname();
		return person.getFirstname();
	}

	private String initials(String name) {
		return Arrays.stream(name.split(" "))
				.map(n -> n.substring(0, 1).toUpperCase())
				.collect(Collectors.joining(" "));
	}

	private class Edition {
		private final String type;
		private final java.util.List<Person> authors;
		private final String date;

		Edition(String type, InstanceEdit instanceEdit) {
			this.type = type;
			this.authors = instanceEdit.getAuthor();
			this.date = instanceEdit.getDateTime().substring(0, 10);
		}

		public String getType() {
			return type;
		}

		java.util.List<Person> getAuthors() {
			return authors;
		}

		String getDate() {
			return date;
		}
	}
}

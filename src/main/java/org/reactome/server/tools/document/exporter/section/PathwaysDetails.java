package org.reactome.server.tools.document.exporter.section;

import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import org.reactome.server.analysis.core.model.AnalysisType;
import org.reactome.server.analysis.core.result.model.FoundEntity;
import org.reactome.server.analysis.core.result.model.FoundInteractors;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.exception.CustomQueryException;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.tools.document.exporter.AnalysisData;
import org.reactome.server.tools.document.exporter.DocumentArgs;
import org.reactome.server.tools.document.exporter.DocumentContent;
import org.reactome.server.tools.document.exporter.style.PdfProfile;
import org.reactome.server.tools.document.exporter.util.ApaStyle;
import org.reactome.server.tools.document.exporter.util.HtmlParser;
import org.reactome.server.tools.document.exporter.util.ImageFactory;

import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Section PathwaysDetails contains the detail info for top hit pathways(sorted by
 * p-value), include the overlay diagram image, entities mapped and description
 * for each pathway.
 */
public class PathwaysDetails implements Section {

	private static final List<String> EDIT_ORDER = Arrays.asList("Authored", "Created", "Edited", "Modified", "Reviewed", "Revised");
	private static final String CONTENT_DETAIL = "/content/detail/";
	private static final java.util.List<String> classOrder = Arrays.asList("Pathway", "Reaction", "BlackBoxEvent");

	// This should be used the same way as in the TOC,
	// to get the anchor for an event call get(ev.getStId),
	// to create a new anchor, get(ev.stId).incrementAndGet()
	private final Map<String, AtomicLong> destinations = new TreeMap<>();
	private AdvancedDatabaseObjectService advancedDatabaseObjectService;

	public PathwaysDetails(AdvancedDatabaseObjectService advancedDatabaseObjectService) {
		this.advancedDatabaseObjectService = advancedDatabaseObjectService;
	}
	@Override
	public void render(Document document, DocumentContent content) {
		details(document, content, content.getEvent(), Collections.emptyList(), 0);
	}

	private void details(Document document, DocumentContent content, Event event, java.util.List<Event> nav, int level) {
		final PdfProfile profile = content.getPdfProfile();
		final AnalysisData analysisData = content.getAnalysisData();
		final DocumentArgs args = content.getArgs();
		document.add(new AreaBreak());
		addTitle(document, content, event, profile);
		addLocation(document, nav, profile);
		addType(document, event, profile);
//		insertStId(document, properties, event, profile);
		addDatabaseObjectList(document, "Cellular compartments", event.getCompartment(), profile);
		addRelatedDiseases(document, event, profile);
		addDatabaseObjectList(document, "Inferred from", event.getInferredFrom(), profile);

		addDiagram(document, event, analysisData);

		addSummations(document, event, profile);
		addPrecedingAndFollowing(document, event, profile);
		addReferences(document, event, profile);
		addEditTable(document, event, profile);

		if (content.getAnalysisData() != null) {
			addFoundElements(document, content.getAnalysisData(), event, content.getPdfProfile());
			addFoundInteractors(document, content.getAnalysisData(), event, content.getPdfProfile());
		}
		if (level < args.getMaxLevel() && event instanceof Pathway) {
			final Pathway pathway = (Pathway) event;
			final java.util.List<Event> events = pathway.getHasEvent();
			events.sort(Comparator.comparingInt(ev -> classOrder.indexOf(ev.getSchemaClass())));
			final ArrayList<Event> nav2 = new ArrayList<>(nav);
			nav2.add(event);
			for (Event ev : events) {
				details(document, content, ev, nav2, level + 1);
			}
		}
	}

	private void addPrecedingAndFollowing(Document document, Event event, PdfProfile profile) {
		if (event instanceof ReactionLikeEvent) {
			addEvents(document, profile, "Preceded by", event.getPrecedingEvent());
			addEvents(document, profile, "Followed by", event.getFollowingEvent());
		}
	}

	private void addEvents(Document document, PdfProfile profile, String title, List<Event> events) {
		if (events.isEmpty()) return;
		final Paragraph preceding = profile.getParagraph("").add(new Text(title + ": ").setFont(profile.getBoldFont()));
		for (int i = 0; i < events.size(); i++) {
			final Event ev = events.get(i);
			if (i > 0) preceding.add(", ");
			preceding.add(profile.getGoTo(ev.getDisplayName(), getCurrentDestination(ev)));
		}
		document.add(preceding);
	}

	private Document addTitle(Document document, DocumentContent content, Event event, PdfProfile profile) {
		return document.add(getTitle(profile, event, content.getServer()));
	}

	private void addStId(Document document, DocumentContent properties, Event event, PdfProfile profile) {
		final Paragraph paragraph = profile.getParagraph("")
				.add(new Text("Stable identifier: ").setFont(profile.getBoldFont()))
				.add(new Text(event.getStId())
						.setAction(PdfAction.createURI(properties.getServer() + CONTENT_DETAIL + event.getStId()))
						.setFontColor(profile.getLinkColor())
						.setDestination(event.getStId()));
		document.add(paragraph);
	}

	private void addType(Document document, Event event, PdfProfile profile) {
		if (event instanceof ReactionLikeEvent) {
			final String type = ((ReactionLikeEvent) event).getCategory();
			final Paragraph paragraph = profile.getParagraph("")
					.add(new Text("Type: ").setFont(profile.getBoldFont()))
					.add(type);
			document.add(paragraph);
		}
	}

	private void addDiagram(Document document, Event event, AnalysisData analysisData) {
		if (event instanceof Pathway) {
			ImageFactory.insertDiagram(event.getStId(), analysisData, document);
		} else if (event instanceof ReactionLikeEvent) {
			ImageFactory.insertReaction(event.getStId(), analysisData, document);
		}
	}

	private void addLocation(Document document, List<Event> nav, PdfProfile profile) {
		if (nav.isEmpty()) return;
		final Paragraph paragraph = profile.getParagraph("")
				.add(new Text("Location: ").setFont(profile.getBoldFont()));
		for (int i = 0; i < nav.size(); i++) {
			if (i > 0) paragraph.add(" > ");  // current font does not support RIGHTARROW'\u2192'
			final Event ev = nav.get(i);
			final String dest = getCurrentDestination(ev);
			final Text text = new Text(ev.getDisplayName())
					.setAction(PdfAction.createGoTo(dest))
					.setFontColor(profile.getLinkColor());
			paragraph.add(text);
		}
		document.add(paragraph);
	}

	private BlockElement getTitle(PdfProfile profile, Event event, String server) {
		final String destination = getNewDestination(event);
		return profile.getH3(event.getDisplayName())
				.add(" (")
				.add(new Text(event.getStId())
						.setAction(PdfAction.createURI(server + CONTENT_DETAIL + event.getStId()))
						.setFontColor(profile.getLinkColor()))
				.add(")")
				.setDestination(destination);
	}

	private String getCurrentDestination(Event ev) {
		// here we don't use computeIfAbsent to insert a new AtomicLong(1).
		// Otherwise, when we call for getNewDestination, it will get a 2
		final long index = destinations.getOrDefault(ev.getStId(), new AtomicLong(1)).get();
		return String.format("%s:%d", ev.getStId(), index);
	}

	private String getNewDestination(Event event) {
		final long index = destinations.computeIfAbsent(event.getStId(), stId -> new AtomicLong()).incrementAndGet();
		return String.format("%s:%d", event.getStId(), index);
	}

	private void addFoundElements(Document document, AnalysisData analysisData, Event event, PdfProfile profile) {
		final Collection<FoundEntity> entities = getFoundEntities(analysisData, event);
		if (entities.isEmpty()) return;
		document.add(profile.getH3(String.format("Entities found in the analysis (%d)", entities.size())));
		for (String resource : analysisData.getResources()) {
			addIdentifiers(document, entities, resource, analysisData, profile);
		}
	}

	private Collection<FoundEntity> getFoundEntities(AnalysisData analysisData, Event event) {
		if (event instanceof Pathway)
			return analysisData.getResult().getFoundEntities(event.getStId()).filter(analysisData.getResource()).getIdentifiers();
		final ReactionLikeEvent reaction = (ReactionLikeEvent) event;
		if (reaction.getEventOf().isEmpty()) return Collections.emptyList();
		final List<FoundEntity> identifiers = analysisData.getResult().getFoundEntities(reaction.getEventOf().get(0).getStId()).filter(analysisData.getResource()).getIdentifiers();
		final Collection<String> idsInEvent = getIdsInReaction(reaction);
		final Collection<FoundEntity> found = new ArrayList<>();
		for (FoundEntity identifier : identifiers) {
			if (identifier.getMapsTo().stream().anyMatch(map -> map.getIds().stream().anyMatch(idsInEvent::contains)))
				found.add(identifier);
		}
		return found;
	}

	private Collection<String> getIdsInReaction(ReactionLikeEvent reaction) {
		final Map<String, Object> map = new HashMap<>();
		map.put("stId", reaction.getStId());
		final String query = "" +
				"MATCH (r:ReactionLikeEvent{stId:{stId}})," +
				" (r)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity)," +
				" (pe)-[:referenceEntity]->(re:ReferenceEntity)" +
				" RETURN DISTINCT re.identifier";
		try {
			return advancedDatabaseObjectService.getCustomQueryResults(String.class, query, map);
		} catch (CustomQueryException e) {
			e.printStackTrace();
		}

		return Collections.emptyList();
	}

	private void addIdentifiers(Document document, Collection<FoundEntity> elements, String resource, AnalysisData analysisData, PdfProfile profile) {
		if (elements.isEmpty()) return;
		final Table identifiersTable = analysisData.getType() == AnalysisType.EXPRESSION
				? Tables.getExpressionTable(elements, resource, profile, analysisData.getResult().getExpressionSummary().getColumnNames())
				: Tables.createEntitiesTable(elements, resource, profile);
		document.add(identifiersTable);
	}

	private void addFoundInteractors(Document document, AnalysisData analysisData, Event event, PdfProfile profile) {
		final FoundInteractors interactors = analysisData.getResult().getFoundInteractors(event.getStId());
		if (interactors == null) return;
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
			final String body = objects.stream()
					.map(DatabaseObject::getDisplayName)
					.collect(Collectors.joining(", "));
			final Paragraph paragraph = profile.getParagraph("")
					.add(new Text(title + ": ").setFont(profile.getBoldFont()))
					.add(body);
			document.add(paragraph);
		}
	}

	private void addReferences(Document document, Event event, PdfProfile profile) {
		if (event.getLiteratureReference().isEmpty()) return;
		document.add(profile.getH3("Literature references"));
		event.getLiteratureReference().stream()
				.limit(5)
				.map(publication -> createPublication(publication, profile))
				.forEach(document::add);
	}

	private Paragraph createPublication(Publication publication, PdfProfile profile) {
		final java.util.List<Text> texts = ApaStyle.toApa(publication);
		final Paragraph paragraph = profile.getParagraph("")
				.setFirstLineIndent(-15)
				.setPaddingLeft(30)
				.setFontSize(profile.getFontSize() - 1f)
				.setMultipliedLeading(1);
		texts.forEach(paragraph::add);
		if (publication instanceof LiteratureReference) {
			final LiteratureReference reference = (LiteratureReference) publication;
			if (reference.getUrl() != null)
//				paragraph.add(" ").add(Images.getLink(reference.getUrl(), profile.getFontSize() - 2f));
				paragraph.add(" ").add(new Text("link").setFontColor(profile.getLinkColor()).setAction(PdfAction.createURI(reference.getUrl())));
		} else if (publication instanceof URL) {
			final URL url = (URL) publication;
//			paragraph.add(Images.getLink(url.getUniformResourceLocator(), profile.getFontSize() - 2f));
			paragraph.add(" ").add(new Text("link").setFontColor(profile.getLinkColor()).setAction(PdfAction.createURI(url.getUniformResourceLocator())));
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
		editions.removeIf(edition -> edition.getDate() == null || edition.getAuthors() == null || edition.getAuthors().isEmpty());
		editions.sort(Comparator.comparing(Edition::getDate).thenComparing(edition -> edition.getAuthors().get(0).getSurname()));
		final java.util.List<java.util.List<Edition>> edits = new ArrayList<>();
		ArrayList<Edition> current = new ArrayList<>();
		current.add(editions.get(0));
		edits.add(current);
		for (int i = 1; i < editions.size(); i++) {
			final Edition edition = editions.get(i);
			if (!edition.getDate().equals(editions.get(i - 1).getDate())
					|| !edition.getAuthors().equals(editions.get(i - 1).getAuthors())) {
				current = new ArrayList<>();
				current.add(edition);
				edits.add(current);
			} else {
				current.add(edition);
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
			final String action = list.stream().map(Edition::getType).distinct().sorted(Comparator.comparingInt(EDIT_ORDER::indexOf))
					.collect(Collectors.joining(", "));
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
		if (persons.isEmpty()) return "";
		String text = persons.stream()
				.limit(maxAuthors)
				.map(this::getGetDisplayName)
				.collect(Collectors.joining(", "));
		if (persons.size() > maxAuthors) text += " et al.";
		return text;
	}

	private String getGetDisplayName(Person person) {
		return person.getDisplayName() + ".";
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

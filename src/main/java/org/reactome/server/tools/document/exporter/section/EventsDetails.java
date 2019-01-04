package org.reactome.server.tools.document.exporter.section;

import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import org.reactome.server.analysis.core.model.AnalysisType;
import org.reactome.server.analysis.core.result.model.FoundEntity;
import org.reactome.server.analysis.core.result.model.FoundInteractors;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.service.ParticipantService;
import org.reactome.server.tools.document.exporter.AnalysisData;
import org.reactome.server.tools.document.exporter.DocumentArgs;
import org.reactome.server.tools.document.exporter.DocumentContent;
import org.reactome.server.tools.document.exporter.profile.PdfProfile;
import org.reactome.server.tools.document.exporter.util.Diagrams;
import org.reactome.server.tools.document.exporter.util.HtmlParser;
import org.reactome.server.tools.document.exporter.util.References;
import org.reactome.server.tools.document.exporter.util.Tables;

import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main section: shows name, type, diagram, summations, references, editions and more information about each event.
 */
public class EventsDetails implements Section {

	private static final List<String> EDIT_ORDER = Arrays.asList("Authored", "Created", "Edited", "Modified", "Reviewed", "Revised");
	private static final String CONTENT_DETAIL = "/content/detail/";
	private static final java.util.List<String> classOrder = Arrays.asList("Pathway", "Reaction", "BlackBoxEvent");

	private final ParticipantService participantService;
	private final Set<Long> printed = new HashSet<>();
	private Map<Long, Integer> map;

	public EventsDetails(ParticipantService participantService, Map<Long, Integer> map) {
		this.participantService = participantService;
		this.map = map;
	}

	@Override
	public void render(Document document, DocumentContent content) {
		details(document, content, content.getEvent(), Collections.emptyList(), 0);
	}

	private void details(Document document, DocumentContent content, Event event, java.util.List<Event> nav, int level) {
		if (printed.contains(event.getId())) return;
		printed.add(event.getId());
		final PdfProfile profile = content.getPdfProfile();
		final AnalysisData analysisData = content.getAnalysisData();
		final DocumentArgs args = content.getArgs();
		document.add(new AreaBreak());

		final PdfPage page = document.getPdfDocument().getLastPage();
		final int number = document.getPdfDocument().getPageNumber(page);
		map.put(event.getId(), number - 1);

		addTitle(document, content, event, profile);
		addLocation(document, nav, profile, content);
//		addAlternativeLocations(document, nav, event, profile, content);
		addType(document, event, profile);
		addDatabaseObjectList(document, "Cellular compartments", event.getCompartment(), profile);
		addRelatedDiseases(document, event, profile);
		addInferred(document, event, profile, content);

		addDiagram(document, event, analysisData);

		addSummations(document, event, profile);
		addPrecedingAndFollowing(document, event, profile, content.getEvents());
		addReferences(document, event, profile);
		addEditTable(document, event, profile);

		// Analysis tables
		if (content.getAnalysisData() != null) {
			addFoundElements(document, content.getAnalysisData(), event, content.getPdfProfile());
			addFoundInteractors(document, content.getAnalysisData(), event, content.getPdfProfile());
		}

		// Call sub events
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

	private void addPrecedingAndFollowing(Document document, Event event, PdfProfile profile, Set<Event> contentEvents) {
		if (event instanceof ReactionLikeEvent) {
			addEvents(document, profile, "Preceded by", inDocument(event.getPrecedingEvent(), contentEvents));
			addEvents(document, profile, "Followed by", inDocument(event.getFollowingEvent(), contentEvents));
		}
	}

	private List<Event> inDocument(List<Event> events, Set<Event> contentEvents) {
		return events.stream().filter(contentEvents::contains).collect(Collectors.toList());
	}

	private void addInferred(Document document, Event event, PdfProfile profile, DocumentContent content) {
		if (event.getInferredFrom() == null || event.getInferredFrom().isEmpty()) return;
		final List<Event> events = new ArrayList<>(event.getInferredFrom());
		final Paragraph paragraph = profile.getParagraph()
				.add(new Text("Inferred from" + ": ").setFont(profile.getBoldFont()));
		for (int i = 0; i < events.size(); i++) {
			final Event ev = events.get(i);
			if (content.getEvents().contains(ev))
				paragraph.add(profile.getGoTo(events.get(i).getDisplayName(), ev.getStId()));
			else
				paragraph.add(profile.getLink(events.get(i).getDisplayName(), content.getServer() + CONTENT_DETAIL + ev.getStId()));
			if (i < events.size() - 1) paragraph.add(", ");
		}
		document.add(paragraph);
	}

	private void addEvents(Document document, PdfProfile profile, String title, List<Event> events) {
		if (events.isEmpty()) return;
		final Paragraph preceding = profile.getParagraph().add(new Text(title + ": ").setFont(profile.getBoldFont()));
		for (int i = 0; i < events.size(); i++) {
			final Event ev = events.get(i);
			if (i > 0) preceding.add(", ");
			preceding.add(profile.getGoTo(ev.getDisplayName(), ev.getStId()));
		}
		document.add(preceding);
	}

	private void addTitle(Document document, DocumentContent content, Event event, PdfProfile profile) {
		document.add(getTitle(profile, event, content.getServer()));
	}

	private void addType(Document document, Event event, PdfProfile profile) {
		if (event instanceof ReactionLikeEvent) {
			final String type = ((ReactionLikeEvent) event).getCategory();
			final Paragraph paragraph = profile.getParagraph()
					.add(new Text("Type: ").setFont(profile.getBoldFont()))
					.add(type);
			document.add(paragraph);
		}
	}

	private void addDiagram(Document document, Event event, AnalysisData analysisData) {
		if (event instanceof Pathway) {
			Diagrams.insertDiagram(event.getStId(), analysisData, document);
		} else if (event instanceof ReactionLikeEvent) {
			Diagrams.insertReaction(event.getStId(), analysisData, document);
		}
	}

	private void addLocation(Document document, List<Event> nav, PdfProfile profile, DocumentContent content) {
		if (nav.isEmpty()) return;
		document.add(getLocationParagraph(nav, profile, "Location", content));
	}

	private Paragraph getLocationParagraph(List<Event> nav, PdfProfile profile, String prefix, DocumentContent content) {
		final Paragraph paragraph = profile.getParagraph()
				.add(new Text(prefix + ": ").setFont(profile.getBoldFont()));
		for (int i = 0; i < nav.size(); i++) {
			if (i > 0) paragraph.add(" > ");  // current font does not support RIGHTARROW'\u2192'
			final Event ev = nav.get(i);
			final Text text = new Text(ev.getDisplayName());
			if (content.getEvents().contains(ev)) {
					text.setAction(PdfAction.createGoTo(ev.getStId()))
						.setFontColor(profile.getLinkColor());
			}
			paragraph.add(text);
		}
		return paragraph;
	}

	private void addAlternativeLocations(Document document, List<Event> nav, Event event, PdfProfile profile, DocumentContent content) {
		final List<List<Event>> navs = getLocations(event);
		for (List<Event> list : navs) list.remove(list.size() - 1); // event will appear ath the end of each list
		navs.removeIf(List::isEmpty);
		navs.removeIf(nav::equals);
		for (List<Event> events : navs) {
			document.add(getLocationParagraph(events, profile, "Alternative location", content));
		}
	}

	private List<List<Event>> getLocations(Event event) {
		if (event.getEventOf().isEmpty()){
			final List<Event> nav = new ArrayList<>();
			nav.add(event);
			final List<List<Event>> navs = new ArrayList<>();
			navs.add(nav);
			return navs;
		}
		final List<List<Event>> navs = new ArrayList<>();
		for (Pathway pathway : event.getEventOf()) {
			final List<List<Event>> subNavs = getLocations(pathway);
			for (List<Event> subNav : subNavs) {
				subNav.add(event);
				navs.add((subNav));
			}
		}
		return navs;
	}

	private BlockElement getTitle(PdfProfile profile, Event event, String server) {
		return profile.getH3(event.getDisplayName())
				.add(" (")
				.add(profile.getLink(event.getStId(), server + CONTENT_DETAIL + event.getStId()))
				.add(")")
				.setDestination(event.getStId());
	}

	private void addFoundElements(Document document, AnalysisData analysisData, Event event, PdfProfile profile) {
		final Collection<FoundEntity> entities = getFoundEntities(analysisData, event);
		if (entities.isEmpty()) return;
		final Div div = new Div().setKeepTogether(true);
		div.add(profile.getH3(String.format("Entities found in the analysis (%d)", entities.size())));
		for (String resource : analysisData.getResources()) {
			addIdentifiers(div, entities, resource, analysisData, profile);
		}
		document.add(div);
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
		return participantService.getParticipatingReferenceEntities(reaction.getStId()).stream()
				.map(ReferenceEntity::getIdentifier)
				.collect(Collectors.toSet());
	}

	private void addIdentifiers(Div div, Collection<FoundEntity> elements, String resource, AnalysisData analysisData, PdfProfile profile) {
		if (elements.isEmpty()) return;
		final Table identifiersTable = analysisData.getType() == AnalysisType.EXPRESSION
				? Tables.getExpressionTable(elements, resource, profile, analysisData.getResult().getExpressionSummary().getColumnNames())
				: Tables.createEntitiesTable(elements, resource, profile);
		div.add(identifiersTable);
	}

	private void addFoundInteractors(Document document, AnalysisData analysisData, Event event, PdfProfile profile) {
		final FoundInteractors interactors = analysisData.getResult().getFoundInteractors(event.getStId());
		if (interactors == null) return;
		if (interactors.getIdentifiers().isEmpty()) return;
		final Div div = new Div().setKeepTogether(true);
		div.add(profile.getH3(String.format("Interactors found in this pathway (%d)", interactors.getIdentifiers().size())));
		for (String resource : analysisData.getResources()) {
			addInteractorsTable(div, interactors.filter(resource), resource, profile);
		}
		document.add(div);
	}

	private void addInteractorsTable(Div div, FoundInteractors interactors, String resource, PdfProfile profile) {
		if (interactors.getIdentifiers().isEmpty()) return;
		final Table table = (interactors.getExpNames() == null || interactors.getExpNames().isEmpty())
				? Tables.getInteractorsTable(interactors.getIdentifiers(), resource, profile)
				: Tables.getInteractorsExpressionTable(interactors.getIdentifiers(), resource, profile, interactors.getExpNames());
		div.add(table);
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
			final Paragraph paragraph = profile.getParagraph()
					.add(new Text(title + ": ").setFont(profile.getBoldFont()))
					.add(body);
			document.add(paragraph);
		}
	}

	private void addReferences(Document document, Event event, PdfProfile profile) {
		if (event.getLiteratureReference().isEmpty()) return;
		document.add(profile.getH3("Literature references").setKeepWithNext(true));
		event.getLiteratureReference().stream()
				.limit(5)
				.map(publication -> References.getPublication(profile, publication))
				.forEach(document::add);
	}

	private void addEditTable(Document document, Event event, PdfProfile profile) {
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
			final Edition previousEdition = editions.get(i - 1);
			if (edition.getDate().equals(previousEdition.getDate())
					&& (edition.getType().equals(previousEdition.getType()) || edition.getAuthors().equals(previousEdition.getAuthors()))) {
				current.add(edition);
			} else {
				current = new ArrayList<>();
				current.add(edition);
				edits.add(current);
			}
		}

		final Table table = new Table(new float[]{0.2f, 0.2f, 1f});
		table.useAllAvailableWidth();
		table.setBorder(Border.NO_BORDER);
		table.setKeepTogether(true);
		for (int row = 0; row < edits.size(); row++) {
			java.util.List<Edition> list = edits.get(row);
			final String date = list.get(0).getDate();
			final String action = list.stream().map(Edition::getType).distinct().sorted(Comparator.comparingInt(EDIT_ORDER::indexOf))
					.collect(Collectors.joining(", "));
			final List<Person> people = list.stream().map(Edition::getAuthors).flatMap(Collection::stream).distinct().sorted().collect(Collectors.toList());
			final String authors = References.getAuthorList(people);
			table.addCell(profile.getBodyCell(date, row));
			table.addCell(profile.getBodyCell(action, row));
			table.addCell(profile.getBodyCell(authors, row));
		}
		document.add(profile.getH3("Editions").setKeepWithNext(true)).add(table);
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

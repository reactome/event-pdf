package org.reactome.server.tools.event.exporter.section;

import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import org.reactome.server.analysis.core.model.AnalysisType;
import org.reactome.server.analysis.core.result.model.FoundEntities;
import org.reactome.server.analysis.core.result.model.FoundEntity;
import org.reactome.server.analysis.core.result.model.FoundInteractor;
import org.reactome.server.analysis.core.result.model.FoundInteractors;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.service.ParticipantService;
import org.reactome.server.tools.event.exporter.AnalysisData;
import org.reactome.server.tools.event.exporter.DocumentArgs;
import org.reactome.server.tools.event.exporter.DocumentContent;
import org.reactome.server.tools.event.exporter.profile.PdfProfile;
import org.reactome.server.tools.event.exporter.util.Diagrams;
import org.reactome.server.tools.event.exporter.util.References;
import org.reactome.server.tools.event.exporter.util.Tables;
import org.reactome.server.tools.event.exporter.util.html.HtmlProcessor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main section: shows name, type, diagram, summations, references, editions and more information about each event.
 */
public class EventsDetails implements Section {

	private static final String CONTENT_DETAIL = "/content/detail/";

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
		addStableIdentifier(document, event, profile);
		addType(document, event, profile);
//		addAlternativeLocations(document, nav, event, profile, content);
		addCompartments(document, event, profile);
		addRelatedDiseases(document, event, profile);
		addInferred(document, event, profile, content);

		addDiagram(document, event, content);

		addSummations(document, event, profile);
		addPrecedingAndFollowing(document, event, profile, content.getEvents());
		addReferences(document, event, profile);
		addEditionsTable(document, event, profile);


		// Analysis tables
		if (content.getAnalysisData() != null) {
			addFoundEntities(document, content.getAnalysisData(), event, content.getPdfProfile());
			addFoundInteractors(document, content.getAnalysisData(), event, content.getPdfProfile());
		}

		// Call sub events
		if (level < args.getMaxLevel() && event instanceof Pathway) {
			final Pathway pathway = (Pathway) event;
			final java.util.List<Event> events = pathway.getHasEvent();
			final ArrayList<Event> nav2 = new ArrayList<>(nav);
			nav2.add(event);
			for (Event ev : events) {
				details(document, content, ev, nav2, level + 1);
			}
		}
	}

	private void addTitle(Document document, DocumentContent content, Event event, PdfProfile profile) {
		document.add(getTitle(profile, event, content.getArgs().getServerName()));
	}

	private Paragraph getTitle(PdfProfile profile, Event event, String server) {
		return profile.getH3(event.getDisplayName())
				.add(" ")
				.add(profile.getLink("\u2197", server + CONTENT_DETAIL + event.getStId()))
				.setDestination(event.getStId());
	}

	private void addLocation(Document document, List<Event> nav, PdfProfile profile, DocumentContent content) {
		if (nav.isEmpty()) return;
		document.add(getLocationParagraph(nav, profile, "Location", content));
	}

	private Paragraph getLocationParagraph(List<Event> nav, PdfProfile profile, String prefix, DocumentContent content) {
		final Paragraph paragraph = profile.getParagraph()
				.add(new Text(prefix + ": ").setFont(profile.getBold()));
		for (int i = 0; i < nav.size(); i++) {
			if (i > 0) paragraph.add(" \u2192 ");
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
		if (event.getEventOf().isEmpty()) {
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

	private void addStableIdentifier(Document document, Event event, PdfProfile profile) {
		document.add(profile.getParagraph()
				.add(new Text("Stable identifier: ").setFont(profile.getBold()))
				.add(event.getStId()));

	}

	private void addType(Document document, Event event, PdfProfile profile) {
		if (event instanceof ReactionLikeEvent) {
			final String type = ((ReactionLikeEvent) event).getCategory();
			final Paragraph paragraph = profile.getParagraph()
					.add(new Text("Type: ").setFont(profile.getBold()))
					.add(type);
			document.add(paragraph);
		}
	}

	private void addCompartments(Document document, Event event, PdfProfile profile) {
		addDatabaseObjectList(document, "Compartments", event.getCompartment(), profile);
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
					.add(new Text(title + ": ").setFont(profile.getBold()))
					.add(body);
			document.add(paragraph);
		}
	}

	private void addInferred(Document document, Event event, PdfProfile profile, DocumentContent content) {
		if (event.getInferredFrom() == null || event.getInferredFrom().isEmpty()) return;
		final List<Event> events = new ArrayList<>(event.getInferredFrom());
		final Paragraph paragraph = profile.getParagraph()
				.add(new Text("Inferred from" + ": ").setFont(profile.getBold()));
		for (int i = 0; i < events.size(); i++) {
			final Event ev = events.get(i);
			final String name = String.format("%s (%s)", ev.getDisplayName(), ev.getSpeciesName());
			if (content.getEvents().contains(ev))
				paragraph.add(profile.getGoTo(name, ev.getStId()));
			else
				paragraph.add(profile.getLink(name, content.getArgs().getServerName() + CONTENT_DETAIL + ev.getStId()));
			if (i < events.size() - 1) paragraph.add(", ");
		}
		document.add(paragraph);
	}

	private void addDiagram(Document document, Event event, DocumentContent content) {
		if (event instanceof Pathway) {
			Diagrams.insertDiagram(event.getStId(), content.getAnalysisData(), document, content.getArgs());
		} else if (event instanceof ReactionLikeEvent) {
			Diagrams.insertReaction(event.getStId(), content.getAnalysisData(), document, content.getArgs());
		}
	}

	private void addSummations(Document document, Event event, PdfProfile profile) {
		for (Summation summation : event.getSummation()) {
			HtmlProcessor.add(document, summation.getText(), profile);
		}
//		event.getSummation().stream()
//				.map(summation -> HtmlParser.parseText(profile, summation.getText()))
//				.flatMap(Collection::stream)
//				.forEach(document::add);
	}

	private void addPrecedingAndFollowing(Document document, Event event, PdfProfile profile, Set<Event> contentEvents) {
		if (event instanceof ReactionLikeEvent) {
			addEvents(document, profile, "Preceded by", inDocument(event.getPrecedingEvent(), contentEvents));
			addEvents(document, profile, "Followed by", inDocument(event.getFollowingEvent(), contentEvents));
		}
	}

	private void addEvents(Document document, PdfProfile profile, String title, List<Event> events) {
		if (events.isEmpty()) return;
		final Paragraph preceding = profile.getParagraph().add(new Text(title + ": ").setFont(profile.getBold()));
		for (int i = 0; i < events.size(); i++) {
			final Event ev = events.get(i);
			if (i > 0) preceding.add(", ");
			preceding.add(profile.getGoTo(ev.getDisplayName(), ev.getStId()));
		}
		document.add(preceding);
	}

	private List<Event> inDocument(List<Event> events, Set<Event> contentEvents) {
		return events.stream().filter(contentEvents::contains).collect(Collectors.toList());
	}

	private void addReferences(Document document, Event event, PdfProfile profile) {
		if (event.getLiteratureReference().isEmpty()) return;
		document.add(profile.getH3("Literature references").setKeepWithNext(true));
		event.getLiteratureReference().stream()
				.limit(5)
				.map(publication -> References.getPublication(profile, publication))
				.forEach(document::add);
	}

	private void addEditionsTable(Document document, Event event, PdfProfile profile) {
		final Table editionsTable = Tables.createEditionsTable(event, profile);
		if (editionsTable != null)
			document.add(profile.getH3("Editions").setKeepWithNext(true))
					.add(editionsTable);
	}

	private void addFoundEntities(Document document, AnalysisData analysisData, Event event, PdfProfile profile) {
		// Check the total entities found in the diagram
		final Collection<FoundEntity> totalEntities = getFoundEntities(analysisData, event, analysisData.getResource());
		if (totalEntities.isEmpty()) return;
		document.add(profile.getH3(String.format("Entities found in the analysis (%d)", totalEntities.size())).setKeepWithNext(true));
		// Split by resource
		for (String resource : analysisData.getResources()) {
			final Collection<FoundEntity> entities = getFoundEntities(analysisData, event, resource);
			if (entities.isEmpty()) continue;
			document.add(getEntitiesTable(entities, resource, analysisData, profile));
		}
	}

	@SuppressWarnings("Duplicates")
	private Collection<FoundEntity> getFoundEntities(AnalysisData analysisData, Event event, String resource) {
		// If event is a pathway we use standard AnalysisStoredResult methods
		if (event instanceof Pathway) {
			final FoundEntities entities = analysisData.getResult().getFoundEntities(event.getStId());
			if (entities == null) return Collections.emptyList();  // Pathway is not in the analysis
			return entities.filter(resource).getIdentifiers();
		}
		// If event is a reaction, we get the elements from the pathway it belongs, and filter them
		final ReactionLikeEvent reaction = (ReactionLikeEvent) event;
		if (reaction.getEventOf().isEmpty()) return Collections.emptyList();  // Orphan reaction
		final FoundEntities entities = analysisData.getResult().getFoundEntities(reaction.getEventOf().get(0).getStId());
		if (entities == null) return Collections.emptyList();  // Pathway is not in the analysis
		final List<FoundEntity> identifiers = entities.filter(resource).getIdentifiers();
		final Collection<String> idsInEvent = getIdsInReaction(reaction);
		return identifiers.stream()
				.filter(identifier -> inReaction(idsInEvent, identifier))
				.collect(Collectors.toList());
	}

	private Collection<String> getIdsInReaction(ReactionLikeEvent reaction) {
		return participantService.getParticipatingReferenceEntities(reaction.getStId()).stream()
				.map(ReferenceEntity::getIdentifier)
				.collect(Collectors.toSet());
	}

	private boolean inReaction(Collection<String> idsInEvent, FoundEntity identifier) {
		return identifier.getMapsTo().stream()
				.flatMap(map -> map.getIds().stream())
				.anyMatch(idsInEvent::contains);
	}

	private Table getEntitiesTable(Collection<FoundEntity> elements, String resource, AnalysisData analysisData, PdfProfile profile) {
		return analysisData.getType() == AnalysisType.EXPRESSION
				? Tables.getExpressionTable(elements, resource, profile, analysisData.getResult().getExpressionSummary().getColumnNames())
				: Tables.createEntitiesTable(elements, resource, profile);
	}

	private void addFoundInteractors(Document document, AnalysisData analysisData, Event event, PdfProfile profile) {
		// Check the total elements found in the diagram
		final Collection<FoundInteractor> entities = getFoundInteractors(analysisData, event, analysisData.getResource());
		if (entities.isEmpty()) return;
		document.add(profile.getH3(String.format("Interactors found in the analysis (%d)", entities.size())).setKeepWithNext(true));
		// Split by resource
		for (String resource : analysisData.getResources()) {
			final Collection<FoundInteractor> elements = getFoundInteractors(analysisData, event, resource);
			if (elements.isEmpty()) continue;
			document.add(getInteractorsTable(elements, resource, profile, analysisData));
		}
	}

	@SuppressWarnings("Duplicates")
	private Collection<FoundInteractor> getFoundInteractors(AnalysisData analysisData, Event event, String resource) {
		// If event is a pathway we use standard AnalysisStoredResult methods
		if (event instanceof Pathway) {
			final FoundInteractors interactors = analysisData.getResult().getFoundInteractors(event.getStId());
			if (interactors == null) return Collections.emptyList();  // Pathway is not in the analysis
			return interactors.filter(resource).getIdentifiers();
		}
		// If event is a reaction, we get the elements from the pathway it belongs, and filter them
		final ReactionLikeEvent reaction = (ReactionLikeEvent) event;
		if (reaction.getEventOf().isEmpty()) return Collections.emptyList();  // Orphan reaction
		final FoundInteractors interactors = analysisData.getResult().getFoundInteractors(reaction.getEventOf().get(0).getStId());
		if (interactors == null) return Collections.emptyList();  // Pathway is not in the analysis
		final List<FoundInteractor> identifiers = interactors.filter(resource).getIdentifiers();
		final Collection<String> idsInEvent = getIdsInReaction(reaction);
		return identifiers.stream()
				.filter(identifier -> inReaction(idsInEvent, identifier))
				.collect(Collectors.toList());
	}

	private boolean inReaction(Collection<String> idsInEvent, FoundInteractor identifier) {
		return identifier.getMapsTo().stream().anyMatch(idsInEvent::contains);
	}

	private Table getInteractorsTable(Collection<FoundInteractor> interactors, String resource, PdfProfile profile, AnalysisData data) {
		return (data.getType() == AnalysisType.EXPRESSION)
				? Tables.getInteractorsExpressionTable(interactors, resource, profile, data.getResult().getExpressionSummary().getColumnNames())
				: Tables.getInteractorsTable(interactors, resource, profile);
	}

}

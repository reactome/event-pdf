package org.reactome.server.tools.document.exporter.section;

import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.List;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.ListNumberingType;
import com.itextpdf.layout.property.TextAlignment;
import org.reactome.server.analysis.core.result.model.FoundEntities;
import org.reactome.server.analysis.core.result.model.FoundInteractors;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.tools.document.exporter.AnalysisData;
import org.reactome.server.tools.document.exporter.style.Images;
import org.reactome.server.tools.document.exporter.style.PdfProfile;
import org.reactome.server.tools.document.exporter.util.ApaStyle;
import org.reactome.server.tools.document.exporter.util.HtmlParser;
import org.reactome.server.tools.document.exporter.util.PdfUtils;

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

	private static final String PATHWAY_DETAIL = "/content/detail/";

	@Override
	public void render(Document document, PdfProfile profile, AnalysisData analysisData, Event event) {
		document.add(new AreaBreak());
		document.add(profile.getH1("Pathways details").setDestination("pathway-details"));
		document.add(profile.getParagraph(PdfUtils.getProperty("pathways.detail")));
		int i = 1;
//		for (PathwayData pathwayData : analysisData.getPathways()) {
//			final Pathway pathway = pathwayData.getPathway();
//			document.add(getTitle(profile, i, pathway, analysisData));
//			ImageFactory.insertDiagram(pathway.getStId(), analysisData.getResult(), analysisData.getResource(), document);
//			addDatabaseObjectList(document, "Cellular compartments", pathway.getCompartment(), profile);
//			addRelatedDiseases(document, pathway, profile);
//			addDatabaseObjectList(document, "Inferred from", pathway.getInferredFrom(), profile);
//
//			addSummations(document, pathway, profile);
//			addReferences(document, pathway, profile);
//
//			addEditTable(document, pathway, profile);
//
//			addFoundElements(document, analysisData, pathway, profile);
//
//			if (analysisData.isInteractors()) {
//				document.add(profile.getParagraph(""));
//				addFoundInteractors(document, analysisData, pathway, profile);
//			}
//
//			document.add(new AreaBreak());
//			document.flush();
//			i += 1;
//		}
	}

	private List getTitle(PdfProfile profile, int i, Pathway pathway, AnalysisData analysisData) {
		final List list = new List(ListNumberingType.DECIMAL)
				.setItemStartIndex(i)
				.setFontSize(profile.getFontSize() + 2)
				.setBold()
				.setSymbolIndent(10);
		final ListItem item = new ListItem();
		final Paragraph paragraph = new Paragraph(pathway.getDisplayName())
				.add(" (")
				.add(new Text(pathway.getStId())
						.setAction(PdfAction.createURI(analysisData.getServerName() + PATHWAY_DETAIL + pathway.getStId()))
						.setFontColor(profile.getLinkColor()))
				.add(")")
				.setDestination(pathway.getStId());
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

	private void addSummations(Document document, Pathway pathway, PdfProfile profile) {
		pathway.getSummation().stream()
				.map(summation -> HtmlParser.parseText(profile, summation.getText()))
				.flatMap(Collection::stream)
				.forEach(document::add);
	}

	private void addRelatedDiseases(Document document, Pathway pathwayDetail, PdfProfile profile) {
		if (pathwayDetail.getDisease() != null) {
			final java.util.List<Disease> diseases = pathwayDetail.getDisease().stream()
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

	private void addReferences(Document document, Pathway pathwayDetail, PdfProfile profile) {
		if (pathwayDetail.getLiteratureReference() != null) {
			document.add(profile.getH3("References"));
			pathwayDetail.getLiteratureReference().stream()
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

	private void addEditTable(Document document, Pathway pathway, PdfProfile profile) {
		document.add(profile.getH3("Edit history"));
		final java.util.List<Edition> editions = new LinkedList<>();
		if (pathway.getCreated() != null)
			editions.add(new Edition("Created", pathway.getCreated()));
		if (pathway.getModified() != null)
			editions.add(new Edition("Modified", pathway.getModified()));
		if (pathway.getAuthored() != null)
			pathway.getAuthored().forEach(instanceEdit -> editions.add(new Edition("Authored", instanceEdit)));
		if (pathway.getEdited() != null)
			pathway.getEdited().forEach(instanceEdit -> editions.add(new Edition("Edited", instanceEdit)));
		if (pathway.getReviewed() != null)
			pathway.getReviewed().forEach(instanceEdit -> editions.add(new Edition("Reviewed", instanceEdit)));
		if (pathway.getRevised() != null)
			pathway.getRevised().forEach(instanceEdit -> editions.add(new Edition("Revised", instanceEdit)));

		// Group by date and type
		final Map<String, Map<String, java.util.List<Edition>>> edits = editions.stream()
				.collect(Collectors.groupingBy(Edition::getDate,
						TreeMap::new,  // forces key sorting
						Collectors.groupingBy(Edition::getType)));

		final Table table = new Table(new float[]{0.2f, 0.2f, 1f});
		table.useAllAvailableWidth();
		table.setBorder(Border.NO_BORDER);
		table.addHeaderCell(profile.getHeaderCell("Date"));
		table.addHeaderCell(profile.getHeaderCell("Action"));
		table.addHeaderCell(profile.getHeaderCell("Author"));
		int row = 0;
		for (Map.Entry<String, Map<String, java.util.List<Edition>>> dateEntry : edits.entrySet()) {
			for (Map.Entry<String, java.util.List<Edition>> typeEntry : dateEntry.getValue().entrySet()) {
				table.addCell(profile.getBodyCell(dateEntry.getKey(), row));
				table.addCell(profile.getBodyCell(typeEntry.getKey(), row));
				final Set<Person> authors = typeEntry.getValue().stream()
						.map(Edition::getAuthors)
						.filter(Objects::nonNull)
						.flatMap(Collection::stream)
						.collect(Collectors.toSet());
				table.addCell(profile.getBodyCell(asString(authors), row).setTextAlignment(TextAlignment.LEFT).setPadding(5));
				row += 1;
			}
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

package org.reactome.server.tools.document.exporter.util;

import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;
import org.reactome.server.analysis.core.result.model.FoundEntity;
import org.reactome.server.analysis.core.result.model.FoundInteractor;
import org.reactome.server.analysis.core.result.model.IdentifierMap;
import org.reactome.server.analysis.core.result.model.IdentifierSummary;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.domain.model.InstanceEdit;
import org.reactome.server.graph.domain.model.Person;
import org.reactome.server.tools.document.exporter.profile.PdfProfile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class to add tables of entities to document.
 */
public class Tables {

	private static final String INPUT = "Input";
	private static final String DELIMITER = ", ";
	private static final String INTERACTS_WITH = "Interacts with";
	private static final int MAX_COLUMNS = 3;


	private Tables() {
	}

	/**
	 * Creates a table of identifiers of a expression analysis. Each row
	 * presents id, mapsTo, values[0], values[1]...
	 *
	 * @param entities    collection of entities to render
	 * @param resource    resource of entities
	 * @param profile     pdf profile to create elements
	 * @param columnNames list of column names for the expression columns
	 */
	public static Table getExpressionTable(Collection<FoundEntity> entities, String resource, PdfProfile profile, List<String> columnNames) {
		final int rows = Math.min(6, columnNames.size());
		final Table table = new Table(UnitValue.createPercentArray(2 + rows));
		table.useAllAvailableWidth();
		table.addHeaderCell(profile.getHeaderCell(INPUT));
		table.addHeaderCell(profile.getHeaderCell(resource + " Id"));
		for (int i = 0; i < rows; i++)
			table.addHeaderCell(profile.getHeaderCell(ellipsis(columnNames.get(i), 11)));
		int row = 0;
		for (FoundEntity entity : entities) {
			table.addCell(profile.getBodyCell(entity.getId(), row));
			table.addCell(profile.getBodyCell(toString(entity.getMapsTo()), row));
			for (int i = 0; i < rows; i++) {
				table.addCell(profile.getBodyCell(Texts.formatNumber(entity.getExp().get(i)), row));
			}
			row++;
		}
		return table;
	}

	private static String ellipsis(String text, int max) {
		if (text.length() < max) return text;
		return text.substring(0, Math.max(max - 3, 1)) + "...";
	}

	private static String toString(Set<IdentifierMap> identifier) {
		return identifier.stream()
				.flatMap(identifierMap -> identifierMap.getIds().stream())
				.sorted()
				.collect(Collectors.joining(DELIMITER));
	}

	/**
	 * This is a custom made layout to present n tables side by side. n is selected depending on the number of entities.
	 *
	 * @param entities list of items to add to the table
	 * @param resource name of the resource
	 * @param profile  pdf profile to create elements
	 * @return a table divided in n columns with id -> mapsTo sorted by id
	 */
	public static Table createEntitiesTable(Collection<FoundEntity> entities, String resource, PdfProfile profile) {
		int columns = getColumns(entities.size());
		return createEntitiesTable(entities, resource, profile, columns);
	}

	private static int getColumns(int elements) {
		int columns = 1;
		int minRows = elements;
		for (int i = 2; i <= MAX_COLUMNS; i++) {
			final int rows = (int) Math.ceil((double) elements / i);
			if (rows < minRows) {
				columns = i;
				minRows = rows;
			}
		}
		return columns;
	}

	private static Table createEntitiesTable(Collection<FoundEntity> entities, String resource, PdfProfile profile, int columns) {
		final float[] widths = new float[2 * columns + (columns - 1)];
		for (int i = 0; i < widths.length; i += 3) {
			widths[i] = 2f;
			widths[i + 1] = 2f;
			if (i + 2 < widths.length) {
				widths[i + 2] = 0.1f;
			}
		}
		// Create table and add headers
		final Table table = new Table(widths);
		table.useAllAvailableWidth();
		final String mapping = String.format("%s Id", resource);
		for (int i = 0; i < columns; i++) {
			table.addHeaderCell(profile.getHeaderCell(INPUT));
			table.addHeaderCell(profile.getHeaderCell(mapping));
			if (i < columns - 1) {
				table.addHeaderCell(profile.getBodyCell("", 0));
			}
		}

		// Add content
		final List<FoundEntity> identifiers = entities.stream()
				.sorted(Comparator.comparing(IdentifierSummary::getId))
				.distinct()
				.collect(Collectors.toList());
		int i = 0;
		int filled = 0;
		for (FoundEntity identifier : identifiers) {
			final int column = i % columns;
			final int row = i / columns;
			table.addCell(profile.getBodyCell(identifier.getId(), row));
			table.addCell(profile.getBodyCell(toString(identifier.getMapsTo()), row));
			filled += 2;
			if (column < columns - 1) {
				filled += 1;
				table.addCell(profile.getBodyCell("", 0));
			}
			i += 1;
		}
		while (filled++ % widths.length != 0) {
			table.addCell(profile.getBodyCell(null, 0));
		}
		return table;
	}

	public static Table getInteractorsTable(Collection<FoundInteractor> interactors, String resource, PdfProfile profile) {
		final Collection<FoundInteractor> sorted = interactors.stream()
				.distinct()
				.sorted(Comparator.comparing(IdentifierSummary::getId))
				.collect(Collectors.toList());
		final Table table = new Table(new float[]{1, 1, 1, 0.1f, 1, 1, 1});
		table.useAllAvailableWidth();
		final String mapping = String.format("%s Id", resource);
		table.addHeaderCell(profile.getHeaderCell(INPUT));
		table.addHeaderCell(profile.getHeaderCell(mapping));
		table.addHeaderCell(profile.getHeaderCell(INTERACTS_WITH));
		table.addHeaderCell(profile.getBodyCell("", 0));
		table.addHeaderCell(profile.getHeaderCell(INPUT));
		table.addHeaderCell(profile.getHeaderCell(mapping));
		table.addHeaderCell(profile.getHeaderCell(INTERACTS_WITH));
		int index = 0;
		int filled = 0;
		for (FoundInteractor interactor : sorted) {
			final int column = index % 2;
			final int row = index / 2;
			table.addCell(profile.getBodyCell(interactor.getId(), row));
			table.addCell(profile.getBodyCell(String.join(DELIMITER, interactor.getMapsTo()), row));
			table.addCell(profile.getBodyCell(String.join(DELIMITER, interactor.getInteractsWith().getIds()), row));
			filled += 3;
			if (column == 0) {
				table.addCell(profile.getBodyCell("", 0));
				filled += 1;
			}
			index++;
		}
		while (filled++ % table.getNumberOfColumns() != 0) {
			table.addCell(profile.getBodyCell(null, 0));
		}
		return table;
	}

	public static Table getInteractorsExpressionTable(Collection<FoundInteractor> interactors, String resource, PdfProfile profile, List<String> columns) {
		final int rows = Math.min(6, columns.size());
		final Table table = new Table(UnitValue.createPercentArray(3 + rows));
		table.useAllAvailableWidth();
		table.addHeaderCell(profile.getHeaderCell(INPUT));
		table.addHeaderCell(profile.getHeaderCell(resource + " Id"));
		table.addHeaderCell(profile.getHeaderCell(INTERACTS_WITH));
		for (int i = 0; i < rows; i++)
			table.addHeaderCell(profile.getHeaderCell(ellipsis(columns.get(i), 10)));
		int row = 0;
		for (FoundInteractor entity : interactors) {
			table.addCell(profile.getBodyCell(entity.getId(), row));
			table.addCell(profile.getBodyCell(String.join(DELIMITER, entity.getMapsTo()), row));
			table.addCell(profile.getBodyCell(String.join(DELIMITER, entity.getInteractsWith().getIds()), row));
			for (int i = 0; i < rows; i++) {
				table.addCell(profile.getBodyCell(Texts.formatNumber(entity.getExp().get(i)), row));
			}
			row++;
		}
		return table;
	}

	public static Table createEditionsTable(Event event, PdfProfile profile) {
		final List<Edition> editions = getEditions(event);

		// Group editions by date/authors or date/type
		final List<List<Edition>> edits = new ArrayList<>();
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
			List<Edition> list = edits.get(row);
			final String date = list.get(0).getDate();
			final String action = list.stream()
					.map(Edition::getType)
					.distinct()
					.sorted(Comparator.comparingInt(Enum::ordinal))
					.map(EditionType::toString)
					.collect(Collectors.joining(", "));
			final List<Person> people = list.stream().map(Edition::getAuthors)
					.flatMap(Collection::stream)
					.distinct()
					.sorted()
					.collect(Collectors.toList());
			final String authors = References.getAuthorList(people);
			table.addCell(profile.getBodyCell(date, row));
			table.addCell(profile.getBodyCell(action, row));
			table.addCell(profile.getBodyCell(authors, row));
		}
		return table;
	}

	/**
	 * Creates the list of editions sorted by date and surname of first author
	 */
	private static List<Edition> getEditions(Event event) {
		final List<Edition> editions = new LinkedList<>();
		if (event.getCreated() != null)
			editions.add(new Edition(EditionType.CREATED, event.getCreated()));
		if (event.getModified() != null)
			editions.add(new Edition(EditionType.MODIFIED, event.getModified()));
		if (event.getAuthored() != null)
			event.getAuthored().forEach(instanceEdit -> editions.add(new Edition(EditionType.AUTHORED, instanceEdit)));
		if (event.getEdited() != null)
			event.getEdited().forEach(instanceEdit -> editions.add(new Edition(EditionType.EDITED, instanceEdit)));
		if (event.getReviewed() != null)
			event.getReviewed().forEach(instanceEdit -> editions.add(new Edition(EditionType.REVIEWED, instanceEdit)));
		if (event.getRevised() != null)
			event.getRevised().forEach(instanceEdit -> editions.add(new Edition(EditionType.REVISED, instanceEdit)));
		editions.removeIf(edition -> edition.getDate() == null || edition.getAuthors() == null || edition.getAuthors().isEmpty());
		editions.sort(Comparator.comparing(Edition::getDate).thenComparing(edition -> edition.getAuthors().get(0).getSurname()));
		return editions;
	}

	private static class Edition {
		private final EditionType type;
		private final List<Person> authors;
		private final String date;

		Edition(EditionType type, InstanceEdit instanceEdit) {
			this.type = type;
			this.authors = instanceEdit.getAuthor();
			this.date = instanceEdit.getDateTime().substring(0, 10);
		}

		public EditionType getType() {
			return type;
		}

		List<Person> getAuthors() {
			return authors;
		}

		String getDate() {
			return date;
		}
	}
}

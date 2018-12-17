package org.reactome.server.tools.document.exporter.util;

import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;
import org.reactome.server.analysis.core.result.model.FoundEntity;
import org.reactome.server.analysis.core.result.model.FoundInteractor;
import org.reactome.server.analysis.core.result.model.IdentifierMap;
import org.reactome.server.analysis.core.result.model.IdentifierSummary;
import org.reactome.server.tools.document.exporter.profile.PdfProfile;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helper class to add tables of entities to document.
 */
public class Tables {

	private static final String INPUT = "Input";
	private static final String DELIMITER = ", ";
	private static final String INTERACTS_WITH = "Interacts with";


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

	private static void fillLastRow(Table table, int identifiers, int row, PdfProfile profile) {
		int n = identifiers % 3;
		int cols = 0;
		if (n == 0) cols = 0;
		if (n == 1) cols = 5;
		if (n == 2) cols = 2;
		for (int j = 0; j < cols; j++)
			table.addCell(profile.getBodyCell("", row));
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
		int columns = getColumns(entities.size(), 3);
		return createEntitiesTable(entities, resource, profile, columns);
	}

	private static int getColumns(int elements, int maxColumns) {
		int columns = 1;
		int minRows = elements;
		for (int i = 2; i <= maxColumns; i++) {
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
		for (int i = 0; i < widths.length; i+= 3) {
			widths[i] = 2f;
			widths[i + 1] = 2f;
			if (i + 2 < widths.length) {
				widths[i + 2] = 0.1f;
			}
		}

		final Table table = new Table(widths);
		table.useAllAvailableWidth();
		final String mapping = String.format("%s Id", resource);
		for (int i = 0; i < columns; i++) {
			table.addHeaderCell(profile.getHeaderCell(INPUT));
			table.addHeaderCell(profile.getHeaderCell(mapping));
			if (i + 2 < widths.length) {
				table.addHeaderCell(profile.getBodyCell("", 0));
			}
		}

		int i = 0;
		final List<FoundEntity> identifiers = entities.stream()
				.sorted(Comparator.comparing(IdentifierSummary::getId))
				.distinct()
				.collect(Collectors.toList());
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
			table.addCell(profile.getBodyCell("", 0));
		}
//		fillLastRow(table, identifiers.size(), 0, profile);
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
		for (FoundInteractor interactor : sorted) {
			final int column = index % 2;
			final int row = index / 2;
			table.addCell(profile.getBodyCell(interactor.getId(), row));
			table.addCell(profile.getBodyCell(String.join(DELIMITER, interactor.getMapsTo()), row));
			table.addCell(profile.getBodyCell(String.join(DELIMITER, interactor.getInteractsWith().getIds()), row));
			if (column == 0)
				table.addCell(profile.getBodyCell("", 0));
			index++;
		}
		fillLastRow(table, interactors.size(), 0, profile);
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

}

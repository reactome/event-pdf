package org.reactome.server.tools.document.exporter.section;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.reactome.server.analysis.core.result.PathwayNodeSummary;
import org.reactome.server.analysis.core.result.model.PathwayBase;
import org.reactome.server.tools.document.exporter.AnalysisData;
import org.reactome.server.tools.document.exporter.DocumentContent;
import org.reactome.server.tools.document.exporter.PathwayData;
import org.reactome.server.tools.document.exporter.profile.PdfProfile;
import org.reactome.server.tools.document.exporter.util.Texts;

import java.util.Arrays;

/**
 * Table of top pathways sorted by p-value.
 *
 * @author Chuan-Deng dengchuanbio@gmail.com
 */
public class TopPathwayTable implements Section {

	private static final java.util.List<String> HEADERS = Arrays.asList(
			"found",
			"ratio",
			"p-value",
			"FDR*",
			"found",
			"ratio"
	);

	@Override
	public void render(Document document, DocumentContent content) {
		final AnalysisData analysisData = content.getAnalysisData();
		final PdfProfile profile = content.getPdfProfile();
		document.add(profile.getH1("Most significant pathways").setDestination("pathway-list"));
		document.add(profile.getParagraph(Texts.getProperty("most.significant.pathways", analysisData.getPathways().size())));
		// Let iText decide the width of the columns
		final Table table = new Table(new float[]{3, 1, 1, 1, 1, 1, 1});
		table.setBorder(Border.NO_BORDER);
		table.useAllAvailableWidth();
		table.setFixedLayout();
		table.addHeaderCell(profile.getHeaderCell("Pathway name", 2, 1));
		table.addHeaderCell(profile.getHeaderCell("Entities", 1, 4));
		table.addHeaderCell(profile.getHeaderCell("Reactions", 1, 2));
		for (String header : HEADERS)
			table.addHeaderCell(profile.getHeaderCell(header));
		int i = 0;
		for (PathwayData pathwayData : analysisData.getPathways()) {
			final PathwayBase pathwayBase = pathwayData.getBase();
			final PathwayNodeSummary pathway = analysisData.getResult().getPathway(pathwayBase.getStId());
			table.addCell(profile.getPathwayCell(i, pathway));
			final String entities = String.format("%,d / %,d", pathwayBase.getEntities().getFound(), pathwayBase.getEntities().getTotal());
			table.addCell(profile.getBodyCell(entities, i));
			table.addCell(profile.getBodyCell(Texts.formatNumber(pathwayBase.getEntities().getRatio()), i));
			table.addCell(profile.getBodyCell(Texts.formatNumber(pathwayBase.getEntities().getpValue()), i));
			table.addCell(profile.getBodyCell(Texts.formatNumber(pathwayBase.getEntities().getFdr()), i));
			final String reactions = String.format("%,d / %,d",
					pathway.getData().getReactionsFound(),
					pathway.getData().getReactionsCount());
			table.addCell(profile.getBodyCell(reactions, i));
			table.addCell(profile.getBodyCell(Texts.formatNumber(pathway.getData().getReactionsRatio()), i));
			i++;
		}
		document.add(table);
		document.add(new Paragraph("* False Discovery Rate").setFontSize(profile.getFontSize() - 2));
	}


}

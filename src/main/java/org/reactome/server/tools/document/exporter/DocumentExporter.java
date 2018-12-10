package org.reactome.server.tools.document.exporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.model.ResourceSummary;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.DiagramService;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.tools.document.exporter.exception.DocumentExporterException;
import org.reactome.server.tools.document.exporter.section.*;
import org.reactome.server.tools.document.exporter.style.PdfProfile;
import org.reactome.server.tools.document.exporter.util.ImageFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DocumentExporter {

	private static final Long DEFAULT_SPECIES = 48887L;
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private final List<Section> SECTIONS = Arrays.asList(
			new CoverPage(),
			new TableOfContent(),
//			new Introduction(),
//			new PropertiesSection(),
//			new TopPathwayTable(),
			new PathwaysDetails()
	);
	private DatabaseObjectService databaseObjectService;


	public DocumentExporter(String diagramPath, String ehldPath, String analysisPath, String fireworksPath, String svgSummary, DiagramService diagramService, DatabaseObjectService databaseObjectService, GeneralService generalService) {
		ImageFactory.setPaths(diagramPath, ehldPath, analysisPath, fireworksPath, svgSummary);
		Locale.setDefault(Locale.ENGLISH);
		ImageFactory.setDiagramService(diagramService);
		AnalysisData.setDatabaseObjectService(databaseObjectService);
		AnalysisData.setGeneralService(generalService);
		this.databaseObjectService = databaseObjectService;
	}

	public void export(String stId, AnalysisStoredResult result, OutputStream destination, Long species, String resource) throws DocumentExporterException {

		final Event event = databaseObjectService.findById(stId);
		if (event == null) throw new DocumentExporterException(stId + " is not an event");

		final PdfProfile pdfProfile = loadProfile("breathe");

		if (species == null) species = DEFAULT_SPECIES;

		final AnalysisData analysisData;
		if (result != null) {
			// if the analysis result not contains the given resource, use the first resource in this analysis.
			if (!result.getResourceSummary().contains(new ResourceSummary(resource, null)))
				resource = getDefaultResource(result);

			analysisData = new AnalysisData(result, resource, species, Integer.MAX_VALUE);
		} else analysisData = null;

		try (Document document = new Document(new PdfDocument(new PdfWriter(destination)))) {
			document.getPdfDocument().getDocumentInfo().setAuthor(String.format("Reactome (%s)", "https://reactome.org"));
			document.getPdfDocument().getDocumentInfo().setCreator(String.format("Reactome (%s)", "https://reactome.org"));
			document.getPdfDocument().getDocumentInfo().setTitle("Reactome | " + stId);
			document.getPdfDocument().getDocumentInfo().setSubject("Reactome | " + stId);
			document.getPdfDocument().getDocumentInfo().setKeywords("pathway,reactome,reaction");
			document.setFont(pdfProfile.getRegularFont());
			document.setMargins(pdfProfile.getMargin().getTop(),
					pdfProfile.getMargin().getRight(),
					pdfProfile.getMargin().getBottom(),
					pdfProfile.getMargin().getLeft());
			document.getPdfDocument().addEventHandler(PdfDocumentEvent.START_PAGE, new FooterEventHandler(document, pdfProfile, "https://reactome.org"));
			for (Section section : SECTIONS)
				section.render(document, pdfProfile, analysisData, event);
		}
	}

	private PdfProfile loadProfile(String profile) throws DocumentExporterException {
		try {
			final InputStream resource = getClass().getResourceAsStream("/profiles/" + profile.toLowerCase() + ".json");
			return MAPPER.readValue(resource, PdfProfile.class);
		} catch (IOException e) {
			throw new DocumentExporterException("Couldn't load profile " + profile, e);
		}
	}

	private String getDefaultResource(AnalysisStoredResult result) {
		final List<ResourceSummary> summary = result.getResourceSummary();
		// Select the second one since first one always "TOTAL" .
		return summary.size() == 2
				? summary.get(1).getResource()
				: summary.get(0).getResource();
	}


}

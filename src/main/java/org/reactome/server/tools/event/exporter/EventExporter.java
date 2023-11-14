package org.reactome.server.tools.event.exporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.model.ResourceSummary;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.service.*;
import org.reactome.server.tools.event.exporter.exception.DocumentExporterException;
import org.reactome.server.tools.event.exporter.profile.PdfProfile;
import org.reactome.server.tools.event.exporter.section.*;
import org.reactome.server.tools.event.exporter.util.Diagrams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Document generator. One instance of this class can create more than one document.
 */
@Component
public class EventExporter {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private DatabaseObjectService databaseObjectService;
	private GeneralService generalService;
	private ParticipantService participantService;


	public EventExporter(String diagramPath, String ehldPath, String analysisPath, String fireworksPath, String svgSummary){
		Diagrams.setPaths(diagramPath, ehldPath, analysisPath, fireworksPath, svgSummary);
		Locale.setDefault(Locale.ENGLISH);
	}

	/**
	 * Creates and configures a new DocumentExporter. Use {@link EventExporter#export(DocumentArgs, AnalysisStoredResult, OutputStream)} to generate documents.
	 *
	 * @param diagramPath                   path where diagrams (.json and .graph.json) are stored
	 * @param ehldPath                      path where EHLD are stored
	 * @param analysisPath                  path where analysis file are stored
	 * @param fireworksPath                 path where fireworks (.json) are stored
	 * @param svgSummary                    path to svgsummary.txt file
	 * @param diagramService                service needed for diagrams
	 * @param databaseObjectService         service needed for events
	 * @param generalService                service needed for general info (db version)
	 * @param advancedDatabaseObjectService service needed for reactions layout
	 * @param participantService            service needed for reaction analysis info
	 */
	public EventExporter(String diagramPath, String ehldPath, String analysisPath, String fireworksPath, String svgSummary,
						 DiagramService diagramService,
						 DatabaseObjectService databaseObjectService,
						 GeneralService generalService,
						 AdvancedDatabaseObjectService advancedDatabaseObjectService,
						 ParticipantService participantService) {
		this(diagramPath, ehldPath, analysisPath, fireworksPath, svgSummary);
		setDatabaseObjectService(databaseObjectService);
		setGeneralService(generalService);
		setParticipantService(participantService);
		setDiagramService(diagramService);
		setAdvancedDatabaseObjectService(advancedDatabaseObjectService);
	}

	public int export(DocumentArgs args, AnalysisStoredResult result, OutputStream destination) throws DocumentExporterException {
		final Event event = databaseObjectService.findById(args.getStId());
		if (event == null) throw new DocumentExporterException(args.getStId() + " is not an event");

		final PdfProfile pdfProfile = loadProfile("breathe");

		final AnalysisData analysisData;
		if (result != null) {
			// if the analysis result not contains the given resource, use the first resource in this analysis.
			if (!result.getResourceSummary().contains(new ResourceSummary(args.getResource(), null)))
				args.setResource(getDefaultResource(result));

			analysisData = new AnalysisData(result, args.isImportableOnly(), args.getResource(), args.getSpecies(), Integer.MAX_VALUE);
		} else analysisData = null;
		final DocumentContent content = new DocumentContent(analysisData, pdfProfile, event, args);
		try (Document document = new Document(new PdfDocument(new PdfWriter(destination)))) {
			document.getPdfDocument().getDocumentInfo().setAuthor(String.format("Reactome (%s)", args.getServerName()));
			document.getPdfDocument().getDocumentInfo().setCreator(String.format("Reactome (%s)", args.getServerName()));
			document.getPdfDocument().getDocumentInfo().setTitle(String.format("Reactome | %s (%s)", event.getDisplayName(), args.getStId()));
			document.getPdfDocument().getDocumentInfo().setSubject(String.format("Reactome | %s (%s)", event.getDisplayName(), args.getStId()));
			document.getPdfDocument().getDocumentInfo().setKeywords("pathway,reactome,reaction");
			document.setFont(pdfProfile.getRegular());
			document.setMargins(pdfProfile.getMargin().getTop(),
					pdfProfile.getMargin().getRight(),
					pdfProfile.getMargin().getBottom(),
					pdfProfile.getMargin().getLeft());
			document.getPdfDocument().addEventHandler(PdfDocumentEvent.START_PAGE, new FooterEventHandler(document, pdfProfile, args.getServerName()));
			final Map<Long, Integer> pages = new HashMap<>();
			final List<Section> SECTIONS = Arrays.asList(
					new CoverPage(),
					new Introduction(generalService),
					new PropertiesSection(),
					new EventsDetails(participantService, pages),
					new TableOfContent(pages)
			);
			for (Section section : SECTIONS)
				section.render(document, content);
			return document.getPdfDocument().getNumberOfPages();
		}
	}

	// TODO: 19/12/18 too weak profile system
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

	@Autowired
	public void setDatabaseObjectService(DatabaseObjectService databaseObjectService) {
		this.databaseObjectService = databaseObjectService;
		Diagrams.setDatabaseObjectService(databaseObjectService);
		AnalysisData.setDatabaseObjectService(databaseObjectService);
	}

	@Autowired
	public void setGeneralService(GeneralService generalService) {
		this.generalService = generalService;
	}

	@Autowired
	public void setParticipantService(ParticipantService participantService) {
		this.participantService = participantService;
	}

	@Autowired
	public void setDiagramService(DiagramService diagramService) {
		Diagrams.setDiagramService(diagramService);
	}

	@Autowired
	public void setAdvancedDatabaseObjectService(AdvancedDatabaseObjectService advancedDatabaseObjectService) {
		Diagrams.setAdvancedDatabaseObjectService(advancedDatabaseObjectService);
	}
}

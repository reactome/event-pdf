package org.reactome.server.tools.event.exporter.util;

import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.HorizontalAlignment;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.graph.domain.model.ReactionLikeEvent;
import org.reactome.server.graph.domain.result.DiagramResult;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.DiagramService;
import org.reactome.server.tools.diagram.data.graph.Graph;
import org.reactome.server.tools.diagram.data.layout.Diagram;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.RasterExporter;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EhldException;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;
import org.reactome.server.tools.event.exporter.AnalysisData;
import org.reactome.server.tools.event.exporter.DocumentArgs;
import org.reactome.server.tools.fireworks.exporter.FireworksExporter;
import org.reactome.server.tools.fireworks.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.fireworks.exporter.common.api.FireworkArgs;
import org.reactome.server.tools.reaction.exporter.diagram.ReactionDiagramFactory;
import org.reactome.server.tools.reaction.exporter.graph.ReactionGraphFactory;
import org.reactome.server.tools.reaction.exporter.layout.LayoutFactory;
import org.reactome.server.tools.reaction.exporter.layout.model.Layout;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Helper class to insert diagrams of events in document
 */
public class Diagrams {

	private static final Integer QUALITY = 3;
	private static String fireworksProfile;
	private static FireworksExporter fireworksExporter;
	private static RasterExporter diagramExporter;
	private static DiagramService diagramService;
	private static DatabaseObjectService databaseObjectService;
	private static AdvancedDatabaseObjectService advancedDatabaseObjectService;

	public static void insertFireworks(Document document, AnalysisData data) throws AnalysisServerError {
		final FireworkArgs args = new FireworkArgs(data.getSpecies().replace(" ", "_"), "png");
		args.setQuality(QUALITY);
		args.setWriteTitle(false);
		args.setProfile(fireworksProfile);
		try {
			addToDocument(document, fireworksExporter.renderPdf(args, data.getResult()), 0.5f);
		} catch (IOException e) {
			LoggerFactory.getLogger(Diagrams.class).error("Couldn't insert fireworks", e);
		}

	}

	public static void insertDiagram(String stId, AnalysisData analysisData, Document document, DocumentArgs documentArgs) {
		final DiagramResult diagramResult = diagramService.getDiagramResult(stId);
		final RasterArgs args = new RasterArgs(diagramResult.getDiagramStId(), "pdf")
			.setSelected(diagramResult.getEvents())
			.setWriteTitle(false)
			.setProfiles(new ColorProfiles(documentArgs.getDiagramProfile(), documentArgs.getAnalysisProfile(), null));
		if (analysisData != null) {
			args.setResource(analysisData.getResource())
			.setColumn(documentArgs.getExpressionColumn());
		}
		try {
			addToDocument(document, diagramExporter.exportToPdf(args, analysisData != null ? analysisData.getResult() : null), 0.5f);
		} catch (AnalysisException | EhldException | DiagramJsonNotFoundException | DiagramJsonDeserializationException | IOException e) {
			LoggerFactory.getLogger(Diagrams.class).error("Couldn't insert diagram " + stId, e);
		}
	}

	public static void insertReaction(String stId, AnalysisData analysisData, Document document, DocumentArgs documentArgs) {
		ReactionLikeEvent rle = databaseObjectService.findByIdNoRelations(stId);
		final String pStId = rle.getEventOf().isEmpty() ? stId : rle.getEventOf().get(0).getStId();

		final LayoutFactory layoutFactory = new LayoutFactory(advancedDatabaseObjectService, databaseObjectService);
		final Layout layout = layoutFactory.getReactionLikeEventLayout(rle, LayoutFactory.Style.BOX);
		final Diagram diagram = ReactionDiagramFactory.get(layout);

		final Graph graph = new ReactionGraphFactory(advancedDatabaseObjectService).getGraph(rle, layout);

		try {
			final RasterArgs args = new RasterArgs(pStId, "pdf")
					.setProfiles(new ColorProfiles(documentArgs.getDiagramProfile(), documentArgs.getAnalysisProfile(), null))
					.setMargin(2)
					.setWriteTitle(false);
			if (analysisData != null) {
				args.setResource(analysisData.getResource())
						.setColumn(args.getColumn());
			}
			final AnalysisStoredResult result = analysisData == null ? null : analysisData.getResult();
			addToDocument(document, diagramExporter.exportToPdf(diagram, graph, args, result), 0.4f);
		} catch (AnalysisException | IOException e) {
			LoggerFactory.getLogger(Diagrams.class).error("Couldn't insert diagram for " + stId, e);
		}

	}

	private static void addToDocument(Document document, Document imagePdf, float proportion) throws IOException {
		final PdfFormXObject object = imagePdf.getPdfDocument().getFirstPage().copyAsFormXObject(document.getPdfDocument());
		final float wi = document.getPdfDocument().getLastPage().getPageSize().getWidth() - document.getLeftMargin() - document.getRightMargin() - 0.1f;  // avoid image too large
		final float he = proportion * document.getPdfDocument().getLastPage().getPageSize().getHeight() - document.getTopMargin() - document.getBottomMargin();
		document.add(new Image(object).scaleToFit(wi, he).setHorizontalAlignment(HorizontalAlignment.CENTER));
		document.flush();
	}

	public static void setPaths(String diagramPath, String ehldPath, String analysisPath, String fireworksPath, String svgSummary) {
		fireworksExporter = new FireworksExporter(fireworksPath, analysisPath);
		diagramExporter = new RasterExporter(diagramPath, ehldPath, analysisPath, svgSummary);
	}

	public static void setDiagramService(DiagramService diagramService) {
		Diagrams.diagramService = diagramService;
	}

	public static void setDatabaseObjectService(DatabaseObjectService databaseObjectService) {
		Diagrams.databaseObjectService = databaseObjectService;
	}

	public static void setAdvancedDatabaseObjectService(AdvancedDatabaseObjectService advancedDatabaseObjectService) {
		Diagrams.advancedDatabaseObjectService = advancedDatabaseObjectService;
	}

}

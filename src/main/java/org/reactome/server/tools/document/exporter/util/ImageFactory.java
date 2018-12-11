package org.reactome.server.tools.document.exporter.util;

import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.property.HorizontalAlignment;
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
import org.reactome.server.tools.document.exporter.AnalysisData;
import org.reactome.server.tools.fireworks.exporter.FireworksExporter;
import org.reactome.server.tools.fireworks.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.fireworks.exporter.common.api.FireworkArgs;
import org.reactome.server.tools.reaction.exporter.diagram.ReactionDiagramFactory;
import org.reactome.server.tools.reaction.exporter.graph.ReactionGraphFactory;
import org.reactome.server.tools.reaction.exporter.layout.LayoutFactory;
import org.reactome.server.tools.reaction.exporter.layout.model.Layout;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ImageFactory {

	private static final double QUALITY = 3.;
	private static String diagramProfile;
	private static String analysisProfile;
	private static String fireworksProfile;
	private static FireworksExporter fireworksExporter;
	private static RasterExporter diagramExporter;
	private static DiagramService diagramService;
	private static DatabaseObjectService databaseObjectService;
	private static AdvancedDatabaseObjectService advancedDatabaseObjectService;

	public static void insertFireworks(Document document, AnalysisData data) throws AnalysisServerError {
		final FireworkArgs args = new FireworkArgs(data.getSpecies().replace(" ", "_"), "png");
		args.setFactor(QUALITY);
		args.setWriteTitle(false);
		args.setProfile(fireworksProfile);
		try {
			addToDocument(document, fireworksExporter.renderPdf(args, data.getResult()));
		} catch (IOException e) {
			LoggerFactory.getLogger(ImageFactory.class).error("Couldn't insert fireworks", e);
		}

	}

	public static void insertDiagram(String stId, AnalysisData analysisData, Document document) {
		final DiagramResult diagramResult = diagramService.getDiagramResult(stId);
		final RasterArgs args = new RasterArgs(diagramResult.getDiagramStId(), "pdf");
		args.setSelected(diagramResult.getEvents());
		args.setWriteTitle(false);
		if (analysisData != null) {
			args.setResource(analysisData.getResource());
		}
		args.setProfiles(new ColorProfiles(diagramProfile, analysisProfile, null));
		try {
			addToDocument(document, diagramExporter.exportToPdf(args, analysisData != null ? analysisData.getResult() : null));
		} catch (AnalysisException | EhldException | DiagramJsonNotFoundException | DiagramJsonDeserializationException | IOException e) {
			LoggerFactory.getLogger(ImageFactory.class).error("Couldn't insert diagram " + stId, e);
		}
	}

	public static void insertReaction(String stId, AnalysisData analysisData, Document document) {
		ReactionLikeEvent rle = databaseObjectService.findById(stId);
		final String pStId = rle.getEventOf().isEmpty() ? stId : rle.getEventOf().get(0).getStId();

		final LayoutFactory layoutFactory = new LayoutFactory(advancedDatabaseObjectService);
		final Layout layout = layoutFactory.getReactionLikeEventLayout(rle, LayoutFactory.Style.BOX);
		final Diagram diagram = ReactionDiagramFactory.get(layout);

		final Graph graph = new ReactionGraphFactory(advancedDatabaseObjectService).getGraph(rle, layout);

		try {
			final RasterArgs args = new RasterArgs(pStId, "pdf");
			if (analysisData != null) args.setResource(analysisData.getResource());
			addToDocument(document, diagramExporter.exportToPdf(diagram, graph, args, analysisData == null ? null : analysisData.getResult()));
		} catch (AnalysisException | IOException e) {
			LoggerFactory.getLogger(ImageFactory.class).error("Couldn't insert diagram for " + stId, e);
		}

	}

	private static void addToDocument(Document document, Document imagePdf) throws IOException {
		final PdfFormXObject object = imagePdf.getPdfDocument().getFirstPage().copyAsFormXObject(document.getPdfDocument());
		final float wi = document.getPdfDocument().getLastPage().getPageSize().getWidth() - document.getLeftMargin() - document.getRightMargin() - 0.1f;  // avoid image too large
		final float he = 0.5f * document.getPdfDocument().getLastPage().getPageSize().getHeight() - document.getTopMargin() - document.getBottomMargin();
		document.add(new Image(object).scaleToFit(wi, he).setHorizontalAlignment(HorizontalAlignment.CENTER));
		document.flush();
	}

	public static void setPaths(String diagramPath, String ehldPath, String analysisPath, String fireworksPath, String svgSummary) {
		fireworksExporter = new FireworksExporter(fireworksPath, analysisPath);
		diagramExporter = new RasterExporter(diagramPath, ehldPath, analysisPath, svgSummary);
	}

	public static void setProfiles(String diagramProfile, String analysisProfile, String fireworksProfile) {
		ImageFactory.diagramProfile = diagramProfile;
		ImageFactory.analysisProfile = analysisProfile;
		ImageFactory.fireworksProfile = fireworksProfile;
	}

	public static void setDiagramService(DiagramService diagramService) {
		ImageFactory.diagramService = diagramService;
	}

	public static void setDatabaseObjectService(DatabaseObjectService databaseObjectService) {
		ImageFactory.databaseObjectService = databaseObjectService;
	}

	public static void setAdvancedDatabaseObjectService(AdvancedDatabaseObjectService advancedDatabaseObjectService) {
		ImageFactory.advancedDatabaseObjectService = advancedDatabaseObjectService;
	}

	public static AdvancedDatabaseObjectService getAdvancedDatabaseObjectService() {
		return advancedDatabaseObjectService;
	}
}

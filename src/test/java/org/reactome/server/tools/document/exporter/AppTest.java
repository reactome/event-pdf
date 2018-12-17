package org.reactome.server.tools.document.exporter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.DiagramService;
import org.reactome.server.graph.service.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppTest extends BaseTest {

	private static final File TEST_DOCS = new File("test-documents");
	private static final String ROOT = "src/test/resources/";
	private static final String DIAGRAM_PATH = ROOT + "diagram";
	private static final String EHLD_PATH = ROOT + "ehld";
	private static final String ANALYSIS_PATH = ROOT + "analysis";
	private static final String FIREWORKS_PATH = ROOT + "fireworks";
	private static final String SVGSUMMARY = ROOT + "ehld/svgsummary.txt";
	private static final String TOKEN_EXP = "MjAxODEwMzAxMDIzMDBfNQ%253D%253D"; // HPA (GeneName)
	private static final String TOKEN_OVER = "MjAxODExMDEwNzI3NDNfOA%253D%253D"; // HPA (GeneName)
	@Autowired
	private DatabaseObjectService databaseObjectService;
	@Autowired
	private DiagramService diagramService;
	@Autowired
	private AdvancedDatabaseObjectService advancedDatabaseObjectService;
	@Autowired
	private ParticipantService participantService;

	@BeforeClass
	public static void setUpClass() {
		logger.info("Running " + AppTest.class.getName());
		if (!TEST_DOCS.exists() && !TEST_DOCS.mkdirs())
			logger.error("Couldn't create test folder " + TEST_DOCS);
	}

	@AfterClass
	public static void afterClass() {
//		try {
//			FileUtils.cleanDirectory(TEST_DOCS);
//		} catch (IOException e) {
//			logger.error("Could't delete test folder " + TEST_DOCS);
//		}
	}

	@Test
	public void test() {
		final DocumentExporter documentExporter = new DocumentExporter(DIAGRAM_PATH, EHLD_PATH, ANALYSIS_PATH, FIREWORKS_PATH, SVGSUMMARY, diagramService, databaseObjectService, generalService, advancedDatabaseObjectService, participantService);
//		final String stId = "R-HSA-901006";   // Reaction
//		final String stId = "R-HSA-354192";   // Integrin alphaIIb beta3 signaling (pathway)
//		final String stId = "R-HSA-8963743";  // Digestion and absorption (small, 50 pages)
		final String stId = "R-HSA-112316";  // Neuronal system (medium, 300 pages)
//		final String stId = "R-HSA-1430728";  // Metabolism (large, 3000 pages)
		try {
			final long start = System.nanoTime();
			final File file = new File(TEST_DOCS, stId + ".pdf");
			final AnalysisStoredResult result = new TokenUtils(ANALYSIS_PATH).getFromToken(TOKEN_OVER);
			documentExporter.export(new DocumentArgs(stId).setMaxLevel(1), null, new FileOutputStream(file));
			final long end = System.nanoTime();
			System.out.println(formatTime(end - start));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static String formatTime(long nanoSeconds) {
		final long hours = nanoSeconds / 3_600_000_000_000L;
		nanoSeconds = nanoSeconds - hours * 3_600_000_000_000L;
		final long minutes = nanoSeconds / 60_000_000_000L;
		nanoSeconds = nanoSeconds - minutes * 60_000_000_000L;
		final long seconds = nanoSeconds / 1_000_000_000L;
		nanoSeconds = nanoSeconds - seconds * 1_000_000_000L;
		final long millis = nanoSeconds / 1_000_000L;
		return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
	}

}

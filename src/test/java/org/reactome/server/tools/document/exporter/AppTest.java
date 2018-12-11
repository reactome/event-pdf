package org.reactome.server.tools.document.exporter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.DiagramService;
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
	@Autowired
	private DatabaseObjectService databaseObjectService;
	@Autowired
	private DiagramService diagramService;
	@Autowired
	private AdvancedDatabaseObjectService advancedDatabaseObjectService;

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
		final DocumentExporter documentExporter = new DocumentExporter(DIAGRAM_PATH, EHLD_PATH, ANALYSIS_PATH, FIREWORKS_PATH, SVGSUMMARY, diagramService, databaseObjectService, generalService, advancedDatabaseObjectService);
//		final String stId = "R-HSA-354192";
		final String stId = "R-HSA-8963743";  // Circadian clock
		try {
			final File file = new File(TEST_DOCS, stId + ".pdf");
			documentExporter.export(new DocumentArgs(stId).setMaxLevel(15), null, new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
}

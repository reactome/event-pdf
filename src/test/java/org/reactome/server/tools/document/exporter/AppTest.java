package org.reactome.server.tools.document.exporter;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.DiagramService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppTest extends BaseTest {

	@Autowired
	private DatabaseObjectService databaseObjectService;
	@Autowired
	private DiagramService diagramService;

	private static final File TEST_DOCS = new File("test-documents");
	private static final String ROOT = "src/test/resources/";
	private static final String DIAGRAM_PATH = ROOT + "diagram";
	private static final String EHLD_PATH = ROOT + "ehld";
	private static final String ANALYSIS_PATH = ROOT + "analysis";
	private static final String FIREWORKS_PATH = ROOT + "fireworks";
	private static final String SVGSUMMARY = ROOT + "ehld/svgsummary.txt";

	@BeforeClass
	public static void setUpClass() {
		logger.info("Running " + AppTest.class.getName());
		if (!TEST_DOCS.exists() && !TEST_DOCS.mkdirs())
			logger.error("Couldn't create test folder " + TEST_DOCS);
	}
	@AfterClass
	public static void afterClass() {
		try {
			FileUtils.cleanDirectory(TEST_DOCS);
		} catch (IOException e) {
			logger.error("Could't delete test folder " + TEST_DOCS);
		}
	}
	@Test
	public void test() {
		final DocumentExporter documentExporter = new DocumentExporter(DIAGRAM_PATH, EHLD_PATH, ANALYSIS_PATH, FIREWORKS_PATH, SVGSUMMARY, diagramService, databaseObjectService, generalService);
		final String stId = "R-HSA-354192";
		try {
			final File file = new File(TEST_DOCS, stId + ".pdf");
			documentExporter.export(stId, null, new FileOutputStream(file), null, null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
}

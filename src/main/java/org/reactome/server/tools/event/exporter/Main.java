package org.reactome.server.tools.event.exporter;

import com.martiansoftware.jsap.*;
import org.reactome.server.graph.domain.model.TopLevelPathway;
import org.reactome.server.graph.exception.CustomQueryException;
import org.reactome.server.graph.service.*;
import org.reactome.server.graph.service.util.DatabaseObjectUtils;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.event.exporter.config.ReactomeNeo4jConfig;
import org.reactome.server.tools.event.exporter.util.ProgressBar;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

public class Main {

	private static boolean verbose;

	public static void main(String[] args) throws JSAPException {
		SimpleJSAP jsap = new SimpleJSAP(Main.class.getName(), "Exports the requested pathway(s) to pdf",
				new Parameter[]{
						new QualifiedSwitch("target", JSAP.STRING_PARSER, null, JSAP.REQUIRED, 't', "target", "Target event to convert. Use either comma separated IDs, pathways for a given species (e.g. 'Homo sapiens') or 'all' for every pathway").setList(true).setListSeparator(','),
						new FlaggedOption("output", JSAP.STRING_PARSER, null, JSAP.REQUIRED, 'o', "output", "The output folder"),

						// diagram options
						new FlaggedOption("ehlds", JSAP.STRING_PARSER, null, JSAP.NOT_REQUIRED, 'e', "ehld", "The folder containing the EHLD svg files"),
						new FlaggedOption("diagrams", JSAP.STRING_PARSER, null, JSAP.NOT_REQUIRED, 'd', "diagram", "The folder containing the diagram json files"),
						new FlaggedOption("analysis", JSAP.STRING_PARSER, null, JSAP.NOT_REQUIRED, 'a', "analysis", "The folder containing the analysis files"),
						new FlaggedOption("summary", JSAP.STRING_PARSER, null, JSAP.NOT_REQUIRED, 's', "summary", "The file containing the summary of pathways with EHLD assigned"),

						// GRAPH-DB options
						new FlaggedOption("host", JSAP.STRING_PARSER, "localhost", JSAP.NOT_REQUIRED, 'h', "host", "The neo4j host"),
						new FlaggedOption("port", JSAP.STRING_PARSER, "7474", JSAP.NOT_REQUIRED, 'p', "port", "The neo4j port"),
						new FlaggedOption("user", JSAP.STRING_PARSER, "neo4j", JSAP.NOT_REQUIRED, 'u', "user", "The neo4j user"),
						new FlaggedOption("password", JSAP.STRING_PARSER, "neo4j", JSAP.REQUIRED, 'w', "password", "The neo4j password"),

						new FlaggedOption("profile", JSAP.STRING_PARSER, "Modern", JSAP.NOT_REQUIRED, 'c', "profile", "The colour diagram [Modern or Standard]"),

						new QualifiedSwitch("verbose", JSAP.BOOLEAN_PARSER, null, JSAP.NOT_REQUIRED, 'v', "verbose", "Requests verbose output.")
				}
		);

		JSAPResult config = jsap.parse(args);
		if (jsap.messagePrinted()) System.exit(1);

        verbose = config.getBoolean("verbose");

		//Initialising ReactomeCore Neo4j configuration
		ReactomeGraphCore.initialise(
				config.getString("host"),
				config.getString("port"),
				config.getString("user"),
				config.getString("password"),
				ReactomeNeo4jConfig.class
		);

		final String diagrams = config.getString("diagrams");
		final String ehlds = config.getString("ehlds");
		final String analysis = config.getString("analysis");
		final String summary = config.getString("summary");
		final File output = new File(config.getString("output"));
		if (!output.exists() && !output.mkdirs()) {
			System.err.println("Couldn't create output folder " + output);
			System.exit(1);
		}

		final Collection<TopLevelPathway> targets = getTargets(config.getStringArray("target"));

		final DiagramService ds = ReactomeGraphCore.getService(DiagramService.class);
		final AdvancedDatabaseObjectService ados = ReactomeGraphCore.getService(AdvancedDatabaseObjectService.class);
		final DatabaseObjectService dos = ReactomeGraphCore.getService(DatabaseObjectService.class);
		final GeneralService gs = ReactomeGraphCore.getService(GeneralService.class);
		final ParticipantService ps = ReactomeGraphCore.getService(ParticipantService.class);
		final EventExporter exporter = new EventExporter(diagrams, ehlds, analysis, null, summary, ds, dos, gs, ados, ps);

		if (verbose) {
			System.out.println(String.format("%d events", targets.size()));
		}
		int i = 0;
		final ProgressBar bar = new ProgressBar();
		for (TopLevelPathway target : targets) {
			if (verbose) {
				final double progress = (double) i / targets.size();
				final String message = String.format("%3d / %-3d\t%-15s\t%s", i + 1, targets.size(), target.getStId(), target.getDisplayName());
				bar.setProgress(progress, message);
			}
			final DocumentArgs documentArgs = new DocumentArgs(target.getStId()).setMaxLevel(null);
			final File dest = new File(output, target.getDisplayName() + ".pdf");
			try {
				exporter.export(documentArgs, null, new FileOutputStream(dest));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			i++;
		}
		bar.setProgress(1.0, String.format("%d / %d", targets.size(), targets.size()));
		bar.clear();
	}

	private static Collection<TopLevelPathway> getTargets(String[] target) {
		AdvancedDatabaseObjectService advancedDatabaseObjectService = ReactomeGraphCore.getService(AdvancedDatabaseObjectService.class);
		String query;
		Map<String, Object> parametersMap = new HashMap<>();
		if (target.length > 1) {
			final List<Long> dbIds = new ArrayList<>();
			final List<String> stIds = new ArrayList<>();
			for (String identifier : target) {
				final String id = DatabaseObjectUtils.getIdentifier(identifier);
				if (DatabaseObjectUtils.isStId(id)) {
					stIds.add(id);
				} else if (DatabaseObjectUtils.isDbId(id)) {
					dbIds.add(Long.parseLong(id));
				}
			}
			query = "MATCH (p:TopLevelPathway) " +
					"WHERE p.dbId IN {dbIds} OR p.stId IN {stIds} " +
					"WITH DISTINCT p " +
					"RETURN p " +
					"ORDER BY p.dbId";
			parametersMap.put("dbIds", dbIds);
			parametersMap.put("stIds", stIds);
		} else {
			String aux = target[0];
			if (aux.toLowerCase().equals("all")) {
				query = "MATCH (p:TopLevelPathway)-[:species]->(s:Species) " +
						"WITH DISTINCT p, s " +
						"RETURN p " +
						"ORDER BY s.dbId, p.dbId";
			} else if (DatabaseObjectUtils.isStId(aux)) {
				query = "MATCH (p:TopLevelPathway{stId:{stId}}) RETURN DISTINCT p";
				parametersMap.put("stId", DatabaseObjectUtils.getIdentifier(aux));
			} else if (DatabaseObjectUtils.isDbId(aux)) {
				query = "MATCH (p:TopLevelPathway{dbId:{dbId}}) RETURN DISTINCT p";
				parametersMap.put("dbId", DatabaseObjectUtils.getIdentifier(aux));
			} else {
				if (verbose) System.out.println(String.format("Detected species '%s'", aux));
				query = "MATCH (p:TopLevelPathway{speciesName:{speciesName}}) " +
						"WITH DISTINCT p " +
						"RETURN p " +
						"ORDER BY p.dbId";
				parametersMap.put("speciesName", aux);
			}
		}

		Collection<TopLevelPathway> pathways = new ArrayList<>();
		try {
			pathways = advancedDatabaseObjectService.getCustomQueryResults(TopLevelPathway.class, query, parametersMap);
		} catch (CustomQueryException e) {
			LoggerFactory.getLogger("document-exporter").error("Problem retrieving the target pathways", e);
		}
		return pathways;

	}

}

package org.reactome.server.tools.document.exporter;

import org.reactome.server.analysis.core.result.PathwayNodeSummary;
import org.reactome.server.analysis.core.result.model.PathwayBase;
import org.reactome.server.graph.domain.model.Pathway;

/**
 * Includes for a stId: <dl> <dt>summary : {@link PathwayNodeSummary}</dt>
 * <dd>The pathway raw data, as is, in the analysis. Data is not filtered by
 * resource neither species.</dd> <dt>base : {@link PathwayBase}</dt>
 * <dd>Statistics of the pathway filtered by resource and species.</dd>
 * <dt>pathway : {@link Pathway}</dt> <dd>Graph database data of the pathway,
 * including summations, references...</dd> </dl>
 */
public class PathwayData {

	final private PathwayNodeSummary summary;
	final private PathwayBase base;
	private Pathway pathway;

	PathwayData(PathwayNodeSummary summary, PathwayBase base, Pathway pathway) {
		this.summary = summary;
		this.base = base;
		this.pathway = pathway;
	}

	/**
	 * Statistics of the pathway filtered by resource and species.
	 */
	public PathwayBase getBase() {
		return base;
	}

	/**
	 * The pathway raw data, as is, in the analysis. Data is not filtered by
	 * resource neither species.
	 */
	public PathwayNodeSummary getSummary() {
		return summary;
	}

	/**
	 * Graph database data of the pathway, including summations, references...
	 */
	public Pathway getPathway() {
		return pathway;
	}
}

package org.reactome.server.tools.document.exporter.section;

import com.itextpdf.layout.Document;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.tools.document.exporter.AnalysisData;
import org.reactome.server.tools.document.exporter.exception.DocumentExporterException;
import org.reactome.server.tools.document.exporter.style.PdfProfile;

/**
 * Whole PDF report will be split into different sections.
 *
 * @author Chuan-Deng dengchuanbio@gmail.com
 */
public interface Section {
	/**
	 * This method is to create the pdf document according to the analysis
	 * result data set.
	 */
	void render(Document document, PdfProfile profile, AnalysisData analysisData, Event event) throws DocumentExporterException;
}

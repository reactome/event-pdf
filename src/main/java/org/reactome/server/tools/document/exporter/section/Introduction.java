package org.reactome.server.tools.document.exporter.section;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import org.reactome.server.tools.document.exporter.DocumentProperties;
import org.reactome.server.tools.document.exporter.style.PdfProfile;
import org.reactome.server.tools.document.exporter.util.HtmlParser;
import org.reactome.server.tools.document.exporter.util.PdfUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Section Introduction contains the analysis introduction and Reactome relative
 * publications
 *
 * @author Chuan-Deng dengchuanbio@gmail.com
 */
public class Introduction implements Section {

	private static final String INTRODUCTION = PdfUtils.getProperty("introduction");
	private static final List<Reference> PUBLICATIONS = PdfUtils.getText(Introduction.class.getResourceAsStream("/texts/references.txt"))
			.stream()
			.map(s -> s.split("\t"))
			.map(line -> new Reference(line[0], line[1]))
			.collect(Collectors.toList());

	@Override
	public void render(Document document, DocumentProperties properties) {
		final PdfProfile profile = properties.getPdfProfile();
		document.add(new AreaBreak());
		document.add(profile.getH2("Introduction").setDestination("introduction"));
		final Collection<Paragraph> intro = HtmlParser.parseText(profile, INTRODUCTION);
		intro.forEach(document::add);

		for (Reference publication : PUBLICATIONS) {
			document.add(profile.getCitation(publication.text, publication.link));
		}
	}

	private static class Reference {
		String text;
		String link;

		Reference(String text, String link) {
			this.text = text;
			this.link = link;
		}
	}
}

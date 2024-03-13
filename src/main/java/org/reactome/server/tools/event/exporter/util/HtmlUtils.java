package org.reactome.server.tools.event.exporter.util;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.layout.element.BlockElement;
import com.itextpdf.layout.element.Paragraph;
import org.reactome.server.tools.event.exporter.profile.PdfProfile;

import java.util.List;
import java.util.stream.Collectors;

public class HtmlUtils {

	public static final String COLOR = String.format("rgb(%d, %d, %d)", PdfProfile.R, PdfProfile.G, PdfProfile.B);

	public static Paragraph getParagraph(String html, PdfProfile profile) {
		Paragraph paragraph = profile.getParagraph();
		getElements(html, profile).forEach(paragraph::add);
		return paragraph;
	}

	public static List<BlockElement<?>> getElements(String html, PdfProfile profile) {
		//language=html
		String text = String.format("<style>a {color: %s;} * {font-size: %s} </style><div>%s</div>", COLOR, profile.getFontSize() + "pt", html);
		return HtmlConverter.convertToElements(text).stream().map(iElement -> (BlockElement<?>) iElement).collect(Collectors.toList());
	}
}

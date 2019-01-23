package org.reactome.server.tools.event.exporter.profile;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.hyphenation.HyphenationConfig;
import com.itextpdf.layout.property.ListNumberingType;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.event.exporter.exception.DocumentExporterException;

import java.io.IOException;

/**
 * Profile model contains the report outlook setting. This settings include
 * margins, font family, font sizes and colors.
 *
 * @author Chuan-Deng dengchuanbio@gmail.com
 */
public class PdfProfile {

	private static final HyphenationConfig HYPHENATION_CONFIG = new HyphenationConfig("en", "uk", 2, 2);
	private static final Color REACTOME_COLOR = new DeviceRgb(47, 158, 194);
	private static final Color LINK_COLOR = REACTOME_COLOR;
	private static final Color LIGHT_GRAY = new DeviceGray(0.9f);
	private static final String FONT_PATH_FORMAT = "/fonts/%s/%s-%s.ttf";
	private static final String FONT_NAME = "SourceSerifPro";
	private float H1;
	private float H2;
	private float H3;
	private float fontSize;
	private float TITLE;
	private float TABLE;
	private PdfFont italic;
	private PdfFont regular;
	private PdfFont bold;
	private PdfFont light;
	private MarginProfile margin;
	private PdfFont boldItalic;

	public PdfProfile() {
		// Every PDF must load the fonts again, as they are hold by one, and only one, document
		try {
			byte[] bytes;
			bytes = getFontBytes("Regular");
			regular = PdfFontFactory.createFont(bytes, PdfEncodings.IDENTITY_H, true);
			bytes = getFontBytes("Bold");
			bold = PdfFontFactory.createFont(bytes, PdfEncodings.IDENTITY_H, true);
			bytes = getFontBytes("Semibold");
			light = PdfFontFactory.createFont(bytes, PdfEncodings.IDENTITY_H, true);
			bytes = getFontBytes("It");
			italic = PdfFontFactory.createFont(bytes, PdfEncodings.IDENTITY_H, true);
			bytes = getFontBytes("BoldIt");
			boldItalic = PdfFontFactory.createFont(bytes, PdfEncodings.IDENTITY_H, true);

		} catch (IOException e) {
			throw new DocumentExporterException("Internal error. Couldn't read fonts", e);
		}
	}

	private byte[] getFontBytes(String regular) throws IOException {
		final String fontPath = String.format(FONT_PATH_FORMAT, FONT_NAME, FONT_NAME, regular);
		return IOUtils.toByteArray(getClass().getResourceAsStream(fontPath));
	}

	public MarginProfile getMargin() {
		return margin;
	}

	public PdfFont getBold() {
		return bold;
	}

	public Color getLinkColor() {
		return LINK_COLOR;
	}

	public float getFontSize() {
		return fontSize;
	}

	public void setFontSize(Integer fontSize) {
		this.fontSize = fontSize;
		TABLE = this.fontSize - 2;
		H3 = this.fontSize + 2;
		H2 = this.fontSize + 4;
		H1 = this.fontSize + 6;
		TITLE = this.fontSize + 14;
	}

	public Paragraph getParagraph(String text) {
		return getParagraph().add(text);
	}

	public Paragraph getParagraph() {
		return new Paragraph()
				.setFont(regular)
				.setFontSize(fontSize)
				.setHyphenation(HYPHENATION_CONFIG)
				.setMultipliedLeading(1.1f)
				.setTextAlignment(TextAlignment.JUSTIFIED);
	}

	public Paragraph getCitation() {
		return getParagraph()
				.setKeepTogether(true)
				.setFontSize(fontSize - 1)
				.setFirstLineIndent(-15)
				.setPaddingLeft(15)
				.setMultipliedLeading(1);
	}

	public Paragraph getH1(String text) {
		return getParagraph(text)
				.setFont(bold)
				.setFontSize(H1)
				.setMultipliedLeading(1.5f)
				.setTextAlignment(TextAlignment.LEFT);
	}

	public Paragraph getH2(String text) {
		return getParagraph(text)
				.setFont(bold)
				.setFontSize(H2)
				.setMultipliedLeading(1.4f)
				.setTextAlignment(TextAlignment.LEFT);
	}

	public Paragraph getH3(String text) {
		return getParagraph(text)
				.setFont(bold)
				.setFontSize(H3)
//				.setMultipliedLeading(1.2f)
				.setTextAlignment(TextAlignment.LEFT);
	}


	public Paragraph getTitle(String text) {
		return getParagraph(text)
				.setFontSize(TITLE)
				.setFont(light)
				.setTextAlignment(TextAlignment.CENTER)
				.setMultipliedLeading(1.7f);

	}

	public Cell getBodyCell(String text, int row) {
		final Cell cell = new Cell()
				.setKeepTogether(true)
				.setVerticalAlignment(VerticalAlignment.MIDDLE)
				.setBorder(Border.NO_BORDER)
				.setBackgroundColor(row % 2 == 0 ? null : LIGHT_GRAY);
		if (text != null)
			cell.add(new Paragraph(text)
					.setFont(regular)
					.setFontSize(TABLE)
					.setTextAlignment(TextAlignment.CENTER)
					.setMultipliedLeading(1.0f));
		return cell;
	}

	public List getList(java.util.List<Paragraph> paragraphList) {
		final List list = getList(false);
		for (Paragraph paragraph : paragraphList) {
			final ListItem item = new ListItem();
			item.add(paragraph.setMultipliedLeading(1.0f));
			list.add(item);
		}
		return list;
	}

	public List getList(boolean ordered) {
		if (ordered) {
			return new List(ListNumberingType.DECIMAL)
					.setMarginLeft(10)
					.setSymbolIndent(10)
					.setFont(regular)
					.setFontSize(fontSize);
		} else return new List()
				.setMarginLeft(10)
				.setSymbolIndent(10)
				.setListSymbol("\u2022")
				.setFont(regular)
				.setFontSize(fontSize);
	}

	public PdfFont getRegular() {
		return regular;
	}

	public PdfFont getItalic() {
		return italic;
	}

	public Cell getHeaderCell(String text) {
		return getHeaderCell(text, 1, 1);
	}

	public Cell getHeaderCell(String text, int rowspan, int colspan) {
		final Cell cell = new Cell(rowspan, colspan)
				.setKeepTogether(true)
				.setVerticalAlignment(VerticalAlignment.MIDDLE)
				.setBorder(new SolidBorder(DeviceGray.WHITE, 1))
				.setFontColor(DeviceGray.WHITE)
				.setFont(bold)
				.setFontSize(TABLE + 1)
				.setBackgroundColor(REACTOME_COLOR);
		if (text != null)
			cell.add(new Paragraph(text)
					.setTextAlignment(TextAlignment.CENTER)
					.setMultipliedLeading(1.0f));
		return cell;
	}

	public Text getLink(String link) {
		return getLink(link, link);
	}

	public Text getLink(String text, String link) {
		return new Text(text).setFontColor(LINK_COLOR).setAction(PdfAction.createURI(link));
	}

	public Text getGoTo(String text, String destination) {
		return new Text(text).setFontColor(LINK_COLOR).setAction(PdfAction.createGoTo(destination));
	}

	public PdfFont getBoldItalic() {
		return boldItalic;
	}
}

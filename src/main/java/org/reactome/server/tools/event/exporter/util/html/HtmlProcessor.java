package org.reactome.server.tools.event.exporter.util.html;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.List;
import com.itextpdf.layout.element.ListItem;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.Property;
import org.reactome.server.tools.event.exporter.profile.PdfProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlProcessor {

	private static final Collection<String> VALID_TAG = Arrays.asList("b", "strong", "i", "em", "sup", "sub", "a", "font");
	private static final Pattern TAG = Pattern.compile("(?i)<(\\w+)(?: (.*?))?>(.*?)</(\\1)>");

	private static final Pattern UL = Pattern.compile("(?i)<(ul|ol)(?: (.*?))?>(.*?)</(\\1)>");
	private static final Pattern BOLD = Pattern.compile("(?i)<(b|strong)>(.*?)</(\\1)>");
	private static final Pattern ITALIC = Pattern.compile("(?i)<(i|em)>(.*?)</(\\1)>");
	private static final Pattern SUB = Pattern.compile("(?i)<(sub)>(.*?)</(\\1)>");
	private static final Pattern SUP = Pattern.compile("(?i)<(sup)>(.*?)</(\\1)>");
	private static final Pattern FONT = Pattern.compile("(?i)<(font)(?: (.*?))?>(.*?)</(\\1)>");
	private static final Pattern A = Pattern.compile("(?i)<a(?:.*href=\"(.*?)\".*)>(.*?)</a>");
	private static final Logger LOGGER = LoggerFactory.getLogger("html-processor");
	private static final Map<String, Color> COLOR_NAMES = new HashMap<String, Color>(){{
		put("red", new DeviceRgb(255, 0,0));
	}};
	private static final Pattern ATTRIBUTE = Pattern.compile("(\\w+)=\"(.+?)\"");
//	private static final Pattern UNQUOTED_ATTRIBUTE = Pattern.compile("(\\w+)=\"?(.+?)\"?");

	public static void add(Document document, String text, PdfProfile profile) {
		if (text == null || text.isEmpty()) return;
		addBlock(document, text, profile);
		final String[] paragraphs = text.split("(?i)<br\\s*>|<p( .*?)?>|</p>|\n|\\\\n");
		for (String paragraph : paragraphs) {
			addParagraph(document, paragraph, profile);
		}
	}

	private static void addBlock(Document document, String text, PdfProfile profile) {

	}

	private static void addParagraph(Document document, String text, PdfProfile profile) {
		// First find other block elements
		Matcher matcher = UL.matcher(text);
		while (matcher.find()) {
			final String prev = text.substring(0, matcher.start());
			if (!prev.isEmpty())
				document.add(profile.getParagraph(prev));
			addList(document, matcher, profile);
			text = text.substring(matcher.end());
			matcher = UL.matcher(text);
		}
		if (!text.isEmpty())
			document.add(createParagraph(text, profile));
		// TODO: 22/01/19 tables

	}

	private static void addList(Document document, Matcher matcher, PdfProfile profile) {
		final String type = matcher.group(1);
		boolean ordered = type.toLowerCase().equals("ol");
		final String attributes = matcher.group(2);
		final String content = matcher.group(3);
		final String[] items = content.split("<li>|</li>");  // very raw, but you can have no closing </li>
		final List list = profile.getList(ordered);
		for (String item : items) {
			item = item.trim();
			if (!item.isEmpty()) {
				final Paragraph paragraph = createParagraph(item, profile);
				final ListItem listItem = new ListItem();
				listItem.add(paragraph);
				list.add(listItem);
			}
		}
		document.add(list);
	}

	/**
	 * Raw paragraph parser. No block elements expected.
	 *
	 * @param text
	 * @param profile
	 * @return
	 */
	private static Paragraph createParagraph(String text, PdfProfile profile) {
		final Paragraph paragraph = profile.getParagraph();
		final java.util.List<Text> texts = getTexts(text, profile);
		for (Text t : texts) paragraph.add(t);
		return paragraph;
	}

	private static java.util.List<Text> getTexts(String text, PdfProfile profile) {
		final java.util.List<Text> texts = new ArrayList<>();
		StringBuilder freeText = new StringBuilder();
		for (int i = 0; i < text.length(); ) {
			if (text.charAt(i) == '<') {
				final String tag = getTag(text, i);
				if (tag == null) {
					freeText.append(text.charAt(i++));
					continue;
				}
				final int endOfTag = endOfTag(text, i, tag);
				if (endOfTag < 0) {
					freeText.append(text.charAt(i++));
					continue;
				}
				if (freeText.length() > 0) {
					texts.addAll(getText(freeText.toString(), profile));
				}
				freeText = new StringBuilder();
				final String subText = text.substring(i, endOfTag);
				i = endOfTag;
				switch (tag.toLowerCase()) {
					case "a":
						texts.addAll(getLink(subText, profile));
						break;
					case "i":
					case "em":
						texts.addAll(getItalic(subText, profile));
						break;
					case "b":
					case "strong":
						texts.addAll(getBold(subText, profile));
						break;
					case "sub":
						texts.addAll(getSub(subText, profile));
						break;
					case "sup":
						texts.addAll(getSup(subText, profile));
						break;
					case "font":
						texts.addAll(getFont(subText, profile));
						break;
					default:
						LOGGER.warn("Unexpected tag " + tag + " in " + text);
						texts.addAll(getText(text, profile));
				}
			} else {
				freeText.append(text.charAt(i++));
			}
		}
		if (freeText.length() > 0) {
			texts.addAll(getText(freeText.toString(), profile));
		}
		return texts;
	}

	private static Collection<Text> getText(String text, PdfProfile profile) {
		return Collections.singleton(new Text(text));
	}

	private static Collection<Text> getSub(String text, PdfProfile profile) {
		final Matcher matcher = SUB.matcher(text);
		if (!matcher.matches()) {
			LOGGER.warn("Unrecognized sub tag " + text);
			return Collections.singletonList(new Text(text));
		}
		final String tag = matcher.group(1);
		final String content = matcher.group(2);
		final java.util.List<Text> texts = getTexts(content, profile);
		for (Text t : texts)
			t.setFontSize(1 + profile.getFontSize() / 2)
					.setTextRise(-2);
		return texts;
	}

	private static Collection<? extends Text> getSup(String text, PdfProfile profile) {
		final Matcher matcher = SUP.matcher(text);
		if (!matcher.matches()) {
			LOGGER.warn("Unrecognized sup tag in " + text);
			return Collections.singletonList(new Text(text));
		}
		final String tag = matcher.group(1);
		final String content = matcher.group(2);
		final java.util.List<Text> texts = getTexts(content, profile);
		for (Text t : texts)
			t.setFontSize(1 + profile.getFontSize() / 2).setTextRise(6);
		return texts;
	}

	private static Collection<Text> getBold(String text, PdfProfile profile) {
		final Matcher matcher = BOLD.matcher(text);
		if (!matcher.matches()) {
			LOGGER.warn("Unrecognized bold tag " + text);
			return Collections.singletonList(new Text(text));
		}
		final String tag = matcher.group(1);
		final String content = matcher.group(2);
		final java.util.List<Text> texts = getTexts(content, profile);
		for (Text t : texts) {
			//  Combine bold+italic
			if (t.getProperty(Property.FONT) == profile.getItalic()) {
				t.setFont(profile.getBoldItalic());
			} else {
				t.setFont(profile.getBold());
			}
		}
		return texts;
	}

	private static Collection<? extends Text> getItalic(String text, PdfProfile profile) {
		final Matcher matcher = ITALIC.matcher(text);
		if (!matcher.matches()) {
			LOGGER.warn("Unrecognized italic tag " + text);
			return Collections.singletonList(new Text(text));
		}
		final String tag = matcher.group(1);
		final String content = matcher.group(2);
		final java.util.List<Text> texts = getTexts(content, profile);
		for (Text t : texts) {
			//  Combine bold+italic
			if (t.getProperty(Property.FONT) == profile.getBold()) {
				t.setFont(profile.getBoldItalic());
			} else {
				t.setFont(profile.getItalic());
			}
		}
		return texts;
	}

	private static Collection<? extends Text> getFont(String text, PdfProfile profile) {
		final Matcher matcher = FONT.matcher(text);
		if (!matcher.matches()) {
			LOGGER.warn("Unrecognized font tag " + text);
			return Collections.singletonList(new Text(text));
		}
		final String tag = matcher.group(1);
		final String attributes = matcher.group(2);
		final Map<String, String> att = parseAttributes(attributes);
		final String content = matcher.group(3);
		final java.util.List<Text> texts = getTexts(content, profile);
		for (Text t : texts) {
				if (att.containsKey("color")) {
					final String color = att.get("color");
					t.setFontColor(getColor(color));
				}
		}
		return texts;
//		return null;
	}

	private static Map<String, String> parseAttributes(String attributes) {
		final Map<String, String> map = new HashMap<>();
		Matcher matcher = ATTRIBUTE.matcher(attributes);
		while (matcher.find()) {
			map.put(matcher.group(1), matcher.group(2));
		}
		return map;
	}

	private static Collection<? extends Text> getLink(String text, PdfProfile profile) {
		final Matcher matcher = A.matcher(text);
		if (!matcher.matches()) {
			System.err.println("Not a link " + text);
			return Collections.singleton(new Text(text));
		}
		final String url = matcher.group(1);
		final String t = matcher.group(2);
		return Collections.singletonList(profile.getLink(t, url));
	}

	private static String getTag(String text, int i) {
		final StringBuilder tag = new StringBuilder();
		int p = i + 1;
		while (text.charAt(p) != ' ' && text.charAt(p) != '>')
			tag.append(text.charAt(p++));
		final String t = tag.toString().toLowerCase();
		if (VALID_TAG.contains(t)) return t;
		LOGGER.info("Unknown tag " + t);
		return null;
//		return VALID_TAG.contains(t) ? t : null;
	}

	private static Color getColor(String color) {
		if (color.startsWith("#")) {
			return getHexColor(color);
		} else if (color.startsWith("rgb")){
			return getRgbColor(color);
		} else return COLOR_NAMES.get(color);
		// color_name 	Specifies the text color with a color name (like "red")
	}

	private static Color getRgbColor(String color) {
		//rgb_number 	Specifies the text color with an rgb code (like "rgb(255,0,0)")
		return null;
	}

	private static Color getHexColor(String color) {
		//hex_number 	Specifies the text color with a hex code (like "#ff0000")
		return null;
	}

	private static int endOfTag(String text, int i, String tag) {
		final Matcher matcher = Pattern.compile("(?i)</" + tag + ">").matcher(text);
		if (!matcher.find(i)) {
			LOGGER.error("Missing closing tag " + tag + " in " + text.substring(i, Math.min(i + 5, text.length())));
			return -1;
		}
		return matcher.start() + 3 + tag.length();
	}
}

package org.reactome.server.tools.document.exporter.util;

import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.document.exporter.exception.DocumentExporterException;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class PdfUtils {

	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.ENGLISH);
	private static final NumberFormat EXP_FORMAT = NumberFormat.getNumberInstance(Locale.ENGLISH);

	private static final Properties properties = new Properties();

	static {
		try {
			final InputStream resource = PdfUtils.class.getResourceAsStream("/texts/properties.properties");
			final InputStreamReader reader = new InputStreamReader(resource, Charset.forName("utf8"));
			properties.load(reader);
		} catch (IOException e) {
			LoggerFactory.getLogger(PdfUtils.class).error("Couldn't load resource properties.properties");
		}
	}

	static {
		NUMBER_FORMAT.setMaximumFractionDigits(3);
		NUMBER_FORMAT.setGroupingUsed(true);
		EXP_FORMAT.setMaximumFractionDigits(2);
	}

	public static List<String> getText(InputStream resource) {
		try {
			return IOUtils.readLines(resource, Charset.defaultCharset());
		} catch (IOException e) {
			throw new DocumentExporterException("Couldn't read internal resource", e);
		}
	}

	public static String formatNumber(Number number) {
		if (number instanceof Integer || number instanceof Long)
			return number.toString();
		if (Double.compare(number.doubleValue(), 0.0) == 0) return "0";
		if (number.doubleValue() < 1e-3)
			return String.format("%.2e", number.doubleValue());
		return NUMBER_FORMAT.format(number);
	}

	public static String getProperty(String key) {
		return properties.getProperty(key);
	}

	public static String getProperty(String key, Object... args) {
		return String.format(getProperty(key), args);
	}

}

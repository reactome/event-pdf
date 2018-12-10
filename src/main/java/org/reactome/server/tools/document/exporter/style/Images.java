package org.reactome.server.tools.document.exporter.style;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.layout.element.Image;

public class Images {

	private static ImageData LOGO_DATA = ImageDataFactory.create(Images.class.getResource("/img/logo.png"));
	private static ImageData LINK_DATA = ImageDataFactory.create(Images.class.getResource("/img/link.png"));

	/**
	 * Gets a new fresh link icon with dimensions fitting scale and pointing to
	 * the url passed by argument.
	 *
	 * @param url  where the link will point.
	 * @param size size of the link
	 */
	public static Image getLink(String url, float size) {
		return new Image(LINK_DATA).scaleToFit(size, size).setAction(PdfAction.createURI(url));
	}

	/**
	 * Gets a new image of the reactome logo
	 */
	public static Image getLogo() {
		return new Image(LOGO_DATA);
	}

}

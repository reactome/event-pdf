package org.reactome.server.tools.document.exporter.util;

import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.tools.document.exporter.profile.PdfProfile;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Helper class to create reference paragraphs
 */
public class References {

	private static final Pattern FACING_CHARACTERS = Pattern.compile("^[\\s,.]+");
	private static final Pattern TRAILING_CHARACTERS = Pattern.compile("[\\s,.]+$");

	public static Paragraph getPublication(PdfProfile profile, Publication publication) {
		if (publication instanceof LiteratureReference)
			return getLiteratureReference(profile, (LiteratureReference) publication);
		else if (publication instanceof URL) {
			return getURL(profile, (URL) publication);
		} else if (publication instanceof Book)
			return getBook(profile, (Book) publication);
		LoggerFactory.getLogger(References.class).warn("Publication subtype not known " + publication.getClassName());
		return profile.getCitation().add(publication.getDisplayName());
	}

	// Author, A. A., & Author, B. B. (Date of publication). Title of article. Title of Journal, volume number, page range. doi:0000000/000000000000 or https://doi.org/10.0000/0000
	// Brownlie, D. (2007). Toward effective poster presentations: An annotated bibliography. European Journal of Marketing, 41, 1245-1283. doi:10.1108/03090560710821161
	// Wooldridge, M.B., & Shapka, J. (2012). Playing with technology: Mother-toddler interaction scores lower during play with electronic toys. Journal of Applied Developmental Psychology, 33(5), 211-218. https://doi.org/10.1016/j.appdev.2012.05.005
	private static Paragraph getLiteratureReference(PdfProfile profile, LiteratureReference reference) {
		final Paragraph citation = profile.getCitation();
		citation.add(String.format("%s (%d). %s", getAuthorList(reference.getAuthor()), reference.getYear(), trim(reference.getTitle())));
		if (reference.getJournal() != null) citation.add(". " + reference.getJournal().trim());
		if (reference.getVolume() != null) citation.add(", " + reference.getVolume());
		if (reference.getPages() != null) citation.add(", " + reference.getPages().trim());
		citation.add(".");
		if (reference.getUrl() != null) citation.add(profile.getLink("link", reference.getUrl()));
		return citation;
	}

	private static String trim(String title) {
		title = FACING_CHARACTERS.matcher(title).replaceAll("");
		title = TRAILING_CHARACTERS.matcher(title).replaceAll("");
		return title;
	}

	private static Paragraph getURL(PdfProfile profile, URL url) {
		return profile.getCitation()
				.add(String.format("%s. Retrieved from ", url.getTitle()))
				.add(profile.getLink(url.getUniformResourceLocator()));
	}

	private static Paragraph getBook(PdfProfile profile, Book book) {
		// year			    *
		// title 			*
		// chapterTitle	    opt
		// ISBN 			opt
		// pages			opt
		final Paragraph citation = profile.getCitation();
		citation.add(getAuthorList(book.getAuthor())
				+ " (" + book.getYear() + ")");
		citation.add(". ");
		if (book.getChapterTitle() != null)
			citation.add(book.getChapterTitle() + ", ");
		citation.add(new Text(book.getTitle().trim()).setItalic());
		if (book.getPages() != null)
			citation.add(new Text(", " + book.getPages().trim()));
		// APA style does not provide ISBN
//		if (book.getISBN() != null)
//			citation.append(". ISBN: ").append(book.getISBN());
		citation.add(".");
		return citation;
	}

	private static String getAuthorList(List<Person> author) {
		return author.stream()
				.limit(6)
				.map(person -> person.getDisplayName() + ".")
				.collect(Collectors.joining(", "))
				+ (author.size() > 6 ? " et. al." : "");
	}
}

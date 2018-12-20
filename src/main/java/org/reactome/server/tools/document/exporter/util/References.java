package org.reactome.server.tools.document.exporter.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.tools.document.exporter.profile.PdfProfile;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Helper class to create reference paragraphs
 */
public class References {

	// The Reactome citation style is almost equivalent to APA style:
	// https://owl.purdue.edu/owl/research_and_citation/using_research/documents/20180719CitationChart.pdf
	// The only difference with APA is in the list of authors when we have more than 6,
	// instead of
	//      6 authors "...last author".
	// we use
	//      6 authors "et al."


	private static final Pattern FACING_CHARACTERS = Pattern.compile("^[\\s,.]+");
	private static final Pattern TRAILING_CHARACTERS = Pattern.compile("[\\s,.]+$");
	private static final String REFERENCES_PATH = "/texts/references.json";
	private static final String ET_AL = " et al.";
	private static final String N_D = "n.d.";
	private static final String PUBMED = "pubmed";
	private static final String RETRIEVED_FROM = "Retrieved from";

	private static List<LiteratureReference> references;

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

	private static Paragraph getLiteratureReference(PdfProfile profile, LiteratureReference reference) {
		// [author] ([year]). [title]. <i>[journal], [volume]</i>, [pages].
		// <i> for italic
		final Paragraph citation = profile.getCitation();
		citation.add(String.format("%s (%d). %s", getAuthorList(reference.getAuthor()), reference.getYear(), trim(reference.getTitle())));
		if (reference.getJournal() != null) citation.add(". ").add(new Text(reference.getJournal().trim()).setItalic());
		if (reference.getVolume() != null) citation.add(new Text(", " + reference.getVolume()).setItalic());
		if (reference.getPages() != null) citation.add(", " + reference.getPages().trim());
		citation.add(".");
		if (reference.getUrl() != null) citation.add(" ").add(profile.getLink(PUBMED, reference.getUrl()));
		return citation;
	}

	private static Paragraph getURL(PdfProfile profile, URL url) {
		// [author] ([year]). [title]. Retrieved from [url].
		return profile.getCitation()
				.add(String.format("%s (%s). ",getAuthorList(url.getAuthor()) , N_D)) // year is always unknown
				.add(url.getTitle())
				.add(String.format(". %s ", RETRIEVED_FROM))
				.add(profile.getLink(url.getUniformResourceLocator()));
	}

	private static Paragraph getBook(PdfProfile profile, Book book) {
		// [author] ([year]). [chapter], [title]. <i>[publisher]</i>, [pages].
		final Paragraph citation = profile.getCitation();
		citation.add(String.format("%s (%d). ", getAuthorList(book.getAuthor()), book.getYear()));
		if (book.getChapterTitle() != null) citation.add(String.format("%s, ", book.getChapterTitle()));
		citation.add(trim(book.getTitle()));
		if (book.getPublisher() != null)
			citation.add(new Text(". " + book.getPublisher().getName().get(0)).setItalic());
		if (book.getPages() != null) {
			if (book.getPublisher() != null) citation.add(", ");
			else citation.add(". ");
			citation.add(book.getPages().trim());
		}
		citation.add(".");
		// APA style does not provide ISBN
		return citation;
	}

	/**
	 * Trims not only spaces, but dots (.) and commas (,) at the beginning and end of the string
	 * @param text a string to trim, null not allowed
	 * @return a new String that does not start neither end with a space, dot (.) or comma (,)
	 */
	private static String trim(String text) {
		text = FACING_CHARACTERS.matcher(text).replaceAll("");
		text = TRAILING_CHARACTERS.matcher(text).replaceAll("");
		return text;
	}

	public static String getAuthorList(List<Person> author) {
		return author.stream()
				.limit(6)
				.map(person -> person.getDisplayName() + ".")
				.collect(Collectors.joining(", "))
				+ (author.size() > 6 ? ET_AL : "");
	}

	/**
	 * @return the static references of Reactome
	 */
	public static Collection<LiteratureReference> getReactomeReferences() {
		if (references != null) return references;
		try {
			references = new ObjectMapper().readValue(References.class.getResourceAsStream(REFERENCES_PATH), Refs.class).getReferences();
		} catch (IOException e) {
			LoggerFactory.getLogger("document-exporter").error(String.format("Couldn't load references '%s'", REFERENCES_PATH), e);
			references = Collections.emptyList();
		}
		return references;
	}

}

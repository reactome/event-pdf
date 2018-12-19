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
	private static final String REFERENCES_JSON = "/texts/references.json";
	private static final String ET_AL = " et al.";
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
		if (reference.getUrl() != null) citation.add(" ").add(profile.getLink("pubmed", reference.getUrl()));
		return citation;
	}

	private static String trim(String title) {
		title = FACING_CHARACTERS.matcher(title).replaceAll("");
		title = TRAILING_CHARACTERS.matcher(title).replaceAll("");
		return title;
	}

	private static Paragraph getURL(PdfProfile profile, URL url) {
		// [author] ([year]). [title]. Retrieved from [url].
		return profile.getCitation()
				.add(getAuthorList(url.getAuthor()))
				.add(" (n.d.). ") // year is always unknown
				.add(url.getTitle())
				.add(". Retrieved from ")
				.add(profile.getLink(url.getUniformResourceLocator()));
	}

	private static Paragraph getBook(PdfProfile profile, Book book) {
		// [author] ([year]). [chapter], [title]. <i>[publisher]</i>, [pages].
		final Paragraph citation = profile.getCitation();
		citation.add(String.format("%s (%d). ", getAuthorList(book.getAuthor()), book.getYear()));
		if (book.getChapterTitle() != null) citation.add(String.format("%s, ", book.getChapterTitle()));
		citation.add(book.getTitle().trim());
		// TODO: 19/12/18 book.getPublisher return null always
//		citation.add(new Text(book.getPublisher().getName().get(0)).setItalic());
		if (book.getPages() != null) citation.add(String.format(", %s", book.getPages().trim()));
		// APA style does not provide ISBN
		citation.add(".");
		return citation;
	}

	public static String getAuthorList(List<Person> author) {
		return author.stream()
				.limit(6)
				.map(person -> person.getDisplayName() + ".")
				.collect(Collectors.joining(", "))
				+ (author.size() > 6 ? ET_AL : "");
	}

	public static Collection<LiteratureReference> getReactomeReferences() {
		if (references != null) return references;
		try {
			references = new ObjectMapper().readValue(References.class.getResourceAsStream(REFERENCES_JSON), Refs.class).getReferences();
		} catch (IOException e) {
			LoggerFactory.getLogger("document-exporter").error(String.format("Couldn't load references '%s'", REFERENCES_JSON), e);
			references = Collections.emptyList();
		}
		return references;
	}

}

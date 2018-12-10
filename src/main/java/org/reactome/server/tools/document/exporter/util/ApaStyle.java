package org.reactome.server.tools.document.exporter.util;

import com.itextpdf.layout.element.Text;
import org.reactome.server.graph.domain.model.*;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ApaStyle {
	private ApaStyle() {}

	public static List<Text> toApa(Publication publication) {
		if (publication instanceof LiteratureReference)
			return toApa((LiteratureReference) publication);
		else if (publication instanceof URL) {
			return toApa((URL) publication);
		} else if (publication instanceof Book)
			return toApa((Book) publication);
		LoggerFactory.getLogger(ApaStyle.class).warn("Publication subtype not known " + publication.getClassName());
		return Collections.singletonList(new Text(publication.getDisplayName()));
	}

	private static List<Text> toApa(LiteratureReference reference) {
		final List<Text> citation = new LinkedList<>();
		citation.add(new Text(toApa(reference.getAuthor())
				+ " (" + reference.getYear() + "). " + trim(reference.getTitle())));
		if (reference.getJournal() != null)
			citation.add(new Text(". " + reference.getJournal().trim()));
		if (reference.getVolume() != null)
			citation.add(new Text(", " + reference.getVolume()));
		if (reference.getPages() != null)
			citation.add(new Text(", " + reference.getPages().trim()));
		citation.add(new Text("."));
		return citation;
	}

	private static String trim(String title) {
		final Pattern FACING_SPACES = Pattern.compile("^[\\s,.]+");
		final Pattern TRAILING_SPACES = Pattern.compile("[\\s,.]+$");
		title = FACING_SPACES.matcher(title).replaceAll("");
		title = TRAILING_SPACES.matcher(title).replaceAll("");
		return title;
	}

	private static List<Text> toApa(URL url) {
		return Collections.singletonList(new Text(url.getTitle() +
				". Retrieved from " +
				url.getUniformResourceLocator()));
	}

	private static List<Text> toApa(Book book) {
		// year			    *
		// title 			*
		// chapterTitle	    opt
		// ISBN 			opt
		// pages			opt
		final List<Text> citation = new LinkedList<>();
		citation.add(new Text(toApa(book.getAuthor())
				+ " (" + book.getYear() + ")"));
		if (book.getChapterTitle() != null) {
			citation.add(new Text(". "));
			citation.add(new Text(book.getChapterTitle()
					+ ", " + book.getTitle()).setItalic());
		} else citation.add(new Text(". " + book.getTitle()).setItalic());
		if (book.getPages() != null)
			citation.add(new Text(", " + book.getPages()));
		// APA style does not provide ISBN
//		if (book.getISBN() != null)
//			citation.append(". ISBN: ").append(book.getISBN());
		citation.add(new Text("."));
		return citation;
	}

	private static String toApa(List<Person> author) {
		final StringBuilder citation = new StringBuilder();
		if (author.size() == 1)
			citation.append(apa(author.get(0)));
		else if (author.size() < 8) {
			for (int i = 0; i < author.size() - 2; i++)
				citation.append(apa(author.get(i))).append(", ");
			citation.append(apa(author.get(author.size() - 2)))
					.append(" & ")
					.append(apa(author.get(author.size() - 1)));
		} else {
			for (int i = 0; i < 6; i++)
				citation.append(apa(author.get(i))).append(", ");
			citation.append("... ").append(apa(author.get(author.size() - 1)));
		}
		return citation.toString();
	}


	private static String apa(Person person) {
		if (person.getSurname() != null && person.getInitial() != null)
			return person.getSurname() + " " + person.getInitial();
		if (person.getSurname() != null && person.getFirstname() != null)
			return person.getSurname() + " " + initials(person.getFirstname());
		if (person.getSurname() != null) return person.getSurname();
		return person.getFirstname();
	}


	private static String initials(String name) {
		return Arrays.stream(name.split(" "))
				.map(n -> n.substring(0, 1).toUpperCase())
				.collect(Collectors.joining(" "));
	}
}

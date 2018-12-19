package org.reactome.server.tools.document.exporter.util;

import org.reactome.server.graph.domain.model.LiteratureReference;

import java.util.List;

class Refs {
	public Refs(){}

	private List<LiteratureReference> references;

	public void setReferences(List<LiteratureReference> references) {
		this.references = references;
	}

	public List<LiteratureReference> getReferences() {
		return references;
	}
}

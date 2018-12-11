package org.reactome.server.tools.document.exporter;

public class DocumentArgs {

	private String stId;
	private int maxLevel = 1;
	private Long species = 48887L;   // Homo sapiens
	private String resource;

	public DocumentArgs(String stId) {
		this.stId = stId;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public DocumentArgs setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
		return this;
	}

	public String getStId() {
		return stId;
	}

	public Long getSpecies() {
		return species;
	}

	public DocumentArgs setSpecies(Long species) {
		this.species = species;
		return this;
	}

	public String getResource() {
		return resource;
	}

	public DocumentArgs setResource(String resource) {
		this.resource = resource;
		return this;
	}
}

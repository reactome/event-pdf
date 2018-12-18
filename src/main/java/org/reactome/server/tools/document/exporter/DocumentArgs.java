package org.reactome.server.tools.document.exporter;

public class DocumentArgs {

	private String stId;
	private int maxLevel = 0;
	private Long species = 48887L;   // Homo sapiens
	private String resource;

	public DocumentArgs(String stId) {
		this.stId = stId;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public DocumentArgs setMaxLevel(Integer maxLevel) {
		this.maxLevel = maxLevel == null ? Integer.MAX_VALUE : maxLevel;
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

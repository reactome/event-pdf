package org.reactome.server.tools.event.exporter;

public class DocumentArgs {

	private String stId;
	private int maxLevel = 0;
	private Long species = 48887L;   // Homo sapiens
	private String resource;
	private boolean importableOnly = false;
	private String diagramProfile;
	private String analysisProfile;
	private Integer expressionColumn;
	private String serverName = "https://reactome.org";

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

	public boolean isImportableOnly() {
		return importableOnly;
	}

	public DocumentArgs setImportableOnly(boolean importableOnly) {
		this.importableOnly = importableOnly;
		return this;
	}

	public String getAnalysisProfile() {
		return analysisProfile;
	}

	public DocumentArgs setAnalysisProfile(String analysisProfile) {
		this.analysisProfile = analysisProfile;
		return this;
	}

	public String getDiagramProfile() {
		return diagramProfile;
	}

	public DocumentArgs setDiagramProfile(String diagramProfile) {
		this.diagramProfile = diagramProfile;
		return this;
	}

	public Integer getExpressionColumn() {
		return expressionColumn;
	}

	public DocumentArgs setExpressionColumn(Integer expressionColumn) {
		this.expressionColumn = expressionColumn;
		return this;
	}

	public DocumentArgs setServerName(String serverName) {
		this.serverName = serverName;
		return this;
	}

	public String getServerName() {
		return serverName;
	}
}

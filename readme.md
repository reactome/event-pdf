[<img src=https://user-images.githubusercontent.com/6883670/31999264-976dfb86-b98a-11e7-9432-0316345a72ea.png height=75 />](https://reactome.org)

# Document exporter
Generates a PDF (Portable Digital Format) document out of a Reactome event (Pathway or Reaction).

The generated document contains the details for the given event and, optionally, its children. These details include:
 - A diagram image
 - Summation
 - Literature references
 - Edit history
 - Other details: type, location, compartments, diseases
 
## Options
Option | Default value | Description
---|---|---
stId | - | stable identifier of event
maxLevel | 1 | number of levels to explore inside sub events
analysis | null | providing an analysis will modify the information displayed, as well as the diagrams

## Installation
Add to maven
```xml
<dependency>
    <groupId>org.reactome.server.tools</groupId>
    <artifactId>event-pdf</artifactId>
    <version>1.0.4</version>
</dependency>

```
with reactome EBI repository
```xml
<repository>
    <id>nexus-ebi-repo</id>
    <name>The EBI internal repository</name>
    <url>http://www.ebi.ac.uk/Tools/maven/repos/content/groups/ebi-repo/</url>
</repository>
```

## Usage
```
EventExporter eventExporter = new EventExporter(DIAGRAM_PATH, EHLD_PATH, ANALYSIS_PATH, FIREWORKS_PATH, SVGSUMMARY, diagramService, databaseObjectService, generalService, advancedDatabaseObjectService);
String stId = "R-HSA-8963743";  // Digestion and absorption (small)
File file = new File(stId + ".pdf");
DocumentArgs args = new DocumentArgs(stId)
    .setMaxLevel(15);
try {
    eventExporter.export(args, null, new FileOutputStream(file));
} catch (FileNotFoundException e) {
    e.printStackTrace();
}
```

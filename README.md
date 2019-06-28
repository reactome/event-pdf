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

## Command line

```
Usage:
  org.reactome.server.tools.event.exporter.Main [--help]
  (-t|--target)[:target1,target2,...,targetN ] (-o|--output) <output> [(-e|--ehld)
  <ehlds>] [(-d|--diagram) <diagrams>] [(-a|--analysis) <analysis>]
  [(-s|--summary) <summary>] [(-h|--host) <host>] [(-p|--port) <port>]
  [(-u|--user) <user>] (-w|--password) <password> [(-c|--profile) <profile>]
  [(-v|--verbose)[:<verbose>]]

Exports the requested pathway(s) to pdf


  [--help]
        Prints this help message.

  (-t|--target)[:target1,target2,...,targetN ]
        Target event to convert. Use either comma separated IDs, pathways for a
        given species (e.g. 'Homo sapiens') or 'all' for every pathway (default: Homo sapiens)

  (-o|--output) <output>
        The output folder

  [(-e|--ehld) <ehlds>]
        The folder containing the EHLD svg files

  [(-d|--diagram) <diagrams>]
        The folder containing the diagram json files

  [(-a|--analysis) <analysis>]
        The folder containing the analysis files

  [(-s|--summary) <summary>]
        The file containing the summary of pathways with EHLD assigned

  [(-h|--host) <host>]
        The neo4j host (default: localhost)

  [(-p|--port) <port>]
        The neo4j port (default: 7474)

  [(-u|--user) <user>]
        The neo4j user (default: neo4j)

  (-w|--password) <password>
        The neo4j password (default: neo4j)

  [(-c|--profile) <profile>]
        The colour diagram [Modern or Standard] (default: Modern)

  [(-v|--verbose)[:<verbose>]]
        Requests verbose output.

```

## Creating The Reactome Book

```console
mvn clean package
java -jar target/event-pdf-jar-with-dependencies.jar -o /tmp/TheReactomeBook  -d diagrams/ -e ehlds/ -s svgsummary.txt -v
```

## Distribution

Zip as TheReactomeBook.pdf.tgz and place it in the download section at Website/static/download/current/

```console
tar -czvf TheReactomeBook.pdf.tgz /tmp/TheReactomeBook
mv TheReactomeBook.pdf.tgz <path>/static/download/current
```

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

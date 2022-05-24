
<h1 align="center">
  <a href="https://www.essi.upc.edu/dtim/nextiadi/"><img src="https://www.essi.upc.edu/~jflores/public/nextiadi_logo.png" alt="NextiaDI" width="300">
  </a>
</h1>

<h4 align="center">Incremental and Agnostic Data Integration</h4>

<p align="center">
  <a href="#about">About</a> •
  <a href="#key-features">Key Features</a> •
  <a href="#how-it-works">How it works</a> •
  <a href="#usage">Usage</a> •
  <a href="#installation">Installation</a> •
  <a href="#demo-zeppelin-notebook">Demo</a> •
  <a href="#reproducibility-of-experiments">Reproducibility</a>
</p>

## About
**NextiaDI**, from <a href="https://nahuatl.uoregon.edu/content/nextia" target="_blank">*nextia*</a> in the <a href="https://en.wikipedia.org/wiki/Nahuatl" target="_blank">Nahuatl</a> language (the old Aztec language), is an incremental and agnostic Data Integration (DI) that facilitates generating schema of heterogeneous data sources and integrating them. **NextiaDI** generates a graph-based schema for JSON and CSV. Note we are working to support more sources. We also provide with an incremental schema integration to annotate how integration is performed. We aim to automate as much as possible the integration of heterogeneous data sources.

Here, we provide you detailed information on how to run and evaluate NextiaDI. To learn more about the project, visit our [website](https://www.essi.upc.edu/dtim/nextiadi/).
  
## Key features   
* Extraction of schamata levaraging on the structure of schemaless data sources 
* Standardization of such extracted schemata into RDFS graph data model
* Annotation-based schema integration for RDF graphs describing unions and joins 
* Automated derivation of DI constructs for specific querying systems (i.e., source schemata, schema mappings, and target schema)


## How it works

We encourage you to read [our paper](http://www.semantic-web-journal.net/system/files/swj3138.pdf) to better understand what NextiaDI is and how can fit your scenarios. 

## Requirements
* Java 11
* org.glassfish.javax.json 1.1.4 

## Installation

The easy way to use NextiaJD is with Maven. 

For Gradle just add the following dependency in your build.sbt:

````
implementation 'edu.upc.essi.dtim:nextiadi:0.1.0'

````

For Apache Maven, just add the following dependency in your pom.xml:


````
<dependency>
  <groupId>edu.upc.essi.dtim</groupId>
  <artifactId>nextiadi</artifactId>
  <version>0.1.0</version>
</dependency>
````

For more ways to import it, please go <a href="https://search.maven.org/artifact/edu.upc.essi.dtim/nextiadi/0.1.0/jar">here</a>

## Usage    
Depending on the intent, we will import different class. We have to main features: extract and standardize schema (bootstrapping) and schema integration
To start using NextiaJD just import the implicits class as below:

### Bootstrapping
We provide two bootstrapping methods: JSON and CSV. Note that we are planning to add more in a future.

#### JSON
To bootstrap a JSON file, we need to import the class:

```
import edu.upc.essi.dtim.nextiadi.bootstraping.JSONBootstrap;

```

Then to start the bootstrapping, we create an instance of the class `JSONBootstrap` as follows:

```
JSONBootstrap b = new JSONBootstrap();
```
Using this instance, namely `b`, we call the method `bootstrapSchema(<Here datasource name>, <path to the data source>)`. This method will return a Jena model containing the schema represented as triples. An example of the used for this method is:

```
String path = "/home/datasources/sales.json"
Model schema_graph_based = b.bootstrapSchema("data source name", path);
```

#### CSV
To bootstrap a CSV file, we need to import the class:

```
import edu.upc.essi.dtim.nextiadi.bootstraping.CSVBootstrap;

```

Then to start the bootstrapping, we create an instance of the class `CSVBootstrap` as follows:

```
CSVBootstrap b = new CSVBootstrap();
```
Using this instance, namely `b`, we call the method `bootstrapSchema(<Here datasource name>, <path to the data source>)`. This method will return a Jena model containing the schema represented as triples. An example of the used for this method is:

```
String path = "/home/datasources/sales.json"
Model schema_graph_based = b.bootstrapSchema("data source name", path);
```


### Schema integration

To perform schema integration, we import the class:
```
import edu.upc.essi.dtim.NextiaDI;
```

Before integrating schemas, we need to read the RDF graph using Jena. We can do it as follows:

```
String pathA = "/somepath"
Model schemaA = RDFDataMgr.loadModel( pathA );
String pathB = "/somepath"
Model schemaB = RDFDataMgr.loadModel( pathB );
```
And have a list of aligments which will be used in the integration

```
# import edu.upc.essi.dtim.nextiadi.models.Alignment;
List<Alignment> alignments = new ArrayList<>();
Alignment a = new Alignment();
a.setIriA("some resource of A");
a.setIriB("some resource of B");
a.setL("some label for the integrated resource");
a.setType("class|datatype|object");
alignments.add(a);
```
Having the models `A` and `B`, and the list of alignments `alignments`. We proceed to integrate by creating an instance of NextiaDI and calling the method `Integrate` which return a model with the integrated annotations.

```
NextiaDI n = new NextiaDI();
Model integrated = n.Integrate(A, B, alignments);
```

If we wish to get a fully merge schema, we need to call the method `getMinimalGraph()` after the integration, as follows:

```
Model minimal = n.getMinimalGraph()
```

## Reproducibility of Experiments

We performed differents experiments to evaluate the predictive performance and efficiency of NextiaDI. In the spirit of open research and experimental reproducibility, we provide detailed information on how to reproduce them. More information about it can be found [here](https://github.com/dtim-upc/NextiaDI/tree/main/experiments).

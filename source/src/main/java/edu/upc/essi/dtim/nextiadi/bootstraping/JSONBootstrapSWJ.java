package edu.upc.essi.dtim.nextiadi.bootstraping;


import edu.upc.essi.dtim.nextiadi.bootstraping.metamodels.JSON_MM;
import edu.upc.essi.dtim.nextiadi.config.DataSourceVocabulary;
import edu.upc.essi.dtim.nextiadi.config.Formats;
import edu.upc.essi.dtim.nextiadi.jena.Graph;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import javax.json.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates an RDFS-compliant representation of a JSON's document schema
 * @author snadal
 */
@Getter
@Setter
public class JSONBootstrapSWJ extends DataSource{

	private int ObjectCounter = 0;
	private int ArrayCounter = 0;

//	private Graph Σ;
//	//SparkSQL query to get a 1NF view of the file
//	private String wrapper;
//	//List of pairs, where left is the IRI in the graph and right is the attribute in the wrapper (this will create sameAs edges)
//	private List<Pair<String,String>> sourceAttributes;
//
//	private List<Pair<String,String>> attributes;
//	private List<Pair<String,String>> lateralViews;

	public JSONBootstrapSWJ(){
		super();
		reset();
	}

	private void reset(){
		init();
		ObjectCounter = 0;
		ArrayCounter = 0;
	}


	public Model bootstrapSchema(String dataSourceName, String dataSourceID, String path) throws FileNotFoundException {
		reset();
		setPrefixesID(dataSourceID);
		id = dataSourceID;
		Document(path,dataSourceName);
		G_source.getModel().setNsPrefixes(prefixes);
//		G_source.write("/Users/javierflores/Documents/upc/projects/NextiaDI/source/source_schemas/source.ttl"  , Lang.TURTLE);

		addMetaData(dataSourceName, dataSourceID, path);

		productionRules_JSON_to_RDFS();

		return G_target.getModel().setNsPrefixes(prefixes);
	}

	/**
		String SELECT = attributes.stream().map(p -> {
			if (p.getLeft().equals(p.getRight())) return p.getLeft();
			else if (p.getLeft().contains("ContainerMembershipProperty")) return p.getRight();
			return p.getRight() + " AS " + p.getRight().replace(".","_");
		}).collect(Collectors.joining(","));
		String FROM = D;
		String LATERAL = lateralViews.stream().map(p -> "LATERAL VIEW explode("+p.getLeft()+") AS "+p.getRight()).collect(Collectors.joining("\n"));
		wrapper = "SELECT " + SELECT + " FROM " + D + " " + LATERAL;

		attributes.stream().forEach(p -> {
			if (p.getLeft().equals(p.getRight())) sourceAttributes.add(Pair.of(p.getRight(),p.getRight()));
			else if (p.getLeft().contains("ContainerMembershipProperty")) sourceAttributes.add(Pair.of(p.getLeft(),p.getRight()));
			else sourceAttributes.add(Pair.of(p.getLeft(),p.getRight().replace(".","_")));
		});

//		Stream.concat(sourceAttributes.stream(), lateralViews.stream()) .forEach(p -> {
//			Σ.addLiteral(createIRI(p.getLeft() ), DataSourceVocabulary.ALIAS.val(), p.getRight() );
//		});
		sourceAttributes.forEach(p -> {
			Σ.addLiteral(createIRI(p.getLeft() ), DataSourceVocabulary.ALIAS.val(), p.getRight() );
		});
**/

	@Deprecated
	private String generateArrayAlias(String a) {
		return Arrays.stream(a.split("\\.")).filter(p -> !p.contains("Seq")).collect(Collectors.joining("_"));
	}

	@Deprecated
	private String removeSeqs(String a) {
		return Arrays.stream(a.split("\\.")).filter(p -> !p.contains("Seq")).collect(Collectors.joining("."));
	}

	private void Document(String path, String D) {
		InputStream fis = null;
		try {
			fis = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		G_source.add(createIRI(D), RDF.type, JSON_MM.Document);
		Object(Json.createReader(fis).readValue().asJsonObject(),D);
	}

	private void DataType(JsonValue D, String p) {
		if (D.getValueType() == JsonValue.ValueType.OBJECT) Object((JsonObject)D, p);
		else if (D.getValueType() == JsonValue.ValueType.ARRAY) Array((JsonArray)D, p);
		else Primitive(D,p);
	}

	private void Object (JsonObject D, String p) {
		String u_prime = freshObject();
		G_source.add(createIRI(u_prime),RDF.type,JSON_MM.Object);
		D.forEach((k,v)-> {
			G_source.add(createIRI(k),RDF.type,JSON_MM.Key);
			G_source.add(createIRI(u_prime),JSON_MM.hasKey,createIRI(k));
			DataType(v,k);
		});
		G_source.add(createIRI(p),JSON_MM.hasValue, createIRI(u_prime));
	}

	private void Array (JsonArray D, String p) {
		String u_prime = freshArray();
		G_source.add(createIRI(u_prime),RDF.type,JSON_MM.Array);
		if (D.size() > 0) {
			DataType(D.get(0),p);
		} else {
			// TODO: some ds have empty array, check below example images array
			G_source.add(createIRI(p),JSON_MM.hasValue,JSON_MM.String);
		}
//		G_source.add(createIRI(p),JSON_MM.hasMember,createIRI(u_prime));
		G_source.add(createIRI(u_prime),JSON_MM.hasMember,createIRI(p));
	}

	private void Primitive (JsonValue D, String p) {
		if (D.getValueType() == JsonValue.ValueType.NUMBER) {
			G_source.add(createIRI(p),JSON_MM.hasValue,JSON_MM.Number);
		}
		// Boolean does not exist in the library
		//else if (D.getValueType() == JsonValue.ValueType.BOOLEAN) {
		//			G_source.add(createIRI(p),JSON_MM.hasValue,JSON_MM.Boolean);
		//		}
		else {
			G_source.add(createIRI(p),JSON_MM.hasValue,JSON_MM.String);
		}
	}

	private void instantiateMetamodel() {
		G_source.add(JSON_MM.Number.getURI(), RDF.type, JSON_MM.Primitive);
		G_source.add(JSON_MM.String.getURI(), RDF.type, JSON_MM.Primitive);
		//G_source.add(JSON_MM.Boolean.getURI(), RDF.type, JSON_MM.Primitive);
	}

	private String freshObject() {
		setObjectCounter(getObjectCounter()+1);
		return "Object_"+getObjectCounter();
	}

	private String freshArray() {
		setArrayCounter(getArrayCounter()+1);
		return "Array_"+getArrayCounter();
	}

	private void addMetaData(String name, String id, String path){
		String ds = DataSourceVocabulary.DataSource.val() +"/" + name;
		if (!id.equals("")){
			ds = DataSourceVocabulary.DataSource.val() +"/" + id;
			G_source.addLiteral( ds , DataSourceVocabulary.HAS_ID.val(), id);
		}
		addBasicMetaData(name, path, ds);
		G_source.addLiteral( ds , DataSourceVocabulary.HAS_FORMAT.val(), Formats.JSON.val());
		//TODO fix for the queries
		//G_source.addLiteral( ds , DataSourceVocabulary.HAS_WRAPPER.val(), wrapper);
	}


	private void productionRules_JSON_to_RDFS() {
		// Rule 1. Instances of J:Object are translated to instances of rdfs:Class .
		G_source.runAQuery("SELECT ?o WHERE { ?o <"+RDF.type+"> <"+JSON_MM.Object+"> }").forEachRemaining(res -> {
			G_target.add(res.getResource("o").getURI(),RDF.type,RDFS.Class);
		});

		// Rule 2. Instances of J:Array are translated to instances of rdfs:Class and rdf:Seq .
		G_source.runAQuery("SELECT ?a WHERE { ?a <"+RDF.type+"> <"+JSON_MM.Array+"> }").forEachRemaining(res -> {
			G_target.add(res.getResource("a").getURI(),RDF.type,RDFS.Class);
			G_target.add(res.getResource("a").getURI(),RDF.type,RDF.Seq);
		});

		// Rule 3. Instances of J:Key are translated to instances of rdf:Property . Additionally, this requires defining the rdfs:domain
		//of such newly defined instance of rdf:Property .
		G_source.runAQuery("SELECT ?o ?k WHERE { ?o <"+JSON_MM.hasKey+"> ?k }").forEachRemaining(res -> {
			G_target.add(res.getResource("k").getURI(),RDF.type,RDF.Property);
			G_target.add(res.getResource("k").getURI(),RDFS.domain,res.getResource("o").getURI());
		});

		//Rule 4. The rdfs:range of an instance of J:Primitive is its corresponding counterpart in the xsd vocabulary. Below we
		//show the case for instances of J:String whose counterpart is xsd:string . The procedure for instances of J:Number and
		//J:Boolean is similar using their pertaining type.
		G_source.runAQuery("SELECT ?k ?v WHERE { ?k <"+JSON_MM.hasValue+"> <"+JSON_MM.String+"> }").forEachRemaining(res -> {
			G_target.add(res.getResource("k").getURI(),RDF.type,RDF.Property);
			G_target.add(res.getResource("k").getURI(),RDFS.range,XSD.xstring);
		});
		G_source.runAQuery("SELECT ?k ?v WHERE { ?k <"+JSON_MM.hasValue+"> <"+JSON_MM.Number+"> }").forEachRemaining(res -> {
			G_target.add(res.getResource("k").getURI(),RDF.type,RDF.Property);
			G_target.add(res.getResource("k").getURI(),RDFS.range,XSD.xint);
		});

		//Rule 5. The rdfs:range of an instance of either J:Array or J:Object is the value itself.
		G_source.runAQuery("SELECT ?k ?v WHERE { ?k <"+JSON_MM.hasValue+"> ?v . ?v <"+RDF.type+"> <"+JSON_MM.Object+"> }").forEachRemaining(res -> {
			G_target.add(res.getResource("k").getURI(),RDF.type,RDF.Property);
			G_target.add(res.getResource("k").getURI(),RDFS.range,res.getResource("v"));
		});
		G_source.runAQuery("SELECT ?k ?v WHERE { ?k <"+JSON_MM.hasValue+"> ?v . ?v <"+RDF.type+"> <"+JSON_MM.Array+"> }").forEachRemaining(res -> {
			G_target.add(res.getResource("k").getURI(),RDF.type,RDF.Property);
			G_target.add(res.getResource("k").getURI(),RDFS.range,res.getResource("v"));
		});

		//Rule 6. Instances of J:Primitive which are members of an instance of J:Array are connected to its corresponding
		//counterpart in the xsd vocabulary using the rdfs:member property. We show the case for instances of J:String whose
		//counterpart is xsd:string . The procedure for instances of J:Number and J:Boolean is similar using their pertaining type.
		G_source.runAQuery("SELECT ?d ?a WHERE { ?a <"+JSON_MM.hasMember+"> ?d . ?d <"+RDF.type+"> <"+JSON_MM.String+"> }").forEachRemaining(res -> {
			G_target.add(XSD.xstring.getURI(),RDFS.member,res.getResource("a").getURI());
		});
		G_source.runAQuery("SELECT ?d ?a WHERE { ?a <"+JSON_MM.hasMember+"> ?d . ?d <"+RDF.type+"> <"+JSON_MM.Number+"> }").forEachRemaining(res -> {
			G_target.add(XSD.xint.getURI(),RDFS.member,res.getResource("a").getURI());
		});

		//Rule 7. Instances of J:Object or J:Array which are members of an instance of J:Array are connected via the rdfs:member
		//property.
		G_source.runAQuery("SELECT ?d ?a WHERE { ?a <"+JSON_MM.hasMember+"> ?d . ?d <"+RDF.type+"> <"+JSON_MM.Object+"> }").forEachRemaining(res -> {
			G_target.add(res.getResource("a").getURI(),RDFS.member,res.getResource("d"));
		});
		G_source.runAQuery("SELECT ?d ?a WHERE { ?a <"+JSON_MM.hasMember+"> ?d . ?d <"+RDF.type+"> <"+JSON_MM.Array+"> }").forEachRemaining(res -> {
			G_target.add(res.getResource("a").getURI(),RDFS.member,res.getResource("d"));
		});

	}


	public static void main(String[] args) throws IOException {
		JSONBootstrapSWJ j = new JSONBootstrapSWJ();
		String D = "cmoa_sample.json";

		Model M = j.bootstrapSchema("cmoa_data", D,"src/main/resources/cmoa_sample.json");

		Graph G = new Graph();
		G.setModel(M);
		java.nio.file.Path temp = Files.createTempFile("bootstrap",".ttl");
		System.out.println("Graph written to "+temp);
		G.write(temp.toString(), Lang.TURTLE);

		System.out.println("Attributes");
		System.out.println(j.getAttributes());

		System.out.println("Source attributes");
		System.out.println(j.getSourceAttributes());

		System.out.println("Lateral views");
		System.out.println(j.getLateralViews());


		List<Pair<String,String>> attributes = j.getAttributes();
		List<Pair<String,String>> lateralViews = j.getLateralViews();

		String SELECT = attributes.stream().map(p -> {
			if (p.getLeft().equals(p.getRight())) return p.getLeft();
			else if (p.getLeft().contains("ContainerMembershipProperty")) return p.getRight();
			return p.getRight() + " AS " + p.getRight().replace(".","_");
		}).collect(Collectors.joining(","));
		String FROM = D;
		String LATERAL = lateralViews.stream().map(p -> "LATERAL VIEW explode("+p.getLeft()+") AS "+p.getRight()).collect(Collectors.joining("\n"));

		String impl = "SELECT " + SELECT + " FROM " + D + " " + LATERAL;
		System.out.println(impl);
	}
}


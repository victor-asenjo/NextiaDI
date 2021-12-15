package edu.upc.essi.dtim.nextiadi.bootstraping;


import edu.upc.essi.dtim.nextiadi.config.DataSourceVocabulary;
import edu.upc.essi.dtim.nextiadi.config.Formats;
import edu.upc.essi.dtim.nextiadi.jena.Graph;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.ResourceImpl;
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
import java.util.stream.Stream;

/**
 * Generates an RDFS-compliant representation of a JSON's document schema
 * @author snadal
 */
@Getter
@Setter
public class JSONBootstrap extends DataSource{

	private int ObjectCounter = 1;
	private int SeqCounter = 1;
	private int CMPCounter = 1;

//	private Graph Σ;
//	//SparkSQL query to get a 1NF view of the file
//	private String wrapper;
//	//List of pairs, where left is the IRI in the graph and right is the attribute in the wrapper (this will create sameAs edges)
//	private List<Pair<String,String>> sourceAttributes;
//
//	private List<Pair<String,String>> attributes;
//	private List<Pair<String,String>> lateralViews;

	public JSONBootstrap(){
		super();
		reset();
	}

	private void reset(){
		init();
		ObjectCounter = 1;
		SeqCounter = 1;
		CMPCounter = 1;
	}


	public Model bootstrapSchema(String dataSourceName, String dataSourceID, String path) throws FileNotFoundException {
		reset();
		setPrefixesID(dataSourceID);
		id = dataSourceID;
		bootstrap(dataSourceName, path).setNsPrefixes(prefixes);
		addMetaData(dataSourceName, dataSourceID, path);
		return Σ.getModel();

	}

	public Model bootstrapSchema(String dataSourceName, String path) throws FileNotFoundException {
		reset();
		bootstrap(dataSourceName, path).setNsPrefixes(prefixes);
		addMetaData(dataSourceName, "", path);
		return Σ.getModel();
	}

	public Model bootstrapSchema(String iri, InputStream fis) throws FileNotFoundException {
		reset();
		JsonValue φ = Json.createReader(fis).readValue();

		Value(φ,iri,iri);
		return Σ.getModel();
	}

	private Model bootstrap(String D, String path) throws FileNotFoundException {
		InputStream fis = new FileInputStream(path);

		JsonValue φ = Json.createReader(fis).readValue();

		Value(φ,D,D);

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

		return Σ.getModel();
	}

	private String generateArrayAlias(String a) {
		return Arrays.stream(a.split("\\.")).filter(p -> !p.contains("Seq")).collect(Collectors.joining("_"));
	}

	private String removeSeqs(String a) {
		return Arrays.stream(a.split("\\.")).filter(p -> !p.contains("Seq")).collect(Collectors.joining("."));
	}

//	public static void main(String[] args) throws IOException {
//
//		JsonObject JSONFile = Json.createReader(JSONBootstrap.class.getResourceAsStream("/bikes.json")).readObject();
//		Dataset Σ = DatasetFactory.createTxnMem() ;
//		Σ.begin(ReadWrite.WRITE);
//		JSON("/bikes.json",JSONFile, Σ);
//		Σ.getNamedModel("G").write(new FileWriter("src/main/resources/bikes.ttl"), "TTL");
//	}

//	private static void JSON(String D, JsonObject φ,  Dataset Σ) {
//		Value(φ,Σ,D);
//	}
	public void write(String file, String lang){
		Σ.write(file,lang);
	}

	private void Value(JsonValue φ, String P, String implP) {
		if (φ.getValueType() == JsonValue.ValueType.STRING) LiteralString((JsonString)φ, P, implP);
		else if (φ.getValueType() == JsonValue.ValueType.NUMBER) LiteralNumber((JsonNumber)φ, P, implP);
		else if (φ.getValueType() == JsonValue.ValueType.OBJECT) Object((JsonObject)φ, P, implP);
		else if (φ.getValueType() == JsonValue.ValueType.ARRAY) Array((JsonArray)φ, P, "Object", implP);
	}

	private void Object (JsonObject φ, String P, String implP) {
		φ.keySet().forEach(k -> {
			JsonValue v = φ.get(k);

//			Σ.add(P+".has_"+k, RDF.type, RDF.Property);
//			Σ.add( P+".has_"+k, RDFS.domain, new ResourceImpl(P) );

//			addTriple(Σ,"G",new ResourceImpl(P+".has_"+k), RDF.type, RDF.Property);
//			addTriple(Σ,"G",new ResourceImpl(P+".has_"+k), RDFS.domain, new ResourceImpl(P));

			String label = "has_" +k;
			String property = P + "." + label;

			if (v.getValueType() == JsonValue.ValueType.STRING) {
				label = k;
				property = P + "." +k;
				LiteralString((JsonString)v, property, implP+"."+k);
			} else if (v.getValueType() == JsonValue.ValueType.NUMBER) {
				label = k;
				property = P + "." +k;
				LiteralNumber((JsonNumber) v, property, implP+"."+k);
			} else if (v.getValueType() == JsonValue.ValueType.OBJECT) {
				String u = k; ObjectCounter++;
				Σ.add(createIRI(property), RDFS.range, createIRI(P+"."+u));
				Object((JsonObject)v,P+"."+u, P+"."+u);
			}
			else if (v.getValueType() == JsonValue.ValueType.ARRAY) {
				String labelSeq = "Seq"+ SeqCounter;
				String u = P +"." + labelSeq ; SeqCounter++;
				Σ.add(createIRI(u), RDF.type, RDF.Seq);
				Σ.addLiteral(createIRI(u), RDFS.label,labelSeq );
				Σ.add( createIRI(P + ".has_" + k), RDFS.range, createIRI(u));
				Array((JsonArray) v, u, k, k);
			}

			Σ.add(createIRI(property), RDF.type, RDF.Property);
			Σ.addLiteral(createIRI(property), RDFS.label, label);
			Σ.add( createIRI(property), RDFS.domain, new ResourceImpl( createIRI(P) ) );
		});
		Σ.add( createIRI(P) , RDF.type, RDFS.Class);
		Σ.addLiteral( createIRI(P) , RDFS.label, P.substring(P.lastIndexOf('.') + 1));
	}

	private void Array (JsonArray φ, String P, String key, String implP) {
		String label = "ContainerMembershipProperty"+CMPCounter;
		String uu = P+"."+label; CMPCounter++;
		String uuIRI = createIRI(uu);
		Σ.add(uuIRI, RDF.type, RDFS.ContainerMembershipProperty);
		Σ.addLiteral(uuIRI, RDFS.label, label);
		Σ.add(uuIRI, RDFS.domain, createIRI(P));
		JsonValue v = φ.get(0);
		if (v.getValueType() == JsonValue.ValueType.STRING || v.getValueType() == JsonValue.ValueType.NUMBER) {
			Value(φ.get(0), uu, generateArrayAlias(P+"."+key));
		} else if (v.getValueType() == JsonValue.ValueType.OBJECT) {
			String u = key; ObjectCounter++;
			Σ.add( uuIRI, RDFS.range, createIRI(P + "." + u) );
			Value(φ.get(0), P + "." + u, generateArrayAlias(P+"."+key));
		}
		lateralViews.add(Pair.of(removeSeqs(P+"."+key),generateArrayAlias(P+"."+key)));
	}

	private String createIRI(String name){
		if(id.equals("")){
			return DataSourceVocabulary.Schema.val() + name;
		}
		return DataSourceVocabulary.Schema.val() + id+"/"+ name;
	}
	private void LiteralString (JsonString φ,  String P, String implP) {
		Σ.add(createIRI(P),RDFS.range,XSD.xstring);
		attributes.add(Pair.of(P,implP));
	}

	private void LiteralNumber (JsonNumber φ, String P, String implP) {
		Σ.add( createIRI(P), RDFS.range, XSD.integer);
		attributes.add(Pair.of(P,implP));
	}

//	private static void addTriple(Dataset d, String namedGraph, Resource s, Property p, Resource o){
//		Txn.executeWrite(d, ()-> {
//			Model graph = d.getNamedModel(namedGraph);
//			graph.add(s, p, o);
//		});
//	}

//	private add

	private void addMetaData(String name, String id, String path){
		String ds = DataSourceVocabulary.DataSource.val() +"/" + name;
		if (!id.equals("")){
			ds = DataSourceVocabulary.DataSource.val() +"/" + id;
			Σ.addLiteral( ds , DataSourceVocabulary.HAS_ID.val(), id);
		}
		addBasicMetaData(name, path, ds);
		Σ.addLiteral( ds , DataSourceVocabulary.HAS_FORMAT.val(), Formats.JSON.val());
		Σ.addLiteral( ds , DataSourceVocabulary.HAS_WRAPPER.val(), wrapper);

	}


	public static void main(String[] args) throws IOException {
		JSONBootstrap j = new JSONBootstrap();
		String D = "stations";

		Model M = j.bootstrapSchema(D,"src/main/resources/stations.json");

		Graph G = new Graph();
		G.setModel(M);
		java.nio.file.Path temp = Files.createTempFile("bootstrap",".ttl");
		System.out.println("Graph written to "+temp);
		G.write(temp.toString(),org.apache.jena.riot.Lang.TTL);

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


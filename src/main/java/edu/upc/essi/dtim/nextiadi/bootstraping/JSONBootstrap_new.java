package edu.upc.essi.dtim.nextiadi.bootstraping;


import edu.upc.essi.dtim.nextiadi.jena.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import javax.json.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Generates an RDFS-compliant representation of a JSON's document schema
 * @author snadal
 */
public class JSONBootstrap_new {

	private int ObjectCounter = 1;
	private int SeqCounter = 1;
	private int CMPCounter = 1;

	private Graph Σ;

	public JSONBootstrap_new(){
		reset();
	}

	private void reset(){
		Σ = new Graph();
		ObjectCounter = 1;
		SeqCounter = 1;
		CMPCounter = 1;
	}


	public Model bootstrapSchema(String iri, String path) throws FileNotFoundException {
		return bootstrap(iri, path);
	}

	public Model bootstrapSchema(String iri, InputStream fis) throws FileNotFoundException {
		reset();
		JsonValue φ = Json.createReader(fis).readValue();

		Value(φ,iri);
		return Σ.getModel();
	}

	private Model bootstrap(String D, String path) throws FileNotFoundException {
		reset();

		InputStream fis = new FileInputStream(path);

		JsonValue φ = Json.createReader(fis).readValue();

		Value(φ,D);
		return Σ.getModel();
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

	private void Value(JsonValue φ, String P) {
		if (φ.getValueType() == JsonValue.ValueType.STRING) LiteralString((JsonString)φ, P);
		else if (φ.getValueType() == JsonValue.ValueType.NUMBER) LiteralNumber((JsonNumber)φ, P);
		else if (φ.getValueType() == JsonValue.ValueType.OBJECT) Object((JsonObject)φ, P);
		else if (φ.getValueType() == JsonValue.ValueType.ARRAY) Array((JsonArray)φ, P, "Object");
	}

	private void Object (JsonObject φ, String P) {
		φ.keySet().forEach(k -> {
			JsonValue v = φ.get(k);

//			Σ.add(P+".has_"+k, RDF.type, RDF.Property);
//			Σ.add( P+".has_"+k, RDFS.domain, new ResourceImpl(P) );

//			addTriple(Σ,"G",new ResourceImpl(P+".has_"+k), RDF.type, RDF.Property);
//			addTriple(Σ,"G",new ResourceImpl(P+".has_"+k), RDFS.domain, new ResourceImpl(P));

			String property = P + ".has_" + k;

			if (v.getValueType() == JsonValue.ValueType.STRING) {
				property = P + "." +k;
				LiteralString((JsonString)v, property);
			} else if (v.getValueType() == JsonValue.ValueType.NUMBER) {
				property = P + "." +k;
				LiteralNumber((JsonNumber) v, property);
			} else if (v.getValueType() == JsonValue.ValueType.OBJECT) {
				String u = k; ObjectCounter++;
				Σ.add(P+".has_"+k, RDFS.range, P+"."+u);
				Object((JsonObject)v,P+"."+u);
			}
			else if (v.getValueType() == JsonValue.ValueType.ARRAY) {
				String u = P +".Seq" + SeqCounter; SeqCounter++;
				Σ.add(u, RDF.type, RDF.Seq);
				Σ.add( P + ".has_" + k, RDFS.range, u);
				Array((JsonArray) v, u, k);
			}

			Σ.add(property, RDF.type, RDF.Property);
			Σ.add( property, RDFS.domain, new ResourceImpl(P) );
		});
		Σ.add(P, RDF.type, RDFS.Class);
	}

	private void Array (JsonArray φ, String P, String key) {
		String uu = P+".ContainerMembershipProperty"+CMPCounter; CMPCounter++;
		Σ.add(uu, RDF.type, RDFS.ContainerMembershipProperty);
		Σ.add(uu, RDFS.domain, P);
		JsonValue v = φ.get(0);
		if (v.getValueType() == JsonValue.ValueType.STRING || v.getValueType() == JsonValue.ValueType.NUMBER) {
			Value(φ.get(0), uu);
		} else if (v.getValueType() == JsonValue.ValueType.OBJECT) {
			String u = key; ObjectCounter++;
			Σ.add( uu, RDFS.range, P + "." + u);
			Value(φ.get(0), P + "." + u);
		}
	}

	private void LiteralString (JsonString φ,  String P) {
		Σ.add(P,RDFS.range,XSD.xstring);
	}

	private void LiteralNumber (JsonNumber φ, String P) {
		Σ.add( P, RDFS.range, XSD.integer);
	}

//	private static void addTriple(Dataset d, String namedGraph, Resource s, Property p, Resource o){
//		Txn.executeWrite(d, ()-> {
//			Model graph = d.getNamedModel(namedGraph);
//			graph.add(s, p, o);
//		});
//	}

}


package edu.upc.essi.dtim.nextiadi.bootstraping;


import edu.upc.essi.dtim.nextiadi.jena.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.system.Txn;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import javax.json.*;
import java.io.*;

/**
 * Generates an RDFS-compliant representation of a JSON's document schema
 * @author snadal
 */
public class JSONBootstrap {

	private int ObjectCounter = 1;
	private int SeqCounter = 1;
	private int CMPCounter = 1;

	private Graph Σ;

	public JSONBootstrap(){
		reset();
	}

	private void reset(){
		Σ = new Graph();
		ObjectCounter = 1;
		SeqCounter = 1;
		CMPCounter = 1;
	}

	/**
	 *
	 * @param D namespace
	 * @param φ JsonObject content
	 */
	public Model bootstrapSchema(String D, String path) throws FileNotFoundException {
		reset();

		InputStream fis = new FileInputStream(path);

		JsonObject  φ = Json.createReader(fis).readObject();

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
		else if (φ.getValueType() == JsonValue.ValueType.ARRAY) Array((JsonArray)φ, P);
	}

	private void Object (JsonObject φ, String P) {
		φ.keySet().forEach(k -> {
			JsonValue v = φ.get(k);

			Σ.add(P+"."+k, RDF.type, RDF.Property);
			Σ.add( P+"."+k, RDFS.domain, new ResourceImpl(P) );
//			addTriple(Σ,"G",new ResourceImpl(P+"."+k), RDF.type, RDF.Property);
//			addTriple(Σ,"G",new ResourceImpl(P+"."+k), RDFS.domain, new ResourceImpl(P));

			if (v.getValueType() == JsonValue.ValueType.STRING) LiteralString((JsonString)v, P+"."+k);
			else if (v.getValueType() == JsonValue.ValueType.NUMBER) LiteralNumber((JsonNumber) v, P+"."+k);
			else if (v.getValueType() == JsonValue.ValueType.OBJECT) {
				String u = "Object"+ObjectCounter; ObjectCounter++;
				Σ.add(P+"."+k, RDFS.range, P+"."+u);
				Object((JsonObject)v,P+"."+u);
			}
			else if (v.getValueType() == JsonValue.ValueType.ARRAY) {
				String u = RDF.Seq.getURI() + SeqCounter; SeqCounter++;
				Σ.add(u, RDF.type, RDF.Seq);
				Σ.add( P + "." + k, RDFS.range, u);
				Array((JsonArray) v, u);
			}
		});
		Σ.add(P, RDF.type, RDFS.Class);
	}

	private void Array (JsonArray φ, String P) {
		String uu = RDFS.ContainerMembershipProperty.getURI()+CMPCounter; CMPCounter++;
		Σ.add(uu, RDF.type, RDFS.ContainerMembershipProperty);
		Σ.add(uu, RDFS.domain, P);
		JsonValue v = φ.get(0);
		if (v.getValueType() == JsonValue.ValueType.STRING || v.getValueType() == JsonValue.ValueType.NUMBER) {
			Value(φ.get(0), uu);
		} else if (v.getValueType() == JsonValue.ValueType.OBJECT) {
			String u = "Object" + ObjectCounter; ObjectCounter++;
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


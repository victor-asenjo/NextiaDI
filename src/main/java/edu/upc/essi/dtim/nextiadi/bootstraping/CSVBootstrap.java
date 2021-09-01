package edu.upc.essi.dtim.nextiadi.bootstraping;

import edu.upc.essi.dtim.nextiadi.jena.Graph;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
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
 * Generates an RDFS-compliant representation of a CSV file schema
 * @author snadal
 */
public class CSVBootstrap {

	Graph Σ;


	public CSVBootstrap(){
		Σ = new Graph();
	}


	public Model bootstrapSchema(String namespace, String path) throws IOException {
		Σ = new Graph();
		String P = namespace;

		BufferedReader br = new BufferedReader(new FileReader(path));
		CSVParser parser = CSVParser.parse(br, CSVFormat.DEFAULT.withFirstRecordAsHeader());

		Σ.add(P, RDF.type, RDFS.Class);
		parser.getHeaderNames().forEach(h -> {
			String h2 = h.replace("\"", "").trim();
//			System.out.println(h2);
			Σ.add(P+"."+h2,RDF.type,RDF.Property);
			Σ.add(P+"."+h2,RDFS.domain,P);
			Σ.add(P+"."+h2,RDFS.range,XSD.xstring);
		});

		return Σ.getModel();
	}

	public void write(String file, String lang){
		Σ.write(file,lang);
	}
//	public static void main(String[] args) throws IOException {
//		BufferedReader br = new BufferedReader(new FileReader("src/main/resources/cities.csv"));
//		CSVParser parser = CSVParser.parse(br, CSVFormat.DEFAULT.withFirstRecordAsHeader());
//
//		Dataset Σ = DatasetFactory.createTxnMem() ;
//		Σ.begin(ReadWrite.WRITE);
//
////		String P = "/cities.csv";
////		addTriple(Σ,"G",new ResourceImpl(P), RDF.type, RDFS.Class);
////		parser.getHeaderNames().forEach(h -> {
////			addTriple(Σ,"G",new ResourceImpl(P+"."+h),RDF.type,RDF.Property);
////			addTriple(Σ,"G", new ResourceImpl(P+"."+h),RDFS.domain,new ResourceImpl(P));
////			addTriple(Σ,"G", new ResourceImpl(P+"."+h),RDFS.range,XSD.xstring);
////		});
//
//		Σ.getNamedModel("G").write(new FileWriter("src/main/resources/cities.ttl"), "TTL");
//	}

//	private static void addTriple(Dataset d, String namedGraph, Resource s, Property p, Resource o){
//		Txn.executeWrite(d, ()-> {
//			Model graph = d.getNamedModel(namedGraph);
//			graph.add(s, p, o);
//		});
//	}

}


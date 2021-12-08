package edu.upc.essi.dtim;

import edu.upc.essi.dtim.nextiadi.jena.Graph;
import edu.upc.essi.dtim.nextiadi.models.Alignment;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.other.G;

import java.util.ArrayList;
import java.util.List;

public class Prueba {

    Graph graphO;
    List<Alignment> unused;

    public Prueba(){
        graphO  = new Graph();

        unused = new ArrayList<>();
    }

    public static void main(String[] args) {


//        Graph graphO = new Graph();
//        String file = "/Users/javierflores/Documents/UPC_projects/new/newODIN/api/src/test/resources/case01/Sergi/integrated_new.ttl";
//        graphO.loadModel(file);
//
//        Model m = graphO.generateOnlyIntegrations();
//        System.out.println(m.size());
//
//        String file2 = "/Users/javierflores/Documents/UPC_projects/new/newODIN/api/src/test/resources/case01/Sergi/integrated_new_clean.ttl";
//
//        Graph g = new Graph();
//        g.setModel(m);
//        g.write(file2, Lang.TURTLE)


//        Graph graphO = new Graph();
//        String file = "/Users/javierflores/Documents/UPC_projects/new/newODIN/api/src/test/resources/case01/Sergi/integrated_new.ttl";
//        graphO.loadModel(file);
//
//        Model m = graphO.generateOnlyIntegrations();
//        System.out.println(m.size());
//
//        String file2 = "/Users/javierflores/Documents/UPC_projects/new/newODIN/api/src/test/resources/case01/Sergi/integrated_new_clean.ttl";
//
//        Graph g = new Graph();
//        g.setModel(m);
//        g.write(file2, Lang.TURTLE);


//        graphO.getModel().difference(    )

    }
}

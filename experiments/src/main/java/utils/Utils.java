package utils;

import edu.upc.essi.dtim.nextiadi.config.Vocabulary;
import edu.upc.essi.dtim.nextiadi.jena.Graph;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

public class Utils {

    public static String getNumberOfClasses(Model m){
        ARQ.init();
        Graph g = new Graph();
        g.setModel(m);
        String query = "SELECT (COUNT ( DISTINCT ?class) AS ?count) WHERE { ?class <" + RDF.type.getURI() + "> <"+ RDFS.Class.getURI()+">.}";
        ResultSet res = g.runAQuery(query);

        if(res.hasNext()){
            QuerySolution r = res.next();
            return r.get("count").asLiteral().getString();
        }
        return "";

    }

    public static String getNumberOfOWLClasses(Model m){
        ARQ.init();
        Graph g = new Graph();
        g.setModel(m);
        String query = "SELECT (COUNT ( DISTINCT ?class) AS ?count) WHERE { ?class <" + RDF.type.getURI() + "> <"+ OWL.Class.getURI()+">.}";
        ResultSet res = g.runAQuery(query);

        if(res.hasNext()){
            QuerySolution r = res.next();
            return r.get("count").asLiteral().getString();
        }
        return "";

    }

    public static String getNumberOfProperties(Model m){
        ARQ.init();
        Graph g = new Graph();
        g.setModel(m);
        String query = "SELECT (COUNT ( DISTINCT ?property) AS ?count) WHERE { ?property <" + RDF.type.getURI() + "> <"+ RDF.Property.getURI()+">.}";
        ResultSet res = g.runAQuery(query);

        if(res.hasNext()){
            QuerySolution r = res.next();
//            System.out.println("Number properties: "+ r.get("count").toString());
            return r.get("count").asLiteral().getString();
        }
        return "";
    }

    public static String getNumberOfOWLObjectProperties(Model m){
        ARQ.init();
        Graph g = new Graph();
        g.setModel(m);
        String query = "SELECT (COUNT ( DISTINCT ?property) AS ?count) WHERE { ?property <" + RDF.type.getURI() + "> <"+ OWL.ObjectProperty.getURI()+">.}";
        ResultSet res = g.runAQuery(query);

        if(res.hasNext()){
            QuerySolution r = res.next();
//            System.out.println("Number properties: "+ r.get("count").toString());
            return r.get("count").asLiteral().getString();
        }
        return "";
    }

    public static String getNumberOfDProperties(Model m){
        ARQ.init();
        Graph g = new Graph();
        g.setModel(m);
        String query = "SELECT (COUNT ( DISTINCT ?property) AS ?count) WHERE { ?property <" + RDF.type.getURI() + "> <"+ RDF.Property.getURI()+">. ?property <"+RDFS.range.getURI()+"> <"+ XSD.xstring.getURI()+">}";
        ResultSet res = g.runAQuery(query);

        if(res.hasNext()){
            QuerySolution r = res.next();
//            System.out.println("Number datatype properties: "+ r.get("count").toString());
            return r.get("count").asLiteral().getString();
        }
        return "";
    }
    public static String getNumberOfOWLDProperties(Model m){
        ARQ.init();
        Graph g = new Graph();
        g.setModel(m);
        String query = "SELECT (COUNT ( DISTINCT ?property) AS ?count) WHERE { ?property <" + RDF.type.getURI() + "> <"+ OWL.DatatypeProperty.getURI()+">. ?property <"+RDFS.range.getURI()+"> <"+ XSD.xstring.getURI()+">}";
        ResultSet res = g.runAQuery(query);

        if(res.hasNext()){
            QuerySolution r = res.next();
//            System.out.println("Number datatype properties: "+ r.get("count").toString());
            return r.get("count").asLiteral().getString();
        }
        return "";
    }

    public static String getNumberOfIDProperties(Model m){
        ARQ.init();
        Graph g = new Graph();
        g.setModel(m);
        String query = "SELECT (COUNT ( DISTINCT ?property) AS ?count) WHERE { ?property <" + RDF.type.getURI() + "> <"+ Vocabulary.IntegrationDProperty.val()+">.}";
        ResultSet res = g.runAQuery(query);

        if(res.hasNext()){
            QuerySolution r = res.next();
//            System.out.println("Number I datatype properties: "+ r.get("count").toString());
            return r.get("count").asLiteral().getString();
        }
        return "";
    }


    public static String getNumberOfIOProperties(Model m){
        ARQ.init();
        Graph g = new Graph();
        g.setModel(m);
        String query = "SELECT (COUNT ( DISTINCT ?property) AS ?count) WHERE { ?property <" + RDF.type.getURI() + "> <"+ Vocabulary.IntegrationOProperty.val()+">.}";
        ResultSet res = g.runAQuery(query);

        if(res.hasNext()){
            QuerySolution r = res.next();
//            System.out.println("Number I object properties: "+ r.get("count").toString());
            return r.get("count").asLiteral().getString();
        }
        return "";
    }
    public static String getNumberOfIClasses(Model m){
        ARQ.init();
        Graph g = new Graph();
        g.setModel(m);
        String query = "SELECT (COUNT ( DISTINCT ?class) AS ?count) WHERE { ?class <" + RDF.type.getURI() + "> <"+ Vocabulary.IntegrationClass.val()+">.}";
        ResultSet res = g.runAQuery(query);

        if(res.hasNext()){
            QuerySolution r = res.next();
//            System.out.println("Number I classes: "+ r.get("count").toString());
            return r.get("count").asLiteral().getString();
        }
        return "";
    }

    public static String getDir(String directory, String fileName){
        if(directory.endsWith("/") ){
            return directory + fileName;
        }
        return directory + "/"+  fileName;
    }

    public static String getStats(Model integrated){
        String s = "";
        s = s + "," + getNumberOfClasses(integrated) + "," + getNumberOfIClasses(integrated);
        int dt = Integer.parseInt(getNumberOfDProperties(integrated)) ;
        s = s + "," + dt + ","+ getNumberOfIDProperties(integrated);
        int np = Integer.parseInt( getNumberOfProperties(integrated));
        int object = np -dt;
        s = s + "," + object + "," + getNumberOfIOProperties(integrated);
        return s;
    }


    public static String getStatsOWL(Model integrated){
        String s = "";
        s = s + "," + getNumberOfOWLClasses(integrated) + "," + getNumberOfIClasses(integrated);
        int dt = Integer.parseInt(getNumberOfOWLDProperties(integrated)) ;
        s = s + "," + dt + ","+ getNumberOfIDProperties(integrated);
        int object = Integer.parseInt( getNumberOfOWLObjectProperties(integrated));
        s = s + "," + object + "," + getNumberOfIOProperties(integrated);
        return s;
    }

    public  static Model setPrefixes(Model m) {
        m.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        m.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        m.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        m.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
        return m;
    }



}

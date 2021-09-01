package edu.upc.essi.dtim.nextiadi.jena;

import edu.upc.essi.dtim.nextiadi.config.Vocabulary;
import edu.upc.essi.dtim.nextiadi.utils.Alignment;
import edu.upc.essi.dtim.nextiadi.utils.models.Subject;
import lombok.Getter;
import lombok.Setter;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter @Setter
public class Graph {

    private Model model;

    public Graph(){
        model = ModelFactory.createDefaultModel();
    }

    public void add(String subject, String predicate, String object) {
        Resource r = model.createResource(subject);
        r.addProperty(model.createProperty(predicate), model.createResource(object));
    }

    public void add(String subject, Property predicate, Resource object) {
        Resource r = model.createResource(subject);
        r.addProperty(predicate, object);
    }

    public void add(String subject, Property predicate, String object) {
        Resource r = model.createResource(subject);
        r.addProperty(predicate, model.createResource(object));
    }

//    public void add(String subject, String predicate, String literal,) {
//        Resource r = model.createResource(subject);
//
//        r.addProperty(model.createProperty(predicate), literal);
//        System.out.println("hola");
//    }

    public void deleteResource(String uri) {
        deleteSubject(uri);
        deleteObject(uri);
    }

    public void deleteSubject(String uri) {
        Resource r = model.createResource(uri);
        model.removeAll(r, null, null);
    }

    public void deleteObject(String uri) {
        Resource r = model.createResource(uri);
        model.removeAll(null, null, r);
    }

//    public void deleteTriple(String subject, String predicate, String object) {
//
//        model.remove(subject, predicate, object)
//
//    }


    public void generateMetaModel() {

        add(Vocabulary.IntegrationClass.val(), RDF.type.getURI(), OWL.Class.getURI()  );
        add(Vocabulary.IntegrationDProperty.val(),RDF.type.getURI(), OWL.Class.getURI()  );
        add(Vocabulary.IntegrationOProperty.val(),RDF.type.getURI(), OWL.Class.getURI()  );

    }

    public void removeMetaModel() {

        delete(Vocabulary.IntegrationClass.val(), RDF.type.getURI(), OWL.Class.getURI()  );
        delete(Vocabulary.IntegrationDProperty.val(),RDF.type.getURI(), OWL.Class.getURI()  );
        delete(Vocabulary.IntegrationOProperty.val(),RDF.type.getURI(), OWL.Class.getURI()  );

    }

    public void delete(String subject, String predicate, String object){
        model.removeAll(new ResourceImpl(subject), new PropertyImpl(predicate), new ResourceImpl(object));
    }

    public void replaceIntegratedClass(Alignment alignment) {


        updateResourceNodeIRI(alignment.getIriA(), alignment.getIriL());
        updateResourceNodeIRI(alignment.getIriB(), alignment.getIriL());

    }


    /**
     * Delete triple with oldIri and insert new triple with newIri in jena graph
     * @param oldIRI actual iri that appears in the triples.
     * @param newIRI new iri that is going to replace the actual iri.
     */
    public void updateResourceNodeIRI(String oldIRI, String newIRI){
        // Look and update triples where oldIRI is object.
        runAnUpdateQuery("DELETE {?s ?p <"+oldIRI+">} " +
                "INSERT {?s ?p <"+newIRI+">} WHERE {  ?s ?p <"+oldIRI+"> }");
        // Look and update triples where oldIRI is subject.
        runAnUpdateQuery("DELETE {<"+oldIRI+"> ?p ?o} " +
                "INSERT {<"+newIRI+"> ?p ?o} WHERE {  <"+oldIRI+"> ?p ?o }");
    }

    public  void runAnUpdateQuery(String sparqlQuery) {

        try {
                UpdateAction.parseExecute(sparqlQuery, model);
        } catch (Exception e) {
                e.printStackTrace();
        }
    }

    public ResultSet runAQuery(String query) {

        try (QueryExecution qExec = QueryExecutionFactory.create(QueryFactory.create(query), model)) {
            ResultSetRewindable results = ResultSetFactory.copyResults(qExec.execSelect());
            qExec.close();
            return results;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean contains(String subject, String predicate, String object){

        return model.contains(model.createResource(subject), model.createProperty(predicate), model.createResource(object)  );
    }


    public boolean isIntegratedClass(String uri){

        if (contains(uri, RDF.type.getURI(), Vocabulary.IntegrationClass.val()  ) ) {
            return true;
        }
        return false;
    }

//    public boolean isDomainIntegratedClass(String uri) {
//
//        String domain = getDomain(uri);
//        if( isIntegratedClass(domain) ) {
//            return true;
//        } else {
//            return contains(uri, RDFS.subClassOf.getURI(), Vocabulary.IntegrationClass.val());
//        }
//
//    }


    public boolean isIntegratedDatatypeProperty(String uri){

        if (contains(uri, RDF.type.getURI(), Vocabulary.IntegrationDProperty.val()  ) ) {
            return true;
        }
        return false;
    }


    public boolean isIntegratedObjectProperty(String uri){

        if (model.contains(model.createResource(uri), model.createProperty(RDF.type.getURI()), model.createResource(Vocabulary.IntegrationOProperty.val())  ) ) {
            return true;
        }
        return false;
    }

    public String getFlexibleRange(Alignment a){

        String rangeA = getRange(a.getIriA());
        String rangeB = getRange(a.getIriB());

        if(rangeA.equals(rangeB)){
            return rangeA;
        } else if (rangeA.equals(XSD.xstring.getURI()) ){
            return rangeA;
        } else if (rangeB.equals(XSD.xstring.getURI()) ){
            return rangeB;
        } else {
            return rangeA;
        }


    }



    public String deleteS(){

        List<Subject> listS = new ArrayList<>();
        for( Resource r : model.listSubjects().toList() ) {

            Subject s = new Subject();
            s.setIri(r.getURI());

            for (Statement statement : r.listProperties().toList()){

                if(statement.getPredicate().equals(RDF.type)){
                    s.setType(statement.getObject().toString());
                } else if(statement.getPredicate().equals(RDFS.domain)){
                    s.setDomain(statement.getObject().toString());
                } else if(statement.getPredicate().equals(RDFS.range)){
                    s.setRange(statement.getObject().toString());
                } else{
                    // do nothing. Probably statement not useful for graphical graph
                }

            }
            if(s.getType() == null) {
                System.out.println("SOMETHING IS WRONG IN THE MODEL. No type definition for resource "+s.getIri());
            } else {
                listS.add(s);
            }

        }

    return "";
    }

    public void test(){
        String query =   "PREFIX rdfs: <"+RDFS.getURI()+">" +
                "PREFIX rdf: <"+RDF.getURI()+">" +
                "SELECT * WHERE { " +
                " ?integratedD rdf:type <"+Vocabulary.IntegrationDProperty.val()+">." +
                " ?s rdfs:subPropertyOf ?integratedD."  +
                " ?s ?dr ?domainRange." +
                " ?s rdf:type ?type." +
                " FILTER (?dr = rdfs:domain || ?dr = rdfs:range )" +
                "}";

        ResultSet result = runAQuery(query);
    }

//    public void minimalIDProperties(){
//
//        String queryD = "PREFIX rdfs: <"+RDFS.getURI()+">" +
//                "PREFIX rdf: <"+RDF.getURI()+">" +
//                "DELETE {" +
//                " ?s rdfs:subPropertyOf ?integratedD. " +
//                "?s rdfs:domain ?domain." +
//                "?s rdfS:range ?range." +
//                "?s rdf:type ?type." +
//                "} " +
//                "INSERT {} WHERE { " +
//                " ?integratedD rdf:type <"+Vocabulary.IntegrationDProperty+">." +
//                " ?s rdfs:subPropertyOf ?integratedD."  +
//                " ?s rdfs:domain ?domain." +
//                " ?s rdfs:range ?range." +
//                " ?s rdf:type ?type." +
//                " }";
//        runAnUpdateQuery(queryD);
//
//    }

    public void minimalIOProperties(){

        String queryD = "PREFIX rdfs: <"+RDFS.getURI()+">" +
                "PREFIX rdf: <"+RDF.getURI()+">" +
                "DELETE {" +
                " ?s rdfs:subPropertyOf ?integratedD. " +
                "?s ?dr ?domainRange." +
//                "?s rdf:range ?range." +
                "?s rdf:type ?type." +
                "} " +
                "INSERT {} WHERE { " +
                " ?integratedD rdf:type ?typeInt." +
                " ?s rdfs:subPropertyOf ?integratedD."  +
                " ?s ?dr ?domainRange." +
//                " ?s rdfs:range ?range." +
                " ?s rdf:type ?type." +
                " FILTER (?dr = rdfs:domain || ?dr = rdfs:range )" +
                " FILTER (?typeInt = <"+Vocabulary.IntegrationOProperty.val()+"> || ?typeInt = <"+Vocabulary.IntegrationDProperty.val()+"> )" +
                " }";
        runAnUpdateQuery(queryD);
    }


    public void minimalClasses(){

        String queryD = "PREFIX rdfs: <"+RDFS.getURI()+">" +
                "PREFIX rdf: <"+RDF.getURI()+">" +
                "DELETE {" +
                " ?s rdfs:subClassOf ?integratedD."  +
                " ?property ?dr ?s." +
                " ?s rdf:type ?type." +
                "} " +
                "INSERT {" +
                "?property ?dr ?integratedD." +
                "} WHERE { " +
                " ?integratedD rdf:type <"+Vocabulary.IntegrationClass.val()+">." +
                " ?s rdfs:subClassOf ?integratedD."  +
                " ?property ?dr ?s." +
                " ?s rdf:type ?type." +
                " FILTER (?dr = rdfs:domain || ?dr = rdfs:range )" +
                " }";
        runAnUpdateQuery(queryD);
    }





    public String getRange(String subject) {

        String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                "SELECT ?range WHERE { <"+subject+"> rdfs:range ?range.}";
        List<String> result =  getVar(query, "range");
        if(result.isEmpty()){
            return null;
        } else {
            return result.get(0);
        }

    }

    public String getSuperRangeFromProperty(String property) {

        String range = getRange(property);

        String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                "SELECT ?superClass WHERE { <"+range+"> rdfs:subClassOf ?superClass.}";
        List<String> result =  getVar(query, "superClass");
        if(result.isEmpty()){
            return range;
        } else {
            return result.get(0);
        }

    }

    public String getSuperDomainFromProperty(String property) {

        String domain = getDomain(property);

        String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                "SELECT ?superClass WHERE { <"+domain+"> rdfs:subClassOf ?superClass.}";
        System.out.println(query);
        List<String> result = getVar(query, "superClass");
        if(result.isEmpty()){
            System.out.println("default domain is: "+domain);
            return domain;
        } else {
            System.out.println(result.get(0));
            return result.get(0);
        }
    }

    public String getDomain(String subject) {

        String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                "SELECT ?domain WHERE { <"+subject+"> rdfs:domain ?domain.}";
        List<String> result = getVar(query, "domain");
        if(result.isEmpty()){
            return null;
        } else {
            return result.get(0);
        }
    }

    private List<String> getVar(String query, String varname) {

        ResultSet results = runAQuery(query);
        List<String> rows = new ArrayList<>();
        while(results.hasNext()) {
            QuerySolution solution = results.nextSolution();
            rows.add(solution.getResource(varname).getURI());
        }
        if(rows.isEmpty()){
            System.out.println("error, no "+varname+" definition....");
        }
        return rows;
    }

//    public void replaceIntegratedClass(String old, String uri){
//
//        if(old != uri) {
//
//            model.remove(old, null,null)
//
//
//        }
//
//    }
//    public void removePropertyValue(String uri, String property, String value) {
//        Resource r = model.createResource(uri);
//        Property p = model.createProperty(property);
//        Literal l = model.createLiteral(value);
//        model.removeAll(r, p, l);
//    }

    public void connect(String subject, String predicate, String object) {
        Resource r = model.createResource(subject);
        Resource r2 = model.createResource(object);
        r.addProperty(model.createProperty(predicate), r2);
    }

    public void loadModel(String path){
//        model = RDFDataMgr.loadModel(path);
        model.read(path, "TURTLE");
    }

    public Map<String,List<Alignment>> performConcordanceProperties(String uri, List<Alignment> alignments){

        List properties = new ArrayList();
        String prefix = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ";
        String query1 = prefix + "SELECT ?class WHERE { ?class rdfs:subClassOf <"+uri+">.}";
        String query2 = prefix + "SELECT ?property WHERE { ?property rdfs:subClassOf <%s>.}";
        for ( String c : getVar(query1, "class") ) {
            for (String p : getVar( String.format(query2, c) , "property") ) {
                properties.add(p);
            }

        }



        List<Alignment> datatypes = new ArrayList();
        List<Alignment> object = new ArrayList();
        for (Alignment a: alignments) {

            if(properties.contains(a.getIriA()) & properties.contains(a.getIriB() )) {

                if ( contains(a.getIriA(), RDF.type.getURI(), OWL.DatatypeProperty.getURI()) ) {
                    datatypes.add(a);
                } else {
                    object.add(a);
                }

            }

        }

        Map<String,List<Alignment>> map =new HashMap();
        map.put("datatype",datatypes);
        map.put("object",object);

        return map;

    }

    public void write(String file, Lang lang) {
        try {
            RDFDataMgr.write(new FileOutputStream(file), model, Lang.TURTLE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void write(String file, String lang) {
        try {
            RDFDataMgr.write(new FileOutputStream(file), model, Lang.TURTLE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}

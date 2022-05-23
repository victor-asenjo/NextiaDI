package others;

import edu.upc.essi.dtim.NextiaDI;
import edu.upc.essi.dtim.nextiadi.config.Namespaces;
import edu.upc.essi.dtim.nextiadi.jena.Graph;
import edu.upc.essi.dtim.nextiadi.models.Alignment;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import utils.pojos.ResourceExp1;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class IntegrationEx1_setup {

    Random rand = new Random(25);

    public Map<ResourceExp1, List<ResourceExp1>> getClassesAndProperties(Model m){
        Graph g = new Graph();
        g.setModel(m);
        ARQ.init();
        String query = "SELECT DISTINCT ?class WHERE { ?class <" + RDF.type.getURI() + "> <"+RDFS.Class.getURI()+">.}";
        ResultSet res = g.runAQuery(query);

        Map<ResourceExp1, List<ResourceExp1>> entities = new HashMap<>();
        while (res.hasNext()) {
            QuerySolution x = res.next();
            Resource r = x.get("class").asResource();
            if(r.getLocalName().contains("."))
//                entities.add(new ResourceExp1(r, "class", false ) );
                entities.put(new ResourceExp1(r, "class", false, new HashSet<>(), null ), new ArrayList<>());
        }
        String queryP = "SELECT DISTINCT ?property ?range ?domain WHERE { ?property <" + RDF.type.getURI() + "> <"+RDF.Property.getURI()+">. ?property  <" + RDFS.range.toString() + "> ?range.  ?property  <" + RDFS.domain.toString() + "> ?domain  .}";
        res = g.runAQuery(queryP);
        while (res.hasNext()) {
            QuerySolution x = res.next();
            Resource r = x.get("property").asResource();
            if(containsKeyByURI(entities.keySet(), x.get("domain").asResource())  ){
                if(x.get("range").toString().contains(XSD.getURI())) {
                    entities.get(getKeyByURI(entities.keySet(), x.get("domain").asResource()))
                            .add(new ResourceExp1(r, "datatype", false, new HashSet<>(), x.get("domain").asResource().getURI()) );
                } else {
                    entities.get(getKeyByURI(entities.keySet(), x.get("domain").asResource()))
                            .add(new ResourceExp1(r, "object", false, new HashSet<>(), x.get("domain").asResource().getURI()) );
                }
            }
        }
        return entities;
    }

    public boolean containsKeyByURI(final Set<ResourceExp1> list, final Resource r){
        return list.stream().filter(o -> o.getResource().getURI().equals(r.getURI())).findFirst().isPresent();
    }
    public ResourceExp1 getKeyByURI(final Set<ResourceExp1> list, final Resource r){
        return list.stream().filter(o -> o.getResource().getURI().equals(r.getURI())).findFirst().get();
    }
//    public boolean containsEntity(final List<ResourceExp1> list, final String URI){
//        return list.stream().filter(o -> o.getResource().getURI().equals(URI)).findFirst().isPresent();
//    }
   /* public ResourceExp1 findKey(Map<ResourceExp1, List<ResourceExp1>> entitiesP, Resource r){
        entitiesP.containsValue()
        entitiesP.values().stream().
    }
*/

    public String getProperty(Model m, String integratedURI){
        Graph g = new Graph();
        g.setModel(m);
        String query = "SELECT DISTINCT ?property WHERE { ?property <" + RDFS.domain.getURI() + "> ?domain. ?domain <"+RDFS.subClassOf.getURI()+"> <"+integratedURI+">.}";
        ResultSet res = g.runAQuery(query);
        if(res.hasNext()){
            QuerySolution x = res.next();
            return x.get("property").asResource().getURI();
        }
        // if null, it's gonna be integrated in this iteration
        System.out.println("NULL******************");
        return null;
    }

    public List<Alignment> generateRandomAlignments(Model A, Model integrated, int iteration, List<String> integratedR) {

        Map<ResourceExp1, List<ResourceExp1>> entitiesP = getClassesAndProperties(A);
        List<Alignment> alignments = new ArrayList<>();

        List<ResourceExp1> entities = new ArrayList<>(entitiesP.keySet() );
        List<ResourceExp1> properties = new ArrayList<>();

        if(iteration == 4){
            System.out.println("hola");
        }
        int indexEntity;
        for (int i = 1; i <= iteration; i++) {
            Alignment a = new Alignment();
            int iA = iteration -1;
            if(iteration==1) iA = iteration+1;
            int randomNum = rand.nextInt((2 - 1) + 1) + 1;
            List<ResourceExp1> availableEntities = entities.stream().filter(x -> !x.getIntegrated()).collect(Collectors.toList());
            List<ResourceExp1> availableProperties = properties.stream().filter(x -> !x.getIntegrated()).collect(Collectors.toList());
            if(availableEntities.size() == 0 && availableProperties.size() >0){
                randomNum =2;
            }

            if( randomNum == 2 && availableProperties.size() > 0) { //property
                indexEntity = rand.nextInt(availableProperties.size());
                ResourceExp1 property = availableProperties.get(indexEntity);

                if(integratedR.contains(getIntegratedIRI(property.getResource()))){
                    a.setIriA(getIntegratedIRI(property.getResource()));
                } else {
                    String uri = getProperty(integrated, getIntegratedIRI(property.getDomain()));
                    int iA2 = iA;
                    if(uri != null) {
                        iA2 = Integer. parseInt(uri.substring(uri.lastIndexOf('_') + 1 ));
                    }
                    a.setIriA(getIRI( property.getResource(), iA2));
                    integratedR.add(getIntegratedIRI(property.getResource()));
//                    property.getIntegratedI().add(iteration);
                }

                a.setIriB(getIRI( property.getResource(), iteration));
                a.setType(property.getType());
                a.setL(createL(property.getResource()));
                property.setIntegrated(true);
//                property.getIntegratedI().add(iteration);
            } else {

                indexEntity = rand.nextInt(availableEntities.size());

                ResourceExp1 entity = availableEntities.get(indexEntity);
                if(integratedR.contains(getIntegratedIRI(entity.getResource()))){
                    a.setIriA(getIntegratedIRI(entity.getResource()));
                } else {
                    // todo if(i ==1) i+1
                    a.setIriA(getIRI(entity.getResource(), iA));
                    integratedR.add(getIntegratedIRI(entity.getResource()));
//                    entity.getIntegratedI().add(iA);

                }
                a.setIriB(getIRI(entity.getResource(), iteration));
                a.setType("class");
                a.setL(createL(entity.getResource()));


                properties.addAll( entitiesP.get(entity));
                entity.setIntegrated(true);
//                entity.getIntegratedI().add(iteration);

            }
            alignments.add(a);
        }
        return alignments;
    }


//    public Alignment createAlignmentEntity(Alignment a,int i,List<Resource> availableEntities, Map<String, Set<String>> integratedE ){
//        int indexEntity = rand.nextInt(availableEntities.size());
//        Resource entity = availableEntities.remove(indexEntity);
////        entitiesProperties.add(entity);
//        if(integratedE.containsKey(getIntegratedIRI(entity))){
//            a.setIriA(getIntegratedIRI(entity));
//            integratedE.get(getIntegratedIRI(entity)).add(getIRI(entity, i));
//        } else {
//            // todo if(i ==1) i+1
//            a.setIriA(getIRI(entity, i-1));
//            integratedE.put(getIntegratedIRI(entity), new HashSet<>());
//            integratedE.get(getIntegratedIRI(entity)).add(getIRI(entity, i-1));
//            integratedE.get(getIntegratedIRI(entity)).add(getIRI(entity, i));
//        }
//        a.setIriB(getIRI(entity, i));
//        a.setType("class");
//        a.setL(createL(entity));
//
//        return a;
//    }

    public String createL(Resource r) {
//        System.out.println("** "+r.getLocalName().substring(r.getLocalName().lastIndexOf('.') + 1));

        String[] names = r.getLocalName().split("\\.");
        String name = names[names.length-2].replaceAll("_([0-9]+)","") +"_" +names[names.length-1].replaceAll("_([0-9]+)","");
/*.substring(0,2)*/
//        return "Integrated_"  +r.getLocalName().substring(r.getLocalName().lastIndexOf('.') + 1,r.getLocalName().lastIndexOf('_') );
        return "Integrated_"  +name;
    }

    public String createL(String r) {
        String[] names = r.split("\\.");
        String name = names[names.length-2].replaceAll("_([0-9]+)","") +"_" +names[names.length-1].replaceAll("_([0-9]+)","");
        return "Integrated_"  +name;
    }

    public String getIRI(Resource r, int i1) {

        //        String integratedIri = Namespaces.NextiaDI.val() + createL(r);
//        Resource integratedR = ResourceFactory.createResource(integratedIri);
//        Resource iriR = ResourceFactory.createResource(iri);
//        if (m.containsResource(integratedR) && !m.containsResource(iriR)) {
//            return integratedIri;
//        } else {
//            for (int i = 0; !m.containsResource(ResourceFactory.createResource(iri)); ++i) {
//                iri = iri.replaceAll("Seq[0-9]*.", "Seq" + i + ".");
//            }http://www.essi.upc.edu/DTIM/NextiaDI/Integrated_artworks_1
//        System.out.println("iri : "+ r.getURI());
            return r.getURI().replaceAll("_([0-9]+)", "_" + i1);
//        }

    }

    public String getIntegratedIRI(Resource r){
//        System.out.println("integrated iri : "+ r.getURI());
        return Namespaces.NextiaDI.val() + createL(r);
    }
    public String getIntegratedIRI(String r){
//        System.out.println("integrated iri : "+ r.getURI());
        return Namespaces.NextiaDI.val() + createL(r);
    }

    public void writeAlignments(int number, List<Alignment> alignments) {
        List<String> newDoc = new ArrayList<>();
        newDoc.add("iriA,iriB,Integrated_label,type");

        for (Alignment a : alignments) {
//            String localNameA = ResourceFactory.createResource(a.getIriA()).getLocalName();
//            String str = ResourceFactory.createResource(a.getIriB()).getLocalName();
            String newLine = a.getIriA() + "," + a.getIriB() + "," + a.getL() + "," + a.getType();
            newDoc.add(newLine);
        }

        try {
            String name = "alignments_I_"+number;
            if(number == 1){
                name = "alignments_" + 1 + "_"+2;
            }
            String directory = "/Users/javierflores/Documents/upc/projects/NextiaDI/experiments/src/main/resources/output/integration/input/alignments";
            FileWriter writer = new FileWriter(directory + "/" + name + ".csv");

            for (String line : newDoc) {
                writer.write(line + System.lineSeparator());
            }
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public void generateAlignments(int nfiles) {
        List<String> times = new ArrayList<>();
        List<String> integratedResources = new ArrayList<>();
        times.add("iteration,time(milliseconds)");
        Model integrated = readModelBootstrap(1);
        for(int i = 1; i <= nfiles; i++){

            Model A = readModelBootstrap(i);
            if(i == 1){
                A = readModelBootstrap(i+1);
            }

            List<Alignment> alignments = generateRandomAlignments(A, integrated,i, integratedResources);
            writeAlignments(i, alignments );

            System.out.println("Integrating: I(...) and " + i);
            if(i == 9){
                System.out.println("his");
            }
            NextiaDI n = new NextiaDI();
            long startTime = System.nanoTime();
            integrated = n.Integrate(integrated, A, alignments);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000L;
            List<Alignment> unused = n.getUnused();
            Model minimal = setPrefixes(n.getMinimalGraph(), i);
            System.out.println("unused: "+unused.size());
            unused.forEach(a -> System.out.println(a.getIriA() +" - "+ a.getIriB()));

            times.add(i + "," + duration);
//            String dir = "/Users/javierflores/Documents/upc/projects/NextiaDI/experiments/src/main/resources/output/integration/results/";
            try {
                writeModels(integrated, minimal, i);
//                RDFDataMgr.write(new FileOutputStream(dir + "integrated_"+ i + "_" + i2+".ttl"), integrated, Lang.TRIG);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

        for(String t: times){
            System.out.println(t);
        }
    }


    public Model setPrefixes(Model m, int iteration) {
        m.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        m.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        m.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        m.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
        m.setNsPrefix("exp", "http://experiment1.com/" + iteration + "/");
        m.setNsPrefix("global", "http://www.essi.upc.edu/dtim/ontology/Global/");
        return m;
    }

    public Model readModelBootstrap(int iteration) {
        String path = "/Users/javierflores/Documents/upc/projects/NextiaDI/experiments/src/main/resources/output/integration/ArtCollection" + iteration + ".ttl" ;
        return RDFDataMgr.loadModel(path);
    }

    public void writeModels(Model integrated, Model minimal, int iteration) throws FileNotFoundException {
        String path = "/Users/javierflores/Documents/upc/projects/NextiaDI/experiments/src/main/resources/output/integration/" + "/results/";
        int i2 = iteration + 1;
        RDFDataMgr.write(new FileOutputStream(path + "integrated_"+ iteration + "_" + i2+".ttl" ), integrated, Lang.TRIG);
        RDFDataMgr.write(new FileOutputStream(path + "minimal"+ iteration + "_" + i2+".ttl"), minimal, RDFFormat.TURTLE_PRETTY);
    }


    public static void main(String[] args) {

        IntegrationEx1_setup ex = new IntegrationEx1_setup();
//        HashMap<String, List<ResourceExp1>> m = ex.getClassesAndProperties(model);
//        List<Alignment> alignments = ex.generateRandomAlignments(A, B, 1);
//        ex.writeAlignments(1, alignments );
        ex.generateAlignments(60);
        System.out.println("hola");



    }

}

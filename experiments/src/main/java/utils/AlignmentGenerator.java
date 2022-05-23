package utils;


import edu.upc.essi.dtim.NextiaDI;
import edu.upc.essi.dtim.nextiadi.config.Namespaces;
import edu.upc.essi.dtim.nextiadi.jena.Graph;
import edu.upc.essi.dtim.nextiadi.models.Alignment;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import utils.pojos.ResourceExp1;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class AlignmentGenerator {

    Random rand = new Random(25);

    String directory;
    String baseBootstrapFileName;
    String bootstrapFolder;
    String alignmentsFolder = "alignments";
    String baseAlignmentFileName = "alignments";

    public AlignmentGenerator(String dir, String bootstrapFolder, String baseBootstrapFileName){
        this.directory = dir;
        this.baseBootstrapFileName = baseBootstrapFileName;
        this.bootstrapFolder = bootstrapFolder;
        ARQ.init();
    }

    public Model readModelBootstrap(int iteration) {
        return RDFDataMgr.loadModel( getDir(bootstrapFolder + "/" + baseBootstrapFileName + iteration+".ttl")  );
    }

    public String getDir(String fileName){
        if(directory.endsWith("/") ){
            return directory + fileName;
        }
        return directory + "/"+  fileName;
    }

    public Map<ResourceExp1, List<ResourceExp1>> getClassesAndPropertiesExp2(Model m){
        Graph g = new Graph();
        g.setModel(m);
        String query = "SELECT DISTINCT ?class WHERE { ?class <" + RDF.type.getURI() + "> <"+ RDFS.Class.getURI()+">.}";
        ResultSet res = g.runAQuery(query);

        Map<ResourceExp1, List<ResourceExp1>> entities = new HashMap<>();
        while (res.hasNext()) {
            QuerySolution x = res.next();
            Resource r = x.get("class").asResource();
            if(r.getLocalName().contains(".") && !r.getLocalName().contains("object"))
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


    public Map<ResourceExp1, List<ResourceExp1>> getClassesAndProperties(Model m){
        Graph g = new Graph();
        g.setModel(m);
        String query = "SELECT DISTINCT ?class WHERE { ?class <" + RDF.type.getURI() + "> <"+ RDFS.Class.getURI()+">.}";
        ResultSet res = g.runAQuery(query);

        Map<ResourceExp1, List<ResourceExp1>> entities = new HashMap<>();
        while (res.hasNext()) {
            QuerySolution x = res.next();
            Resource r = x.get("class").asResource();
            if(r.getLocalName().contains("."))
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

    public void generateAlignmentsExp2(int nfiles){
        try {
            Files.createDirectories(Paths.get(getDir(alignmentsFolder) ));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        List<String> times = new ArrayList<>();
        times.add("iteration,time(milliseconds)");
        Model integrated = readModelBootstrap(1);

        for(int i = 1; i <= nfiles; i++){

            if(i!=2) { // iteration 2 is integrated with i=1
                Model A = readModelBootstrap(i);
                if(i == 1){
                    A = readModelBootstrap(i+1);
                }
                List<Alignment> alignments = generateFixedAlignments(A, integrated, i );

                if(i == 6){
                    System.out.println("hola");
                }
            NextiaDI n = new NextiaDI();
            long startTime = System.nanoTime();
            integrated = n.Integrate(integrated, A, alignments);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000L;
            List<Alignment> unused = n.getUnused();
            System.out.println("iteration "+i+ "unused: "+unused.size());
            unused.forEach(a -> System.out.println(a.getIriA() +" - "+ a.getIriB()));
            times.add(i + "," + duration);
                writeAlignments(i, alignments );
            }

        }
        times.forEach(System.out::println);
    }

    public List<Alignment> generateFixedAlignments(Model A, Model B, int iteration) {

        List<Alignment> alignments = new ArrayList<>();
        Map<ResourceExp1, List<ResourceExp1>> entitiesP = getClassesAndPropertiesExp2(A);
        Alignment a;

//        if(iteration == 4) {
//            System.out.println("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/exp1_6.Seq6.educators.name ");
//
//        }

        for( var entry: entitiesP.entrySet() ) {

            ResourceExp1 entity = entry.getKey();
            a = new Alignment();
            if( iteration == 1 ){
                a.setIriA(getIRI(entity.getResource().getURI(), iteration, B));
                a.setIriB(getIRI(entity.getResource().getURI(), iteration+1));
            } else {
                a.setIriA(getIntegratedIRI(entity.getResource().getURI()));
                a.setIriB(getIRI(entity.getResource().getURI(), iteration));
            }
            a.setL(createL(entity.getResource().getURI()));
            a.setType(entity.getType());
            alignments.add(a);
            for(ResourceExp1 property : entry.getValue()){
                a = new Alignment();
                if( iteration == 1 ){
                    a.setIriA(getIRI(property.getResource().getURI(), iteration, B));
                    a.setIriB(getIRI(property.getResource().getURI(), iteration+1));
                } else {
                    a.setIriA(getIntegratedIRI(property.getResource().getURI()));
                    a.setIriB(getIRI(property.getResource().getURI(), iteration));
                }
                a.setL(createL(property.getResource().getURI()));
                a.setType(property.getType());
                alignments.add(a);
            }
        }

        return alignments;
    }

    public List<Alignment> generateRandomAlignments(Model A, Model integrated, int iteration, List<String> integratedR) {

        Map<ResourceExp1, List<ResourceExp1>> entitiesP = getClassesAndProperties(A);
        List<Alignment> alignments = new ArrayList<>();

        List<ResourceExp1> entities = new ArrayList<>(entitiesP.keySet() );
        List<ResourceExp1> properties = new ArrayList<>();

//        if(iteration == 7){
//            System.out.println("hola");
//        }
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

                if(integratedR.contains(getIntegratedIRI(property.getResource().getURI()))){
                    a.setIriA(getIntegratedIRI(property.getResource().getURI()));
                } else {
                    String uri = getProperty(integrated, getIntegratedIRI(property.getDomain()));
                    int iA2 = iA;
                    if(uri != null) {
                        try{
                            iA2 = Integer.parseInt(uri.substring(uri.lastIndexOf('_') + 1 ));
                        } catch (NumberFormatException ex) {
                            iA2 = Integer.parseInt(uri.substring(uri.lastIndexOf('_') + 1, uri.lastIndexOf('.') ));
                        }

                    }
                    a.setIriA(getIRI( property.getResource().getURI(), iA2));
                    integratedR.add(getIntegratedIRI(property.getResource().getURI()));
                }

                a.setIriB(getIRI( property.getResource().getURI(), iteration));
                a.setType(property.getType());
                a.setL(createL(property.getResource().getURI()));
                property.setIntegrated(true);
            } else {

                indexEntity = rand.nextInt(availableEntities.size());

                ResourceExp1 entity = availableEntities.get(indexEntity);
                if(integratedR.contains(getIntegratedIRI(entity.getResource().getURI()))){
                    a.setIriA(getIntegratedIRI(entity.getResource().getURI()));
                } else {
                    // todo if(i ==1) i+1
                    a.setIriA(getIRI(entity.getResource().getURI(), iA));
                    integratedR.add(getIntegratedIRI(entity.getResource().getURI()));
                }
                a.setIriB(getIRI(entity.getResource().getURI(), iteration));
                a.setType("class");
                a.setL(createL(entity.getResource().getURI()));

                properties.addAll( entitiesP.get(entity));
                entity.setIntegrated(true);
            }
            alignments.add(a);
        }
        return alignments;
    }

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
        return null;
    }

    public String getIntegratedIRI(String r){
        return Namespaces.NextiaDI.val() + createL(r);
    }

    public String createL(String r) {
        String[] names = r.split("\\.");
        String name = names[names.length-2].replaceAll("_([0-9]+)","").replaceAll("([0-9]+)","") +"_" +names[names.length-1].replaceAll("_([0-9]+)","");
//        name = name;
        return "Integrated_"  +name;
    }

    public String getIRI(String r, int i1) {
        return r.replaceAll("_([0-9]+)", "_" + i1);
    }

    public String getIRI(String r, int i1, Model m) {

        String iri = r.replaceAll("_([0-9]+)", "_" + i1);
        for(int i = 0; !m.containsResource(ResourceFactory.createResource(iri)); ++i) {
            iri = iri.replaceAll("Seq[0-9]*.", "Seq" + i + ".");
        }

//        return r.replaceAll("_([0-9]+)", "_" + i1);
        return iri;
    }

    public void writeAlignments(int number, List<Alignment> alignments) {
        List<String> newDoc = new ArrayList<>();
        newDoc.add("iriA,iriB,Integrated_label,type");

        for (Alignment a : alignments) {
            String newLine = a.getIriA() + "," + a.getIriB() + "," + a.getL() + "," + a.getType();
            newDoc.add(newLine);
        }

        try {
            String name = baseAlignmentFileName +"_I_"+number;
            if(number == 1){
                name = baseAlignmentFileName + "_" + 1 + "_"+2;
            }
            String dir = getDir(alignmentsFolder);
            Path file = Paths.get(dir + "/" + name + ".csv");
            Files.write(file, newDoc, StandardCharsets.UTF_8);

//            FileWriter writer = new FileWriter(dir + "/" + name + ".csv");

//            for (String line : newDoc) {
//                writer.write(line + System.lineSeparator());
//            }
//            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void generateAlignments(int nfiles) {
        try {
            Files.createDirectories(Paths.get(getDir(alignmentsFolder) ));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
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
//            System.out.println("Integrating I and "+i);
//            System.out.println(alignments.size());
//            if(i == 15){
//                alignments.forEach(x -> System.out.println(x.getIriA() +" - "+ x.getIriB()));
//            }
            NextiaDI n = new NextiaDI();
            long startTime = System.nanoTime();
            integrated = n.Integrate(integrated, A, alignments);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000L;
            List<Alignment> unused = n.getUnused();
//            System.out.println("unused: "+unused.size());
            unused.forEach(a -> System.out.println(a.getIriA() +" - "+ a.getIriB()));
            times.add(i + "," + duration);

        }
        times.forEach(System.out::println);
    }
}

package edu.upc.essi.dtim;

import edu.upc.essi.dtim.nextiadi.config.Vocabulary;
import edu.upc.essi.dtim.nextiadi.jena.Graph;
import edu.upc.essi.dtim.nextiadi.models.Alignment;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NextiaDI {

    Graph graphO = new Graph();
    List<Alignment> unused;

    public NextiaDI(){
        unused = new ArrayList<>();
    }

    public Model Integrate(Model graphA, Model graphB, List<Alignment> alignments){

        List<Alignment> classes = alignments.stream().filter( a -> a.getType().contains("class") ).collect(Collectors.toList());
        List<Alignment> datatypes = alignments.stream().filter( a -> a.getType().contains("datatype") ).collect(Collectors.toList());
        List<Alignment> properties = alignments.stream().filter( a -> a.getType().contains("object") ).collect(Collectors.toList());

        return Integrate(graphA, graphB, classes, datatypes, properties, new ArrayList<>());

    }

    public Model Integrate(Model graphA, Model graphB, List<Alignment> alignments, List<Alignment> unused){

        List<Alignment> classes = alignments.stream().filter( a -> a.getType().contains("class") ).collect(Collectors.toList());
        List<Alignment> datatypes = alignments.stream().filter( a -> a.getType().contains("datatype") ).collect(Collectors.toList());
        List<Alignment> properties = alignments.stream().filter( a -> a.getType().contains("object") ).collect(Collectors.toList());

        return  Integrate(graphA, graphB, classes, datatypes, properties, unused);

    }

    public Model Integrate(Model graphA, Model graphB, List<Alignment> Ac, List<Alignment> ADT, List<Alignment> AO, List<Alignment> unused ) {
        this.unused = unused;
        Model integratedGraph = graphA.union(graphB);
        graphO.setModel(integratedGraph);
        graphO.generateMetaModel();
        IntegrateClasses( Ac );
        IntegrateDatatypeProperties(ADT);
        return graphO.getModel();
    }

    public List<Alignment> getUnused(){
        return unused;
    }

    public List<Alignment> IntegrateClasses( List<Alignment> Ac) {

        for (Alignment a : Ac) {

            if( graphO.isIntegratedClass(a.getIriA() ) & graphO.isIntegratedClass(a.getIriB()) ) {
                graphO.replaceIntegratedClass(a);
            } else if ( graphO.isIntegratedClass(a.getIriA()  ) ) {
                graphO.add(a.getIriB(), RDFS.subClassOf.getURI(), a.getIriA());
            } else if ( graphO.isIntegratedClass(a.getIriB() ) ) {
                graphO.add(a.getIriA(), RDFS.subClassOf.getURI(), a.getIriB());
            } else {
                graphO.add(a.getIriL(), RDF.type.getURI(), Vocabulary.IntegrationClass.val());
                graphO.add(a.getIriA(), RDFS.subClassOf.getURI(), a.getIriL());
                graphO.add(a.getIriB(), RDFS.subClassOf.getURI(), a.getIriL());
            }
            unused = performConcordanceProperties( graphO.performConcordanceProperties(a.getIriL(), unused)) ;
        }
        return unused;

    }

    public List<Alignment> performConcordanceProperties(Map<String,List<Alignment>> map  ) {

        List<Alignment> a = map.get("datatype");

        IntegrateDatatypeProperties(a);
        a = map.get("object");
        return IntegrateObjectProperties(a);


    }


    public List<Alignment> IntegrateDatatypeProperties(List<Alignment> ADP) {

        for ( Alignment a: ADP ) {

            String domainA = graphO.getSuperDomainFromProperty(a.getIriA());
            String domainB = graphO.getSuperDomainFromProperty(a.getIriB());

            if( graphO.isIntegratedClass(domainA) & domainA.equals(domainB) ) {

                if( graphO.isIntegratedDatatypeProperty(a.getIriA()) & graphO.isIntegratedDatatypeProperty(a.getIriB()) ) {

                } else if ( graphO.isIntegratedDatatypeProperty(a.getIriA()) ) {
                    graphO.add(a.getIriB(), RDFS.subPropertyOf.getURI(), a.getIriB() );
                } else if ( graphO.isIntegratedDatatypeProperty(a.getIriA()) ) {
                    graphO.add(a.getIriA(), RDFS.subPropertyOf.getURI(), a.getIriB() );
                } else {
                    graphO.add(a.getIriL(), RDF.type.getURI(), Vocabulary.IntegrationDProperty.val());

                    System.out.println(a.getIriL()+" - "+RDF.type.getURI()+" - "+Vocabulary.IntegrationDProperty.val());

                    graphO.add(a.getIriA(), RDFS.subPropertyOf.getURI(), a.getIriL());
                    graphO.add(a.getIriB(), RDFS.subPropertyOf.getURI(), a.getIriL());

                    // TODO: compare the two ranges and choose the more flexible. E.g. xsd:string
                    String range = graphO.getFlexibleRange(a);
                    graphO.add(a.getIriL(), RDFS.range.getURI(), range);
                    graphO.add(a.getIriL(), RDFS.domain.getURI(), domainA);
                }

            } else {
                unused.add(a);
            }

        }
        return unused;
    }

    public List<Alignment> IntegrateObjectProperties(List<Alignment> ADP) {

        for ( Alignment a: ADP ) {

            String domainA = graphO.getSuperDomainFromProperty(a.getIriA());
            String domainB = graphO.getSuperDomainFromProperty(a.getIriB());
            String rangeA = graphO.getSuperRangeFromProperty(a.getIriA());
            String rangeB = graphO.getSuperRangeFromProperty(a.getIriB());

            if( graphO.isIntegratedClass(domainA) & domainA.equals(domainB) & graphO.isIntegratedClass(rangeA) & rangeA.equals(rangeB) ) {

                if( graphO.isIntegratedDatatypeProperty(a.getIriA()) & graphO.isIntegratedDatatypeProperty(a.getIriB()) ) {

                } else if ( graphO.isIntegratedDatatypeProperty(a.getIriA()) ) {
                    graphO.add(a.getIriB(), RDFS.subPropertyOf.getURI(), a.getIriB() );
                } else if ( graphO.isIntegratedDatatypeProperty(a.getIriA()) ) {
                    graphO.add(a.getIriA(), RDFS.subPropertyOf.getURI(), a.getIriB() );
                } else {
                    graphO.add(a.getIriL(), RDF.type.getURI(), Vocabulary.IntegrationDProperty.val());
                    graphO.add(a.getIriA(), RDFS.subPropertyOf.getURI(), a.getIriL());
                    graphO.add(a.getIriB(), RDFS.subPropertyOf.getURI(), a.getIriL());

                    graphO.add(a.getIriL(), RDFS.range.getURI(), rangeA);
                    graphO.add(a.getIriL(), RDFS.domain.getURI(), domainA);
                }
            } else {
                unused.add(a);
            }



        }
        return unused;
    }

    public Model getMinimalGraph(){
        Graph minimalG = new Graph();

        minimalG.setModel(graphO.getModel());

        minimalG.test();
//        minimalG.minimalIDProperties();
        minimalG.minimalIOProperties();
        minimalG.minimalClasses();
minimalG.removeMetaModel();

        return minimalG.getModel();



//        graphO2.setModel(graphO.getModel());




    }




//    public void findProperties(Class<?> concept) {
//        Method[] x = concept.getDeclaredMethods();
//        System.out.println(x.toString());
//
//        Class<?> superclass = concept.getSuperclass();
//
//        Class<?>[] interfaces = concept.getInterfaces();
//        System.out.println(interfaces.toString());
//    }


    public static void main(String[] args) throws FileNotFoundException {
//
//        IntegrationClass in = new IntegrationClass();
//        Class<?> proxy = in.getClass();
//
//        NextiaDI main = new NextiaDI();
//        main.findProperties(proxy);


//        Model m = ModelFactory.createDefaultModel();

//        JenaModel jena = new JenaModel();
//        jena.setModel(m);
//        jena.add("http://javi.com/s1","http://javi.com/p1","http://javi.com/o1" );
//        jena.add("http://javi.com/s1","http://javi.com/p2","http://javi.com/o2" );
//        jena.add("http://javi.com/s2","http://javi.com/p3","http://javi.com/s1" );
//        jena.deleteResource("http://javi.com/s1");
////        jena.addL("http://javi.com/s1","http://javi.com/p1","http://javi.com/o1" );
//
////        m.add(
////                new ResourceImpl("http://javi.com/s1"),
////                new PropertyImpl("http://javi.com/p1"),
////                new ResourceImpl("http://javi.com/o1"));
//
//
//        RDFDataMgr.write(new FileOutputStream("/Users/javierflores/Documents/UPC Projects/nuupdi/src/main/resources/prueba.ttl"), m, Lang.TURTLE);
////        m.write(new FileOutputStream("/Users/javierflores/Documents/UPC Projects/nuupdi/src/main/resources/prueba.ttl"), "RDF/XML-ABBREV");
//
//        System.out.println("hola");

    }
}

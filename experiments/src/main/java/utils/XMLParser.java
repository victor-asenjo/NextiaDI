package utils;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import edu.upc.essi.dtim.nextiadi.jena.Graph;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import utils.pojos.ResourceExp1;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class XMLParser {

//    File file = new File("F:\\XMLFile.xml");
//    Model human;
    Graph g;

    public XMLParser(){
        Model h = RDFDataMgr.loadModel("/Users/javierflores/Documents/upc/projects/NextiaDI/experiments/src/main/resources/anatomy/human.owl") ;
        g = new Graph();
        g.setModel(h);
    }


    public String getType(String resource){
        String s = "none";
        String query = "SELECT DISTINCT ?type WHERE { <"+resource+"> <" + RDF.type.getURI() + "> ?type.}";
        ResultSet res = g.runAQuery(query);
        if (res.hasNext()) {
            QuerySolution x = res.next();
            Resource r = x.get("type").asResource();
            return r.getLocalName().toLowerCase(Locale.ROOT);
        }
        return s;
    }

    public void parse() {
        List<String> ilabels = new ArrayList<>();
        try {
            InputStream in = XMLParser.class.getClassLoader().getResourceAsStream("anatomy/reference.rdf");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(in);
            doc.getDocumentElement().normalize();

            System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
            NodeList nodeList = doc.getElementsByTagName("Cell");
            System.out.println(nodeList.getLength());

            List<String> newDoc = new ArrayList<>();
            newDoc.add("iriA,iriB,Integrated_label,type");
            Model model = ModelFactory.createDefaultModel();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
//                System.out.println("\nNode Name :" + node.getNodeName());
//                System.out.println(node.getNodeType());
//                Node.ELEMENT_NODE
                Element eElement = (Element) node;
//                if(!eElement.getElementsByTagName("relation").item(0).getTextContent().equals("=")) {
//                    System.out.println("tehre ad");
//                }
//                System.out.println(eElement.);
                String iriA = ((Element) eElement.getElementsByTagName("entity1").item(0)).getAttribute("rdf:resource");
                String iriB = ((Element) eElement.getElementsByTagName("entity2").item(0)).getAttribute("rdf:resource");
                String relation =  eElement.getElementsByTagName("relation").item(0).getTextContent();
                if(relation.equals("=")) {
                    String type = getType(iriB);
                    Resource A = model.createResource(iriA);
                    Resource B = model.createResource(iriB);
                    String integratedLabel ="I_"+A.getLocalName()+B.getLocalName();
                    if(ilabels.contains(integratedLabel))
                        System.out.println("label repetied : "+integratedLabel + "for A: "+A.getLocalName() + " for B: " + B.getLocalName());
                    ilabels.add(integratedLabel);
                    newDoc.add(iriA+","+iriB+","+ integratedLabel +","+ type);
                } else {
                    System.out.println("Unhandle relation in alignments");
                }
//                System.out.println("entity1: "+ iriA );
//                System.out.println("entity2: "+ iriB );
//                System.out.println("relation: "+ relation);
//                System.out.println(getType(((Element) eElement.getElementsByTagName("entity2").item(0)).getAttribute("rdf:resource")));
            }


            try {
                Path file = Paths.get("/Users/javierflores/Documents/upc/projects/NextiaDI/experiments/src/main/resources/anatomy/alignments/alignments.csv");
                Files.write(file, newDoc, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                ex.printStackTrace();
            }


                } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void main(String[] args) {
        XMLParser xml = new XMLParser();
        xml.parse();
    }


}

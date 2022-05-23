package others;

import edu.upc.essi.dtim.NextiaDI;
import edu.upc.essi.dtim.nextiadi.models.Alignment;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static utils.Utils.getStatsOWL;
import static utils.Utils.setPrefixes;

public class IntegrationExp3_2 {

    String directory;
    String baseBootstrapFileName = "bootstrap";
    String bootstrapFolder = "bootstrap";
    String alignmentFolder = "alignments";
    String baseAlignmentFileName = "alignments";
    String resultFolder = "results";
    String integrationFolder = "models";

    public IntegrationExp3_2(String directory){
        this.directory = directory;
    }

    public String getDir(String fileName){
        if(directory.endsWith("/") ){
            return directory + fileName;
        }
        return directory + "/"+  fileName;
    }

    public List<Alignment> readAlignments(){
        List<Alignment> alignments = new ArrayList<>();
        try{
            String fileName = baseAlignmentFileName+".csv";
            List<String> lines = Files.readAllLines(Path.of(getDir(alignmentFolder+"/"+fileName)), Charset.defaultCharset());
            lines.remove(0); // remove header
            for (String line : lines) {
                String[] values = line.split(",");
                Alignment a = new Alignment();
                a.setIriA(values[0]);
                a.setIriB(values[1]);
                a.setL(values[2]);
                a.setType(values[3]);
                alignments.add(a);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return alignments;
    }

    public void integrate(int repetition) {
        try {
            Files.createDirectories(Paths.get(getDir(resultFolder+"/"+integrationFolder) ));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
//        HashMap<Integer, List<String>> allTimes = new HashMap<>() ;
        List<String> times = new ArrayList<>();
        Model A = RDFDataMgr.loadModel("/Users/javierflores/Documents/upc/projects/NextiaDI/experiments/src/main/resources/largeBio/FMA.owl") ;
        Model B = RDFDataMgr.loadModel("/Users/javierflores/Documents/upc/projects/NextiaDI/experiments/src/main/resources/largeBio/NCI.owl") ;

        System.out.println("fma: "+getStatsOWL(A));
        System.out.println("nci: " +getStatsOWL(B));

        for(int k = 0; k <= repetition; k++){ // 0 warmup

            times.add("repetition,time(milliseconds),alignments_generated,alignments_unused,numberOfClasses,numberOfIntegratedClasses,numberOfDatatype,numberOfIntegratedDatatype,numberOfObject,numberOfIntegratedObjects");

                    List<Alignment> alignments = readAlignments();
                    System.gc();
                    NextiaDI n = new NextiaDI();
                    long startTime = System.nanoTime();
                    Model integrated = n.Integrate(A, B, alignments);
                    long endTime = System.nanoTime();
                    long duration = (endTime - startTime) / 1000000L;
                    List<Alignment> unused = n.getUnused();
                    Model minimal = setPrefixes(n.getMinimalGraph());
                    if (k!=0)
                        times.add(k + "," + duration+","+alignments.size()+","+unused.size()+ getStatsOWL(integrated));
                    try {
                        if(k==1)
                            writeModels(integrated, minimal, k);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
//            allTimes.put(k, times);
            writeTimes(times);
        }
//        standardizeTimes(allTimes, repetition, nfiles);
    }

    public void standardizeTimes(HashMap<Integer, List<String>> times, int repetitions, int nfiles){

        List<String> newTimes = new ArrayList<>();
        newTimes.add(times.get(1).get(0));
        for (int i = 1; i < nfiles; i++){ // start in 1 because of header
            int duration = 0;
            for (int k = 1; k <= repetitions; k++ ) {
                String[] t = times.get(k).get(i).split(",");
                duration = duration + Integer.parseInt(t[1]);
            }
            String[] t = times.get(1).get(i).split(",");
            duration = duration / repetitions;
            String line = "";
            for (int j = 0; j < t.length;  j++) {
                if(j==1){
                    line = line + "," + duration;
                } else if(line.equals("")) {
                    line = t[j];
                } else {
                    line = line + "," +t[j];
                }
            }
            newTimes.add(line );
        }
        writeTimes(newTimes);

    }

    public void log(int iteration, int nA, int nUnused, long duration) {
        if(iteration == 1){
            System.out.println("----------------- Integrating: 1 and 2  ------------------");
        } else {
            System.out.println("------------- Integrating: I(...) and " + iteration + "  ---------------");
        }
        System.out.println("Number of alignments used: " + nA);
        System.out.println("Number of alignments unused: " + nUnused);
        System.out.println("Duration: " + duration + " milliseconds");
        System.out.println("----------------------------------------------------------");
    }

    public void writeModels(Model integrated, Model minimal, int iteration) throws FileNotFoundException {
        String name = "I_"+iteration;
        if(iteration == 1){
            name =  1 + "_"+2;
        }
        String path = getDir(resultFolder+"/"+integrationFolder +"/");
        int i2 = iteration + 1;
        RDFDataMgr.write(new FileOutputStream(path + "integrated_"+ name+".ttl" ), integrated, Lang.TRIG);
        RDFDataMgr.write(new FileOutputStream(path + "minimal_"+ name+".ttl"), minimal, RDFFormat.TURTLE_PRETTY);
    }

    public void writeTimes(List<String> lines){
        try {
            Path file = Paths.get(getDir(resultFolder+"/times.csv"));
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void writeTimes(List<String> lines, int repetition){
        try {
            Path file = Paths.get(getDir(resultFolder+"/times_"+repetition+".csv"));
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String dir = "/Users/javierflores/Documents/upc/projects/NextiaDI/experiments/src/main/resources/largeBio";
        IntegrationExp3_2 exp = new IntegrationExp3_2(dir);
        exp.integrate(10);

    }

}

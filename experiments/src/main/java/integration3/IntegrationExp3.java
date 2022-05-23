package integration3;

import edu.upc.essi.dtim.NextiaDI;
import edu.upc.essi.dtim.nextiadi.models.Alignment;
import integration1.IntegrationExp1;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import picocli.CommandLine;

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

import static utils.Utils.*;

public class IntegrationExp3 {

    @CommandLine.Option(names = "-o", required = true, description = "Output directory")
    String directory;
    @CommandLine.Option(names = "-d", required = true, description = "Data sources directory")
    String datasourcesDir;
    String baseAlignmentFileName = "alignments";
    String resultFolder = "results";
    String integrationFolder = "models";
    @CommandLine.Option(names = "-t", defaultValue = "anatomy", description = "Anatomy or largeBio experiment")
    String type;
    @CommandLine.Option(names = "-r", defaultValue = "1", description = "Number of repetitions")
    int repetition;

    public IntegrationExp3(){};
    public IntegrationExp3(String directory){
        this.directory = directory;
    }

    public String getDir(String directory, String fileName){


        String folder  = "anatomy";
        if(type.toLowerCase().equals("largebio")){
            folder = "largeBio";
        }
        if(directory.endsWith("/") ){
            return directory + folder+"/"+ fileName;
        }
        return directory + "/"+folder+"/"+  fileName;
    }

    public List<Alignment> readAlignments(){
        List<Alignment> alignments = new ArrayList<>();
        try{
            String fileName = baseAlignmentFileName+".csv";
            List<String> lines = Files.readAllLines(Path.of(getDir(datasourcesDir, fileName)), Charset.defaultCharset());
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

    public void integrate() {
        try {
            Files.createDirectories(Paths.get(getDir(directory,resultFolder+"/"+integrationFolder) ));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
//        HashMap<Integer, List<String>> allTimes = new HashMap<>() ;
        List<String> times = new ArrayList<>();

        String adir = getDir(datasourcesDir, "mouse.owl" );
        String bdir = getDir(datasourcesDir, "human.owl" );
        if(type.toLowerCase().equals("largebio")){
            adir =  getDir(datasourcesDir, "FMA.owl" );
            bdir = getDir(datasourcesDir, "NCI.owl" );
        }
        Model A = RDFDataMgr.loadModel(adir) ;
        Model B = RDFDataMgr.loadModel(bdir) ;

        System.out.println(getStatsOWL(A));
        System.out.println(getStatsOWL(B));

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
        String path = getDir(directory, resultFolder+"/"+integrationFolder +"/");
        int i2 = iteration + 1;
        RDFDataMgr.write(new FileOutputStream(path + "integrated_"+ name+".ttl" ), integrated, Lang.TRIG);
        RDFDataMgr.write(new FileOutputStream(path + "minimal_"+ name+".ttl"), minimal, RDFFormat.TURTLE_PRETTY);
    }

    public void writeTimes(List<String> lines){
        try {
            Path file = Paths.get(getDir( directory,resultFolder+"/times.csv"));
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void writeTimes(List<String> lines, int repetition){
        try {
            Path file = Paths.get(getDir(directory,resultFolder+"/times_"+repetition+".csv"));
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {

        IntegrationExp3 exp = new IntegrationExp3();
        CommandLine cmd = new CommandLine(exp);

        try{ cmd.parseArgs(args);  }
        catch (CommandLine.MissingParameterException e) {
            e.printStackTrace();
            System.out.println("Missing required parameters:");
            cmd.usage(System.out);
            System.exit(0);
        }
        exp.integrate();
//        String dir = "/Users/javierflores/Documents/upc/projects/NextiaDI/experiments/src/main/resources/anatomy";
//        IntegrationExp3 exp = new IntegrationExp3(dir);
//        exp.integrate(10);

    }

}

package bootstrapping;

import edu.upc.essi.dtim.nextiadi.bootstraping.JSONBootstrap;
import edu.upc.essi.dtim.nextiadi.bootstraping.CSVBootstrap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import picocli.CommandLine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static utils.Utils.getDir;

public class BootstrappingExp1 {

    @CommandLine.Option(names = "-f", defaultValue = "bootstrap", description = "Base bootstrap file name")
    String baseBootstrapFileName = "bootstrap";
    String bootstrapFolder = "bootstrap";
    String fileName = "ArtCollection.json";
    @CommandLine.Option(names = "-o", required = true, description = "Output directory")
    String directory;
    @CommandLine.Option(names = "-d", required = true, description = "Data sources directory")
    String datasourcesFolder = "datasources";
    @CommandLine.Option(names = "-n", defaultValue = "100", description = "Number of datasets")
    int nfiles = 100;
    @CommandLine.Option(names = "-r", defaultValue = "1", description = "Number of repetitions")
    int repetition = 1;

    public BootstrappingExp1(){}
    public BootstrappingExp1(String directory) {
        this.directory = directory;
    }

    public String getDirForDataSource(String directory ,int iteration, String type){
        String dir = directory  +"/"+type;
        if(directory.endsWith("/") ){
            dir = directory +type;
        }

        dir = dir+ "/" + fileName.replace(".json", iteration +"."+type);
//        String dir = directory + "/"+ datasourcesFolder+ "/"+fileName.replace(".json", iteration +".json");
//        if(directory.endsWith("/") ){
//            dir = directory + datasourcesFolder+ "/" + fileName.replace(".json", iteration +".json");
//        }
        File file = new File(dir);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dir;
    }

//    public String getDirForDataSource(int iteration, int type){
//        String dir = directory + "/"+ datasourcesFolder+ "/"+fileName.replace(".json", iteration +".json");
//        if(directory.endsWith("/") ){
//            dir = directory + datasourcesFolder+ "/" + fileName.replace(".json", iteration +".json");
//        }
//        File file = new File(dir);
//        try {
//            file.createNewFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return dir;
//    }

//    int nfiles, int repetition
    public void bootstrap(){

        try {
            Files.createDirectories(Paths.get(getDir(directory, bootstrapFolder) ));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }


        HashMap<Integer, List<String>> allTimes = new HashMap<>() ;
        for(int k = 1; k <= repetition; k++) {
            List<String> times = new ArrayList<>();
            times.add("iteration, time(milliseconds),time(seconds),numberOfKeys,timeCSV(milliseconds),timeCSV(seconds),numberOfheaders");
            int nkeys = 100;
            int nheader = 91;
            for (int i = 0; i <= nfiles; i++) { // 0 for warm-up
                int i2 = i == 0? 1: i;
                String path = getDirForDataSource(datasourcesFolder, i2,"json");
                String pathcsv = getDirForDataSource(datasourcesFolder, i2, "csv");
                System.gc();
                try {
                    JSONBootstrap b = new JSONBootstrap();
                    long startTime = System.nanoTime();
                    Model model = b.bootstrapSchema("exp1_" + i2, path);
                    long endTime = System.nanoTime();
                    long duration = (endTime - startTime) / 1000000L;
                    long durationS = (endTime - startTime) / 1000000000L;

                    System.gc();
                    long startTime2 = System.nanoTime();
                    CSVBootstrap c = new CSVBootstrap();
                    Model csv = c.bootstrapSchema("http://www.exp1.com/" + i2+"/", pathcsv);
                    long endTime2 = System.nanoTime();
                    long duration2 = (endTime2 - startTime2) / 1000000L;
                    long durationS2 = (endTime2 - startTime2) / 1000000000L;


                    if (i > 1){
                        nkeys = nkeys + 50;
                        nheader = nheader + 49;
                    }
                    if(i != 0)
                        times.add(i + "," + duration + ","+durationS+"," + nkeys+","+duration2+","+durationS2+","+nheader);

                    if(k==1) {
                        String path2 = getDir(directory, bootstrapFolder + "/json/" + baseBootstrapFileName + i + ".ttl");
                        try {
                            Files.createDirectories(Paths.get(getDir(directory, bootstrapFolder + "/json/"  )));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        RDFDataMgr.write(new FileOutputStream(path2), model, RDFFormat.TURTLE_PRETTY);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            allTimes.put(k, times);
            writeTimes(times, k);
        }
        standardizeTimes(allTimes, repetition, nfiles);
    }

    public void standardizeTimes(HashMap<Integer, List<String>> times, int repetitions, int nfiles){

        List<String> newTimes = new ArrayList<>();
        newTimes.add(times.get(1).get(0));
        for (int i = 1; i<= nfiles; i++){ // start in 1 because of header
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


    public void writeTimes(List<String> lines, int repetition){
        try {
            Files.createDirectories(Paths.get(getDir(directory, "results") ));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            Path file = Paths.get(getDir(directory, "results/times_"+repetition+".csv"));
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void writeTimes(List<String> lines){
        try {
            Path file = Paths.get(getDir(directory, "results/times.csv"));
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }



    public static void main(String[] args) {
        BootstrappingExp1 b = new BootstrappingExp1();
        CommandLine cmd = new CommandLine(b);

        try{ cmd.parseArgs(args);  }
        catch (CommandLine.MissingParameterException e) {
            e.printStackTrace();
            System.out.println("Missing required paramaters:");
            cmd.usage(System.out);
            System.exit(0);
        }
        b.bootstrap();
//        String dir = "/Users/javierflores/Documents/tmp/experiments/bootstrapping/exp1";
//        BootstrappingExp1 b = new BootstrappingExp1(dir);
//        b.bootstrap(100,10);

    }

}

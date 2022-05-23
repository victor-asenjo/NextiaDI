package bootstrapping;

import edu.upc.essi.dtim.nextiadi.bootstraping.CSVBootstrap;
import edu.upc.essi.dtim.nextiadi.bootstraping.JSONBootstrap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static utils.Utils.getDir;

public class BootstrappingExp2 {

    String baseBootstrapFileName = "bootstrap";
    String bootstrapFolder = "bootstrap";
    String fileName = "ArtCollection.json";
    String directory;
    String datasourcesFolder = "datasources";

    public BootstrappingExp2(String directory) {
        this.directory = directory;
    }

    public String getDirForDataSource(int iteration, String type){
        String dir = directory + "/"+ datasourcesFolder +"/"+type;
        if(directory.endsWith("/") ){
            dir = directory + datasourcesFolder +"/" +type;
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

    public String getDirForDataSource(int iteration, int type){
        String dir = directory + "/"+ datasourcesFolder+ "/"+fileName.replace(".json", iteration +".json");
        if(directory.endsWith("/") ){
            dir = directory + datasourcesFolder+ "/" + fileName.replace(".json", iteration +".json");
        }
        File file = new File(dir);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dir;
    }


    public void bootstrap(int nfiles, int repetition){
        try {
            Files.createDirectories(Paths.get(getDir(directory, bootstrapFolder) ));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }


        HashMap<Integer, List<String>> allTimes = new HashMap<>() ;
        for(int k = 1; k <= repetition; k++) {
            List<String> times = new ArrayList<>();
            times.add("iteration, time(milliseconds),time(seconds),sizeJson,timeCSV(milliseconds),timeCSV(seconds),sizeCSV");
//            int nkeys = 100;
//            int nheader = 91;
            for (int i = 0; i <= nfiles; i++) {
                int i2 = i == 0? 1: i;
                String path = getDirForDataSource(i2,"json");
                String pathcsv = getDirForDataSource(i2, "csv");
                System.gc();
                try {
                    JSONBootstrap b = new JSONBootstrap();
                    long startTime = System.nanoTime();
                    Model model = b.bootstrapSchema("exp1_" + i2, path);
                    long endTime = System.nanoTime();
                    long duration = (endTime - startTime) / 1000000L;
                    long durationS = (endTime - startTime) / 1000000000L;
                    String sizeJ = getFileSizeNIO(path);

                    System.gc();
                    long startTime2 = System.nanoTime();
                    CSVBootstrap c = new CSVBootstrap();
                    Model csv = c.bootstrapSchema("http://www.exp1.com/" + i2+"/", pathcsv);
                    long endTime2 = System.nanoTime();
                    long duration2 = (endTime2 - startTime2) / 1000000L;
                    long durationS2 = (endTime2 - startTime2) / 1000000000L;

                    String sizeC = getFileSizeNIO(pathcsv);
                    if(i != 0)
                        times.add(i + "," + duration + ","+durationS+"," + sizeJ+","+duration2+","+durationS2+","+sizeC);

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


    public String getFileSizeNIO(String fileName) {

        Path path = Paths.get(fileName);

        try {

            // size of a file (in bytes)
            long bytes = Files.size(path);
            System.out.println(String.format("%,d bytes", bytes));
            double size_kb = bytes / 1024L;
            System.out.println(String.format("%,d kilobytes", bytes / 1024));

            double size_mb = size_kb / 1024L;
            System.out.println(size_mb +" mb");
            return size_mb+"";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "0";
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
        String dir = "/Users/javierflores/Documents/tmp/experiments/bootstrapping/exp2";
        BootstrappingExp2 b = new BootstrappingExp2(dir);

        b.bootstrap(15,10);

    }

}

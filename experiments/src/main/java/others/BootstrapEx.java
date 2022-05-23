package others;

import edu.upc.essi.dtim.nextiadi.bootstraping.JSONBootstrap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class BootstrapEx {

    String baseIRI = "http://experiment1.com/";
    String baseFilename = "ArtCollection";

    public List<String> listFilesForFolder(File folder) {
        List<String> files = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (!fileEntry.isDirectory()) {
//                listFilesForFolder(fileEntry);
                files.add(fileEntry.getAbsolutePath());
            }
        }
        return files;
    }

    public String getOutDir(String dir, String fileName){
        if(dir.endsWith("/") ){
            return dir + fileName;
        }
        return dir + "/"+ fileName;
    }

    public void bootstrapFiles(){
//        File folder = new File("/Users/javierflores/Documents/upc/projects/NextiaDI/experiments/src/main/resources/output/integration");
//        List<String> files = listFilesForFolder(folder);
        int n = 60;
        String out = "/Users/javierflores/Documents/upc/projects/NextiaDI/experiments/src/main/resources/output/integration";
        for(int i = 1; i <= n; i++){

//            System.out.println();
            String path = getOutDir(out, baseFilename + i+".json");
            String iri = this.baseIRI + i + "/";

            try {
                JSONBootstrap b = new JSONBootstrap();
                Model model = b.bootstrapSchema("exp1_"+i, path);
                String path2 = out + "/" + baseFilename + i +".ttl";
                RDFDataMgr.write(new FileOutputStream(path2), model, RDFFormat.TURTLE_PRETTY);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            System.out.println(iri);
        }


    }

    public static void main(String[] args) {
        BootstrapEx b = new BootstrapEx();
        b.bootstrapFiles();
    }


}

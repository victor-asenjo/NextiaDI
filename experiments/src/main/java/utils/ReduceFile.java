package utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static utils.Utils.getDir;

public class ReduceFile {

    String dir = "/Users/javierflores/Documents/tmp/experiments/bootstrapping/exp1/datasources/";
    String output = "";
    String basefile = "";
    int n = 100;

    public void reduce() throws IOException {

        for (int i = 1; i <= n; i++ ) {
            String name = "ArtCollection" + i+".csv";
            URL resource = new URL("file:///"+dir +"csv2/"+ name);
            List<String> lines = Files.readAllLines(Path.of(resource.getPath()), Charset.defaultCharset());
//            lines.remove(0); // remove header
            try {
                Path file = Paths.get(dir+"csv/"+name);
                Files.write(file, lines.subList(0,40), StandardCharsets.UTF_8);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }

    }

    public static void main(String[] args) throws IOException {


        ReduceFile r = new ReduceFile();
        r.reduce();
    }
}

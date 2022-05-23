package utils;

import com.devskiller.jfairy.Fairy;
import com.devskiller.jfairy.producer.company.Company;
import com.devskiller.jfairy.producer.company.CompanyProperties;
import com.github.javafaker.*;
import com.github.opendevl.JFlat;
import edu.upc.essi.dtim.nextiadi.bootstraping.JSONBootstrap;
import exceptions.NotValidDirectoryException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.json.JSONObject;
import picocli.CommandLine;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;
import utils.pojos.*;
import utils.pojos.Artist;
import utils.pojos.Book;
import utils.pojos.Color;
import utils.pojos.Educator;
import utils.pojos.Nation;
import utils.pojos.University;

import java.io.*;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static utils.Utils.getDir;

public class DatasetGenerator {

    @CommandLine.Option(names = "-o", required = true, description = "Output directory")
    String directory;
    @CommandLine.Option(names = "-f", defaultValue="ArtCollection.json", description = "Base file name for generated dataset")
    String fileName;
    @CommandLine.Option(names = "-n", defaultValue="1", description = "Number of datasets")
    Integer numberOfDs;
    @CommandLine.Option(names = "-e", defaultValue="integration1", description = "experiment type")
    String experimentType;


    String baseBootstrapFileName = "bootstrap";
    String bootstrapFolder = "bootstrap";
    String datasourcesFolder = "datasources";

    String resourcesFolder = "datasetGenerator";
    String museumsFile = "museums.csv";
    String artworksFile = "artworks.csv";
    String artistFile = "artists.csv";

    List<Museums> museums = new ArrayList<>();
    List<Artworks> works = new ArrayList<>();
    List<Artist> artists = new ArrayList<>();
    List<Exhibitions> exhibitions = new ArrayList<>();
    List<University> universities = new ArrayList<>();

    List<Book> books = new ArrayList<>();
    List<Educator> educators = new ArrayList<>();
    List<Color> colors = new ArrayList<>();
    List<Nation> nations = new ArrayList<>();



    public DatasetGenerator(){

    }

    public DatasetGenerator(String directory, String fileName, Integer numberOfDatasets) throws NotValidDirectoryException {
        this.directory = directory;
        this.fileName = fileName;
        this.numberOfDs = numberOfDatasets;

    }


    public String getDirForDataSource(int iteration){
        String dir = directory + "/"+ datasourcesFolder+ "/"+fileName.replace(".json", iteration +".json");
        if(directory.endsWith("/") ){
            dir = directory + datasourcesFolder+ "/" + fileName.replace(".json", iteration +".json");
        }
//        System.out.println(dir);
        File file = new File(dir);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dir;
    }

    public String getDirForDataSource(int iteration, String type){
        String dir = directory + "/"+ datasourcesFolder +"/"+type;
        if(directory.endsWith("/") ){
            dir = directory + datasourcesFolder +"/" +type;
        }

        try {
            Files.createDirectories(Paths.get(dir ));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        dir = dir+ "/" + fileName.replace(".json", iteration +"."+type);
//        System.out.println(dir);
        File file = new File(dir);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dir;
    }

    public String getDir(String fileName){
        if(directory.endsWith("/") ){
            return directory + fileName;
        }
        return directory + "/"+  fileName;
    }


    public void bootstrapFiles(int n){
        try {
            Files.createDirectories(Paths.get(getDir(bootstrapFolder) ));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        for(int i = 1; i <= n; i++){
            String path = getDirForDataSource(i);

            try {
                JSONBootstrap b = new JSONBootstrap();
                Model model = b.bootstrapSchema("exp1_"+i, path);
                String path2 = getDir( bootstrapFolder + "/" + baseBootstrapFileName + i+".ttl");
                RDFDataMgr.write(new FileOutputStream(path2), model, RDFFormat.TURTLE_PRETTY);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }


    }


    public void duplicateFiles(int iteration){
        try {
            Files.createDirectories(Paths.get(getDir(datasourcesFolder) ));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String json = generateBaseFile();
        for(int i = 1; i <= iteration; ++i) {
            duplicate(json, i);
        }

    }

    public String incrementFields(Field[] attributes, String line, int number){
        String newL = line;
        for (Field a : attributes) {
            String key = "\"" + a.getName() + "\":";
            newL = newL.replace(key, "\"" + a.getName() + "_" + number + "\":");
        }
        return newL;
    }

    public void duplicate(String json, int iteration){

        Field[] attributes = Artist.class.getDeclaredFields();
        String newJSON = this.incrementFields(attributes, json, iteration);
        attributes = Artworks.class.getDeclaredFields();
        newJSON = this.incrementFields(attributes, newJSON, iteration);
        attributes = Museums.class.getDeclaredFields();
        newJSON = this.incrementFields(attributes, newJSON, iteration);
        attributes = Place.class.getDeclaredFields();
        newJSON = this.incrementFields(attributes, newJSON, iteration);
        attributes = JsonFile.class.getDeclaredFields();
        newJSON = this.incrementFields(attributes, newJSON, iteration);

        try {

            PrintWriter writer = new PrintWriter(getDirForDataSource(iteration));
            writer.print(newJSON);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public String generateBaseFile() {
        readMuseums();
        readArtworks();
        readArtist();
        loadExhibitions();
        loadUniversities();
        loadBooks();
        loadColors();
        loadEducator();
        loadNations();
        System.out.println("Museums: "+museums.size());
        System.out.println("Artworks: "+works.size());
        System.out.println("Artist: "+artists.size());
        JsonFile file = new JsonFile();
        file.setArtists(artists.subList(0, 20));
        file.setMuseums(museums.subList(0, 20));
        file.setArtworks(works.subList(0, 20));
        file.setExhibitions(exhibitions.subList(0,20));
        file.setBooks(books.subList(0,20));
        file.setEducators(educators.subList(0,20));
        file.setColors(colors.subList(0,20));
        file.setNations(nations.subList(0,20));
        JSONObject jsonObj = new JSONObject(file);

        return jsonObj.toString();
    }

    public String generateBaseFile2() {
        readMuseums();
        readArtworks();
        readArtist();
        loadExhibitions();
        loadUniversities();
        loadBooks();
        loadColors();
        loadEducator();
        loadNations();
        System.out.println("Museums: "+museums.size());
        System.out.println("Artworks: "+works.size());
        System.out.println("Artist: "+artists.size());
        JsonFile file = new JsonFile();
        file.setArtists(artists.subList(0,artists.size()/6));
        file.setMuseums(museums.subList(0,artists.size()/6));
        file.setArtworks(works.subList(0,artists.size()/6));
        file.setExhibitions(exhibitions);
        file.setBooks(books);
        file.setEducators(educators);
        file.setColors(colors);
        file.setNations(nations);
        JSONObject jsonObj = new JSONObject(file);

        return jsonObj.toString();
    }

    public String generateBaseFileExpBootstrapping(int i) {
        // generates an initial file of 24mb
        readMuseums();
        readArtworks();
        readArtist();
        loadExhibitions();
        loadUniversities();
        loadBooks();
        loadColors();
        loadEducator();
        loadNations();
        JsonFile file = new JsonFile();

        if(i == 1) {
            file.setArtists( Collections.nCopies(i,artists.subList(0,artists.size()/6)).stream().flatMap(List::stream).collect(Collectors.toList()) );
            file.setMuseums(Collections.nCopies(i,museums.subList(0,museums.size()/6)).stream().flatMap(List::stream).collect(Collectors.toList()) );
            file.setArtworks( Collections.nCopies(i,works.subList(0,works.size()/6)).stream().flatMap(List::stream).collect(Collectors.toList()) );
        } else {
            file.setArtists( Collections.nCopies(i,artists.subList(0,artists.size()/10)).stream().flatMap(List::stream).collect(Collectors.toList()) );
            file.setMuseums(Collections.nCopies(i,museums.subList(0,museums.size()/10)).stream().flatMap(List::stream).collect(Collectors.toList()) );
            file.setArtworks( Collections.nCopies(i,works.subList(0,works.size()/10)).stream().flatMap(List::stream).collect(Collectors.toList()) );
        }
        file.setExhibitions( Collections.nCopies(i,exhibitions).stream().flatMap(List::stream).collect(Collectors.toList()) );
        file.setBooks( Collections.nCopies(i,books).stream().flatMap(List::stream).collect(Collectors.toList()) );
        file.setEducators( Collections.nCopies(i,educators).stream().flatMap(List::stream).collect(Collectors.toList()) );
        file.setColors( Collections.nCopies(i, colors).stream().flatMap(List::stream).collect(Collectors.toList()) );
        file.setNations( Collections.nCopies(i,nations).stream().flatMap(List::stream).collect(Collectors.toList()) );
        JSONObject jsonObj = new JSONObject(file);

        return jsonObj.toString();
    }

    public String pathR(String fileName){
        return "/"+resourcesFolder + "/" + fileName;
    }

    public void loadExhibitions(){

        for(int i=0; i<50; i++){
            PodamFactory factory = new PodamFactoryImpl();
            Exhibitions ex = factory.manufacturePojo(Exhibitions.class);
            this.exhibitions.add(ex);
        }
    }

    public void loadUniversities(){

        for(int i=0; i<50; i++){
            Faker faker = new Faker();
            University u = new University();
            u.setName(faker.university().name());
            u.setPrefix(faker.university().prefix());
            u.setSuffix(faker.university().suffix());
            u.setAddress(faker.address().fullAddress());
            universities.add(u);
        }
    }

    public void loadBooks(){

        for(int i=0; i<50; i++){
            Faker faker = new Faker();
            Book b = new Book();
            b.setAuthor(faker.book().author());
            b.setGenre(faker.book().genre());
            b.setPublisher(faker.book().publisher());
            b.setTitle(faker.book().title());
            books.add(b);
        }
    }

    public void loadEducator(){

        for(int i=0; i<50; i++){
            Faker faker = new Faker();
            Educator e = new Educator();
            e.setUniversityName(faker.educator().university());
            e.setCampus(faker.educator().campus());
            e.setCourse(faker.educator().course());
            e.setName(faker.name().fullName());
            educators.add(e);
        }
    }

    public void loadColors(){

        for(int i=0; i<50; i++){
            Faker faker = new Faker();
            Color c = new Color();
            c.setColorName(faker.color().name());
            c.setHexCode(faker.color().hex());
            colors.add(c);
        }
    }

    public void loadNations(){

        for(int i=0; i<50; i++){
            Faker faker = new Faker();
            Nation n = new Nation();
            n.setFlag(faker.nation().flag());
            n.setNationName(faker.nation().nationality());
            n.setCapitalCity(faker.nation().capitalCity());
            n.setLanguage(faker.nation().language());

            nations.add(n);
        }
    }

    public void readMuseums() {
        try {
            URL resource = this.getClass().getResource(pathR(museumsFile));
            List<String> lines = Files.readAllLines(Path.of(resource.getPath()), Charset.defaultCharset());
            lines.remove(0); // remove header
            for (String line : lines) {
                String[] values = line.split(",");
                if (values.length == 25) {
                    Museums m = new Museums();
                    Place p = new Place();
                    m.setMuseumID(values[0]);
                    m.setName(values[1]);
                    m.setLegalName(values[2]);
                    m.setAlternateName(values[3]);
                    m.setMuseumType(values[4]);
                    m.setInstitutionName(values[5]);
                    p.setStreetAddress(values[6]);
                    p.setCity(values[7]);
                    p.setState(values[8]);
                    p.setZipCode(values[9]);
                    p.setStreetAddressPhysical(values[10]);
                    p.setCityPhysical(values[11]);
                    p.setStatePhysical(values[12]);
                    p.setZipCodePhysical(values[13]);
                    m.setPhoneNumber(values[14]);
                    p.setLatitude(values[15]);
                    p.setLongitude(values[16]);
                    p.setLocaleCode(values[17]);
                    p.setCountryCode(values[18]);
                    p.setStateCode(values[19]);
                    p.setRegionCode(values[20]);
                    p.setAddress("add");
                    m.setEmployerID(values[21]);
                    m.setTaxPeriod(values[22]);
                    m.setIncome(values[23]);
                    m.setRevenue(values[24]);
                    String placeID = UUID.randomUUID().toString();
                    p.setPlaceID(placeID);
                    m.setPlace(p);
                    this.museums.add(m);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readArtworks() {
        try {
            URL resource = this.getClass().getResource(pathR(artworksFile));
            List<String> lines = Files.readAllLines(Path.of(resource.getPath()), Charset.defaultCharset());
            lines.remove(0); // remove header
            for (String line : lines) {
                String[] values = line.split(",");
                if (values.length == 21) {
                    works.add(new Artworks(values));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void readArtist() {

        try{
            URL resource = this.getClass().getResource(pathR(artistFile));
            List<String> lines = Files.readAllLines(Path.of(resource.getPath()), Charset.defaultCharset());
            lines.remove(0); // remove header
            for (String line : lines) {
                String[] values = line.split(",");
                if (values.length == 6) {
                    artists.add(new Artist(values));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public JSONObject growFileForIntegration( JSONObject o, int numberObj, int numberKeys, int starter ){

        for(int k = 0; k < numberObj; ++k) {
            JSONObject obj = new JSONObject();

            for(int i = 0; i < numberKeys; ++i) {
//                Fairy fairy = Fairy.create();
                Faker faker = new Faker();

//                Company c = fairy.company(new CompanyProperties.CompanyProperty[0]);
//                obj.put("y" + k + "_" + i, c.getVatIdentificationNumber());
//                obj.put("y" + k + "and" + i,faker.lorem().paragraph() );
                obj.put("y" + k + "and" + i,faker.business().creditCardNumber() );
            }

            o.put("object" + starter, obj);
            ++starter;
        }

        return o;
    }

    public JSONObject growFileForBootstrapping( JSONObject o, int numberObj, int numberKeys, int starter ){

        for(int k = 0; k < numberObj; ++k) {
            JSONObject obj = new JSONObject();

            for(int i = 0; i < numberKeys; ++i) {
//                Fairy fairy = Fairy.create();
                Faker faker = new Faker();

//                Company c = fairy.company(new CompanyProperties.CompanyProperty[0]);
//                obj.put("y" + k + "_" + i, c.getVatIdentificationNumber());
//                obj.put("y" + k + "and" + i,faker.lorem().paragraph() );
                obj.put("y" + k + "and" + i,faker.business().creditCardNumber() );
            }
            o.put("object" + starter, obj);
            ++starter;
        }

        return o;
    }

    public void exp1(int n){
        duplicateFiles(n);
        bootstrapFiles(n);
        AlignmentGenerator a  = new AlignmentGenerator(directory, bootstrapFolder, baseBootstrapFileName);
        a.generateAlignments(n);
    }

    public void exp2(int numberOfDs, int numberOfKeys) {
        try {
            Files.createDirectories(Paths.get(getDir(datasourcesFolder) ));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String jsonStr = generateBaseFile();
        JSONObject json = new JSONObject(jsonStr);

        for(int i = 1; i <= numberOfDs; ++i) {
            if(i != 1)
                json = growFileForIntegration(json, 1, numberOfKeys, i);
            try {
                PrintWriter writer = new PrintWriter(getDirForDataSource(i));
                writer.print(json.toString());
                writer.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        bootstrapFiles(numberOfDs);
        AlignmentGenerator a  = new AlignmentGenerator(directory, bootstrapFolder, baseBootstrapFileName);
        a.generateAlignmentsExp2(numberOfDs);
    }

    public void expBootstrap1(int numberOfDs, int numberOfKeys) {
        try {
            Files.createDirectories(Paths.get(getDir(datasourcesFolder) ));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String jsonStr = generateBaseFile2();
        JSONObject json = new JSONObject(jsonStr);

        for(int i = 1; i <= numberOfDs; ++i) {
            if(i != 1)
                json = growFileForIntegration(json, 1, numberOfKeys, i);
            try {

                Path file = Paths.get(getDirForDataSource(i, "json"));
                List<String> lines = new ArrayList<>();
                lines.add(json.toString());
                Files.write(file, lines, StandardCharsets.UTF_8);


                JSONObject json2 = new JSONObject(json.toString());
                JFlat flatMe = new JFlat(json2.put("id"+i, "lorem ipsum").toString());
                flatMe.json2Sheet().headerSeparator("_").write2csv(getDirForDataSource(i, "csv"), ',');

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        bootstrapFiles(numberOfDs);
    }

    public void expBootstrap2(int numberOfDs) {
        try {
            Files.createDirectories(Paths.get(getDir(datasourcesFolder) ));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for(int i = 13; i <= numberOfDs; ++i) {
            String jsonStr = generateBaseFileExpBootstrapping(i);
            try {
                System.gc();
                PrintWriter writer = new PrintWriter(getDirForDataSource(i, "json"));
                writer.print( jsonStr);
                writer.close();

                System.gc();
                JSONObject json2 = new JSONObject(jsonStr);
                JFlat flatMe = new JFlat(json2.put("id"+i, "lorem ipsum").toString());
                flatMe.json2Sheet().headerSeparator("_").write2csv(getDirForDataSource(i, "csv"), ',');

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        bootstrapFiles(numberOfDs);
    }


    public void run() {

        switch (experimentType){

            case "integration1":
                exp1(100);
                break;
            case "integration2":
                exp2(100,50);
                break;
            case "bootstrapping1":
                // 1 object with 49 keys -> in total 50 keys
                expBootstrap1(100,49);
                break;
            case "bootstrapping2":
                expBootstrap2(100);
                break;
            default:
                System.out.println("No experiment type");
        }
    }
    public static void main(String[] args) {
        DatasetGenerator d = new DatasetGenerator();
        CommandLine cmd = new CommandLine(d);

        for (String arg : args) {
            System.out.println("sf " +arg);
        }
        try{ cmd.parseArgs(args);  }
        catch (CommandLine.MissingParameterException e) {
            e.printStackTrace();
            System.out.println("Missing required paramaters:");
            cmd.usage(System.out);
            System.exit(0);
        }
        d.run();
//        d.exp1(100);
//        d.exp2(100,50);

//        d.expBootstrap1(100,50);
//        d.expBootstrap2(100);
    }

}

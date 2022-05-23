package utils.pojos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class Artworks {
    String artworkID;
    String title;
    String artistIdentifier;
    String name;
    String date;
    String medium;
    String dimensions;
    String acquisitionDate;
    String credit;
    String catalogue;
    String department;
    String classification;
    String objectNumber;
    String diameter;
    String circumference;
    String height;
    String length;
    String width;
    String depth;
    String weight;
    String duration;
    String museumIdentifier;


    public Artworks(String[] data ){
        setArtworkID(data[0]);
        setTitle(data[1]);
        setArtistIdentifier(data[2]);
        setName(data[3]);
        setDate(data[4]);
        setMedium(data[5]);
        setDimensions(data[6]);
        setAcquisitionDate(data[7]);
        setCredit(data[8]);
        setCatalogue(data[9]);
        setDepartment(data[10]);
        setClassification(data[11]);
        setObjectNumber(data[12]);
        setDiameter(data[13]);
        setCircumference(data[14]);
        setHeight(data[15]);
        setLength(data[16]);
        setWidth(data[17]);
        setDepth(data[18]);
        setWeight(data[19]);
        setDuration(data[20]);
        String museumID = UUID.randomUUID().toString();
        setMuseumIdentifier(museumID);
    }

}

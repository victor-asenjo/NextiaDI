package utils.pojos;

import lombok.Data;

import java.util.List;

@Data
public class JsonFile {

    List<Museums> museums;
    List<Artist> artists;
    List<Artworks> artworks;
    List<Exhibitions> exhibitions;
    List<University> universities;
    List<Book> books;
    List<Educator> educators;
    List<Color> colors;
    List<Nation> nations;

}

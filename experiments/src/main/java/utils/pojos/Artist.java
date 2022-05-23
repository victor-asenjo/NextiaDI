package utils.pojos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Random;

@Data
@NoArgsConstructor
public class Artist {

    String artistID;
    String name;
    String nationality;
    String gender;
    String birthYear;
    String deathYear;
    String biography;
    String biography75Words;
    String biography50Words;
    String numberOfObjects;
    String supersedes;
    String supersededBy;
    String profileURL;

    public Artist(String[] data){
        setArtistID(data[0]);
        setName(data[1]);
        setNationality(data[2]);
        setGender(data[3]);
        setBirthYear(data[4]);
        setDeathYear(data[5]);
        setBiography("Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit...");
        setBiography50Words("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut at blandit justo, vitae ultricies felis. Fusce lacinia sodales risus, bibendum pulvinar erat gravida sit amet. Vivamus in felis tincidunt, cursus leo vel, sodales nisl. Sed porta purus metus, ac eleifend arcu posuere et. Sed auctor feugiat lectus. Phasellus luctus tincidunt.");
        setBiography75Words("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur ornare, metus et elementum mattis, odio eros vehicula purus, nec tincidunt risus nisi vel nibh. Pellentesque at purus consectetur, consequat enim sed, tristique ligula. Pellentesque quis leo a nunc iaculis vehicula varius vel ante. Curabitur molestie quis felis sit amet rutrum. Nulla facilisi. Suspendisse et hendrerit odio. Nulla sollicitudin sodales felis a porttitor. Nunc lorem dui, lobortis et augue eget, volutpat commodo purus. Phasellus tincidunt a.");
        setNumberOfObjects(Math.random()+"");
        setSupersedes("na");
        setSupersededBy("na");
        setProfileURL("www.somewhere.com");
    }

}

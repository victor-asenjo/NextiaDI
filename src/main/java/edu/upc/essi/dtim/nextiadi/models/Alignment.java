package edu.upc.essi.dtim.nextiadi.models;

import edu.upc.essi.dtim.nextiadi.config.Namespaces;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Alignment {

    String iriA;
    String iriB;
    String l;
    String type;
//    String score?

    public Alignment(){

    }

    public Alignment(String iriA, String iriB, String l) {
        this.iriA = iriA;
        this.iriB = iriB;
        this.l = l;
    }

    public Alignment(String iriA, String iriB, String l, String type) {
        this.iriA = iriA;
        this.iriB = iriB;
        this.l = l;
        this.type = type;
    }


    public String getIriL() {
        return Namespaces.G.val() + l;
    }
}

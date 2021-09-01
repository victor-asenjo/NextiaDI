package edu.upc.essi.dtim.nextiadi.config;

public enum Namespaces {

    G("http://www.essi.upc.edu/dtim/ontology/Global/");

    private String element;

    Namespaces(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }

}

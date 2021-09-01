package edu.upc.essi.dtim.nextiadi.config;


public enum Vocabulary {

    IntegrationClass(Namespaces.G.val() + "IntegrationClass"),
    IntegrationDProperty( Namespaces.G.val() + "IntegrationDProperty"),
    IntegrationOProperty( Namespaces.G.val() + "IntegrationOProperty");

    private String element;

    Vocabulary(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }



}

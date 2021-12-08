package edu.upc.essi.dtim.nextiadi.config;


public enum Vocabulary {

    IntegrationClass(Namespaces.NextiaDI.val() + "IntegrationClass"),
    IntegrationDProperty( Namespaces.NextiaDI.val() + "IntegrationDProperty"),
    IntegrationOProperty( Namespaces.NextiaDI.val() + "IntegrationOProperty");

    private String element;

    Vocabulary(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }



}

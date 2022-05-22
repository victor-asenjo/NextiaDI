package edu.upc.essi.dtim.nextiadi.config;


public enum Vocabulary {


    IntegrationClass(Namespaces.NextiaDI.val() + "IntegratedClass"),
    IntegrationDProperty( Namespaces.NextiaDI.val() + "IntegratedDatatypeProperty"),
    IntegrationOProperty( Namespaces.NextiaDI.val() + "IntegratedObjectProperty");

    private String element;

    Vocabulary(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }



}

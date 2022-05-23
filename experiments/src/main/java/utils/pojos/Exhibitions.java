package utils.pojos;

import lombok.Data;

@Data
public class Exhibitions {

//    inspired by https://github.com/cooperhewitt/collection/blob/master/exhibitions/136/253/035/136253035.json
    String exhibitionsID;
    String url;
    String title;
    String museumID;
    String type;
    String notes;
    String description;
    String shortDescription;
    String startDate;
    String endDate;
    String isActive;
    String countObjects;
    String countObjectsPublic;
    String departmentID;
}

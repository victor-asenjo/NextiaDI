package utils.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.jena.rdf.model.Resource;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceExp1 {

    Resource resource;
    String type;
    Boolean integrated = false;
    Set<Integer> integratedI;
    String domain;
}

/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.pat.transferdata.point;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Point {

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String index;

    @XmlAttribute
    private String region;

    @XmlAttribute
    private String area;

    @XmlAttribute
    private String city;

    @XmlAttribute
    private String cityName;

    @XmlAttribute
    private String street;

    @XmlAttribute
    private String house;

    @XmlAttribute
    private String building;

    @XmlAttribute
    private String flat;
}

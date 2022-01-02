package entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "product")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String name;
    private Date timeCreated;

    @OneToMany(mappedBy = "product")
    private List<Price> prices;
    private Integer firstPrice;
    private Boolean status;
}

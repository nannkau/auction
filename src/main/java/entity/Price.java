package entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
@Entity
@Table(name = "price")
@Data
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private Integer value;
    private Date timeCreated;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}

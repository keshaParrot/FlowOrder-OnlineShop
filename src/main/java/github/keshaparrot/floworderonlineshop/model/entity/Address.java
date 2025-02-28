package github.keshaparrot.floworderonlineshop.model.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Address
{
    private String street;
    private String city;
    private String postalCode;
    private String country;
}

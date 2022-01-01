package dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UserForm implements Serializable {
    private String username;
    private String password;
}

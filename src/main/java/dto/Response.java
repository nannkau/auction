package dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Response  implements Serializable {
    private Integer statusCode;
    private String data;
    private String error;
}

package dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Request <T> implements Serializable {
    private Action action;
    private T data;
}

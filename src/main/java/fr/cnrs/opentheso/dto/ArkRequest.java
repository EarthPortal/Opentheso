package fr.cnrs.opentheso.dto;

import lombok.Data;

@Data
public class ArkRequest {

    private String ark;
    private String naan;
    private String type;
    private String urlTarget;
    private String title;
    private String creator;
}

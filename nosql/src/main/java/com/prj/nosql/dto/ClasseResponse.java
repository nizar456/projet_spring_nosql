package com.prj.nosql.dto;

import com.prj.nosql.model.Classe;
import lombok.Data;

import java.util.List;

@Data
public class ClasseResponse {
    private String id;
    private String nom;
    private Classe.Niveau niveau;
    private List<EtudiantSimpleResponse> etudiants;
}

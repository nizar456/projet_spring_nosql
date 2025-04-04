package com.prj.nosql.dto;

import com.prj.nosql.model.Classe.Niveau;
import lombok.Data;

import java.util.List;

@Data
public class ClasseCreateRequest {
    private String nom;
    private Niveau niveau;
}


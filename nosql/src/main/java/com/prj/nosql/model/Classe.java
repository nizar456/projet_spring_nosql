package com.prj.nosql.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "classes")
public class Classe {
    @Id
    private String id;
    private String nom;
    private Niveau niveau;
    private List<String> etudiantIds = new ArrayList<>();

    public enum Niveau {
        PREMIERE_ANNEE,
        DEUXIEME_ANNEE,
        TROISIEME_ANNEE,
        QUATRIEME_ANNEE,
        CINQUIEME_ANNEE
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Niveau getNiveau() {
        return niveau;
    }

    public void setNiveau(Niveau niveau) {
        this.niveau = niveau;
    }

    public List<String> getEtudiantIds() {
        return etudiantIds;
    }

    public void setEtudiantIds(List<String> etudiantIds) {
        this.etudiantIds = etudiantIds;
    }
}
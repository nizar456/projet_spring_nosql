package com.prj.nosql.service;

import com.prj.nosql.dto.*;
import com.prj.nosql.model.Classe;
import com.prj.nosql.model.User;
import com.prj.nosql.model.UserRole;
import com.prj.nosql.repository.ClasseRepository;
import com.prj.nosql.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClasseService {

    @Autowired
    private ClasseRepository classeRepository;

    @Autowired
    private UserRepository userRepository;

    public ClasseResponse createClasse(ClasseCreateRequest request) {
        Classe classe = new Classe();
        classe.setNom(request.getNom());
        classe.setNiveau(request.getNiveau());
        classe.setEtudiantIds(new ArrayList<>()); // Initialisation explicite
        Classe savedClasse = classeRepository.save(classe);
        return convertToResponse(savedClasse);
    }

    public List<ClasseResponse> getAllClasses() {
        return classeRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public ClasseResponse getClasseById(String id) {
        return classeRepository.findById(id)
                .map(this::convertToResponse)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
    }

    public ClasseResponse updateClasse(String id, ClasseCreateRequest request) {
        Classe classe = classeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
        classe.setNom(request.getNom());
        classe.setNiveau(request.getNiveau());
        Classe updated = classeRepository.save(classe);
        return convertToResponse(updated);
    }

    public void deleteClasse(String id) {
        classeRepository.deleteById(id);
    }

    public ClasseResponse affecterEtudiant(AffectationEtudiantRequest request) {
        Classe classe = classeRepository.findById(request.getClasseId())
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));

        User etudiant = userRepository.findById(request.getEtudiantId())
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));

        if (!etudiant.getRole().equals(UserRole.STUDENT)) {
            throw new RuntimeException("Seuls les étudiants peuvent être affectés à une classe");
        }

        if (!classe.getEtudiantIds().contains(etudiant.getId())) {
            classe.getEtudiantIds().add(etudiant.getId());
            classeRepository.save(classe);
        }

        return convertToResponse(classe);
    }

    private ClasseResponse convertToResponse(Classe classe) {
        ClasseResponse response = new ClasseResponse();
        response.setId(classe.getId());
        response.setNom(classe.getNom());
        response.setNiveau(classe.getNiveau());

        // Initialiser une liste vide si etudiantIds est null
        List<String> etudiantIds = classe.getEtudiantIds() != null ?
                classe.getEtudiantIds() : Collections.emptyList();

        response.setEtudiants(etudiantIds.stream()
                .map(userRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(user -> {
                    EtudiantSimpleResponse esr = new EtudiantSimpleResponse();
                    esr.setId(user.getId());
                    esr.setNom(user.getNom());
                    esr.setPrenom(user.getPrenom());
                    return esr;
                })
                .collect(Collectors.toList()));

        return response;
    }
}
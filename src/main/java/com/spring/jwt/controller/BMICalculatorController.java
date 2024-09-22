package com.spring.jwt.controller;

import com.spring.jwt.BMIRecord.BMIRecord;
import com.spring.jwt.BMIRecord.BMIRecordRepository;
import com.spring.jwt.BMIRecord.BMIRequest;
import com.spring.jwt.entity.LoginRequest;
import com.spring.jwt.repository.UserRespository;
import com.spring.jwt.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bmi")
@RequiredArgsConstructor
public class BMICalculatorController {

    private final BMIRecordRepository bmiRecordRepository;
    private final UserRespository userRespository;

    @PostMapping("/calculate")
    public ResponseEntity<BMIRecord> calculateAndSaveBMI(@RequestBody BMIRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User currentUser = (User) authentication.getPrincipal();

        // Calcul du BMI
        double bmi = request.getWeight() / (request.getHeight() * request.getHeight()); // Correction de la formule

        // Créer et sauvegarder un enregistrement BMI
        BMIRecord bmiRecord = new BMIRecord();
        bmiRecord.setUserId(currentUser.getId());
        bmiRecord.setWeight(request.getWeight());
        bmiRecord.setHeight(request.getHeight());
        bmiRecord.setBmi(bmi);

        bmiRecordRepository.save(bmiRecord);

        return ResponseEntity.ok(bmiRecord);
    }


    @GetMapping("/history")
    public ResponseEntity<List<BMIRecord>> getBMIHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal(); // Récupérer l'utilisateur authentifié

        // Récupérer l'historique des enregistrements BMI pour cet utilisateur
        List<BMIRecord> bmiHistory = bmiRecordRepository.findByUserId(currentUser.getId());

        return ResponseEntity.ok(bmiHistory);
    }

    //Mise à jour du poids et de la taille
    @PutMapping("/update/{id}")
    public ResponseEntity<BMIRecord> updateBMI(@PathVariable int id, @RequestBody BMIRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Vérifier si le BMIRecord existe et appartient à l'utilisateur authentifié
        BMIRecord bmiRecord = bmiRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BMI Record not found"));

        if (bmiRecord.getUserId()!=currentUser.getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Si l'utilisateur n'est pas le propriétaire
        }

        // Mettre à jour les informations et recalculer le BMI
        bmiRecord.setWeight(request.getWeight());
        bmiRecord.setHeight(request.getHeight());
        double bmi = request.getWeight() / (request.getHeight() * request.getHeight());
        bmiRecord.setBmi(bmi);

        bmiRecordRepository.save(bmiRecord);

        return ResponseEntity.ok(bmiRecord);
    }



    //Suppression d’un enregistrement de BMI
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<BMIRecord> deleteBMI(@PathVariable int id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Vérifier si le BMIRecord existe et appartient à l'utilisateur authentifié
        BMIRecord bmiRecord = bmiRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BMI Record not found"));

        if (bmiRecord.getUserId() != currentUser.getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Si l'utilisateur n'est pas le propriétaire
        }

        bmiRecordRepository.delete(bmiRecord);
        return ResponseEntity.ok(bmiRecord);

    }
}


package com.example.testmanagement.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO pour les paramètres de configuration d'un test de performance
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceConfigDTO {
    
    /**
     * Type de test de performance
     * Exemples: LOAD_TEST, STRESS_TEST, SPIKE_TEST, ENDURANCE_TEST
     */
    private String testType;
    
    /**
     * Nombre d'utilisateurs virtuels (threads)
     */
    private Integer numberOfUsers;
    
    /**
     * Durée du test en secondes
     */
    private Integer durationSeconds;
    
    /**
     * Ramp-up time en secondes (temps pour atteindre le nombre d'utilisateurs)
     */
    private Integer rampUpSeconds;
    
    /**
     * Nombre de requêtes par seconde (pour certains types de tests)
     */
    private Integer requestsPerSecond;
    
    /**
     * Timeout en millisecondes
     */
    private Integer timeoutMs;
    
    /**
     * Paramètres supplémentaires spécifiques au type de test
     */
    private Map<String, Object> additionalParams;
}


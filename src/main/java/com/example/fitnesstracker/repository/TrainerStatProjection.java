package com.example.fitnesstracker.repository;

/**
 * Proyección para estadísticas de trainers
 * Usado por findTrainersWithMemberCount()
 */
public interface TrainerStatProjection {
    Long getTrainerId();
    String getFirstName();
    String getLastName();
    String getSpecialty();
    Long getMemberCount();
}
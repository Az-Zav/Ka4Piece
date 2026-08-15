package com.ka4piece.model;

public class RankedVacancy {
    private Vacancy vacancy;
    double score;

    public RankedVacancy(Vacancy vacancy, double score) {
        this.vacancy = vacancy;
        this.score = score;
    }

    //Getters only
    public Vacancy getVacancy() {return vacancy;}
    public double getScore() {return score;}
}
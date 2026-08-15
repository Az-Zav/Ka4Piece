package com.ka4piece.model;

public class RankedCandidate {
    private JobSeekerProfile profile;
    double score;
    String status;

    public RankedCandidate(JobSeekerProfile profile, double score, String status) {
        this.profile = profile;
        this.score = score;
        this.status = status;
    }

    //Getters only
    public JobSeekerProfile getProfile() {return profile;}
    public double getScore() {return score;}
    public String getStatus() {return status;}
}

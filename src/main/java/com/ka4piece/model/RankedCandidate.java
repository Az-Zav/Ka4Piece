package com.ka4piece.model;

public class RankedCandidate {
    private JobseekerProfile profile;
    double score;
    String status;

    public RankedCandidate(JobseekerProfile profile, double score, String status) {
        this.profile = profile;
        this.score = score;
        this.status = status;
    }

    //Getters only
    public JobseekerProfile getProfile() {return profile;}
    public double getScore() {return score;}
    public String getStatus() {return status;}
}

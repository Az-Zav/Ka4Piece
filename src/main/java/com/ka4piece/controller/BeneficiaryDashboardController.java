package com.ka4piece.controller;

import com.ka4piece.manager.ComplianceManager;
import com.ka4piece.repository.AuthRepository;

public class BeneficiaryDashboardController {
    private AuthRepository authRepository;
    private ComplianceManager complianceManager;

    public BeneficiaryDashboardController(AuthRepository authRepository, ComplianceManager complianceManager) {
        this.authRepository = authRepository;
        this.complianceManager = complianceManager;
    }

    public BeneficiaryDashboardController() {
    }
}

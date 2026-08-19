package com.ka4piece.manager;

import com.ka4piece.model.BarangayOfficial;
import com.ka4piece.model.Household;
import com.ka4piece.model.Session;
import com.ka4piece.repository.AuthRepository;
import com.ka4piece.utilities.IdUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class AuthManager {
    AuthRepository authRepository;

    public AuthManager(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    /**
     * Authenticates a user by role, email, and password.
     * Role is selected explicitly by the UI toggle -- no more inferring role from a
     * household-then-official lookup chain, and no cross-table email collision risk.
     * Exited households cannot log in.
     */
    public Session login(String role, String email, String password) {
        if (role.equals("HOUSEHOLD")) {
            Household h = authRepository.findHouseholdByEmail(email);
            if (h == null || !h.getPassword().equals(password)) return null;
            if (h.getStatus().equals("EXITED")) return null;   // exited households can't log in
            Session s = new Session(h.getHouseholdId(), "HOUSEHOLD", h.getHeadName());
            return s;
        } else { // "OFFICIAL"
            BarangayOfficial o = authRepository.findOfficialByEmail(email);
            if (o == null || !o.getPassword().equals(password)) return null;
            Session s = new Session(o.getOfficialId(), "OFFICIAL", o.getName());
            s.setAdmin(o.isAdmin());
            return s;
        }
    }

    /** Clears any in-memory session reference the app holds. No persistence action. */
    public void logout() { /* no persistence action, clears in-memory session reference */ }

    /**
     * Registers a new household. Throws if the email is already taken.
     */
    public void registerHousehold(Household h) {
        if (authRepository.findHouseholdByEmail(h.getEmail()) != null)
            throw new IllegalArgumentException("Email already taken.");
        h.setStatus("ACTIVE");
        authRepository.saveHousehold(h);
    }

    /**
     * Registers a new barangay official. Only an admin official may call this.
     * The first-ever admin is a one-time manual DB seed row, not created through this method.
     */
    public void registerOfficial(BarangayOfficial o, Session callerSession) {
        if (!callerSession.isAdmin())
            throw new SecurityException("Only an admin official can register new officials.");
        if (authRepository.findOfficialByEmail(o.getEmail()) != null)
            throw new IllegalArgumentException("email already taken.");
        authRepository.saveOfficial(o);
    }

    /**
     * Official-assisted password reset only -- no email/token infrastructure.
     */
    public void resetPassword(String userId, String role, String newPassword) {
        if (role.equals("HOUSEHOLD")) {
            Household h = authRepository.findHouseholdById(userId);
            h.setPassword(newPassword);
            authRepository.saveHousehold(h);
        } else {
            BarangayOfficial o = authRepository.findOfficialById(userId);
            o.setPassword(newPassword);
            authRepository.saveOfficial(o);
        }
    }

    /**
     * Searches households by ID, head name, or address (case-insensitive substring match).
     */
    public List<Household> searchHouseholds(String query) {
        if (query == null || query.isBlank()) {
            return authRepository.findAllHouseholds();
        }
        String q = query.toLowerCase().trim();
        return authRepository.findAllHouseholds().stream()
            .filter(h -> (h.getHouseholdId() != null && h.getHouseholdId().toLowerCase().contains(q))
                      || (h.getHeadName() != null && h.getHeadName().toLowerCase().contains(q))
                      || (h.getAddress() != null && h.getAddress().toLowerCase().contains(q)))
            .collect(Collectors.toList());
    }

    /**
     * Sets the exit status of a household. Pass null exitReason and exitDate when reactivating.
     */
    public void setHouseholdExitStatus(String householdId, String status, String exitReason, LocalDate exitDate) {
        Household h = authRepository.findHouseholdById(householdId);
        h.setStatus(status);
        h.setExitReason(exitReason);
        h.setExitDate(exitDate);
        authRepository.saveHousehold(h);
    }
}

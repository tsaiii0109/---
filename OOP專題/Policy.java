package OOP專題;

import java.util.Arrays;
import java.util.List;

public class Policy {
    public static boolean checkUserAccess(User user, List<String> allowedRoles) {
        return allowedRoles.contains(user.toString());
    }

    public static boolean canViewPatientDetails(User user) {
        return checkUserAccess(user, Arrays.asList("Nurse", "Doctor"));
    }

    public static boolean canEditPatientDetails(User user) {
        return checkUserAccess(user, Arrays.asList("Doctor"));
    }
}

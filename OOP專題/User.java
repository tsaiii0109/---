package OOP專題;

import java.io.Serializable;

public class User implements Serializable {
    protected String name;
    protected String username;
    protected String password;

    public User(String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    @Override
    public String toString() {
    	return "Welcome , User_" + this.getName();
    }
}


class Doctor extends User implements Serializable {
    public Doctor(String name, String username, String password) {
        super(name, username, password);
    }

    @Override
    public String toString() {
    	return "Welcome , Doctor_" + this.getName();
    }
}

class Nurse extends User implements Serializable {
    public Nurse(String name, String username, String password) {
        super(name, username, password);
    }

    @Override
    public String toString() {
    	return "Welcome , Nurse_" + this.getName();
    }
}


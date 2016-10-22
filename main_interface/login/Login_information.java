package main_interface.login;

/**
 * Created by Leander on 09.09.2016.
 * Wrapper class to store login information
 */
public class Login_information {

    private String user;
    private String pw;

    Login_information(String user, String pw)
    {
        this.user = user;
        this.pw = pw;
    }

    public String get_user(){return user;}
    public String get_pw(){return pw;}

}

package transcendentlabs.com.cloudwalk;


import com.parse.ParseUser;

public class Constants {

    private static String username;

    public static String getUserName(){
        if(username == null){
            username = ParseUser.getCurrentUser().getUsername().toString();
        }
        return username;
    }

    public static void setUsername(String usrname){
        username = usrname;
    }
}

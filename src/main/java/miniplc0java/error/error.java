package miniplc0java.error;

public class error extends Exception{
    public String ErrorMessage;
    public error(String errorMessage)
    {
        this.ErrorMessage = errorMessage;
    }
}

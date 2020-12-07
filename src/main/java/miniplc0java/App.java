package miniplc0java;
import miniplc0java.analysizer.*;
import java.io.File;
import java.io.IOException;

public class App {

    public static void main(String[] args) throws IOException {
        Analysizer A = new Analysizer(args[0],args[1]);
    }
}

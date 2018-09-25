import javax.swing.UIManager;
import org.wind.gui.WindMainPanel;

public class Main {
    public static void main(String [] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Throwable e ) {
            e.printStackTrace();
        }
        new WindMainPanel();
    }
}

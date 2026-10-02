import ui.VentanaPrincipal;

import javax.swing.*;

/**
 * Punto de entrada.
 * Arranca la UI en el Event Dispatch Thread.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal v = new VentanaPrincipal();
            v.setVisible(true);
        });
    }
}
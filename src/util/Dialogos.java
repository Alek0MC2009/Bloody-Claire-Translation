package util;

import javax.swing.*;
import java.awt.*;

/**
 * Helpers de diálogos modales.
 */
public final class Dialogos {

    private Dialogos() {}

    public static String pedirTexto(Component padre, String titulo, String mensaje, String valorInicial) {
        Object r = JOptionPane.showInputDialog(padre, mensaje, titulo,
                JOptionPane.PLAIN_MESSAGE, null, null, valorInicial);
        return r == null ? null : r.toString();
    }

    public static boolean confirmar(Component padre, String titulo, String mensaje) {
        int r = JOptionPane.showConfirmDialog(padre, mensaje, titulo,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return r == JOptionPane.YES_OPTION;
    }

    public static void aviso(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Aviso", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
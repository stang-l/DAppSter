package org.web3j.sample.util;

import javafx.scene.text.Text;

public class Utils {
    public static void write(Text text, String s) {
        text.setText(text.getText() + '\n' + s);
    }
}

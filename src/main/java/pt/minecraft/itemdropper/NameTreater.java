
package pt.minecraft.itemdropper;

public class NameTreater {

    public static String treat(String s) {
        if (s.length() == 0)
            return s;

        s = s.replaceAll("_", " ");
        return s.toLowerCase();
    }

}
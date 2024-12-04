package me.aviso.ArmyAntiCheat.utils;

import net.md_5.bungee.api.ChatColor;

public class Color {

    public static String reColor(String text) {
        return text.replace('&', '§');
    }

    public static String hexcolor(String input) {
        int count = (int) input.chars().filter(ch -> ch == '#').count();
        switch (count) {
            case 1: {
                String[] messages = input.split("#");
                String startTag = messages[1].substring(0, 6);
                String newmessage = input.replace("#" + startTag, "");

                double r1 = hexToRgb(startTag.substring(0, 2));
                double g1 = hexToRgb(startTag.substring(2, 4));
                double b1 = hexToRgb(startTag.substring(4, 6));

                if (r1 < 0 || g1 < 0 || b1 < 0) return input;

                return ChatColor.of(new java.awt.Color((int) r1, (int) g1, (int) b1)) + newmessage;
            }
            case 2: {
                String[] messages = input.split("#");
                String startTag = messages[1].substring(0, 6);
                String endTag = messages[2].substring(0, 6);
                String newmessage = input.replace("#" + startTag, "").replace("#" + endTag, "");

                double r1 = hexToRgb(startTag.substring(0, 2));
                double g1 = hexToRgb(startTag.substring(2, 4));
                double b1 = hexToRgb(startTag.substring(4, 6));

                double r2 = hexToRgb(endTag.substring(0, 2));
                double g2 = hexToRgb(endTag.substring(2, 4));
                double b2 = hexToRgb(endTag.substring(4, 6));

                if (r1 < 0 || g1 < 0 || b1 < 0 || r2 < 0 || g2 < 0 || b2 < 0) return input;

                double length = newmessage.length();
                double incrementR = (r1 - r2) / length;
                double incrementG = (g1 - g2) / length;
                double incrementB = (b1 - b2) / length;

                StringBuilder returnMessage = new StringBuilder();
                for (int i = 0; i < length; i++) {
                    returnMessage.append(ChatColor.of(new java.awt.Color((int) r1, (int) g1, (int) b1)));
                    returnMessage.append(newmessage.charAt(i));

                    r1 -= incrementR;
                    g1 -= incrementG;
                    b1 -= incrementB;
                }
                return returnMessage.toString();
            }
            default:
                return reColor(input);
        }
    }

    private static double hexToRgb(String hex) {
        try {
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}

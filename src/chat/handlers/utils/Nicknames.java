package chat.handlers.utils;

import java.util.Arrays;
import java.util.Random;

public class Nicknames {
    private static Boolean[] alreadyTaken = new Boolean[11];
    private static String[] nicknames = {
            "Leia",
            "Han Solo",
            "Chewbacca",
            "Darth Vader",
            "Palpatine",
            "Kylo Ren",
            "Darth Maul",
            "General Grievous",
            "Luke",
            "Obi-Wan Kenobi",
            "Yoda",
    };

    public static void init(){
        Arrays.fill(alreadyTaken, false);
    }

    public static String getNickname(String arg){
        if (arg.equals("")) {
            Random random = new Random();
            int randomInteger;
            do {
                randomInteger = random.nextInt(11);
            } while (alreadyTaken[randomInteger]);
            alreadyTaken[randomInteger] = true;
            return nicknames[randomInteger];
        }
        else
            return nicknames[Integer.parseInt(arg)];
    }
}
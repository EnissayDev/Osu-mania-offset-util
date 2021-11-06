package fr.enissay;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Main implements NativeKeyListener {

    private final HashMap<Integer, Long> delayKey = new HashMap<>();
    private static final List<String> keyList = new ArrayList<String>();

    private int delayCounter, keyPressed, previousOffset = 0;

    public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
        return map.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), value))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        if (keyList.contains(NativeKeyEvent.getKeyText(e.getKeyCode()))) {
            //System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
            delayKey.put(e.getKeyCode(), System.currentTimeMillis());
            keyPressed++;
        }
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        if (keyList.contains(NativeKeyEvent.getKeyText(e.getKeyCode()))) {
            //System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
            delayKey.values().forEach(sets -> {
                if (getKeysByValue(delayKey, sets).contains(e.getKeyCode())) {
                    final long start = sets.longValue();
                    final long end = System.currentTimeMillis();
                    final long time = end - start;
                    final long timeInMs = TimeUnit.MILLISECONDS.toMillis(time);
                    //System.out.println("Delay: " + timeInMs + " ms");
                    delayCounter += timeInMs;
                }
            });
        }else if (NativeKeyEvent.getKeyText(e.getKeyCode()).equalsIgnoreCase("N")){
            if (keyPressed > 0) {
                final int offset = ((delayCounter + previousOffset) / (previousOffset > 0 ? (keyPressed + 1) : (keyPressed)));

                if (previousOffset > 0)
                    System.out.println("Offset recommended: " + offset + " | Previous offset: " + previousOffset);
                else System.out.println("Offset recommended: " + offset);
                previousOffset = offset;

                delayKey.clear();

                delayCounter = 0;
                keyPressed = 0;

                System.out.println("------------------------------------------------------------------");

            }else System.out.println("You haven't typed anything yet!");
        }
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
        //System.out.println("Key Typed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
    }

    public static void main(String args[]){
        //Just put this into your main:

        keyList.addAll(Arrays.asList("Q", "S", "K", "L"));

        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);

        try {
            GlobalScreen.registerNativeHook();
        }
        catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            System.exit(1);
        }

        GlobalScreen.addNativeKeyListener(new Main());
        //Remember to include this^                     ^- Your class
    }
}

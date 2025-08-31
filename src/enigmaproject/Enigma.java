package enigmaproject;

import java.util.*;

public class Enigma {
    
    private Rotor[] rotors;
    private Reflector reflector;
    private Plugboard plugboard;
    private int numberOfRotors;
    
    // Inner class untuk Rotor
    private class Rotor {
        private String wiring;
        private char notch;
        private int position;    // Current position (0-25)
        private int ringSetting; // Ring setting (0-25)
        private boolean hasAdvanced;
        
        public Rotor(String wiring, char notch, int ringSetting, int initialPosition) {
            this.wiring = wiring.toUpperCase();
            this.notch = Character.toUpperCase(notch);
            this.ringSetting = ringSetting;
            this.position = initialPosition;
            this.hasAdvanced = false;
        }
        
        public char encodeForward(char input) {
            // Convert input through rotor (right to left)
            int offset = (charToInt(input) + position - ringSetting + 26) % 26;
            char encoded = wiring.charAt(offset);
            return intToChar((charToInt(encoded) - position + ringSetting + 26) % 26);
        }
        
        public char encodeBackward(char input) {
            // Convert input through rotor backwards (left to right)
            int adjustedInput = (charToInt(input) + position - ringSetting + 26) % 26;
            char inputChar = intToChar(adjustedInput);
            int wiringIndex = wiring.indexOf(inputChar);
            return intToChar((wiringIndex - position + ringSetting + 26) % 26);
        }
        
        public boolean advance() {
            hasAdvanced = true;
            position = (position + 1) % 26;
            return isAtNotch();
        }
        
        public boolean isAtNotch() {
            return intToChar(position) == notch;
        }
        
        public char getCurrentPosition() {
            return intToChar(position);
        }
        
        public void setPosition(char pos) {
            this.position = charToInt(pos);
        }
        
        public boolean hasAdvancedThisCycle() {
            return hasAdvanced;
        }
        
        public void resetAdvancedFlag() {
            hasAdvanced = false;
        }
    }
    
    // Inner class untuk Reflector
    private class Reflector {
        private String wiring;
        
        public Reflector(String wiring) {
            this.wiring = wiring.toUpperCase();
        }
        
        public char reflect(char input) {
            return wiring.charAt(charToInt(input));
        }
    }
    
    // Inner class untuk Plugboard
    private class Plugboard {
        private Map<Character, Character> connections;
        
        public Plugboard(String[] pairs) {
            connections = new HashMap<>();
            
            for (String pair : pairs) {
                if (pair.length() == 2) {
                    char first = Character.toUpperCase(pair.charAt(0));
                    char second = Character.toUpperCase(pair.charAt(1));
                    connections.put(first, second);
                    connections.put(second, first);
                }
            }
        }
        
        public char swap(char input) {
            return connections.getOrDefault(Character.toUpperCase(input), Character.toUpperCase(input));
        }
    }
    
    // Constructor utama
    public Enigma(String[] rotorWires, char[] notches, String reflectorWiring, 
                  String ringSettings, String initialPositions, String[] plugboardPairs) {
        
        numberOfRotors = rotorWires.length;
        rotors = new Rotor[numberOfRotors];
        
        // Parse ring settings dan initial positions
        String[] ringParts = ringSettings.trim().split("\\s+");
        String[] posParts = initialPositions.trim().split("\\s+");
        
        // Buat rotors
        for (int i = 0; i < numberOfRotors; i++) {
            int ringSetting = (i < ringParts.length) ? charToInt(ringParts[i].charAt(0)) : 0;
            int initialPos = (i < posParts.length) ? charToInt(posParts[i].charAt(0)) : 0;
            
            rotors[i] = new Rotor(rotorWires[i], notches[i], ringSetting, initialPos);
        }
        
        // Buat reflector dan plugboard
        reflector = new Reflector(reflectorWiring);
        plugboard = new Plugboard(plugboardPairs);
    }
    
    // Method enkripsi utama
    public String encipher(String input) {
        StringBuilder result = new StringBuilder();
        
        for (char c : input.toCharArray()) {
            if (Character.isLetter(c)) {
                result.append(encipherChar(Character.toUpperCase(c)));
            } else {
                result.append(c); // Non-alphabetic characters pass through
            }
        }
        
        return result.toString();
    }
    
    private char encipherChar(char input) {
        // Reset advanced flags
        for (Rotor rotor : rotors) {
            rotor.resetAdvancedFlag();
        }
        
        // Advance rotors sesuai mekanisme Enigma
        advanceRotors();
        
        char current = input;
        
        // 1. Through plugboard
        current = plugboard.swap(current);
        
        // 2. Through rotors (right to left)
        for (int i = numberOfRotors - 1; i >= 0; i--) {
            current = rotors[i].encodeForward(current);
        }
        
        // 3. Through reflector
        current = reflector.reflect(current);
        
        // 4. Back through rotors (left to right)
        for (int i = 0; i < numberOfRotors; i++) {
            current = rotors[i].encodeBackward(current);
        }
        
        // 5. Back through plugboard
        current = plugboard.swap(current);
        
        return current;
    }
    
    private void advanceRotors() {
        if (numberOfRotors < 1) return;
        
        // Enigma stepping mechanism
        boolean[] shouldAdvance = new boolean[numberOfRotors];
        
        // Rightmost rotor always advances
        shouldAdvance[numberOfRotors - 1] = true;
        
        // Check for double-stepping dan notch positions
        for (int i = numberOfRotors - 2; i >= 0; i--) {
            // Jika rotor di sebelah kanan ada di notch position
            if (rotors[i + 1].isAtNotch()) {
                shouldAdvance[i] = true;
                // Double stepping: jika rotor tengah akan advance, rotor tengah juga advance
                if (i < numberOfRotors - 1) {
                    shouldAdvance[i + 1] = true;
                }
            }
        }
        
        // Advance rotors yang perlu diadvance
        for (int i = 0; i < numberOfRotors; i++) {
            if (shouldAdvance[i]) {
                rotors[i].advance();
            }
        }
    }
    
    // Method untuk mendapatkan posisi rotor saat ini (dibutuhkan GUI)
    public String getCurrentRotorPositions() {
        StringBuilder positions = new StringBuilder();
        for (int i = 0; i < numberOfRotors; i++) {
            if (i > 0) positions.append(" ");
            positions.append(rotors[i].getCurrentPosition());
        }
        return positions.toString();
    }
    
    // Method untuk reset posisi rotor
    public void resetRotorPositions(String positions) {
        String[] posParts = positions.trim().split("\\s+");
        for (int i = 0; i < numberOfRotors && i < posParts.length; i++) {
            if (posParts[i].length() > 0) {
                rotors[i].setPosition(posParts[i].charAt(0));
            }
        }
    }
    
    // Utility methods
    private int charToInt(char c) {
        return Character.toUpperCase(c) - 'A';
    }
    
    private char intToChar(int i) {
        return (char) ('A' + (i % 26));
    }
    
    // Method untuk mendapatkan informasi konfigurasi
    public String getConfiguration() {
        StringBuilder config = new StringBuilder();
        config.append("Rotor Positions: ").append(getCurrentRotorPositions()).append("\n");
        config.append("Number of Rotors: ").append(numberOfRotors).append("\n");
        return config.toString();
    }
    
    // Method untuk debugging
    public void printRotorStates() {
        System.out.println("=== ROTOR STATES ===");
        for (int i = 0; i < numberOfRotors; i++) {
            System.out.printf("Rotor %d: Position=%c, AtNotch=%b%n", 
                i + 1, rotors[i].getCurrentPosition(), rotors[i].isAtNotch());
        }
        System.out.println("==================");
    }
}
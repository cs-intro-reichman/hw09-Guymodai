import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
    private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     * seed value. Generating texts from this model multiple times with the 
     * same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
    public void train(String fileName) {
        String window = "";
        char c;
        In in = new In(fileName);
        // Reads just enough characters to form the first window. [cite: 380, 381]
        for (int i = 0; i < windowLength; i++) {
            if (!in.isEmpty()) {
                window += in.readChar();
            }
        }
        // Processes the entire text, one character at a time [cite: 382]
        while (!in.isEmpty()) {
            // Gets the next character [cite: 384, 386]
            c = in.readChar();
            // Checks if the window is already in the map [cite: 387, 388]
            List probs = CharDataMap.get(window);
            // If the window was not found in the map [cite: 390, 391]
            if (probs == null) {
                // Creates a new empty list, and adds (window, list) to the map [cite: 393, 394]
                probs = new List();
                CharDataMap.put(window, probs);
            }
            // Calculates the counts of the current character. [cite: 396, 397]
            probs.update(c);
            // Advances the window: adds c to the window's end, and deletes the window's first character. [cite: 398, 399]
            window = window.substring(1) + c;
        }
        // The entire file has been processed, and all the characters have been counted. [cite: 400]
        // Proceeds to compute and set the p and cp fields of all the CharData objects [cite: 401]
        // in each linked list in the map. [cite: 402, 403, 404, 405]
        for (List probs : CharDataMap.values()) {
            calculateProbabilities(probs);
        }
    }

    // Computes and sets the probabilities (p and cp fields) of all the
    // characters in the given list. [cite: 120, 121]
    void calculateProbabilities(List probs) {
        int totalCount = 0;
        // Iterate over the list and compute how many characters exist in total. [cite: 122]
        for (int i = 0; i < probs.getSize(); i++) {
            totalCount += probs.get(i).count;
        }
        int cumulativeCount = 0;
        for (int i = 0; i < probs.getSize(); i++) {
            CharData cd = probs.get(i);
            // Compute and set the values of p and cp of every list element [cite: 123]
            cd.p = (double) cd.count / totalCount;
            cumulativeCount += cd.count;
            cd.cp = (double) cumulativeCount / totalCount;
        }
    }

    // Returns a random character from the given probabilities list. [cite: 144]
    char getRandomChar(List probs) {
        // Drawing a random number in [0,1). Let's call the resulting number r. [cite: 138]
        double r = randomGenerator.nextDouble();
        // Iterate the list, reading the cumulative probabilities (the cp fields) [cite: 139]
        for (int i = 0; i < probs.getSize(); i++) {
            CharData cd = probs.get(i);
            // Stop at the element whose cumulative probability is greater than r [cite: 140]
            if (cd.cp > r) {
                return cd.chr;
            }
        }
        // return the character of this element. [cite: 140]
        return probs.get(probs.getSize() - 1).chr;
    }

    /**
     * Generates a random text, based on the probabilities that were learned during training. 
     * @param initialText - text to start with.
     * @param textLength - the size of text to generate
     * @return the generated text
     */
    public String generate(String initialText, int textLength) {
        // If initialText's length is less than windowLength, we generate no text. [cite: 227, 228]
        if (initialText.length() < windowLength) {
            return initialText;
        }
        
        StringBuilder generatedText = new StringBuilder(initialText);
        // Initial window set to the last windowLength characters of the initial text. [cite: 230]
        String window = initialText.substring(initialText.length() - windowLength);
        
        // The process stops when the length of the added text equals textLength. [cite: 232]
        while (generatedText.length() < textLength + initialText.length()) {
            List probs = CharDataMap.get(window);
            // In any iteration, if the current window is not found in the map, we stop. [cite: 233]
            if (probs == null) {
                break;
            }
            char nextChar = getRandomChar(probs);
            generatedText.append(nextChar);
            // Window is set to the last windowLength characters of the generated text. [cite: 231]
            window = generatedText.substring(generatedText.length() - windowLength);
        }
        
        return generatedText.toString();
    }

    /** Returns a string representing the map of this language model. */
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (String key : CharDataMap.keySet()) {
            List keyProbs = CharDataMap.get(key);
            str.append(key + " : " + keyProbs + "\n");
        }
        return str.toString();
    }

    public static void main(String[] args) {
        int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]);
        Boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];
        // Create the LanguageModel object [cite: 271]
        LanguageModel lm;
        if (randomGeneration) {
            lm = new LanguageModel(windowLength); [cite: 272, 274]
        } else {
            lm = new LanguageModel(windowLength, 20); [cite: 273, 275]
        }
        // Trains the model, creating the map. [cite: 276]
        lm.train(fileName);
        // Generates text, and prints it. [cite: 277, 278]
        System.out.println(lm.generate(initialText, generatedTextLength));
    }
}
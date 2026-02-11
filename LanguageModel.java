import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap; // [cite: 180]
    
    // The window length used in this model.
    int windowLength; // [cite: 226]
    
    // The random number generator used by this model. 
    private Random randomGenerator; // [cite: 255]

    /** Constructs a language model with the given window length and a given
     * seed value. Generating texts from this model multiple times with the 
     * same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) { // [cite: 256]
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) { // [cite: 256]
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
    public void train(String fileName) { // [cite: 199]
        String window = "";
        char c;
        In in = new In(fileName); // [cite: 379]

        // Reads just enough characters to form the first window.
        for (int i = 0; i < windowLength; i++) { // [cite: 380-381]
            if (!in.isEmpty()) {
                window += in.readChar();
            }
        }

        // Processes the entire text, one character at a time
        while (!in.isEmpty()) { // [cite: 382]
            // Gets the next character
            c = in.readChar(); // [cite: 384-386]

            // Checks if the window is already in the map
            List probs = CharDataMap.get(window); // [cite: 387-389]

            // If the window was not found in the map
            if (probs == null) { // [cite: 390-391]
                // Creates a new empty list, and adds (window, list) to the map
                probs = new List(); // [cite: 393-395]
                CharDataMap.put(window, probs);
            }

            // Calculates the counts of the current character.
            probs.update(c); // [cite: 397]

            // Advances the window: adds c to the window's end, and deletes the window's first character.
            window = window.substring(1) + c; // [cite: 398-399]
        }

        // The entire file has been processed, and all the characters have been counted.
        // Proceeds to compute and set the p and cp fields of all the CharData objects in each linked list in the map.
        for (List probs : CharDataMap.values()) { // [cite: 403-405]
            calculateProbabilities(probs);
        }
    }

    // Computes and sets the probabilities (p and cp fields) of all the characters in the given list.
    void calculateProbabilities(List probs) { // [cite: 120-121]
        int totalCount = 0;
        // Iterate over the list and compute how many characters exist in total.
        for (int i = 0; i < probs.getSize(); i++) { // [cite: 122]
            totalCount += probs.get(i).count;
        }

        double cumulativeProb = 0;
        for (int i = 0; i < probs.getSize(); i++) {
            CharData cd = probs.get(i);
            // Compute and set the values of p and cp of every list element
            cd.p = (double) cd.count / totalCount; // [cite: 123]
            cumulativeProb += cd.p; // 
            cd.cp = cumulativeProb;
        }
    }

    // Returns a random character from the given probabilities list.
    char getRandomChar(List probs) { // [cite: 144]
        // Drawing a random number in [0,1). Let's call the resulting number r.
        double r = randomGenerator.nextDouble(); // [cite: 138, 259]

        // Iterate the list, reading the cumulative probabilities (the cp fields)
        for (int i = 0; i < probs.getSize(); i++) { // [cite: 139]
            // Stop at the element whose cumulative probability is greater than r
            if (probs.get(i).cp > r) { // [cite: 140-141]
                return probs.get(i).chr;
            }
        }
        // return the character of the last element as a fallback
        return probs.get(probs.getSize() - 1).chr;
    }

    /**
     * Generates a random text, based on the probabilities that were learned during training. 
     * @param initialText - text to start with.
     * @param textLength - the total length of the text to generate
     * @return the generated text
     */
    public String generate(String initialText, int textLength) { // [cite: 206]
        // If the length of the initial text provided is less than windowLength, terminate.
        if (initialText.length() < windowLength) { // [cite: 227-228]
            return initialText;
        }

        StringBuilder generatedText = new StringBuilder(initialText);
        // Setting the initial window to the last windowLength characters of initialText.
        String window = initialText.substring(initialText.length() - windowLength); // [cite: 230]

        // The process stops when the length of the generated text equals textLength.
        while (generatedText.length() < textLength) { // 
            List probs = CharDataMap.get(window);
            
            // If the current window is not found in the map, stop the process.
            if (probs == null) { // [cite: 233]
                break;
            }

            char nextChar = getRandomChar(probs);
            generatedText.append(nextChar);

            // In each iteration, the window is set to the last windowLength characters of the generated text.
            window = generatedText.substring(generatedText.length() - windowLength); // [cite: 231]
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

    public static void main(String[] args) { // [cite: 267]
        int windowLength = Integer.parseInt(args[0]); // [cite: 268]
        String initialText = args[1]; // [cite: 269]
        int generatedTextLength = Integer.parseInt(args[2]); // [cite: 270]
        Boolean randomGeneration = args[3].equals("random"); // [cite: 270]
        String fileName = args[4]; // [cite: 270]

        // Create the LanguageModel object
        LanguageModel lm; // [cite: 271]
        if (randomGeneration) {
            lm = new LanguageModel(windowLength); // [cite: 272, 274]
        } else {
            lm = new LanguageModel(windowLength, 20); // [cite: 273, 275]
        }

        // Trains the model, creating the map.
        lm.train(fileName); // [cite: 276]

        // Generates text, and prints it.
        System.out.println(lm.generate(initialText, generatedTextLength)); // [cite: 277-278]
    }
}
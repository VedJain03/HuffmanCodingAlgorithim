package huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class contains methods which, when used together, perform the
 * entire Huffman Coding encoding and decoding process
 * 
 * @author Ishaan Ivaturi
 * @author Prince Rawal
 */
public class HuffmanCoding {
    /**
     * Writes a given string of 1's and 0's to the given file byte by byte
     * and NOT as characters of 1 and 0 which take up 8 bits each
     * 
     * @param filename The file to write to (doesn't need to exist yet)
     * @param bitString The string of 1's and 0's to write to the file in bits
     */
    public static void writeBitString(String filename, String bitString) {
        byte[] bytes = new byte[bitString.length() / 8 + 1];
        int bytesIndex = 0, byteIndex = 0, currentByte = 0;

        // Pad the string with initial zeroes and then a one in order to bring
        // its length to a multiple of 8. When reading, the 1 signifies the
        // end of padding.
        int padding = 8 - (bitString.length() % 8);
        String pad = "";
        for (int i = 0; i < padding-1; i++) pad = pad + "0";
        pad = pad + "1";
        bitString = pad + bitString;

        // For every bit, add it to the right spot in the corresponding byte,
        // and store bytes in the array when finished
        for (char c : bitString.toCharArray()) {
            if (c != '1' && c != '0') {
                System.out.println("Invalid characters in bitstring");
                System.exit(1);
            }

            if (c == '1') currentByte += 1 << (7-byteIndex);
            byteIndex++;
            
            if (byteIndex == 8) {
                bytes[bytesIndex] = (byte) currentByte;
                bytesIndex++;
                currentByte = 0;
                byteIndex = 0;
            }
        }
        
        // Write the array of bytes to the provided file
        try {
            FileOutputStream out = new FileOutputStream(filename);
            out.write(bytes);
            out.close();
        }
        catch(Exception e) {
            System.err.println("Error when writing to file!");
        }
    }
    
    /**
     * Reads a given file byte by byte, and returns a string of 1's and 0's
     * representing the bits in the file
     * 
     * @param filename The encoded file to read from
     * @return String of 1's and 0's representing the bits in the file
     */
    public static String readBitString(String filename) {
        String bitString = "";
        
        try {
            FileInputStream in = new FileInputStream(filename);
            File file = new File(filename);

            byte bytes[] = new byte[(int) file.length()];
            in.read(bytes);
            in.close();
            
            // For each byte read, convert it to a binary string of length 8 and add it
            // to the bit string
            for (byte b : bytes) {
                bitString = bitString + 
                String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            }

            // Detect the first 1 signifying the end of padding, then remove the first few
            // characters, including the 1
            for (int i = 0; i < 8; i++) {
                if (bitString.charAt(i) == '1') return bitString.substring(i+1);
            }
            
            return bitString.substring(8);
        }
        catch(Exception e) {
            System.out.println("Error while reading file!");
            return "";
        }
    }

    /**
     * Reads a given text file character by character, and returns an arraylist
     * of CharFreq objects with frequency > 0, sorted by frequency
     * 
     * @param filename The text file to read from
     * @return Arraylist of CharFreq objects, sorted by frequency
     */
    public static ArrayList<CharFreq> makeSortedList(String filename) {
        StdIn.setFile(filename);

        ArrayList<CharFreq> freq = new ArrayList<CharFreq>(); //ArrayList to return

        //System.out.println(filename.length()); //easier to make a length variable of the original String
        int[] counter = new int[128]; //counts the frequency of the characters that appear using ASCII values
        int charCounter = 0;
        char curr;
        while(StdIn.hasNextChar()){ //while the String is not empty
            curr = StdIn.readChar(); //curr is the next char in filename
            counter[curr]++; //the frequency will be updated as filename is traversed
            charCounter++;
        }

        for (int i = 0; i < 128; i++) { //going through the frequency array
            if (counter[i] > 0){ //if there is at least one occurrence
                freq.add(new CharFreq((char)i, (double)counter[i]/charCounter)); //add new CharFreq with the char and its prob of occur
                //System.out.println((char)i + " " + counter[i]);
            }                                                                 
        }
        if (freq.size() == 1){ //if there is only one character with occurrences
            int index = (int)freq.get(0).getCharacter(); //get the ascii value for the character with occurrence
            if (index == 127){
                index = -1; //wrap the ascii values around to the start
            }
            freq.add(new CharFreq ((char)(index+1), 0));//add next char in ASCII with probOcurr 0
        }
        Collections.sort(freq);
        return freq;
    }

    /**
     * Uses a given sorted arraylist of CharFreq objects to build a huffman coding tree
     * 
     * @param sortedList The arraylist of CharFreq objects to build the tree from
     * @return A TreeNode representing the root of the huffman coding tree
     */
    public static TreeNode makeTree(ArrayList<CharFreq> sortedList) {
        Queue<TreeNode> source = new Queue<TreeNode>();
        Queue<TreeNode> target = new Queue<TreeNode>();

        for (int i = 0; i < sortedList.size(); i++) {
            source.enqueue(new TreeNode(sortedList.get(i), null, null)); //add all values of sortedList as tree nodes
            // System.out.println(1);
        }
        TreeNode left; //left node
        TreeNode right; //right node
        TreeNode curr; //parent node to left and right
        //NEED TO FIND A BETTER WAY TO CHECK IF TARGET AND SOURCE ARE NULL.... or not :)
        while (!source.isEmpty() || target.size() != 1){ //while source isnt empty and target doesnt have only 1 value    
           if (target.isEmpty()){
                left = source.dequeue();
                // System.out.println(2);
            }
            else if (source.isEmpty()){
                left = target.dequeue();
                // System.out.println(3);
            }
            else if(source.peek().getData().getProbOccurrence() <= target.peek().getData().getProbOccurrence()){//source has smaller or equal occurrence
                left = source.dequeue();
                // System.out.println(4);
            }
            else{//target has smaller occurrence
                left = target.dequeue();
                // System.out.println(5);
            }

            //right node
            if (target.isEmpty()){
                right = source.dequeue();
                // System.out.println(6);
            }
            else if (source.isEmpty()){
                right = target.dequeue();
                // System.out.println(7);
            }
            else if(source.peek().getData().getProbOccurrence() <= target.peek().getData().getProbOccurrence()){//source has smaller or equal occurrence
                right = source.dequeue();
                // System.out.println(8);
            }
            else{//target has smaller occurrence
                right = target.dequeue();
                // System.out.println(9);
            }

            curr = new TreeNode(new CharFreq(null, left.getData().getProbOccurrence() + right.getData().getProbOccurrence()), left, right); //adds the probOcc of left and right and adds to the parent node
            target.enqueue(curr);
        }
        // System.out.println(10);
        return target.peek();
    }
    private static String findBin(TreeNode root, String s, char c)
    {
        if(root == null){
            return "";
        }
        if (root.getLeft() == null && root.getRight() == null && 
            root.getData().getCharacter() != null && root.getData().getCharacter().equals(c)) {
 
            // c is the character in the node
            return s;
        }
 

        String s1 = findBin(root.getLeft(), s + "0", c);
        if(s1 != null && !s1.equals("")){
            return s1;
        }
        String s2 = findBin(root.getRight(), s + "1", c);
        if(s2 != null && !s2.equals("")){
            return s2;
        }

        return null; //should never reach this point
    }

    private static ArrayList<Character> findCharList(TreeNode root){
        ArrayList<Character> c = new ArrayList<Character>();

        ArrayList<TreeNode> s = new ArrayList<TreeNode>();
        TreeNode curr = root;
 
        while (curr != null || s.isEmpty() == false)
        {
            while (curr != null)
            {
                s.add(curr);
                curr = curr.getLeft();
            }
 
            // Current should be null at this point
            curr = s.remove(s.size()-1);
            if(curr.getData().getCharacter() != null){
                c.add(curr.getData().getCharacter());
            }
            curr = curr.getRight();
 
        } 
        return c;
    }


    /**
     * Uses a given huffman coding tree to create a string array of size 128, where each
     * index in the array contains that ASCII character's bitstring encoding. Characters not
     * present in the huffman coding tree should have their spots in the array left null
     * 
     * @param root The root of the given huffman coding tree
     * @return Array of strings containing only 1's and 0's representing character encodings
     */
    public static String[] makeEncodings(TreeNode root) { //NEED TO FIX THIS METHOD //bst slide 27
        String[] result = new String[128]; //array to return
        
        ArrayList<Character> charList = findCharList(root);
        for (int i = 0; i < charList.size(); i++) {
            String bin = findBin(root, "", charList.get(i));
            if(bin != null){
                result[(int)charList.get(i)] = bin;
                // System.out.println(charList.get(i) + " " + bin);
            }
        }
    
        return result;
    }

    /**
     * Using a given string array of encodings, a given text file, and a file name to encode into,
     * this method makes use of the writeBitString method to write the final encoding of 1's and
     * 0's to the encoded file.
     * 
     * @param encodings The array containing binary string encodings for each ASCII character
     * @param textFile The text file which is to be encoded
     * @param encodedFile The file name into which the text file is to be encoded
     */
    public static void encodeFromArray(String[] encodings, String textFile, String encodedFile) {
        StdIn.setFile(textFile);
        
        String bin = "";
        //System.out.println(encodings.length);
        while(StdIn.hasNextChar()){
            char curr = StdIn.readChar(); //next character in string
            if (encodings[curr] != null){ //if there is a binary string for the character and it isn't null
                // System.out.println(encodings[curr]);
                bin += encodings[curr]; //add the binary to bin
            }
        }
        writeBitString(encodedFile, bin); //turn string to bytes
    }
    
    /**
     * Using a given encoded file name and a huffman coding tree, this method makes use of the 
     * readBitString method to convert the file into a bit string, then decodes the bit string
     * using the tree, and writes it to a file.
     * 
     * @param encodedFile The file which contains the encoded text we want to decode
     * @param root The root of your Huffman Coding tree
     * @param decodedFile The file which you want to decode into
     */
    public static void decode(String encodedFile, TreeNode root, String decodedFile) { //the problem might be with maketree
        StdOut.setFile(decodedFile);
        
        String text = readBitString(encodedFile); //turn bytes to string of 0s and 1s
        boolean loopWhileTrue; //until the curr binary doesn't form a letter
        TreeNode curr = root; //current TreeNode while traversing the string
        // System.out.println(text);
        while(!text.equals("")){ //until all the binary values are tested
            // System.out.println(0);
            loopWhileTrue = true;
            while (loopWhileTrue){ //loops until a character is reached
                // System.out.println(1);
                if(curr.getData().getCharacter() != null){ //if there is a character at curr's current value
                    StdOut.print(curr.getData().getCharacter()); //print the current character to the file, not sure if this the right syntax
                    loopWhileTrue = false; //get out of the inner loop
                }
                else if (!text.equals("") && text.substring(0,1).equals("0")){ //if current first binary number is 0
                    // System.out.println(2);
                    curr = curr.getLeft(); //go left
                    text = text.substring(1); //remove the 0
                }
                else if (!text.equals("") && text.substring(0,1).equals("1")){ //if current first binary number is 1
                    // System.out.println(3);
                    curr = curr.getRight(); //go right
                    text = text.substring(1); //remove the 1
                }
                else{
                    loopWhileTrue = false; //idk if this works this is a bandaid solution
                }
            }
            // System.out.println(4);
            curr = root; //reset curr
        }
        // System.out.println(5);
    }
}
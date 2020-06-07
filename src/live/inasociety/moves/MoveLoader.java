package live.inasociety.moves;

import live.inasociety.interactors.HitBox;
import live.inasociety.interactors.HurtBox;

import java.io.*;
import java.util.ArrayList;

public class MoveLoader {
    private static char hitChar = 'X';
    private static  char hurtChar = 'C';
    private static String movePath;

    public static Move loadMove(String charName, String moveName) {
        try {
            // Load move from package
            movePath = "/live/inasociety/moves/" + charName + "/" + moveName;
            InputStream moveInputStream = MoveLoader.class.getResourceAsStream(movePath);

            // Write move data to memory
            byte[] moveData = moveInputStream.readAllBytes();
            moveInputStream.close();
            StringBuilder moveDataString = new StringBuilder();

            // Create string version of move data
            for (byte b : moveData) {
                char c = (char) b;
                moveDataString.append(c);
            }

            // Format move data string
            moveDataString = new StringBuilder(moveDataString.toString().replaceAll("\n", ""));
            String[] moveDataSections = moveDataString.toString().split("]");

            for (int i = 0; i < moveDataSections.length; i++) {
                moveDataSections[i] = moveDataSections[i].replaceAll("\\[", "");
            }

            MoveFrame[] moveFrames = new MoveFrame[moveDataSections.length];
            int startUp = 1;
            int activeFrames = 0;
            int endLag = 0;

            for (String section : moveDataSections) {
                if (section.isBlank()) {
                    System.out.printf("Error: blank section in %s\n", movePath);
                }

                if (section.charAt(0) != hitChar) {
                    if (activeFrames == 0) {
                        startUp ++;
                    }
                    else {
                        endLag ++;
                    }
                }
                else {
                    // Frame with at least one hitbox
                    // It also probably has hurtboxes!
                    // Set up lists of boxes
                    ArrayList<HitBox> hitBoxes = new ArrayList<>();
                    ArrayList<HurtBox> hurtBoxes = new ArrayList<>();

                    // Parse through the boxes (in string form)
                    String[] sectionBoxes = section.split(",");
                    for (String sectionBox : sectionBoxes) {
                        String[] subSection = sectionBox.split(" ");

                        // Assign relevant data
                        char boxIdentifier = subSection[0].charAt(0);
                        int x1 = Integer.parseInt(subSection[1]);
                        int y1 = Integer.parseInt(subSection[2]);
                        int x2 = Integer.parseInt(subSection[3]);
                        int y2 = Integer.parseInt(subSection[4]);
                        int width = x2 - x1;
                        int height = y2 - y1;

                        HitBox hitBox;
                        HurtBox hurtBox;

                        if (boxIdentifier == hitChar) {
                            // hitboxes have an additional field
                            // Initialize the hitbox based on parsed data
                            int power = Integer.parseInt(subSection[5]);
                            hitBox = new HitBox(
                                    new double[] {x1, y1},
                                    new double[] {width, height},
                                    power,
                                    10
                            );
                            hitBoxes.add(hitBox);
                        }
                        else if (boxIdentifier == hurtChar) {
                            // Initialize hurtbox based on parsed data
                            hurtBox = new HurtBox(
                                    new double[] {x1, y1},
                                    new double[] {width, height},
                                    false
                            );
                            hurtBoxes.add(hurtBox);
                        }
                        else {
                            System.out.println("Invalid box identifier in " + movePath);
                        }
                    }
                    moveFrames[activeFrames] = new MoveFrame(hitBoxes, hurtBoxes);
                    activeFrames ++;
                }
            }

            System.out.printf("loaded move info: %s %s %s %s\n", moveName, startUp, activeFrames, endLag);
            return new Move(moveName, startUp, activeFrames, endLag, moveFrames);

        } catch (IOException e) {
            System.out.println("Error loading move " + movePath + " Falling back to default move");
            return loadMove("unknown", "Default");
        } catch (NullPointerException ne) {
            System.out.println(movePath + " Does not exist! Falling back to default move");
            return loadMove("unknown", "Default");
        }
    }
}

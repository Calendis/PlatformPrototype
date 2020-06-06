package live.inasociety.arena;

import live.inasociety.arena.Arena;
import live.inasociety.arena.ArenaBody;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ArenaLoader {
    // Define the arena keywords for the arena files
    private static final String doneKW = "DONE";
    private static final String commentKW = "COMMENT:";
    private static final String metaKW = "META";
    private static final String titleKW = "TITLE";
    private static final String sizeKW = "SIZE";
    private static final String platKW = "PLATFORM";
    private static final String widthKW = "W";
    private static final String heightKW = "H";
    private static final String horizCentreKW = "HC";
    private static final String vertCentreKW = "VC";
    private static final String rKW = "R";
    private static final String gKW = "G";
    private static final String bKW = "B";
    private static final String wallTypeKW = "TYPE";

    private static String[] validSections = {"META", "PLATFORM", "SIZE"};

    public static Arena loadArena(String arenaFileName) {
        try {
            // Convert the name of the arena into a proper file path
            arenaFileName = "./resources/arenas/" + arenaFileName+".saf";

            File arenaFile = new File(arenaFileName);
            BufferedReader br = new BufferedReader(new FileReader(arenaFile));

            // Set up arena defaults
            ArrayList<ArenaBody> arenaBodies = new ArrayList<ArenaBody>();
            String arenaTitle = "MISSING NAME";
            int arenaWidth = 1000;
            int arenaHeight = 1000;
            int platformWidth = 0;
            int platformHeight = 0;
            int platformHorizontalCentre = 0;
            int platformVerticalCentre = 0;
            int platformR = 0;
            int platformG = 0;
            int platformB = 0;
            WallType wallType = WallType.INTANGIBLE;

            // Read the arena file and create the arena
            String currentLine;
            boolean inSection = false;
            String currentSection = null;
            while ((currentLine = br.readLine()) != null) {
                if (currentLine.isEmpty()) {
                    // Skip blank lines
                    continue;
                }
                String[] splitLine = currentLine.split(" ");

                // Are we not in a section?
                if (!inSection) {
                    // Are we entering a valid section?
                    if (Arrays.asList(validSections).contains(splitLine[0])) {
                        inSection = true;
                        currentSection = splitLine[0];
                    }
                    else {
                        throw new IOException("No valid section");
                    }
                }
                else {
                    // We're in a section, which one?
                    switch (currentSection) {
                        case metaKW:
                            switch (splitLine[0]) {
                                case doneKW:
                                    inSection = false;
                                    break;
                                case titleKW:
                                    arenaTitle = splitLine[1];
                                    break;
                                case commentKW:
                                    break;
                                default:
                                    throw new IOException("Invalid meta subsection");
                            }
                            break;
                        case platKW:
                            switch (splitLine[0]) {
                                case doneKW:
                                    ArenaBody arenaBody = new ArenaBody(platformWidth, platformHeight, platformHorizontalCentre, platformVerticalCentre,
                                            platformR, platformG, platformB, wallType);
                                    arenaBodies.add(arenaBody);
                                    inSection = false;
                                    break;
                                case widthKW:
                                    platformWidth = Integer.parseInt(splitLine[1]);
                                    break;
                                case heightKW:
                                    platformHeight = Integer.parseInt(splitLine[1]);
                                    break;
                                case horizCentreKW:
                                    platformHorizontalCentre = Integer.parseInt(splitLine[1]);
                                    break;
                                case vertCentreKW:
                                    platformVerticalCentre = Integer.parseInt(splitLine[1]);
                                    break;
                                case rKW:
                                    platformR = Integer.parseInt(splitLine[1]);
                                    break;
                                case gKW:
                                    platformG = Integer.parseInt(splitLine[1]);
                                    break;
                                case bKW:
                                    platformB = Integer.parseInt(splitLine[1]);
                                    break;
                                case wallTypeKW:
                                    wallType = wallTypeConversion(splitLine[1]);
                                    break;
                                case commentKW:
                                    break;
                                default:
                                    throw new IOException("Invalid platform subsection");
                            }
                            break;
                        case sizeKW:
                            switch(splitLine[0]) {
                                case doneKW:
                                    inSection = false;
                                    break;
                                case widthKW:
                                    arenaWidth = Integer.parseInt(splitLine[1]);
                                    break;
                                case heightKW:
                                    arenaHeight = Integer.parseInt(splitLine[1]);
                                    break;
                                default:
                                    throw new IOException("Invalid size subsection");
                            }
                            break;
                        default:
                            throw new IOException("In invalid section. THIS SHOULD NEVER APPEAR!");
                    }
                }
            }
            Arena arena = new Arena(arenaTitle, arenaWidth, arenaHeight, arenaBodies);
            return arena;

        } catch (FileNotFoundException fnfE) {
            // Fall back on the default arena if the specified path does not exist
            System.out.println("Error: Arena " + arenaFileName + " not found! Falling back to default arena.");
            return loadArena("default");
        } catch (IOException ioE) {
            System.out.println("Error: Arena " + arenaFileName + " contains errors! Falling back to default arena.");
            ioE.printStackTrace();
            return loadArena("default");
        }
    }

    private static WallType wallTypeConversion(String wallType) {
        switch (wallType) {
            case "FLOOR":
                return WallType.FLOOR;
            case "CEILING":
                return WallType.CEILING;
            case "DROPTHROUGH":
                return WallType.DROPTHROUGH;
            case "INTANGIBLE":
                return WallType.INTANGIBLE;
            default:
                System.out.println("Unknown platform type! Defaulting to intangible");
                return WallType.INTANGIBLE;
        }
    }
}

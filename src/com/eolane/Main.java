//Created by: Petr Pribytkov
//Creation date: 11.09.2019

package com.eolane;

import com.eolane.ini.InitializationXML;
import com.sun.istack.internal.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.nio.file.Path;

public class Main {

    private static final String VERSION = "1.2";

    private static boolean createProgramFile(String fileName, Path programFolderPath){
        fileName = programFolderPath + "\\"+fileName + ".txt";
        File newProgramFile = new File(fileName);

        try{
            if(newProgramFile.exists()){
                System.out.println("File is already created!");
            } else{
                newProgramFile.createNewFile();
            }

            Runtime.getRuntime().exec("explorer "+ newProgramFile);
            return true;
        } catch (IOException exp){
            exp.printStackTrace();
        }

        return false;
    }

    private static boolean isNumber (String value){
        try{
            int a = Integer.parseInt(value);
            return true;
        } catch (NumberFormatException | NullPointerException exp){
            return false;
        }
    }

    private static File folderSelection(List<File> listOfFiles, String folderName){
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        File selectedProgramPath = null;
        String input = "";

        if (listOfFiles.size() == 1){
            selectedProgramPath = listOfFiles.get(0);
        }
        else if(listOfFiles.size() > 1){
            for(int i =0; i < listOfFiles.size(); i++){
                System.out.println( i + " ) " + listOfFiles.get(i).getPath());
            }
            do{
                System.out.println("Please select number which folder do you want to use for `"+ folderName +"`");
                try{
                    input = reader.readLine();
                } catch (IOException exp){
                    exp.printStackTrace();
                }

                if(!isNumber(input)){
                    System.out.println("Should be a number!");
                    input = "-1";
                }


            } while (Integer.parseInt(input) < 0 || Integer.parseInt(input) >= listOfFiles.size());

            selectedProgramPath = listOfFiles.get(Integer.parseInt(input));

            if(!selectedProgramPath.exists()){
                System.out.println("Selected folder is not found! Please check configurations");
                System.exit(0);
            }

        } else if (listOfFiles.size() <= 0) {
            System.out.println("There is no any program folder path in config file!");
            System.exit(0);
        }


        return selectedProgramPath;
    }

    private static void copyFiles(File from, File where, Set<String> components){
        File[] sourceLibraryItems = from.listFiles();
        File[] transferLibraryItems = where.listFiles();
        int copiedDirectories = 0;
        int copiedModels = 0;
        boolean directoryFound = false;

        //Checking if sourceLibraryItems is not null
        assert sourceLibraryItems != null;
        if(sourceLibraryItems.length > 0){
            assert transferLibraryItems != null;
            List<String> newComponentsList = sortingComponents(transferLibraryItems, components);

            if(newComponentsList.size() != 0){
                for(String component : newComponentsList){
                    for(File sourceItem : sourceLibraryItems){
                        if(sourceItem.isDirectory()){
                            if(component.equals(sourceItem.getName())){
                                File directoryToCopy = new File(where + "\\" + sourceItem.getName());
                                try{
                                    Files.copy(sourceItem.toPath(), directoryToCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                    copiedDirectories++;
                                    debug(component + " copied");
                                } catch (IOException exp){
                                    exp.printStackTrace();
                                }
                                File[] listOfFilesInDirectoryToCopy = sourceItem.listFiles();
                                assert listOfFilesInDirectoryToCopy != null;

                                if(listOfFilesInDirectoryToCopy.length != 0){
                                    //File copiedDirectory = new File(where + "\\" + sourceItem.getName() + "\\");
                                    Path copiedDirectory = directoryToCopy.toPath();
                                    for(File fileInDir : listOfFilesInDirectoryToCopy){
                                        try {
                                            Files.copy(fileInDir.toPath(), copiedDirectory.resolve(fileInDir.getName()), StandardCopyOption.REPLACE_EXISTING);
                                            copiedModels++;
                                        } catch (IOException exp){
                                            exp.printStackTrace();
                                        }
                                    }
                                }
                                directoryFound = true;
                                break;
                            }
                        }
                    }
                    if(!directoryFound){
                        System.out.println(component + " is not found in source library");
                    } else {
                        directoryFound = false;
                    }
                }
                System.out.println("Copied directories: "+ copiedDirectories);
                System.out.println("Copied models: "+ copiedModels);
            } else {
                System.out.println("Nothing to copy");
            }
        } else {
            System.out.println("Source library is empty!");
        }
    }

    @NotNull
    private static List<String> sortingComponents (File[] transferLibraryItems, Set<String> components){
        List<String> sortedComponents =  new ArrayList<>();
        boolean duplicate;

        if (transferLibraryItems.length > 1){
            for (String component : components) {
                duplicate = false;
                for (int i = 0; i < transferLibraryItems.length; i++) {
                    if (component.equals(transferLibraryItems[i].getName())) {
                        debug(transferLibraryItems[i].getName() + " already exists");
                        duplicate = true;
                        i = transferLibraryItems.length;
                    }
                }
                if(!duplicate) sortedComponents.add(component);
            }

        } else {
            sortedComponents.addAll(components);
        }
        debug("");
        return sortedComponents;
    }

    private static void debug(String debugText){
        System.out.println(debugText);
    }

    public static void main(String[] args) throws IOException {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        Set<String> componentsList = new HashSet<>();

        boolean programFileCreation = true;

        System.out.println("LibraryTransfer version: "+ VERSION);
        debug("Initialization...");
        HashMap<String, List<File>> directionsInit = new InitializationXML().parse();

        List<File> programFolder = new ArrayList<>(directionsInit.get("ProgramFolder"));
        List<File> sourceDirection = new ArrayList<>(directionsInit.get("SourceDirection"));
        List<File> transferDirections = new ArrayList<>(directionsInit.get("TransferDirection"));
        debug("Initialization completed\n");

        File selectedProgramPath = folderSelection(programFolder, "Program Folder");

        while (programFileCreation){
            String inputCommand;
            System.out.print("Type 'e' to finish creating program files or enter new program name: ");
            inputCommand = inputReader.readLine();

            if(inputCommand.equals("e")){
                programFileCreation = false;
            } else if(createProgramFile(inputCommand, selectedProgramPath.toPath())){
                System.out.println("Program: " + inputCommand + " created");
            } else {
                System.out.println("Cannot create program");
            }

        }

        File selectedTransferFolder = folderSelection (transferDirections, "Transfer Folder");
        File selectedSourceFolder = folderSelection(sourceDirection, "Source Folder");

//        //System.out.print("Please check the path for transfer folder. (Y/N): `"+ selectedTransferFolder.toPath() + "` : ");
//        char letter;
//        do{
//            letter = inputReader.readLine().charAt(0);
//        } while (letter != 'y' && letter != 'n');
//
//        if(letter == 'y'){
//            debug("Copy started");
//        } else {
//            System.out.println("Change the transfer folder");
//            System.exit(0);
//        }

        File[] listOfPrograms = selectedProgramPath.listFiles();

        if(listOfPrograms != null && listOfPrograms.length > 0){
            for(File program : listOfPrograms){
                try{
                    BufferedReader fileReader = new BufferedReader(new FileReader(program));
                    String line;
                    boolean collectingComponents = false;
                    while((line = fileReader.readLine()) != null){
                        if (line.equals("COMP 2")){
                            debug(program.getName());
                            debug("Found components: ");
                            for(String component : componentsList) System.out.println(component);
                            System.out.println("\nComponents value: "+ componentsList.size() + "\n");
                            copyFiles(selectedSourceFolder, selectedTransferFolder,  componentsList);
                            break;
                        } else if(line.equals("COMP 1")){
                            collectingComponents = true;
                        } else if (collectingComponents){
                            String[] lineWithComponent = line.split(" ");
                            componentsList.add(lineWithComponent[5]);
                        }
                    }
                    fileReader.close();
                } catch (FileNotFoundException exp){
                    System.out.println("Program `"+ program.getName() + "` not found!");
                }
                debug("");
            }
        } else {
            System.out.println("No programs in the program folder!");
        }
    }
}
